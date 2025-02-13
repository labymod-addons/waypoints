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

import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.core.WaypointsConfiguration;
import net.labymod.addons.waypoints.core.activity.WaypointsActivity;
import net.labymod.addons.waypoints.core.activity.WaypointsActivity.Action;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
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

    if (config.permanentHotkey().get() == event.key()) {
      this.createWaypoint(WaypointType.PERMANENT);
    } else if (config.serverHotkey().get() == event.key()) {
      this.createWaypoint(WaypointType.SERVER_SESSION);
    }
  }

  private void createWaypoint(WaypointType type) {
    WaypointsActivity activity = new WaypointsActivity(false);

    activity.setAction(Action.ADD);
    activity.setModifier(meta -> meta.setType(type));
    activity.setManageTitle(Component.translatable("labyswaypoints.gui.create." + switch (type) {
      case PERMANENT -> "permanent";
      case SERVER_SESSION -> "temporary";
      default -> throw new IllegalStateException("Unexpected value: " + type);
    }));

    Laby.labyAPI().minecraft().minecraftWindow().displayScreen(activity);
  }

}
