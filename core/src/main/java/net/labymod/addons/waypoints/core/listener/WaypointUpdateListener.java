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
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.render.GameRenderEvent;
import net.labymod.api.util.math.position.Position;
import net.labymod.api.util.math.vector.DoubleVector3;

public class WaypointUpdateListener {

  private static final float DEFAULT_SIZE = 1F;
  private static final double TARGET_DISTANCE = 110.0F;
  private final WaypointsAddon addon;
  private final WaypointService waypointService;

  public WaypointUpdateListener(WaypointsAddon addon) {
    this.addon = addon;
    this.waypointService = Waypoints.references().waypointService();

    this.addon.configuration().alwaysShowWaypoints().addChangeListener(
        value -> this.waypointService.setWaypointsRenderCache(false)
    );
  }

  @Subscribe
  public void onGameRender(GameRenderEvent event) {
    ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();

    if (!this.shouldRenderWaypoints(event) || player == null) {
      return;
    }

    Position playerPosition = player.position();
    for (Waypoint waypoint : this.waypointService.getVisible()) {
      WaypointObjectMeta waypointObjectMeta = waypoint.waypointObjectMeta();

      this.updateWaypoint(playerPosition, waypoint, waypointObjectMeta);
    }

    this.waypointService.setWaypointsRenderCache(true);
  }

  private boolean shouldRenderWaypoints(GameRenderEvent event) {
    return Laby.labyAPI().minecraft().isIngame() && event.phase() != Phase.POST;
  }

  private void updateWaypoint(
      Position playerPosition,
      Waypoint waypoint,
      WaypointObjectMeta waypointObjectMeta
  ) {

    DoubleVector3 waypointLocation = waypoint.meta().location();
    DoubleVector3 distanceVec = new DoubleVector3(
        (float) (playerPosition.getX() - waypointLocation.getX()),
        (float) (playerPosition.getY() - waypointLocation.getY()),
        (float) (playerPosition.getZ() - waypointLocation.getZ())
    );

    double distanceToPlayer = distanceVec.length();
    if (false) {
      return;
    }

    waypointObjectMeta.setDistance(distanceToPlayer);
    waypointObjectMeta.setOutOfRange(false);

    if (this.addon.configuration().alwaysShowWaypoints().get()) {
      if (distanceToPlayer <= TARGET_DISTANCE) {
        waypointObjectMeta.setScale(
            (float) (4F * (distanceToPlayer / TARGET_DISTANCE) + DEFAULT_SIZE)
        );

        waypointObjectMeta.pos().set(waypointLocation);
      } else {
        waypointObjectMeta.setScale(5F);
        double normalizationFactor = TARGET_DISTANCE / distanceToPlayer;

        DoubleVector3 newPosition = new DoubleVector3(
            playerPosition.getX() - (distanceVec.getX() * normalizationFactor),
            playerPosition.getY() - (distanceVec.getY() * normalizationFactor),
            playerPosition.getZ() - (distanceVec.getZ() * normalizationFactor)
        );

        waypointObjectMeta.pos().set(newPosition);
      }
    } else {
      waypointObjectMeta.setOutOfRange(distanceToPlayer > TARGET_DISTANCE);
      waypointObjectMeta.setScale(DEFAULT_SIZE);
      waypointObjectMeta.pos().set(waypointLocation);
    }
  }
}
