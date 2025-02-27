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

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.activity.WaypointsActivity;
import net.labymod.addons.waypoints.event.WaypointAddEvent;
import net.labymod.addons.waypoints.event.WaypointRemoveEvent;
import net.labymod.addons.waypoints.utils.Formatting;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.ActivitySettingWidget.ActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.KeybindWidget.KeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.annotation.Exclude;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingRequires;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.event.DefaultCancellable;
import net.labymod.api.util.Color;
import net.labymod.api.util.MethodOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointsConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true)
      .addChangeListener(value -> Waypoints.refresh());

  @SettingSection("Waypoints")
  @KeyBindSetting(acceptMouseButtons = true)
  private final ConfigProperty<Key> permanentHotkey = new ConfigProperty<>(Key.M);

  @KeyBindSetting(acceptMouseButtons = true)
  private final ConfigProperty<Key> editClosestKey = new ConfigProperty<>(Key.NONE);

  @SettingSection("Settings")
  @SwitchSetting
  private final ConfigProperty<Boolean> background = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> icon = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showHudIndicators = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> alwaysShowWaypoints = new ConfigProperty<>(true);

  @SwitchSetting
  @SettingRequires("alwaysShowWaypoints")
  private final ConfigProperty<Boolean> hideWhenOutOfRange = new ConfigProperty<>(true);

  @SliderSetting(min = 128, max = 8192, steps = 128)
  @SettingRequires("hideWhenOutOfRange")
  private final ConfigProperty<Integer> outOfRangeDistance = new ConfigProperty<>(2048);

  @SettingSection("distance")
  @DropdownSetting
  private final ConfigProperty<Formatting> distanceFormatting = ConfigProperty.createEnum(
      Formatting.BRACKETS);

  @ColorPickerSetting
  private final ConfigProperty<Color> distanceBracketColor = new ConfigProperty<>(Color.GRAY);

  @ColorPickerSetting
  private final ConfigProperty<Color> distanceValueColor = new ConfigProperty<>(Color.WHITE);

  @SwitchSetting
  private final ConfigProperty<Boolean> distanceBeforeName = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> hideDistance = new ConfigProperty<>(false);

  @Exclude
  private final List<WaypointMeta> waypoints = new ArrayList<>();

  private final transient List<WaypointMeta> unmodifiableWaypoints = Collections.unmodifiableList(
      this.waypoints);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Key> createKey() {
    return this.permanentHotkey;
  }

  public ConfigProperty<Key> editClosestKey() {
    return this.editClosestKey;
  }

  public ConfigProperty<Boolean> background() {
    return this.background;
  }

  public ConfigProperty<Boolean> icon() {
    return this.icon;
  }

  public ConfigProperty<Boolean> scaleDynamically() {
    return this.alwaysShowWaypoints;
  }

  public ConfigProperty<Boolean> showHudIndicators() {
    return this.showHudIndicators;
  }

  public ConfigProperty<Color> distanceValueColor() {
    return this.distanceValueColor;
  }

  public ConfigProperty<Color> distanceBracketColor() {
    return this.distanceBracketColor;
  }

  public ConfigProperty<Formatting> distanceFormatting() {
    return this.distanceFormatting;
  }

  public ConfigProperty<Boolean> distanceBeforeName() {
    return this.distanceBeforeName;
  }

  public ConfigProperty<Boolean> hideDistance() {
    return this.hideDistance;
  }

  public ConfigProperty<Boolean> hideWhenOutOfRange() {
    return this.hideWhenOutOfRange;
  }

  public ConfigProperty<Integer> outOfRangeDistance() {
    return this.outOfRangeDistance;
  }

  @ActivitySetting
  @MethodOrder(after = "editClosestKey")
  public Activity openWaypoints() {
    return new WaypointsActivity();
  }

  public @Unmodifiable List<WaypointMeta> getWaypoints() {
    return this.waypoints;
  }

  public boolean addWaypoint(WaypointMeta meta) {
    if (this.hasWaypoint(meta)) {
      throw new IllegalArgumentException("A waypoint with this identifier is already registered.");
    }

    DefaultCancellable event = Laby.fireEvent(new WaypointAddEvent(meta));
    if (event.isCancelled()) {
      return false;
    }

    if (meta.type() == WaypointType.PERMANENT) {
      this.waypoints.add(meta);
    }

    return true;
  }

  public boolean removeWaypoint(@NotNull WaypointMeta meta) {
    if (!this.hasWaypoint(meta)) {
      throw new IllegalArgumentException("No waypoint with this identifier is registered.");
    }

    DefaultCancellable event = Laby.fireEvent(new WaypointRemoveEvent(meta));
    if (event.isCancelled()) {
      return false;
    }

    this.waypoints.remove(meta);
    return true;
  }

  public boolean update(@NotNull WaypointMeta meta) {
    int index = -1;
    for (int i = 0; i < this.waypoints.size(); i++) {
      if (this.waypoints.get(i).equals(meta)) {
        index = i;
        break;
      }
    }

    if (index == -1) {
      return false;
    }

    this.waypoints.set(index, meta);
    return true;
  }

  public boolean hasWaypoint(WaypointMeta meta) {
    return this.waypoints.contains(meta);
  }

  @Override
  public int getConfigVersion() {
    return 2;
  }
}
