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
