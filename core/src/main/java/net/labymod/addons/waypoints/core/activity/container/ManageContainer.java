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
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.util.math.vector.DoubleVector3;

public class ManageContainer {

  private final WaypointListItemWidget waypointListItemWidget;
  private final Component manageTitle;
  private final FlexibleContentWidget inputWidget;
  private final WaypointService waypointService;
  private final WaypointsActivity activity;


  public ManageContainer(WaypointListItemWidget waypointListItemWidget, Component manageTitle,
      FlexibleContentWidget inputWidget,
      WaypointsActivity activity) {
    this.waypointListItemWidget = waypointListItemWidget;
    this.manageTitle = manageTitle;
    this.inputWidget = inputWidget;
    this.waypointService = Waypoints.references().waypointService();
    this.activity = activity;
  }

  public DivWidget initializeManageContainer() {
    ButtonWidget doneButton = ButtonWidget.i18n("labymod.ui.button.done");

    DivWidget inputContainer = new DivWidget();
    inputContainer.addId("input-container");

    if (this.manageTitle != null) {
      ComponentWidget manageTitle = ComponentWidget.component(this.manageTitle);
      manageTitle.addId("title");
      inputContainer.addChild(manageTitle);
    }

    this.inputWidget.addId("input-list");

    WaypointMeta meta = this.waypointListItemWidget.getWaypointMeta();

    DivWidget nameLabelList = new DivWidget();
    nameLabelList.addId("input-name-list");
    nameLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.name"))
        .addId("input-label");

    TextFieldWidget nameInput = new TextFieldWidget();
    nameInput.addId("input-text");
    nameInput.setText(PlainTextComponentSerializer.plainText().serialize(meta.title()));
    nameInput.maximalLength(50);
    nameInput.updateListener(newValue -> doneButton.setEnabled(!newValue.trim().isEmpty()));
    nameLabelList.addChild(nameInput);

    this.inputWidget.addContent(nameLabelList);

    DivWidget colorLabelList = new DivWidget();
    colorLabelList.addId("input-name-list");
    colorLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.color"))
        .addId("input-label");

    ColorPickerWidget colorPicker = ColorPickerWidget.of(meta.getColor());
    colorPicker.addId("input-color");
    colorLabelList.addChild(colorPicker);

    this.inputWidget.addContent(colorLabelList);

    DivWidget xLabelList = new DivWidget();
    xLabelList.addId("input-name-list");
    xLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.x"))
        .addId("input-label");

    TextFieldWidget xInput = new TextFieldWidget();
    xInput.addId("input-text");
    xInput.setText(String.valueOf((int) meta.location().getX()));
    xLabelList.addChild(xInput);

    this.inputWidget.addContent(xLabelList);

    DivWidget yLabelList = new DivWidget();
    yLabelList.addId("input-name-list");
    yLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.y"))
        .addId("input-label");

    TextFieldWidget yInput = new TextFieldWidget();
    yInput.addId("input-text");
    yInput.setText(String.valueOf((int) meta.location().getY()));
    yLabelList.addChild(yInput);

    this.inputWidget.addContent(yLabelList);

    DivWidget zLabelList = new DivWidget();
    zLabelList.addId("input-name-list");
    zLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.z"))
        .addId("input-label");

    TextFieldWidget zInput = new TextFieldWidget();
    zInput.addId("input-text");
    zInput.setText(String.valueOf((int) meta.location().getZ()));
    zLabelList.addChild(zInput);

    this.inputWidget.addContent(zLabelList);

    HorizontalListWidget buttonList = new HorizontalListWidget();
    buttonList.addId("edit-button-menu");

    doneButton.setEnabled(!nameInput.getText().trim().isEmpty());
    doneButton.setPressable(() -> {
      // Remove the old waypoint in case this is an edit (or the exact same waypoint already exists)
      boolean permanent = this.waypointService.remove(meta);

      try {
        meta.setLocation(new DoubleVector3(
            Integer.parseInt(xInput.getText()) - 0.5D,
            Integer.parseInt(yInput.getText()),
            Integer.parseInt(zInput.getText()) - 0.5D
        ));
      } catch (NumberFormatException ignored) {
        if (!permanent) {
          return;
        }
      }

      if (nameInput.getText().length() > 0) {
        meta.setTitle(Component.text(nameInput.getText()));
      } else {
        meta.setTitle(Component.text("New Waypoint"));
      }

      meta.setColor(colorPicker.value());
      if (permanent) {
        meta.setType(WaypointType.PERMANENT);
      }

      this.waypointService.add(meta);

      Waypoint waypoint = this.waypointService.get(meta);

      if (waypoint != null) {
        waypoint.waypointObjectMeta().clearTitleCache();
      }

      this.activity.setAction(null);
      this.waypointService.refresh();
    });

    buttonList.addEntry(doneButton);

    buttonList.addEntry(
        ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.activity.setAction(null)));
    inputContainer.addChild(this.inputWidget);
    this.inputWidget.addContent(buttonList);
    return inputContainer;
  }
}