package dev.violet.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;

public final class SplashRenderer {
   private static final RenderPipeline DOME_PIPELINE = RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
      .withLocation(class_2960.method_60655("violet", "pipeline/splash_dome"))
      .withCull(false)
      .withDepthWrite(false)
      .build();
   public static final class_1921 PUDDLE = layer("PUDDLE", BlendFunction.TRANSLUCENT);
   public static final class_1921 RING = layer("RING", BlendFunction.TRANSLUCENT);
   public static final class_1921 DOME = class_1921.method_75940("violet_splash_dome", class_12247.method_75927(DOME_PIPELINE).method_75937().method_75938());
   public static final class_1921 DROP = layer("DROP", BlendFunction.TRANSLUCENT);
   public static final class_1921 BUBBLE = layer("BUBBLE", BlendFunction.TRANSLUCENT);
   public static final class_1921 MIST = layer("MIST", BlendFunction.LIGHTNING);
   public static final class_1921 FLASH = layer("FLASH", BlendFunction.LIGHTNING);
   public static final class_1921 SPARKLE = layer("SPARKLE", BlendFunction.LIGHTNING);
   private static final int RINGS = 14;
   private static final int SEGMENTS = 22;
   private static final float[][] SPHERE = new float[345][];
   private final class_4598 immediate;
   private double camX;
   private double camY;
   private double camZ;
   private double rx;
   private double ry;
   private double rz;
   private double ux;
   private double uy;
   private double uz;

   private static class_1921 layer(String define, BlendFunction blend) {
      String name = define.toLowerCase();
      RenderPipeline pipeline = RenderPipeline.builder(new Snippet[]{class_10799.field_60125, class_10799.field_60126})
         .withLocation(class_2960.method_60655("violet", "pipeline/splash_" + name))
         .withVertexShader(class_2960.method_60655("violet", "core/splash_fx"))
         .withFragmentShader(class_2960.method_60655("violet", "core/splash_fx"))
         .withShaderDefine(define)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withBlend(blend)
         .withDepthWrite(false)
         .withCull(false)
         .build();
      return class_1921.method_75940("violet_splash_" + name, class_12247.method_75927(pipeline).method_75937().method_75938());
   }

   public SplashRenderer() {
      SequencedMap<class_1921, class_9799> buffers = new LinkedHashMap<>();
      buffers.put(PUDDLE, new class_9799(16384));
      buffers.put(RING, new class_9799(8192));
      buffers.put(DOME, new class_9799(65536));
      buffers.put(DROP, new class_9799(131072));
      buffers.put(BUBBLE, new class_9799(8192));
      buffers.put(MIST, new class_9799(8192));
      buffers.put(FLASH, new class_9799(4096));
      buffers.put(SPARKLE, new class_9799(16384));
      this.immediate = class_4597.method_22992(buffers, new class_9799(4096));
   }

   public void begin(class_243 camera, float pitch, float yaw) {
      this.camX = camera.field_1352;
      this.camY = camera.field_1351;
      this.camZ = camera.field_1350;
      class_243 forward = class_243.method_1030(pitch, yaw);
      double fx = forward.field_1352;
      double fy = forward.field_1351;
      double fz = forward.field_1350;
      this.rx = -fz;
      this.ry = 0.0;
      this.rz = fx;
      double length = Math.sqrt(this.rx * this.rx + this.rz * this.rz);
      if (length < 1.0E-6) {
         this.rx = 1.0;
         this.rz = 0.0;
         length = 1.0;
      }

      this.rx /= length;
      this.rz /= length;
      this.ux = this.ry * fz - this.rz * fy;
      this.uy = this.rz * fx - this.rx * fz;
      this.uz = this.rx * fy - this.ry * fx;
   }

   public void end() {
      this.immediate.method_22993();
   }

   public void billboard(
      class_1921 layer, double x, double y, double z, double vx, double vy, double vz, double size, double stretch, double rotation, int argb
   ) {
      if (argb >>> 24 != 0 && !(size <= 0.0)) {
         double px = x - this.camX;
         double py = y - this.camY;
         double pz = z - this.camZ;
         double distance = Math.sqrt(px * px + py * py + pz * pz);
         if (!(distance < 0.08)) {
            double wx = px / distance;
            double wy = py / distance;
            double wz = pz / distance;
            double dot = vx * wx + vy * wy + vz * wz;
            double ax = vx - dot * wx;
            double ay = vy - dot * wy;
            double az = vz - dot * wz;
            double speed = Math.sqrt(ax * ax + ay * ay + az * az);
            double scale = 1.0;
            if (stretch > 0.0 && speed > 5.0E-4) {
               ax /= speed;
               ay /= speed;
               az /= speed;
               scale = 1.0 + Math.min(speed * stretch, 1.8);
            } else {
               double up = this.ux * wx + this.uy * wy + this.uz * wz;
               ax = this.ux - up * wx;
               ay = this.uy - up * wy;
               az = this.uz - up * wz;
               double length = Math.sqrt(ax * ax + ay * ay + az * az);
               if (length < 1.0E-6) {
                  return;
               }

               ax /= length;
               ay /= length;
               az /= length;
               if (rotation != 0.0) {
                  double cx = ay * wz - az * wy;
                  double cy = az * wx - ax * wz;
                  double cz = ax * wy - ay * wx;
                  double cos = Math.cos(rotation);
                  double sin = Math.sin(rotation);
                  ax = ax * cos + cx * sin;
                  ay = ay * cos + cy * sin;
                  az = az * cos + cz * sin;
               }
            }

            double bx = ay * wz - az * wy;
            double by = az * wx - ax * wz;
            double bz = ax * wy - ay * wx;
            double bl = Math.sqrt(bx * bx + by * by + bz * bz);
            if (!(bl < 1.0E-6)) {
               bx /= bl;
               by /= bl;
               bz /= bl;
               float sxr = (float)(bx * this.rx + by * this.ry + bz * this.rz);
               float sxu = (float)(bx * this.ux + by * this.uy + bz * this.uz);
               float syr = (float)(ax * this.rx + ay * this.ry + az * this.rz);
               float syu = (float)(ax * this.ux + ay * this.uy + az * this.uz);
               double along = size * scale;
               class_4588 buffer = this.immediate.method_73477(layer);
               corner(buffer, px, py, pz, ax, ay, az, bx, by, bz, -1, -1, along, size, sxr, sxu, syr, syu, argb);
               corner(buffer, px, py, pz, ax, ay, az, bx, by, bz, 1, -1, along, size, sxr, sxu, syr, syu, argb);
               corner(buffer, px, py, pz, ax, ay, az, bx, by, bz, 1, 1, along, size, sxr, sxu, syr, syu, argb);
               corner(buffer, px, py, pz, ax, ay, az, bx, by, bz, -1, 1, along, size, sxr, sxu, syr, syu, argb);
            }
         }
      }
   }

   private static void corner(
      class_4588 buffer,
      double px,
      double py,
      double pz,
      double ax,
      double ay,
      double az,
      double bx,
      double by,
      double bz,
      int cx,
      int cy,
      double along,
      double across,
      float sxr,
      float sxu,
      float syr,
      float syu,
      int argb
   ) {
      buffer.method_22912(
            (float)(px + ax * (double)cy * along + bx * (double)cx * across),
            (float)(py + ay * (double)cy * along + by * (double)cx * across),
            (float)(pz + az * (double)cy * along + bz * (double)cx * across)
         )
         .method_22913((float)cx * sxr + (float)cy * syr, (float)cx * sxu + (float)cy * syu)
         .method_39415(argb);
   }

   public void ground(class_1921 layer, double x, double y, double z, double half, double rotation, int argb) {
      if (argb >>> 24 != 0 && !(half <= 0.0)) {
         class_4588 buffer = this.immediate.method_73477(layer);
         double cos = Math.cos(rotation) * half;
         double sin = Math.sin(rotation) * half;
         float py = (float)(y - this.camY);
         int[][] corners = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

         for (int[] c : corners) {
            double ox = (double)c[0] * cos - (double)c[1] * sin;
            double oz = (double)c[0] * sin + (double)c[1] * cos;
            buffer.method_22912((float)(x + ox - this.camX), py, (float)(z + oz - this.camZ)).method_22913((float)c[0], (float)c[1]).method_39415(argb);
         }
      }
   }

   public void dome(double x, double y, double z, double radius, double age, double fade, int rgb) {
      if (!(fade <= 0.0) && !(radius <= 0.0)) {
         class_4588 buffer = this.immediate.method_73477(DOME);
         float br = (float)(rgb >> 16 & 0xFF) / 255.0F;
         float bg = (float)(rgb >> 8 & 0xFF) / 255.0F;
         float bb = (float)(rgb & 0xFF) / 255.0F;
         int[] colors = new int[SPHERE.length];
         float[][] points = new float[SPHERE.length][];
         double lx = -0.4;
         double ly = 0.8;
         double lz = 0.4;
         double ll = Math.sqrt(lx * lx + ly * ly + lz * lz);
         lx /= ll;
         ly /= ll;
         lz /= ll;

         for (int i = 0; i < SPHERE.length; i++) {
            float[] n = SPHERE[i];
            double wobble = 1.0
               + 0.1 * Math.sin((double)(n[0] * 9.0F) + age * 1.3) * Math.sin((double)(n[1] * 7.0F) - age)
               + 0.06 * Math.sin((double)(n[2] * 11.0F) + age * 2.0);
            double px = x + (double)n[0] * radius * wobble;
            double py = y + (double)n[1] * radius * wobble;
            double pz = z + (double)n[2] * radius * wobble;
            double vx = this.camX - px;
            double vy = this.camY - py;
            double vz = this.camZ - pz;
            double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
            vx /= vl;
            vy /= vl;
            vz /= vl;
            double ndv = Math.abs((double)n[0] * vx + (double)n[1] * vy + (double)n[2] * vz);
            double rim = Math.pow(1.0 - ndv, 2.2);
            double hx = lx + vx;
            double hy = ly + vy;
            double hz = lz + vz;
            double hl = Math.sqrt(hx * hx + hy * hy + hz * hz);
            double spec = Math.pow(Math.max(0.0, ((double)n[0] * hx + (double)n[1] * hy + (double)n[2] * hz) / hl), 40.0);
            double mix = rim * 0.85 + 0.15;
            double r = Math.min(1.0, (double)br + (double)(1.0F - br) * mix + spec);
            double g = Math.min(1.0, (double)bg + (double)(1.0F - bg) * mix + spec);
            double b = Math.min(1.0, (double)bb + (double)(1.0F - bb) * mix + spec);
            double a = Math.min(1.0, (0.08 + 0.85 * rim + spec) * fade);
            colors[i] = (int)(a * 255.0) << 24 | (int)(r * 255.0) << 16 | (int)(g * 255.0) << 8 | (int)(b * 255.0);
            points[i] = new float[]{(float)(px - this.camX), (float)(py - this.camY), (float)(pz - this.camZ)};
         }

         for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 22; j++) {
               int a = i * 23 + j;
               int b = a + 1;
               int c = a + 22 + 2;
               int d = a + 22 + 1;
               buffer.method_22912(points[a][0], points[a][1], points[a][2]).method_39415(colors[a]);
               buffer.method_22912(points[b][0], points[b][1], points[b][2]).method_39415(colors[b]);
               buffer.method_22912(points[c][0], points[c][1], points[c][2]).method_39415(colors[c]);
               buffer.method_22912(points[d][0], points[d][1], points[d][2]).method_39415(colors[d]);
            }
         }
      }
   }

   static {
      for (int i = 0; i <= 14; i++) {
         double theta = 3.141592653589793 * (double)i / 14.0;

         for (int j = 0; j <= 22; j++) {
            double phi = 6.283185307179586 * (double)j / 22.0;
            SPHERE[i * 23 + j] = new float[]{(float)(Math.sin(theta) * Math.cos(phi)), (float)Math.cos(theta), (float)(Math.sin(theta) * Math.sin(phi))};
         }
      }
   }
}
