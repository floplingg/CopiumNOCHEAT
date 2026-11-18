package dev.violet.gui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class Icons {
   private static final int SAMPLES = 5;
   private static final float UNITS = 12.0F;
   private static final Map<Integer, class_2960> CACHE = new HashMap<>();

   private Icons() {
   }

   public static void draw(class_332 context, Icons.Icon icon, int x, int y, int size, int color) {
      if (color >>> 24 != 0 && size > 0) {
         int pixels = Math.max(8, Math.min(256, (int)Math.ceil((double)((float)size * Smooth.pixelScale()))));
         class_2960 id = CACHE.computeIfAbsent(icon.ordinal() * 1000 + pixels, key -> build(icon, pixels));
         context.method_25293(class_10799.field_56883, id, x, y, 0.0F, 0.0F, size, size, pixels, pixels, pixels, pixels, color);
      }
   }

   private static class_2960 build(Icons.Icon icon, int pixels) {
      class_1011 image = new class_1011(pixels, pixels, false);
      float unit = 12.0F / (float)pixels;

      for (int px = 0; px < pixels; px++) {
         for (int py = 0; py < pixels; py++) {
            int hits = 0;

            for (int sx = 0; sx < 5; sx++) {
               for (int sy = 0; sy < 5; sy++) {
                  float u = ((float)px + ((float)sx + 0.5F) / 5.0F) * unit;
                  float v = ((float)py + ((float)sy + 0.5F) / 5.0F) * unit;
                  if (distance(icon, u, v) <= 0.0F) {
                     hits++;
                  }
               }
            }

            image.method_61941(px, py, Math.round((float)hits * 255.0F / 25.0F) << 24 | 16777215);
         }
      }

      class_2960 id = class_2960.method_60655("violet", "dynamic/icon_" + icon.name().toLowerCase() + "_" + pixels);
      Smooth.register(id, image);
      return id;
   }

   private static float length(float x, float y) {
      return (float)Math.sqrt((double)(x * x + y * y));
   }

   private static float circle(float u, float v, float cx, float cy, float r) {
      return length(u - cx, v - cy) - r;
   }

   private static float ring(float u, float v, float cx, float cy, float r, float width) {
      return Math.abs(length(u - cx, v - cy) - r) - width / 2.0F;
   }

   private static float segment(float u, float v, float ax, float ay, float bx, float by, float width) {
      float ex = bx - ax;
      float ey = by - ay;
      float wx = u - ax;
      float wy = v - ay;
      float t = Math.max(0.0F, Math.min(1.0F, (wx * ex + wy * ey) / (ex * ex + ey * ey)));
      return length(wx - ex * t, wy - ey * t) - width / 2.0F;
   }

   private static float box(float u, float v, float x1, float y1, float x2, float y2, float r) {
      float cx = (x1 + x2) / 2.0F;
      float cy = (y1 + y2) / 2.0F;
      float hw = (x2 - x1) / 2.0F;
      float hh = (y2 - y1) / 2.0F;
      float qx = Math.abs(u - cx) - hw + r;
      float qy = Math.abs(v - cy) - hh + r;
      return length(Math.max(qx, 0.0F), Math.max(qy, 0.0F)) + Math.min(Math.max(qx, qy), 0.0F) - r;
   }

   private static float polyline(float u, float v, float width, float... points) {
      float best = 3.4028235E38F;

      for (int i = 0; i + 3 < points.length; i += 2) {
         best = Math.min(best, segment(u, v, points[i], points[i + 1], points[i + 2], points[i + 3], width));
      }

      return best;
   }

   private static float distance(Icons.Icon icon, float u, float v) {
      return switch (icon) {
         case SEARCH -> Math.min(ring(u, v, 5.1F, 5.1F, 3.5F, 1.5F), segment(u, v, 7.8F, 7.8F, 10.7F, 10.7F, 1.8F));
         case DONUT -> ring(u, v, 6.0F, 6.0F, 3.7F, 2.6F);
         case EYE -> {
            float lens = Math.max(circle(u, v, 6.0F, 10.6F, 7.4F), circle(u, v, 6.0F, 1.4F, 7.4F));
            yield Math.min(Math.abs(lens) - 0.65F, circle(u, v, 6.0F, 6.0F, 1.75F));
         }
         case SPARKLE -> {
            float dx = Math.abs(u - 5.4F);
            float dy = Math.abs(v - 6.4F);
            float big = (float)(Math.sqrt((double)dx) + Math.sqrt((double)dy) - Math.sqrt(5.2));
            float sx = Math.abs(u - 10.0F);
            float sy = Math.abs(v - 2.2F);
            float small = (float)(Math.sqrt((double)sx) + Math.sqrt((double)sy) - Math.sqrt(1.9));
            yield Math.min(big, small);
         }
         case CUBE -> Math.min(
         polyline(u, v, 1.25F, 6.0F, 1.0F, 10.6F, 3.6F, 10.6F, 8.4F, 6.0F, 11.0F, 1.4F, 8.4F, 1.4F, 3.6F, 6.0F, 1.0F),
         Math.min(
            segment(u, v, 1.4F, 3.6F, 6.0F, 6.2F, 1.25F),
            Math.min(segment(u, v, 10.6F, 3.6F, 6.0F, 6.2F, 1.25F), segment(u, v, 6.0F, 6.2F, 6.0F, 11.0F, 1.25F))
         )
      );
         case CHEST -> Math.min(
         Math.abs(box(u, v, 1.2F, 2.4F, 10.8F, 10.4F, 1.8F)) - 0.65F,
         Math.min(segment(u, v, 1.4F, 5.6F, 10.6F, 5.6F, 1.2F), box(u, v, 4.9F, 4.4F, 7.1F, 7.6F, 0.7F))
      );
         case PICKAXE -> {
            float handle = segment(u, v, 1.6F, 10.6F, 8.2F, 4.0F, 1.6F);
            float cx = 2.5F;
            float cy = 9.5F;
            float radius = 8.25F;
            double angle = Math.atan2((double)(v - cy), (double)(u - cx));
            double a0 = Math.atan2(-8.0, 2.0);
            double a1 = Math.atan2(-2.0, 8.0);
            float head;
            if (angle >= a0 && angle <= a1) {
               float t = (float)((angle - a0) / (a1 - a0));
               float middle = 1.0F - Math.abs(t - 0.5F) * 2.0F;
               head = Math.abs(length(u - cx, v - cy) - radius) - (0.35F + 1.05F * (float)Math.pow((double)middle, 0.6));
            } else {
               head = Math.min(
                  circle(u, v, cx + radius * (float)Math.cos(a0), cy + radius * (float)Math.sin(a0), 0.35F),
                  circle(u, v, cx + radius * (float)Math.cos(a1), cy + radius * (float)Math.sin(a1), 0.35F)
               );
            }

            yield Math.min(handle, head);
         }
         case PULSE -> polyline(u, v, 1.35F, 0.9F, 6.6F, 3.2F, 6.6F, 4.6F, 2.6F, 6.8F, 10.2F, 8.4F, 4.8F, 9.3F, 6.6F, 11.1F, 6.6F);
         case COLUMNS -> Math.min(
         box(u, v, 1.0F, 1.6F, 3.6F, 10.4F, 0.9F), Math.min(box(u, v, 4.7F, 1.6F, 7.3F, 10.4F, 0.9F), box(u, v, 8.4F, 1.6F, 11.0F, 10.4F, 0.9F))
      );
         case WINDOW -> Math.min(Math.abs(box(u, v, 1.2F, 1.8F, 10.8F, 10.2F, 1.8F)) - 0.6F, segment(u, v, 4.5F, 2.0F, 4.5F, 10.0F, 1.2F));
         case GRID -> Math.min(
         Math.min(box(u, v, 1.0F, 1.0F, 5.4F, 5.4F, 1.3F), box(u, v, 6.6F, 1.0F, 11.0F, 5.4F, 1.3F)),
         Math.min(box(u, v, 1.0F, 6.6F, 5.4F, 11.0F, 1.3F), box(u, v, 6.6F, 6.6F, 11.0F, 11.0F, 1.3F))
      );
         case CHEVRONS -> Math.min(polyline(u, v, 1.6F, 2.0F, 6.4F, 6.0F, 2.4F, 10.0F, 6.4F), polyline(u, v, 1.6F, 2.0F, 10.4F, 6.0F, 6.4F, 10.0F, 10.4F));
         case CLOSE -> Math.min(segment(u, v, 2.6F, 2.6F, 9.4F, 9.4F, 1.6F), segment(u, v, 9.4F, 2.6F, 2.6F, 9.4F, 1.6F));
         case MOON -> Math.max(circle(u, v, 5.6F, 6.4F, 4.6F), -circle(u, v, 8.6F, 4.0F, 3.9F));
         case CROWN -> polyline(u, v, 1.3F, 1.4F, 9.6F, 1.4F, 3.6F, 4.0F, 6.4F, 6.0F, 2.4F, 8.0F, 6.4F, 10.6F, 3.6F, 10.6F, 9.6F, 1.4F, 9.6F);
         case SPEED -> Math.min(
         segment(u, v, 1.5F, 3.4F, 8.0F, 3.4F, 1.4F), Math.min(segment(u, v, 3.6F, 6.0F, 10.5F, 6.0F, 1.4F), segment(u, v, 1.5F, 8.6F, 7.0F, 8.6F, 1.4F))
      );
         case CAMERA -> Math.min(
         Math.abs(box(u, v, 1.2F, 3.4F, 10.8F, 10.2F, 1.6F)) - 0.6F, Math.min(ring(u, v, 6.0F, 6.8F, 1.9F, 1.2F), segment(u, v, 4.0F, 2.2F, 7.4F, 2.2F, 1.3F))
      );
         case RINGS -> {
            float outer = (length((u - 6.0F) / 5.0F, (v - 6.6F) / 2.4F) - 1.0F) * 2.4F;
            float inner = (length((u - 6.0F) / 2.5F, (v - 6.6F) / 1.1F) - 1.0F) * 1.1F;
            yield Math.min(Math.abs(outer) - 0.6F, Math.abs(inner) - 0.55F);
         }
         case TAG -> Math.min(polyline(u, v, 1.3F, 1.4F, 3.4F, 7.4F, 3.4F, 10.6F, 6.0F, 7.4F, 8.6F, 1.4F, 8.6F, 1.4F, 3.4F), circle(u, v, 7.1F, 6.0F, 0.9F));
         case CROSSHAIR -> Math.min(
         ring(u, v, 6.0F, 6.0F, 3.3F, 1.2F),
         Math.min(
            Math.min(segment(u, v, 6.0F, 0.8F, 6.0F, 3.2F, 1.3F), segment(u, v, 6.0F, 8.8F, 6.0F, 11.2F, 1.3F)),
            Math.min(segment(u, v, 0.8F, 6.0F, 3.2F, 6.0F, 1.3F), segment(u, v, 8.8F, 6.0F, 11.2F, 6.0F, 1.3F))
         )
      );
         case PERSON -> Math.min(circle(u, v, 6.0F, 3.6F, 2.2F), Math.max(circle(u, v, 6.0F, 11.8F, 4.8F), v - 11.2F));
         case SWING -> Math.min(
         polyline(u, v, 1.3F, 1.8F, 7.0F, 2.36F, 4.9F, 3.9F, 3.36F, 6.0F, 2.8F, 8.1F, 3.36F, 9.64F, 4.9F, 10.2F, 7.2F),
         Math.min(segment(u, v, 10.2F, 7.4F, 8.5F, 5.9F, 1.3F), segment(u, v, 10.2F, 7.4F, 11.3F, 5.6F, 1.3F))
      );
         case CLOUD -> {
            float body = Math.min(
               Math.min(circle(u, v, 4.2F, 7.0F, 2.6F), circle(u, v, 6.6F, 5.2F, 3.1F)),
               Math.min(circle(u, v, 8.8F, 7.3F, 2.3F), box(u, v, 2.2F, 7.2F, 10.2F, 9.6F, 1.2F))
            );
            yield Math.abs(body) - 0.6F;
         }
         case KEY -> Math.min(
         ring(u, v, 3.8F, 6.0F, 2.3F, 1.3F),
         Math.min(
            segment(u, v, 6.1F, 6.0F, 11.0F, 6.0F, 1.3F), Math.min(segment(u, v, 9.2F, 6.0F, 9.2F, 8.4F, 1.3F), segment(u, v, 10.8F, 6.0F, 10.8F, 7.8F, 1.3F))
         )
      );
         case SWORD -> Math.min(
         segment(u, v, 3.4F, 8.6F, 10.6F, 1.4F, 1.5F), Math.min(segment(u, v, 2.0F, 6.4F, 5.6F, 10.0F, 1.3F), segment(u, v, 1.2F, 10.8F, 3.6F, 8.4F, 1.6F))
      );
         case GEAR -> {
            float teeth = 3.4028235E38F;

            for (int k = 0; k < 6; k++) {
               double a = (double)k * 3.141592653589793 / 3.0;
               float c = (float)Math.cos(a);
               float s = (float)Math.sin(a);
               teeth = Math.min(teeth, segment(u, v, 6.0F + c * 3.4F, 6.0F + s * 3.4F, 6.0F + c * 5.0F, 6.0F + s * 5.0F, 1.7F));
            }

            yield Math.min(ring(u, v, 6.0F, 6.0F, 2.8F, 1.5F), teeth);
         }
         case CHEVRON -> polyline(u, v, 1.7F, 4.4F, 2.0F, 8.4F, 6.0F, 4.4F, 10.0F);
         case PLUS -> Math.min(segment(u, v, 6.0F, 1.8F, 6.0F, 10.2F, 1.6F), segment(u, v, 1.8F, 6.0F, 10.2F, 6.0F, 1.6F));
         case MINUS -> segment(u, v, 1.8F, 6.0F, 10.2F, 6.0F, 1.6F);
         case STAR -> {
            float[] points = new float[20];

            for (int k = 0; k < 10; k++) {
               double a = -1.5707963267948966 + (double)k * 3.141592653589793 / 5.0;
               float r = k % 2 == 0 ? 5.4F : 2.3F;
               points[k * 2] = 6.0F + (float)Math.cos(a) * r;
               points[k * 2 + 1] = 6.4F + (float)Math.sin(a) * r;
            }

            boolean in = false;
            int k = 0;

            for (int j = 9; k < 10; j = k++) {
               float xi = points[k * 2];
               float yi = points[k * 2 + 1];
               float xj = points[j * 2];
               float yj = points[j * 2 + 1];
               if (yi > v != yj > v && u < (xj - xi) * (v - yi) / (yj - yi) + xi) {
                  in = !in;
               }
            }

            float edge = 3.4028235E38F;
            k = 0;

            for (int j = 9; k < 10; j = k++) {
               edge = Math.min(edge, segment(u, v, points[j * 2], points[j * 2 + 1], points[k * 2], points[k * 2 + 1], 0.0F));
            }

            yield (in ? -edge : edge) - 0.3F;
         }
      };
   }

   public static enum Icon {
      SEARCH,
      DONUT,
      EYE,
      SPARKLE,
      CUBE,
      CHEST,
      PICKAXE,
      PULSE,
      COLUMNS,
      WINDOW,
      GRID,
      CHEVRONS,
      CLOSE,
      MOON,
      CROWN,
      SPEED,
      CAMERA,
      RINGS,
      TAG,
      CROSSHAIR,
      PERSON,
      SWING,
      CLOUD,
      KEY,
      SWORD,
      GEAR,
      CHEVRON,
      PLUS,
      MINUS,
      STAR;
   }
}
