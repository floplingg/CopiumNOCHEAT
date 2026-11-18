package dev.violet.sky;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import dev.violet.SoberClient;
import dev.violet.modules.CustomGlint;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_12245;
import net.minecraft.class_12246;
import net.minecraft.class_12247;
import net.minecraft.class_12250;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_918;

public final class GlintCover {
   private static final int ARMOR = 0;
   private static final int GLINT = 1;
   private static final int TRANSLUCENT = 2;
   private static final int ENTITY = 3;
   private static class_1921[] cover;
   private static final Map<GlintType, class_1921[]> SHADERS = new EnumMap<>(GlintType.class);
   private static final Map<GlintType, RenderPipeline> PIPELINES = new EnumMap<>(GlintType.class);
   private static final Map<GlintType, Boolean> VALID = new EnumMap<>(GlintType.class);

   private GlintCover() {
   }

   private static boolean active() {
      CustomGlint module = SoberClient.MODULES == null ? null : SoberClient.MODULES.get(CustomGlint.class);
      return module != null && module.isActive() && GlintManager.current() != GlintType.VANILLA;
   }

   private static class_1921[] layers(RenderPipeline pipeline, String name) {
      class_1921[] layers = new class_1921[]{
         null,
         null,
         class_1921.method_75940(
            name + "_translucent",
            class_12247.method_75927(pipeline)
               .method_75934("Sampler0", class_918.field_43087)
               .method_75933(class_12250.field_64069)
               .method_75931(class_12246.field_63983)
               .method_75938()
         ),
         class_1921.method_75940(
            name + "_entity",
            class_12247.method_75927(pipeline).method_75934("Sampler0", class_918.field_43087).method_75933(class_12250.field_64070).method_75938()
         )
      };
      layers[0] = class_1921.method_75940(
         name + "_armor_entity",
         class_12247.method_75927(pipeline)
            .method_75934("Sampler0", class_918.field_43086)
            .method_75933(class_12250.field_64071)
            .method_75930(class_12245.field_63976)
            .method_75938()
      );
      layers[1] = class_1921.method_75940(
         name, class_12247.method_75927(pipeline).method_75934("Sampler0", class_918.field_43087).method_75933(class_12250.field_64069).method_75938()
      );
      return layers;
   }

   private static void create() {
      if (cover == null) {
         RenderPipeline pipeline = RenderPipeline.builder(new Snippet[]{class_10799.field_60125, class_10799.field_56848, class_10799.field_60126})
            .withLocation(class_2960.method_60655("violet", "pipeline/glint_cover"))
            .withVertexShader("core/glint")
            .withFragmentShader(class_2960.method_60655("violet", "core/glint_cover"))
            .withSampler("Sampler0")
            .withDepthWrite(false)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(class_290.field_1585, class_5596.field_27382)
            .build();
         cover = layers(pipeline, "violet_glint");

         for (GlintType type : GlintType.values()) {
            String shader = type.shader();
            if (shader != null) {
               RenderPipeline fx = RenderPipeline.builder(new Snippet[]{class_10799.field_60125, class_10799.field_56848, class_10799.field_60126})
                  .withLocation(class_2960.method_60655("violet", "pipeline/glintfx_" + shader))
                  .withVertexShader(class_2960.method_60655("violet", "core/glintfx"))
                  .withFragmentShader(class_2960.method_60655("violet", "core/glintfx_" + shader))
                  .withSampler("Sampler0")
                  .withDepthWrite(false)
                  .withCull(false)
                  .withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
                  .withBlend(BlendFunction.GLINT)
                  .withVertexFormat(class_290.field_1585, class_5596.field_27382)
                  .build();
               PIPELINES.put(type, fx);
               SHADERS.put(type, layers(fx, "violet_glintfx_" + shader));
            }
         }
      }
   }

   public static List<class_1921> layers() {
      create();
      List<class_1921> all = new ArrayList<>(List.of(cover));

      for (class_1921[] set : SHADERS.values()) {
         all.addAll(List.of(set));
      }

      return all;
   }

   private static class_1921 pick(int index) {
      if (!active()) {
         return null;
      } else {
         create();
         GlintType type = GlintManager.current();
         if (type.shader() == null) {
            return cover[index];
         } else {
            class_1921[] set = SHADERS.get(type);
            return set != null && valid(type) ? set[index] : null;
         }
      }
   }

   private static boolean valid(GlintType type) {
      Boolean known = VALID.get(type);
      if (known != null) {
         return known;
      } else {
         boolean ok;
         try {
            ok = RenderSystem.getDevice().precompilePipeline(PIPELINES.get(type)).isValid();
         } catch (RuntimeException var4) {
            ok = false;
         }

         if (!ok) {
            SoberClient.LOG.error("Glint shader '{}' failed to compile, falling back to the vanilla glint", type.shader());
         }

         VALID.put(type, ok);
         return ok;
      }
   }

   public static class_1921 glint() {
      return pick(1);
   }

   public static class_1921 translucent() {
      return pick(2);
   }

   public static class_1921 entity() {
      return pick(3);
   }

   public static class_1921 armor() {
      return pick(0);
   }
}
