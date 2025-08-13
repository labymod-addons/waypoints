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

package net.labymod.addons.waypoints;

import net.labymod.addons.waypoints.api.generated.ReferenceStorage;
import net.labymod.api.client.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Waypoints {

  public static final String NAMESPACE = "labyswaypoints";
  private static ReferenceStorage references;

  public static @NotNull ReferenceStorage references() {
    if (references == null) {
      throw new IllegalStateException("Waypoints is not initialized yet");
    }

    return Waypoints.references;
  }

  public static void init(ReferenceStorage references) {
    if (Waypoints.references != null) {
      throw new IllegalStateException("Waypoints already initialized");
    }

    Waypoints.references = references;
  }

  public static void refresh() {
    references().waypointService().refresh();
  }

  public static ResourceLocation ofPath(String path) {
    return ResourceLocation.create(NAMESPACE, path);
  }

  /**
   * @deprecated Use {@link #references()} instead
   */
  @Deprecated
  public static @Nullable ReferenceStorage getReferences() {
    return Waypoints.references;
  }
}
