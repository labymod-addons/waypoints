package net.labymod.addons.waypoints.listener;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.event.client.world.WorldEnterEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;

public class ServerWaypointListener {

  private final WaypointService waypointService;

  public ServerWaypointListener() {
    this.waypointService = Waypoints.getReferences().waypointService();
  }

  @Subscribe
  public void loadWaypoints(ServerJoinEvent event) {
    this.waypointService.refreshWaypoints();
  }

  @Subscribe
  public void loadWaypoints(WorldEnterEvent event) {
    this.waypointService.refreshWaypoints();
  }

  @Subscribe
  public void clearWaypointsCache(ServerDisconnectEvent event) {
    this.clearTemporaryWaypoints();
  }

  @Subscribe
  public void clearWaypointsCache(WorldLeaveEvent event) {
    this.clearTemporaryWaypoints();
  }

  @Subscribe
  public void clearWaypointsCache(SubServerSwitchEvent event) {
    this.waypointService.refreshWaypoints();
  }

  private void clearTemporaryWaypoints() {
    this.waypointService.removeWaypoints(
        waypoint -> waypoint.meta().type() == WaypointType.SERVER_SESSION);
    this.waypointService.setWaypointsRenderCache(true);
  }
}
