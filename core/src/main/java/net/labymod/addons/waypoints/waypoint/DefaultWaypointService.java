package net.labymod.addons.waypoints.waypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import javax.inject.Singleton;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
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
    //TODO load waypoints dimension dependent
    this.addon = addon;

    for (WaypointMeta meta : this.addon.configuration().getWaypoints()) {
      WaypointObjectMeta waypointObjectMeta = new WaypointObjectMeta(meta);
      Waypoints.getWaypointObjects().put(meta, waypointObjectMeta);
      Waypoint waypoint = new DefaultWaypoint(meta);

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
    Waypoints.getWaypointObjects().put(meta, new WaypointObjectMeta(meta));
    Waypoint waypoint = new DefaultWaypoint(meta);
    this.waypoints.add(waypoint);

    this.worldObjectRegistry.register(waypoint);

    if (waypoint.type() == WaypointType.PERMANENT) {
      this.addon.configuration().getWaypoints().add(waypoint.meta());
      this.addon.saveConfiguration();
    }
  }

  @Override
  public boolean removeWaypoint(WaypointMeta meta) {
    removeWaypointFromRegistry(meta);
    this.waypoints.removeIf((Waypoint waypoint) -> waypoint.meta() == meta);

    if (this.addon.configuration().getWaypoints().remove(meta)) {
      this.addon.saveConfiguration();

      return true;
    }
    return false;
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

  private void removeWaypointFromRegistry(WaypointMeta meta) {
    for(Waypoint waypoint : this.waypoints) {
      if(waypoint.meta() == meta) {
        this.worldObjectRegistry.unregister(v -> v.getValue() == waypoint);
      }
    }
  }
}
