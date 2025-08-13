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

package net.labymod.addons.waypoints.core.waypoint;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.api.models.Implements;
import net.labymod.api.util.Color;
import net.labymod.api.util.debug.Preconditions;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.NotNull;

@Implements(WaypointBuilder.class)
public class DefaultWaypointBuilder implements WaypointBuilder {

  private String identifier;
  private Component title;
  private Color color = Color.WHITE;
  private Icon icon = WaypointIcon.DEFAULT;
  private WaypointType type;
  private DoubleVector3 location;
  private boolean visible = true;
  private String dimension;
  private WaypointContext contextType;
  private String context;

  @Override
  public @NotNull WaypointBuilder identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder title(Component title) {
    this.title = title;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder color(Color color) {
    this.color = color;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder icon(Icon icon) {
    this.icon = icon;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder type(WaypointType type) {
    this.type = type;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder location(DoubleVector3 location) {
    this.location = location;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder visible(boolean visible) {
    this.visible = visible;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder singlePlayer(String world) {
    this.contextType = world == null ? null : WaypointContext.SINGLE_PLAYER;
    this.context = world;
    return this;
  }

  @Override
  public @NotNull WaypointBuilder server(ServerAddress server) {
    this.contextType = server == null ? null : WaypointContext.MULTI_PLAYER;
    this.context = server == null ? null : server.toString();
    return this;
  }

  @Override
  public @NotNull WaypointBuilder dimension(String dimension) {
    this.dimension = dimension;
    return this;
  }

  @Override
  public @NotNull WaypointMeta build() {
    WaypointService waypointService = Waypoints.references().waypointService();
    if (this.identifier == null) {
      this.identifier = waypointService.generateUniqueIdentifier();
    }

    Preconditions.notNull(this.title, "Missing title");
    Preconditions.notNull(this.color, "Missing color");
    Preconditions.notNull(this.type, "Missing type");
    Preconditions.notNull(this.location, "Missing location");
    Preconditions.notNull(this.icon, "Missing icon");
    Preconditions.notNull(this.dimension, "Missing dimension");
    if (this.context == null || this.contextType == null) {
      throw new NullPointerException("Missing server or world");
    }

    return new WaypointMeta(
        this.identifier,
        this.title,
        this.color,
        this.type,
        this.location,
        this.contextType,
        this.context,
        this.icon,
        this.dimension,
        this.visible
    );
  }
}
