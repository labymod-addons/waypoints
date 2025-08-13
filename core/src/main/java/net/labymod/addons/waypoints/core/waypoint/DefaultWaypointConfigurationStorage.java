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

import net.labymod.addons.waypoints.WaypointConfigurationStorage;
import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.core.WaypointsConfiguration;
import net.labymod.addons.waypoints.utils.DistanceFormatting;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.Color;

import java.util.function.Consumer;

public class DefaultWaypointConfigurationStorage implements WaypointConfigurationStorage {

  private final WaypointsAddon addon;
  private final DefaultWaypointService waypointService;

  private DistanceFormatting distanceFormatting;
  private TextColor distanceBracketColor;
  private TextColor distanceValueColor;
  private boolean distanceBeforeName;
  private boolean hideDistance;
  private boolean convertToKilometers;
  private int kilometersThreshold;

  protected DefaultWaypointConfigurationStorage(
      WaypointsAddon addon,
      DefaultWaypointService waypointService
  ) {
    this.addon = addon;
    this.waypointService = waypointService;

    WaypointsConfiguration configuration = addon.configuration();
    this.apply(
        configuration.distanceFormatting(),
        formatting -> this.distanceFormatting = formatting
    );
    this.apply(
        configuration.distanceBracketColor(),
        color -> this.distanceBracketColor = this.toTextColor(color)
    );
    this.apply(
        configuration.distanceValueColor(),
        color -> this.distanceValueColor = this.toTextColor(color)
    );
    this.apply(
        configuration.distanceBeforeName(),
        value -> this.distanceBeforeName = value
    );
    this.apply(
        configuration.hideDistance(),
        value -> this.hideDistance = value
    );
    this.apply(
        configuration.convertToKilometers(),
        value -> this.convertToKilometers = value
    );
    this.apply(
        configuration.kilometersThreshold(),
        value -> this.kilometersThreshold = value
    );
  }

  @Override
  public DistanceFormatting distanceFormatting() {
    return this.distanceFormatting;
  }

  @Override
  public TextColor distanceBracketColor() {
    return this.distanceBracketColor;
  }

  @Override
  public TextColor distanceValueColor() {
    return this.distanceValueColor;
  }

  @Override
  public boolean isDistanceBeforeName() {
    return this.distanceBeforeName;
  }

  @Override
  public boolean isHideDistance() {
    return this.hideDistance;
  }

  @Override
  public boolean isConvertToKilometers() {
    return this.convertToKilometers;
  }

  @Override
  public int getKilometersThreshold() {
    return this.kilometersThreshold;
  }

  private <T> void apply(ConfigProperty<T> property, Consumer<T> consumer) {
    consumer.accept(property.get());
    property.addChangeListener((type, oldValue, newValue) -> {
      if (newValue == null) {
        consumer.accept(property.defaultValue());
      } else {
        consumer.accept(newValue);
      }

      for (Waypoint waypoint : this.waypointService.getAll()) {
        waypoint.waypointObjectMeta().clearTitleCache();
      }
    });
  }

  private TextColor toTextColor(Color color) {
    return TextColor.color(color.getValue());
  }
}
