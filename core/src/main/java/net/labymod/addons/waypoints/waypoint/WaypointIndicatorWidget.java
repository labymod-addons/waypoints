package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.util.bounds.ModifyReason;

public class WaypointIndicatorWidget extends IconWidget {

  private final Waypoint waypoint;

  public WaypointIndicatorWidget(Waypoint waypoint) {
    super(WaypointTextures.MARKER_ICON);
    this.waypoint = waypoint;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.bounds().setSize(
        DefaultWaypoint.ICON_WIDTH,
        DefaultWaypoint.ICON_HEIGHT,
        ModifyReason.of(Waypoint.class, "waypoint")
    );
    this.color().set(this.waypoint.meta().getColor().get());
  }
}
