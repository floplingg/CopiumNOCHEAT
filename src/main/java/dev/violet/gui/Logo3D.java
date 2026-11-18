package dev.violet.gui;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

final class Logo3D {
   private static final GuiQuads QUADS = new GuiQuads();
   private static final int STRIPS = 28;
   private static final int LAYERS = 8;
   private static final int FRONT = -7342342;
   private static final int DEEP = -15844800;

   private Logo3D() {
   }

   static void draw(class_332 context, float cx, float cy, float size, float yaw, float pitch, float depth, float shine, float shinePos, float alpha) {
      draw(context, cx, cy, size, yaw, pitch, depth, shine, shinePos, alpha, 0.0F);
   }

   static void draw(
      class_332 context, float cx, float cy, float size, float yaw, float pitch, float depth, float shine, float shinePos, float alpha, float bloom
   ) {
      if (!(alpha <= 0.003F) && !(size <= 0.5F)) {
         float pixels = size * Smooth.pixelsPerUnit(context);
         class_2960 colored = Smooth.brandStep(pixels, false);
         class_2960 white = Smooth.brandStep(pixels, true);
         if (colored != null && white != null) {
            float cosY = (float)Math.cos((double)yaw);
            float sinY = (float)Math.sin((double)yaw);
            float cosP = (float)Math.cos((double)pitch);
            float sinP = (float)Math.sin((double)pitch);
            float camera = size * 2.8F;
            float thickness = size * 0.14F * depth;
            boolean front = cosY * cosP >= 0.0F;
            int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
            if (thickness > 0.05F) {
               for (int n = 0; n < 8; n++) {
                  int k = front ? 8 - n : n + 1;
                  float t = (float)k / 8.0F;
                  int shade = lerp(lerp(-7342342, -15844800, 0.35F), -15844800, t);
                  float light = 0.55F + 0.45F * Math.abs(sinY);
                  int tint = Math.round((float)a * Math.min(1.0F, depth * 1.6F)) << 24 | scale(shade, light);
                  strips(context, white, cx, cy, size, thickness * t, cosY, sinY, cosP, sinP, camera, tint, null);
               }
            }

            if (bloom > 0.01F && front) {
               int halo = Math.round((float)a * 0.3F * bloom) << 24 | 9434874;
               int haze = Math.round((float)a * 0.13F * bloom) << 24 | 9434874;
               strips(context, white, cx, cy, size * 1.09F, 0.0F, cosY, sinY, cosP, sinP, camera, haze, null);
               strips(context, white, cx, cy, size * 1.04F, 0.0F, cosY, sinY, cosP, sinP, camera, halo, null);
            }

            float facing = Math.abs(cosY * cosP);
            int face = a << 24 | scale(16777215, front ? 0.72F + 0.28F * facing : 0.5F);
            float faceDepth = front ? 0.0F : thickness;
            strips(context, colored, cx, cy, size, faceDepth, cosY, sinY, cosP, sinP, camera, face, null);
            if (shine > 0.01F && front) {
               strips(context, white, cx, cy, size, 0.0F, cosY, sinY, cosP, sinP, camera, a << 24 | 16777215, new float[]{shinePos, shine});
            }
         }
      }
   }

   private static void strips(
      class_332 context,
      class_2960 texture,
      float cx,
      float cy,
      float size,
      float z,
      float cosY,
      float sinY,
      float cosP,
      float sinP,
      float camera,
      int tint,
      float[] sweep
   ) {
      GuiQuads quads = QUADS.reset();
      float[] corners = new float[8];
      float[] coords = new float[8];
      int[] tints = new int[4];
      int baseAlpha = tint >>> 24;

      for (int i = 0; i < 28; i++) {
         float u0 = (float)i / 28.0F;
         float u1 = (float)(i + 1) / 28.0F;
         float[] us = new float[]{u0, u0, u1, u1};
         float[] vs = new float[]{0.0F, 1.0F, 1.0F, 0.0F};
         boolean visible = sweep == null;

         for (int k = 0; k < 4; k++) {
            float lx = (us[k] - 0.5F) * size;
            float ly = (vs[k] - 0.5F) * size;
            float x1 = lx * cosY + z * sinY;
            float z1 = -lx * sinY + z * cosY;
            float y2 = ly * cosP - z1 * sinP;
            float z2 = ly * sinP + z1 * cosP;
            float f = camera / Math.max(camera * 0.2F, camera + z2);
            corners[k * 2] = cx + x1 * f;
            corners[k * 2 + 1] = cy + y2 * f;
            coords[k * 2] = us[k];
            coords[k * 2 + 1] = vs[k];
            if (sweep == null) {
               tints[k] = tint;
            } else {
               float d = (us[k] + vs[k] * 0.45F) / 1.45F - sweep[0];
               float glow = (float)Math.exp((double)(-d * d / 0.012F)) * sweep[1];
               int value = Math.round((float)baseAlpha * Math.min(1.0F, glow) * 0.9F);
               tints[k] = value << 24 | tint & 16777215;
               if (value > 0) {
                  visible = true;
               }
            }
         }

         if (visible) {
            quads.quad(corners, coords, tints);
         }
      }

      quads.submit(context, class_10799.field_56883, texture);
   }

   private static int scale(int rgb, float factor) {
      int r = Math.min(255, Math.round((float)(rgb >> 16 & 0xFF) * factor));
      int g = Math.min(255, Math.round((float)(rgb >> 8 & 0xFF) * factor));
      int b = Math.min(255, Math.round((float)(rgb & 0xFF) * factor));
      return r << 16 | g << 8 | b;
   }

   private static int lerp(int a, int b, float t) {
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
}
