package net.labymod.addons.waypoints.activity.widgets;

import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;

@AutoWidget
public class WaypointListItemWidget extends SimpleWidget {

  private final WaypointMeta meta;
  private final CheckBoxWidget checkbox;
  private final WaypointWidget waypointWidget;

  public WaypointListItemWidget(WaypointMeta meta) {
    this.meta = meta;
    this.checkbox = new CheckBoxWidget();
    this.waypointWidget = new WaypointWidget(meta);
    this.waypointWidget.addId("preview");
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.opacity().set(this.meta.isVisible() ? 1F : 0.5F);

    this.checkbox.setState(this.meta.isVisible() ? State.CHECKED : State.UNCHECKED);
    this.checkbox.addId("checkbox");
    this.addChild(this.checkbox);

    this.addChild(this.waypointWidget);
  }

  public CheckBoxWidget getCheckbox() {
    return this.checkbox;
  }

  public WaypointMeta getWaypointMeta() {
    return this.meta;
  }
}