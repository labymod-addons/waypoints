package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointTextures;
import net.labymod.api.Laby;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import org.jetbrains.annotations.NotNull;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private final WaypointMeta meta;

  public DefaultWaypoint(WaypointMeta meta) {
    super(meta.getLocation());

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

    stack.scale(0.03F);

    RenderPipeline render = Laby.labyAPI().renderPipeline();

    render.componentRenderer().builder()
        .text(this.meta.getTitle())
        .pos(0, -12F)
        .centered(true)
        .shadow(false)
        .render(stack);

    WaypointTextures.MARKER_ICON.render(
        stack,
        -6.25F,
        0,
        13.5F,
        24F,
        false,
        this.color().get()
    );

    stack.pop();
  }
}
