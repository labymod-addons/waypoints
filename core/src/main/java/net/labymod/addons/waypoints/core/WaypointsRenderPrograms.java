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

package net.labymod.addons.waypoints.core;

import static net.labymod.api.laby3d.pipeline.RenderStates.DEFAULT_SHADER_SNIPPET;
import static net.labymod.api.laby3d.pipeline.RenderStates.SHADER_RESOLVER;

import net.labymod.addons.waypoints.Waypoints;
import net.labymod.api.laby3d.vertex.VertexDescriptions;
import net.labymod.laby3d.api.pipeline.ComparisonStrategy;
import net.labymod.laby3d.api.pipeline.DrawingMode;
import net.labymod.laby3d.api.pipeline.RenderState;
import net.labymod.laby3d.api.pipeline.blend.DefaultBlendFunctions;
import net.labymod.laby3d.api.pipeline.shader.ShaderProgramDescription;
import net.labymod.laby3d.api.pipeline.shader.UniformSamplerDescription;
import net.labymod.laby3d.api.resource.AssetId;

public final class WaypointsRenderPrograms {

  public static final ShaderProgramDescription SHADER = ShaderProgramDescription.builder(
          DEFAULT_SHADER_SNIPPET
      )
      .setId(buildProgramId("background"))
      .setVertexShader(SHADER_RESOLVER.apply("core/simple_level_geometry.vsh"))
      .setFragmentShader(SHADER_RESOLVER.apply("core/simple_level_geometry.fsh"))
      .addSampler(new UniformSamplerDescription("DiffuseSampler", 0))
      .build();

  public static final RenderState BACKGROUND = RenderState.builder()
      .setId(buildStateId("background"))
      .setVertexDescription(VertexDescriptions.POSITION_UV_COLOR)
      .setBlendFunction(DefaultBlendFunctions.TRANSLUCENT)
      .setDepthTestStrategy(ComparisonStrategy.NEVER)
      .setShaderProgramDescription(SHADER)
      .setDrawingMode(DrawingMode.QUADS)
      .build();

  public static final RenderState BEACON_BEAM = RenderState.builder()
      .setId(buildStateId("beacon_beam"))
      .setVertexDescription(VertexDescriptions.POSITION_UV_COLOR)
      .setBlendFunction(DefaultBlendFunctions.TRANSLUCENT)
      .setCull(false)
      .setShaderProgramDescription(SHADER)
      .setDrawingMode(DrawingMode.QUADS)
      .build();

  private static AssetId buildStateId(String name) {
    return AssetId.of(Waypoints.NAMESPACE, "renderstate/" + name);
  }

  private static AssetId buildProgramId(String name) {
    return AssetId.of(Waypoints.NAMESPACE, "shaderprogram/" + name);
  }

}
