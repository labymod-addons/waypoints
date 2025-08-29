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

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.core.WaypointsAddon;
import net.labymod.addons.waypoints.core.WaypointsRenderPrograms;
import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.addons.waypoints.waypoint.Waypoint;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.util.WidgetMeta;
import net.labymod.api.client.render.batch.RectangleRenderContext;
import net.labymod.api.client.render.draw.batch.BatchResourceRenderer;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private static final float BACKGROUND_DEPTH = 0.01F;
  private static final float WAYPOINT_SCALE = 0.04F;

  private static final float ICON_SIZE = 8;
  private static final float GAP = 3;

  private static final ResourceLocation BEACON_BEAM = Waypoints.ofPath("textures/beacon_beam.png");

  private static final RectangleRenderContext RECTANGLE_RENDER_CONTEXT = Laby.references()
      .rectangleRenderContext();
  private static final ComponentRenderer COMPONENT_RENDERER = Laby.references().componentRenderer();

  private final WidgetMeta widgetMeta = new WidgetMeta();
  private final WaypointObjectMeta waypointObjectMeta;
  private final WaypointMeta meta;
  private final WaypointsAddon addon;
  private final DoubleVector3 prevPosition;
  private float rectX;
  private float rectY;
  private boolean hasPrevPosition;

  public DefaultWaypoint(
      WaypointsAddon addon,
      WaypointMeta meta,
      WaypointObjectMeta waypointObjectMeta
  ) {
    super(waypointObjectMeta.pos().copy());
    this.prevPosition = new DoubleVector3();

    this.addon = addon;
    this.waypointObjectMeta = waypointObjectMeta;
    this.meta = meta;
  }

  @Override
  public WaypointMeta meta() {
    return this.meta;
  }

  @Override
  public @NotNull DoubleVector3 previousPosition() {
    return this.prevPosition;
  }

  public boolean hasPrevPosition() {
    return this.hasPrevPosition;
  }

  public void applyPreviousPosition() {
    this.prevPosition.set(this.position());
    this.hasPrevPosition = true;
  }

  public void setHasPrevPosition(boolean hasPrevPosition) {
    this.hasPrevPosition = hasPrevPosition;
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
    this.renderBeaconBeam(cam, stack, x, y, z, delta);
    stack.pop();

    stack.push();
    float alpha = this.waypointObjectMeta.getAlpha();
    if (alpha != 1.0F) {
      this.widgetMeta.multiplyAlpha(alpha);
    }

    stack.scale(WAYPOINT_SCALE * this.waypointObjectMeta.getScale());

    this.rotateHorizontally(cam, stack);
    this.rotateVertically(cam, stack);

    stack.push();
    stack.translate(0, 0, BACKGROUND_DEPTH);

    this.renderBackground(stack, 1F);

    stack.pop();

    this.renderIcon(stack);
    this.renderText(stack);

    if (alpha != 1.0F) {
      this.widgetMeta.revertAlphaState();
    }
    stack.pop();
  }

  private void renderBeaconBeam(
      @NotNull MinecraftCamera cam,
      @NotNull Stack stack,
      double x, double y, double z,
      float delta
  ) {
    if (!this.addon.configuration().beaconBeam().get()) {
      return;
    }

    float size = 0.2F;
    float rotation = System.currentTimeMillis() % 3600 / 20F;
    float upwards = System.currentTimeMillis() % 2000 / 1000F * size;
    int color = this.meta.color().get();

    stack.rotate(rotation, 0, 1, 0);
    stack.translate(-size / 2, -upwards, -size / 2);

    BatchResourceRenderer renderer = Laby.labyAPI()
        .renderPipeline()
        .resourceRenderer()
        .beginBatch(stack, BEACON_BEAM);
    for (int i = 0; i < 4; i++) {
      stack.rotate(90, 0, 1, 0);
      stack.translate(-size, 0, 0);

      for (int h = -256; h < 2048; h++) {
        renderer.pos(0, -h * size)
            .size(size, size)
            .color(color)
            .sprite(0, 0, 256, 256)
            .build();
      }
    }
    renderer.upload(WaypointsRenderPrograms.BEACON_BEAM);
  }

  public void renderBackground(Stack stack, float padding) {
    Component text = this.waypointObjectMeta().formatTitle();

    float iconWidth = this.addon.configuration().icon().get() ? ICON_SIZE + GAP : 0;
    this.rectX = (COMPONENT_RENDERER.width(text) + iconWidth - 1) / 2;
    this.rectY = COMPONENT_RENDERER.height() / 2;

    if (!this.addon.configuration().background().get()) {
      return;
    }

    RECTANGLE_RENDER_CONTEXT.begin(stack);
    RECTANGLE_RENDER_CONTEXT.render(
        -this.rectX - padding,
        -this.rectY - padding,
        this.rectX + padding,
        this.rectY + padding - 1F,
        Colors.BACKGROUND_COLOR
    );
    RECTANGLE_RENDER_CONTEXT.uploadToBuffer(WaypointsRenderPrograms.BACKGROUND);
  }

  public void renderIcon(Stack stack) {
    if (!this.addon.configuration().icon().get()) {
      return;
    }

    Icon icon = this.meta().icon();
    icon.render(
        stack,
        -this.rectX,
        -this.rectY,
        ICON_SIZE,
        ICON_SIZE,
        false,
        -1
    );
  }

  public void renderText(Stack stack) {
    Component text = this.waypointObjectMeta().formatTitle();

    // render twice to fix that clouds are rendered in front of the text
    float iconWidth = this.addon.configuration().icon().get() ? ICON_SIZE + GAP : 0;
    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .pos(-this.rectX + iconWidth, -this.rectY)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .render(stack);
    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .discrete(true)
        .pos(-this.rectX + iconWidth, -this.rectY)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .render(stack);
  }
}