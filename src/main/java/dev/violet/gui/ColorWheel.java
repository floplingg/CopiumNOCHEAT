package dev.violet.gui;

import dev.violet.render.Color;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class ColorWheel {
   private static final class_2960 TEXTURE = class_2960.method_60655("violet", "dynamic/color_wheel");
   private static final int SIZE = 256;
   private static boolean created;

   private ColorWheel() {
   }

   private static void ensureCreated() {
      if (!created) {
         created = true;
         class_1011 image = new class_1011(256, 256, false);
         float center = 128.0F;
         float radius = 126.0F;

         for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
               float dx = (float)x + 0.5F - center;
               float dy = (float)y + 0.5F - center;
               float distance = (float)Math.sqrt((double)(dx * dx + dy * dy));
               if (distance > radius + 1.5F) {
                  image.method_61941(x, y, 0);
               } else {
                  float hue = (float)((Math.atan2((double)dy, (double)dx) / 6.283185307179586 + 1.0) % 1.0);
                  float saturation = Math.min(1.0F, distance / radius);
                  float edge = Math.max(0.0F, Math.min(1.0F, (radius + 1.5F - distance) / 2.0F));
                  Color color = Color.fromHsv(hue, saturation, 1.0F, Math.round(255.0F * edge));
                  image.method_61941(x, y, color.packed());
               }
            }
         }

         Smooth.register(TEXTURE, image);
      }
   }

   public static void draw(class_332 context, int x, int y, int size, float brightness, float alpha) {
      ensureCreated();
      int v = Math.round(255.0F * Math.max(0.15F, brightness));
      int tint = Math.round(255.0F * alpha) << 24 | v << 16 | v << 8 | v;
      context.method_25293(class_10799.field_56883, TEXTURE, x, y, 0.0F, 0.0F, size, size, 256, 256, 256, 256, tint);
   }

   public static float[] pick(double mouseX, double mouseY, int x, int y, int size) {
      double radius = (double)size / 2.0;
      double dx = mouseX - ((double)x + radius);
      double dy = mouseY - ((double)y + radius);
      double distance = Math.min(radius, Math.sqrt(dx * dx + dy * dy));
      float hue = (float)((Math.atan2(dy, dx) / 6.283185307179586 + 1.0) % 1.0);
      float saturation = (float)(distance / radius);
      return new float[]{hue, saturation};
   }
}
