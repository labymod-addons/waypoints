/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.FloatVector3;

public class WaypointObjectMeta {

  private final WaypointMeta meta;
  private final DoubleVector3 position;
  private float scale;
  private double distanceToPlayer;
  private boolean outOfRange;
  private Component cachedTitle;

  public WaypointObjectMeta(WaypointMeta meta) {
    this.scale = 0;

    this.meta = meta;
    this.position = meta.location().copy();
  }

  public DoubleVector3 pos() {
    return this.position;
  }

  /**
   * @deprecated use {@link #pos()} instead
   */
  @Deprecated
  public FloatVector3 position() {
    return new FloatVector3(
        (float) this.position.getX(),
        (float) this.position.getY(),
        (float) this.position.getZ()
    );
  }

  public float getScale() {
    return this.scale;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public double getDistance() {
    return this.distanceToPlayer;
  }

  public void setDistance(double distanceToPlayer) {
    this.clearTitleCache();
    this.distanceToPlayer = distanceToPlayer;
  }

  @Deprecated
  public void setDistance(float distanceToPlayer) {
    this.clearTitleCache();
    this.distanceToPlayer = distanceToPlayer;
  }

  /**
   * @deprecated use {@link #getDistance()} instead
   */
  @Deprecated
  public float getDistanceToPlayer() {
    return (float) this.distanceToPlayer;
  }

  public boolean isOutOfRange() {
    return this.outOfRange;
  }

  public void setOutOfRange(boolean outOfRange) {
    this.outOfRange = outOfRange;
  }

  public void clearTitleCache() {
    this.cachedTitle = null;
  }

  public Component formatTitle() {
    // TODO more Formatting Options
    if (this.cachedTitle != null) {
      return this.cachedTitle;
    }

    long distanceToPlayer = Math.round(this.distanceToPlayer);

    Component title = this.meta.title().copy();
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
