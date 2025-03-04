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

package net.labymod.addons.waypoints.core.activity.layout;

import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.api.client.gui.icon.Icon;
import org.jetbrains.annotations.NotNull;

public class WaypointContextCollection extends WaypointCollection {

  private final WaypointContext context;
  private final String contextValue;

  public WaypointContextCollection(
      @NotNull WaypointContext context,
      @NotNull String contextValue
  ) {
    super(
        getIconByContext(context, contextValue),
        getNameByContext(context, contextValue)
    );

    this.context = context;
    this.contextValue = contextValue;
  }

  private static Icon getIconByContext(WaypointContext context, String contextValue) {
    if (context == WaypointContext.MULTI_PLAYER) {
      return Icon.server(contextValue);
    }

    // todo World Icon?
    return Icon.defaultServer();
  }

  private static String getNameByContext(WaypointContext context, String contextValue) {
    return contextValue;
  }

  public @NotNull WaypointContext context() {
    return this.context;
  }

  public @NotNull String getContextValue() {
    return this.contextValue;
  }
}
