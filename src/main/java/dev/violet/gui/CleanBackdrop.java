package dev.violet.gui;

import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

final class CleanBackdrop {
   private static final class_2960 ID = class_2960.method_60655("violet", "dynamic/clean_backdrop");
   private static int builtW;
   private static int builtH;

   private CleanBackdrop() {
   }

   static void draw(class_332 context, float opacity) {
      if (!(opacity <= 0.002F)) {
         class_1041 window = class_310.method_1551().method_22683();
         int w = Math.max(1, window.method_4489());
         int h = Math.max(1, window.method_4506());
         if (w != builtW || h != builtH) {
            build(w, h);
         }

         float ratio = (float)h / (float)Math.max(1, window.method_4502());
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.identity();
         matrices.scale(1.0F / ratio, 1.0F / ratio);
         context.method_25293(class_10799.field_56883, ID, 0, 0, 0.0F, 0.0F, w, h, w, h, w, h, MenuKit.alpha(-1, opacity));
         matrices.popMatrix();
      }
   }

   private static void build(int w, int h) {
      class_1011 image = new class_1011(w, h, false);
      float cx = (float)w * 0.5F;
      float cy = (float)h * 0.42F;
      float sx = 1.0F / ((float)w * 0.7F);
      float sy = 1.0F / ((float)h * 0.85F);
      int seed = -1640531527;

      for (int y = 0; y < h; y++) {
         float dy = ((float)y - cy) * sy;

         for (int x = 0; x < w; x++) {
            float dx = ((float)x - cx) * sx;
            float d = Math.min(1.0F, (float)Math.sqrt((double)(dx * dx + dy * dy)));
            float t = d * d * (3.0F - 2.0F * d);
            int var20 = seed ^ seed << 13;
            int var21 = var20 ^ var20 >>> 17;
            seed = var21 ^ var21 << 5;
            float n1 = (float)(seed & 65535) / 65535.0F;
            float n2 = (float)(seed >>> 16 & 65535) / 65535.0F;
            float noise = n1 + n2 - 1.0F;
            int r = clamp(Math.round(22.0F - 10.0F * t + noise));
            int g = clamp(Math.round(22.0F - 10.0F * t + noise));
            int b = clamp(Math.round(25.0F - 11.0F * t + noise));
            image.method_61941(x, y, 0xFF000000 | r << 16 | g << 8 | b);
         }
      }

      class_310.method_1551().method_1531().method_4615(ID);
      Smooth.register(ID, image);
      builtW = w;
      builtH = h;
   }

   private static int clamp(int v) {
      return Math.max(0, Math.min(255, v));
   }
}
