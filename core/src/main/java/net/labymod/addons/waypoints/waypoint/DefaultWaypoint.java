package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.Laby;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import org.jetbrains.annotations.NotNull;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private final WaypointMeta meta;

  public DefaultWaypoint(WaypointMeta meta) {
    super(Waypoints.getWaypointObjects().get(meta).getLocation());

    this.meta = meta;
  }

  @Override
  public WaypointMeta meta() {
    return this.meta;
  }

  @Override
  public void renderInWorld(
      @NotNull MinecraftCamera cam, @NotNull Stack stack, float x, float y,
      float z, float delta, boolean darker) {
    stack.push();

    this.rotateHorizontally(cam, stack);
    this.rotateVertically(cam, stack);

    stack.scale(0.03F);

    WaypointObjectMeta waypointObjectMeta = Waypoints.getWaypointObjects().get(meta);
    RenderPipeline render = Laby.labyAPI().renderPipeline();

    render.componentRenderer().builder()
        .text(waypointObjectMeta.formatTitle(meta.getTitle()))
        .pos(0, -12F)
        .scale(waypointObjectMeta.getScale())
        .centered(true)
        .shadow(false)
        .render(stack);

    WaypointTextures.MARKER_ICON.render(
        stack,
        -6F,
        14F,
        13.5F * waypointObjectMeta.getMarkerScale(),
        24F * waypointObjectMeta.getMarkerScale(),
        false,
        this.color().get()
    );

    stack.pop();
  }
}
