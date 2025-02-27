package net.labymod.addons.waypoints.core.serverapi.handler;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.client.component.Component;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.serverapi.api.packet.PacketHandler;
import net.labymod.serverapi.integration.waypoints.model.ServerWaypoint;
import net.labymod.serverapi.integration.waypoints.packets.WaypointPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WaypointPacketHandler implements PacketHandler<WaypointPacket> {

  @Override
  public void handle(@NotNull UUID sender, @NotNull WaypointPacket packet) {
    for (ServerWaypoint waypoint : packet.getWaypoints()) {
      try {
        this.addServerWaypoint(waypoint);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    Waypoints.references().waypointService().refresh();
  }

  private void addServerWaypoint(ServerWaypoint waypoint) {
    if (!waypoint.isValid()) {
      return;
    }

    WaypointBuilder builder = WaypointBuilder.create()
        .identifier("server_" + waypoint.getId())
        .title(Component.text(waypoint.getName()))
        .type(WaypointType.SERVER_SESSION)
        .applyCurrentContext()
        .location(new DoubleVector3(waypoint.getX(), waypoint.getY(), waypoint.getZ()));

    if (waypoint.getDimension() != null) {
      builder.dimension(waypoint.getDimension());
    } else {
      builder.currentDimension();
    }

    if (waypoint.getColor() != null) {
      builder.color(Color.of(waypoint.getColor()));
    }

    if (waypoint.getIcon() != null) {
      ServerWaypoint.ServerWaypointIconType serverWaypointIconType = waypoint.iconType();
      if (serverWaypointIconType == ServerWaypoint.ServerWaypointIconType.URL) {
        builder.icon(WaypointIcon.createCustom(waypoint.getIcon(), waypoint.getIcon()));
      } else {
        builder.icon(WaypointIcon.get(waypoint.getIcon()));
      }
    }

    WaypointService waypointService = Waypoints.references().waypointService();
    WaypointMeta meta = builder.build();

    Waypoint existingWaypoint = waypointService.get(meta.getIdentifier());
    if (existingWaypoint != null) {
      if (existingWaypoint.meta().type() == WaypointType.SERVER_SESSION) {
        waypointService.update(meta);
        return;
      }

      throw new IllegalStateException(
          "Waypoint identifier " + meta.getIdentifier() + " already in use"
      );
    }

    waypointService.add(meta);
  }
}
