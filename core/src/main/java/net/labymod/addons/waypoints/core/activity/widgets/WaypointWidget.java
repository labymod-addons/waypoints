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

package net.labymod.addons.waypoints.core.activity.widgets;

import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;

@AutoWidget
public class WaypointWidget extends FlexibleContentWidget {

  protected final WaypointMeta meta;
  protected final WaypointObjectMeta worldObjectMeta;

  private IconWidget iconWidget;
  private ComponentWidget titleWidget;
  private ComponentWidget distanceWidget;
  private double lastDistance = Double.MIN_VALUE;

  public WaypointWidget(WaypointMeta meta, WaypointObjectMeta worldObjectMeta) {
    this.meta = meta;
    this.worldObjectMeta = worldObjectMeta;
    this.addId("waypoint-widget");
  }

  public WaypointWidget(WaypointMeta meta) {
    this(meta, null);
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.iconWidget = new IconWidget(this.meta.icon());

    this.iconWidget.color().set(this.meta.color().get());
    this.iconWidget.addId("icon", "waypoint-icon");
    this.addContent(this.iconWidget);

    this.titleWidget = ComponentWidget.component(this.meta.title());
    this.titleWidget.addId("title");
    this.titleWidget.textColor().set(this.meta.color().get());
    this.addFlexibleContent(this.titleWidget);

    if (this.meta.type() == WaypointType.SERVER_SESSION) {
      IconWidget typeWidget = new IconWidget(Textures.SpriteCommon.EXCLAMATION_MARK_LIGHT);

      typeWidget.addId("type");
      typeWidget.setHoverComponent(Component.translatable("labyswaypoints.gui.overview"
          + ".temporary"));

      this.addContent(typeWidget);
    }

    if (this.worldObjectMeta == null) {
      this.distanceWidget = null;
    } else {
      this.lastDistance = this.worldObjectMeta.getDistance();
      this.distanceWidget = ComponentWidget.component(
          this.worldObjectMeta.createDistanceComponent()
      );

      this.addContent(this.distanceWidget);
    }
  }

  @Override
  public void tick() {
    super.tick();
    this.updateDistance();
  }

  public void updateTitle() {
    if (this.titleWidget == null) {
      return;
    }

    this.titleWidget.setComponent(this.meta.title());
  }

  public void updateColor() {
    if (this.iconWidget != null) {
      this.iconWidget.color().set(this.meta.color().get());
    }

    if (this.titleWidget != null) {
      this.titleWidget.textColor().set(this.meta.color().get());
    }
  }

  public void updateDistance() {
    if (this.distanceWidget == null || this.worldObjectMeta == null) {
      return;
    }

    double distance = this.worldObjectMeta.getDistance();
    if (this.lastDistance == distance) {
      return;
    }

    this.lastDistance = distance;
    this.distanceWidget.setComponent(this.worldObjectMeta.createDistanceComponent());
  }

  public void updateIcon() {
    if (this.iconWidget == null) {
      return;
    }

    this.iconWidget.icon().set(this.meta.icon());
  }
}