package net.labymod.addons.waypoints.waypoint;

import net.labymod.api.client.component.Component;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.FloatVector3;

public class WaypointMeta {

  private Component title;
  private Color color;
  private WaypointType type;
  private FloatVector3 location;
  private boolean visible;
  private String world;


  public WaypointMeta(Component title, Color color, WaypointType type, FloatVector3 location, boolean visible,
      String world) {
    this.title = title;
    this.color = color;
    this.type = type;
    this.location = location;
    this.visible = visible;
    this.world = world;
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
    return type;
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

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public String getWorld() {
    return world;
  }

  public void setWorld(String world) {
    this.world = world;
  }
}
