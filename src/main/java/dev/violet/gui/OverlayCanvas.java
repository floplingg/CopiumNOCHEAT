package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.module.HudOverlayProvider;
import dev.violet.module.Module;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_8030;
import org.joml.Matrix3x2fStack;

public final class OverlayCanvas {
   private static final float FONT_PX = 9.0F;
   private static long lastFrame;
   private static float deltaTime;
   private final class_332 context;

   private OverlayCanvas(class_332 context) {
      this.context = context;
   }

   public static void renderProviders(class_332 context) {
      class_310 mc = class_310.method_1551();
      long now = System.nanoTime();
      deltaTime = lastFrame == 0L ? 0.0F : Math.min(0.1F, (float)(now - lastFrame) / 1.0E9F);
      lastFrame = now;
      float width = (float)mc.method_22683().method_4489();
      float height = (float)mc.method_22683().method_4506();
      float scale = (float)(1.0 / (double)mc.method_22683().method_4495());
      OverlayCanvas canvas = null;

      for (Module module : SoberClient.MODULES.all()) {
         if (module instanceof HudOverlayProvider provider && provider.isHudOverlayEnabled()) {
            if (canvas == null) {
               canvas = new OverlayCanvas(context);
            }

            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.scale(scale, scale);

            try {
               provider.renderHudOverlay(canvas, width, height);
            } catch (RuntimeException var16) {
               SoberClient.LOG.debug("Overlay of {} failed", module.name, var16);
            } finally {
               matrices.popMatrix();
            }
         }
      }
   }

   public static float deltaTime() {
      return deltaTime;
   }

   public static int argb(int abgr) {
      return abgr & -16711936 | abgr >> 16 & 0xFF | (abgr & 0xFF) << 16;
   }

   public class_332 context() {
      return this.context;
   }

   public static OverlayCanvas of(class_332 context) {
      return new OverlayCanvas(context);
   }

   public void addImage(class_2960 texture, float x1, float y1, float x2, float y2, int abgr) {
      int left = Math.round(Math.min(x1, x2));
      int top = Math.round(Math.min(y1, y2));
      int w = Math.max(1, Math.round(Math.abs(x2 - x1)));
      int h = Math.max(1, Math.round(Math.abs(y2 - y1)));
      this.context.method_25293(class_10799.field_56883, texture, left, top, 0.0F, 0.0F, w, h, w, h, w, h, argb(abgr));
   }

   public void pushClipRect(float x1, float y1, float x2, float y2, boolean intersect) {
      int left = (int)Math.floor((double)x1);
      int top = (int)Math.floor((double)y1);
      int right = Math.max(left, (int)Math.ceil((double)x2));
      int bottom = Math.max(top, (int)Math.ceil((double)y2));
      this.context.field_44659.method_49700(new class_8030(left, top, right - left, bottom - top).method_65185(this.context.method_51448()));
   }

   public void popClipRect() {
      this.context.field_44659.method_49699();
   }

   public void addRectFilled(float x1, float y1, float x2, float y2, int abgr) {
      this.addRectFilled(x1, y1, x2, y2, abgr, 0.0F);
   }

   public void addRectFilled(float x1, float y1, float x2, float y2, int abgr, float rounding) {
      int left = Math.round(Math.min(x1, x2));
      int top = Math.round(Math.min(y1, y2));
      int right = Math.round(Math.max(x1, x2));
      int bottom = Math.round(Math.max(y1, y2));
      if (right > left && bottom > top) {
         Smooth.rect(this.context, left, top, right - left, bottom - top, Math.round(rounding), argb(abgr));
      }
   }

   public void addRectFilled(float x1, float y1, float x2, float y2, int abgr, float rounding, int flags) {
      this.addRectFilled(x1, y1, x2, y2, abgr, rounding);
   }

   public void addRect(float x1, float y1, float x2, float y2, int abgr) {
      this.addRect(x1, y1, x2, y2, abgr, 0.0F, 0, 1.0F);
   }

   public void addRect(float x1, float y1, float x2, float y2, int abgr, float rounding) {
      this.addRect(x1, y1, x2, y2, abgr, rounding, 0, 1.0F);
   }

   public void addRect(float x1, float y1, float x2, float y2, int abgr, float rounding, int flags, float thickness) {
      float t = Math.max(1.0F, thickness);
      this.addRectFilled(x1, y1, x2, y1 + t, abgr);
      this.addRectFilled(x1, y2 - t, x2, y2, abgr);
      this.addRectFilled(x1, y1 + t, x1 + t, y2 - t, abgr);
      this.addRectFilled(x2 - t, y1 + t, x2, y2 - t, abgr);
   }

   public void addRectFilledMultiColor(float x1, float y1, float x2, float y2, int upperLeft, int upperRight, int bottomRight, int bottomLeft) {
      int left = Math.round(Math.min(x1, x2));
      int top = Math.round(Math.min(y1, y2));
      int right = Math.round(Math.max(x1, x2));
      int bottom = Math.round(Math.max(y1, y2));
      if (right > left && bottom > top) {
         if (upperLeft == upperRight && bottomLeft == bottomRight) {
            this.context.method_25296(left, top, right, bottom, argb(upperLeft), argb(bottomLeft));
         } else {
            int columns = Math.max(1, Math.min(96, right - left));

            for (int i = 0; i < columns; i++) {
               float k = ((float)i + 0.5F) / (float)columns;
               int from = left + (right - left) * i / columns;
               int to = left + (right - left) * (i + 1) / columns;
               if (to > from) {
                  this.context.method_25296(from, top, to, bottom, argb(mix(upperLeft, upperRight, k)), argb(mix(bottomLeft, bottomRight, k)));
               }
            }
         }
      }
   }

   public void addLine(float x1, float y1, float x2, float y2, int abgr) {
      this.addLine(x1, y1, x2, y2, abgr, 1.0F);
   }

   public void addLine(float x1, float y1, float x2, float y2, int abgr, float thickness) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float length = (float)Math.sqrt((double)(dx * dx + dy * dy));
      if (!(length < 0.001F)) {
         float half = Math.max(0.5F, thickness * 0.5F);
         float nx = -dy / length * half;
         float ny = dx / length * half;
         this.fillPolygon(argb(abgr), x1 + nx, y1 + ny, x2 + nx, y2 + ny, x2 - nx, y2 - ny, x1 - nx, y1 - ny);
      }
   }

   public void addTriangleFilled(float x1, float y1, float x2, float y2, float x3, float y3, int abgr) {
      this.fillPolygon(argb(abgr), x1, y1, x2, y2, x3, y3);
   }

   public void addTriangle(float x1, float y1, float x2, float y2, float x3, float y3, int abgr, float thickness) {
      this.addLine(x1, y1, x2, y2, abgr, thickness);
      this.addLine(x2, y2, x3, y3, abgr, thickness);
      this.addLine(x3, y3, x1, y1, abgr, thickness);
   }

   public void addCircleFilled(float cx, float cy, float radius, int abgr) {
      int r = Math.max(1, Math.round(radius));
      Smooth.circle(this.context, Math.round(cx), Math.round(cy), r, argb(abgr));
   }

   public void addCircleFilled(float cx, float cy, float radius, int abgr, int segments) {
      this.addCircleFilled(cx, cy, radius, abgr);
   }

   public void addCircle(float cx, float cy, float radius, int abgr, int segments, float thickness) {
      int steps = Math.max(12, segments);

      for (int i = 0; i < steps; i++) {
         double a = 6.283185307179586 * (double)i / (double)steps;
         double b = 6.283185307179586 * (double)(i + 1) / (double)steps;
         this.addLine(
            cx + (float)Math.cos(a) * radius,
            cy + (float)Math.sin(a) * radius,
            cx + (float)Math.cos(b) * radius,
            cy + (float)Math.sin(b) * radius,
            abgr,
            thickness
         );
      }
   }

   public void addText(float x, float y, int abgr, String text) {
      this.addText(13.0F, x, y, abgr, text, true);
   }

   public void addText(float px, float x, float y, int abgr, String text, boolean shadow) {
      if (text != null && !text.isEmpty() && abgr >>> 24 != 0) {
         class_327 font = class_310.method_1551().field_1772;
         float scale = px / 9.0F;
         Matrix3x2fStack matrices = this.context.method_51448();
         matrices.pushMatrix();
         matrices.translate(x, y);
         matrices.scale(scale, scale);
         this.context.method_51433(font, text, 0, 0, argb(abgr), shadow);
         matrices.popMatrix();
      }
   }

   public static float textWidth(String text, float px) {
      return text != null && !text.isEmpty() ? (float)class_310.method_1551().field_1772.method_1727(text) * px / 9.0F : 0.0F;
   }

   private void fillPolygon(int argb, float... points) {
      if (argb >>> 24 != 0) {
         int count = points.length / 2;
         float minY = 3.4028235E38F;
         float maxY = -3.4028235E38F;

         for (int i = 0; i < count; i++) {
            minY = Math.min(minY, points[i * 2 + 1]);
            maxY = Math.max(maxY, points[i * 2 + 1]);
         }

         for (int row = (int)Math.floor((double)minY); row < (int)Math.ceil((double)maxY); row++) {
            float y = (float)row + 0.5F;
            float left = 3.4028235E38F;
            float right = -3.4028235E38F;

            for (int i = 0; i < count; i++) {
               float ax = points[i * 2];
               float ay = points[i * 2 + 1];
               float bx = points[(i + 1) % count * 2];
               float by = points[(i + 1) % count * 2 + 1];
               if (ay <= y && by > y || by <= y && ay > y) {
                  float x = ax + (y - ay) / (by - ay) * (bx - ax);
                  left = Math.min(left, x);
                  right = Math.max(right, x);
               }
            }

            if (right > left) {
               this.context.method_25294(Math.round(left), row, Math.round(right), row + 1, argb);
            }
         }
      }
   }

   private static int mix(int from, int to, float k) {
      int a = Math.round((float)(from >>> 24) + (float)((to >>> 24) - (from >>> 24)) * k);
      int b = Math.round((float)(from >> 16 & 0xFF) + (float)((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * k);
      int g = Math.round((float)(from >> 8 & 0xFF) + (float)((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * k);
      int r = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * k);
      return a << 24 | b << 16 | g << 8 | r;
   }
}
