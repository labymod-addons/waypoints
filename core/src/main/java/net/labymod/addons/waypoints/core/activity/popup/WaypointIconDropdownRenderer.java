package net.labymod.addons.waypoints.core.activity.popup;

import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.renderer.EntryRenderer;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import org.jetbrains.annotations.NotNull;

public class WaypointIconDropdownRenderer implements EntryRenderer<WaypointIcon> {

  @Override
  public float getWidth(WaypointIcon entry, float maxWidth) {
    return entry.getScaledWidth(this.getHeight(entry));
  }

  @Override
  public float getHeight(WaypointIcon entry, float maxWidth) {
    return this.getHeight(entry);
  }

  @Override
  public @NotNull Widget createEntryWidget(WaypointIcon entry) {
    IconWidget iconWidget = new IconWidget(entry.icon()).addId("waypoint-icon-with-vars");
    iconWidget.setVariable("--height", this.getHeight(entry));
    iconWidget.setVariable("--width", entry.getScaledWidth(this.getHeight(entry)));
    return iconWidget;
  }

  private float getHeight(WaypointIcon icon) {
    return Math.min(16, icon.getHeight());
  }
}
