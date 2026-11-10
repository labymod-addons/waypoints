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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonObject;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import org.junit.jupiter.api.Test;

class WaypointShareKindTest {

  private static SharedWaypoint home() {
    return SharedWaypoint.of(
        "Home", "New Waypoint", 0xFF55FF55, WaypointType.PERMANENT,
        120.5, 64, -340.5,
        WaypointContext.MULTI_PLAYER, "play.example.net",
        "textures/icons/home.png", "minecraft:overworld",
        -4172144997902289642L, true
    );
  }

  @Test
  void keepsTheKindAndTheFirstVersion() {
    assertEquals("labyswaypoints:waypoint", WaypointShareKind.ID);
    assertEquals(1, WaypointShareKind.CODEC.version());
  }

  @Test
  void writesTheWaypointPayloadUnchanged() {
    SharedWaypoint waypoint = home();
    assertNotNull(waypoint);
    assertEquals(waypoint.write(), WaypointShareKind.CODEC.write(waypoint));
  }

  @Test
  void readsWhatItWrites() {
    SharedWaypoint waypoint = home();
    assertNotNull(waypoint);
    JsonObject data = WaypointShareKind.CODEC.write(waypoint);
    assertEquals(waypoint, WaypointShareKind.CODEC.read(data, 1));
  }

  @Test
  void rejectsInvalidPayloads() {
    assertNull(WaypointShareKind.CODEC.read(new JsonObject(), 1));

    JsonObject data = WaypointShareKind.CODEC.write(home());
    data.addProperty("color", "red");
    assertNull(WaypointShareKind.CODEC.read(data, 1));
  }
}
