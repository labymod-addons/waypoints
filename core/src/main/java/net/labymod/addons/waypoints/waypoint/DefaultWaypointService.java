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
  private Collection<Waypoint> waypoints;
  private WaypointsAddon addon;
  private Collection<Waypoint> visibleWaypoints;
  private String actualWorld;
  private String actualServer;
  private byte actualDimension;
  private boolean waypointsRenderCache = false;

  public DefaultWaypointService() {
    this.worldObjectRegistry = Laby.references().worldObjectRegistry();

    this.waypoints = new ArrayList<>();
    this.visibleWaypoints = new ArrayList<>();
  }

  public void load(WaypointsAddon addon) {
    this.addon = addon;

    Collection<Waypoint> newWaypoints = new ArrayList<>();

    for (WaypointMeta meta : this.addon.configuration().getWaypoints()) {
      WaypointObjectMeta waypointObjectMeta = new WaypointObjectMeta(meta);
      Waypoint waypoint = new DefaultWaypoint(this.addon, meta, waypointObjectMeta);

      newWaypoints.add(waypoint);
    }

    this.waypoints = newWaypoints;
  }

  public void refreshWaypoints() {
    Collection<Waypoint> newWaypoints = new ArrayList<>();

    try {
      // When added to API Laby.references().integratedServer().getLocalWorld().folderName()
      this.actualWorld = Laby.references().integratedServer().getLocalWorld().worldName();
    } catch (NullPointerException e) {
      this.actualWorld = null;
    }

    try {
      this.actualServer = Laby.references().serverController().getCurrentServerData().address()
          .toString();
    } catch (NullPointerException e) {
      this.actualServer = "SINGLEPLAYER";
    }

    try {
      this.actualDimension = 0;
      // When added to API Laby.labyAPI().minecraft().clientWorld().dimension()
    } catch (NullPointerException e) {
      this.actualDimension = 0;
    }

    for (Waypoint waypoint : this.waypoints) {
      WaypointMeta meta = waypoint.meta();

      this.removeWaypointFromRegistry(meta);

      if (
          meta.isVisible()
              && (
              meta.server() == null
                  || (meta.server().equals("SINGLEPLAYER") && meta.world().equals(this.actualWorld))
                  || (!meta.server().equals("SINGLEPLAYER") && meta.server()
                  .equals(this.actualServer))
          )
              && meta.dimension() == this.actualDimension
      ) {
        newWaypoints.add(waypoint);
        this.worldObjectRegistry.register(waypoint);
      }
    }

    this.visibleWaypoints = newWaypoints;
    this.setWaypointsRenderCache(false);
  }

  @Override
  public void addWaypoint(WaypointMeta meta) {
    WaypointObjectMeta waypointObjectMeta = new WaypointObjectMeta(meta);
    Waypoint waypoint = new DefaultWaypoint(this.addon, meta, waypointObjectMeta);
    this.waypoints.add(waypoint);

    this.worldObjectRegistry.register(waypoint);

    if (waypoint.meta().type() == WaypointType.PERMANENT) {
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
      if (waypoint.meta().equals(meta)) {
        return waypoint;
      }
    }
    return null;
  }

  @Override
  public Collection<Waypoint> getAllWaypoints() {
    return this.waypoints;
  }

  //*@code \
  @Override
  public Collection<Waypoint> getVisibleWaypoints() {
    return this.visibleWaypoints;
  }

  @Override
  public boolean isWaypointsRenderCache() {
    return this.waypointsRenderCache;
  }

  @Override
  public void setWaypointsRenderCache(boolean waypointsRenderCache) {
    this.waypointsRenderCache = waypointsRenderCache;
  }

  @Override
  public String getActualWorld() {
    return this.actualWorld;
  }

  @Override
  public String getActualServer() {
    return this.actualServer;
  }

  @Override
  public byte getActualDimension() {
    return this.actualDimension;
  }
}
