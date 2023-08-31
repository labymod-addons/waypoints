package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.math.vector.FloatVector3;

public class WaypointObjectMeta {
  private float scale;

  private float markerScale;
  private FloatVector3 location;
  private float distanceToPlayer;

  public WaypointObjectMeta(WaypointMeta waypointMeta) {
    this.scale = 1.5f;

    FloatVector3 waypointLocation = new FloatVector3(waypointMeta.getLocation());
    this.location = waypointLocation;
  }

  public float getScale() {
    return this.scale;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public FloatVector3 getLocation() {
    return this.location;
  }

  public void setLocation(FloatVector3 location) {
    this.location = location;
  }

  public float getDistanceToPlayer() {
    return this.distanceToPlayer;
  }

  public void setDistanceToPlayer(float distanceToPlayer) {
    this.distanceToPlayer = distanceToPlayer;
  }

  public float getMarkerScale() {
    return markerScale;
  }

  public void setMarkerScale(float markerScale) {
    this.markerScale = markerScale;
  }

  public Component formatTitle(Component name) {
    //TODO more Formatting Options
    int distanceToPlayer = Math.round(this.distanceToPlayer);

    Component title = name.copy();

    Component bracket1 = Component.text(" [").color(TextColor.color(Colors.BRACKET_COLOR));
    Component formattedDistance = Component.text(distanceToPlayer + "m").color(TextColor.color(Colors.VALUE_COLOR));
    Component bracket2 = Component.text("]").color(TextColor.color(Colors.BRACKET_COLOR));

    title.append(bracket1);
    title.append(formattedDistance);
    title.append(bracket2);

    return title;
  }
}
