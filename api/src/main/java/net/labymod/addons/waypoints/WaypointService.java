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

package net.labymod.addons.waypoints;

import net.labymod.addons.waypoints.event.RefreshWaypointsEvent;
import net.labymod.addons.waypoints.event.WaypointAddEvent;
import net.labymod.addons.waypoints.event.WaypointInitializeEvent;
import net.labymod.addons.waypoints.event.WaypointRemoveEvent;
import net.labymod.addons.waypoints.event.WaypointVisibleEvent;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.reference.annotation.Referenceable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Referenceable
public interface WaypointService {

  @Deprecated
  String SINGLELAYER_SERVER = "SINGLEPLAYER";

  WaypointConfigurationStorage configurationStorage();

  /**
   * Refreshes {@link #getVisible()} based on {@link #getDimension()} and the current server address
   * or single player world. Fires {@link WaypointVisibleEvent} for each waypoint that passed the
   * checks and {@link RefreshWaypointsEvent} when done.
   */
  void refresh();

  /**
   * Adds the provided waypoint to the world & the config (if the type demands it). Also fires
   * {@link WaypointAddEvent} & {@link WaypointInitializeEvent}.
   *
   * @param meta the waypoint to add
   * @return the added waypoint or {@code null} if the waypoint was not added
   * @throws IllegalArgumentException if the identifier is already taken
   */
  @Nullable Waypoint add(@NotNull WaypointMeta meta);

  /**
   * Removes the provided waypoint from the world & the config. Also fires
   * {@link WaypointRemoveEvent} before being removed.
   *
   * @return {@code true} if the waypoint has been removed, {@code false} otherwise.
   * @throws IllegalArgumentException if no waypoint with the provided meta is registered
   */
  boolean remove(@NotNull WaypointMeta meta);

  /**
   * Updates the provided waypoint in the world & the config. Also fires
   * {@link WaypointInitializeEvent}.
   *
   * @param meta the waypoint to update
   * @return the added waypoint or {@code null} if the waypoint was not updated
   */
  @Nullable Waypoint update(@NotNull WaypointMeta meta);

  /**
   * Removes all waypoints that match the provided predicate from the world & the config. Also fires
   * {@link WaypointRemoveEvent} for each waypoint before being removed
   *
   * @param predicate the predicate to match
   * @return {@code true} if at least one waypoint has been removed, {@code false} otherwise.
   */
  boolean remove(@NotNull Predicate<Waypoint> predicate);

  /**
   * Gets the waypoint with the provided identifier.
   *
   * @param identifier the identifier of the waypoint
   * @return the registered waypoint with the provided identifier or {@code null} if no waypoint was
   * found
   */
  @Nullable Waypoint get(@NotNull String identifier);

  /**
   * Gets the waypoint with the provided meta.
   *
   * @param meta the meta of the waypoint
   * @return the registered waypoint with the provided meta or {@code null} if no waypoint was found
   */
  default @Nullable Waypoint get(@NotNull WaypointMeta meta) {
    return this.get(meta.getIdentifier());
  }

  /**
   * @return an unmodifiable list of all registered waypoints (that passed
   * {@link WaypointInitializeEvent} during startup).
   */
  @Unmodifiable
  @NotNull List<Waypoint> getAll();

  /**
   * @return an unmodifiable set of all currently visible waypoints (that passed
   * {@link WaypointInitializeEvent} during startup and {@link WaypointVisibleEvent} during the last
   * {@link #refresh()}).
   */
  @Unmodifiable
  @NotNull Set<Waypoint> getVisible();

  boolean isWaypointsRenderCache();

  void setWaypointsRenderCache(boolean waypointRenderCache);

  /**
   * @return the current single player world that {@link #getVisible()} was last populated with by
   * {@link #refresh()}. Returns {@code null} if the player was not in a single player world or
   * {@link #getVisible()} was not populated yet.
   */
  @Nullable String getSinglePlayerWorld();

  /**
   * @return the current single player world that {@link #getVisible()} was last populated with by
   * {@link #refresh()}. Returns {@code null} if the player was not on a multi-player server or
   * {@link #getVisible()} was not populated yet.
   */
  @Nullable ServerAddress getServerAddress();

  /**
   * Gets the waypoint dimension
   *
   * @return the dimension
   */
  @Nullable String getDimension();

  /**
   * Sets the waypoint dimension. Waypoints have to be refreshed via {@link #refresh()} to actually
   * apply the change.
   *
   * @param dimension the dimension to set
   */
  void setDimension(@NotNull String dimension);

  /**
   * Sets the waypoint dimension based on the provided {@link ResourceLocation}. Waypoints have to
   * be refreshed via {@link #refresh()} to actually apply the change.
   *
   * @param dimension the dimension to set
   */
  default void setDimension(@NotNull ResourceLocation dimension) {
    this.setDimension(dimension.toString());
  }

  /**
   * Checks if the given identifier is available.
   *
   * @param identifier the identifier to check
   * @return {@code true} if the identifier is available, {@code false} otherwise
   */
  boolean isIdentifierAvailable(@NotNull String identifier);

  /**
   * Generates a new and available unique identifier with the optional given prefix.
   *
   * @param prefix the prefix of the identifier
   * @return a unique and unused identifier
   */
  String generateUniqueIdentifier(@Nullable String prefix);

  /**
   * Generates a new and available unique identifier without prefix.
   *
   * @return a unique and unused identifier
   */
  default String generateUniqueIdentifier() {
    return this.generateUniqueIdentifier(null);
  }

  /**
   * Sets the waypoint dimension based on the dimension the player is currently in. Will reset the
   * dimension if the player is not ingame. Waypoints have to be refreshed via {@link #refresh()} to
   * actually apply the change.
   */
  default void setCurrentDimension() {
    this.setDimension(Laby.labyAPI().minecraft().clientWorld().dimension());
  }

  @Deprecated
  default void setActualDimension(String dimension) {
    this.setDimension(dimension);
  }

  @Deprecated
  default String actualDimension() {
    return this.getDimension();
  }

  /**
   * @deprecated use {@link #refresh()} instead
   */
  @Deprecated
  default void refreshWaypoints() {
    this.refresh();
  }

  /**
   * @deprecated use {@link #add(WaypointMeta)} instead
   */
  @Deprecated
  default void addWaypoint(WaypointMeta meta) {
    this.add(meta);
  }

  /**
   * @deprecated use {@link #remove(WaypointMeta)} instead
   */
  @Deprecated
  default boolean removeWaypoint(WaypointMeta meta) {
    return this.remove(meta);
  }

  /**
   * @deprecated use {@link #remove(Predicate)} instead
   */
  @Deprecated
  default void removeWaypoints(Predicate<Waypoint> predicate) {
    this.remove(predicate);
  }

  /**
   * @deprecated use {@link #get(WaypointMeta)} or {@link #get(String)} instead
   */
  @Deprecated
  default Waypoint getWaypoint(WaypointMeta meta) {
    return this.get(meta);
  }

  /**
   * @deprecated use {@link #getAll()} instead
   */
  @Deprecated
  default Collection<Waypoint> getAllWaypoints() {
    return this.getAll();
  }

  /**
   * @deprecated use {@link #getVisible()} instead
   */
  @Deprecated
  default Collection<Waypoint> getVisibleWaypoints() {
    return this.getVisible();
  }

  /**
   * @deprecated use {@link #getSinglePlayerWorld()} instead
   */
  @Deprecated
  default @Nullable String getWorld() {
    return this.getSinglePlayerWorld();
  }

  /**
   * @deprecated use {@link #getServerAddress()} instead
   */
  @Deprecated
  default @NotNull String getServer() {
    if (this.getServerAddress() == null) {
      return SINGLELAYER_SERVER;
    }

    return this.getServerAddress().toString();
  }
}
