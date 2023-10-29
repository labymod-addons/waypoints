package net.labymod.addons.waypoints.listener;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
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
    this.waypointService = Waypoints.getReferences().waypointService();
  }

  @Subscribe
  public void reloadWaypoints(ServerJoinEvent event) {
    this.reloadWaypoints();
  }

  @Subscribe
  public void reloadWaypoints(WorldEnterEvent event) {
    this.reloadWaypoints();
  }

  @Subscribe
  public void reloadWaypoints(DimensionChangeEvent event) {
    this.reloadWaypoints();
  }

  @Subscribe
  public void reloadWaypoints(SubServerSwitchEvent event) {
    this.reloadWaypoints();
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
    this.waypointService.removeWaypoints(
        waypoint -> waypoint.meta().type() == WaypointType.SERVER_SESSION);
    this.waypointService.setWaypointsRenderCache(true);
  }

  private void reloadWaypoints() {
    this.waypointService.setActualDimension(
        Laby.labyAPI().minecraft().clientWorld().dimension().toString());
    this.waypointService.refreshWaypoints();
  }
}
