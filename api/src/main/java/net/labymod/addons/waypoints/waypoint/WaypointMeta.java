package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.client.component.Component;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.FloatVector3;
import org.jetbrains.annotations.Nullable;

public class WaypointMeta {

  @Nullable
  private final String world;
  private final String server;
  private final String dimension;
  private Component title;
  private Color color;
  private WaypointType type;
  private FloatVector3 location;
  private boolean visible;


  public WaypointMeta(Component title, Color color, WaypointType type, FloatVector3 location,
      boolean visible, @Nullable String world, String server, String dimension) {
    this.title = title;
    this.color = color;
    this.type = type;
    this.location = location;
    this.visible = visible;
    this.world = world;
    this.server = server;
    this.dimension = dimension;
  }

  public Component title() {
    return this.title;
  }

  public void setTitle(Component title) {
    this.title = title;
  }

  public Color color() {
    return this.color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public WaypointType type() {
    return this.type;
  }

  public void setType(WaypointType type) {
    this.type = type;
  }

  public FloatVector3 location() {
    return this.location;
  }

  public void setLocation(FloatVector3 location) {
    this.location = location;
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    Waypoints.getReferences().waypointService().refreshWaypoints();
  }

  @Nullable
  public String world() {
    return this.world;
  }

  public String server() {
    return server;
  }

  public String dimension() {
    return dimension;
  }
}
