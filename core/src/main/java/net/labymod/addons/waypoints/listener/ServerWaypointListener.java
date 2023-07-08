package net.labymod.addons.waypoints.listener;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;

public class ServerWaypointListener {

  @Subscribe
  public void clearTemporaryWaypoints(ServerDisconnectEvent event) {
    this.clearTemporaryWaypoints();
  }

  @Subscribe
  public void clearTemporaryWaypoints(WorldLeaveEvent event) {
    this.clearTemporaryWaypoints();
  }

  private void clearTemporaryWaypoints() {
    WaypointService waypointService = Waypoints.getReferences().waypointService();

    waypointService.removeWaypoints(waypoint -> waypoint.type() == WaypointType.SERVER_SESSION);
  }
}
