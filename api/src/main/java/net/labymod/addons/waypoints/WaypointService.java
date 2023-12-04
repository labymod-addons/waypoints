package net.labymod.addons.waypoints;

import java.util.Collection;
import java.util.function.Predicate;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface WaypointService {

  void refreshWaypoints();

  void addWaypoint(WaypointMeta meta);

  /**
   * @return {@code true} if the waypoint has been removed from the config, {@code false} otherwise.
   * It may still be removed from the {@link #getAllWaypoints() waypoints} in memory.
   */
  boolean removeWaypoint(WaypointMeta meta);

  void removeWaypoints(Predicate<Waypoint> predicate);

  Waypoint getWaypoint(WaypointMeta meta);

  Collection<Waypoint> getAllWaypoints();

  Collection<Waypoint> getVisibleWaypoints();

  boolean isWaypointsRenderCache();

  void setWaypointsRenderCache(boolean waypointRenderCache);

  String actualWorld();

  String actualServer();

  String actualDimension();

  void setActualDimension(String dimension);
}
