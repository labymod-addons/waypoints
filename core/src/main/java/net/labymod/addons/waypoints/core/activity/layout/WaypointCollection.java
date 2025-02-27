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
