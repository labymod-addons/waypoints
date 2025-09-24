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
import net.labymod.addons.waypoints.core.waypoint.DefaultWaypoint;
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

  private static final float DEFAULT_SIZE = .7F;
  private static final int TARGET_DISTANCE = 128;
  private static final int FADE_OUT_DISTANCE = 8;
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
    boolean update =
        !this.waypointService.isWaypointsRenderCache() || x != this.prevX || y != this.prevY
            || z != this.prevZ;
    this.prevX = x;
    this.prevY = y;
    this.prevZ = z;

    int renderDistance = Laby.labyAPI().minecraft().options().getActualRenderDistance() * 16;
    float partialTicks = event.getPartialTicks();
    for (Waypoint waypoint : this.waypointService.getVisible()) {
      if (update) {
        this.updateWaypoint(
            x,
            y,
            z,
            waypoint,
            renderDistance
        );
      }

      WaypointObjectMeta objectMeta = waypoint.waypointObjectMeta();
      if (!objectMeta.isOutOfRange() && waypoint instanceof DefaultWaypoint defaultWaypoint) {
        DoubleVector3 pos = objectMeta.pos();
        DoubleVector3 previousPosition = waypoint.previousPosition();
        boolean hasPrevPosition = defaultWaypoint.hasPrevPosition();
        if (hasPrevPosition
            && previousPosition.getX() == pos.getX()
            && previousPosition.getY() == pos.getY()
            && previousPosition.getZ() == pos.getZ()) {
          continue;
        }

        defaultWaypoint.applyPreviousPosition();
        if (defaultWaypoint.waypointObjectMeta().isInterpolatePosition() && hasPrevPosition) {
          double previousX = previousPosition.getX();
          double previousY = previousPosition.getY();
          double previousZ = previousPosition.getZ();

          waypoint.position().set(
              MathHelper.lerp(pos.getX(), previousX, partialTicks),
              MathHelper.lerp(pos.getY(), previousY, partialTicks),
              MathHelper.lerp(pos.getZ(), previousZ, partialTicks)
          );
        } else {
          waypoint.position().set(pos);
        }
      }
    }

    if (update) {
      this.waypointService.setWaypointsRenderCache(true);
    }
  }

  private void updateWaypoint(
      double playerX,
      double playerY,
      double playerZ,
      Waypoint waypoint,
      int renderDistance
  ) {
    DoubleVector3 location = waypoint.meta().location();
    double distanceX = playerX - location.getX();
    double distanceY = playerY - location.getY();
    double distanceZ = playerZ - location.getZ();

    double distanceSquared = MathHelper.square(distanceX)
        + MathHelper.square(distanceY)
        + MathHelper.square(distanceZ);

    double distanceToPlayer = Math.sqrt(distanceSquared);
    WaypointObjectMeta waypointObjectMeta = waypoint.waypointObjectMeta();
    waypointObjectMeta.setDistance(distanceToPlayer);

    if (!this.scaleDynamically) {
      waypointObjectMeta.setOutOfRange(distanceToPlayer > renderDistance);
      if (!waypointObjectMeta.isOutOfRange()
          && distanceToPlayer > renderDistance - FADE_OUT_DISTANCE) {
        float alpha = (float) (1
            - (distanceToPlayer - (renderDistance - FADE_OUT_DISTANCE)) / FADE_OUT_DISTANCE);
        waypointObjectMeta.setAlpha(alpha);
      } else {
        waypointObjectMeta.setAlpha(1.0F);
      }

      waypointObjectMeta.setScale(DEFAULT_SIZE);
      waypointObjectMeta.pos().set(location);
      waypointObjectMeta.setInterpolatePosition(false);
      if (waypoint instanceof DefaultWaypoint defaultWaypoint) {
        defaultWaypoint.setHasPrevPosition(false);
      }

      return;
    }

    waypointObjectMeta.setAlpha(1.0F);
    if (this.hideWhenOutOfRange) {
      if (distanceToPlayer > this.outOfRangeDistance) {
        waypointObjectMeta.setOutOfRange(true);
        waypointObjectMeta.pos().set(location);
        waypointObjectMeta.setInterpolatePosition(false);
        if (waypoint instanceof DefaultWaypoint defaultWaypoint) {
          defaultWaypoint.setHasPrevPosition(false);
        }

        return;
      }

      if (this.fadeOut && distanceToPlayer > this.outOfRangeDistance - FADE_OUT_DISTANCE) {
        float alpha = (float) (1
            - (distanceToPlayer - (this.outOfRangeDistance - FADE_OUT_DISTANCE))
            / FADE_OUT_DISTANCE);
        waypointObjectMeta.setAlpha(alpha);
      }
    }

    int targetDistance = TARGET_DISTANCE;
    float scale;
    if (distanceToPlayer < 10) {
      scale = DEFAULT_SIZE;
    } else if (distanceToPlayer < targetDistance) {
      distanceToPlayer -= 10;
      final float normalizedDistance = (float) (distanceToPlayer / targetDistance);
      float factor = 5 + 2 * normalizedDistance;
      scale = DEFAULT_SIZE + factor * normalizedDistance;
    } else {
      scale = 7;
    }

    waypointObjectMeta.setScale(scale);
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
