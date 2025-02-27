package net.labymod.addons.waypoints.core.serverapi.handler;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.serverapi.api.packet.PacketHandler;
import net.labymod.serverapi.integration.waypoints.packets.WaypointDimensionPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaypointDimensionPacketHandler implements PacketHandler<WaypointDimensionPacket> {

  private WaypointDimensionPacket.Until until;
  private String dimension;

  @Override
  public void handle(@NotNull UUID sender, @NotNull WaypointDimensionPacket packet) {
    this.dimension = packet.getDimension();
    this.until = packet.until();
    if (this.dimension == null) {
      this.until = null;
    }

    WaypointService waypointService = Waypoints.references().waypointService();
    if (this.dimension == null) {
      waypointService.setCurrentDimension();
    } else {
      waypointService.setDimension(this.dimension);
    }

    waypointService.refresh();
  }

  public @Nullable WaypointDimensionPacket.Until getUntil() {
    return this.until;
  }

  public @Nullable String getDimension() {
    return this.dimension;
  }

  public void clear() {
    this.dimension = null;
    this.until = null;
  }
}
