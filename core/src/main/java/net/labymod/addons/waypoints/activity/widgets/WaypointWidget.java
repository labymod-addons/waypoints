package net.labymod.addons.waypoints.activity.widgets;

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
  private final IconWidget icon = new IconWidget(WaypointTextures.MARKER_ICON);
  private final ComponentWidget title;

  public WaypointWidget(WaypointMeta meta) {
    this.meta = meta;
    this.title = ComponentWidget.component(this.meta.title());
    this.title.addId("title");
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.title.textColor().set(this.meta.color().get());
    this.addChild(this.title);

    this.icon.color().set(this.meta.color().get());
    this.icon.addId("icon");
    this.addChild(icon);

    if (this.meta.type() == WaypointType.SERVER_SESSION) {
      IconWidget typeWidget = new IconWidget(Textures.SpriteCommon.EXCLAMATION_MARK_LIGHT);

      typeWidget.addId("type");
      typeWidget.setHoverComponent(Component.translatable("labyswaypoints.gui.overview.temporary"));

      this.addChild(typeWidget);
    }
  }
}