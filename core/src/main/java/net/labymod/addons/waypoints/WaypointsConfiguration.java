package net.labymod.addons.waypoints;

import java.util.ArrayList;
import java.util.Collection;
import net.labymod.addons.waypoints.activity.WaypointsActivity;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.ActivitySettingWidget.ActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.KeybindWidget.KeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.Exclude;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.MethodOrder;

public class WaypointsConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true)
      .addChangeListener(value -> Waypoints.refresh());

  @KeyBindSetting(acceptMouseButtons = true)
  private final ConfigProperty<Key> permanentHotkey = new ConfigProperty<>(Key.M);

  @KeyBindSetting(acceptMouseButtons = true)
  private final ConfigProperty<Key> serverHotkey = new ConfigProperty<>(Key.NONE);

  @SettingSection("Settings")
  @SwitchSetting
  private final ConfigProperty<Boolean> background = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> icon = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> alwaysShowWaypoints = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> showHudIndicators = new ConfigProperty<>(true);

  @Exclude
  private final Collection<WaypointMeta> waypoints = new ArrayList<>();

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Key> permanentHotkey() {
    return this.permanentHotkey;
  }

  public ConfigProperty<Key> serverHotkey() {
    return this.serverHotkey;
  }

  public ConfigProperty<Boolean> background() {
    return this.background;
  }

  public ConfigProperty<Boolean> icon() {
    return this.icon;
  }

  public ConfigProperty<Boolean> alwaysShowWaypoints() {
    return this.alwaysShowWaypoints;
  }

  public ConfigProperty<Boolean> showHudIndicators() {
    return this.showHudIndicators;
  }

  @SettingSection("Waypoints")
  @ActivitySetting
  @MethodOrder(after = "showHudIndicators")
  public Activity openWaypoints() {
    return new WaypointsActivity(true);
  }

  public Collection<WaypointMeta> getWaypoints() {
    return this.waypoints;
  }

  @Override
  public int getConfigVersion() {
    return 2;
  }
}
