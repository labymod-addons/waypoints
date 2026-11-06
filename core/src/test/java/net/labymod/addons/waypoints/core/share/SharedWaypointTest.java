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
package net.labymod.addons.waypoints.core.share;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import org.junit.jupiter.api.Test;

class SharedWaypointTest {

  private static SharedWaypoint home() {
    return SharedWaypoint.of(
        "Home", "New Waypoint", 0xFF55FF55, WaypointType.PERMANENT,
        120.5, 64, -340.5,
        WaypointContext.MULTI_PLAYER, "play.example.net",
        "textures/icons/home.png", "minecraft:overworld",
        -4172144997902289642L, true
    );
  }

  private static JsonObject payload() {
    return home().write();
  }

  private static SharedWaypoint read(String json) {
    return SharedWaypoint.read(JsonParser.parseString(json).getAsJsonObject());
  }

  @Test
  void roundTrip() {
    SharedWaypoint waypoint = home();
    assertNotNull(waypoint);
    assertEquals(waypoint, SharedWaypoint.read(waypoint.write()));
  }

  @Test
  void writesTheKeysOfASavedWaypoint() {
    JsonObject data = payload();
    assertEquals("Home", data.getAsJsonObject("title").get("text").getAsString());
    assertEquals("MULTI_PLAYER", data.get("contextType").getAsString());
    assertEquals("play.example.net", data.get("context").getAsString());
    assertEquals("textures/icons/home.png",
        data.getAsJsonObject("icon").get("path").getAsString());
    assertEquals(64, data.getAsJsonObject("location").get("y").getAsDouble());
    assertFalse(data.has("id"));
  }

  @Test
  void ignoresUnknownKeys() {
    JsonObject data = payload();
    data.addProperty("id", "someone-elses-id");
    data.addProperty("visible", false);
    data.addProperty("future", 42);
    assertEquals(home(), SharedWaypoint.read(data));
  }

  @Test
  void acceptsTheMinimalPayload() {
    SharedWaypoint waypoint = read("""
        {"title": "Base", "color": -1, "type": "PERMANENT",
         "location": {"x": 0, "y": 70, "z": 0},
         "contextType": "SINGLE_PLAYER", "context": "New World"}
        """);
    assertNotNull(waypoint);
    assertEquals("Base", waypoint.title());
    assertNull(waypoint.iconPath());
    assertNull(waypoint.dimension());
    assertNull(waypoint.hashedSeed());
    assertFalse(waypoint.isWorldBound());
  }

  @Test
  void titleIsPlainText() {
    JsonObject data = payload();
    JsonObject title = new JsonObject();
    title.addProperty("text", "§cRed§r Base\n");
    title.addProperty("color", "red");
    title.addProperty("bold", true);
    data.add("title", title);
    SharedWaypoint waypoint = SharedWaypoint.read(data);
    assertNotNull(waypoint);
    assertEquals("Red Base", waypoint.title());
  }

  @Test
  void rejectsTitlesThatAreNotPlainText() {
    for (String title : new String[]{
        "{\"translate\": \"block.minecraft.chest\"}",
        "{\"text\": \"a\", \"extra\": [\"b\"]}",
        "{\"text\": \"a\", \"keybind\": \"key.jump\"}",
        "{\"selector\": \"@a\"}",
        "{\"score\": {\"name\": \"@s\", \"objective\": \"x\"}}",
        "{\"text\": 5}",
        "[\"Home\"]",
        "\"\"",
        "\"   \"",
        "\"§a\"",
        "\"" + "x".repeat(SharedWaypoint.MAX_TITLE_LENGTH + 1) + "\"",
    }) {
      JsonObject data = payload();
      data.add("title", JsonParser.parseString(title));
      assertNull(SharedWaypoint.read(data), title);
    }

    JsonObject data = payload();
    data.remove("title");
    assertNull(SharedWaypoint.read(data));
  }

  @Test
  void acceptsTheLongestTitle() {
    JsonObject data = payload();
    data.addProperty("title", "x".repeat(SharedWaypoint.MAX_TITLE_LENGTH));
    assertNotNull(SharedWaypoint.read(data));
  }

  @Test
  void colorIsOpaque() {
    JsonObject data = payload();
    data.addProperty("color", 0x00123456);
    assertEquals(0xFF123456, SharedWaypoint.read(data).color());

    data.addProperty("color", 4294967295L);
    assertEquals(0xFFFFFFFF, SharedWaypoint.read(data).color());
  }

  @Test
  void rejectsInvalidColors() {
    for (String color : new String[]{"\"red\"", "1.5", "4294967296", "-2147483649", "null",
        "{\"value\": -1}", "1e40"}) {
      JsonObject data = payload();
      data.add("color", JsonParser.parseString(color));
      assertNull(SharedWaypoint.read(data), color);
    }
  }

  @Test
  void rejectsCoordinatesOutsideTheWorld() {
    for (String axis : new String[]{"x", "y", "z"}) {
      for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, 4.0E7, -4.0E7}) {
        JsonObject data = payload();
        data.getAsJsonObject("location").addProperty(axis, value);
        assertNull(SharedWaypoint.read(data), axis + "=" + value);
      }

      JsonObject data = payload();
      data.getAsJsonObject("location").addProperty(axis, "1");
      assertNull(SharedWaypoint.read(data), axis + " as a string");

      data = payload();
      data.getAsJsonObject("location").remove(axis);
      assertNull(SharedWaypoint.read(data), axis + " missing");
    }

    JsonObject data = payload();
    data.getAsJsonObject("location").addProperty("y", 2.5E7);
    assertNull(SharedWaypoint.read(data));

    data.getAsJsonObject("location").addProperty("y", -64);
    data.getAsJsonObject("location").addProperty("x", 29_999_984);
    assertNotNull(SharedWaypoint.read(data));

    data = payload();
    data.addProperty("location", "0 64 0");
    assertNull(SharedWaypoint.read(data));
  }

  @Test
  void rejectsUnknownTypesAndContexts() {
    JsonObject data = payload();
    data.addProperty("type", "FOREVER");
    assertNull(SharedWaypoint.read(data));

    data = payload();
    data.addProperty("type", "permanent");
    assertNull(SharedWaypoint.read(data));

    data = payload();
    data.addProperty("contextType", "LAN");
    assertNull(SharedWaypoint.read(data));

    data = payload();
    data.remove("contextType");
    assertNull(SharedWaypoint.read(data));

    data = payload();
    data.remove("context");
    assertNull(SharedWaypoint.read(data));
  }

  @Test
  void checksTheContextValue() {
    assertTrue(SharedWaypoint.isValidContext(WaypointContext.MULTI_PLAYER, "play.example.net"));
    assertTrue(SharedWaypoint.isValidContext(WaypointContext.MULTI_PLAYER, "127.0.0.1:25566"));
    assertTrue(SharedWaypoint.isValidContext(WaypointContext.MULTI_PLAYER, "[::1]:25566"));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.MULTI_PLAYER, "evil host"));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.MULTI_PLAYER, ""));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.MULTI_PLAYER,
        "a".repeat(SharedWaypoint.MAX_CONTEXT_LENGTH + 1)));

    assertTrue(SharedWaypoint.isValidContext(WaypointContext.SINGLE_PLAYER, "My World (2)"));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.SINGLE_PLAYER, "../saves"));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.SINGLE_PLAYER, "a\\b"));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.SINGLE_PLAYER, "a\u0000b"));
    assertFalse(SharedWaypoint.isValidContext(WaypointContext.SINGLE_PLAYER, " padded "));
  }

  @Test
  void checksDimensionAndIcon() {
    JsonObject data = payload();
    data.addProperty("dimension", "myserver:farm_world");
    assertEquals("myserver:farm_world", SharedWaypoint.read(data).dimension());

    data.add("dimension", null);
    assertNull(SharedWaypoint.read(data).dimension());

    for (String dimension : new String[]{"overworld", "Minecraft:Overworld", "minecraft:",
        "a:b c"}) {
      data = payload();
      data.addProperty("dimension", dimension);
      assertNull(SharedWaypoint.read(data), dimension);
    }

    data = payload();
    data.addProperty("dimension", 0);
    assertNull(SharedWaypoint.read(data));

    for (String path : new String[]{"textures/icons/../../secret.png", "textures/other.png",
        "textures/icons/Home.png", "textures/icons/home.jpg"}) {
      data = payload();
      data.getAsJsonObject("icon").addProperty("path", path);
      assertNull(SharedWaypoint.read(data), path);
    }

    data = payload();
    data.addProperty("icon", "home");
    assertNull(SharedWaypoint.read(data));
  }

  @Test
  void checksTheWorld() {
    JsonObject data = payload();
    data.addProperty("hashedSeed", 1.5);
    assertNull(SharedWaypoint.read(data));

    data.addProperty("hashedSeed", "123");
    assertNull(SharedWaypoint.read(data));

    data.addProperty("hashedSeed", new java.math.BigInteger("9223372036854775808"));
    assertNull(SharedWaypoint.read(data));

    data = payload();
    data.remove("hashedSeed");
    assertNull(SharedWaypoint.read(data), "bound to a world it does not name");

    data.addProperty("worldBound", false);
    assertNotNull(SharedWaypoint.read(data));

    data.addProperty("worldBound", "yes");
    assertNull(SharedWaypoint.read(data));

    data = payload();
    data.addProperty("hashedSeed", Long.MIN_VALUE);
    assertEquals(Long.MIN_VALUE, SharedWaypoint.read(data).hashedSeed());
  }

  @Test
  void ownWaypointsAreCutToFit() {
    SharedWaypoint waypoint = SharedWaypoint.of(
        "y".repeat(100), "New Waypoint", 0xFFFFFFFF, WaypointType.SERVER_SESSION,
        0, 0, 0, WaypointContext.MULTI_PLAYER, "play.example.net",
        null, null, null, true
    );
    assertNotNull(waypoint);
    assertEquals(SharedWaypoint.MAX_TITLE_LENGTH, waypoint.title().length());
    assertFalse(waypoint.isWorldBound(), "no seed, so not bound");
    assertEquals(waypoint, SharedWaypoint.read(waypoint.write()));

    SharedWaypoint unnamed = SharedWaypoint.of(
        " §l ", "New Waypoint", 0xFFFFFFFF, WaypointType.PERMANENT,
        0, 0, 0, WaypointContext.MULTI_PLAYER, "play.example.net",
        null, null, null, false
    );
    assertEquals("New Waypoint", unnamed.title());
  }

  @Test
  void ownWaypointsWithoutContextCannotBeShared() {
    assertNull(SharedWaypoint.of(
        "Old", "New Waypoint", 0xFFFFFFFF, WaypointType.PERMANENT,
        0, 0, 0, null, null, null, null, null, false
    ));
    assertNull(SharedWaypoint.of(
        "Far", "New Waypoint", 0xFFFFFFFF, WaypointType.PERMANENT,
        5.0E7, 0, 0, WaypointContext.MULTI_PLAYER, "play.example.net",
        null, null, null, false
    ));
  }
}
