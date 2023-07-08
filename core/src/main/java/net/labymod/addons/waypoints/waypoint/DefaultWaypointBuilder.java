package net.labymod.addons.waypoints.waypoint;

import net.labymod.api.client.component.Component;
import net.labymod.api.models.Implements;
import net.labymod.api.util.Color;
import net.labymod.api.util.debug.Preconditions;
import net.labymod.api.util.math.vector.FloatVector3;

@Implements(WaypointBuilder.class)
public class DefaultWaypointBuilder implements WaypointBuilder {

  private Component title;
  private Color color;
  private WaypointType type;
  private FloatVector3 location;

  @Override
  public WaypointBuilder title(Component title) {
    this.title = title;
    return this;
  }

  @Override
  public WaypointBuilder color(Color color) {
    this.color = color;
    return this;
  }

  @Override
  public WaypointBuilder type(WaypointType type) {
    this.type = type;
    return this;
  }

  @Override
  public WaypointBuilder location(FloatVector3 location) {
    this.location = location;
    return this;
  }

  @Override
  public Waypoint build() {
    Preconditions.notNull(this.title, "Missing title");
    Preconditions.notNull(this.color, "Missing color");
    Preconditions.notNull(this.type, "Missing type");
    Preconditions.notNull(this.location, "Missing location");

    return new DefaultWaypoint(new WaypointMeta(this.title, this.color, this.type, this.location));
  }
}