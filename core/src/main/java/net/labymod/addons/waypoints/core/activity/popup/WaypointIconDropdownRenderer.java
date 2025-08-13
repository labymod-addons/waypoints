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

import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.renderer.EntryRenderer;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import org.jetbrains.annotations.NotNull;

public class WaypointIconDropdownRenderer implements EntryRenderer<Icon> {

  @Override
  public float getWidth(Icon entry, float maxWidth) {
    return 16;
  }

  @Override
  public float getHeight(Icon entry, float maxWidth) {
    return 16;
  }

  @Override
  public @NotNull Widget createEntryWidget(Icon entry) {
    IconWidget iconWidget = new IconWidget(entry);
    iconWidget.addId("waypoint-icon");
    return iconWidget;
  }

}
