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

package net.labymod.addons.waypoints.core.activity.widgets;

import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;

@AutoWidget
public class WaypointListItemWidget extends WaypointWidget {

  private final CheckBoxWidget checkbox;

  public WaypointListItemWidget(WaypointMeta meta, WaypointObjectMeta worldObjectMeta) {
    super(meta, worldObjectMeta);
    this.checkbox = new CheckBoxWidget();
  }

  public WaypointListItemWidget(WaypointMeta meta) {
    this(meta, null);
  }

  @Override
  public void initialize(Parent parent) {
    this.opacity().set(this.meta.isVisible() ? 1F : 0.5F);

    this.checkbox.setState(this.meta.isVisible() ? State.CHECKED : State.UNCHECKED);
    this.checkbox.addId("checkbox");
    this.addContent(this.checkbox);

    super.initialize(parent);
  }

  public CheckBoxWidget getCheckbox() {
    return this.checkbox;
  }

  public WaypointMeta getWaypointMeta() {
    return this.meta;
  }
}