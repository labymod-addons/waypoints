package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.client.gfx.color.GFXAlphaFunction;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import net.labymod.api.util.bounds.ModifyReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private static final float BACKGROUND_DEPTH = 0.01F;
  private static final float ICON_WIDTH = 6.1F;
  private static final float ICON_HEIGHT = 11F;
  private static final float WAYPOINT_SCALE = 0.04F;

  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.labyAPI().renderPipeline()
      .rectangleRenderer();
  private static final ComponentRenderer COMPONENT_RENDERER = Laby.labyAPI().renderPipeline()
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
  public void renderInWorld(
      @NotNull MinecraftCamera cam,
      @NotNull Stack stack,
      float x,
      float y,
      float z,
      float delta,
      boolean darker
  ) {
    if (this.waypointObjectMeta.isOutOfRange()) {
      return;
    }

    stack.push();

    GFXBridge gfx = Laby.gfx();

    stack.scale(WAYPOINT_SCALE * this.waypointObjectMeta.getScale());

    this.rotateHorizontally(cam, stack);
    this.rotateVertically(cam, stack);

    stack.push();
    stack.translate(0, 0, BACKGROUND_DEPTH);
    gfx.depthFunc(GFXAlphaFunction.NEVER);

    this.renderBackground(stack, 2F);

    gfx.depthFunc(GFXAlphaFunction.LEQUAL);
    stack.pop();

    this.renderIcon(stack);
    this.renderText(stack);

    stack.pop();
  }

  @Override
  public @Nullable Widget createWidget() {
    IconWidget widget = new IconWidget(WaypointTextures.MARKER_ICON);
    widget.bounds().setSize(
        ICON_WIDTH,
        ICON_HEIGHT,
        ModifyReason.of(Waypoint.class, "waypoint")
    );
    widget.color().set(this.meta.getColor().get());
    return widget;
  }

  public void renderBackground(Stack stack, float padding) {
    Component text = this.waypointObjectMeta().formatTitle();

    this.marginBetweenTextAndIcon = this.iconWidth == 0F ? 0F : 2F;

    this.rectX =
        (COMPONENT_RENDERER.width(text) + this.iconWidth + this.marginBetweenTextAndIcon) / 2;
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
      this.iconWidth = 0F;
      return;
    }

    this.iconWidth = ICON_WIDTH;

    WaypointTextures.MARKER_ICON.render(
        stack,
        -this.rectX,
        -this.rectY - 0.5F,
        this.iconWidth,
        ICON_HEIGHT,
        false,
        this.meta().getColor().get()
    );
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