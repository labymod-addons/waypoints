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

package net.labymod.addons.waypoints.core.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.activity.layout.WaypointCollection;
import net.labymod.addons.waypoints.core.activity.layout.WaypointContextCollection;
import net.labymod.addons.waypoints.core.activity.popup.ManageWaypointSimplePopup;
import net.labymod.addons.waypoints.core.activity.widgets.HeaderWidget;
import net.labymod.addons.waypoints.core.activity.widgets.WaypointListItemWidget;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointContext;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.Textures.SpriteCommon;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.HrWidget;
import net.labymod.api.client.network.server.ServerAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoActivity
@Link("manage.lss")
@Link("overview.lss")
public class WaypointsActivity extends Activity {

  private final WaypointService waypointService;
  private final VerticalListWidget<WaypointListItemWidget> waypointList;
  private final HeaderWidget headerWidget;
  private final List<WaypointContextCollection> waypointContexts = new ArrayList<>();
  private WaypointContextCollection activeContext;
  private ArrayList<WaypointListItemWidget> waypointWidgets;
  private WaypointListItemWidget selectedWaypoint;
  private ButtonWidget removeButton;
  private ButtonWidget editButton;
  private WaypointCollection selectedCollection;

  public WaypointsActivity() {
    this.waypointService = Waypoints.references().waypointService();

    this.waypointList = new VerticalListWidget<>();
    this.waypointWidgets = new ArrayList<>();
    this.waypointList.addId("waypoints-list");
    this.headerWidget = new HeaderWidget(this);
    this.waypointList.setSelectCallback(waypointListItemWidget -> {
      WaypointListItemWidget selectedWidget = this.waypointList.listSession().getSelectedEntry();
      if (selectedWidget == null
          || selectedWidget.getWaypointMeta() != waypointListItemWidget.getWaypointMeta()) {
        this.editButton.setEnabled(true);
        this.removeButton.setEnabled(true);
      }
    });

    this.waypointList.setDoubleClickCallback(waypointListItemWidget -> this.setAction(Action.EDIT));

    this.updateWaypointContextList();
  }

  private void updateWaypointContextList() {
    this.waypointContexts.clear();
    for (Waypoint waypoint : this.waypointService.getAll()) {
      WaypointContextCollection collection = this.getOrCreateByContext(waypoint);
      collection.add(waypoint);
    }

    this.waypointContexts.sort(Comparator.comparing(WaypointContextCollection::getName));
    this.activeContext = this.evaluateActiveContext();
  }

  private @Nullable WaypointContextCollection evaluateActiveContext() {
    ServerAddress serverAddress = this.waypointService.getServerAddress();
    String singlePlayerWorld = this.waypointService.getSinglePlayerWorld();
    if (serverAddress == null && singlePlayerWorld == null) {
      return null;
    }

    WaypointContextCollection activeContext = null;
    WaypointContext targetContext =
        serverAddress != null ? WaypointContext.MULTI_PLAYER : WaypointContext.SINGLE_PLAYER;
    String targetContextValue =
        serverAddress != null ? serverAddress.toString() : singlePlayerWorld;
    for (WaypointContextCollection waypointContext : this.waypointContexts) {
      if (waypointContext.context() == targetContext
          && waypointContext.getContextValue().equals(targetContextValue)) {
        activeContext = waypointContext;
        break;
      }
    }

    return activeContext;
  }

  private @NotNull WaypointContextCollection getOrCreateByContext(Waypoint waypoint) {
    WaypointMeta meta = waypoint.meta();
    for (WaypointContextCollection waypointContext : this.waypointContexts) {
      if (meta.matchesContext(waypointContext.context(), waypointContext.getContextValue())) {
        return waypointContext;
      }
    }

    WaypointContextCollection collection = new WaypointContextCollection(meta.contextType(), meta.getContext());
    this.waypointContexts.add(collection);
    return collection;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    FlexibleContentWidget container = new FlexibleContentWidget();
    container.addId("waypoints-container");

    ArrayList<WaypointListItemWidget> listItemWidgets = new ArrayList<>();

    this.waypointWidgets = listItemWidgets;

    container.addContent(this.headerWidget);

    VerticalListWidget<Widget> widgets = new VerticalListWidget<>();
    widgets.addId("waypoints-overview-list");

    if (this.selectedCollection != null || this.activeContext != null) {
      WaypointCollection collection = this.selectedCollection != null
          ? this.selectedCollection
          : this.activeContext;

      HorizontalListWidget header = new HorizontalListWidget();
      header.addId("waypoints-list-header");

      if (this.selectedCollection != null) {
        ButtonWidget backButton = ButtonWidget.icon(SpriteCommon.BACK_BUTTON);
        backButton.addId("back-button");
        backButton.setPressListener(() -> {
          this.selectedCollection = this.selectedCollection.getParent();
          this.reload();
          return true;
        });

        header.addEntry(backButton);
      }

      header.addEntry(ComponentWidget.text("Waypoints on " + collection.getName()));
      widgets.addChild(header);

      for (Object entry : collection.getEntries()) {
        //todo groups
        if (entry instanceof Waypoint waypoint) {
          WaypointObjectMeta objectMeta;
          if (this.waypointService.getVisible().contains(waypoint)) {
            objectMeta = waypoint.waypointObjectMeta();
          } else {
            objectMeta = null;
          }

          WaypointListItemWidget listItemWidget = new WaypointListItemWidget(
              waypoint.meta(),
              objectMeta
          );
          listItemWidget.setPressable(() -> {
            this.waypointList.listSession().setSelectedEntry(listItemWidget);
          });
          widgets.addChild(listItemWidget);
          listItemWidgets.add(listItemWidget);
          listItemWidget.getCheckbox().setPressable(() -> {
            this.handleWaypointWidgetStyle(
                listItemWidget, !listItemWidget.getWaypointMeta().isVisible()
            );

            this.headerWidget.getCheckbox()
                .setState(this.hasVisibleWaypoint() ? State.CHECKED : State.UNCHECKED);
          });
        }
      }

      if (this.selectedCollection == null && this.waypointContexts.size() > 1) {
        widgets.addChild(new HrWidget());
        ComponentWidget component = ComponentWidget.text("Other Waypoints");
        component.addId("other-waypoints-header");
        widgets.addChild(component);
      }
    }

    if (this.selectedCollection == null) {
      for (WaypointContextCollection waypointContext : this.waypointContexts) {
        if (this.activeContext != null && waypointContext == this.activeContext) {
          continue;
        }

        ButtonWidget contextButton = ButtonWidget.text(
            waypointContext.getName(),
            waypointContext.icon()
        );
        contextButton.setPressListener(() -> {
          this.selectedCollection = waypointContext;
          this.reload();
          return true;
        });

        widgets.addChild(contextButton);
      }
    }

    container.addFlexibleContent(new ScrollWidget(widgets));

    this.selectedWaypoint = this.waypointList.listSession().getSelectedEntry();

    HorizontalListWidget menu = new HorizontalListWidget();
    menu.addId("overview-button-menu");

    ButtonWidget addButton = ButtonWidget.i18n(
        "labymod.ui.button.add",
        () -> this.setAction(Action.ADD)
    );
    addButton.setEnabled(Laby.labyAPI().minecraft().isIngame());
    menu.addEntry(addButton);

    this.editButton = ButtonWidget.i18n(
        "labymod.ui.button.edit",
        () -> this.setAction(Action.EDIT)
    );
    this.editButton.setEnabled(this.selectedWaypoint != null);
    menu.addEntry(this.editButton);

    this.removeButton = ButtonWidget.i18n(
        "labymod.ui.button.remove",
        () -> this.setAction(Action.REMOVE)
    );
    this.removeButton.setEnabled(this.selectedWaypoint != null);
    menu.addEntry(this.removeButton);

    container.addContent(menu);

    this.document().addChild(container);
  }

  public ArrayList<WaypointListItemWidget> getWaypointWidgets() {
    return this.waypointWidgets;
  }

  public boolean hasVisibleWaypoint() {
    for (Waypoint waypoint : this.waypointService.getAll()) {
      if (waypoint.meta().isVisible()) {
        return true;
      }
    }
    return false;
  }

  public void handleWaypointWidgetStyle(WaypointListItemWidget waypointWidget, boolean visibility) {
    waypointWidget.getWaypointMeta().setVisible(visibility);
    this.waypointService.update(waypointWidget.getWaypointMeta());
    this.waypointService.refresh();
    waypointWidget.opacity().set(visibility ? 1F : 0.5F);
    waypointWidget.getCheckbox().setState(visibility ? State.CHECKED : State.UNCHECKED);
  }

  @Override
  public boolean mouseClicked(MutableMouse mouse, MouseButton mouseButton) {
    try {
      return super.mouseClicked(mouse, mouseButton);
    } finally {
      this.selectedWaypoint = this.waypointList.listSession().getSelectedEntry();
      this.removeButton.setEnabled(this.selectedWaypoint != null);
      this.editButton.setEnabled(this.selectedWaypoint != null);
    }
  }

  @Override
  public boolean keyPressed(Key key, InputType type) {
    return super.keyPressed(key, type);
  }

  public void setAction(Action action) {
    switch (action) {
      case EDIT:
        ManageWaypointSimplePopup editWaypointPopup = new ManageWaypointSimplePopup(
            this.selectedWaypoint.getWaypointMeta()
        ).onSave(waypoint -> {
          this.updateWaypointContextList();
          this.reload();
        });

        editWaypointPopup.displayInOverlay();
        break;
      case REMOVE:
        if (this.selectedWaypoint == null) {
          return;
        }
        WaypointMeta waypointMeta = this.selectedWaypoint.getWaypointMeta();
        this.waypointService.remove(waypointMeta);
        this.waypointList.listSession().setSelectedEntry(null);
        this.selectedWaypoint = null;
        this.updateWaypointContextList();
        this.reload();
        break;
      case ADD:
      default:
        ManageWaypointSimplePopup addWaypointPopup = new ManageWaypointSimplePopup()
            .onSave(() -> {
              this.updateWaypointContextList();
              this.reload();
            });
        addWaypointPopup.displayInOverlay();
        break;
    }
  }

  public enum Action {
    ADD, EDIT, REMOVE
  }
}