package net.labymod.addons.waypoints;

import net.labymod.api.client.gfx.pipeline.program.DefaultBlendFunctions;
import net.labymod.api.client.gfx.pipeline.program.DepthTestFunction;
import net.labymod.api.client.gfx.pipeline.program.RenderProgram;
import net.labymod.api.client.gfx.pipeline.program.RenderPrograms;
import net.labymod.api.client.gfx.pipeline.program.ShaderConfig.UniformType;
import net.labymod.api.client.gfx.vertex.VertexFormats;
import net.labymod.api.client.resources.ResourceLocation;

public class WaypointsRenderPrograms {

  public static final RenderProgram BACKGROUND = RenderProgram.builder()
      .withId(ResourceLocation.create("waypoints", "pipeline/background"))
      .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR)
      .withDepthTest(DepthTestFunction.NEVER)
      .withBlendFunction(DefaultBlendFunctions.TRANSLUCENT)
      .withShader(
          shader -> shader.withVertexShader(
                  RenderPrograms.SHADER_RESOURCE_PATH.apply("vertex/position_texture_color.vsh"))
              .withFragmentShader(
                  RenderPrograms.SHADER_RESOURCE_PATH.apply("vertex/position_texture_color.fsh"))
              .withUniform("ColorModulator", UniformType.VEC4),
          RenderPrograms.MATRICES_SNIPPET
      ).build();

  public static final RenderProgram SEE_THROUGH_TEXTURED = RenderProgram.builder()
      .withId(ResourceLocation.create("waypoints", "pipeline/see_through_textured"))
      .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR)
      .withDepthTest(DepthTestFunction.NEVER)
      .withBlendFunction(DefaultBlendFunctions.TRANSLUCENT)
      .withShader(
          builder -> builder
              .withVertexShader(RenderPrograms.SHADER_RESOURCE_PATH.apply("vertex/position_texture_color.vsh"))
              .withFragmentShader(RenderPrograms.SHADER_RESOURCE_PATH.apply("vertex/position_texture_color.fsh"))
              .withSampler("DiffuseSampler", 0)
              .withUniform("ColorModulator", UniformType.VEC4),
          RenderPrograms.MATRICES_SNIPPET
      ).build();


}
