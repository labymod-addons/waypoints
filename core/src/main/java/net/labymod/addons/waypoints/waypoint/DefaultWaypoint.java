package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.WaypointsRenderPrograms;
import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gfx.pipeline.program.RenderPrograms;
import net.labymod.api.client.gfx.pipeline.texture.data.Sprite;
import net.labymod.api.client.gfx.shader.ShaderTextures;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.render.batch.RectangleRenderContext;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  public static final float ICON_WIDTH = 6.1F;
  public static final float ICON_HEIGHT = 11F;

  private static final float BACKGROUND_DEPTH = 0.01F;
  private static final float WAYPOINT_SCALE = 0.04F;

  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.references().rectangleRenderer();
  private static final ComponentRenderer COMPONENT_RENDERER = Laby.references()
      .componentRenderer();

  private final WaypointObjectMeta waypointObjectMeta;
  private final WaypointMeta meta;
  private final WaypointsAddon addon;
  private float marginBetweenTextAndIcon;
  private float iconWidth;

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

    stack.scale(WAYPOINT_SCALE * this.waypointObjectMeta.getScale());

    this.rotateHorizontally(cam, stack);
    this.rotateVertically(cam, stack);

    stack.push();
    stack.translate(0, 0, BACKGROUND_DEPTH);
    this.renderBackground(stack, 2F);
    stack.pop();

    this.renderIcon(stack);
    this.renderText(stack);

    stack.pop();
  }

  public void renderBackground(Stack stack, float padding) {
    Component text = this.waypointObjectMeta().formatTitle();

    this.marginBetweenTextAndIcon = this.iconWidth == 0F ? 0F : 2F;

    this.rectX =
        (COMPONENT_RENDERER.width(text) + this.iconWidth + this.marginBetweenTextAndIcon) / 2;
    this.rectY = COMPONENT_RENDERER.height() / 2;

    if (!this.addon.configuration().background().get()) {
      return;
    }

    RectangleRenderContext context = Laby.references().rectangleRenderContext();
    context.begin(stack)
        .render(
            this.rectX + padding,
            this.rectY + padding,
            -this.rectX - padding,
            -this.rectY - padding,
            Colors.BACKGROUND_COLOR
        ).uploadToBuffer(WaypointsRenderPrograms.BACKGROUND);
  }

  public void renderIcon(Stack stack) {
    if (!this.addon.configuration().icon().get()) {
      this.iconWidth = 0F;
      return;
    }

    this.iconWidth = ICON_WIDTH;

    Icon markerIcon = WaypointTextures.MARKER_ICON;
    Sprite sprite = markerIcon.sprite();

    ShaderTextures.setShaderTexture(0, ResourceLocation.create("labyswaypoints", "textures/marker.png"));
    ResourceRenderContext context = Laby.references().resourceRenderContext();

    int resWidth = markerIcon.getResolutionWidth();
    int resHeight = markerIcon.getResolutionHeight();

    float width = sprite.getWidth();
    width = width == 0.0f ? resWidth : width;

    float height = sprite.getHeight();
    height = height == 0.0f ? resHeight : height;

    context.begin(stack)
        .blit(
            -this.rectX,
            -this.rectY - 0.5F,
            this.iconWidth,
            ICON_HEIGHT,
            sprite.getX(), sprite.getY(), width, height,
            resWidth, resHeight,
            -1
        ).uploadToBuffer(WaypointsRenderPrograms.SEE_THROUGH_TEXTURED);
  }

  public void renderText(Stack stack) {
    Component text = this.waypointObjectMeta().formatTitle();

    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .discrete(true)
        .centered(true)
        .pos(this.iconWidth / 2 + this.marginBetweenTextAndIcon + 1F, -this.rectY)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .shouldBatch(false)
        .render(stack);
  }
}