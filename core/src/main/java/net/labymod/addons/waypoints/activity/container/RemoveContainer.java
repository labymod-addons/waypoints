package net.labymod.addons.waypoints.activity.container;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.activity.WaypointsActivity;
import net.labymod.addons.waypoints.activity.widgets.WaypointListItemWidget;
import net.labymod.addons.waypoints.activity.widgets.WaypointWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;

public class RemoveContainer {

  private final WaypointListItemWidget selectedWaypoint;
  private final VerticalListWidget<WaypointListItemWidget> waypointList;
  private final FlexibleContentWidget inputWidget;
  private final WaypointService waypointService;
  private final WaypointsActivity activity;


  public RemoveContainer(WaypointListItemWidget selectedWaypoint,
      VerticalListWidget<WaypointListItemWidget> waypointList, FlexibleContentWidget inputWidget,
      WaypointsActivity activity) {
    this.selectedWaypoint = selectedWaypoint;
    this.waypointList = waypointList;
    this.inputWidget = inputWidget;
    this.waypointService = Waypoints.getReferences().waypointService();
    this.activity = activity;
  }

  public FlexibleContentWidget initializeRemoveContainer() {
    this.inputWidget.addId("remove-container");

    ComponentWidget confirmationWidget = ComponentWidget.i18n(
        "labyswaypoints.gui.manage.remove.title");
    confirmationWidget.addId("remove-confirmation");
    this.inputWidget.addContent(confirmationWidget);

    WaypointWidget previewWidget = new WaypointWidget(this.selectedWaypoint.getWaypointMeta());
    previewWidget.addId("remove-preview");
    this.inputWidget.addContent(previewWidget);

    HorizontalListWidget menu = new HorizontalListWidget();
    menu.addId("remove-button-menu");

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.remove", () -> {
      this.waypointService.removeWaypoint(this.selectedWaypoint.getWaypointMeta());
      this.waypointList.listSession().setSelectedEntry(null);
      this.activity.setAction(null);
    }));

    menu.addEntry(
        ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.activity.setAction(null)));
    this.inputWidget.addContent(menu);

    return this.inputWidget;
  }

}