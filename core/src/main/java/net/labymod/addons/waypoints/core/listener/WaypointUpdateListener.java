/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.labymod.addons.waypoints.core.listener;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.core.WaypointsConfiguration;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.render.GameRenderEvent;
import net.labymod.api.util.math.MathHelper;
import net.labymod.api.util.math.position.Position;
import net.labymod.api.util.math.vector.DoubleVector3;

import java.util.function.Consumer;

public class WaypointUpdateListener {

  private static final float DEFAULT_SIZE = 1F;
  private static final double TARGET_DISTANCE = 128;
  private final WaypointService waypointService;

  private double prevX = 0;
  private double prevY = 0;
  private double prevZ = 0;

  private boolean scaleDynamically;
  private boolean hideWhenOutOfRange;
  private double outOfRangeDistance;
  private boolean fadeOut;

  public WaypointUpdateListener(WaypointsAddon addon) {
    this.waypointService = Waypoints.references().waypointService();

    WaypointsConfiguration configuration = addon.configuration();
    this.apply(
        configuration.scaleDynamically(),
        value -> this.scaleDynamically = value
    );

    this.apply(
        configuration.hideWhenOutOfRange(),
        value -> this.hideWhenOutOfRange = value
    );

    this.apply(
        configuration.outOfRangeDistance(),
        value -> this.outOfRangeDistance = value
    );

    this.apply(
        configuration.fadeOut(),
        value -> this.fadeOut = value
    );
  }

  @Subscribe
  public void onGameRender(GameRenderEvent event) {
    Minecraft minecraft = Laby.labyAPI().minecraft();
    ClientPlayer player = minecraft.getClientPlayer();
    if (player == null || !minecraft.isIngame() || event.phase() != Phase.PRE) {
      return;
    }

    Position playerPosition = player.position();
    double x = playerPosition.getX();
    double y = playerPosition.getY() + player.getEyeHeight();
    double z = playerPosition.getZ();
    if (this.waypointService.isWaypointsRenderCache()
        && x == this.prevX
        && y == this.prevY
        && z == this.prevZ) {
      return;
    }

    this.prevX = x;
    this.prevY = y;
    this.prevZ = z;

    for (Waypoint waypoint : this.waypointService.getVisible()) {
      this.updateWaypoint(
          x,
          y,
          z,
          waypoint.meta().location(),
          waypoint.waypointObjectMeta()
      );
    }

    this.waypointService.setWaypointsRenderCache(true);
  }

  private void updateWaypoint(
      double playerX,
      double playerY,
      double playerZ,
      DoubleVector3 location,
      WaypointObjectMeta waypointObjectMeta
  ) {
    double distanceXSquared = MathHelper.square(playerX - location.getX());
    double distanceYSquared = MathHelper.square(playerY - location.getY());
    double distanceZSquared = MathHelper.square(playerZ - location.getZ());
    double distanceSquared = distanceXSquared + distanceYSquared + distanceZSquared;
    double distanceToPlayer = Math.sqrt(distanceSquared);
    waypointObjectMeta.setDistance(distanceToPlayer);

    if (!this.scaleDynamically) {
      waypointObjectMeta.setOutOfRange(distanceToPlayer > TARGET_DISTANCE);
      if (!waypointObjectMeta.isOutOfRange() && distanceToPlayer > TARGET_DISTANCE - 32) {
        float alpha = (float) (1 - (distanceToPlayer - (TARGET_DISTANCE - 32)) / 32);
        waypointObjectMeta.setAlpha(alpha);
      } else {
        waypointObjectMeta.setAlpha(1.0F);
      }

      waypointObjectMeta.setScale(DEFAULT_SIZE);
      return;
    }

    waypointObjectMeta.setAlpha(1.0F);
    if (this.hideWhenOutOfRange) {
      if (distanceToPlayer > this.outOfRangeDistance) {
        waypointObjectMeta.setOutOfRange(true);
        return;
      }

      if (this.fadeOut && distanceToPlayer > this.outOfRangeDistance - 32) {
        float alpha = (float) (1 - (distanceToPlayer - (this.outOfRangeDistance - 32)) / 32);
        waypointObjectMeta.setAlpha(alpha);
      }
    }

    final float normalizedDistance = (float) (distanceToPlayer / TARGET_DISTANCE);
    float factor = 4 + (distanceToPlayer >= TARGET_DISTANCE ? 1 : 1 * normalizedDistance);
    waypointObjectMeta.setScale(factor * normalizedDistance + DEFAULT_SIZE);
    waypointObjectMeta.setOutOfRange(false);
  }

  private <T> void apply(ConfigProperty<T> property, Consumer<T> consumer) {
    consumer.accept(property.get());
    property.addChangeListener(newValue -> {
      consumer.accept(newValue);
      this.waypointService.setWaypointsRenderCache(false);
    });
  }
}
