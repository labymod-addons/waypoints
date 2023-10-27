package net.labymod.addons.waypoints.listener;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.render.GameRenderEvent;
import net.labymod.api.util.math.vector.FloatVector3;
import org.jetbrains.annotations.NotNull;

public class WaypointUpdateListener {

  private static final float DEFAULT_SIZE = 1F;
  private static final float TARGET_DISTANCE = 110.0F;
  private final WaypointsAddon addon;
  private final WaypointService waypointService;
  private boolean alwaysShowWaypoints;

  public WaypointUpdateListener(WaypointsAddon addon) {
    this.addon = addon;
    this.waypointService = Waypoints.getReferences().waypointService();
    this.alwaysShowWaypoints = addon.configuration().alwaysShowWaypoints().get();
  }

  @Subscribe
  public void tick(GameRenderEvent event) {
    ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();

    if (!this.shouldRenderWaypoints(event, player)) {
      return;
    }

    @NotNull FloatVector3 playerPosition = player.position();

    for (Waypoint waypoint : this.waypointService.getVisibleWaypoints()) {
      WaypointObjectMeta waypointObjectMeta = waypoint.waypointObjectMeta();

      this.updateWaypoint(playerPosition, waypoint, waypointObjectMeta);
    }

    if (!this.waypointService.isWaypointsRenderCache()
        && this.alwaysShowWaypoints == this.addon.configuration().alwaysShowWaypoints().get()) {
      this.waypointService.setWaypointsRenderCache(true);
    } else if (this.alwaysShowWaypoints != this.addon.configuration().alwaysShowWaypoints().get()) {
      this.alwaysShowWaypoints = this.addon.configuration().alwaysShowWaypoints().get();
      this.waypointService.setWaypointsRenderCache(false);
    }
  }

  private boolean shouldRenderWaypoints(GameRenderEvent event, ClientPlayer player) {
    return Laby.labyAPI().minecraft().isIngame() && player != null && event.phase() != Phase.POST;
  }

  private void updateWaypoint(FloatVector3 playerPosition, Waypoint waypoint,
      WaypointObjectMeta waypointObjectMeta) {
    float deltaX = playerPosition.getX() - waypoint.meta().location().getX();
    float deltaY = playerPosition.getY() - waypoint.meta().location().getY();
    float deltaZ = playerPosition.getZ() - waypoint.meta().location().getZ();

    float distanceToPlayer = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

    if (distanceToPlayer == waypointObjectMeta.getDistanceToPlayer()
        && this.waypointService.isWaypointsRenderCache()) {
      return;
    }

    waypointObjectMeta.setDistanceToPlayer(distanceToPlayer);

    if (this.addon.configuration().alwaysShowWaypoints().get()) {
      if (distanceToPlayer <= TARGET_DISTANCE) {
        waypointObjectMeta.setScale(4F * (distanceToPlayer / TARGET_DISTANCE) + DEFAULT_SIZE);
        waypointObjectMeta.getLocation().set(waypoint.meta().location());
      } else {
        waypointObjectMeta.setScale(5F);
        float normalizationFactor = TARGET_DISTANCE / distanceToPlayer;

        float newX = playerPosition.getX() - (deltaX * normalizationFactor);
        float newY = playerPosition.getY() - (deltaY * normalizationFactor);
        float newZ = playerPosition.getZ() - (deltaZ * normalizationFactor);

        waypointObjectMeta.getLocation().set(new FloatVector3(newX, newY, newZ));
      }
    } else {
      waypointObjectMeta.setScale(DEFAULT_SIZE);
      waypointObjectMeta.getLocation().set(waypoint.meta().location());
    }
  }
}
