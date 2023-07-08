package net.labymod.addons.waypoints.activity;

import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;

@AutoWidget
public class WaypointWidget extends SimpleWidget {

  private final Waypoint waypoint;

  public WaypointWidget(Waypoint waypoint) {
    this.waypoint = waypoint;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    IconWidget avatar = new IconWidget(WaypointTextures.MARKER_ICON);
    avatar.color().set(this.waypoint.color().get());
    avatar.addId("avatar");
    this.addChild(avatar);

    ComponentWidget title = ComponentWidget.component(this.waypoint.title());
    title.addId("title");
    this.addChild(title);

    if (this.waypoint.type() == WaypointType.SERVER_SESSION) {
      IconWidget typeWidget = new IconWidget(Textures.SpriteCommon.EXCLAMATION_MARK_LIGHT);

      typeWidget.addId("type");
      typeWidget.setHoverComponent(Component.translatable("labyswaypoints.gui.overview.temporary"));

      this.addChild(typeWidget);
    }
  }

  public Waypoint getWaypoint() {
    return this.waypoint;
  }
}