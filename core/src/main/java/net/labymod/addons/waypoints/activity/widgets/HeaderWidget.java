package net.labymod.addons.waypoints.activity.widgets;

import net.labymod.addons.waypoints.WaypointsConfiguration;
import net.labymod.addons.waypoints.utils.WidgetUtils;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;

@AutoWidget
public class HeaderWidget extends SimpleWidget {

    private final WaypointsConfiguration configuration;
    private final CheckBoxWidget checkbox;
    private final ComponentWidget title = ComponentWidget.component(Component.text("Name"));

    public HeaderWidget(WaypointsConfiguration configuration) {
        this.configuration = configuration;
        this.checkbox = new CheckBoxWidget();
    }

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);

        this.checkbox.setState(
            WidgetUtils.hasVisibleWaypoint() ? State.CHECKED : State.UNCHECKED);

        this.checkbox.setPressable(() -> {
            WidgetUtils.handleWaypointWidgetStyle();
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