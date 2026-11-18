package dev.violet.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import dev.violet.SoberClient;
import dev.violet.module.Module;
import dev.violet.modules.BlockOverlay;
import dev.violet.modules.CustomAccessories;
import dev.violet.modules.CustomGlint;
import dev.violet.modules.Sky;
import dev.violet.sky.AuraRenderer;
import dev.violet.sky.SkyRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10366;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11286;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_490;
import net.minecraft.class_6367;
import net.minecraft.class_9334;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class ModulePreview {
   private static final RenderPipeline ROUNDED = RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
      .withLocation(class_2960.method_60655("violet", "pipeline/rounded_texture"))
      .withFragmentShader(class_2960.method_60655("violet", "core/rounded_texture"))
      .build();
   private static final RenderPipeline BLOCK = RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
      .withLocation(class_2960.method_60655("violet", "pipeline/preview_block"))
      .withVertexShader(class_2960.method_60656("core/position_tex_color"))
      .withFragmentShader(class_2960.method_60656("core/position_tex_color"))
      .withSampler("Sampler0")
      .withDepthWrite(true)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withCull(false)
      .withVertexFormat(class_290.field_1575, class_5596.field_27379)
      .build();
   private static final class_2960 STONE = class_2960.method_60656("textures/block/stone.png");
   private static final class_11286 SKY_PROJECTION = new class_11286("Violet sky preview");
   private static final class_11286 BLOCK_PROJECTION = new class_11286("Violet overlay preview");
   private static final Map<String, ModulePreview.Target> TARGETS = new HashMap<>();
   private static final GuiBatch BATCH = new GuiBatch();
   private static final float BLOCK_FOV = (float)Math.toRadians(38.0);
   private static final Vector3f EYE = new Vector3f(2.15F, 1.55F, 2.45F);
   private static class_1799 sword;
   private static boolean broken;

   private ModulePreview() {
   }

   public static boolean supports(Module module) {
      return !(module instanceof Sky) && !(module instanceof BlockOverlay) && !(module instanceof CustomGlint)
         ? module instanceof CustomAccessories && class_310.method_1551().field_1724 != null
         : true;
   }

   public static String subtitle(Module module) {
      if (module instanceof Sky sky) {
         return sky.previewName();
      } else if (module instanceof BlockOverlay overlay) {
         return overlay.style.get().title;
      } else if (module instanceof CustomGlint glint) {
         return String.valueOf(glint.glint.get());
      } else {
         return module instanceof CustomAccessories ? "Wings and crown" : "";
      }
   }

   private static int alpha(int color, float alpha) {
      return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | color & 16777215;
   }

   private static float seconds() {
      return (float)(System.nanoTime() / 1000000L % 3600000L) / 1000.0F;
   }

   public static void draw(class_332 context, Module module, float x, float y, float w, float h, float opacity) {
      if (!(opacity <= 0.01F) && !(w < 8.0F) && !(h < 8.0F)) {
         int radius = Math.max(4, Math.round(Math.min(w, h) * 0.08F));
         int accent = 0xFF000000 | Theme.accent();
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(x, y);
         int iw = Math.round(w);
         int ih = Math.round(h);
         Smooth.rect(context, 0, 0, iw, ih, radius, alpha(920086, 0.92F * opacity));

         try {
            if (module instanceof Sky sky) {
               sky(context, sky, iw, ih, radius, opacity);
            } else if (module instanceof BlockOverlay overlay) {
               overlay(context, overlay, iw, ih, radius, opacity, accent);
            } else if (module instanceof CustomGlint) {
               glint(context, iw, ih, opacity, accent);
            } else if (module instanceof CustomAccessories) {
               accessories(context, x, y, iw, ih, opacity, accent);
            }
         } catch (RuntimeException var14) {
            if (!broken) {
               SoberClient.LOG.warn("Module preview failed", var14);
            }

            broken = true;
         }

         Smooth.outline(context, 0, 0, iw, ih, radius, alpha(16777215, 0.14F * opacity));
         matrices.popMatrix();
      }
   }

   private static ModulePreview.Target target(String name) {
      return TARGETS.computeIfAbsent(name, ModulePreview.Target::new);
   }

   private static int[] pixels(class_332 context, int w, int h) {
      float ppu = Math.max(1.0F, Smooth.pixelsPerUnit(context));
      return new int[]{Math.max(8, Math.min(1024, Math.round((float)w * ppu))), Math.max(8, Math.min(1024, Math.round((float)h * ppu))), Math.round(ppu)};
   }

   private static void show(class_332 context, ModulePreview.Target target, int w, int h, int radius, float opacity, float ppu) {
      float r = (float)Math.floor((double)((float)radius * ppu));
      GuiBatch batch = BATCH.reset();
      batch.quad(0, 0, w, h, r * 4.0F + 1.5F, 5.5F, r * 4.0F + 2.5F, 6.5F);
      batch.submitTextured(context, ROUNDED, target.id, alpha(16777215, opacity));
   }

   private static void sky(class_332 context, Sky sky, int w, int h, int radius, float opacity) {
      if (RenderSystem.getDevice().precompilePipeline(ROUNDED).isValid()) {
         int[] px = pixels(context, w, h);
         ModulePreview.Target target = target("sky");
         target.ensure(px[0], px[1], false);
         float time = seconds() * sky.previewSpeed();
         Matrix4f view = new Matrix4f().rotateX((float)Math.toRadians(-14.0)).rotateY(time * 0.06F);
         Matrix4f projection = new Matrix4f().perspective((float)Math.toRadians(75.0), (float)px[0] / (float)px[1], 0.05F, 100.0F);
         RenderSystem.backupProjectionMatrix();

         try {
            RenderSystem.setProjectionMatrix(SKY_PROJECTION.method_71123(projection), class_10366.field_54953);
            if (!SkyRenderer.renderPreview(
               sky.previewType(),
               target.framebuffer.method_71639(),
               view,
               time % 7200.0F,
               sky.previewBrightness(),
               sky.previewPalette(),
               sky.previewAuroraColor()
            )) {
               RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.framebuffer.method_30277(), -14998464);
            }
         } finally {
            RenderSystem.restoreProjectionMatrix();
         }

         show(context, target, w, h, radius, opacity, Smooth.pixelsPerUnit(context));
      }
   }

   private static Matrix4f blockView() {
      return new Matrix4f().lookAt(EYE, new Vector3f(0.0F, -0.05F, 0.0F), new Vector3f(0.0F, 1.0F, 0.0F));
   }

   private static Matrix4f blockProjection(float aspect) {
      return new Matrix4f().perspective(BLOCK_FOV, aspect, 0.05F, 100.0F);
   }

   private static void overlay(class_332 context, BlockOverlay overlay, int w, int h, int radius, float opacity, int accent) {
      if (RenderSystem.getDevice().precompilePipeline(ROUNDED).isValid() && RenderSystem.getDevice().precompilePipeline(BLOCK).isValid()) {
         int[] px = pixels(context, w, h);
         ModulePreview.Target target = target("overlay");
         target.ensure(px[0], px[1], true);
         float time = seconds();
         int rgb = overlay.color(time);
         Matrix4f view = blockView();
         Matrix4f projection = blockProjection((float)px[0] / (float)px[1]);
         RenderSystem.backupProjectionMatrix();

         try {
            RenderSystem.setProjectionMatrix(BLOCK_PROJECTION.method_71123(projection), class_10366.field_54953);
            stone(target, view);
            BlockOverlay.Style style = overlay.style.get();
            if (overlay.overlay.get() && style != BlockOverlay.Style.Classic && style != BlockOverlay.Style.Target) {
               AuraRenderer.preview(target.framebuffer, view, style, time, rgb);
            }
         } finally {
            RenderSystem.restoreProjectionMatrix();
         }

         show(context, target, w, h, radius, opacity, Smooth.pixelsPerUnit(context));
         if (overlay.overlay.get()) {
            Matrix4f mvp = new Matrix4f(projection).mul(view);
            BlockOverlay.Style style = overlay.style.get();
            if (style == BlockOverlay.Style.Target) {
               target2d(context, mvp, w, h, time, rgb, accent, opacity);
            } else {
               classic2d(
                  context,
                  mvp,
                  w,
                  h,
                  rgb,
                  style == BlockOverlay.Style.Classic ? (float)overlay.fill.get().intValue() / 100.0F : 0.0F,
                  opacity,
                  overlay.pulse.get(),
                  time
               );
            }
         }
      }
   }

   private static void stone(ModulePreview.Target target, Matrix4f view) {
      GpuTextureView stone = class_310.method_1551().method_1531().method_4619(STONE).method_71659();
      float[][][] faces = new float[][][]{
         {{-0.5F, 0.5F, -0.5F}, {-0.5F, 0.5F, 0.5F}, {0.5F, 0.5F, 0.5F}, {0.5F, 0.5F, -0.5F}},
         {{-0.5F, -0.5F, 0.5F}, {0.5F, -0.5F, 0.5F}, {0.5F, 0.5F, 0.5F}, {-0.5F, 0.5F, 0.5F}},
         {{0.5F, -0.5F, 0.5F}, {0.5F, -0.5F, -0.5F}, {0.5F, 0.5F, -0.5F}, {0.5F, 0.5F, 0.5F}}
      };
      int[] shade = new int[]{-1, -3355444, -6645094};
      float[][] uv = new float[][]{{0.0F, 1.0F}, {1.0F, 1.0F}, {1.0F, 0.0F}, {0.0F, 0.0F}};
      int vertices = faces.length * 6;
      class_9799 bytes = class_9799.method_72201(vertices * class_290.field_1575.getVertexSize());

      try {
         class_287 builder = new class_287(bytes, class_5596.field_27379, class_290.field_1575);

         for (int f = 0; f < faces.length; f++) {
            for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
               float[] p = faces[f][k];
               builder.method_22912(p[0], p[1], p[2]).method_22913(uv[k][0], uv[k][1]).method_39415(shade[f]);
            }
         }

         class_9801 mesh = builder.method_60800();

         try {
            GpuBuffer buffer = class_290.field_1575.uploadImmediateVertexBuffer(mesh.method_60818());
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
               .method_71106(view, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
            class_6367 fb = target.framebuffer;
            RenderPass pass = RenderSystem.getDevice()
               .createCommandEncoder()
               .createRenderPass(() -> "Violet overlay preview block", fb.method_71639(), OptionalInt.of(-15396578), fb.method_71640(), OptionalDouble.of(1.0));

            try {
               pass.setPipeline(BLOCK);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", transforms);
               pass.bindTexture("Sampler0", stone, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
               pass.setVertexBuffer(0, buffer);
               pass.draw(0, vertices);
            } catch (Throwable var19) {
               if (pass != null) {
                  try {
                     pass.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (pass != null) {
               pass.close();
            }
         } catch (Throwable var20) {
            if (mesh != null) {
               try {
                  mesh.close();
               } catch (Throwable var17) {
                  var20.addSuppressed(var17);
               }
            }

            throw var20;
         }

         if (mesh != null) {
            mesh.close();
         }
      } catch (Throwable var21) {
         if (bytes != null) {
            try {
               bytes.close();
            } catch (Throwable var16) {
               var21.addSuppressed(var16);
            }
         }

         throw var21;
      }

      if (bytes != null) {
         bytes.close();
      }
   }

   private static float[] project(Matrix4f mvp, float x, float y, float z, int w, int h) {
      Vector4f v = mvp.transform(new Vector4f(x, y, z, 1.0F));
      float nx = v.x / v.w;
      float ny = v.y / v.w;
      return new float[]{(nx * 0.5F + 0.5F) * (float)w, (1.0F - (ny * 0.5F + 0.5F)) * (float)h};
   }

   private static void line(class_332 context, float[] a, float[] b, float width, int c0, int c1) {
      Smooth.polyline(context, new float[]{a[0], b[0]}, new float[]{a[1], b[1]}, new float[]{width, width}, new int[]{c0, c1}, 2);
   }

   private static void classic2d(class_332 context, Matrix4f mvp, int w, int h, int rgb, float fill, float opacity, int pulseSpeed, float time) {
      float e = 0.503F;
      float[][] c = new float[8][];

      for (int i = 0; i < 8; i++) {
         c[i] = project(mvp, (i & 1) == 0 ? -e : e, (i & 2) == 0 ? -e : e, (i & 4) == 0 ? -e : e, w, h);
      }

      float pulse = pulseSpeed <= 0 ? 1.0F : 0.72F + 0.28F * (float)Math.sin((double)(time * 3.0F * (float)pulseSpeed / 100.0F));
      if (fill > 0.0F) {
         int color = alpha(rgb, fill * pulse * opacity);
         Smooth.polygon(context, new float[]{c[2][0], c[2][1], c[6][0], c[6][1], c[7][0], c[7][1], c[3][0], c[3][1]}, color);
         Smooth.polygon(context, new float[]{c[4][0], c[4][1], c[5][0], c[5][1], c[7][0], c[7][1], c[6][0], c[6][1]}, color);
         Smooth.polygon(context, new float[]{c[1][0], c[1][1], c[5][0], c[5][1], c[7][0], c[7][1], c[3][0], c[3][1]}, color);
      }

      int edge = alpha(rgb, opacity);
      int[][] edges = new int[][]{{1, 3}, {2, 3}, {4, 5}, {4, 6}, {1, 5}, {2, 6}, {3, 7}, {5, 7}, {6, 7}};

      for (int[] pair : edges) {
         line(context, c[pair[0]], c[pair[1]], 1.2F, edge, edge);
      }
   }

   private static void target2d(class_332 context, Matrix4f mvp, int w, int h, float time, int rgb, int accent, float opacity) {
      float spread = 0.13F + 0.012F * (float)Math.sin((double)(time * 3.2F));
      float e = 0.5F + spread;
      float len = 0.3F;
      int top = alpha(rgb, opacity);
      int bottom = alpha(accent, opacity);

      for (int i = 0; i < 8; i++) {
         if (i != 0) {
            float x = (i & 1) == 0 ? -e : e;
            float y = (i & 2) == 0 ? -e : e;
            float z = (i & 4) == 0 ? -e : e;
            int color = y > 0.0F ? top : bottom;
            float[] p = project(mvp, x, y, z, w, h);
            line(context, p, project(mvp, x - Math.signum(x) * len, y, z, w, h), 1.3F, color, color);
            line(context, p, project(mvp, x, y - Math.signum(y) * len, z, w, h), 1.3F, color, color);
            line(context, p, project(mvp, x, y, z - Math.signum(z) * len, w, h), 1.3F, color, color);
         }
      }

      float spin = time * 1.1F;
      ring(context, mvp, w, h, 0.86F, spin, 0.28F, bottom, 1.2F);
      ring(context, mvp, w, h, 0.72F, -spin * 1.35F, -0.42F, top, 0.9F);
   }

   private static void ring(class_332 context, Matrix4f mvp, int w, int h, float radius, float spin, float tilt, int color, float width) {
      for (int arc = 0; arc < 2; arc++) {
         int steps = 26;
         float[] xs = new float[steps + 1];
         float[] ys = new float[steps + 1];
         float[] ws = new float[steps + 1];
         int[] cs = new int[steps + 1];

         for (int s = 0; s <= steps; s++) {
            float t = spin + (float)arc * 3.1415927F + 2.261947F * (float)s / (float)steps;
            float vx = radius * (float)Math.cos((double)t);
            float vz = radius * (float)Math.sin((double)t);
            float vy = vz * (float)Math.sin((double)tilt);
            vz *= (float)Math.cos((double)tilt);
            float[] p = project(mvp, vx, vy, vz, w, h);
            xs[s] = p[0];
            ys[s] = p[1];
            ws[s] = width;
            float fade = (float)Math.sin(3.141592653589793 * (double)s / (double)steps);
            cs[s] = alpha(color, (float)(color >>> 24) / 255.0F * (0.2F + 0.8F * fade));
         }

         Smooth.polyline(context, xs, ys, ws, cs, steps + 1);
      }
   }

   private static void glint(class_332 context, int w, int h, float opacity, int accent) {
      Smooth.glow(context, w / 2, h / 2, Math.round((float)Math.min(w, h) * 0.55F), alpha(accent, 0.22F * opacity));
      if (sword == null) {
         sword = new class_1799(class_1802.field_22022);
         sword.method_57379(class_9334.field_49641, true);
      }

      float scale = (float)Math.min(w, h) * 0.78F / 16.0F;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate((float)w / 2.0F - 8.0F * scale, (float)h / 2.0F - 8.0F * scale);
      matrices.scale(scale, scale);
      context.method_51427(sword, 0, 0);
      matrices.popMatrix();
   }

   private static void accessories(class_332 context, float x, float y, int w, int h, float opacity, int accent) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         Smooth.glow(context, w / 2, Math.round((float)h * 0.45F), Math.round((float)Math.min(w, h) * 0.6F), alpha(accent, 0.22F * opacity));
         float time = seconds();
         Matrix3x2fStack matrices = context.method_51448();
         Vector2f origin = matrices.transformPosition(0.0F, 0.0F, new Vector2f());
         Vector2f corner = matrices.transformPosition((float)w, (float)h, new Vector2f());
         int x1 = Math.round(origin.x);
         int y1 = Math.round(origin.y);
         int x2 = Math.round(corner.x);
         int y2 = Math.round(corner.y);
         int size = Math.max(10, Math.round((float)(y2 - y1) * 0.36F));
         float lookX = (float)(x1 + x2) / 2.0F + (float)Math.sin((double)(time * 0.7F)) * (float)(x2 - x1) * 0.9F;
         float lookY = (float)y1 + (float)(y2 - y1) * 0.35F;
         matrices.pushMatrix();
         matrices.identity();
         class_490.method_2486(context, x1, y1, x2, y2, size, 0.0625F, lookX, lookY, mc.field_1724);
         matrices.popMatrix();
      }
   }

   private static final class Target {
      final class_2960 id;
      final ModulePreview.Texture texture = new ModulePreview.Texture();
      class_6367 framebuffer;

      Target(String name) {
         this.id = class_2960.method_60655("violet", "dynamic/preview_" + name);
      }

      void ensure(int width, int height, boolean depth) {
         if (this.framebuffer == null) {
            this.framebuffer = new class_6367("Violet preview " + this.id.method_12832(), width, height, depth);
            this.texture.use(this.framebuffer.method_30277(), this.framebuffer.method_71639());
            class_310.method_1551().method_1531().method_4616(this.id, this.texture);
         } else if (this.framebuffer.field_1482 != width || this.framebuffer.field_1481 != height) {
            this.framebuffer.method_1234(width, height);
         }

         this.texture.use(this.framebuffer.method_30277(), this.framebuffer.method_71639());
      }
   }

   private static final class Texture extends class_1044 {
      void use(GpuTexture gpu, GpuTextureView view) {
         this.field_56974 = gpu;
         this.field_60597 = view;
         this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      }

      public void close() {
      }
   }
}
