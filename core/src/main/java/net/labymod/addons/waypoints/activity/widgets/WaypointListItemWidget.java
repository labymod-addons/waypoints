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
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.opacity().set(meta.isVisible() ? 1F : 0.5F);
    checkbox.setState(meta.isVisible() ? State.CHECKED : State.UNCHECKED);

    checkbox.addId("checkbox");
    waypointWidget.addId("preview");

    this.addChild(checkbox);
    this.addChild(waypointWidget);
  }

  public CheckBoxWidget getCheckbox() {
    return checkbox;
  }

  public WaypointMeta getWaypointMeta() {
    return this.meta;
  }
}