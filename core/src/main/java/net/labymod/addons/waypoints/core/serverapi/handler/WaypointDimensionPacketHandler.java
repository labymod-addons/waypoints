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
