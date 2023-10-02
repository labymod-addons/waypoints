package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.math.vector.FloatVector3;

public class WaypointObjectMeta {

  private final WaypointMeta meta;
  private final FloatVector3 location;
  private float scale;
  private float distanceToPlayer;
  private Component cachedTitle;

  public WaypointObjectMeta(WaypointMeta meta) {
    this.scale = 0;

    FloatVector3 waypointLocation = new FloatVector3(meta.getLocation());
    this.meta = meta;
    this.location = waypointLocation;
  }

  public FloatVector3 getLocation() {
    return this.location;
  }

  public float getScale() {
    return this.scale;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public float getDistanceToPlayer() {
    return this.distanceToPlayer;
  }

  public void setDistanceToPlayer(float distanceToPlayer) {
    this.clearTitleCache();
    this.distanceToPlayer = distanceToPlayer;
  }

  public void clearTitleCache() {
    this.cachedTitle = null;
  }

  public Component formatTitle() {
    //TODO more Formatting Options
    if (this.cachedTitle != null) {
      return this.cachedTitle;
    }

    int distanceToPlayer = Math.round(this.distanceToPlayer);

    Component title = this.meta.getTitle().copy();
    title.color(TextColor.color(this.meta.getColor().get()));

    Component bracket1 = Component.text(" [");
    bracket1.color(TextColor.color(Colors.BRACKET_COLOR));

    Component formattedDistance = Component.text(distanceToPlayer + "m");
    formattedDistance.color(TextColor.color(Colors.VALUE_COLOR));

    Component bracket2 = Component.text("]");
    bracket2.color(TextColor.color(Colors.BRACKET_COLOR));

    title.append(bracket1);
    title.append(formattedDistance);
    title.append(bracket2);

    this.cachedTitle = title;

    return title;
  }
}
