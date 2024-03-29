package net.labymod.addons.waypoints;

import net.labymod.addons.waypoints.api.generated.ReferenceStorage;


public class Waypoints {

  private static ReferenceStorage references;

  public static ReferenceStorage getReferences() {
    return Waypoints.references;
  }

  public static void init(ReferenceStorage references) {
    if (Waypoints.references != null) {
      throw new IllegalStateException("Waypoints already initialized");
    }

    Waypoints.references = references;
  }


  public static void refresh() {
    ReferenceStorage references = getReferences();
    if (references == null) {
      return;
    }

    WaypointService waypointService = references.waypointService();
    if (waypointService == null) {
      return;
    }

    waypointService.refreshWaypoints();
  }
}
