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

package net.labymod.addons.waypoints.core.listener;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.core.WaypointsConfiguration;
import net.labymod.addons.waypoints.core.activity.popup.ManageWaypointSimplePopup;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.api.Laby;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;

public class WaypointHotkeyListener {

  private final WaypointsAddon addon;

  public WaypointHotkeyListener(WaypointsAddon addon) {
    this.addon = addon;
  }

  @Subscribe
  public void createWaypoints(KeyEvent event) {
    if (event.state() != State.PRESS
        || Laby.labyAPI().minecraft().minecraftWindow().isScreenOpened()) {
      return;
    }

    WaypointsConfiguration config = this.addon.configuration();
    if (config.createKey().get() == event.key()) {
      new ManageWaypointSimplePopup().displayAsActivity();
      return;
    }

    if (config.editClosestKey().get() != event.key()) {
      return;
    }

    WaypointService waypointService = Waypoints.references().waypointService();
    Waypoint closest = null;
    for (Waypoint waypoint : waypointService.getVisible()) {
      double distance = waypoint.waypointObjectMeta().getDistance();
      if (distance <= 15) {
        if (closest == null) {
          closest = waypoint;
          continue;
        }

        if (distance < closest.waypointObjectMeta().getDistance()) {
          closest = waypoint;
        }
      }
    }

    if (closest == null) {
      //todo i18n
      this.addon.labyAPI().minecraft().chatExecutor().displayClientMessage("No Waypoint to edit "
          + "within 15 blocks found.");
      return;
    }

    new ManageWaypointSimplePopup(closest).displayAsActivity();
  }
}
