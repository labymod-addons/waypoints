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

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.world.object.snapshot.AbstractWorldObjectSnapshot;
import net.labymod.api.laby3d.renderer.snapshot.Extras;

public class WaypointSnapshot extends AbstractWorldObjectSnapshot {

  private final float scale;
  private final int color;
  private final int iconColor;
  private final Icon icon;
  private final Component formattedTitle;
  private final float textWidth;
  private final float textHeight;
  private final float cameraYaw;
  private final float cameraPitch;
  private final boolean beaconBeam;
  private final boolean background;
  private final boolean showIcon;
  private final boolean visible;

  public WaypointSnapshot(
      double x, double y, double z, int lightCoords,
      float scale,
      int color,
      int iconColor,
      Icon icon,
      Component formattedTitle,
      float textWidth,
      float textHeight,
      float cameraYaw,
      float cameraPitch,
      boolean beaconBeam,
      boolean background,
      boolean showIcon,
      boolean visible
  ) {
    super(x, y, z, lightCoords, Extras.empty());
    this.scale = scale;
    this.color = color;
    this.iconColor = iconColor;
    this.icon = icon;
    this.formattedTitle = formattedTitle;
    this.textWidth = textWidth;
    this.textHeight = textHeight;
    this.cameraYaw = cameraYaw;
    this.cameraPitch = cameraPitch;
    this.beaconBeam = beaconBeam;
    this.background = background;
    this.showIcon = showIcon;
    this.visible = visible;
  }

  public float scale() {
    return this.scale;
  }

  public int color() {
    return this.color;
  }

  public int iconColor() {
    return this.iconColor;
  }

  public Icon icon() {
    return this.icon;
  }

  public Component formattedTitle() {
    return this.formattedTitle;
  }

  public float textWidth() {
    return this.textWidth;
  }

  public float textHeight() {
    return this.textHeight;
  }

  public float cameraYaw() {
    return this.cameraYaw;
  }

  public float cameraPitch() {
    return this.cameraPitch;
  }

  public boolean beaconBeam() {
    return this.beaconBeam;
  }

  public boolean background() {
    return this.background;
  }

  public boolean showIcon() {
    return this.showIcon;
  }

  public boolean visible() {
    return this.visible;
  }
}
