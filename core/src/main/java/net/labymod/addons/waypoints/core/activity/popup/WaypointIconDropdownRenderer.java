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

package net.labymod.addons.waypoints.core.activity.popup;

import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.renderer.EntryRenderer;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import org.jetbrains.annotations.NotNull;

public class WaypointIconDropdownRenderer implements EntryRenderer<WaypointIcon> {

  @Override
  public float getWidth(WaypointIcon entry, float maxWidth) {
    return entry.getScaledWidth(this.getHeight(entry));
  }

  @Override
  public float getHeight(WaypointIcon entry, float maxWidth) {
    return this.getHeight(entry);
  }

  @Override
  public @NotNull Widget createEntryWidget(WaypointIcon entry) {
    IconWidget iconWidget = new IconWidget(entry.icon()).addId("waypoint-icon-with-vars");
    iconWidget.setVariable("--height", this.getHeight(entry));
    iconWidget.setVariable("--width", entry.getScaledWidth(this.getHeight(entry)));
    return iconWidget;
  }

  private float getHeight(WaypointIcon icon) {
    return Math.min(16, icon.getHeight());
  }
}
