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

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.addons.waypoints.waypoint.WaypointType;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;

@AutoWidget
public class WaypointWidget extends SimpleWidget {

  private final WaypointMeta meta;
  private final IconWidget icon = new IconWidget(WaypointTextures.MARKER_ICON);
  private final ComponentWidget title;
  private final WaypointObjectMeta worldObjectMeta;

  public WaypointWidget(WaypointMeta meta, WaypointObjectMeta worldObjectMeta) {
    this.meta = meta;
    this.worldObjectMeta = worldObjectMeta;

    //todo distance
    this.title = ComponentWidget.component(this.meta.title());
    this.title.addId("title");
  }

  public WaypointWidget(WaypointMeta meta) {
    this(meta, null);
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.title.textColor().set(this.meta.getColor().get());
    this.addChild(this.title);

    this.icon.color().set(this.meta.getColor().get());
    this.icon.addId("icon");
    this.addChild(this.icon);

    if (this.meta.getType() == WaypointType.SERVER_SESSION) {
      IconWidget typeWidget = new IconWidget(Textures.SpriteCommon.EXCLAMATION_MARK_LIGHT);

      typeWidget.addId("type");
      typeWidget.setHoverComponent(Component.translatable("labyswaypoints.gui.overview.temporary"));

      this.addChild(typeWidget);
    }
  }

  @Override
  public void tick() {
    super.tick();
  }
}