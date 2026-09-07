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
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.event.client.world.DimensionChangeEvent;
import net.labymod.api.event.client.world.WorldEnterEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;
import net.labymod.serverapi.integration.waypoints.packets.WaypointDimensionPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

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

  /**
   * Re-collects all world related state from the client, as if the player had just joined. While
   * the addon is switched off it does not receive any events, so the session waypoints, the
   * dimension override sent by a server and the current dimension may still belong to a server or
   * world the player has left in the meantime. Session waypoints and the dimension override are
   * only kept if they belong to the server the player is currently on. Waypoints have to be
   * refreshed via {@link WaypointService#refresh()} afterwards to apply the changes.
   */
  public void resync() {
    ServerAddress serverAddress = this.waypointService.getServerAddress();
    WaypointContext contextType = serverAddress != null
        ? WaypointContext.MULTI_PLAYER
        : WaypointContext.SINGLE_PLAYER;
    String context = serverAddress != null
        ? serverAddress.toString()
        : this.waypointService.getSinglePlayerWorld();
    boolean ingame = Laby.labyAPI().minecraft().isIngame();

    // Drop the session waypoints of servers the player is no longer on
    this.clearTemporaryWaypoints(
        waypoint -> !ingame || !waypoint.meta().matchesContext(contextType, context)
    );

    // Drop the dimension override of a server the player is no longer on
    if (!Objects.equals(this.packetHandler.getServerAddress(), serverAddress)) {
      this.packetHandler.clear();
    }

    this.updateDimension(null);
  }

  private void clearTemporaryWaypoints() {
    this.clearTemporaryWaypoints(waypoint -> true);
  }

  private void clearTemporaryWaypoints(Predicate<Waypoint> filter) {
    this.waypointService.remove(
        waypoint -> waypoint.type() == WaypointType.SERVER_SESSION && filter.test(waypoint)
    );

    this.waypointService.setWaypointsRenderCache(true);
  }

  private void setDimension() {
    this.setDimension(null);
  }

  private void setDimension(@Nullable ResourceLocation dimension) {
    if (this.updateDimension(dimension)) {
      this.waypointService.refresh();
    }
  }

  /**
   * Applies the given dimension, or the dimension the player is currently in if null, unless a
   * server has forced a dimension until the player disconnects. A dimension override that only
   * lasts until the next dimension change is dropped, also when called from {@link #resync()} as
   * the addon cannot know whether a dimension change was missed while it was switched off.
   *
   * @return false if the forced dimension of the server was kept and nothing changed
   */
  private boolean updateDimension(@Nullable ResourceLocation dimension) {
    String serverDimension = this.packetHandler.getDimension();
    WaypointDimensionPacket.Until serverDimensionUntil = this.packetHandler.getUntil();
    if (serverDimensionUntil != null && serverDimension != null) {
      if (serverDimensionUntil == WaypointDimensionPacket.Until.DISCONNECT) {
        return false;
      }

      this.packetHandler.clear();
    }

    if (dimension == null) {
      this.waypointService.setCurrentDimension();
    } else {
      this.waypointService.setDimension(dimension);
    }

    return true;
  }
}
