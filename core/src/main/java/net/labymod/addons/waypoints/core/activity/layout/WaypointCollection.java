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

package net.labymod.addons.waypoints.core.activity.layout;

import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.api.client.gui.icon.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class WaypointCollection {

  private final List<Object> entries = new ArrayList<>();
  private final Icon icon;
  private final String name;
  private WaypointCollection parent;

  protected WaypointCollection(@NotNull Icon icon, @NotNull String name) {
    this.icon = icon;
    this.name = name;
  }

  public @NotNull Icon icon() {
    return this.icon;
  }

  public @NotNull String getName() {
    return this.name;
  }

  public @NotNull List<Object> getEntries() {
    return this.entries;
  }

  public @Nullable WaypointCollection getParent() {
    return this.parent;
  }

  public boolean hasParent() {
    return this.parent != null;
  }

  public void add(Waypoint waypoint) {
    this.entries.add(waypoint);
  }

  public void add(WaypointCollection collection) {
    if (collection == this) {
      throw new IllegalArgumentException("Cannot add collection to itself");
    }

    this.entries.add(collection);
    collection.parent = this;
  }

  public void sort() {
    this.entries.sort((a, b) -> {
      // order collections first
      if (a instanceof WaypointCollection && b instanceof Waypoint) {
        return -1;
      }

      // order collections first
      if (a instanceof Waypoint && b instanceof WaypointCollection) {
        return 1;
      }

      // order collections by name
      if (a instanceof WaypointCollection collectionA
          && b instanceof WaypointCollection collectionB) {
        return collectionA.getName().compareTo(collectionB.getName());
      }

      // keep waypoint order as is
      return 0;
    });
  }
}
