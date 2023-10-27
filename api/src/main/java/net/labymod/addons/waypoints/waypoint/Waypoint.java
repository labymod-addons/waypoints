package net.labymod.addons.waypoints.waypoint;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.world.object.WorldObject;
import net.labymod.api.util.Color;

public interface Waypoint extends WorldObject {

  WaypointMeta meta();

  WaypointObjectMeta waypointObjectMeta();

  default Component title() {
    return this.meta().title();
  }

  default Color color() {
    return this.meta().color();
  }

  default WaypointType type() {
    return this.meta().type();
  }
}
