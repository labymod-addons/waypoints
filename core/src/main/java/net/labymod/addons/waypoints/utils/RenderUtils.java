package net.labymod.addons.waypoints.utils;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;

public class RenderUtils {

  private final static  RectangleRenderer RECTANGLE_RENDERER = Laby.labyAPI().renderPipeline().rectangleRenderer();
  private final static  ComponentRenderer COMPONENT_RENDERER = Laby.labyAPI().renderPipeline().componentRenderer();

  private final static float PADDING = 2F;
  private static float marginBetweenTextAndIcon;
  private static float icon_width;
  private static float icon_height;

  private static float rect_x;
  private static float rect_y;

  public static void renderBackground(WaypointsAddon addon, WaypointMeta meta, Stack stack) {
    Component text = Waypoints.getWaypointObjects().get(meta).formatTitle();

    marginBetweenTextAndIcon = icon_width == 0F ? 0F : 2F;

    rect_x = (COMPONENT_RENDERER.width(text) + icon_width + marginBetweenTextAndIcon) / 2;
    rect_y = COMPONENT_RENDERER.height() / 2;

    if(!addon.configuration().background().get()) return;

    RECTANGLE_RENDERER
        .pos(
            rect_x + PADDING,
            rect_y + PADDING,
            -rect_x - PADDING,
            -rect_y - PADDING
        )
        .color(1275068416)
        .render(stack);
  }

  public static void renderIcon(WaypointsAddon addon, int color,Stack stack) {
    if(!addon.configuration().icon().get()) {
      icon_width = 0F;
      icon_height = 0F;
      return;
    }

    icon_width = 6.1F;
    icon_height = 11F;

    WaypointTextures.MARKER_ICON.render(
        stack,
        -rect_x,
        -rect_y - 0.5F,
        icon_width,
        icon_height,
        false,
        color
    );
  }

  public static void renderText(WaypointMeta meta, Stack stack) {

    Component text = Waypoints.getWaypointObjects().get(meta).formatTitle();

    COMPONENT_RENDERER.builder()
        .text(text)
        .shadow(false)
        .discrete(true)
        .centered(true)
        .pos(icon_width/2 + marginBetweenTextAndIcon + 1F, -rect_y)
        .useFloatingPointPosition(true)
        .allowColors(true)
        .shouldBatch(false)
        .render(stack);
  }
}