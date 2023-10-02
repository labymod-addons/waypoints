package net.labymod.addons.waypoints.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.labymod.addons.waypoints.WaypointsConfiguration;
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

          waypoint.addProperty("world", "PLACEHOLDER");
          waypoint.addProperty("visible", true);
        }
      }

      event.setJsonObject(json);
    }
  }

}
