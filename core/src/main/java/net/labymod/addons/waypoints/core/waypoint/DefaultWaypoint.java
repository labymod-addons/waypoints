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

package net.labymod.addons.waypoints.core.waypoint;

import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointIcon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.client.gfx.GlConst;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.util.WidgetMeta;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private static final float BACKGROUND_DEPTH = 0.01F;
  private static final float WAYPOINT_SCALE = 0.04F;

  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.labyAPI().renderPipeline()
      .rectangleRenderer();
  private static final ComponentRenderer COMPONENT_RENDERER = Laby.labyAPI().renderPipeline()
      .componentRenderer();

  private final WidgetMeta widgetMeta = new WidgetMeta();
  private final WaypointObjectMeta waypointObjectMeta;
  private final WaypointMeta meta;
  private final WaypointsAddon addon;
  private float marginBetweenTextAndIcon;
  private float iconWidth;

  private float rectX;
  private float rectY;

  public DefaultWaypoint(
      WaypointsAddon addon,
      WaypointMeta meta,
      WaypointObjectMeta waypointObjectMeta
  ) {
    super(waypointObjectMeta.pos());

    this.addon = addon;
    this.waypointObjectMeta = waypointObjectMeta;
    this.meta = meta;
  }

  @Override
  public WaypointMeta meta() {
    return this.meta;
  }

  @Override
  public WaypointObjectMeta waypointObjectMeta() {
    return this.waypointObjectMeta;
  }

  @Override
  public boolean shouldRender() {
    return !this.waypointObjectMeta.isOutOfRange();
  }

  @Override
  public boolean shouldRenderInOverlay() {
    return this.addon.configuration().showHudIndicators().get();
  }

  @Override
  public @Nullable Widget createWidget() {
    return new WaypointIndicatorWidget(this);
  }

  @Override
  public void renderInWorld(
      @NotNull MinecraftCamera cam,
      @NotNull Stack stack,
      double x,
      double y,
      double z,
      float delta,
      boolean darker
  ) {
    stack.push();

    float alpha = this.waypointObjectMeta.getAlpha();
    if (alpha != 1.0F) {
      this.widgetMeta.multiplyAlpha(alpha);
    }

    GFXBridge gfx = Laby.gfx();

    stack.scale(WAYPOINT_SCALE * this.waypointObjectMeta.getScale());

    this.rotateHorizontally(cam, stack);
    this.rotateVertically(cam, stack);

    stack.push();
    stack.translate(0, 0, BACKGROUND_DEPTH);
    gfx.depthFunc(GlConst.GL_NEVER);

    this.renderBackground(stack, 1F);

    gfx.depthFunc(GlConst.GL_LEQUAL);
    stack.pop();

    this.renderIcon(stack);
    this.renderText(stack);

    if (alpha != 1.0F) {
      this.widgetMeta.revertAlphaState();
    }

    stack.pop();
  }

  public void renderBackground(Stack stack, float padding) {
    Component text = this.waypointObjectMeta().formatTitle();

    this.marginBetweenTextAndIcon = this.iconWidth == 0F ? 0F : 4F;

    this.rectX =
        (COMPONENT_RENDERER.width(text) + this.iconWidth + this.marginBetweenTextAndIcon - 1) / 2;
    this.rectY = COMPONENT_RENDERER.height() / 2;

    if (!this.addon.configuration().background().get()) {
      return;
    }

    RECTANGLE_RENDERER
        .pos(
            -this.rectX - padding,
            -this.rectY - padding,
            this.rectX + padding,
            this.rectY + padding - 1F
        )
        .color(Colors.BACKGROUND_COLOR)
        .render(stack);
  }

  public void renderIcon(Stack stack) {
    if (!this.addon.configuration().icon().get()) {
      this.iconWidth = 0F;
      return;
    }

    WaypointIcon icon = this.meta().icon();
    float renderHeight = COMPONENT_RENDERER.height() - 1;
    this.iconWidth = icon.getScaledWidth(renderHeight);

    icon.icon().render(
        stack,
        -this.rectX /*- this.iconWidth - this.marginBetweenTextAndIcon - 1*/,
        -this.rectY,
        this.iconWidth,
        renderHeight,
        false,
        this.meta().getColor().get()
    );
  }

  public void renderText(Stack stack) {
    Component text = this.waypointObjectMeta().formatTitle();

    // render twice to fix that clouds are rendered in front of the text
    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .pos(-this.rectX + this.iconWidth + this.marginBetweenTextAndIcon, -this.rectY)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .render(stack);
    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .discrete(true)
        .pos(-this.rectX + this.iconWidth + this.marginBetweenTextAndIcon, -this.rectY)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .render(stack);
  }
}