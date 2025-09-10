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

package net.labymod.addons.waypoints.core.activity.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import net.labymod.addons.waypoints.WaypointService;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.activity.widgets.IconPickerWidget;
import net.labymod.addons.waypoints.core.activity.widgets.WaypointWidget;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointBuilder;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.NumberTextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.popup.SimpleAdvancedPopup;
import net.labymod.api.util.I18n;
import net.labymod.api.util.math.position.Position;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Link("overview.lss")
@Link("manage-popup.lss")
public class ManageWaypointSimplePopup extends SimpleAdvancedPopup {

  private final Action action;
  private final SimplePopupButton doneButton;
  private final List<Condition> conditions = new ArrayList<>();
  private final WaypointMeta waypoint;
  private final WaypointWidget waypointWidget;

  private Consumer<Waypoint> saveListener;

  protected ManageWaypointSimplePopup(
      @NotNull Action action,
      @NotNull WaypointMeta waypoint,
      @Nullable WaypointObjectMeta worldObjectMeta
  ) {
    this.action = action;
    this.title = switch (action) {
      case ADD -> Component.text("Create Waypoint");
      case EDIT -> Component.text("Edit Waypoint");
    };

    this.waypoint = waypoint;
    this.doneButton = SimplePopupButton.create(
        Component.translatable("labymod.ui.button.save"),
        button -> this.saveWaypoint()
    );

    this.buttons = new ArrayList<>();
    this.buttons.add(SimplePopupButton.cancel());
    this.buttons.add(this.doneButton);

    if (worldObjectMeta == null) {
      this.waypointWidget = null;
    } else {
      this.waypointWidget = new WaypointWidget(waypoint, worldObjectMeta);
    }
  }

  public ManageWaypointSimplePopup() {
    this(Action.ADD, createDefaultBuilder().build(), null);
  }

  public ManageWaypointSimplePopup(@NotNull WaypointMeta waypoint) {
    this(Action.EDIT, waypoint.copy(), null);
  }

  public ManageWaypointSimplePopup(@NotNull Waypoint waypoint) {
    this(Action.EDIT, waypoint.meta().copy(), waypoint.waypointObjectMeta());
  }

  private static @NotNull WaypointBuilder createDefaultBuilder() {
    ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
    if (player == null) {
      throw new IllegalStateException("Player is null");
    }

    String name = I18n.getTranslation("labyswaypoints.defaultName");
    if (name == null) {
      name = "New Waypoint";
    }

    Position position = player.position();
    return WaypointBuilder.create()
        .title(Component.text(name))
        .type(WaypointType.PERMANENT)
        .location(new DoubleVector3(
            ((int) position.getX() + 1) - 0.5D,
            (int) (position.getY() + player.getEyeHeight()),
            ((int) position.getZ()) - 0.5D
        ))
        .applyCurrentContext()
        .currentDimension();
  }

  @Override
  protected void initializeCustomWidgets(VerticalListWidget<Widget> container) {
    this.conditions.clear();

    if (this.waypointWidget != null) {
      container.addChild(this.waypointWidget);
    }

    FlexibleContentWidget nameWrapper = new FlexibleContentWidget();
    nameWrapper.addId("horizontal-wrapper");

    nameWrapper.addContent(this.createLabeledWidget(
        Component.text("Icon"),
        () -> {
          IconPickerWidget iconPicker = new IconPickerWidget();
          iconPicker.addId("icon-picker");
          iconPicker.addAll(WaypointIcon.getDefaultIcons());
          iconPicker.setSelected(this.waypoint.icon());
          iconPicker.setChangeListener(icon -> {
            this.waypoint.setIcon(icon);
            if (this.waypointWidget != null) {
              this.waypointWidget.updateIcon();
            }
          });
          return iconPicker;
        }
    ));

    nameWrapper.addFlexibleContent(this.createLabeledWidget(
        Component.text("Name"),
        () -> {
          String text = PlainTextComponentSerializer.plainText().serialize(this.waypoint.title());
          Condition condition = new Condition(!text.isEmpty());
          this.conditions.add(condition);

          TextFieldWidget nameInput = new TextFieldWidget();
          nameInput.clearButton().set(true);
          nameInput.setText(text);
          nameInput.placeholder(Component.text("Enter a name"));
          nameInput.updateListener(newValue -> {
            newValue = newValue.trim();
            this.waypoint.setTitle(Component.text(newValue));
            if (this.waypointWidget != null) {
              this.waypointWidget.updateTitle();
            }

            this.updateCondition(condition, !newValue.isEmpty());
          });

          return nameInput;
        }
    )).addId("flexible");

    nameWrapper.addContent(this.createLabeledWidget(
        Component.text("Color"),
        () -> {
          ColorPickerWidget colorPickerWidget = ColorPickerWidget.of(this.waypoint.color());
          colorPickerWidget.addUpdateListener(this, color -> {
            this.waypoint.setColor(color);
            if (this.waypointWidget != null) {
              this.waypointWidget.updateColor();
            }
          });

          return colorPickerWidget;
        }
    ));
    container.addChild(nameWrapper);

    FlexibleContentWidget positionWrapper = new FlexibleContentWidget();
    positionWrapper.addId("horizontal-wrapper");
    positionWrapper.addFlexibleContent(this.createPositionWidget(
        "X",
        this.waypoint.location().getX(),
        value -> this.waypoint.location().setX(value)
    ));

    positionWrapper.addFlexibleContent(this.createPositionWidget(
        "Y",
        this.waypoint.location().getY(),
        value -> this.waypoint.location().setY(value)
    ));

    positionWrapper.addFlexibleContent(this.createPositionWidget(
        "Z",
        this.waypoint.location().getZ(),
        value -> this.waypoint.location().setZ(value)
    ));
    container.addChild(positionWrapper);

    this.doneButton.enabled(this.allConditionsMet());
  }

  @Override
  protected void initializeButtons(VerticalListWidget<Widget> container) {
    super.initializeButtons(container);
  }

  private Widget createPositionWidget(
      String label,
      double defaultValue,
      DoubleConsumer valueConsumer
  ) {
    return this.createLabeledWidget(
        Component.text(label),
        () -> {
          NumberTextFieldWidget textField = new NumberTextFieldWidget();
          textField.setValue((int) defaultValue);
          textField.onUpdate(newValue -> {
            double value = newValue;
            if (label.equals("X") || label.equals("Z")) {
              value += 0.5;
            }

            valueConsumer.accept(value);
          });

          return textField;
        }
    );
  }

  private Widget createLabeledWidget(Component label, Supplier<Widget> widgetSupplier) {
    FlexibleContentWidget labelContainer = new FlexibleContentWidget();
    labelContainer.addId("labeled-widget");
    labelContainer.addContent(ComponentWidget.component(label));
    labelContainer.addContent(widgetSupplier.get());
    return labelContainer;
  }

  private void updateCondition(Condition condition, boolean fulfilled) {
    condition.fulfilled = fulfilled;
    this.doneButton.enabled(this.allConditionsMet());
  }

  private boolean allConditionsMet() {
    for (Condition condition : this.conditions) {
      if (!condition.fulfilled) {
        return false;
      }
    }

    return true;
  }

  private void saveWaypoint() {
    WaypointService waypointService = Waypoints.references().waypointService();
    Waypoint waypoint;
    if (this.action == Action.ADD) {
      waypoint = waypointService.add(this.waypoint);
    } else {
      waypoint = waypointService.update(this.waypoint);
    }

    waypointService.refresh();
    if (this.saveListener != null) {
      this.saveListener.accept(waypoint);
    }
  }

  public ManageWaypointSimplePopup onSave(Consumer<Waypoint> waypoint) {
    this.saveListener = waypoint;
    return this;
  }

  public ManageWaypointSimplePopup onSave(Runnable runnable) {
    return this.onSave(saved -> runnable.run());
  }

  public enum Action {
    ADD,
    EDIT
  }

  private static class Condition {

    boolean fulfilled;

    Condition(boolean defaultFulfilled) {
      this.fulfilled = defaultFulfilled;
    }
  }
}
