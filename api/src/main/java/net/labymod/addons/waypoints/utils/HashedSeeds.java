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
package net.labymod.addons.waypoints.utils;

import java.util.OptionalLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Decides whether a waypoint bound to a hashed world seed belongs to the current world.
 *
 * @see net.labymod.api.client.world.ClientWorld#hashedSeed()
 */
public final class HashedSeeds {

  private HashedSeeds() {
  }

  /**
   * Checks whether a waypoint bound to the given hashed seed belongs to the world the player is
   * currently in.
   *
   * <p>A waypoint without a bound seed belongs to every world of its context. An empty current seed
   * (not ingame, Minecraft versions below 1.15 or a server that hides its seed) never hides
   * anything, so bound waypoints degrade to being visible everywhere instead of disappearing.</p>
   *
   * @param boundSeed   the hashed seed the waypoint is bound to, or {@code null} if it is not bound
   *                    to a world
   * @param currentSeed the hashed seed of the world the player is currently in
   * @return {@code true} if the waypoint belongs to the current world
   */
  public static boolean matches(@Nullable Long boundSeed, @NotNull OptionalLong currentSeed) {
    return boundSeed == null || currentSeed.isEmpty() || currentSeed.getAsLong() == boundSeed;
  }
}
