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

import java.util.UUID;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.serverapi.api.packet.PacketHandler;
import net.labymod.serverapi.integration.waypoints.model.ServerWaypoint;
import net.labymod.serverapi.integration.waypoints.packets.WaypointPacket;
import org.jetbrains.annotations.NotNull;

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

    String icon = waypoint.getIcon();
    if (icon != null) {
      ServerWaypoint.ServerWaypointIconType type = waypoint.iconType();
      switch (type) {
        case URL -> builder.icon(Icon.url(icon));
        case BUILTIN -> builder.icon(WaypointIcon.getByPath(icon));
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
