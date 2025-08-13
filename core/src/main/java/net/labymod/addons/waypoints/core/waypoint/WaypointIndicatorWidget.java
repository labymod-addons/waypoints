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

import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.util.bounds.ModifyReason;

public class WaypointIndicatorWidget extends IconWidget {

  private final Waypoint waypoint;

  public WaypointIndicatorWidget(Waypoint waypoint) {
    super(waypoint.meta().icon());
    this.waypoint = waypoint;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    float size = 13;
    this.bounds().setSize(
        size,
        size,
        ModifyReason.of(Waypoint.class, "waypoint")
    );

    this.color().set(this.waypoint.meta().color().get());
  }
}
