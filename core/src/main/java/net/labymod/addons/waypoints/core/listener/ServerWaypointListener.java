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
import net.labymod.addons.waypoints.event.RefreshWaypointsEvent;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.api.client.network.server.ServerData;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.event.client.world.DimensionChangeEvent;
import net.labymod.api.event.client.world.WorldEnterEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;
import net.labymod.serverapi.integration.waypoints.packets.WaypointDimensionPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Predicate;

public class ServerWaypointListener {

  private final WaypointService waypointService;
  private final WaypointDimensionPacketHandler packetHandler;

  // The world the waypoints were last refreshed for, see onTick
  private WorldIdentity lastRefreshedWorld;

  public ServerWaypointListener(WaypointDimensionPacketHandler packetHandler) {
    this.waypointService = Waypoints.references().waypointService();
    this.packetHandler = packetHandler;
    this.lastRefreshedWorld = WorldIdentity.current(this.waypointService);
  }

  /**
   * Refreshes the waypoints whenever the world the player is in changed without the events below
   * noticing. The hashed seed is the only way to tell apart several worlds behind the same server
   * address, but nothing fires when only the seed changes: a proxy switching the backend via a
   * respawn packet fires no event at all, and {@link SubServerSwitchEvent} is not guaranteed to
   * fire after LabyMod has captured the seed of the new login packet. Comparing a few values per
   * tick is cheaper and more robust than tracking every path a world change can take.
   */
  @Subscribe
  public void onTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    if (!WorldIdentity.current(this.waypointService).equals(this.lastRefreshedWorld)) {
      this.waypointService.refresh();
    }
  }

  /**
   * Every refresh, no matter who triggered it, records the world it ran for so that
   * {@link #onTick} only refreshes again once the world changes afterwards.
   */
  @Subscribe
  public void onRefresh(RefreshWaypointsEvent event) {
    this.lastRefreshedWorld = WorldIdentity.current(this.waypointService);
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

  /**
   * The values that decide which waypoints {@link WaypointService#refresh()} shows, apart from the
   * dimension, which is tracked by the events above. {@link ServerData} is compared by identity as
   * LabyMod keeps the same instance for the whole connection, including sub-server switches.
   */
  private record WorldIdentity(
      boolean ingame,
      @Nullable ServerData serverData,
      OptionalLong hashedSeed
  ) {

    static WorldIdentity current(WaypointService waypointService) {
      return new WorldIdentity(
          Laby.labyAPI().minecraft().isIngame(),
          Laby.references().serverController().getCurrentServerData(),
          waypointService.currentHashedSeed()
      );
    }
  }
}
