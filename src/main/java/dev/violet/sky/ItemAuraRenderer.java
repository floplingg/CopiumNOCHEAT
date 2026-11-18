package dev.violet.sky;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import dev.violet.SoberClient;
import dev.violet.modules.CustomGlint;
import dev.violet.modules.FpsBooster;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_156;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class ItemAuraRenderer {
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60655("violet", "pipeline/item_aura"))
      .withVertexShader("core/screenquad")
      .withFragmentShader(class_2960.method_60655("violet", "core/item_aura"))
      .withSampler("InSampler")
      .withUniform("DynamicTransforms", class_10789.field_60031)
      .withUniform("Globals", class_10789.field_60031)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withColorWrite(true, false)
      .withVertexFormat(class_290.field_60033, class_5596.field_27379)
      .build();
   private static boolean handSubmitted;
   private static boolean reportedInvalid;

   private ItemAuraRenderer() {
   }

   private static boolean shouldRender(@Nullable class_746 player) {
      return SoberClient.MODULES.get(CustomGlint.class).itemAura.get()
         && player != null
         && (!player.method_6047().method_7960() || !player.method_6079().method_7960());
   }

   public static void onHandSubmitted(@Nullable class_746 player) {
      handSubmitted = !FpsBooster.lowEnd() && shouldRender(player);
   }

   public static void afterFeatures() {
      if (handSubmitted) {
         handSubmitted = false;
         class_310 mc = class_310.method_1551();
         mc.method_22940().method_23000().method_22993();
         if (!RenderSystem.getDevice().precompilePipeline(PIPELINE).isValid()) {
            if (!reportedInvalid) {
               reportedInvalid = true;
               SoberClient.LOG.error("Item aura shader failed to compile, item aura disabled");
            }
         } else {
            class_276 main = mc.method_1522();
            if (main.method_71640() != null) {
               float seconds = (float)(class_156.method_658() % 3600000L) / 1000.0F;
               CustomGlint glint = SoberClient.MODULES.get(CustomGlint.class);
               int rgb = glint.itemAuraColor(seconds);
               float scale = Math.max(1.0F, (float)main.field_1481 / 540.0F);
               GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                  .method_71106(
                     new Matrix4f(),
                     new Vector4f((float)(rgb >> 16 & 0xFF) / 255.0F, (float)(rgb >> 8 & 0xFF) / 255.0F, (float)(rgb & 0xFF) / 255.0F, 1.0F),
                     new Vector3f((float)glint.itemWidth.get().intValue() * scale, (float)glint.itemGlow.get().intValue() * 2.0F * scale, seconds),
                     new Matrix4f()
                  );
               RenderPass pass = RenderSystem.getDevice()
                  .createCommandEncoder()
                  .createRenderPass(() -> "Violet item aura", main.method_71639(), OptionalInt.empty());

               try {
                  pass.setPipeline(PIPELINE);
                  RenderSystem.bindDefaultUniforms(pass);
                  pass.setUniform("DynamicTransforms", transforms);
                  pass.bindTexture("InSampler", main.method_71640(), RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                  pass.draw(0, 3);
               } catch (Throwable var11) {
                  if (pass != null) {
                     try {
                        pass.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (pass != null) {
                  pass.close();
               }
            }
         }
      }
   }
}
