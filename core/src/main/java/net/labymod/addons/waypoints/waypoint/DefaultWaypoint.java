package net.labymod.addons.waypoints.waypoint;

import net.labymod.addons.waypoints.WaypointsAddon;
import net.labymod.addons.waypoints.utils.RenderUtils;
import net.labymod.api.Laby;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.MinecraftCamera;
import net.labymod.api.client.world.object.AbstractWorldObject;
import org.jetbrains.annotations.NotNull;

public class DefaultWaypoint extends AbstractWorldObject implements Waypoint {

  private final WaypointObjectMeta waypointObjectMeta;
  private final WaypointMeta meta;
  private final WaypointsAddon addon;


  public DefaultWaypoint(WaypointsAddon addon, WaypointMeta meta,
      WaypointObjectMeta waypointObjectMeta) {
    super(waypointObjectMeta.getLocation());

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

    RenderUtils.renderBackground(this.addon, this, stack);
    RenderUtils.renderIcon(this.addon, this.color().get(), stack);
    RenderUtils.renderText(this, stack);

    gfx.restoreBlaze3DStates();

    stack.pop();
  }
}