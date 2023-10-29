package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import org.jetbrains.annotations.NotNull;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private final WaypointObjectMeta waypointObjectMeta;
  private final WaypointMeta meta;
  private final WaypointsAddon addon;

  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.labyAPI().renderPipeline()
      .rectangleRenderer();
  private static final ComponentRenderer COMPONENT_RENDERER = Laby.labyAPI().renderPipeline()
      .componentRenderer();

  private float marginBetweenTextAndIcon;
  private float icon_width;
  private float icon_height;

  private float rectX;
  private float rectY;

  public DefaultWaypoint(WaypointsAddon addon, WaypointMeta meta,
      WaypointObjectMeta waypointObjectMeta) {
    super(waypointObjectMeta.position());

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
  public void renderInWorld(
      @NotNull MinecraftCamera cam, @NotNull Stack stack, float x, float y,
      float z, float delta, boolean darker) {
    stack.push();

    GFXBridge gfx = Laby.gfx();
    gfx.storeBlaze3DStates();

    stack.scale(0.04F * this.waypointObjectMeta.getScale());

    this.rotateHorizontally(cam, stack);
    this.rotateVertically(cam, stack);

    gfx.depthMask(false);

    this.renderBackground(stack, 2F);
    this.renderIcon(stack);
    this.renderText(stack);

    gfx.restoreBlaze3DStates();

    stack.pop();
  }


  public void renderBackground(Stack stack, float padding) {
    Component text = this.waypointObjectMeta().formatTitle();

    this.marginBetweenTextAndIcon = this.icon_width == 0F ? 0F : 2F;

    this.rectX = (COMPONENT_RENDERER.width(text) + this.icon_width + this.marginBetweenTextAndIcon) / 2;
    this.rectY = COMPONENT_RENDERER.height() / 2;

    if (!addon.configuration().background().get()) {
      return;
    }

    RECTANGLE_RENDERER
        .pos(
            this.rectX + padding,
            this.rectY + padding,
            -this.rectX - padding,
            -this.rectY - padding
        )
        .color(Colors.BACKGROUND_COLOR)
        .render(stack);
  }

  public void renderIcon(Stack stack) {
    if (!addon.configuration().icon().get()) {
      this.icon_width = 0F;
      this.icon_height = 0F;
      return;
    }

    this.icon_width = 6.1F;
    this.icon_height = 11F;

    WaypointTextures.MARKER_ICON.render(
        stack,
        -this.rectX,
        -this.rectY - 0.5F,
        this.icon_width,
        this.icon_height,
        false,
        this.meta().color().get()
    );
  }

  public void renderText(Stack stack) {

    Component text = this.waypointObjectMeta().formatTitle();

    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .discrete(true)
        .centered(true)
        .pos(this.icon_width / 2 + this.marginBetweenTextAndIcon + 1F, -this.rectY)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .shouldBatch(false)
        .render(stack);
  }
}