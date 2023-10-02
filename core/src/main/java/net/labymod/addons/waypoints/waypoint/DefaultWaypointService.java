package net.labymod.addons.waypoints.waypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import javax.inject.Singleton;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.api.Laby;
import net.labymod.api.client.world.object.WorldObjectRegistry;
import net.labymod.api.models.Implements;

@Singleton
@Implements(WaypointService.class)
public class DefaultWaypointService implements WaypointService {

  private final WorldObjectRegistry worldObjectRegistry;

  private final Collection<Waypoint> waypoints;

  private WaypointsAddon addon;

  public DefaultWaypointService() {
    this.worldObjectRegistry = Laby.references().worldObjectRegistry();

    this.waypoints = new ArrayList<>();
  }

  public void load(WaypointsAddon addon) {
    this.addon = addon;

    for (WaypointMeta meta : this.addon.configuration().getWaypoints()) {
      WaypointObjectMeta waypointObjectMeta = new WaypointObjectMeta(meta);
      Waypoint waypoint = new DefaultWaypoint(this.addon, meta, waypointObjectMeta);

      this.waypoints.add(waypoint);
      this.worldObjectRegistry.register(waypoint);
    }
  }

  @Override
  public Collection<Waypoint> getAllWaypoints() {
    return this.waypoints;
  }

  @Override
  public void addWaypoint(WaypointMeta meta) {
    WaypointObjectMeta waypointObjectMeta = new WaypointObjectMeta(meta);
    Waypoint waypoint = new DefaultWaypoint(this.addon, meta, waypointObjectMeta);
    this.waypoints.add(waypoint);

    this.worldObjectRegistry.register(waypoint);

    if (waypoint.type() == WaypointType.PERMANENT) {
      this.addon.configuration().getWaypoints().add(meta);
      this.addon.saveConfiguration();
    }
  }

  @Override
  public boolean removeWaypoint(WaypointMeta meta) {
    this.removeWaypointFromRegistry(meta);
    this.waypoints.removeIf((Waypoint waypoint) -> waypoint.meta() == meta);

    if (this.addon.configuration().getWaypoints().remove(meta)) {
      this.addon.saveConfiguration();
      return true;
    }

    return false;
  }

  public void removeWaypointFromRegistry(WaypointMeta meta) {
    Waypoint waypoint = this.getWaypoint(meta);
    if (waypoint != null) {
      this.worldObjectRegistry.unregister(v -> v.getValue() == waypoint);
    }
  }

  @Override
  public void removeWaypoints(Predicate<Waypoint> predicate) {
    boolean modified = this.waypoints.removeIf(waypoint -> {
      if (!predicate.test(waypoint)) {
        return false;
      }

      this.worldObjectRegistry.unregister(v -> v.getValue() == waypoint);
      this.addon.configuration().getWaypoints().remove(waypoint.meta());

      return true;
    });

    if (modified) {
      this.addon.saveConfiguration();
    }
  }

  @Override
  public Waypoint getWaypoint(WaypointMeta meta) {
    for (Waypoint waypoint : this.waypoints) {
      if (waypoint.meta() == meta) {
        return waypoint;
      }
    }
    return null;
  }
}
