package dev.violet.gui;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public final class CrosshairRenderer {
   private static final Map<String, CrosshairRenderer.Mask> CACHE = new LinkedHashMap<>();
   private static final int SUPER = 4;
   private static int counter;

   private CrosshairRenderer() {
   }

   public static void draw(
      class_332 context, CrosshairRenderer.Shape shape, int size, int thickness, int gap, boolean outline, boolean glow, int color, float opacity
   ) {
      class_310 mc = class_310.method_1551();
      int pixelsWide = mc.method_22683().method_4489();
      int pixelsHigh = mc.method_22683().method_4506();
      float scaleX = (float)pixelsWide / (float)Math.max(1, context.method_51421());
      float scaleY = (float)pixelsHigh / (float)Math.max(1, context.method_51443());
      String key = shape + "|" + size + "|" + thickness + "|" + gap;
      CrosshairRenderer.Mask mask = CACHE.get(key);
      if (mask == null) {
         mask = create(shape, size, thickness, gap);
         if (mask == null) {
            return;
         }

         CACHE.put(key, mask);
         if (CACHE.size() > 64) {
            Iterator<Entry<String, CrosshairRenderer.Mask>> oldest = CACHE.entrySet().iterator();
            CrosshairRenderer.Mask old = oldest.next().getValue();
            oldest.remove();
            mc.method_1531().method_4615(old.fill());
            mc.method_1531().method_4615(old.outline());
            mc.method_1531().method_4615(old.glow());
         }
      }

      int n = mask.size();
      int x = pixelsWide / 2 - n / 2;
      int y = pixelsHigh / 2 - n / 2;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.identity();
      matrices.scale(1.0F / scaleX, 1.0F / scaleY);
      int alpha = Math.round(Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F);
      int tinted = Math.round((float)(color >>> 24) / 255.0F * (float)alpha) << 24 | color & 16777215;
      if (glow) {
         context.method_25293(
            class_10799.field_56883, mask.glow(), x, y, 0.0F, 0.0F, n, n, n, n, n, n, Math.round((float)alpha * 0.55F) << 24 | color & 16777215
         );
      }

      if (outline) {
         context.method_25293(class_10799.field_56883, mask.outline(), x, y, 0.0F, 0.0F, n, n, n, n, n, n, Math.round((float)alpha * 0.8F) << 24);
      }

      context.method_25293(class_10799.field_56883, mask.fill(), x, y, 0.0F, 0.0F, n, n, n, n, n, n, tinted);
      matrices.popMatrix();
   }

   public static void bar(class_332 context, int offset, int width, int height, float progress, int color, boolean outline, float opacity, float halo) {
      class_310 mc = class_310.method_1551();
      int pixelsWide = mc.method_22683().method_4489();
      int pixelsHigh = mc.method_22683().method_4506();
      float scaleX = (float)pixelsWide / (float)Math.max(1, context.method_51421());
      float scaleY = (float)pixelsHigh / (float)Math.max(1, context.method_51443());
      float alpha = Math.max(0.0F, Math.min(1.0F, opacity));
      if (!(alpha <= 0.003F)) {
         int x = pixelsWide / 2 - width / 2;
         int y = pixelsHigh / 2 + offset;
         int radius = Math.max(1, height / 2 + height % 2);
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.identity();
         matrices.scale(1.0F / scaleX, 1.0F / scaleY);
         if (halo > 0.01F) {
            Smooth.rect(context, x - 2, y - 2, width + 4, height + 4, radius + 2, tint(color, 0.22F * halo * alpha));
         }

         if (outline) {
            Smooth.rect(context, x - 1, y - 1, width + 2, height + 2, radius + 1, tint(-16777216, 0.5F * alpha));
         }

         Smooth.rect(context, x, y, width, height, radius, tint(-16777216, 0.42F * alpha));
         float filled = Math.max(0.0F, Math.min(1.0F, progress)) * (float)width;
         int full = (int)filled;
         if (full >= radius * 2) {
            Smooth.rect(context, x, y, full, height, radius, tint(color, alpha));
         } else if (filled > 0.05F) {
            Smooth.rect(context, x, y, radius * 2, height, radius, tint(color, alpha * filled / ((float)radius * 2.0F)));
         }

         matrices.popMatrix();
      }
   }

   private static int tint(int color, float alpha) {
      int a = Math.round(Math.max(0.0F, Math.min(1.0F, (float)(color >>> 24) / 255.0F * alpha)) * 255.0F);
      return a << 24 | color & 16777215;
   }

   private static CrosshairRenderer.Mask create(CrosshairRenderer.Shape shape, int size, int thickness, int gap) {
      float extent = switch (shape) {
         case PALM -> (float)size * 1.9F + (float)thickness + 3.0F;
         case DOT -> 0.6F + (float)size * 0.22F + (float)thickness * 0.5F;
         case CROSS, Y -> (float)(gap + size + thickness);
         case RING, WAVE -> (float)size * 0.8F + (float)gap * 0.5F + (float)size * 0.12F + (float)thickness;
         case SUN -> (float)thickness * 1.2F + 1.5F + (float)gap + (float)size * 0.6F + (float)thickness;
         case BRACKETS -> (float)gap + (float)size * 0.7F + (float)thickness;
         case HIT -> (float)size * 0.95F + (float)thickness;
         case ARC -> (float)(size + thickness);
      };
      int pad = 7;
      int n = 2 * ((int)Math.ceil((double)extent) + pad) + 1;
      float[] fill = new float[n * n];
      float center = (float)n / 2.0F;

      for (int py = 0; py < n; py++) {
         for (int px = 0; px < n; px++) {
            int inside = 0;

            for (int sy = 0; sy < 4; sy++) {
               for (int sx = 0; sx < 4; sx++) {
                  float x = (float)px + ((float)sx + 0.5F) / 4.0F - center;
                  float y = (float)py + ((float)sy + 0.5F) / 4.0F - center;
                  if (distance(shape, x, y, size, thickness, gap) <= 0.0F) {
                     inside++;
                  }
               }
            }

            fill[py * n + px] = (float)inside / 16.0F;
         }
      }

      float[] outline = dilate(fill, n, 1.25F);
      float[] glow = blur(fill, n, 3);
      int id = counter++;
      return new CrosshairRenderer.Mask(
         register("crosshair_fill_" + id, fill, n, 1.0F),
         register("crosshair_outline_" + id, outline, n, 1.0F),
         register("crosshair_glow_" + id, glow, n, 1.8F),
         n
      );
   }

   private static class_2960 register(String name, float[] coverage, int n, float boost) {
      class_1011 image = new class_1011(n, n, false);

      for (int i = 0; i < n * n; i++) {
         int a = Math.round(Math.max(0.0F, Math.min(1.0F, coverage[i] * boost)) * 255.0F);
         image.method_61941(i % n, i / n, a << 24 | 16777215);
      }

      class_2960 id = class_2960.method_60655("violet", "dynamic/" + name);
      Smooth.register(id, image);
      return id;
   }

   private static float capsule(float x, float y, float ax, float ay, float bx, float by, float radius) {
      float dx = bx - ax;
      float dy = by - ay;
      float t = Math.max(0.0F, Math.min(1.0F, ((x - ax) * dx + (y - ay) * dy) / Math.max(1.0E-6F, dx * dx + dy * dy)));
      float qx = ax + dx * t - x;
      float qy = ay + dy * t - y;
      return (float)Math.sqrt((double)(qx * qx + qy * qy)) - radius;
   }

   private static float ray(float x, float y, double angle, float from, float to, float radius) {
      float c = (float)Math.cos(angle);
      float s = (float)Math.sin(angle);
      return capsule(x, y, c * from, s * from, c * to, s * to, radius);
   }

   private static float distance(CrosshairRenderer.Shape shape, float x, float y, int size, int thickness, int gap) {
      float r = (float)Math.sqrt((double)(x * x + y * y));
      float half = (float)thickness / 2.0F;
      float dot = Math.max(0.9F, (float)thickness * 0.75F);
      switch (shape) {
         case PALM: {
            float d = r - dot;
            float start = dot + 1.5F;
            float length = (float)size * 1.15F;
            double[] angles = new double[]{-165.0, -125.0, -55.0, -15.0, -90.0};
            float[] droops = new float[]{0.55F, 0.35F, 0.35F, 0.55F, 0.08F};
            float[] lengths = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 0.7F};

            for (int i = 0; i < angles.length; i++) {
               double a = Math.toRadians(angles[i]);
               float dx = (float)Math.cos(a);
               float dy = (float)Math.sin(a);
               float len = length * lengths[i];
               float px = dx * start;
               float py = dy * start;

               for (int k = 1; k <= 4; k++) {
                  float s = (float)k / 4.0F;
                  float qx = dx * (start + len * s);
                  float qy = dy * (start + len * s) + droops[i] * len * s * s;
                  d = Math.min(d, capsule(x, y, px, py, qx, qy, half * (1.15F - 0.45F * s)));
                  px = qx;
                  py = qy;
               }
            }

            float tx1 = (float)size * 0.12F;
            float ty1 = start + (float)size * 0.7F;
            float tx2 = (float)size * 0.2F;
            float ty2 = start + (float)size * 1.4F;
            d = Math.min(d, capsule(x, y, 0.0F, start, tx1, ty1, half * 1.1F));
            return Math.min(d, capsule(x, y, tx1, ty1, tx2, ty2, half * 1.25F));
         }
         case DOT: {
            return r - (0.6F + (float)size * 0.22F + (float)thickness * 0.5F);
         }
         case CROSS: {
            float d = 3.4028235E38F;

            for (int i = 0; i < 4; i++) {
               d = Math.min(d, ray(x, y, (double)i * 3.141592653589793 / 2.0, (float)gap + half, (float)(gap + size), half));
            }

            return d;
         }
         case RING: {
            float radius = (float)size * 0.8F + (float)gap * 0.5F;
            return Math.min(Math.abs(r - radius) - half, r - dot);
         }
         case SUN: {
            float core = (float)thickness * 1.2F + 1.0F;
            float d = r - core;
            float start = core + 1.5F + (float)gap;
            float end = start + (float)size * 0.6F;

            for (int i = 0; i < 8; i++) {
               d = Math.min(d, ray(x, y, (double)i * 3.141592653589793 / 4.0 - 1.5707963267948966, start, end, half * 0.9F));
            }

            return d;
         }
         case WAVE: {
            float radius = (float)size * 0.8F + (float)gap * 0.5F;
            double theta = Math.atan2((double)y, (double)x);
            float wave = radius + Math.max(0.8F, (float)size * 0.12F) * (float)Math.sin(theta * 8.0);
            return Math.min(Math.abs(r - wave) - half, r - dot);
         }
         case Y: {
            float d = r - dot;
            double[] angles = new double[]{1.5707963267948966, Math.toRadians(210.0), Math.toRadians(330.0)};

            for (double a : angles) {
               d = Math.min(d, ray(x, y, a, (float)gap + half + 1.0F, (float)(gap + size), half));
            }

            return d;
         }
         case BRACKETS: {
            float corner = (float)gap + (float)size * 0.7F;
            float arm = Math.max(2.0F, (float)size * 0.45F);
            float d = r - dot;

            for (int sx = -1; sx <= 1; sx += 2) {
               for (int sy = -1; sy <= 1; sy += 2) {
                  float cx = (float)sx * corner;
                  float cy = (float)sy * corner;
                  d = Math.min(d, capsule(x, y, cx, cy, cx - (float)sx * arm, cy, half));
                  d = Math.min(d, capsule(x, y, cx, cy, cx, cy - (float)sy * arm, half));
               }
            }

            return d;
         }
         case HIT: {
            float d = 3.4028235E38F;

            for (int i = 0; i < 4; i++) {
               d = Math.min(d, ray(x, y, 0.7853981633974483 + (double)i * 3.141592653589793 / 2.0, (float)size * 0.35F, (float)size * 0.95F, half));
            }

            return d;
         }
         case ARC: {
            double theta = Math.atan2((double)x, (double)(-y));
            if (theta < 0.0) {
               theta += 6.283185307179586;
            }

            if (theta > (double)gap / 24.0 * 3.141592653589793 * 2.0) {
               return 3.4028235E38F;
            }

            return Math.abs(r - (float)size) - half;
         }
         default: {
            return r - dot;
         }
      }
   }

   private static float[] dilate(float[] source, int n, float radius) {
      float[] out = new float[n * n];
      int r = (int)Math.ceil((double)radius);

      for (int y = 0; y < n; y++) {
         for (int x = 0; x < n; x++) {
            float best = 0.0F;

            for (int dy = -r; dy <= r; dy++) {
               for (int dx = -r; dx <= r; dx++) {
                  int nx = x + dx;
                  int ny = y + dy;
                  if (nx >= 0 && ny >= 0 && nx < n && ny < n) {
                     float distance = (float)Math.sqrt((double)(dx * dx + dy * dy));
                     float weight = Math.max(0.0F, Math.min(1.0F, radius + 0.5F - distance));
                     best = Math.max(best, source[ny * n + nx] * weight);
                  }
               }
            }

            out[y * n + x] = best;
         }
      }

      return out;
   }

   private static float[] blur(float[] source, int n, int radius) {
      float[] a = (float[])source.clone();

      for (int pass = 0; pass < 2; pass++) {
         float[] h = new float[n * n];

         for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
               float sum = 0.0F;

               for (int k = -radius; k <= radius; k++) {
                  int xx = Math.max(0, Math.min(n - 1, x + k));
                  sum += a[y * n + xx];
               }

               h[y * n + x] = sum / (float)(2 * radius + 1);
            }
         }

         float[] v = new float[n * n];

         for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
               float sum = 0.0F;

               for (int k = -radius; k <= radius; k++) {
                  int yy = Math.max(0, Math.min(n - 1, y + k));
                  sum += h[yy * n + x];
               }

               v[y * n + x] = sum / (float)(2 * radius + 1);
            }
         }

         a = v;
      }

      return a;
   }

   private static record Mask(class_2960 fill, class_2960 outline, class_2960 glow, int size) {
   }

   public static enum Shape {
      PALM,
      DOT,
      CROSS,
      RING,
      SUN,
      WAVE,
      Y,
      BRACKETS,
      HIT,
      ARC;
   }
}
