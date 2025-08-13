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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.labymod.addons.waypoints.core.WaypointsConfiguration;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.labymod.config.ConfigurationVersionUpdateEvent;

public class ConfigurationVersionUpdateListener {

  @Subscribe
  public void onConfigurationVersionUpdate(ConfigurationVersionUpdateEvent event) {
    if (event.getConfigClass() != WaypointsConfiguration.class) {
      return;
    }

    int usedVersion = event.getUsedVersion();
    if (usedVersion < 2) { // Update from version 1 to version 2
      JsonObject json = event.getJsonObject();

      if (json.has("waypoints") && json.get("waypoints").isJsonArray()) {
        for (JsonElement waypointElement : json.getAsJsonArray("waypoints")) {
          if (!waypointElement.isJsonObject()) {
            continue;
          }

          JsonObject waypoint = waypointElement.getAsJsonObject();

          waypoint.addProperty("visible", true);
        }
      }

      event.setJsonObject(json);
    }
  }

}
