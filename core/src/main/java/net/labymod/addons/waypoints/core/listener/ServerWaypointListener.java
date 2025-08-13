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
import net.labymod.addons.waypoints.core.serverapi.handler.WaypointDimensionPacketHandler;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.event.client.world.DimensionChangeEvent;
import net.labymod.api.event.client.world.WorldEnterEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;
import net.labymod.serverapi.integration.waypoints.packets.WaypointDimensionPacket;

public class ServerWaypointListener {

  private final WaypointService waypointService;
  private final WaypointDimensionPacketHandler packetHandler;

  public ServerWaypointListener(WaypointDimensionPacketHandler packetHandler) {
    this.waypointService = Waypoints.references().waypointService();
    this.packetHandler = packetHandler;
  }

  @Subscribe
  public void reloadWaypoints(ServerJoinEvent event) {
    this.setDimension();
  }

  @Subscribe
  public void reloadWaypoints(WorldEnterEvent event) {
    this.setDimension();
  }

  @Subscribe
  public void reloadWaypoints(DimensionChangeEvent event) {
    this.setDimension(event.toDimension());
  }

  @Subscribe
  public void reloadWaypoints(SubServerSwitchEvent event) {
    this.setDimension();
  }

  @Subscribe
  public void clearWaypointsCache(ServerDisconnectEvent event) {
    this.clearTemporaryWaypoints();
    this.packetHandler.clear();
    this.waypointService.refresh();
  }

  @Subscribe
  public void clearWaypointsCache(WorldLeaveEvent event) {
    this.clearTemporaryWaypoints();
    this.waypointService.refresh();
  }

  private void clearTemporaryWaypoints() {
    this.waypointService.remove(
        waypoint -> waypoint.type() == WaypointType.SERVER_SESSION
    );

    this.waypointService.setWaypointsRenderCache(true);
  }

  private void setDimension() {
    this.setDimension(null);
  }

  private void setDimension(ResourceLocation dimension) {
    String serverDimension = this.packetHandler.getDimension();
    WaypointDimensionPacket.Until serverDimensionUntil = this.packetHandler.getUntil();
    if (serverDimensionUntil != null && serverDimension != null) {
      if (serverDimensionUntil == WaypointDimensionPacket.Until.DISCONNECT) {
        return;
      }

      this.packetHandler.clear();
    }

    if (dimension == null) {
      this.waypointService.setCurrentDimension();
    } else {
      this.waypointService.setDimension(dimension);
    }

    this.waypointService.refresh();
  }
}
