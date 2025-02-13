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
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.event.client.world.DimensionChangeEvent;
import net.labymod.api.event.client.world.WorldEnterEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;

public class ServerWaypointListener {

  private final WaypointService waypointService;

  public ServerWaypointListener() {
    this.waypointService = Waypoints.references().waypointService();
  }

  @Subscribe
  public void reloadWaypoints(ServerJoinEvent event) {
    this.waypointService.setCurrentDimension();
    this.waypointService.refresh();
  }

  @Subscribe
  public void reloadWaypoints(WorldEnterEvent event) {
    this.waypointService.setCurrentDimension();
    this.waypointService.refresh();
  }

  @Subscribe
  public void reloadWaypoints(DimensionChangeEvent event) {
    this.waypointService.setDimension(event.toDimension());
    this.waypointService.refresh();
  }

  @Subscribe
  public void reloadWaypoints(SubServerSwitchEvent event) {
    this.waypointService.setCurrentDimension();
    this.waypointService.refresh();
  }

  @Subscribe
  public void clearWaypointsCache(ServerDisconnectEvent event) {
    this.clearTemporaryWaypoints();
  }

  @Subscribe
  public void clearWaypointsCache(WorldLeaveEvent event) {
    this.clearTemporaryWaypoints();
  }

  private void clearTemporaryWaypoints() {
    this.waypointService.remove(
        waypoint -> waypoint.type() == WaypointType.SERVER_SESSION
    );

    this.waypointService.setWaypointsRenderCache(true);
  }
}
