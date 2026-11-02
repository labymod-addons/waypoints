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

import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point to Laby Chat sharing. It names no type of the share API, not even in a signature,
 * a field or an argument, so it loads and verifies on clients without it; only
 * {@link WaypointShareable} does, and it is reached after {@link #register()} succeeded.
 */
public final class WaypointChatShare {

  private static boolean available;

  private WaypointChatShare() {
  }

  /**
   * Registers the waypoint kind. Throws a {@link LinkageError} on a client without the share API.
   */
  public static void register() {
    if (available) {
      return;
    }

    WaypointShareable.register();
    available = true;
  }

  public static boolean isAvailable() {
    return available;
  }

  /**
   * @return whether the waypoint can be shared: the share API is there and the waypoint belongs to
   * a server or world and lies inside it
   */
  public static boolean canShare(@NotNull WaypointMeta meta) {
    return available && WaypointShareable.canShare(meta);
  }

  /**
   * Lets the player pick a friend and sends them the waypoint. Called on the render thread.
   */
  public static void share(@NotNull WaypointMeta meta) {
    if (available) {
      WaypointShareable.share(meta);
    }
  }
}
