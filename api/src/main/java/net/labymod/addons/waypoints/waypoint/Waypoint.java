package net.labymod.addons.waypoints.waypoint;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.world.object.WorldObject;
import net.labymod.api.util.Color;

public interface Waypoint extends WorldObject {

  WaypointMeta meta();

  default Component title() {
    return this.meta().getTitle();
  }

  default Color color() {
    return this.meta().getColor();
  }

  default WaypointType type() {
    return this.meta().getType();
  }

  @Override
  default boolean isSeeThrough() {
    return true;
  }
}
