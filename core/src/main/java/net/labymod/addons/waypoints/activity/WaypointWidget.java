package net.labymod.addons.waypoints.activity;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
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

  private final WaypointMeta meta;

  public WaypointWidget(WaypointMeta meta) {
    this.meta = meta;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    IconWidget avatar = new IconWidget(WaypointTextures.MARKER_ICON);
    avatar.color().set(this.meta.getColor().get());
    avatar.addId("avatar");
    this.addChild(avatar);

    ComponentWidget title = ComponentWidget.component(this.meta.getTitle());
    title.addId("title");
    this.addChild(title);

    if (this.meta.getType() == WaypointType.SERVER_SESSION) {
      IconWidget typeWidget = new IconWidget(Textures.SpriteCommon.EXCLAMATION_MARK_LIGHT);

      typeWidget.addId("type");
      typeWidget.setHoverComponent(Component.translatable("labyswaypoints.gui.overview.temporary"));

      this.addChild(typeWidget);
    }
  }

  public WaypointMeta getWaypoint() {
    return this.meta;
  }
}