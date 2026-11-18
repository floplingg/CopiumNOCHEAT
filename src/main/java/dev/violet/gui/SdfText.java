package dev.violet.gui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class SdfText {
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60655("violet", "sdf_text"))
      .withVertexShader(class_2960.method_60656("core/position_tex_color"))
      .withFragmentShader(class_2960.method_60655("violet", "sdf_text"))
      .withUniform("DynamicTransforms", class_10789.field_60031)
      .withUniform("Projection", class_10789.field_60031)
      .withSampler("Sampler0")
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withVertexFormat(class_290.field_1575, class_5596.field_27382)
      .build();
   private static final class_2960 ATLAS = class_2960.method_60655("violet", "textures/font/xuong_regular_sdf.png");
   private static final GuiBatch BATCH = new GuiBatch();
   private static final int[] ADVANCES = new int[]{
      2,
      2,
      3,
      7,
      6,
      6,
      7,
      2,
      2,
      2,
      4,
      4,
      2,
      4,
      2,
      3,
      6,
      4,
      6,
      5,
      6,
      5,
      6,
      5,
      6,
      6,
      2,
      2,
      4,
      4,
      4,
      5,
      8,
      6,
      6,
      6,
      6,
      6,
      5,
      6,
      6,
      2,
      5,
      6,
      4,
      7,
      6,
      6,
      6,
      6,
      5,
      6,
      5,
      6,
      6,
      9,
      6,
      5,
      6,
      2,
      3,
      2,
      6,
      4,
      4,
      6,
      6,
      6,
      6,
      6,
      5,
      6,
      6,
      2,
      5,
      6,
      4,
      7,
      6,
      6,
      6,
      6,
      5,
      6,
      5,
      6,
      6,
      9,
      6,
      5,
      6,
      3,
      2,
      3,
      5
   };

   private SdfText() {
   }

   public static boolean supports(String value) {
      return value.codePoints().allMatch(c -> c >= 32 && c < 127);
   }

   public static int width(String value) {
      int width = 0;

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         width += c >= ' ' && c < 127 ? ADVANCES[c - ' '] : 6;
      }

      return width;
   }

   public static void draw(class_332 context, String value, int x, int y, int color) {
      if (color >>> 24 != 0) {
         GuiBatch batch = BATCH.reset();
         int cursor = x;

         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= ' ' && c < 127) {
               int index = c - ' ';
               if (c != ' ') {
                  float u = (float)(index % 16) * 96.0F;
                  float v = (float)(index / 16) * 96.0F;
                  batch.quad(cursor - 1, y - 5, cursor + 15, y + 11, (u + 0.0F) / 1536.0F, (v + 0.0F) / 576.0F, (u + 96.0F) / 1536.0F, (v + 96.0F) / 576.0F);
               }

               cursor += ADVANCES[index];
            } else {
               cursor += 6;
            }
         }

         batch.submitTextured(context, PIPELINE, ATLAS, color);
      }
   }
}
