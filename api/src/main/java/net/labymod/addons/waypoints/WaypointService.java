package net.labymod.addons.waypoints;

import java.util.Collection;
import java.util.function.Predicate;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface WaypointService {

  Collection<Waypoint> getAllWaypoints();

  void addWaypoint(Waypoint waypoint);

  /**
   * @return {@code true} if the waypoint has been removed from the config, {@code false} otherwise.
   * It may still be removed from the {@link #getAllWaypoints() waypoints} in memory.
   */
  boolean removeWaypoint(Waypoint waypoint);

  void removeWaypoints(Predicate<Waypoint> predicate);
}
