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
package net.labymod.addons.waypoints.core.serverapi.handler;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.serverapi.api.packet.PacketHandler;
import net.labymod.serverapi.integration.waypoints.packets.WaypointDimensionPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaypointDimensionPacketHandler implements PacketHandler<WaypointDimensionPacket> {

  private WaypointDimensionPacket.Until until;
  private String dimension;
  private ServerAddress serverAddress;

  @Override
  public void handle(@NotNull UUID sender, @NotNull WaypointDimensionPacket packet) {
    WaypointService waypointService = Waypoints.references().waypointService();
    String dimension = packet.getDimension();
    if (dimension == null) {
      this.clear();
      waypointService.setCurrentDimension();
    } else {
      this.dimension = dimension;
      this.until = packet.until();
      this.serverAddress = waypointService.getServerAddress();
      waypointService.setDimension(dimension);
    }

    waypointService.refresh();
  }

  public @Nullable WaypointDimensionPacket.Until getUntil() {
    return this.until;
  }

  public @Nullable String getDimension() {
    return this.dimension;
  }

  /**
   * @return the address of the server the current dimension override was received from, or null
   * if there is no override
   */
  public @Nullable ServerAddress getServerAddress() {
    return this.serverAddress;
  }

  public void clear() {
    this.dimension = null;
    this.until = null;
    this.serverAddress = null;
  }
}
