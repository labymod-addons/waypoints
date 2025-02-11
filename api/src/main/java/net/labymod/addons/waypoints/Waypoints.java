package net.labymod.addons.waypoints;

import net.labymod.addons.waypoints.api.generated.ReferenceStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Waypoints {

  private static ReferenceStorage references;

  public static @NotNull ReferenceStorage references() {
    if (references == null) {
      throw new IllegalStateException("Waypoints is not initialized yet");
    }

    return Waypoints.references;
  }

  public static void init(ReferenceStorage references) {
    if (Waypoints.references != null) {
      throw new IllegalStateException("Waypoints already initialized");
    }

    Waypoints.references = references;
  }

  public static void refresh() {
    references().waypointService().refreshWaypoints();
  }

  /**
   * @deprecated Use {@link #references()} instead
   */
  @Deprecated
  public static @Nullable ReferenceStorage getReferences() {
    return Waypoints.references;
  }
}
