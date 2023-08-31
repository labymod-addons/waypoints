package net.labymod.addons.waypoints.listener;

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.math.vector.FloatVector3;

public class GameTickListener {

  //TODO SMOOTHER ANIMATIONS
  private WaypointsAddon addon;
  private final float DEFAULT_SIZE = 1.5F;
  private final float TARGET_DISTANCE = 160.0F;

  public GameTickListener(WaypointsAddon addon) {
    this.addon = addon;
  }

  @Subscribe
  public void onGameTickChange(GameTickEvent event) {
    if (!addon.configuration().enabled().get())
      return;
    ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
    if (player == null)
      return;

    FloatVector3 playerPosition = Laby.labyAPI().minecraft().getClientPlayer().getPosition();

    for(WaypointMeta waypoint : addon.configuration().getWaypoints()) {
      WaypointObjectMeta waypointObjectMeta = Waypoints.getWaypointObjects().get(waypoint);

      float DeltaX = playerPosition.getX() - waypoint.getLocation().getX();
      float DeltaY = playerPosition.getY() - waypoint.getLocation().getY();
      float DeltaZ = playerPosition.getZ() - waypoint.getLocation().getZ();

      float distanceToPlayer = (float) Math.sqrt(DeltaX * DeltaX + DeltaY * DeltaY + DeltaZ * DeltaZ);

      waypointObjectMeta.setDistanceToPlayer(distanceToPlayer);

      if (!addon.configuration().alwaysShowWaypoints().get()) {
          waypointObjectMeta.getLocation().setX(waypoint.getLocation().getX());
          waypointObjectMeta.getLocation().setY(waypoint.getLocation().getY());
          waypointObjectMeta.getLocation().setZ(waypoint.getLocation().getZ());

          waypointObjectMeta.setMarkerScale(DEFAULT_SIZE);
          waypointObjectMeta.setScale(DEFAULT_SIZE);
      } else {
        float normalizationFactor = TARGET_DISTANCE / waypointObjectMeta.getDistanceToPlayer();
        waypointObjectMeta.setMarkerScale(0);

        float newX = playerPosition.getX() - (DeltaX * normalizationFactor);
        float newY = playerPosition.getY() - (DeltaY * normalizationFactor);
        float newZ = playerPosition.getZ() - (DeltaZ * normalizationFactor);

        if (distanceToPlayer <= TARGET_DISTANCE) {
          waypointObjectMeta.setScale(8.5F * (waypointObjectMeta.getDistanceToPlayer() / TARGET_DISTANCE) + DEFAULT_SIZE);
        } else {
          waypointObjectMeta.setScale(10F);
          waypointObjectMeta.getLocation().setX(newX);
          waypointObjectMeta.getLocation().setY(newY);
          waypointObjectMeta.getLocation().setZ(newZ);
        }
      }
    }
  }
}
