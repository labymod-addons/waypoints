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
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import java.util.Objects;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.Pair;
import net.labymod.api.util.gson.LabyGsonTypeAdapter;

public class WaypointIconTypeAdapter extends LabyGsonTypeAdapter<WaypointIcon> {

  @Override
  public WaypointIcon deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
  ) throws JsonParseException {
    if (json.isJsonPrimitive()) {
      JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
      if (jsonPrimitive.isString()) {
        return WaypointIcon.get(jsonPrimitive.getAsString());
      }
    }

    if (json.isJsonObject()) {
      JsonObject object = json.getAsJsonObject();
      String identifier = object.getAsJsonPrimitive("identifier").getAsString();
      if (object.has("resourceLocation")) {
        return WaypointIcon.getOrCompute(identifier, key -> WaypointIcon.createCustom(
            key,
            ResourceLocation.parse(object.get("resourceLocation").getAsString())
        ));
      } else if (object.has("url")) {
        return WaypointIcon.getOrCompute(identifier, key -> WaypointIcon.createCustom(
            key,
            object.get("url").getAsString()
        ));
      } else if (object.has("configData")) {
        return WaypointIcon.getOrCompute(identifier, key -> WaypointIcon.createUnknown(
            key,
            object.get("configData")
        ));
      }
    }

    throw new JsonParseException("Invalid waypoint icon json: " + json);
  }

  @Override
  public JsonElement serialize(
      WaypointIcon icon,
      Type typeOfSrc,
      JsonSerializationContext context
  ) {
    Object configData = icon.getConfigData();
    if (icon.isBuiltIn() || configData == null) {
      return new JsonPrimitive(icon.getIdentifier());
    }

    JsonObject object = new JsonObject();
    object.addProperty("identifier", icon.getIdentifier());
    if (configData instanceof ResourceLocation) {
      object.addProperty("resourceLocation", configData.toString());
    } else if (configData instanceof Pair<?, ?> pair) {
      if (pair.getFirst() instanceof String key && key.equals("url")) {
        object.addProperty("url", Objects.requireNonNull(pair.getSecond()).toString());
      } else {
        object.add("configData", context.serialize(configData));
      }
    } else if (configData instanceof JsonElement configDataObject) {
      object.add("configData", configDataObject);
    } else {
      object.add("configData", context.serialize(configData));
    }

    return object;
  }
}
