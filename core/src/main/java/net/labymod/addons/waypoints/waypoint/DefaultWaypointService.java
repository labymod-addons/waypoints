package net.labymod.addons.waypoints.waypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import javax.inject.Singleton;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.api.Laby;
import net.labymod.api.client.network.server.ServerData;
import net.labymod.api.client.world.object.WorldObjectRegistry;
import net.labymod.api.models.Implements;
import net.labymod.api.server.LocalWorld;

@Singleton
@Implements(WaypointService.class)
public class DefaultWaypointService implements WaypointService {

  private final WorldObjectRegistry worldObjectRegistry;
  private Collection<Waypoint> waypoints;
  private WaypointsAddon addon;
  private Collection<Waypoint> visibleWaypoints;
  private String actualWorld;
  private String actualServer;
  private String actualDimension;
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
    this.refreshWaypoints();
  }

  public void refreshWaypoints() {
    Collection<Waypoint> newWaypoints = new ArrayList<>();

    LocalWorld localWorld = Laby.references().integratedServer().getLocalWorld();
    ServerData serverData = Laby.references().serverController().getCurrentServerData();

    if (localWorld != null) {
      this.actualWorld = localWorld.folderName();
    } else {
      this.actualWorld = null;
    }

    if (serverData != null) {
      this.actualServer = serverData.address().toString();
    } else {
      this.actualServer = SINGLELAYER_SERVER;
    }

    for (Waypoint waypoint : this.waypoints) {
      WaypointMeta meta = waypoint.meta();

      this.removeWaypointFromRegistry(meta);

      boolean singlePlayer = SINGLELAYER_SERVER.equals(waypoint.meta().getServer());
      if (
          meta.isVisible()
              &&
              (meta.getServer() == null
                  || (singlePlayer && Objects.equals(meta.getWorld(), this.actualWorld))
                  || (!singlePlayer && meta.getServer().equals(this.actualServer))
              )
              && (meta.getDimension() == null || meta.getDimension().equals(this.actualDimension))
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
  public String actualWorld() {
    return this.actualWorld;
  }

  @Override
  public String actualServer() {
    return this.actualServer;
  }

  @Override
  public String actualDimension() {
    return this.actualDimension;
  }

  @Override
  public void setActualDimension(String dimension) {
    this.actualDimension = dimension;
  }
}
