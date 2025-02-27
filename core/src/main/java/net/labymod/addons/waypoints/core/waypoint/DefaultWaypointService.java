/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.labymod.addons.waypoints.core.waypoint;

import net.labymod.addons.waypoints.WaypointConfigurationStorage;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.core.WaypointsConfiguration;
import net.labymod.addons.waypoints.event.RefreshWaypointsEvent;
import net.labymod.addons.waypoints.event.WaypointInitializeEvent;
import net.labymod.addons.waypoints.event.WaypointRemoveEvent;
import net.labymod.addons.waypoints.event.WaypointVisibleEvent;
import net.labymod.addons.waypoints.event.WaypointsDimensionChangeEvent;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.network.server.ServerAddress;
import net.labymod.api.client.network.server.ServerData;
import net.labymod.api.client.world.object.WorldObjectRegistry;
import net.labymod.api.event.DefaultCancellable;
import net.labymod.api.generated.ReferenceStorage;
import net.labymod.api.models.Implements;
import net.labymod.api.server.LocalWorld;
import net.labymod.api.util.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Singleton
@Implements(WaypointService.class)
public class DefaultWaypointService implements WaypointService {

  private final WorldObjectRegistry worldObjectRegistry;
  private final List<Waypoint> waypoints = new ArrayList<>();
  private final List<Waypoint> unmodifiableWaypoints = Collections.unmodifiableList(
      this.waypoints
  );
  private final Set<Waypoint> visibleWaypoints = new HashSet<>();
  private final Set<Waypoint> unmodifiableVisibleWaypoints = Collections.unmodifiableSet(
      this.visibleWaypoints
  );
  private final WaypointConfigurationStorage configurationStorage;
  private WaypointsAddon addon;
  private String actualWorld;
  private ServerAddress serverAddress;
  private String dimension;
  private boolean waypointsRenderCache = false;

  public DefaultWaypointService(WaypointsAddon addon) {
    this.worldObjectRegistry = Laby.references().worldObjectRegistry();
    this.configurationStorage = new DefaultWaypointConfigurationStorage(addon, this);
  }

  @Override
  public WaypointConfigurationStorage configurationStorage() {
    return this.configurationStorage;
  }

  public void load(WaypointsAddon addon) {
    this.addon = addon;

    this.waypoints.clear();
    for (WaypointMeta meta : this.addon.configuration().getWaypoints()) {
      Waypoint waypoint = this.initializeWaypoint(meta);
      if (waypoint != null) {
        this.waypoints.add(waypoint);
      }
    }

    this.refresh();
  }

  public void refresh() {
    ThreadSafe.ensureRenderThread();

    this.visibleWaypoints.clear();
    ReferenceStorage references = Laby.references();
    LocalWorld localWorld = references.integratedServer().getLocalWorld();
    ServerData serverData = references.serverController().getCurrentServerData();
    this.actualWorld = localWorld != null ? localWorld.folderName() : null;
    this.serverAddress = serverData != null ? serverData.address() : null;

    WaypointContext targetContext = this.serverAddress != null
        ? WaypointContext.MULTI_PLAYER
        : WaypointContext.SINGLE_PLAYER;
    for (Waypoint waypoint : this.waypoints) {
      WaypointMeta meta = waypoint.meta();
      this.removeWaypointFromRegistry(meta);
      if (!meta.isVisible() || meta.contextType() != targetContext) {
        continue;
      }

      boolean target;
      String context = meta.getContext();
      if (targetContext == WaypointContext.SINGLE_PLAYER) {
        target = Objects.equals(context, this.actualWorld);
      } else {
        target = context.equals(this.serverAddress.toString());
      }

      if (target && (meta.getDimension() == null || meta.getDimension().equals(this.dimension))) {
        if (!Laby.fireEvent(new WaypointVisibleEvent(waypoint)).isCancelled()) {
          this.visibleWaypoints.add(waypoint);
          this.worldObjectRegistry.register(waypoint);
        }
      }
    }

    this.setWaypointsRenderCache(false);
    Laby.fireEvent(new RefreshWaypointsEvent());
  }

  @Override
  public Waypoint add(@NotNull WaypointMeta meta) {
    if (!this.addon.configuration().addWaypoint(meta)) {
      return null;
    }

    Waypoint waypoint = this.initializeWaypoint(meta);
    if (waypoint != null) {
      this.waypoints.add(waypoint);
    }

    this.addon.saveConfiguration();
    return waypoint;
  }

  private @Nullable Waypoint initializeWaypoint(WaypointMeta meta) {
    meta = meta.copy();
    WaypointObjectMeta waypointObjectMeta = new WaypointObjectMeta(meta);
    Waypoint waypoint = new DefaultWaypoint(this.addon, meta, waypointObjectMeta);
    if (Laby.fireEvent(new WaypointInitializeEvent(waypoint)).isCancelled()) {
      return null;
    }

    return waypoint;
  }

  @Override
  public boolean remove(@NotNull WaypointMeta meta) {
    return this.removeInternal(meta, true);
  }

  @Override
  public Waypoint update(@NotNull WaypointMeta meta) {
    if (this.addon.configuration().update(meta)) {
      this.addon.saveConfiguration();
    }

    this.removeWaypointFromRegistry(meta);
    int index = -1;
    Waypoint existingWaypoint = null;
    for (int i = 0; i < this.waypoints.size(); i++) {
      Waypoint waypoint = this.waypoints.get(i);
      if (waypoint.meta().equals(meta)) {
        index = i;
        existingWaypoint = waypoint;
        break;
      }
    }

    Waypoint waypoint = this.initializeWaypoint(meta);
    if (waypoint != null) {
      if (existingWaypoint != null) {
        waypoint.waypointObjectMeta().setDistance(
            existingWaypoint.waypointObjectMeta().getDistance()
        );
      }

      if (index == -1) {
        this.waypoints.add(waypoint);
      } else {
        this.waypoints.set(index, waypoint);
      }
    }

    return waypoint;
  }

  public void removeWaypointFromRegistry(WaypointMeta meta) {
    Waypoint waypoint = this.get(meta);
    if (waypoint != null) {
      this.worldObjectRegistry.unregister(v -> v.getValue() == waypoint);
    }
  }

  @Override
  public boolean remove(@NotNull Predicate<Waypoint> predicate) {
    boolean modified = this.waypoints.removeIf(waypoint -> {
      if (waypoint == null || !predicate.test(waypoint)) {
        return false;
      }

      return this.removeInternal(waypoint.meta(), false);
    });

    if (modified) {
      this.addon.saveConfiguration();
    }

    return true;
  }

  @Override
  public @Nullable Waypoint get(@NotNull String identifier) {
    for (Waypoint waypoint : this.waypoints) {
      if (waypoint.meta().getIdentifier().equals(identifier)) {
        return waypoint;
      }
    }

    return null;
  }

  @Override
  public @NotNull List<Waypoint> getAll() {
    return this.unmodifiableWaypoints;
  }

  @Override
  public @NotNull Set<Waypoint> getVisible() {
    return this.unmodifiableVisibleWaypoints;
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
  public String getSinglePlayerWorld() {
    return this.actualWorld;
  }

  @Override
  public @NotNull ServerAddress getServerAddress() {
    return this.serverAddress;
  }

  @Override
  public @Nullable String getDimension() {
    return this.dimension;
  }

  @Override
  public void setDimension(@NotNull String dimension) {
    if (Objects.equals(this.dimension, dimension)
        || (this.dimension == null && dimension.equals("labymod:unknown"))) {
      return;
    }

    WaypointsDimensionChangeEvent event = Laby.fireEvent(
        new WaypointsDimensionChangeEvent(dimension)
    );

    if (event.isCancelled()) {
      return;
    }

    String eventDimension = event.getDimension();
    this.dimension = eventDimension.equals("labymod:unknown") ? null : eventDimension;
  }

  @Override
  public boolean isIdentifierAvailable(@NotNull String identifier) {
    for (Waypoint waypoint : this.waypoints) {
      if (waypoint.meta().getIdentifier().equals(identifier)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String generateUniqueIdentifier(@Nullable String prefix) {
    String identifier = this.generateIdentifierInternal(prefix);
    while (!this.isIdentifierAvailable(identifier)) {
      identifier = this.generateIdentifierInternal(prefix);
    }

    return identifier;
  }

  private String generateIdentifierInternal(@Nullable String prefix) {
    return prefix == null ? UUID.randomUUID().toString() : prefix + ":" + UUID.randomUUID();
  }

  private boolean removeInternal(WaypointMeta meta, boolean save) {
    WaypointsConfiguration configuration = this.addon.configuration();
    if (configuration.hasWaypoint(meta)) {
      if (!configuration.removeWaypoint(meta)) {
        return false;
      }

      if (save) {
        this.addon.saveConfiguration();
      }
    } else {
      for (Waypoint waypoint : this.waypoints) {
        if (waypoint.meta().equals(meta)) {
          meta = waypoint.meta();
          break;
        }
      }

      if (meta == null) {
        throw new IllegalArgumentException("No waypoint with this identifier is registered.");
      }

      DefaultCancellable event = Laby.fireEvent(new WaypointRemoveEvent(meta));
      if (event.isCancelled()) {
        return false;
      }
    }

    this.removeWaypointFromRegistry(meta);
    @NotNull WaypointMeta finalMeta = meta;
    if (save) {
      this.waypoints.removeIf(waypoint -> waypoint.meta() == finalMeta);
    }

    return true;
  }
}
