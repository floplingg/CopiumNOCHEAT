package dev.violet.gui;

import java.util.Random;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

final class OrbitBackdrop {
   static final OrbitBackdrop START = new OrbitBackdrop("orbit_backdrop", 0.79F, 0.68F, 0.34F, 0.32F, 0.45F, true);
   static final OrbitBackdrop CORNER = new OrbitBackdrop("orbit_backdrop_corner", 0.965F, -0.02F, 0.17F, 0.5F, 0.5F, false);
   static final float RING_RX = 1.95F;
   static final float RING_RY = 0.52F;
   static final float RING_TILT = -22.0F;
   private static final float RING_ALPHA = 0.42F;
   private static final float[][] STARS = stars(90);
   final float planetX;
   final float planetY;
   final float planetR;
   private final class_2960 id;
   private final float glowX;
   private final float glowY;
   private final boolean moon;
   private final long createdAt = System.nanoTime();
   private int builtW;
   private int builtH;

   private OrbitBackdrop(String name, float planetX, float planetY, float planetR, float glowX, float glowY, boolean moon) {
      this.id = class_2960.method_60655("violet", "dynamic/" + name);
      this.planetX = planetX;
      this.planetY = planetY;
      this.planetR = planetR;
      this.glowX = glowX;
      this.glowY = glowY;
      this.moon = moon;
   }

   void draw(class_332 context, float opacity) {
      if (!(opacity <= 0.002F)) {
         class_1041 window = class_310.method_1551().method_22683();
         int w = Math.max(1, window.method_4489());
         int h = Math.max(1, window.method_4506());
         if (w != this.builtW || h != this.builtH) {
            this.build(w, h);
         }

         float ratio = (float)h / (float)Math.max(1, window.method_4502());
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.identity();
         matrices.scale(1.0F / ratio, 1.0F / ratio);
         context.method_25293(class_10799.field_56883, this.id, 0, 0, 0.0F, 0.0F, w, h, w, h, w, h, MenuKit.alpha(-1, opacity));
         matrices.popMatrix();
      }
   }

   void sky(class_332 context, int width, int height, int mouseX, int mouseY, float fade) {
      float time = (float)(System.nanoTime() - this.createdAt) / 1.0E9F;
      float pcx = (float)width * this.planetX;
      float pcy = (float)height * this.planetY;
      float pr = (float)height * this.planetR;
      float mx = ((float)mouseX - (float)width / 2.0F) / Math.max(1.0F, (float)width);
      float my = ((float)mouseY - (float)height / 2.0F) / Math.max(1.0F, (float)height);

      for (float[] star : STARS) {
         float depth = star[2];
         float x = star[0] * (float)width - mx * 6.0F * depth;
         float y = star[1] * (float)height - my * 6.0F * depth;
         float dx = x - pcx;
         float dy = y - pcy;
         if (!(dx * dx + dy * dy < (pr + 3.0F) * (pr + 3.0F))) {
            float twinkle = 0.45F + 0.55F * (0.5F + 0.5F * (float)Math.sin((double)(time * star[4] + star[3])));
            int alpha = Math.round(255.0F * twinkle * (0.35F + 0.4F * depth));
            Smooth.softDot(context, x, y, depth * 1.6F, MenuKit.alpha(alpha << 24 | 15328255, fade));
         }
      }

      if (this.moon) {
         double tilt = Math.toRadians(-22.0);
         double a = (double)time * 0.16;
         float ox = (float)Math.cos(a) * pr * 2.35F;
         float oy = (float)Math.sin(a) * pr * 0.62F;
         float moonX = pcx + ox * (float)Math.cos(tilt) - oy * (float)Math.sin(tilt);
         float moonY = pcy + ox * (float)Math.sin(tilt) + oy * (float)Math.cos(tilt);
         float mdx = moonX - pcx;
         float mdy = moonY - pcy;
         if (!(Math.sin(a) < 0.0) || !(mdx * mdx + mdy * mdy < pr * pr)) {
            float r = Math.max(2.5F, pr * 0.04F);
            Smooth.softDot(context, moonX, moonY, r * 3.2F, MenuKit.alpha(1084722170, fade));
            Smooth.circle(context, Math.round(moonX), Math.round(moonY), Math.round(r), MenuKit.alpha(-1448961, fade));
         }
      }
   }

   private static float[][] stars(int count) {
      Random random = new Random(7L);
      float[][] list = new float[count][];

      for (int i = 0; i < count; i++) {
         float size = 0.35F + (float)Math.pow((double)random.nextFloat(), 3.0) * 1.1F;
         list[i] = new float[]{random.nextFloat(), random.nextFloat(), size, random.nextFloat() * 6.283F, 0.6F + random.nextFloat() * 1.6F};
      }

      return list;
   }

   private void build(int w, int h) {
      class_1011 image = new class_1011(w, h, false);
      float gx = (float)w * this.glowX;
      float gy = (float)h * this.glowY;
      float sx = 1.0F / ((float)w * 0.75F);
      float sy = 1.0F / ((float)h * 0.9F);
      float pcx = (float)w * this.planetX;
      float pcy = (float)h * this.planetY;
      float pr = (float)h * this.planetR;
      float rx = pr * 1.95F;
      float ry = pr * 0.52F;
      float thick = pr * 0.035F;
      float gap = pr * 0.03F;
      double tilt = Math.toRadians(-22.0);
      float cos = (float)Math.cos(tilt);
      float sin = (float)Math.sin(tilt);
      float[] light = normalize(-0.55F, -0.6F, 0.58F);
      int seed = -1640531527;
      float[] bg = new float[3];
      float[] c = new float[3];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int var51 = seed ^ seed << 13;
            int var52 = var51 ^ var51 >>> 17;
            seed = var52 ^ var52 << 5;
            float noise = (float)((seed & 65535) + (seed >>> 16 & 65535)) / 65535.0F - 1.0F;
            float dx = ((float)x - gx) * sx;
            float dy = ((float)y - gy) * sy;
            float d = Math.min(1.0F, (float)Math.sqrt((double)(dx * dx + dy * dy)));
            float t = d * d * (3.0F - 2.0F * d);
            bg[0] = 38.0F - 25.0F * t;
            bg[1] = 27.0F - 17.0F * t;
            bg[2] = 66.0F - 41.0F * t;
            c[0] = bg[0];
            c[1] = bg[1];
            c[2] = bg[2];
            float px = (float)x + 0.5F - pcx;
            float py = (float)y + 0.5F - pcy;
            float dist = (float)Math.sqrt((double)(px * px + py * py));
            float planet = clamp01(0.5F - (dist - pr));
            float u = px * cos + py * sin;
            float v = -px * sin + py * cos;
            float k = (float)Math.sqrt((double)(u * u / (rx * rx) + v * v / (ry * ry)));
            float grad = (float)Math.sqrt((double)(u * u / (rx * rx * rx * rx) + v * v / (ry * ry * ry * ry))) / Math.max(k, 1.0E-4F);
            float ringDist = Math.abs((k - 1.0F) / Math.max(grad, 1.0E-6F));
            float ring = clamp01(0.5F - (ringDist - thick / 2.0F));
            float front = clamp01(0.5F + v);
            mix(c, 237.0F, 231.0F, 255.0F, ring * 0.42F);
            if (planet > 0.0F) {
               float nx = px / pr;
               float ny = py / pr;
               float nz = (float)Math.sqrt((double)Math.max(0.0F, 1.0F - nx * nx - ny * ny));
               float lambert = Math.max(0.0F, nx * light[0] + ny * light[1] + nz * light[2]);
               float shade = 0.1F + 0.75F * (float)Math.pow((double)lambert, 1.2000000476837158);
               float r = 46.0F + 82.0F * shade;
               float g = 34.0F + 74.0F * shade;
               float b = 86.0F + 110.0F * shade;
               float rim = clamp01(1.0F - (pr - dist) / (pr * 0.04F)) * lambert;
               r += (237.0F - r) * rim * 0.5F;
               g += (231.0F - g) * rim * 0.5F;
               b += (255.0F - b) * rim * 0.5F;
               mix(c, r, g, b, planet);
               float gapCover = clamp01(0.5F - (ringDist - thick / 2.0F - gap)) * front * planet;
               mix(c, bg[0], bg[1], bg[2], gapCover);
               mix(c, 237.0F, 231.0F, 255.0F, ring * 0.42F * front * planet);
            }

            int r = clamp(Math.round(c[0] + noise));
            int g = clamp(Math.round(c[1] + noise));
            int b = clamp(Math.round(c[2] + noise));
            image.method_61941(x, y, 0xFF000000 | r << 16 | g << 8 | b);
         }
      }

      class_310.method_1551().method_1531().method_4615(this.id);
      Smooth.register(this.id, image);
      this.builtW = w;
      this.builtH = h;
   }

   private static void mix(float[] c, float r, float g, float b, float amount) {
      if (!(amount <= 0.0F)) {
         c[0] += (r - c[0]) * amount;
         c[1] += (g - c[1]) * amount;
         c[2] += (b - c[2]) * amount;
      }
   }

   private static float[] normalize(float x, float y, float z) {
      float l = (float)Math.sqrt((double)(x * x + y * y + z * z));
      return new float[]{x / l, y / l, z / l};
   }

   private static float clamp01(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static int clamp(int v) {
      return Math.max(0, Math.min(255, v));
   }
}
