package net.labymod.addons.waypoints.listener;

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

public class GameTickListener {

  private static final float DEFAULT_SIZE = 1F;
  private static final float TARGET_DISTANCE = 110.0F;
  private final WaypointsAddon addon;

  public GameTickListener(WaypointsAddon addon) {
    this.addon = addon;
  }

  @Subscribe
  public void onGameTickChange(GameRenderEvent event) {
    //TODO load waypoints world and dimension dependent

    ClientPlayer PLAYER = Laby.labyAPI().minecraft().getClientPlayer();

    if (!Laby.labyAPI().minecraft().isIngame() || PLAYER == null || event.phase() == Phase.PRE) {
      return;
    }

    FloatVector3 playerPosition = PLAYER.position();

    for (Waypoint waypoint : Waypoints.getReferences().waypointService().getAllWaypoints()) {
      WaypointObjectMeta waypointObjectMeta = waypoint.waypointObjectMeta();

      if (waypoint.meta().isVisible() && waypoint.meta().getWorld().equals("PLACEHOLDER")
          || waypoint.meta().getWorld().equals("LEGACY_WAYPOINT")) {
        float DeltaX = playerPosition.getX() - waypoint.meta().getLocation().getX();
        float DeltaY = playerPosition.getY() - waypoint.meta().getLocation().getY();
        float DeltaZ = playerPosition.getZ() - waypoint.meta().getLocation().getZ();

        float distanceToPlayer = (float) Math.sqrt(
            DeltaX * DeltaX + DeltaY * DeltaY + DeltaZ * DeltaZ);

        if (distanceToPlayer != waypointObjectMeta.getDistanceToPlayer()) {
          waypointObjectMeta.setDistanceToPlayer(distanceToPlayer);
        }

        if (addon.configuration().alwaysShowWaypoints().get()) {
          if (distanceToPlayer <= TARGET_DISTANCE) {
            waypointObjectMeta.setScale(
                4F * (waypointObjectMeta.getDistanceToPlayer() / TARGET_DISTANCE) + DEFAULT_SIZE);
          } else {
            waypointObjectMeta.setScale(5F);
            float normalizationFactor = TARGET_DISTANCE / waypointObjectMeta.getDistanceToPlayer();

            float newX = playerPosition.getX() - (DeltaX * normalizationFactor);
            float newY = playerPosition.getY() - (DeltaY * normalizationFactor);
            float newZ = playerPosition.getZ() - (DeltaZ * normalizationFactor);

            waypointObjectMeta.getLocation().setX(newX);
            waypointObjectMeta.getLocation().setY(newY);
            waypointObjectMeta.getLocation().setZ(newZ);
          }
        } else {
          waypointObjectMeta.setScale(DEFAULT_SIZE);
          waypointObjectMeta.getLocation().set(waypoint.meta().getLocation());
        }
      } else {
        waypointObjectMeta.setScale(0F);
      }
    }
  }

}