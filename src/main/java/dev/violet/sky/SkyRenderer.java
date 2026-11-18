package dev.violet.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import dev.violet.SoberClient;
import dev.violet.modules.FpsBooster;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10799;
import net.minecraft.class_156;
import net.minecraft.class_1937;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class SkyRenderer {
   private static final float TIME_WRAP = 7200.0F;
   private static final int CUBE_VERTICES = 36;
   private static final Map<SkyType, RenderPipeline> PIPELINES = new EnumMap<>(SkyType.class);
   private static final Map<SkyType, Boolean> REPORTED_INVALID = new EnumMap<>(SkyType.class);
   @Nullable
   private static GpuBuffer cube;
   private static float time = 20.0F;
   private static long lastNanos = -1L;
   private static boolean drawnThisFrame;

   private SkyRenderer() {
   }

   private static RenderPipeline pipeline(SkyType type) {
      return PIPELINES.computeIfAbsent(
         type,
         t -> {
            Builder builder = RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
               .withLocation(class_2960.method_60655("violet", "pipeline/sky_" + t.id()))
               .withVertexShader(class_2960.method_60655("violet", "core/sky"))
               .withFragmentShader(class_2960.method_60655("violet", "core/sky_" + t.id()))
               .withDepthWrite(false)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .withVertexFormat(class_290.field_1592, class_5596.field_27379);
            if (t.texture() != null) {
               builder.withSampler("Sampler0");
            }

            return builder.build();
         }
      );
   }

   public static boolean isCustomSkySelected() {
      class_638 world = class_310.method_1551().field_1687;
      return SkySettings.sky.isCustom() && world != null && world.method_27983() == class_1937.field_25179;
   }

   public static boolean drewThisFrame() {
      return drawnThisFrame;
   }

   @Nullable
   public static float[] fogColor() {
      if (SkySettings.matchFog && isCustomSkySelected()) {
         float[] fog = SkySettings.sky.fogColor();
         if (fog == null) {
            return null;
         } else {
            float brightness = (float)SkySettings.brightness / 100.0F;
            return new float[]{Math.min(fog[0] * brightness, 1.0F), Math.min(fog[1] * brightness, 1.0F), Math.min(fog[2] * brightness, 1.0F)};
         }
      } else {
         return null;
      }
   }

   public static boolean renderSky() {
      drawnThisFrame = false;
      if (FpsBooster.lowEnd()) {
         return false;
      } else {
         advanceClock();
         SkyType type = SkySettings.sky;
         if (!type.isCustom()) {
            return false;
         } else {
            RenderPipeline pipeline = pipeline(type);
            if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
               if (REPORTED_INVALID.put(type, Boolean.TRUE) == null) {
                  SoberClient.LOG.error("Sky shader '{}' failed to compile, falling back to the vanilla sky", type.id());
               }

               return false;
            } else {
               int color = SkySettings.auroraColor;
               GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                  .method_71106(
                     RenderSystem.getModelViewMatrix(),
                     new Vector4f(time, (float)SkySettings.brightness / 100.0F, (float)SkySettings.auroraPalette, 0.0F),
                     new Vector3f((float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F),
                     new Matrix4f()
                  );
               class_276 target = class_310.method_1551().method_1522();
               GpuTextureView texture = null;
               if (type.texture() != null) {
                  try {
                     texture = class_310.method_1551().method_1531().method_4619(type.texture()).method_71659();
                  } catch (RuntimeException var10) {
                     if (REPORTED_INVALID.put(type, Boolean.TRUE) == null) {
                        SoberClient.LOG.error("Sky texture '{}' could not be loaded", type.texture(), var10);
                     }

                     return false;
                  }

                  if (texture == null) {
                     return false;
                  }
               }

               RenderPass pass = RenderSystem.getDevice()
                  .createCommandEncoder()
                  .createRenderPass(() -> "Violet sky", target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty());

               try {
                  pass.setPipeline(pipeline);
                  RenderSystem.bindDefaultUniforms(pass);
                  pass.setUniform("DynamicTransforms", transforms);
                  if (texture != null) {
                     pass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                  }

                  pass.setVertexBuffer(0, cube());
                  pass.draw(0, 36);
               } catch (Throwable var11) {
                  if (pass != null) {
                     try {
                        pass.close();
                     } catch (Throwable var9) {
                        var11.addSuppressed(var9);
                     }
                  }

                  throw var11;
               }

               if (pass != null) {
                  pass.close();
               }

               drawnThisFrame = true;
               return true;
            }
         }
      }
   }

   public static boolean renderPreview(SkyType type, GpuTextureView target, Matrix4f view, float seconds, float brightness, int palette, int color) {
      if (type != null && type.isCustom()) {
         RenderPipeline pipeline = pipeline(type);
         if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
            return false;
         } else {
            GpuTextureView texture = null;
            if (type.texture() != null) {
               texture = class_310.method_1551().method_1531().method_4619(type.texture()).method_71659();
               if (texture == null) {
                  return false;
               }
            }

            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
               .method_71106(
                  view,
                  new Vector4f(seconds, brightness, (float)palette, 0.0F),
                  new Vector3f((float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F),
                  new Matrix4f()
               );
            RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Violet sky preview", target, OptionalInt.of(-16777216));

            try {
               pass.setPipeline(pipeline);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", transforms);
               if (texture != null) {
                  pass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
               }

               pass.setVertexBuffer(0, cube());
               pass.draw(0, 36);
            } catch (Throwable var14) {
               if (pass != null) {
                  try {
                     pass.close();
                  } catch (Throwable var13) {
                     var14.addSuppressed(var13);
                  }
               }

               throw var14;
            }

            if (pass != null) {
               pass.close();
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static void advanceClock() {
      long now = class_156.method_648();
      if (lastNanos >= 0L) {
         float dt = Math.min((float)(now - lastNanos) / 1.0E9F, 0.25F);
         time = (time + dt * (float)SkySettings.speed / 100.0F) % 7200.0F;
      }

      lastNanos = now;
   }

   private static GpuBuffer cube() {
      if (cube == null || cube.isClosed()) {
         float s = 10.0F;
         float[][] corners = new float[][]{{-s, -s, -s}, {s, -s, -s}, {s, s, -s}, {-s, s, -s}, {-s, -s, s}, {s, -s, s}, {s, s, s}, {-s, s, s}};
         int[][] faces = new int[][]{{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
         class_9799 bytes = class_9799.method_72201(36 * class_290.field_1592.getVertexSize());

         try {
            class_287 builder = new class_287(bytes, class_5596.field_27379, class_290.field_1592);

            for (int[] face : faces) {
               for (int index : new int[]{face[0], face[1], face[2], face[0], face[2], face[3]}) {
                  float[] c = corners[index];
                  builder.method_22912(c[0], c[1], c[2]);
               }
            }

            class_9801 mesh = builder.method_60800();

            try {
               cube = RenderSystem.getDevice().createBuffer(() -> "Violet sky cube", 32, mesh.method_60818());
            } catch (Throwable var16) {
               if (mesh != null) {
                  try {
                     mesh.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (mesh != null) {
               mesh.close();
            }
         } catch (Throwable var17) {
            if (bytes != null) {
               try {
                  bytes.close();
               } catch (Throwable var14) {
                  var17.addSuppressed(var14);
               }
            }

            throw var17;
         }

         if (bytes != null) {
            bytes.close();
         }
      }

      return cube;
   }
}
