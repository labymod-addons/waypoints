package net.labymod.addons.waypoints.activity.widgets;

import net.labymod.addons.waypoints.activity.WaypointsActivity;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;

@AutoWidget
public class HeaderWidget extends SimpleWidget {

  private final WaypointsActivity activity;
  private final CheckBoxWidget checkbox;
  private final ComponentWidget title;

  public HeaderWidget(WaypointsActivity activity) {
    this.checkbox = new CheckBoxWidget();
    this.activity = activity;

    this.title = ComponentWidget.component(Component.text("Name"));
    this.title.addId("title");
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.checkbox.setState(this.activity.hasVisibleWaypoint() ? State.CHECKED : State.UNCHECKED);
    this.checkbox.setPressable(() -> this.handleWaypointWidgetStyle());
    this.checkbox.addId("checkbox");

    this.addChild(this.checkbox);
    this.addChild(this.title);
  }

  public void handleWaypointWidgetStyle() {
    for (WaypointListItemWidget waypointWidget : this.activity.getWaypointWidgets()) {
      this.activity.handleWaypointWidgetStyle(
          waypointWidget, this.checkbox.state() == State.CHECKED
      );
    }
  }

  public CheckBoxWidget getCheckbox() {
    return this.checkbox;
  }
}