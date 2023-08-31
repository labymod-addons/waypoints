package net.labymod.addons.waypoints.activity;

import java.util.function.Consumer;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
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
  private final VerticalListWidget<WaypointWidget> waypointList;

  private WaypointWidget selectedWaypoint;

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
    this.waypointList.addId("waypoints-list");
    this.waypointList.setSelectCallback(waypointWidget -> {
      WaypointWidget selectedWidget = this.waypointList.session().getSelectedEntry();
      if (selectedWidget == null
          || selectedWidget.getWaypoint() != waypointWidget.getWaypoint()) {
        this.editButton.setEnabled(true);
        this.removeButton.setEnabled(true);
      }
    });

    this.waypointList.setDoubleClickCallback(waypointWidget -> this.setAction(Action.EDIT));
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    FlexibleContentWidget container = new FlexibleContentWidget();
    container.addId("waypoints-container");

    if (this.overview) {
      for (Waypoint waypoint : this.waypointService.getAllWaypoints()) {
        this.waypointList.addChild(new WaypointWidget(waypoint.meta()));
      }

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
        WaypointWidget newWaypoint = new WaypointWidget(
            WaypointBuilder.newBuilder()
                .title(Component.text("New Waypoint"))
                .color(Color.WHITE)
                .type(WaypointType.PERMANENT)
                .location(player != null ? player.eyePosition() : new FloatVector3(0F, 80F, 0F))
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

  private FlexibleContentWidget initializeRemoveContainer(WaypointWidget waypointWidget) {
    this.inputWidget = new FlexibleContentWidget();
    this.inputWidget.addId("remove-container");

    ComponentWidget confirmationWidget = ComponentWidget.i18n(
        "labyswaypoints.gui.manage.remove.title");
    confirmationWidget.addId("remove-confirmation");
    this.inputWidget.addContent(confirmationWidget);

    WaypointWidget previewWidget = new WaypointWidget(waypointWidget.getWaypoint());
    previewWidget.addId("remove-preview");
    this.inputWidget.addContent(previewWidget);

    HorizontalListWidget menu = new HorizontalListWidget();
    menu.addId("remove-button-menu");

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.remove", () -> {
      this.waypointService.removeWaypoint(waypointWidget.getWaypoint());
      System.out.println();
      this.waypointList.session().setSelectedEntry(null);
      this.setAction(null);
    }));

    menu.addEntry(ButtonWidget.i18n("labymod.ui.button.cancel", () -> this.setAction(null)));
    this.inputWidget.addContent(menu);

    return this.inputWidget;
  }

  private DivWidget initializeManageContainer(WaypointWidget waypointWidget) {
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

    WaypointMeta waypointMeta = waypointWidget.getWaypoint();

    DivWidget nameLabelList = new DivWidget();
    nameLabelList.addId("input-name-list");
    nameLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.name"))
        .addId("input-label");

    TextFieldWidget nameInput = new TextFieldWidget();
    nameInput.addId("input-text");
    nameInput.setText(PlainTextComponentSerializer.plainText().serialize(waypointMeta.getTitle()));
    nameInput.maximalLength(50);
    nameInput.updateListener(newValue -> doneButton.setEnabled(!newValue.trim().isEmpty()));
    nameLabelList.addChild(nameInput);

    this.inputWidget.addContent(nameLabelList);

    DivWidget colorLabelList = new DivWidget();
    colorLabelList.addId("input-name-list");
    colorLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.color"))
        .addId("input-label");

    ColorPickerWidget colorPicker = ColorPickerWidget.of(waypointMeta.getColor());
    colorPicker.addId("input-color");
    colorLabelList.addChild(colorPicker);

    this.inputWidget.addContent(colorLabelList);

    DivWidget xLabelList = new DivWidget();
    xLabelList.addId("input-name-list");
    xLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.x"))
        .addId("input-label");

    TextFieldWidget xInput = new TextFieldWidget();
    xInput.addId("input-text");
    xInput.setText(String.valueOf((int) waypointMeta.getLocation().getX()));
    xLabelList.addChild(xInput);

    this.inputWidget.addContent(xLabelList);

    DivWidget yLabelList = new DivWidget();
    yLabelList.addId("input-name-list");
    yLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.y"))
        .addId("input-label");

    TextFieldWidget yInput = new TextFieldWidget();
    yInput.addId("input-text");
    yInput.setText(String.valueOf((int) waypointMeta.getLocation().getY()));
    yLabelList.addChild(yInput);

    this.inputWidget.addContent(yLabelList);

    DivWidget zLabelList = new DivWidget();
    zLabelList.addId("input-name-list");
    zLabelList.addChild(ComponentWidget.i18n("labyswaypoints.gui.manage.z"))
        .addId("input-label");

    TextFieldWidget zInput = new TextFieldWidget();
    zInput.addId("input-text");
    zInput.setText(String.valueOf((int) waypointMeta.getLocation().getZ()));
    zLabelList.addChild(zInput);

    this.inputWidget.addContent(zLabelList);

    HorizontalListWidget buttonList = new HorizontalListWidget();
    buttonList.addId("edit-button-menu");

    doneButton.setEnabled(!nameInput.getText().trim().isEmpty());
    doneButton.setPressable(() -> {
      // Remove the old waypoint in case this is an edit( or the exact same waypoint already exists)
      boolean permanent = this.waypointService.removeWaypoint(waypointMeta);

      try {
        waypointMeta.setLocation(new FloatVector3(
            Integer.parseInt(xInput.getText()),
            Integer.parseInt(yInput.getText()),
            Integer.parseInt(zInput.getText())
        ));
      } catch (NumberFormatException ignored) {
        return;
      }

      waypointMeta.setTitle(Component.text(nameInput.getText()));
      waypointMeta.setColor(colorPicker.value());
      if (permanent) {
        waypointMeta.setType(WaypointType.PERMANENT);
      }

      if (this.modifier != null) {
        this.modifier.accept(waypointMeta);
      }

      this.waypointService.addWaypoint(waypointMeta);

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