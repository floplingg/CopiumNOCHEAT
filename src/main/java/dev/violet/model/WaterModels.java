package dev.violet.model;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import dev.violet.SoberClient;
import java.util.function.Supplier;
import net.minecraft.class_10799;
import net.minecraft.class_11659;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_4587.class_4665;

public final class WaterModels {
   private static final class_2960 WHITE = class_2960.method_60655("violet", "textures/models/white.png");
   private static final RenderPipeline BODY = RenderPipeline.builder(new Snippet[]{class_10799.field_56854, class_10799.field_60126})
      .withLocation(class_2960.method_60655("violet", "pipeline/water_item"))
      .withVertexShader(class_2960.method_60655("violet", "core/water_item"))
      .withFragmentShader(class_2960.method_60655("violet", "core/water_item"))
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthWrite(true)
      .withCull(true)
      .build();
   private static final RenderPipeline RIM = RenderPipeline.builder(new Snippet[]{class_10799.field_56854, class_10799.field_60126})
      .withLocation(class_2960.method_60655("violet", "pipeline/water_item_rim"))
      .withVertexShader(class_2960.method_60655("violet", "core/water_item"))
      .withFragmentShader(class_2960.method_60655("violet", "core/water_item"))
      .withShaderDefine("RIM")
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthWrite(false)
      .withCull(true)
      .build();
   private static class_1921 body;
   private static class_1921 rim;
   private static volatile WaterMesh[][] meshes;
   private static boolean building;

   private WaterModels() {
   }

   public static WaterModels.Kind kind(String item) {
      if (item == null) {
         return null;
      } else if (item.equals("totem_of_undying")) {
         return WaterModels.Kind.TOTEM;
      } else if (item.equals("trident")) {
         return WaterModels.Kind.TRIDENT;
      } else if (item.endsWith("_pickaxe")) {
         return WaterModels.Kind.PICKAXE;
      } else if (item.endsWith("_sword")) {
         return WaterModels.Kind.SWORD;
      } else {
         return item.endsWith("_shovel") ? WaterModels.Kind.SHOVEL : null;
      }
   }

   private static void build() {
      if (!building) {
         building = true;
         Thread thread = new Thread(() -> {
            try {
               WaterModels.Kind[] kinds = WaterModels.Kind.values();
               WaterMesh[][] built = new WaterMesh[2][kinds.length];

               for (WaterModels.Kind kind : kinds) {
                  built[0][kind.ordinal()] = kind.body.get();
                  built[1][kind.ordinal()] = kind.glow.get();
               }

               meshes = built;
            } catch (Throwable var6) {
               SoberClient.LOG.warn("Could not build the water models", var6);
            }
         }, "Violet Water Models");
         thread.setDaemon(true);
         thread.setPriority(1);
         thread.start();
      }
   }

   public static WaterMesh mesh(WaterModels.Kind kind) {
      WaterMesh[][] all = meshes;
      if (all == null) {
         build();
         return null;
      } else {
         return all[0][kind.ordinal()];
      }
   }

   public static boolean ready() {
      if (meshes == null) {
         build();
      }

      return meshes != null;
   }

   public static void render(class_4587 matrices, class_11659 queue, WaterModels.Kind kind, int color, int light, boolean glow) {
      WaterMesh[][] all = meshes;
      if (all != null) {
         WaterMesh mesh = all[0][kind.ordinal()];
         WaterMesh shell = glow ? all[1][kind.ordinal()] : null;
         if (body == null) {
            body = class_1921.method_75940(
               "violet_water_item", class_12247.method_75927(BODY).method_75934("Sampler0", WHITE).method_75928().method_75937().method_75938()
            );
            rim = class_1921.method_75940(
               "violet_water_item_rim", class_12247.method_75927(RIM).method_75934("Sampler0", WHITE).method_75928().method_75937().method_75938()
            );
         }

         queue.method_73483(matrices, body, (entry, consumer) -> emit(entry, consumer, mesh, color, light, false));
         if (shell != null) {
            queue.method_73483(matrices, rim, (entry, consumer) -> emit(entry, consumer, shell, color, light, true));
         }
      }
   }

   private static void emit(class_4665 entry, class_4588 consumer, WaterMesh mesh, int color, int light, boolean inside) {
      float[] p = mesh.positions;
      float[] n = mesh.normals;
      int total = mesh.quads * 4;

      for (int k = 0; k < total; k++) {
         int v = inside ? k & -4 | 3 - (k & 3) : k;
         int i = v * 3;
         float nx = n[i];
         float ny = n[i + 1];
         float nz = n[i + 2];
         float x = p[i];
         float y = p[i + 1];
         consumer.method_56824(entry, x, y, p[i + 2])
            .method_39415(color)
            .method_22913(x, y)
            .method_22922(class_4608.field_21444)
            .method_60803(light)
            .method_60831(entry, nx, ny, nz);
      }
   }

   public static enum Kind {
      PICKAXE(WaterMesh::pickaxe, WaterMesh::pickaxeGlow),
      SWORD(WaterMesh::sword, WaterMesh::swordGlow),
      SHOVEL(WaterMesh::shovel, WaterMesh::shovelGlow),
      TRIDENT(WaterMesh::trident, WaterMesh::tridentGlow),
      TOTEM(WaterMesh::totem, WaterMesh::totemGlow);

      private final Supplier<WaterMesh> body;
      private final Supplier<WaterMesh> glow;

      private Kind(Supplier<WaterMesh> body, Supplier<WaterMesh> glow) {
         this.body = body;
         this.glow = glow;
      }
   }
}
