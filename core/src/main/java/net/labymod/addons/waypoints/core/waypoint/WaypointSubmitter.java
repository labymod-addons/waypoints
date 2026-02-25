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
import net.labymod.addons.waypoints.core.WaypointsRenderPrograms;
import net.labymod.addons.waypoints.utils.Colors;
import net.labymod.addons.waypoints.waypoint.WaypointMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta;
import net.labymod.addons.waypoints.waypoint.WaypointObjectMeta.Type;
import net.labymod.api.Laby;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gfx.pipeline.renderer.text.FontFlags;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.font.ComponentRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.render.state.world.CameraSnapshot;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.world.object.submit.WorldObjectSubmitter;
import net.labymod.api.laby3d.pipeline.material.LevelMaterial;
import net.labymod.api.laby3d.render.queue.SubmissionCollector;
import net.labymod.api.laby3d.render.queue.submissions.IconSubmission.DisplayMode;
import org.jetbrains.annotations.NotNull;

public class WaypointSubmitter extends WorldObjectSubmitter<DefaultWaypoint, WaypointSnapshot> {

  private static final float BACKGROUND_DEPTH = 0.01F;
  private static final float WAYPOINT_SCALE = 0.04F;
  private static final float BEACON_BEAM_SIZE = 0.3F;
  private static final float BEACON_BEAM_START_Y = -1024.0F;
  private static final float BEACON_BEAM_END_Y = 1024.0F * 2.0F;
  private static final float BEACON_BEAM_SPRITE_U_MIN = 0.0F;
  private static final float BEACON_BEAM_SPRITE_U_MAX = 1.0F;
  private static final float BEACON_BEAM_SPRITE_V_MIN = 0.0F;
  private static final float BEACON_BEAM_SPRITE_V_MAX = 256.0F * BEACON_BEAM_END_Y * 5.0F / 256.0F;

  private static final float ICON_SIZE = 8;
  private static final float GAP = 3;

  private static final ResourceLocation BEACON_BEAM_TEXTURE =
      Waypoints.ofPath("textures/beacon_beam.png");

  private static final ComponentRenderer COMPONENT_RENDERER =
      Laby.references().componentRenderer();

  @Override
  public @NotNull WaypointSnapshot createSnapshot(
      @NotNull DefaultWaypoint object,
      double x, double y, double z,
      int lightCoords,
      @NotNull CameraSnapshot camera
  ) {
    WaypointObjectMeta objectMeta = object.waypointObjectMeta();
    WaypointMeta meta = object.meta();
    var config = object.addon().configuration();

    boolean beaconBeam = config.beaconBeam().get();
    Type type = beaconBeam ? Type.WITHOUT_COLOR : Type.WITH_COLOR;
    boolean showIcon = config.icon().get();

    Component formattedTitle = objectMeta.formatTitle(type);
    float iconWidth = showIcon ? ICON_SIZE + GAP : 0;

    boolean visible = config.enabled().get() && !objectMeta.isOutOfRange();

    return new WaypointSnapshot(
        x, y, z, lightCoords,
        objectMeta.getScale(),
        meta.color().get(),
        meta.iconColor(),
        meta.icon(),
        formattedTitle,
        COMPONENT_RENDERER.width(formattedTitle) + iconWidth - 1,
        COMPONENT_RENDERER.height(),
        camera.getYaw(),
        camera.getPitch(),
        beaconBeam,
        config.background().get(),
        showIcon,
        visible
    );
  }

  @Override
  public void submit(
      @NotNull Stack stack,
      @NotNull SubmissionCollector collector,
      @NotNull WaypointSnapshot snapshot
  ) {
    if (!snapshot.visible()) {
      return;
    }

    stack.push();
    stack.translate(snapshot.x(), snapshot.y(), snapshot.z());

    this.submitBeaconBeam(stack, collector, snapshot);
    this.submitLabel(stack, collector, snapshot);

    stack.pop();
  }

  private void submitBeaconBeam(
      Stack stack,
      SubmissionCollector collector,
      WaypointSnapshot snapshot
  ) {
    if (!snapshot.beaconBeam()) {
      return;
    }

    float dynamicSize = (float) (BEACON_BEAM_SIZE
        * (1 + (snapshot.x() + snapshot.z()) / 180));

    float rotation = System.currentTimeMillis() % 3600 / 20F;
    float upwards = System.currentTimeMillis() % 2000 / 1000F * dynamicSize;
    int color = snapshot.color();
    int light = snapshot.lightCoords();

    stack.push();
    stack.rotate(rotation, 0, 1, 0);
    stack.translate(-dynamicSize / 2, -upwards, -dynamicSize / 2);

    for (int i = 0; i < 4; i++) {
      stack.rotate(90, 0, 1, 0);
      stack.translate(-dynamicSize, 0, 0);

      collector.submitCustomGeometry(
          stack,
          LevelMaterial
              .builder(WaypointsRenderPrograms.BEACON_BEAM)
              .setTexture(0, BEACON_BEAM_TEXTURE)
              .build(),
          (pose, consumer) -> {
            consumer.addVertex(pose, 0, BEACON_BEAM_START_Y, 0)
                .setColor(color)
                .setUv(BEACON_BEAM_SPRITE_U_MIN, BEACON_BEAM_SPRITE_V_MIN)
                .setPackedLight(light);
            consumer.addVertex(pose, dynamicSize, BEACON_BEAM_START_Y, 0)
                .setColor(color)
                .setUv(BEACON_BEAM_SPRITE_U_MAX, BEACON_BEAM_SPRITE_V_MIN)
                .setPackedLight(light);
            consumer.addVertex(pose, dynamicSize, BEACON_BEAM_END_Y, 0)
                .setColor(color)
                .setUv(BEACON_BEAM_SPRITE_U_MAX, BEACON_BEAM_SPRITE_V_MAX)
                .setPackedLight(light);
            consumer.addVertex(pose, 0, BEACON_BEAM_END_Y, 0)
                .setColor(color)
                .setUv(BEACON_BEAM_SPRITE_U_MIN, BEACON_BEAM_SPRITE_V_MAX)
                .setPackedLight(light);
          }
      );
    }

    stack.pop();
  }

  private void submitLabel(
      Stack stack,
      SubmissionCollector collector,
      WaypointSnapshot snapshot
  ) {
    stack.push();
    stack.scale(WAYPOINT_SCALE * snapshot.scale());

    stack.rotate(-snapshot.cameraYaw(), 0, 1, 0);
    stack.rotate(snapshot.cameraPitch(), 1, 0, 0);
    stack.scale(-1, -1, 1);

    float rectX = snapshot.textWidth() / 2;
    float rectY = snapshot.textHeight() / 2;

    this.submitBackground(stack, collector, snapshot, rectX, rectY);
    this.submitIcon(stack, collector, snapshot, rectX, rectY);
    this.submitText(stack, collector, snapshot, rectX, rectY);

    stack.pop();
  }

  private void submitBackground(
      Stack stack,
      SubmissionCollector collector,
      WaypointSnapshot snapshot,
      float rectX,
      float rectY
  ) {
    if (!snapshot.background()) {
      return;
    }

    float padding = 1F;

    stack.push();
    stack.translate(0, 0, BACKGROUND_DEPTH * 16);

    collector.submitRectangle(
        LevelMaterial.builder(WaypointsRenderPrograms.BACKGROUND)
            .setTexture(0, Textures.WHITE)
            .build(),
        stack,
        -rectX - padding, -rectY - padding,
        rectX * 2.0F + padding, snapshot.textHeight() + padding - 1.0F,
        Colors.BACKGROUND_COLOR,
        snapshot.lightCoords()
    );

    stack.pop();
  }

  private void submitIcon(
      Stack stack,
      SubmissionCollector collector,
      WaypointSnapshot snapshot,
      float rectX,
      float rectY
  ) {
    Icon icon = snapshot.icon();
    if (!snapshot.showIcon() || icon.getResourceLocation() == null) {
      return;
    }

    collector.submitIcon(
        WaypointsRenderPrograms.ICON,
        stack,
        icon,
        DisplayMode.NORMAL,
        -rectX, -rectY,
        ICON_SIZE, ICON_SIZE,
        snapshot.iconColor()
    );
  }

  private void submitText(
      Stack stack,
      SubmissionCollector collector,
      WaypointSnapshot snapshot,
      float rectX,
      float rectY
  ) {
    float iconWidth = snapshot.showIcon() ? ICON_SIZE + GAP : 0;

    collector.submitComponent(
        stack,
        snapshot.formattedTitle(),
        -rectX + iconWidth, -rectY,
        -1,
        snapshot.lightCoords(),
        0,
        FontFlags.DISPLAY_MODE_NORMAL
    );
  }
}
