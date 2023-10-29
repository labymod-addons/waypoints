package net.labymod.addons.waypoints.waypoint;

import net.labymod.api.client.world.object.WorldObject;

public interface Waypoint extends WorldObject {

  WaypointMeta meta();

  WaypointObjectMeta waypointObjectMeta();
}
