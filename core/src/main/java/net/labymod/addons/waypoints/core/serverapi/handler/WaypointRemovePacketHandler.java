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
