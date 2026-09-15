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

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.listener.ConfigurationVersionUpdateListener;
import net.labymod.addons.waypoints.core.listener.JsonConfigLoaderInitializeListener;
import net.labymod.addons.waypoints.core.listener.ServerWaypointListener;
import net.labymod.addons.waypoints.core.listener.WaypointHotkeyListener;
import net.labymod.addons.waypoints.core.listener.WaypointUpdateListener;
import net.labymod.addons.waypoints.core.serverapi.handler.WaypointDimensionPacketHandler;
import net.labymod.addons.waypoints.core.serverapi.handler.WaypointPacketHandler;
import net.labymod.addons.waypoints.core.serverapi.handler.WaypointRemovePacketHandler;
import net.labymod.addons.waypoints.core.waypoint.DefaultWaypoint;
import net.labymod.addons.waypoints.core.waypoint.DefaultWaypointService;
import net.labymod.addons.waypoints.core.waypoint.WaypointSubmitter;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.world.object.WorldObjectDispatcher;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.reference.annotation.Referenceable;
import net.labymod.api.serverapi.LabyModProtocolService;
import net.labymod.api.util.ThreadSafe;
import net.labymod.serverapi.core.AddonProtocol;
import net.labymod.serverapi.integration.waypoints.WaypointsIntegration;
import net.labymod.serverapi.integration.waypoints.packets.WaypointDimensionPacket;
import net.labymod.serverapi.integration.waypoints.packets.WaypointPacket;
import net.labymod.serverapi.integration.waypoints.packets.WaypointRemovePacket;

import javax.inject.Singleton;

@AddonMain
@Singleton
@Referenceable
public class WaypointsAddon extends LabyAddon<WaypointsConfiguration> {

  private ServerWaypointListener serverWaypointListener;
  private boolean externalDevices;

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

    WaypointDimensionPacketHandler dimensionPacketHandler = new WaypointDimensionPacketHandler();
    this.serverWaypointListener = new ServerWaypointListener(dimensionPacketHandler);

    this.registerListener(new WaypointHotkeyListener(this));
    this.registerListener(this.serverWaypointListener);
    this.registerListener(new WaypointUpdateListener(this));

    LabyModProtocolService protocolService = Laby.references().labyModProtocolService();
    WaypointsIntegration integration = protocolService.getOrRegisterIntegration(
        WaypointsIntegration.class,
        WaypointsIntegration::new
    );

    AddonProtocol protocol = integration.waypointsProtocol();
    protocol.registerHandler(
        WaypointPacket.class,
        new WaypointPacketHandler(this, this.serverWaypointListener)
    );
    protocol.registerHandler(WaypointRemovePacket.class, new WaypointRemovePacketHandler());
    protocol.registerHandler(WaypointDimensionPacket.class, dimensionPacketHandler);

    WorldObjectDispatcher dispatcher = Laby.references().worldObjectDispatcher();
    dispatcher.registerSubmitter(DefaultWaypoint.class, new WaypointSubmitter());
  }

    // Let a paired controller (a Stream Deck, the Laby app) set a waypoint through LabyMod's
    // External Devices service. Guarded so the addon still works on client builds that don't ship
    // the API yet; a LinkageError means an older one that has the service but not its control side.
    try {
      Class.forName("net.labymod.api.externaldevice.ExternalDeviceControl");
      ExternalDeviceCommands.register();
      this.externalDevices = true;
    } catch (ClassNotFoundException | LinkageError ignored) {
      this.logger().info(
          "External Devices API not available in this client build, waypoints are not offered to "
              + "paired controllers"
      );
    }
  }

  /**
   * Switched off in the mods menu a listener simply stops receiving events, but the waypoint
   * operation is pulled by the controller, so it has to be taken off the list itself.
   */
  @Override
  protected void onDeactivated() {
    if (this.externalDevices) {
      ExternalDeviceCommands.unregister();
    }
  }

  @Override
  protected void onActivated() {
    if (this.externalDevices) {
      ExternalDeviceCommands.register();
    }

    // No events were received while the addon was switched off, so the world state is stale
    ThreadSafe.executeOnRenderThread(() -> {
      this.serverWaypointListener.resync();
      Waypoints.refresh();
    });
  }

  @Override
  protected Class<WaypointsConfiguration> configurationClass() {
    return WaypointsConfiguration.class;
  }
}
