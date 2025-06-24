package net.labymod.addons.waypoints.activity;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.activity.container.ManageContainer;
import net.labymod.addons.waypoints.activity.container.RemoveContainer;
import net.labymod.addons.waypoints.activity.widgets.HeaderWidget;
import net.labymod.addons.waypoints.activity.widgets.WaypointListItemWidget;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.FloatVector3;

@AutoActivity
@Link("manage.lss")
@Link("overview.lss")
public class WaypointsActivity extends Activity {

  private final WaypointService waypointService;
  private final boolean overview;
  private final VerticalListWidget<WaypointListItemWidget> waypointList;
  private final HeaderWidget headerWidget;
  private ArrayList<WaypointListItemWidget> waypointWidgets;
  private WaypointListItemWidget selectedWaypoint;
  private ButtonWidget addButton;
  private ButtonWidget removeButton;
  private ButtonWidget editButton;
  private FlexibleContentWidget inputWidget;
  private Action action;
  private Component manageTitle;
  private Consumer<WaypointMeta> modifier;

  public WaypointsActivity(boolean overview) {
    this.overview = overview;
    this.waypointService = Waypoints.getReferences().waypointService();

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
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    FlexibleContentWidget container = new FlexibleContentWidget();
    container.addId("waypoints-container");

    ArrayList<WaypointListItemWidget> listItemWidgets = new ArrayList<>();

    if (this.overview) {
      for (Waypoint waypoint : this.waypointService.getAllWaypoints()) {
        WaypointListItemWidget listItemWidget = new WaypointListItemWidget(waypoint.meta());
        this.waypointList.addChild(listItemWidget);
        listItemWidgets.add(listItemWidget);

        listItemWidget.getCheckbox().setPressable(() -> {
          this.handleWaypointWidgetStyle(
              listItemWidget, !listItemWidget.getWaypointMeta().isVisible()
          );

          this.headerWidget.getCheckbox()
              .setState(this.hasVisibleWaypoint() ? State.CHECKED : State.UNCHECKED);
        });
      }

      this.waypointWidgets = listItemWidgets;

      container.addContent(this.headerWidget);
      container.addFlexibleContent(new ScrollWidget(this.waypointList));

      this.selectedWaypoint = this.waypointList.listSession().getSelectedEntry();

      HorizontalListWidget menu = new HorizontalListWidget();
      menu.addId("overview-button-menu");

      this.addButton = ButtonWidget.i18n("labymod.ui.button.add", () -> this.setAction(Action.ADD));
      this.addButton.setEnabled(Laby.labyAPI().minecraft().isIngame());
      menu.addEntry(this.addButton);

      this.editButton = ButtonWidget.i18n("labymod.ui.button.edit",
          () -> this.setAction(Action.EDIT));
      this.editButton.setEnabled(this.selectedWaypoint != null);
      menu.addEntry(this.editButton);

      this.removeButton = ButtonWidget.i18n(
          "labymod.ui.button.remove",
          () -> this.setAction(Action.REMOVE)
      );
      this.removeButton.setEnabled(this.selectedWaypoint != null);
      menu.addEntry(this.removeButton);

      container.addContent(menu);
    }

    this.document().addChild(container);
    if (this.action == null) {
      return;
    }

    DivWidget manageContainer = new DivWidget();
    manageContainer.addId("manage-container");

    Widget overlayWidget;
    switch (this.action) {
      default:
      case ADD:
        ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
        WaypointListItemWidget newWaypoint = new WaypointListItemWidget(
            WaypointBuilder.newBuilder()
                .title(Component.text("New Waypoint"))
                .color(Color.WHITE)
                .type(WaypointType.PERMANENT)
                .location(player != null ? new DoubleVector3(player.eyePosition()) : new DoubleVector3(0F, 80F, 0F))
                .visible(true)
                .world(this.waypointService.actualWorld())
                .server(this.waypointService.actualServer())
                .dimension(this.waypointService.actualDimension() != null
                    ? this.waypointService.actualDimension() : "labymod:unknown")
                .build()
        );

        this.inputWidget = new FlexibleContentWidget();
        ManageContainer manageContainerAdd = new ManageContainer(newWaypoint, this.manageTitle,
            this.modifier, this.inputWidget, this);
        overlayWidget = manageContainerAdd.initializeManageContainer();
        break;
      case EDIT:
        this.inputWidget = new FlexibleContentWidget();
        ManageContainer manageContainerWidgetEdit = new ManageContainer(this.selectedWaypoint,
            this.manageTitle, this.modifier, this.inputWidget, this);
        overlayWidget = manageContainerWidgetEdit.initializeManageContainer();
        break;
      case REMOVE:
        this.inputWidget = new FlexibleContentWidget();
        RemoveContainer removeContainer = new RemoveContainer(this.selectedWaypoint,
            this.waypointList, this.inputWidget, this);
        overlayWidget = removeContainer.initializeRemoveContainer();
        break;
    }

    manageContainer.addChild(overlayWidget);
    this.document().addChild(manageContainer);
  }

  public ArrayList<WaypointListItemWidget> getWaypointWidgets() {
    return waypointWidgets;
  }

  public boolean hasVisibleWaypoint() {
    for (Waypoint waypoint : this.waypointService.getAllWaypoints()) {
      if (waypoint.meta().isVisible()) {
        return true;
      }
    }
    return false;
  }

  public void handleWaypointWidgetStyle(WaypointListItemWidget waypointWidget, boolean visibility) {
    waypointWidget.getWaypointMeta().setVisible(visibility);
    waypointWidget.opacity().set(visibility ? 1F : 0.5F);
    waypointWidget.getCheckbox().setState(visibility ? State.CHECKED : State.UNCHECKED);
  }

  @Override
  public boolean mouseClicked(MutableMouse mouse, MouseButton mouseButton) {
    try {
      if (this.action != null) {
        return this.inputWidget.mouseClicked(mouse, mouseButton);
      }

      return super.mouseClicked(mouse, mouseButton);
    } finally {
      if (this.overview) {
        this.selectedWaypoint = this.waypointList.listSession().getSelectedEntry();
        this.removeButton.setEnabled(this.selectedWaypoint != null);
        this.editButton.setEnabled(this.selectedWaypoint != null);
      }
    }
  }

  @Override
  public boolean keyPressed(Key key, InputType type) {
    if (key.getId() == 256 && this.action != null) {
      this.setAction(null);
      return true;
    }

    return super.keyPressed(key, type);
  }

  public void setAction(Action action) {
    this.action = action;

    if (!this.overview) {
      this.displayPreviousScreen();
      return;
    }

    if (this.isOpen()) {
      this.reload();
    }
  }

  public void setManageTitle(Component manageTitle) {
    this.manageTitle = manageTitle;
  }

  public void setModifier(Consumer<WaypointMeta> modifier) {
    this.modifier = modifier;
  }

  public enum Action {
    ADD, EDIT, REMOVE
  }
}