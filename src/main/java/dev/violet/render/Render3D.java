package dev.violet.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import dev.violet.SoberClient;
import dev.violet.gui.Smooth;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_12245;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;

public class Render3D {
   private static final RenderPipeline QUADS_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
      .withLocation(class_2960.method_60655("violet", "pipeline/esp_quads"))
      .withCull(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .build();
   private static final RenderPipeline DEPTH_QUADS_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
      .withLocation(class_2960.method_60655("violet", "pipeline/depth_quads"))
      .withCull(false)
      .withDepthWrite(false)
      .build();
   private static final RenderPipeline LOGO_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
      .withLocation(class_2960.method_60655("violet", "pipeline/esp_logo"))
      .withCull(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .build();
   private static final RenderPipeline DECAL_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
      .withLocation(class_2960.method_60655("violet", "pipeline/decal"))
      .withCull(false)
      .withDepthWrite(false)
      .build();
   private static final int[] LOGO_SIZES = new int[]{256, 128, 64, 32, 16};
   private static final int[] DECAL_SIZES = new int[]{512, 256, 128, 64, 32};
   private static final class_1921[] LOGOS = new class_1921[LOGO_SIZES.length];
   private static final Map<String, class_1921[]> DECALS = new HashMap<>();
   private static final class_1921 DEPTH_QUADS;
   private static final class_1921 QUADS;
   private static final class_1921 LINE_QUADS;
   private static final class_1921 DEPTH_LINE_QUADS;
   private static boolean logosCreated;
   private final class_4598 immediate;
   private double camX;
   private double camY;
   private double camZ;
   private double lookX;
   private double lookY;
   private double lookZ;
   private double pixelScale = 0.001;
   private double viewHalfAngle = 3.141592653589793;
   private boolean depthTest;
   private class_4588 quads;
   private class_4588 depthQuads;
   private class_4588 lineQuads;
   private class_4588 depthLineQuads;
   private float lineWidth = 2.0F;
   private float alpha = 1.0F;
   private final double[] axes = new double[6];
   private static volatile long lastTracer;

   public Render3D() {
      SequencedMap<class_1921, class_9799> buffers = new LinkedHashMap<>();
      buffers.put(DEPTH_QUADS, new class_9799(1048576));
      buffers.put(QUADS, new class_9799(1048576));

      for (class_1921 logo : LOGOS) {
         buffers.put(logo, new class_9799(4096));
      }

      buffers.put(LINE_QUADS, new class_9799(2097152));
      buffers.put(DEPTH_LINE_QUADS, new class_9799(524288));
      this.immediate = class_4597.method_22992(buffers, new class_9799(65536));
   }

   private static void createLogos() {
      if (!logosCreated) {
         logosCreated = true;

         try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655("violet", "textures/logo_mask.png"))) {
            class_1011 image = class_1011.method_4309(stream);

            for (int i = 0; i < LOGO_SIZES.length; i++) {
               int size = LOGO_SIZES[i];
               class_1011 level = new class_1011(size, size, false);
               int step = image.method_4307() / size;

               for (int x = 0; x < size; x++) {
                  for (int y = 0; y < size; y++) {
                     long a = 0L;
                     long r = 0L;
                     long g = 0L;
                     long b = 0L;

                     for (int sx = 0; sx < step; sx++) {
                        for (int sy = 0; sy < step; sy++) {
                           int color = image.method_61940(x * step + sx, y * step + sy);
                           int ca = color >>> 24;
                           a += (long)ca;
                           r += (long)(color >> 16 & 0xFF) * (long)ca;
                           g += (long)(color >> 8 & 0xFF) * (long)ca;
                           b += (long)(color & 0xFF) * (long)ca;
                        }
                     }

                     int count = step * step;
                     int outA = (int)(a / (long)count);
                     int outR = a == 0L ? 255 : (int)(r / a);
                     int outG = a == 0L ? 255 : (int)(g / a);
                     int outB = a == 0L ? 255 : (int)(b / a);
                     level.method_61941(x, y, outA << 24 | outR << 16 | outG << 8 | outB);
                  }
               }

               Smooth.register(class_2960.method_60655("violet", "dynamic/logo_mask_" + size), level);
            }

            image.close();
         } catch (Exception var23) {
            SoberClient.LOG.error("Could not load logo mask", var23);
         }
      }
   }

   private static class_1921[] decalLayers(String texture) {
      if (DECALS.containsKey(texture)) {
         return DECALS.get(texture);
      } else {
         class_1921[] layers = null;

         try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655("violet", "textures/" + texture + ".png"))) {
            class_1011 level = class_1011.method_4309(stream);
            String key = texture.replace('/', '_');
            layers = new class_1921[DECAL_SIZES.length];

            for (int i = 0; i < DECAL_SIZES.length; i++) {
               int size = DECAL_SIZES[i];

               while (level.method_4307() > size) {
                  level = half(level);
               }

               class_2960 id = class_2960.method_60655("violet", "dynamic/decal_" + key + "_" + size);
               class_1011 copy = new class_1011(level.method_4307(), level.method_4323(), false);
               copy.method_4317(level);
               Smooth.register(id, copy);
               layers[i] = class_1921.method_75940(
                  "violet_decal_" + key + "_" + size,
                  class_12247.method_75927(DECAL_PIPELINE)
                     .method_76560(
                        "Sampler0",
                        id,
                        () -> RenderSystem.getSamplerCache()
                              .method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false)
                     )
                     .method_75937()
                     .method_75938()
               );
            }

            level.close();
         } catch (Exception var11) {
            SoberClient.LOG.error("Could not load decal {}", texture, var11);
         }

         DECALS.put(texture, layers);
         return layers;
      }
   }

   private static class_1011 half(class_1011 source) {
      int w = Math.max(1, source.method_4307() / 2);
      int h = Math.max(1, source.method_4323() / 2);
      class_1011 out = new class_1011(w, h, false);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            long a = 0L;
            long r = 0L;
            long g = 0L;
            long b = 0L;

            for (int d = 0; d < 4; d++) {
               int c = source.method_61940(Math.min(source.method_4307() - 1, x * 2 + (d & 1)), Math.min(source.method_4323() - 1, y * 2 + (d >> 1)));
               int ca = c >>> 24;
               a += (long)ca;
               r += (long)(c >> 16 & 0xFF) * (long)ca;
               g += (long)(c >> 8 & 0xFF) * (long)ca;
               b += (long)(c & 0xFF) * (long)ca;
            }

            int outR = a == 0L ? 0 : (int)(r / a);
            int outG = a == 0L ? 0 : (int)(g / a);
            int outB = a == 0L ? 0 : (int)(b / a);
            out.method_61941(x, y, (int)(a / 4L) << 24 | outR << 16 | outG << 8 | outB);
         }
      }

      source.close();
      return out;
   }

   public void decal(String texture, double cx, double y, double cz, double size, double rotation, Color color) {
      color = this.apply(color);
      if (color.a() > 0 && !(size <= 0.0)) {
         class_1921[] layers = decalLayers(texture);
         if (layers != null) {
            double pixels = size / this.pixel(cx, y, cz);
            int index = 0;

            for (int i = DECAL_SIZES.length - 1; i >= 0; i--) {
               if ((double)DECAL_SIZES[i] >= pixels) {
                  index = i;
                  break;
               }
            }

            class_4588 buffer = this.immediate.method_73477(layers[index]);
            int argb = color.packed();
            double half = size / 2.0;
            double[][] corners = new double[][]{{-half, -half, 0.0, 0.0}, {-half, half, 0.0, 1.0}, {half, half, 1.0, 1.0}, {half, -half, 1.0, 0.0}};
            double cos = Math.cos(rotation);
            double sin = Math.sin(rotation);

            for (double[] c : corners) {
               double x = cx + c[0] * cos - c[1] * sin;
               double z = cz + c[0] * sin + c[1] * cos;
               buffer.method_22912((float)(x - this.camX), (float)(y - this.camY), (float)(z - this.camZ))
                  .method_22913((float)c[2], (float)c[3])
                  .method_39415(argb);
            }
         }
      }
   }

   public void begin(class_243 cameraPos, float pitch, float yaw) {
      this.camX = cameraPos.field_1352;
      this.camY = cameraPos.field_1351;
      this.camZ = cameraPos.field_1350;
      class_243 look = class_243.method_1030(pitch, yaw);
      this.lookX = look.field_1352;
      this.lookY = look.field_1351;
      this.lookZ = look.field_1350;
      this.computeAxes();
      class_310 mc = class_310.method_1551();
      double fov = Math.toRadians((double)((Integer)mc.field_1690.method_41808().method_41753()).intValue());
      this.pixelScale = 2.0 * Math.tan(fov / 2.0) / (double)Math.max(1, mc.method_22683().method_4506());
      double aspect = (double)mc.method_22683().method_4489() / (double)Math.max(1, mc.method_22683().method_4506());
      double wide = Math.min(Math.toRadians(170.0), fov * 1.5);
      this.viewHalfAngle = Math.atan(Math.tan(wide / 2.0) * Math.sqrt(1.0 + aspect * aspect)) + Math.toRadians(4.0);
      this.quads = null;
      this.depthQuads = null;
      this.lineQuads = null;
      this.depthLineQuads = null;
      this.depthTest = false;
      createLogos();
   }

   public void end() {
      this.immediate.method_22993();
      this.quads = null;
      this.depthQuads = null;
      this.lineQuads = null;
      this.depthLineQuads = null;
   }

   public void setDepthTest(boolean depthTest) {
      this.depthTest = depthTest;
   }

   public class_243 cameraPos() {
      return new class_243(this.camX, this.camY, this.camZ);
   }

   public void setLineWidth(float width) {
      this.lineWidth = width;
   }

   private double depth(double x, double y, double z) {
      return Math.max(0.05, (x - this.camX) * this.lookX + (y - this.camY) * this.lookY + (z - this.camZ) * this.lookZ);
   }

   private double pixel(double x, double y, double z) {
      return this.depth(x, y, z) * this.pixelScale;
   }

   private class_4588 lineQuads() {
      if (this.depthTest) {
         if (this.depthLineQuads == null) {
            this.depthLineQuads = this.immediate.method_73477(DEPTH_LINE_QUADS);
         }

         return this.depthLineQuads;
      } else {
         if (this.lineQuads == null) {
            this.lineQuads = this.immediate.method_73477(LINE_QUADS);
         }

         return this.lineQuads;
      }
   }

   private static void vertex(class_4588 buffer, double x, double y, double z, int color) {
      buffer.method_22912((float)x, (float)y, (float)z).method_39415(color);
   }

   private void strip(double x1, double y1, double z1, double x2, double y2, double z2, int from, int to) {
      double ax = x1 - this.camX;
      double ay = y1 - this.camY;
      double az = z1 - this.camZ;
      double bx = x2 - this.camX;
      double by = y2 - this.camY;
      double bz = z2 - this.camZ;
      double dx = bx - ax;
      double dy = by - ay;
      double dz = bz - az;
      if (!(dx * dx + dy * dy + dz * dz < 1.0E-12)) {
         double sx = dy * az - dz * ay;
         double sy = dz * ax - dx * az;
         double sz = dx * ay - dy * ax;
         double length = Math.sqrt(sx * sx + sy * sy + sz * sz);
         if (length < 1.0E-9) {
            sx = -dz;
            sy = 0.0;
            sz = dx;
            length = Math.sqrt(sx * sx + dx * dx);
            if (length < 1.0E-9) {
               sx = 1.0;
               sz = 0.0;
               length = 1.0;
            }
         }

         sx /= length;
         sy /= length;
         sz /= length;
         band(this.lineQuads(), ax, ay, az, bx, by, bz, sx, sy, sz, this.pixel(x1, y1, z1), this.pixel(x2, y2, z2), this.lineWidth, from, to);
      }
   }

   public static class_1921 outlineLayer() {
      return DEPTH_LINE_QUADS;
   }

   public static double pixelScale() {
      class_310 mc = class_310.method_1551();
      return 2.0
         * Math.tan(Math.toRadians((double)((Integer)mc.field_1690.method_41808().method_41753()).intValue()) / 2.0)
         / (double)Math.max(1, mc.method_22683().method_4506());
   }

   public static void edge(class_4588 buffer, double ax, double ay, double az, double bx, double by, double bz, class_243 look, float width, int color) {
      edge(buffer, ax, ay, az, bx, by, bz, look, width, color, color);
   }

   public static void edge(class_4588 buffer, double ax, double ay, double az, double bx, double by, double bz, class_243 look, float width, int from, int to) {
      double dx = bx - ax;
      double dy = by - ay;
      double dz = bz - az;
      double sx = dy * az - dz * ay;
      double sy = dz * ax - dx * az;
      double sz = dx * ay - dy * ax;
      double length = Math.sqrt(sx * sx + sy * sy + sz * sz);
      if (!(length < 1.0E-9)) {
         double scale = pixelScale();
         double pa = Math.max(0.05, ax * look.field_1352 + ay * look.field_1351 + az * look.field_1350) * scale;
         double pb = Math.max(0.05, bx * look.field_1352 + by * look.field_1351 + bz * look.field_1350) * scale;
         band(buffer, ax, ay, az, bx, by, bz, sx / length, sy / length, sz / length, pa, pb, width, from, to);
      }
   }

   private static void band(
      class_4588 buffer,
      double ax,
      double ay,
      double az,
      double bx,
      double by,
      double bz,
      double sx,
      double sy,
      double sz,
      double pa,
      double pb,
      float width,
      int from,
      int to
   ) {
      double half = Math.max(0.25, (double)width / 2.0);
      double ca = half * pa;
      double cb = half * pb;
      double fa = (half + 1.0) * pa;
      double fb = (half + 1.0) * pb;
      int clearFrom = from & 16777215;
      int clearTo = to & 16777215;
      vertex(buffer, ax - sx * ca, ay - sy * ca, az - sz * ca, from);
      vertex(buffer, bx - sx * cb, by - sy * cb, bz - sz * cb, to);
      vertex(buffer, bx + sx * cb, by + sy * cb, bz + sz * cb, to);
      vertex(buffer, ax + sx * ca, ay + sy * ca, az + sz * ca, from);
      vertex(buffer, ax - sx * fa, ay - sy * fa, az - sz * fa, clearFrom);
      vertex(buffer, bx - sx * fb, by - sy * fb, bz - sz * fb, clearTo);
      vertex(buffer, bx - sx * cb, by - sy * cb, bz - sz * cb, to);
      vertex(buffer, ax - sx * ca, ay - sy * ca, az - sz * ca, from);
      vertex(buffer, ax + sx * ca, ay + sy * ca, az + sz * ca, from);
      vertex(buffer, bx + sx * cb, by + sy * cb, bz + sz * cb, to);
      vertex(buffer, bx + sx * fb, by + sy * fb, bz + sz * fb, clearTo);
      vertex(buffer, ax + sx * fa, ay + sy * fa, az + sz * fa, clearFrom);
   }

   public void lineGradient(double x1, double y1, double z1, double x2, double y2, double z2, Color from, Color to) {
      from = this.apply(from);
      to = this.apply(to);
      if (from.a() > 0 || to.a() > 0) {
         this.strip(x1, y1, z1, x2, y2, z2, from.packed(), to.packed());
      }
   }

   public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      color = this.apply(color);
      if (color.a() > 0) {
         this.strip(x1, y1, z1, x2, y2, z2, color.packed(), color.packed());
      }
   }

   public void quadGradient(
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      Color c1,
      Color c2,
      Color c3,
      Color c4
   ) {
      class_4588 buffer = this.quads();
      buffer.method_22912((float)(x1 - this.camX), (float)(y1 - this.camY), (float)(z1 - this.camZ)).method_39415(this.apply(c1).packed());
      buffer.method_22912((float)(x2 - this.camX), (float)(y2 - this.camY), (float)(z2 - this.camZ)).method_39415(this.apply(c2).packed());
      buffer.method_22912((float)(x3 - this.camX), (float)(y3 - this.camY), (float)(z3 - this.camZ)).method_39415(this.apply(c3).packed());
      buffer.method_22912((float)(x4 - this.camX), (float)(y4 - this.camY), (float)(z4 - this.camZ)).method_39415(this.apply(c4).packed());
   }

   public void logo(double cx, double y, double cz, double size, double rotation, Color color) {
      color = this.apply(color);
      if (color.a() > 0 && !(size <= 0.0)) {
         double pixels = size / this.pixel(cx, y, cz);
         int index = 0;

         for (int i = LOGO_SIZES.length - 1; i >= 0; i--) {
            if ((double)LOGO_SIZES[i] >= pixels) {
               index = i;
               break;
            }
         }

         class_4588 buffer = this.immediate.method_73477(LOGOS[index]);
         int argb = color.packed();
         double half = size / 2.0;
         double[][] corners = new double[][]{{-half, -half, 0.0, 0.0}, {-half, half, 0.0, 1.0}, {half, half, 1.0, 1.0}, {half, -half, 1.0, 0.0}};
         double cos = Math.cos(rotation);
         double sin = Math.sin(rotation);

         for (double[] c : corners) {
            double x = cx + c[0] * cos - c[1] * sin;
            double z = cz + c[0] * sin + c[1] * cos;
            buffer.method_22912((float)(x - this.camX), (float)(y - this.camY), (float)(z - this.camZ))
               .method_22913((float)c[2], (float)c[3])
               .method_39415(argb);
         }
      }
   }

   public void cylinder(double cx, double cz, double y1, double y2, double radius, Color bottom, Color top, int segments) {
      double step = 6.283185307179586 / (double)segments;

      for (int i = 0; i < segments; i++) {
         double a0 = (double)i * step;
         double a1 = a0 + step;
         double x0 = cx + Math.cos(a0) * radius;
         double z0 = cz + Math.sin(a0) * radius;
         double x1 = cx + Math.cos(a1) * radius;
         double z1 = cz + Math.sin(a1) * radius;
         this.quadGradient(x0, y1, z0, x1, y1, z1, x1, y2, z1, x0, y2, z0, bottom, bottom, top, top);
      }
   }

   private int smoothSegments(double cx, double y, double cz, double radius, int segments) {
      double pixels = radius / this.pixel(cx, y, cz);
      return (int)Math.max((long)segments, Math.min(256L, Math.round(6.283185307179586 * pixels / 5.0)));
   }

   private void annulus(double cx, double y, double cz, double inner, double outer, Color innerColor, Color outerColor, int segments) {
      double step = 6.283185307179586 / (double)segments;

      for (int i = 0; i < segments; i++) {
         double a0 = (double)i * step;
         double a1 = a0 + step;
         double c0 = Math.cos(a0);
         double s0 = Math.sin(a0);
         double c1 = Math.cos(a1);
         double s1 = Math.sin(a1);
         this.quadGradient(
            cx + c0 * inner,
            y,
            cz + s0 * inner,
            cx + c1 * inner,
            y,
            cz + s1 * inner,
            cx + c1 * outer,
            y,
            cz + s1 * outer,
            cx + c0 * outer,
            y,
            cz + s0 * outer,
            innerColor,
            innerColor,
            outerColor,
            outerColor
         );
      }
   }

   public void disc(double cx, double y, double cz, double inner, double outer, Color innerColor, Color outerColor, int segments) {
      int smooth = this.smoothSegments(cx, y, cz, outer, segments);
      this.annulus(cx, y, cz, inner, outer, innerColor, outerColor, smooth);
      double feather = this.pixel(cx, y, cz) * 1.2;
      if (this.apply(outerColor).a() > 0) {
         this.annulus(cx, y, cz, outer, outer + feather, outerColor, outerColor.withAlpha(0), smooth);
      }

      if (inner > feather && this.apply(innerColor).a() > 0) {
         this.annulus(cx, y, cz, inner - feather, inner, innerColor.withAlpha(0), innerColor, smooth);
      }
   }

   public void ring(double cx, double y, double cz, double radius, Color color, int segments) {
      int smooth = this.smoothSegments(cx, y, cz, radius, segments);
      double step = 6.283185307179586 / (double)smooth;

      for (int i = 0; i < smooth; i++) {
         double a0 = (double)i * step;
         double a1 = a0 + step;
         this.line(cx + Math.cos(a0) * radius, y, cz + Math.sin(a0) * radius, cx + Math.cos(a1) * radius, y, cz + Math.sin(a1) * radius, color);
      }
   }

   public class_4588 relativeQuads() {
      return this.quads();
   }

   public int tint(int argb) {
      return this.apply(Color.fromPacked(argb)).packed();
   }

   public double[] billboardBasis() {
      return this.billboardAxes();
   }

   private double[] billboardAxes() {
      return this.axes;
   }

   private void computeAxes() {
      double rx = -this.lookZ;
      double rz = this.lookX;
      double length = Math.sqrt(rx * rx + rz * rz);
      if (length < 1.0E-6) {
         rx = 1.0;
         rz = 0.0;
         length = 1.0;
      }

      rx /= length;
      rz /= length;
      double ux = -rz * this.lookY;
      double uy = rz * this.lookX - rx * this.lookZ;
      double uz = rx * this.lookY;
      double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
      this.axes[0] = rx;
      this.axes[1] = 0.0;
      this.axes[2] = rz;
      this.axes[3] = ux / ul;
      this.axes[4] = uy / ul;
      this.axes[5] = uz / ul;
   }

   public void billboardGlow(double cx, double cy, double cz, double radius, Color center, Color edge, int segments) {
      double pixels = radius / this.pixel(cx, cy, cz);
      if (!(pixels < 0.4)) {
         Color inner = this.apply(center);
         Color outer = this.apply(edge);
         if (inner.a() > 0 || outer.a() > 0) {
            segments = (int)Math.max(6L, Math.min((long)segments, Math.round(6.283185307179586 * pixels / 5.0)));
            double[] axes = this.billboardAxes();
            class_4588 buffer = this.quads();
            double step = 6.283185307179586 / (double)segments;
            float ox = (float)(cx - this.camX);
            float oy = (float)(cy - this.camY);
            float oz = (float)(cz - this.camZ);

            for (int i = 0; i < segments; i++) {
               double a0 = (double)i * step;
               double a1 = a0 + step;
               double c0 = Math.cos(a0) * radius;
               double s0 = Math.sin(a0) * radius;
               double c1 = Math.cos(a1) * radius;
               double s1 = Math.sin(a1) * radius;
               buffer.method_22912(ox, oy, oz).method_39415(inner.packed());
               buffer.method_22912(ox, oy, oz).method_39415(inner.packed());
               buffer.method_22912(
                     (float)((double)ox + axes[0] * c1 + axes[3] * s1),
                     (float)((double)oy + axes[1] * c1 + axes[4] * s1),
                     (float)((double)oz + axes[2] * c1 + axes[5] * s1)
                  )
                  .method_39415(outer.packed());
               buffer.method_22912(
                     (float)((double)ox + axes[0] * c0 + axes[3] * s0),
                     (float)((double)oy + axes[1] * c0 + axes[4] * s0),
                     (float)((double)oz + axes[2] * c0 + axes[5] * s0)
                  )
                  .method_39415(outer.packed());
            }
         }
      }
   }

   public void billboardHalo(double cx, double cy, double cz, double radius, int rgb, float alpha, int segments) {
      double pixels = radius / this.pixel(cx, cy, cz);
      if (!(pixels < 0.6) && !(alpha <= 0.003F)) {
         int rings = (int)Math.max(3L, Math.min(9L, Math.round(pixels / 5.0)));
         segments = (int)Math.max(8L, Math.min((long)segments, Math.round(6.283185307179586 * pixels / 6.0)));
         double[] axes = this.billboardAxes();
         class_4588 buffer = this.quads();
         float fade = (float)this.apply(Color.WHITE).a() / 255.0F;
         float ox = (float)(cx - this.camX);
         float oy = (float)(cy - this.camY);
         float oz = (float)(cz - this.camZ);
         int[] colors = new int[rings + 1];

         for (int i = 0; i <= rings; i++) {
            double t = (double)i / (double)rings;
            double a = (double)(alpha * fade) * Math.exp(-4.0 * t * t) * (1.0 - t * t);
            double white = Math.max(0.0, 1.0 - t * 4.0) * 0.55;
            int r = (int)Math.round((double)(rgb >> 16 & 0xFF) + (double)(255 - (rgb >> 16 & 0xFF)) * white);
            int g = (int)Math.round((double)(rgb >> 8 & 0xFF) + (double)(255 - (rgb >> 8 & 0xFF)) * white);
            int b = (int)Math.round((double)(rgb & 0xFF) + (double)(255 - (rgb & 0xFF)) * white);
            colors[i] = (int)Math.round(Math.max(0.0, Math.min(1.0, a)) * 255.0) << 24 | r << 16 | g << 8 | b;
         }

         double step = 6.283185307179586 / (double)segments;

         for (int i = 0; i < rings; i++) {
            double r0 = radius * (double)i / (double)rings;
            double r1 = radius * (double)(i + 1) / (double)rings;

            for (int s = 0; s < segments; s++) {
               double c0 = Math.cos((double)s * step);
               double s0 = Math.sin((double)s * step);
               double c1 = Math.cos((double)(s + 1) * step);
               double s1 = Math.sin((double)(s + 1) * step);
               buffer.method_22912(
                     (float)((double)ox + (axes[0] * c0 + axes[3] * s0) * r0),
                     (float)((double)oy + (axes[1] * c0 + axes[4] * s0) * r0),
                     (float)((double)oz + (axes[2] * c0 + axes[5] * s0) * r0)
                  )
                  .method_39415(colors[i]);
               buffer.method_22912(
                     (float)((double)ox + (axes[0] * c0 + axes[3] * s0) * r1),
                     (float)((double)oy + (axes[1] * c0 + axes[4] * s0) * r1),
                     (float)((double)oz + (axes[2] * c0 + axes[5] * s0) * r1)
                  )
                  .method_39415(colors[i + 1]);
               buffer.method_22912(
                     (float)((double)ox + (axes[0] * c1 + axes[3] * s1) * r1),
                     (float)((double)oy + (axes[1] * c1 + axes[4] * s1) * r1),
                     (float)((double)oz + (axes[2] * c1 + axes[5] * s1) * r1)
                  )
                  .method_39415(colors[i + 1]);
               buffer.method_22912(
                     (float)((double)ox + (axes[0] * c1 + axes[3] * s1) * r0),
                     (float)((double)oy + (axes[1] * c1 + axes[4] * s1) * r0),
                     (float)((double)oz + (axes[2] * c1 + axes[5] * s1) * r0)
                  )
                  .method_39415(colors[i]);
            }
         }
      }
   }

   public void billboardRing(double cx, double cy, double cz, double radius, Color color, int segments) {
      double[] axes = this.billboardAxes();
      double step = 6.283185307179586 / (double)segments;

      for (int i = 0; i < segments; i++) {
         double a0 = (double)i * step;
         double a1 = a0 + step;
         double c0 = Math.cos(a0) * radius;
         double s0 = Math.sin(a0) * radius;
         double c1 = Math.cos(a1) * radius;
         double s1 = Math.sin(a1) * radius;
         this.line(
            cx + axes[0] * c0 + axes[3] * s0,
            cy + axes[1] * c0 + axes[4] * s0,
            cz + axes[2] * c0 + axes[5] * s0,
            cx + axes[0] * c1 + axes[3] * s1,
            cy + axes[1] * c1 + axes[4] * s1,
            cz + axes[2] * c1 + axes[5] * s1,
            color
         );
      }
   }

   public void beam(double cx, double cz, double bottom, double top, double radius, Color color, double pulse) {
      if (!(top - bottom <= 0.0)) {
         double mid = bottom + (top - bottom) * 0.55;
         Color clear = color.withAlpha(0);
         Color white = new Color(255, 255, 255, color.a());
         this.cylinder(cx, cz, bottom, mid, radius * 4.5, color.scaleAlpha(0.07 * pulse), color.scaleAlpha(0.04 * pulse), 24);
         this.cylinder(cx, cz, mid, top, radius * 4.5, color.scaleAlpha(0.04 * pulse), clear, 24);
         this.cylinder(cx, cz, bottom, mid, radius * 2.2, color.scaleAlpha(0.2 * pulse), color.scaleAlpha(0.12 * pulse), 20);
         this.cylinder(cx, cz, mid, top, radius * 2.2, color.scaleAlpha(0.12 * pulse), clear, 20);
         this.cylinder(cx, cz, bottom, mid, radius, color.scaleAlpha(0.7), color.scaleAlpha(0.45), 16);
         this.cylinder(cx, cz, mid, top, radius, color.scaleAlpha(0.45), clear, 16);
         this.cylinder(cx, cz, bottom, mid, radius * 0.35, white.scaleAlpha(0.5), white.scaleAlpha(0.25), 10);
         this.cylinder(cx, cz, mid, top, radius * 0.35, white.scaleAlpha(0.25), white.withAlpha(0), 10);
         this.disc(cx, bottom + 0.02, cz, 0.0, radius * 6.0, color.scaleAlpha(0.3 * pulse), clear, 32);
      }
   }

   private class_4588 quads() {
      if (this.depthTest) {
         if (this.depthQuads == null) {
            this.depthQuads = this.immediate.method_73477(DEPTH_QUADS);
         }

         return this.depthQuads;
      } else {
         if (this.quads == null) {
            this.quads = this.immediate.method_73477(QUADS);
         }

         return this.quads;
      }
   }

   public void setAlpha(float alpha) {
      this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
   }

   private Color apply(Color color) {
      return this.alpha >= 1.0F ? color : color.scaleAlpha((double)this.alpha);
   }

   public void quad(
      double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color
   ) {
      color = this.apply(color);
      if (color.a() > 0) {
         class_4588 buffer = this.quads();
         int argb = color.packed();
         buffer.method_22912((float)(x1 - this.camX), (float)(y1 - this.camY), (float)(z1 - this.camZ)).method_39415(argb);
         buffer.method_22912((float)(x2 - this.camX), (float)(y2 - this.camY), (float)(z2 - this.camZ)).method_39415(argb);
         buffer.method_22912((float)(x3 - this.camX), (float)(y3 - this.camY), (float)(z3 - this.camZ)).method_39415(argb);
         buffer.method_22912((float)(x4 - this.camX), (float)(y4 - this.camY), (float)(z4 - this.camZ)).method_39415(argb);
      }
   }

   public boolean inView(double x1, double y1, double z1, double x2, double y2, double z2) {
      double cx = (x1 + x2) / 2.0 - this.camX;
      double cy = (y1 + y2) / 2.0 - this.camY;
      double cz = (z1 + z2) / 2.0 - this.camZ;
      double hx = Math.abs(x2 - x1) / 2.0;
      double hy = Math.abs(y2 - y1) / 2.0;
      double hz = Math.abs(z2 - z1) / 2.0;
      double radius = Math.sqrt(hx * hx + hy * hy + hz * hz);
      double distance = Math.sqrt(cx * cx + cy * cy + cz * cz);
      if (distance <= radius + 1.0) {
         return true;
      } else if (this.viewHalfAngle >= 1.5607963267948965) {
         return cx * this.lookX + cy * this.lookY + cz * this.lookZ > -radius;
      } else {
         double cos = (cx * this.lookX + cy * this.lookY + cz * this.lookZ) / distance;
         double angle = Math.acos(Math.max(-1.0, Math.min(1.0, cos)));
         double spread = Math.asin(Math.min(1.0, radius / distance));
         return angle - spread <= this.viewHalfAngle;
      }
   }

   public void box(double x1, double y1, double z1, double x2, double y2, double z2, Color sides, Color outline, ShapeMode mode) {
      if (this.inView(x1, y1, z1, x2, y2, z2)) {
         if (mode.sides()) {
            this.boxSides(x1, y1, z1, x2, y2, z2, sides.scaleAlpha(0.28));
         }

         if (mode.lines()) {
            this.boxLines(x1, y1, z1, x2, y2, z2, outline);
         }
      }
   }

   public void box(class_238 box, Color sides, Color outline, ShapeMode mode) {
      this.box(box.field_1323, box.field_1322, box.field_1321, box.field_1320, box.field_1325, box.field_1324, sides, outline, mode);
   }

   public void box(class_2338 pos, Color sides, Color outline, ShapeMode mode) {
      this.box(
         (double)pos.method_10263(),
         (double)pos.method_10264(),
         (double)pos.method_10260(),
         (double)(pos.method_10263() + 1),
         (double)(pos.method_10264() + 1),
         (double)(pos.method_10260() + 1),
         sides,
         outline,
         mode
      );
   }

   public void boxLines(double x1, double y1, double z1, double x2, double y2, double z2, Color c) {
      this.line(x1, y1, z1, x2, y1, z1, c);
      this.line(x2, y1, z1, x2, y1, z2, c);
      this.line(x2, y1, z2, x1, y1, z2, c);
      this.line(x1, y1, z2, x1, y1, z1, c);
      if (!(y2 - y1 < 0.001)) {
         this.line(x1, y2, z1, x2, y2, z1, c);
         this.line(x2, y2, z1, x2, y2, z2, c);
         this.line(x2, y2, z2, x1, y2, z2, c);
         this.line(x1, y2, z2, x1, y2, z1, c);
         this.line(x1, y1, z1, x1, y2, z1, c);
         this.line(x2, y1, z1, x2, y2, z1, c);
         this.line(x2, y1, z2, x2, y2, z2, c);
         this.line(x1, y1, z2, x1, y2, z2, c);
      }
   }

   public void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, Color c) {
      this.quad(x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, c);
      if (!(y2 - y1 < 0.001)) {
         this.quad(x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, c);
         this.quad(x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, c);
         this.quad(x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, c);
         this.quad(x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, c);
         this.quad(x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, c);
      }
   }

   public void tracer(double x, double y, double z, Color color) {
      this.line(this.camX + this.lookX, this.camY + this.lookY, this.camZ + this.lookZ, x, y, z, color);
   }

   public static boolean tracersActive() {
      return System.currentTimeMillis() - lastTracer < 300L;
   }

   public void tracer(double x, double y, double z, Color color, float width, boolean fade) {
      lastTracer = System.currentTimeMillis();
      double dx = x - this.camX;
      double dy = y - this.camY;
      double dz = z - this.camZ;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      Color c = color.scaleAlpha(Math.max(0.5, 1.0 - distance / 240.0));
      float previous = this.lineWidth;
      double sx = this.camX + this.lookX;
      double sy = this.camY + this.lookY;
      double sz = this.camZ + this.lookZ;
      this.lineWidth = width * 2.8F;
      this.lineGradient(sx, sy, sz, x, y, z, c.scaleAlpha(fade ? 0.04 : 0.15), c.scaleAlpha(0.22));
      this.lineWidth = width;
      if (fade) {
         this.lineGradient(sx, sy, sz, x, y, z, c.scaleAlpha(0.35), c);
      } else {
         this.line(sx, sy, sz, x, y, z, c);
      }

      this.lineWidth = previous;
   }

   static {
      for (int i = 0; i < LOGO_SIZES.length; i++) {
         class_2960 id = class_2960.method_60655("violet", "dynamic/logo_mask_" + LOGO_SIZES[i]);
         LOGOS[i] = class_1921.method_75940(
            "violet_logo_" + LOGO_SIZES[i],
            class_12247.method_75927(LOGO_PIPELINE)
               .method_76560(
                  "Sampler0",
                  id,
                  () -> RenderSystem.getSamplerCache()
                        .method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false)
               )
               .method_75937()
               .method_75938()
         );
      }

      DEPTH_QUADS = class_1921.method_75940("violet_depth_quads", class_12247.method_75927(DEPTH_QUADS_PIPELINE).method_75938());
      QUADS = class_1921.method_75940("violet_quads", class_12247.method_75927(QUADS_PIPELINE).method_75937().method_75938());
      LINE_QUADS = class_1921.method_75940("violet_line_quads", class_12247.method_75927(QUADS_PIPELINE).method_75937().method_75938());
      DEPTH_LINE_QUADS = class_1921.method_75940(
         "violet_depth_line_quads", class_12247.method_75927(DEPTH_QUADS_PIPELINE).method_75930(class_12245.field_63976).method_75937().method_75938()
      );
   }
}
