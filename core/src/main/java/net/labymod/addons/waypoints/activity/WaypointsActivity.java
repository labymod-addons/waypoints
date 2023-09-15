package net.labymod.addons.waypoints.activity;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.labymod.addons.waypoints.WaypointsConfiguration;
import net.labymod.addons.waypoints.activity.widgets.HeaderWidget;
import net.labymod.addons.waypoints.activity.widgets.WaypointListItemWidget;
import net.labymod.addons.waypoints.activity.widgets.WaypointWidget;
import net.labymod.addons.waypoints.utils.WidgetUtils;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
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
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget.State;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.vector.FloatVector3;

@AutoActivity
@Link("manage.lss")
@Link("overview.lss")
public class WaypointsActivity extends Activity {

  private final WaypointService waypointService;
  private final boolean overview;
  private final VerticalListWidget<WaypointListItemWidget> waypointList;
  private WaypointListItemWidget selectedWaypoint;
  private ButtonWidget removeButton;
  private ButtonWidget editButton;
  private FlexibleContentWidget inputWidget;
  private Action action;
  private Component manageTitle;
  private Consumer<WaypointMeta> modifier;
  private final HeaderWidget headerWidget;
  private final WaypointsConfiguration configuration;

  public WaypointsActivity(boolean overview, WaypointsConfiguration configuration) {
    this.overview = overview;
    this.waypointService = Waypoints.getReferences().waypointService();

    this.waypointList = new VerticalListWidget<>();
    this.waypointList.addId("waypoints-list");
    this.headerWidget = new HeaderWidget(configuration);
    this.configuration = configuration;
    this.waypointList.setSelectCallback(waypointListItemWidget -> {
      WaypointListItemWidget selectedWidget = this.waypointList.session().getSelectedEntry();
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
          listItemWidget.getWaypointMeta().setVisible(!listItemWidget.getWaypointMeta().isVisible());
          listItemWidget.opacity().set(listItemWidget.getWaypointMeta().isVisible() ? 1F : 0.5F);

          if (listItemWidget.getCheckbox().state() == State.CHECKED) {
            headerWidget.getCheckbox().setState(State.CHECKED);
          }

          if (!WidgetUtils.hasVisibleWaypoint()) {
            headerWidget.getCheckbox().setState(State.UNCHECKED);
          }
        });
      }

      WidgetUtils.setWaypointWidgets(listItemWidgets);

      container.addContent(headerWidget);
      container.addFlexibleContent(new ScrollWidget(this.waypointList));

      this.selectedWaypoint = this.waypointList.session().getSelectedEntry();

      HorizontalListWidget menu = new HorizontalListWidget();
      menu.addId("overview-button-menu");

      menu.addEntry(ButtonWidget.i18n("labymod.ui.button.add", () -> this.setAction(Action.ADD)));

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
                .location(player != null ? player.eyePosition() : new FloatVector3(0F, 80F, 0F))
                .visible(true)
                .world("PLACEHOLDER")
                .build()
        );
        overlayWidget = this.initializeManageContainer(newWaypoint);
        break;
      case EDIT:
        overlayWidget = this.initializeManageContainer(this.selectedWaypoint);
        break;
      case REMOVE:
        overlayWidget = this.initializeRemoveContainer(this.selectedWaypoint);
        break;
    }

    manageContainer.addChild(overlayWidget);
    this.document().addChild(manageContainer);
  }

  private FlexibleContentWidget initializeRemoveContainer(
      WaypointListItemWidget waypointListItemWidget) {
    this.inputWidget = new FlexibleContentWidget();
    this.inputWidget.addId("remove-container");

    ComponentWidget confirmationWidget = ComponentWidget.i18n(
        "labyswaypoints.gui.manage.remove.title");
    confirmationWidget.addId("remove-confirmation");
    this.inputWidget.addContent(confirmationWidget);

    WaypointWidget previewWidget = new WaypointWidget(waypointListItemWidget.getWaypointMeta());
    previewWidget.addId("remove-preview");
    this.inputWidget.addContent(previewWidget);

    HorizontalListWidget menu = new HorizontalListWidget();
    menu.addId("remove-button-menu");

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.remove", () -> {
      this.waypointService.removeWaypoint(waypointListItemWidget.getWaypointMeta());
      this.waypointList.session().setSelectedEntry(null);
      this.setAction(null);
    }));

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.setAction(null)));
    this.inputWidget.addContent(menu);

    return this.inputWidget;
  }

  private DivWidget initializeManageContainer(WaypointListItemWidget waypointListItemWidget) {
    ButtonWidget doneButton = ButtonWidget.i18n("labymod.ui.button.done");

    DivWidget inputContainer = new DivWidget();
    inputContainer.addId("input-container");

    if (this.manageTitle != null) {
      ComponentWidget manageTitle = ComponentWidget.component(this.manageTitle);
      manageTitle.addId("title");
      inputContainer.addChild(manageTitle);
    }

    this.inputWidget = new FlexibleContentWidget();
    this.inputWidget.addId("input-list");

    WaypointMeta meta = waypointListItemWidget.getWaypointMeta();

    DivWidget nameLabelList = new DivWidget();
    nameLabelList.addId("input-name-list");
    nameLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.name"))
        .addId("input-label");

    TextFieldWidget nameInput = new TextFieldWidget();
    nameInput.addId("input-text");
    nameInput.setText(PlainTextComponentSerializer.plainText().serialize(meta.getTitle()));
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
    xInput.setText(String.valueOf((int) meta.getLocation().getX()));
    xLabelList.addChild(xInput);

    this.inputWidget.addContent(xLabelList);

    DivWidget yLabelList = new DivWidget();
    yLabelList.addId("input-name-list");
    yLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.y"))
        .addId("input-label");

    TextFieldWidget yInput = new TextFieldWidget();
    yInput.addId("input-text");
    yInput.setText(String.valueOf((int) meta.getLocation().getY()));
    yLabelList.addChild(yInput);

    this.inputWidget.addContent(yLabelList);

    DivWidget zLabelList = new DivWidget();
    zLabelList.addId("input-name-list");
    zLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.z"))
        .addId("input-label");

    TextFieldWidget zInput = new TextFieldWidget();
    zInput.addId("input-text");
    zInput.setText(String.valueOf((int) meta.getLocation().getZ()));
    zLabelList.addChild(zInput);

    this.inputWidget.addContent(zLabelList);

    HorizontalListWidget buttonList = new HorizontalListWidget();
    buttonList.addId("edit-button-menu");

    doneButton.setEnabled(!nameInput.getText().trim().isEmpty());
    doneButton.setPressable(() -> {
      // Remove the old waypoint in case this is an edit( or the exact same waypoint already exists)
      boolean permanent = this.waypointService.removeWaypoint(meta);

      try {
        meta.setLocation(new FloatVector3(
            Integer.parseInt(xInput.getText()),
            Integer.parseInt(yInput.getText()),
            Integer.parseInt(zInput.getText())
        ));
      } catch (NumberFormatException ignored) {
        return;
      }

      meta.setTitle(Component.text(nameInput.getText()));
      meta.setColor(colorPicker.value());
      if (permanent) {
        meta.setType(WaypointType.PERMANENT);
      }

      if (this.modifier != null) {
        this.modifier.accept(meta);
      }

      this.waypointService.addWaypoint(meta);

      this.setAction(null);
    });

    buttonList.addEntry(doneButton);

    buttonList.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.setAction(null)));
    inputContainer.addChild(this.inputWidget);
    this.inputWidget.addContent(buttonList);
    return inputContainer;
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
        this.selectedWaypoint = this.waypointList.session().getSelectedEntry();
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