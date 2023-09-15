package net.labymod.addons.waypoints.utils;

import java.util.ArrayList;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.WaypointsConfiguration;
import net.labymod.addons.waypoints.activity.widgets.WaypointListItemWidget;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;

public class WidgetUtils {

    private static ArrayList<WaypointListItemWidget> waypointWidgets = new ArrayList<>();

    public static void setWaypointWidgets(ArrayList<WaypointListItemWidget> waypointWidgets) {
        WidgetUtils.waypointWidgets = waypointWidgets;
    }

    public static void handleWaypointWidgetStyle() {

        boolean OneEntryChecked = WidgetUtils.hasVisibleWaypoint();

        for (WaypointListItemWidget waypointWidget : waypointWidgets) {
            waypointWidget.getCheckbox().setState(OneEntryChecked ? State.UNCHECKED : State.CHECKED);
            waypointWidget.getWaypointMeta().setVisible(!OneEntryChecked);
            waypointWidget.opacity().set(OneEntryChecked ? 0.5F : 1F);
        }
    }

    public static boolean hasVisibleWaypoint() {
        for (Waypoint waypoint : Waypoints.getReferences().waypointService().getAllWaypoints()) {
            if (waypoint.meta().isVisible()) return true;
        }
        return false;
    }

}
