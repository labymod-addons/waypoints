package net.labymod.addons.waypoints;

import javax.inject.Singleton;
import net.labymod.addons.waypoints.listener.ConfigurationVersionUpdateListener;
import net.labymod.addons.waypoints.listener.ServerWaypointListener;
import net.labymod.addons.waypoints.listener.WaypointHotkeyListener;
import net.labymod.addons.waypoints.listener.WaypointUpdateListener;
import net.labymod.addons.waypoints.waypoint.DefaultWaypointService;
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
  protected void enable() {
    Waypoints.init(this.referenceStorageAccessor());

    this.registerSettingCategory();

    ((DefaultWaypointService) Waypoints.getReferences().waypointService()).load(this);

    this.registerListener(new WaypointHotkeyListener(this));
    this.registerListener(new ServerWaypointListener());
    this.registerListener(new WaypointUpdateListener(this));
  }

  @Override
  protected Class<WaypointsConfiguration> configurationClass() {
    return WaypointsConfiguration.class;
  }
}
