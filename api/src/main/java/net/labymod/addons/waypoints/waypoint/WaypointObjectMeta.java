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

import net.labymod.addons.waypoints.WaypointConfigurationStorage;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.utils.DistanceFormatting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.FloatVector3;

public class WaypointObjectMeta {

  private static final WaypointConfigurationStorage CONFIGURATION_STORAGE = Waypoints.references()
      .waypointService().configurationStorage();

  private final WaypointMeta meta;
  private final DoubleVector3 position;
  private float scale;
  private double distanceToPlayer;
  private boolean outOfRange;
  private Component cachedTitle;
  private boolean interpolatePosition = false;
  private float alpha = 1.0F;

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
    this.setDistance((double) distanceToPlayer);
  }

  public boolean isInterpolatePosition() {
    return this.interpolatePosition;
  }

  public void setInterpolatePosition(boolean interpolatePosition) {
    this.interpolatePosition = interpolatePosition;
  }

  public float getAlpha() {
    return this.alpha;
  }

  public void setAlpha(float alpha) {
    this.alpha = alpha;
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
    if (this.cachedTitle != null) {
      return this.cachedTitle;
    }

    boolean before = CONFIGURATION_STORAGE.isDistanceBeforeName();
    Component title = Component.text("", TextColor.color(this.meta.color().get()));
    if (before && !CONFIGURATION_STORAGE.isHideDistance()) {
      title.append(this.createDistanceComponent(true, false));
    }

    title.append(this.meta.title());

    if (!before && !CONFIGURATION_STORAGE.isHideDistance()) {
      title.append(this.createDistanceComponent(true, true));
    }

    this.cachedTitle = title;
    return title;
  }

  public Component createDistanceComponent(boolean withSpace, boolean after) {
    long distanceToPlayer = Math.round(this.distanceToPlayer);
    String distanceString;
    if (CONFIGURATION_STORAGE.isConvertToKilometers()
        && distanceToPlayer >= CONFIGURATION_STORAGE.getKilometersThreshold()) {
      double kilometers = distanceToPlayer / 1000D;
      double roundedKilometers;
      if (distanceToPlayer > 2500) {
        roundedKilometers = Math.round(kilometers * 10D) / 10D;
      } else {
        roundedKilometers = Math.round(kilometers * 100D) / 100D;
      }

      distanceString = roundedKilometers + "km";
    } else {
      distanceString = distanceToPlayer + "m";
    }

    Component distanceComponent = Component.text(
        distanceString,
        CONFIGURATION_STORAGE.distanceValueColor()
    );

    Component component = CONFIGURATION_STORAGE.distanceFormatting().build(
        distanceComponent,
        after
    ).color(CONFIGURATION_STORAGE.distanceBracketColor());

    if (withSpace) {
      if (after) {
        component.append(0, DistanceFormatting.space());
      } else {
        component.append(DistanceFormatting.space());
      }
    }

    return component;
  }

  public Component createDistanceComponent() {
    return this.createDistanceComponent(false, true);
  }
}
