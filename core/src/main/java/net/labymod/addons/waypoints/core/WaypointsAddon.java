package net.labymod.addons.waypoints.core;

import javax.inject.Singleton;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.listener.ConfigurationVersionUpdateListener;
import net.labymod.addons.waypoints.core.listener.ServerWaypointListener;
import net.labymod.addons.waypoints.core.listener.WaypointHotkeyListener;
import net.labymod.addons.waypoints.core.listener.WaypointUpdateListener;
import net.labymod.addons.waypoints.core.waypoint.DefaultWaypointService;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@Singleton
@AddonMain
public class WaypointsAddon extends LabyAddon<WaypointsConfiguration> {

  @Override
  protected void preConfigurationLoad() {
    this.registerListener(new ConfigurationVersionUpdateListener());
  }

  @Override
  protected void load() {
    // Initialize the Waypoints API as soon as possible
    Waypoints.init(this.referenceStorageAccessor());
  }

  @Override
  protected void enable() {
    this.registerSettingCategory();

    ((DefaultWaypointService) Waypoints.references().waypointService()).load(this);

    this.registerListener(new WaypointHotkeyListener(this));
    this.registerListener(new ServerWaypointListener());
    this.registerListener(new WaypointUpdateListener(this));
  }

  @Override
  protected Class<WaypointsConfiguration> configurationClass() {
    return WaypointsConfiguration.class;
  }
}
