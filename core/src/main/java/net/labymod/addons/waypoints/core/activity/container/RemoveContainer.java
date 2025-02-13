/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.labymod.addons.waypoints.core.activity.container;

import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.activity.WaypointsActivity;
import net.labymod.addons.waypoints.core.activity.widgets.WaypointListItemWidget;
import net.labymod.addons.waypoints.core.activity.widgets.WaypointWidget;
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
    this.waypointService = Waypoints.references().waypointService();
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
      this.waypointService.remove(this.selectedWaypoint.getWaypointMeta());
      this.waypointList.listSession().setSelectedEntry(null);
      this.activity.setAction(null);
      this.waypointService.refresh();
    }));

    menu.addEntry(
        ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.activity.setAction(null)));
    this.inputWidget.addContent(menu);

    return this.inputWidget;
  }

}