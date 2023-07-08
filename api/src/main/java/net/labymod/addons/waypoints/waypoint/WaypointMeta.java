package net.labymod.addons.waypoints.waypoint;

import java.util.Objects;
import net.labymod.api.client.component.Component;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.FloatVector3;

public class WaypointMeta {

  private Component title;
  private Color color;
  private WaypointType type;
  private FloatVector3 location;

  public WaypointMeta(Component title, Color color, WaypointType type, FloatVector3 location) {
    this.title = title;
    this.color = color;
    this.type = type;
    this.location = location;
  }

  public Component getTitle() {
    return this.title;
  }

  public void setTitle(Component title) {
    this.title = title;
  }

  public Color getColor() {
    return this.color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public WaypointType getType() {
    return this.type;
  }

  public void setType(WaypointType type) {
    this.type = type;
  }

  public FloatVector3 getLocation() {
    return this.location;
  }

  public void setLocation(FloatVector3 location) {
    this.location = location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WaypointMeta that = (WaypointMeta) o;
    return Objects.equals(this.title, that.title)
        && Objects.equals(this.color, that.color)
        && this.type == that.type
        && Objects.equals(this.location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.title, this.color, this.type, this.location);
  }
}
