package net.labymod.addons.waypoints;

import net.labymod.addons.waypoints.api.generated.ReferenceStorage;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import java.util.HashMap;

public class Waypoints {

  private static ReferenceStorage references;
  private static HashMap<WaypointMeta, WaypointObjectMeta> waypointObjects = new HashMap<>();

  public static HashMap<WaypointMeta, WaypointObjectMeta> getWaypointObjects() {
    return waypointObjects;
  }

  public static ReferenceStorage getReferences() {
    return Waypoints.references;
  }

  public static void init(ReferenceStorage references) {
    if (Waypoints.references != null) {
      throw new IllegalStateException("Waypoints already initialized");
    }

    Waypoints.references = references;
  }
}
