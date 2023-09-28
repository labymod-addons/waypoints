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
  private final ComponentWidget title = ComponentWidget.component(Component.text("Name"));

  public HeaderWidget(WaypointsActivity activity) {
    this.checkbox = new CheckBoxWidget();
    this.activity = activity;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.checkbox.setState(
        activity.hasVisibleWaypoint() ? State.CHECKED : State.UNCHECKED);

    this.checkbox.setPressable(() -> {
      activity.handleWaypointWidgetStyle();
    });

    this.checkbox.addId("checkbox");
    title.addId("title");

    this.addChild(this.checkbox);
    this.addChild(title);
  }

  public CheckBoxWidget getCheckbox() {
    return checkbox;
  }
}