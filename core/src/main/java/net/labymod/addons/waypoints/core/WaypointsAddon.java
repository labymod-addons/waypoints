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

package net.labymod.addons.waypoints.core;

import javax.inject.Singleton;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.listener.ConfigurationVersionUpdateListener;
import net.labymod.addons.waypoints.core.listener.JsonConfigLoaderInitializeListener;
import net.labymod.addons.waypoints.core.listener.ServerWaypointListener;
import net.labymod.addons.waypoints.core.listener.WaypointHotkeyListener;
import net.labymod.addons.waypoints.core.listener.WaypointUpdateListener;
import net.labymod.addons.waypoints.core.waypoint.DefaultWaypointService;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.reference.annotation.Referenceable;

@AddonMain
@Singleton
@Referenceable
public class WaypointsAddon extends LabyAddon<WaypointsConfiguration> {

  @Override
  protected void preConfigurationLoad() {
    this.registerListener(new ConfigurationVersionUpdateListener());
    this.registerListener(new JsonConfigLoaderInitializeListener());
  }

  @Override
  protected void load() {
    // Initialize the Waypoints API as soon as possible
    Waypoints.init(this.referenceStorageAccessor());
  }

  @Override
  protected void enable() {
    this.registerSettingCategory();

    WaypointService waypointService = Waypoints.references().waypointService();
    ((DefaultWaypointService) waypointService).load(this);

    // Apply the current dimension, in case the user is already ingame
    waypointService.setCurrentDimension();

    this.registerListener(new WaypointHotkeyListener(this));
    this.registerListener(new ServerWaypointListener());
    this.registerListener(new WaypointUpdateListener(this));
  }

  @Override
  protected Class<WaypointsConfiguration> configurationClass() {
    return WaypointsConfiguration.class;
  }
}
