package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.client.component.Component;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.Nullable;

public class WaypointMeta {

  @Nullable
  private final String world;
  private final String server;
  private final String dimension;
  private Component title;
  private Color color;
  private WaypointType type;
  private DoubleVector3 location;
  private boolean visible;

  public WaypointMeta(
      Component title,
      Color color,
      WaypointType type,
      DoubleVector3 location,
      boolean visible,
      @Nullable String world,
      String server,
      String dimension
  ) {
    this.title = title;
    this.color = color;
    this.type = type;
    this.location = location;
    this.visible = visible;
    this.world = world;
    this.server = server;
    this.dimension = dimension;
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

  public DoubleVector3 getLocation() {
    return this.location;
  }

  public void setLocation(DoubleVector3 location) {
    this.location = location;
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setVisible(boolean visible) {
    if (this.visible != visible) {
      this.visible = visible;
      Waypoints.refresh();
    }
  }

  @Nullable
  public String getWorld() {
    return this.world;
  }

  public String getServer() {
    return this.server;
  }

  public String getDimension() {
    return this.dimension;
  }
}
