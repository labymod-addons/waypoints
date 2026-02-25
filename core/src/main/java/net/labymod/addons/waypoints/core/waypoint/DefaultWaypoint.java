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
package net.labymod.addons.waypoints.core.waypoint;

import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.world.ClientWorld;
import net.labymod.api.client.world.object.AbstractWorldObject;
import net.labymod.api.client.world.object.CullVolume;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private static final float HALF_WIDTH = 1.0F;
  private final WaypointObjectMeta waypointObjectMeta;
  private final WaypointMeta meta;
  private final WaypointsAddon addon;
  private final DoubleVector3 prevPosition;
  private boolean hasPrevPosition;

  public DefaultWaypoint(
      WaypointsAddon addon,
      WaypointMeta meta,
      WaypointObjectMeta waypointObjectMeta
  ) {
    super(waypointObjectMeta.pos().copy());
    this.prevPosition = new DoubleVector3();

    this.addon = addon;
    this.waypointObjectMeta = waypointObjectMeta;
    this.meta = meta;
  }

  @Override
  public WaypointMeta meta() {
    return this.meta;
  }

  @Override
  public @NotNull DoubleVector3 previousPosition() {
    return this.prevPosition;
  }

  public boolean hasPrevPosition() {
    return this.hasPrevPosition;
  }

  public void applyPreviousPosition() {
    this.prevPosition.set(this.position());
    this.hasPrevPosition = true;
  }

  public void setHasPrevPosition(boolean hasPrevPosition) {
    this.hasPrevPosition = hasPrevPosition;
  }

  public WaypointsAddon addon() {
    return this.addon;
  }

  @Override
  public @NotNull CullVolume cullVolume() {
    DoubleVector3 pos = this.position();
    ClientWorld level = Laby.references().clientWorld();
    int minBuildHeight = level.getMinBuildHeight();
    int maxBuildHeight = level.getMaxBuildHeight();
    return CullVolume.box(
        pos.getX() - HALF_WIDTH, minBuildHeight, pos.getZ() - HALF_WIDTH,
        pos.getX() + HALF_WIDTH, maxBuildHeight, pos.getZ() + HALF_WIDTH
    );
  }

  @Override
  public WaypointObjectMeta waypointObjectMeta() {
    return this.waypointObjectMeta;
  }

  @Override
  public boolean shouldRenderInOverlay() {
    return this.isEnabled() && this.addon.configuration().showHudIndicators().get();
  }

  @Override
  public @Nullable Widget createWidget() {
    return new WaypointIndicatorWidget(this);
  }

  private boolean isEnabled() {
    return this.addon.configuration().enabled().get();
  }
}