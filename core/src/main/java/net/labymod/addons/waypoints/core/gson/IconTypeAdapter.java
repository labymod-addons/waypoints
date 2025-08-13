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

package net.labymod.addons.waypoints.core.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.gson.LabyGsonTypeAdapter;

public class IconTypeAdapter extends LabyGsonTypeAdapter<Icon> {

  @Override
  public Icon deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
  ) throws JsonParseException {
    if (json.isJsonObject()) {
      JsonObject object = json.getAsJsonObject();
      if (object.has("path")) {
        String path = object.get("path").getAsString();
        return WaypointIcon.getByPath(path);
      }
    }
    throw new JsonParseException("Invalid waypoint icon json: " + json);
  }

  @Override
  public JsonElement serialize(
      Icon icon,
      Type typeOfSrc,
      JsonSerializationContext context
  ) {
    ResourceLocation resourceLocation = icon.getResourceLocation();
    if (resourceLocation == null) {
      throw new JsonParseException(
          "Cannot serialize waypoint icon without resource location: " + icon
      );
    }

    JsonObject object = new JsonObject();
    object.addProperty("path", resourceLocation.getPath());
    return object;
  }
}
