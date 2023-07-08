package net.labymod.addons.waypoints.listener;

import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.WaypointsConfiguration;
import net.labymod.addons.waypoints.activity.WaypointsActivity;
import net.labymod.addons.waypoints.activity.WaypointsActivity.Action;
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
    activity.setModifier(waypoint -> waypoint.meta().setType(type));
    activity.setManageTitle(Component.translatable("labyswaypoints.gui.create." + switch (type) {
      case PERMANENT -> "permanent";
      case SERVER_SESSION -> "temporary";
    }));

    Laby.labyAPI().minecraft().minecraftWindow().displayScreen(activity);
  }

}
