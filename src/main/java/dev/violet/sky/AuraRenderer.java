package dev.violet.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import dev.violet.SoberClient;
import dev.violet.gui.Theme;
import dev.violet.modules.BlockOverlay;
import dev.violet.render.Render3D;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_10799;
import net.minecraft.class_12074;
import net.minecraft.class_12249;
import net.minecraft.class_156;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.class_9848;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class AuraRenderer {
   private static final RenderPipeline GALAXY_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
      .withLocation(class_2960.method_60655("violet", "pipeline/aura_galaxy"))
      .withVertexShader(class_2960.method_60655("violet", "core/aura_galaxy"))
      .withFragmentShader(class_2960.method_60655("violet", "core/aura_galaxy"))
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withCull(false)
      .withVertexFormat(class_290.field_1592, class_5596.field_27379)
      .build();
   private static final RenderPipeline PALM_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
      .withLocation(class_2960.method_60655("violet", "pipeline/aura_palm"))
      .withVertexShader(class_2960.method_60655("violet", "core/aura_palm"))
      .withFragmentShader(class_2960.method_60655("violet", "core/aura_palm"))
      .withSampler("Sampler0")
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withCull(false)
      .withVertexFormat(class_290.field_1585, class_5596.field_27379)
      .build();
   private static final RenderPipeline HOLOGRAM_PIPELINE = facePipeline("hologram");
   private static final RenderPipeline OPAL_PIPELINE = facePipeline("opal");
   private static final RenderPipeline MOLTEN_PIPELINE = facePipeline("molten");
   private static final int[][] FACE_CORNERS = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};
   private static final class_2960 PALM_TEXTURE = class_2960.method_60655("violet", "textures/aura/palm.png");
   private static final int[][] FACES = new int[][]{{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
   private static final int[][] PALM_FACES = new int[][]{{1, 0, 3, 2}, {4, 5, 6, 7}, {0, 4, 7, 3}, {5, 1, 2, 6}, {7, 6, 2, 3}, {0, 1, 5, 4}};
   private static final float[][] UV = new float[][]{{0.0F, 1.0F}, {1.0F, 1.0F}, {1.0F, 0.0F}, {0.0F, 0.0F}};
   private static final float SLIDE_MS = 170.0F;
   private static class_2338 targetPos;
   private static class_238 shownBox;
   private static class_238 fromBox;
   private static long switchedAt;
   private static long targetSince;
   private static long targetFrame;
   private static float targetProgress;
   private static double targetSpin;
   private static class_276 previewTarget;
   private static Matrix4f previewView;

   private AuraRenderer() {
   }

   public static boolean render(WorldRenderContext context, class_12074 outline) {
      BlockOverlay overlay = SoberClient.MODULES.get(BlockOverlay.class);
      if (overlay.isActive() && overlay.overlay.get()) {
         class_265 shape = outline.comp_4935();
         if (shape.method_1110()) {
            return true;
         } else {
            class_243 camera = context.worldState().field_63082.field_63078;
            class_2338 pos = outline.comp_4932();
            double x = (double)pos.method_10263() - camera.field_1352;
            double y = (double)pos.method_10264() - camera.field_1351;
            double z = (double)pos.method_10260() - camera.field_1350;
            float seconds = (float)(class_156.method_658() % 3600000L) / 1000.0F;
            int pulseSpeed = overlay.pulse.get();
            float pulse = pulseSpeed <= 0 ? 1.0F : 0.72F + 0.28F * (float)Math.sin((double)(seconds * 3.0F * (float)pulseSpeed / 100.0F));
            int rgb = overlay.color(seconds);
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            class_4587 matrices = context.matrices();
            class_4597 consumers = context.consumers();
            float baseWidth = class_310.method_1551().method_22683().method_75291();
            BlockOverlay.Style style = overlay.style.get();
            class_4184 view = class_310.method_1551().field_1773.method_19418();
            class_243 look = class_243.method_1030(view.method_19329(), view.method_19330());
            float width = baseWidth * (0.75F + 0.25F * (float)overlay.width.get().intValue());
            if (style == BlockOverlay.Style.Target) {
               drawTarget(consumers, matrices, pos, shape.method_1107().method_1014(0.02).method_996(pos), camera, seconds, rgb, look, width);
               return false;
            } else {
               if (style == BlockOverlay.Style.Galaxy) {
                  drawGalaxy(shape.method_1090(), x, y, z, seconds, rgb);
               } else if (style == BlockOverlay.Style.Palm) {
                  drawPalms(shape.method_1090(), x, y, z, seconds, rgb);
               } else if (style == BlockOverlay.Style.Hologram) {
                  drawFaces(shape.method_1090(), x, y, z, seconds, rgb, HOLOGRAM_PIPELINE, "Violet hologram aura");
               } else if (style == BlockOverlay.Style.Opal) {
                  drawFaces(shape.method_1090(), x, y, z, seconds, rgb, OPAL_PIPELINE, "Violet opal aura");
               } else if (style == BlockOverlay.Style.Molten) {
                  drawFaces(shape.method_1090(), x, y, z, seconds, rgb, MOLTEN_PIPELINE, "Violet molten aura");
               } else if (overlay.fill.get() > 0) {
                  int fillAlpha = Math.round((float)(255 * overlay.fill.get()) / 100.0F * pulse);
                  class_4588 fill = consumers.method_73477(class_12249.method_76019());

                  for (class_238 box : shape.method_1090()) {
                     drawBox(matrices, fill, box.method_1014(0.0025).method_989(x, y, z), class_9848.method_61324(fillAlpha, r, g, b));
                  }
               }

               class_4588 edge = consumers.method_73477(Render3D.outlineLayer());
               int color = class_9848.method_61324(255, r, g, b);
               shape.method_1104((x1, y1, z1, x2, y2, z2) -> Render3D.edge(edge, x1 + x, y1 + y, z1 + z, x2 + x, y2 + y, z2 + z, look, width, color));
               return false;
            }
         }
      } else {
         return true;
      }
   }

   private static void drawTarget(
      class_4597 consumers, class_4587 matrices, class_2338 pos, class_238 world, class_243 camera, float seconds, int rgb, class_243 look, float width
   ) {
      long now = class_156.method_658();
      boolean fresh = targetFrame == 0L || now - targetFrame > 150L || shownBox == null;
      float dt = fresh ? 0.0F : Math.min(0.1F, (float)(now - targetFrame) / 1000.0F);
      targetFrame = now;
      if (fresh) {
         shownBox = world;
         fromBox = world;
         targetPos = pos.method_10062();
         targetSince = now;
         switchedAt = 0L;
      } else if (!pos.equals(targetPos)) {
         fromBox = shownBox;
         targetPos = pos.method_10062();
         switchedAt = now;
      }

      float slide = switchedAt == 0L ? 1.0F : Math.min(1.0F, (float)(now - switchedAt) / 170.0F);
      double eased = 1.0 - Math.pow(1.0 - (double)slide, 3.0);
      shownBox = slide >= 1.0F
         ? world
         : new class_238(
            lerp(fromBox.field_1323, world.field_1323, eased),
            lerp(fromBox.field_1322, world.field_1322, eased),
            lerp(fromBox.field_1321, world.field_1321, eased),
            lerp(fromBox.field_1320, world.field_1320, eased),
            lerp(fromBox.field_1325, world.field_1325, eased),
            lerp(fromBox.field_1324, world.field_1324, eased)
         );
      class_238 b = shownBox.method_989(-camera.field_1352, -camera.field_1351, -camera.field_1350);
      float appear = Math.min(1.0F, (float)(now - targetSince) / 260.0F);
      appear = 1.0F - (1.0F - appear) * (1.0F - appear) * (1.0F - appear);
      class_310 mc = class_310.method_1551();
      float goal = 0.0F;
      if (mc.field_1761 != null && mc.field_1761.method_2923()) {
         goal = (float)Math.max(0, mc.field_1761.method_51888() + 1) / 10.0F;
      }

      targetProgress = targetProgress + (goal - targetProgress) * (1.0F - (float)Math.exp((double)(-dt * 14.0F)));
      float progress = Math.max(0.0F, Math.min(1.0F, targetProgress));
      targetSpin += (double)dt * (1.1 + 3.2 * (double)progress);
      int top = fade(0xFF000000 | rgb, appear);
      int bottom = fade(0xFF000000 | Theme.accent(), appear);
      double spread = 0.13 * (double)(1.0F - progress) + 0.24 * (double)(1.0F - appear) + 0.012 * Math.sin((double)seconds * 3.2);
      class_238 e = b.method_1014(spread);
      double shrink = 1.0 - 0.35 * (double)progress;
      double lx = Math.min(0.3, b.method_17939() * 0.28) * shrink;
      double ly = Math.min(0.3, b.method_17940() * 0.28) * shrink;
      double lz = Math.min(0.3, b.method_17941() * 0.28) * shrink;
      double[] xs = new double[]{e.field_1323, e.field_1320};
      double[] ys = new double[]{e.field_1322, e.field_1325};
      double[] zs = new double[]{e.field_1321, e.field_1324};
      float reach = (float)(ly / Math.max(0.001, e.method_17940()));
      class_4588 edge = consumers.method_73477(Render3D.outlineLayer());

      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
               double cx = xs[i];
               double cy = ys[j];
               double cz = zs[k];
               int c = j == 1 ? top : bottom;
               int toward = fade(mix(c, j == 1 ? bottom : top, reach), appear);
               Render3D.edge(edge, cx, cy, cz, cx + (i == 0 ? lx : -lx), cy, cz, look, width, c, c);
               Render3D.edge(edge, cx, cy, cz, cx, cy, cz + (k == 0 ? lz : -lz), look, width, c, c);
               Render3D.edge(edge, cx, cy, cz, cx, cy + (j == 0 ? ly : -ly), cz, look, width, c, toward);
            }
         }
      }

      double cx = (b.field_1323 + b.field_1320) / 2.0;
      double cy = (b.field_1322 + b.field_1325) / 2.0;
      double cz = (b.field_1321 + b.field_1324) / 2.0;
      double radius = (Math.max(b.method_17939(), b.method_17941()) * 0.5 * 1.32 + 0.06) * (1.0 + 0.3 * (double)(1.0F - appear) - 0.12 * (double)progress);
      int alpha = Math.round(255.0F * appear);
      ring(edge, cx, cy, cz, radius, targetSpin, Math.toRadians(16.0), (double)seconds * 0.6, look, width, bottom, alpha);
      ring(
         edge,
         cx,
         cy,
         cz,
         radius * 0.84,
         -targetSpin * 1.35,
         Math.toRadians(-24.0),
         (double)seconds * 0.4 + 1.7,
         look,
         width * 0.7F,
         top,
         Math.round(150.0F * appear)
      );
      reticle(edge, b, progress, appear, look, width, top, bottom);
      class_4588 fill = consumers.method_73477(class_12249.method_76019());
      double d = 0.03;

      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
               drawBox(matrices, fill, new class_238(xs[i] - d, ys[j] - d, zs[k] - d, xs[i] + d, ys[j] + d, zs[k] + d), j == 1 ? top : bottom);
            }
         }
      }
   }

   private static void reticle(class_4588 edge, class_238 b, float progress, float appear, class_243 look, float width, int inner, int outer) {
      double cx = (b.field_1323 + b.field_1320) / 2.0;
      double cy = (b.field_1322 + b.field_1325) / 2.0;
      double cz = (b.field_1321 + b.field_1324) / 2.0;
      double length = Math.sqrt(cx * cx + cy * cy + cz * cz);
      if (!(length < 1.0E-4)) {
         double nx = -cx / length;
         double ny = -cy / length;
         double nz = -cz / length;
         double hx = b.method_17939() / 2.0;
         double hy = b.method_17940() / 2.0;
         double hz = b.method_17941() / 2.0;
         double t = Math.min(hx / Math.max(1.0E-6, Math.abs(nx)), Math.min(hy / Math.max(1.0E-6, Math.abs(ny)), hz / Math.max(1.0E-6, Math.abs(nz)))) + 0.03;
         double px = cx + nx * t;
         double py = cy + ny * t;
         double pz = cz + nz * t;
         double rx = -nz;
         double ry = 0.0;
         double rz = nx;
         double rl = Math.sqrt(rx * rx + nx * nx);
         if (rl < 1.0E-4) {
            rx = 1.0;
            rz = 0.0;
            rl = 1.0;
         }

         rx /= rl;
         rz /= rl;
         double ux = ny * rz - nz * ry;
         double uy = nz * rx - nx * rz;
         double uz = nx * ry - ny * rx;
         double[] center = new double[]{px, py, pz};
         double[] right = new double[]{rx, ry, rz};
         double[] up = new double[]{ux, uy, uz};
         double size = Math.min(hx, Math.min(hy, hz)) * 2.0;
         double r = size * 0.34 * (1.0 - 0.45 * (double)progress) * (0.6 + 0.4 * (double)appear);

         for (int k = 0; k < 2; k++) {
            arc(edge, center, right, up, r, targetSpin * 1.2 + (double)k * 3.141592653589793, 1.9477874452256718, look, width, outer);
         }

         for (int k = 0; k < 4; k++) {
            arc(edge, center, right, up, r * 0.62, -targetSpin * 0.8 + (double)k * 3.141592653589793 / 2.0, 1.1309733552923256, look, width * 0.9F, inner);
         }

         double s = r * 0.1;
         double[][] corners = new double[][]{{-s, -s}, {s, -s}, {s, s}, {-s, s}};

         for (int k = 0; k < 4; k++) {
            double[] a = corners[k];
            double[] c = corners[(k + 1) % 4];
            Render3D.edge(
               edge,
               px + right[0] * a[0] + up[0] * a[1],
               py + right[1] * a[0] + up[1] * a[1],
               pz + right[2] * a[0] + up[2] * a[1],
               px + right[0] * c[0] + up[0] * c[1],
               py + right[1] * c[0] + up[1] * c[1],
               pz + right[2] * c[0] + up[2] * c[1],
               look,
               width * 0.9F,
               inner,
               inner
            );
         }
      }
   }

   private static void arc(
      class_4588 edge, double[] center, double[] right, double[] up, double radius, double start, double span, class_243 look, float width, int color
   ) {
      int steps = 14;
      double ox = 0.0;
      double oy = 0.0;
      double oz = 0.0;

      for (int s = 0; s <= steps; s++) {
         double a = start + span * (double)s / (double)steps;
         double cos = Math.cos(a) * radius;
         double sin = Math.sin(a) * radius;
         double x = center[0] + right[0] * cos + up[0] * sin;
         double y = center[1] + right[1] * cos + up[1] * sin;
         double z = center[2] + right[2] * cos + up[2] * sin;
         if (s > 0) {
            Render3D.edge(edge, ox, oy, oz, x, y, z, look, width, color, color);
         }

         ox = x;
         oy = y;
         oz = z;
      }
   }

   private static double lerp(double from, double to, double t) {
      return from + (to - from) * t;
   }

   private static int fade(int color, float amount) {
      int alpha = Math.round((float)(color >>> 24) * Math.max(0.0F, Math.min(1.0F, amount)));
      return alpha << 24 | color & 16777215;
   }

   private static void ring(
      class_4588 edge, double cx, double cy, double cz, double radius, double spin, double tilt, double axis, class_243 look, float width, int color, int alpha
   ) {
      double kx = Math.cos(axis);
      double kz = Math.sin(axis);
      double cos = Math.cos(tilt);
      double sin = Math.sin(tilt);
      double span = 2.261946710584651;
      int steps = 26;

      for (int arc = 0; arc < 2; arc++) {
         double start = spin + (double)arc * 3.141592653589793;
         double px = 0.0;
         double py = 0.0;
         double pz = 0.0;
         int previous = 0;

         for (int s = 0; s <= steps; s++) {
            double t = start + span * (double)s / (double)steps;
            double vx = radius * Math.cos(t);
            double vz = radius * Math.sin(t);
            double dot = kx * vx + kz * vz;
            double rx = vx * cos + kx * dot * (1.0 - cos);
            double ry = (kz * vx - kx * vz) * sin;
            double rz = vz * cos + kz * dot * (1.0 - cos);
            float fade = (float)Math.sin(3.141592653589793 * (double)s / (double)steps);
            int current = ((int)((float)alpha * (0.2F + 0.8F * fade)) & 0xFF) << 24 | color & 16777215;
            if (s > 0) {
               Render3D.edge(edge, cx + px, cy + py, cz + pz, cx + rx, cy + ry, cz + rz, look, width, previous, current);
            }

            px = rx;
            py = ry;
            pz = rz;
            previous = current;
         }
      }
   }

   private static int mix(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return 0xFF000000
         | Math.round((float)ar + (float)(br - ar) * t) << 16
         | Math.round((float)ag + (float)(bg - ag) * t) << 8
         | Math.round((float)ab + (float)(bb - ab) * t);
   }

   private static float[][] corners(class_238 box, double x, double y, double z) {
      class_238 b = box.method_1014(0.003).method_989(x, y, z);
      float x0 = (float)b.field_1323;
      float y0 = (float)b.field_1322;
      float z0 = (float)b.field_1321;
      float x1 = (float)b.field_1320;
      float y1 = (float)b.field_1325;
      float z1 = (float)b.field_1324;
      return new float[][]{{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
   }

   private static void drawGalaxy(List<class_238> boxes, double x, double y, double z, float seconds, int rgb) {
      if (RenderSystem.getDevice().precompilePipeline(GALAXY_PIPELINE).isValid()) {
         int vertices = boxes.size() * 36;
         class_9799 bytes = class_9799.method_72201(vertices * class_290.field_1592.getVertexSize());

         try {
            class_287 builder = new class_287(bytes, class_5596.field_27379, class_290.field_1592);

            for (class_238 box : boxes) {
               float[][] c = corners(box, x, y, z);

               for (int[] f : FACES) {
                  for (int i : new int[]{f[0], f[1], f[2], f[0], f[2], f[3]}) {
                     builder.method_22912(c[i][0], c[i][1], c[i][2]);
                  }
               }
            }

            draw(builder, class_290.field_1592, GALAXY_PIPELINE, "Violet galaxy aura", vertices, seconds, rgb, null);
         } catch (Throwable var24) {
            if (bytes != null) {
               try {
                  bytes.close();
               } catch (Throwable var23) {
                  var24.addSuppressed(var23);
               }
            }

            throw var24;
         }

         if (bytes != null) {
            bytes.close();
         }
      }
   }

   private static void drawPalms(List<class_238> boxes, double x, double y, double z, float seconds, int rgb) {
      if (RenderSystem.getDevice().precompilePipeline(PALM_PIPELINE).isValid()) {
         GpuTextureView texture = class_310.method_1551().method_1531().method_4619(PALM_TEXTURE).method_71659();
         int vertices = boxes.size() * 36;
         class_9799 bytes = class_9799.method_72201(vertices * class_290.field_1585.getVertexSize());

         try {
            class_287 builder = new class_287(bytes, class_5596.field_27379, class_290.field_1585);

            for (class_238 box : boxes) {
               float[][] c = corners(box, x, y, z);

               for (int[] f : PALM_FACES) {
                  for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
                     builder.method_22912(c[f[k]][0], c[f[k]][1], c[f[k]][2]).method_22913(UV[k][0], UV[k][1]);
                  }
               }
            }

            draw(builder, class_290.field_1585, PALM_PIPELINE, "Violet palm aura", vertices, seconds, rgb, texture);
         } catch (Throwable var25) {
            if (bytes != null) {
               try {
                  bytes.close();
               } catch (Throwable var24) {
                  var25.addSuppressed(var24);
               }
            }

            throw var25;
         }

         if (bytes != null) {
            bytes.close();
         }
      }
   }

   private static RenderPipeline facePipeline(String name) {
      return RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("violet", "pipeline/aura_" + name))
         .withVertexShader(class_2960.method_60655("violet", "core/aura_face"))
         .withFragmentShader(class_2960.method_60655("violet", "core/aura_" + name))
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27379)
         .build();
   }

   private static void drawFaces(List<class_238> boxes, double x, double y, double z, float seconds, int rgb, RenderPipeline pipeline, String label) {
      if (!boxes.isEmpty() && RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
         class_238 bounds = boxes.get(0);

         for (class_238 box : boxes) {
            bounds = bounds.method_991(box);
         }

         double[] low = new double[]{bounds.field_1323, bounds.field_1322, bounds.field_1321};
         double[] size = new double[]{
            Math.max(1.0E-4, bounds.method_17939()), Math.max(1.0E-4, bounds.method_17940()), Math.max(1.0E-4, bounds.method_17941())
         };
         int vertices = boxes.size() * 36;
         class_9799 bytes = class_9799.method_72201(vertices * class_290.field_1575.getVertexSize());

         try {
            class_287 builder = new class_287(bytes, class_5596.field_27379, class_290.field_1575);

            for (class_238 box : boxes) {
               class_238 b = box.method_1014(0.003);
               double[] min = new double[]{b.field_1323, b.field_1322, b.field_1321};
               double[] max = new double[]{b.field_1320, b.field_1325, b.field_1324};

               for (int face = 0; face < 6; face++) {
                  int axis = face < 2 ? 1 : (face < 4 ? 2 : 0);
                  int ua = axis == 0 ? 2 : 0;
                  int va = axis == 1 ? 2 : 1;
                  double[][] corner = new double[4][3];

                  for (int k = 0; k < 4; k++) {
                     corner[k][axis] = face % 2 == 1 ? max[axis] : min[axis];
                     corner[k][ua] = FACE_CORNERS[k][0] == 0 ? min[ua] : max[ua];
                     corner[k][va] = FACE_CORNERS[k][1] == 0 ? min[va] : max[va];
                  }

                  for (int k : new int[]{0, 1, 2, 0, 2, 3}) {
                     double[] p = corner[k];
                     builder.method_22912((float)(p[0] + x), (float)(p[1] + y), (float)(p[2] + z))
                        .method_22913((float)FACE_CORNERS[k][0], (float)FACE_CORNERS[k][1])
                        .method_1336(unit((p[0] - low[0]) / size[0]), unit((p[1] - low[1]) / size[1]), unit((p[2] - low[2]) / size[2]), face * 51);
                  }
               }
            }

            draw(builder, class_290.field_1575, pipeline, label, vertices, seconds, rgb, null);
         } catch (Throwable var33) {
            if (bytes != null) {
               try {
                  bytes.close();
               } catch (Throwable var32) {
                  var33.addSuppressed(var32);
               }
            }

            throw var33;
         }

         if (bytes != null) {
            bytes.close();
         }
      }
   }

   private static int unit(double value) {
      return (int)Math.round(Math.max(0.0, Math.min(1.0, value)) * 255.0);
   }

   public static void preview(class_276 target, Matrix4f view, BlockOverlay.Style style, float seconds, int rgb) {
      previewTarget = target;
      previewView = view;

      try {
         List<class_238> boxes = List.of(new class_238(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));
         switch (style) {
            case Galaxy:
               drawGalaxy(boxes, 0.0, 0.0, 0.0, seconds, rgb);
               break;
            case Palm:
               drawPalms(boxes, 0.0, 0.0, 0.0, seconds, rgb);
               break;
            case Hologram:
               drawFaces(boxes, 0.0, 0.0, 0.0, seconds, rgb, HOLOGRAM_PIPELINE, "Violet hologram preview");
               break;
            case Opal:
               drawFaces(boxes, 0.0, 0.0, 0.0, seconds, rgb, OPAL_PIPELINE, "Violet opal preview");
               break;
            case Molten:
               drawFaces(boxes, 0.0, 0.0, 0.0, seconds, rgb, MOLTEN_PIPELINE, "Violet molten preview");
         }
      } finally {
         previewTarget = null;
         previewView = null;
      }
   }

   private static void draw(
      class_287 builder, VertexFormat format, RenderPipeline pipeline, String label, int vertices, float seconds, int rgb, @Nullable GpuTextureView texture
   ) {
      class_9801 mesh = builder.method_60800();

      try {
         GpuBuffer buffer = format.uploadImmediateVertexBuffer(mesh.method_60818());
         GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
            .method_71106(
               previewView != null ? previewView : RenderSystem.getModelViewMatrix(),
               new Vector4f(seconds, (float)(rgb >> 16 & 0xFF) / 255.0F, (float)(rgb >> 8 & 0xFF) / 255.0F, (float)(rgb & 0xFF) / 255.0F),
               new Vector3f(),
               new Matrix4f()
            );
         class_276 target = previewTarget != null ? previewTarget : class_310.method_1551().method_1522();
         RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> label, target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty());

         try {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            if (texture != null) {
               pass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().method_75297(FilterMode.LINEAR));
            }

            pass.setVertexBuffer(0, buffer);
            pass.draw(0, vertices);
         } catch (Throwable var17) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }
            }

            throw var17;
         }

         if (pass != null) {
            pass.close();
         }
      } catch (Throwable var18) {
         if (mesh != null) {
            try {
               mesh.close();
            } catch (Throwable var15) {
               var18.addSuppressed(var15);
            }
         }

         throw var18;
      }

      if (mesh != null) {
         mesh.close();
      }
   }

   private static void drawBox(class_4587 matrices, class_4588 consumer, class_238 box, int color) {
      class_4665 entry = matrices.method_23760();
      float x0 = (float)box.field_1323;
      float y0 = (float)box.field_1322;
      float z0 = (float)box.field_1321;
      float x1 = (float)box.field_1320;
      float y1 = (float)box.field_1325;
      float z1 = (float)box.field_1324;
      quad(entry, consumer, color, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
      quad(entry, consumer, color, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
      quad(entry, consumer, color, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
      quad(entry, consumer, color, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
      quad(entry, consumer, color, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
      quad(entry, consumer, color, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
   }

   private static void quad(
      class_4665 entry,
      class_4588 consumer,
      int color,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float cx,
      float cy,
      float cz,
      float dx,
      float dy,
      float dz
   ) {
      consumer.method_56824(entry, ax, ay, az).method_39415(color);
      consumer.method_56824(entry, bx, by, bz).method_39415(color);
      consumer.method_56824(entry, cx, cy, cz).method_39415(color);
      consumer.method_56824(entry, dx, dy, dz).method_39415(color);
   }

   public static int hsvToRgb(float h, float s, float v) {
      float r = channel(5, h, s, v);
      float g = channel(3, h, s, v);
      float b = channel(1, h, s, v);
      return Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
   }

   private static float channel(int n, float h, float s, float v) {
      float k = ((float)n + h * 6.0F) % 6.0F;
      return v - v * s * Math.max(0.0F, Math.min(Math.min(k, 4.0F - k), 1.0F));
   }
}
