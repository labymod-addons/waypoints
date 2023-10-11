package net.labymod.addons.waypoints;

import net.labymod.addons.waypoints.api.generated.ReferenceStorage;


public class Waypoints {

  private static boolean waypointsRenderCache = false;

  private static ReferenceStorage references;

  public static boolean isWaypointsRenderCache() {
    return waypointsRenderCache;
  }

  public static void setWaypointsRenderCache(boolean waypointsRenderCache) {
    Waypoints.waypointsRenderCache = waypointsRenderCache;
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
