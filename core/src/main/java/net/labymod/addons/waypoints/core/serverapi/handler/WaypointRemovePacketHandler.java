package net.labymod.addons.waypoints.core.serverapi.handler;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.serverapi.api.packet.PacketHandler;
import net.labymod.serverapi.integration.waypoints.packets.WaypointRemovePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WaypointRemovePacketHandler implements PacketHandler<WaypointRemovePacket> {

  @Override
  public void handle(@NotNull UUID sender, @NotNull WaypointRemovePacket packet) {
    WaypointService waypointService = Waypoints.references().waypointService();
    Waypoint waypoint = waypointService.get("server_" + packet.getId());
    if (waypoint == null || waypoint.meta().type() != WaypointType.SERVER_SESSION) {
      return;
    }

    waypointService.remove(waypoint.meta());
    waypointService.refresh();
  }
}
