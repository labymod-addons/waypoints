package net.labymod.addons.waypoints.core.activity.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.gui.screen.widget.overlay.WidgetReference;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.GridWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.client.gui.window.Window;
import net.labymod.api.util.bounds.ModifyReason;
import net.labymod.api.util.bounds.MutableRectangle;
import org.jetbrains.annotations.NotNull;

@Link("manage-popup.lss")
@AutoWidget
public class IconPickerWidget extends ButtonWidget {

  private static final ModifyReason DROPDOWN_POSITION = ModifyReason.of("dropdownPosition");

  private final List<Icon> icons = new ArrayList<>();
  private Icon selectedIcon;

  private Consumer<Icon> changeListener;

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.icon().set(this.selectedIcon);
  }

  @Override
  public boolean onPress() {
    GridWidget<IconWidget> grid = new GridWidget<>();
    grid.addId("icon-picker-grid");
    for (Icon icon : this.icons) {
      icon.aspectRatio(1);

      IconWidget iconWidget = new IconWidget(icon);
      iconWidget.setPressable(() -> {
        this.setSelected(icon);

        if (this.changeListener != null) {
          this.changeListener.accept(icon);
        }
      });
      grid.addChild(iconWidget);
    }
    WidgetReference reference = this.displayInOverlay(grid);
    reference.boundsUpdater((ref, bounds) -> {
      // Get drop up type
      Window window = this.labyAPI.minecraft().minecraftWindow();
      MutableRectangle attachTo = Bounds.absoluteBounds(this);
      boolean dropUp = attachTo.getY() > window.getScaledHeight() / 2.0F;

      // Get the width and height of the entire list
      float padding = 7;
      float width = attachTo.getWidth() - padding;
      float height = ref.widget().getEffectiveHeight();

      // Update size
      bounds.setSize(width, height, DROPDOWN_POSITION);

      // Update position
      float x = attachTo.getX() + padding;
      float y = attachTo.getY() + (dropUp ? -height - 5 : attachTo.getHeight() + 5);
      bounds.setPosition(x, y, DROPDOWN_POSITION);

      grid.updateBounds();
    });

    return super.onPress();
  }

  public Icon getSelectedIcon() {
    return this.selectedIcon;
  }

  public void setSelected(@NotNull Icon icon) {
    this.selectedIcon = icon;
    this.updateIcon(icon);
  }

  public void addAll(Collection<Icon> icons) {
    this.icons.addAll(icons);
  }

  public void setChangeListener(Consumer<Icon> callback) {
    this.changeListener = callback;
  }

}
