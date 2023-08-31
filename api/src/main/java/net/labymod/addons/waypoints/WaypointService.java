package net.labymod.addons.waypoints;

import java.util.Collection;
import java.util.function.Predicate;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface WaypointService {

  Collection<Waypoint> getAllWaypoints();

  void addWaypoint(WaypointMeta waypointMeta);

  /**
   * @return {@code true} if the waypoint has been removed from the config, {@code false} otherwise.
   * It may still be removed from the {@link #getAllWaypoints() waypoints} in memory.
   */
  boolean removeWaypoint(WaypointMeta waypointMeta);

  void removeWaypoints(Predicate<Waypoint> predicate);
}
