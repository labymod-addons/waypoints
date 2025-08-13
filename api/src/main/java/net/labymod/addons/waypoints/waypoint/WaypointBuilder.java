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

package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.api.reference.annotation.Referenceable;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.FloatVector3;
import org.jetbrains.annotations.NotNull;

@Referenceable
public interface WaypointBuilder {

  /**
   * @deprecated use {@link #create()} instead
   */
  @Deprecated
  static WaypointBuilder newBuilder() {
    return Waypoints.references().waypointBuilder();
  }

  /**
   * @return a new instance of the {@link WaypointBuilder}
   */
  static @NotNull WaypointBuilder create() {
    return Waypoints.references().waypointBuilder();
  }

  /**
   * Generates and sets a unique and unused identifier for the waypoint. Default value is a randomly
   * generated identifier.
   *
   * @param prefix the prefix of the identifier
   * @return the builder instance
   */
  default @NotNull WaypointBuilder identifierPrefix(String prefix) {
    return this.identifier(
        Waypoints.references().waypointService().generateUniqueIdentifier(prefix)
    );
  }

  /**
   * Sets the identifier of the waypoint. The identifier is used to identify the waypoint and thus
   * should be unique. Default value is a randomly generated identifier.
   *
   * @param identifier the identifier
   * @return the builder instance
   */
  @NotNull WaypointBuilder identifier(String identifier);

  /**
   * Sets the title of the waypoint.
   *
   * @param title the title
   * @return the builder instance
   */
  @NotNull WaypointBuilder title(Component title);

  /**
   * Sets the color of the waypoint. Default is {@link Color#WHITE}.
   *
   * @param color the color
   * @return the builder instance
   */
  @NotNull WaypointBuilder color(Color color);

  /**
   * Sets the icon of the waypoint. Default is {@link WaypointIcon#DEFAULT}.
   *
   * @param icon the icon
   * @return the builder instance
   */
  @NotNull WaypointBuilder icon(Icon icon);

  /**
   * Sets the {@link WaypointType}. Check {@link WaypointType} for more information on what each
   * type is for.
   *
   * @param type the type
   * @return the builder instance
   */
  @NotNull WaypointBuilder type(WaypointType type);

  /**
   * Sets the location of the waypoint.
   *
   * @param location the location
   * @return the builder instance
   */
  @NotNull WaypointBuilder location(DoubleVector3 location);

  /**
   * Sets whether the waypoint is visible or not. Default is {@code true}.
   *
   * @param visible whether the waypoint is visible or not
   * @return the builder instance
   */
  @NotNull WaypointBuilder visible(boolean visible);

  /**
   * Sets the target world of the waypoint. Will unset everything server related.
   *
   * @param world the target world
   * @return the builder instance
   */
  @NotNull WaypointBuilder singlePlayer(String world);

  /**
   * Sets the target server address of the waypoint. Will unset everything single player related.
   *
   * @param address the target server address
   * @return the builder instance
   */
  @NotNull WaypointBuilder server(ServerAddress address);

  /**
   * Sets the target dimension of the waypoint.
   *
   * @param dimension the target dimension
   * @return the builder instance
   */
  @NotNull WaypointBuilder dimension(String dimension);

  /**
   * Builds the waypoint with the provided information.
   *
   * @return the built waypoint
   * @throws NullPointerException if the title, color, type, location, server or dimension not set
   */
  @NotNull WaypointMeta build();

  /**
   * @deprecated use {@link #location(DoubleVector3)} instead
   */
  @Deprecated
  default @NotNull WaypointBuilder location(FloatVector3 location) {
    return this.location(new DoubleVector3(location.getX(), location.getY(), location.getZ()));
  }

  /**
   * Sets the target server address of the waypoint to the server the player is currently on. Will
   * unset everything single player related.
   *
   * @return the builder instance
   * @throws IllegalStateException if the player is not on a server
   */
  default @NotNull WaypointBuilder server() {
    ServerAddress currentServer = Waypoints.references().waypointService().getServerAddress();
    if (currentServer == null) {
      throw new IllegalStateException("Player is not on a server");
    }

    return this.server(currentServer);
  }


  /**
   * Sets the target world of the waypoint to the single player world the player is currently on.
   * Will unset everything server related.
   *
   * @return the builder instance
   * @throws IllegalStateException if the player is not in single player
   */
  default @NotNull WaypointBuilder singlePlayer() {
    String currentWorld = Waypoints.references().waypointService().getSinglePlayerWorld();
    if (currentWorld == null) {
      throw new IllegalStateException("Player is not in single player");
    }

    return this.singlePlayer(currentWorld);
  }

  /**
   * Sets the current server or the single player world as the context of the waypoint.
   *
   * @return the builder instance
   * @throws IllegalStateException if the player is not in single player or on a server
   */
  default @NotNull WaypointBuilder applyCurrentContext() {
    WaypointService waypointService = Waypoints.references().waypointService();
    if (waypointService.getServerAddress() != null) {
      return this.server(waypointService.getServerAddress());
    }

    String singlePlayerWorld = waypointService.getSinglePlayerWorld();
    if (singlePlayerWorld != null) {
      return this.singlePlayer(singlePlayerWorld);
    }

    throw new IllegalStateException("Player is not in single player or on a server");
  }

  /**
   * Sets tje current dimension as the target dimension of the waypoint.
   *
   * @return the builder instance
   * @throws IllegalStateException if the player is not in a dimension
   */
  default @NotNull WaypointBuilder currentDimension() {
    String dimension = Waypoints.references().waypointService().getDimension();
    if (dimension == null) {
      throw new IllegalStateException("Player is not in a dimension");
    }

    return this.dimension(dimension);
  }

  /**
   * @deprecated use {@link #server()} or {@link #server(ServerAddress)} instead
   */
  @Deprecated
  default @NotNull WaypointBuilder server(String server) {
    return this.server(ServerAddress.parse(server));
  }

  /**
   * @deprecated use {@link #singlePlayer(String)} instead
   */
  @Deprecated
  default @NotNull WaypointBuilder world(String world) {
    return this.singlePlayer(world);
  }
}
