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

package net.labymod.addons.waypoints.waypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WaypointIcon {

  public static final List<Icon> ICONS = new ArrayList<>();
  public static final @NotNull Icon DEFAULT;

  static {
    DEFAULT = register("waypoint");


  }

  public static Icon register(@NotNull String path) {
    ResourceLocation resourceLocation = ResourceLocation.create(
        "labyswaypoints",
        "/textures/icons/" + path + ".png"
    );

    Icon icon = Icon.texture(resourceLocation);
    ICONS.add(icon);
    return icon;
  }

  public static Collection<Icon> getDefaultIcons() {
    return ICONS;
  }

  public static Icon getByPath(String path) {
    for (Icon icon : ICONS) {
      if (icon.getResourceLocation() != null && icon.getResourceLocation().getPath().equals(path)) {
        return icon;
      }
    }
    return DEFAULT;
  }
}
