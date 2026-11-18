package dev.violet.gui;

import dev.violet.util.TracerEngine;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;

public final class WorldLabel {
   private static final int PRIMARY = -1;
   private static final int SECONDARY = -1315861;
   private static final float MAIN_PX = 17.0F;
   private static final float SMALL_PX = 14.0F;

   private WorldLabel() {
   }

   public static float[] project(class_310 mc, class_243 world, float screenW, float screenH) {
      class_4184 camera = mc.field_1773.method_19418();
      if (camera != null && camera.method_19332() && TracerEngine.isInFront(camera, world)) {
         class_243 ndc = mc.field_1773.method_70778(world);
         if (Double.isFinite(ndc.field_1352) && Double.isFinite(ndc.field_1351)) {
            float x = (float)(ndc.field_1352 + 1.0) * 0.5F * screenW;
            float y = (float)(1.0 - ndc.field_1351) * 0.5F * screenH;
            return !(x < -200.0F) && !(x > screenW + 200.0F) && !(y < -100.0F) && !(y > screenH + 100.0F) ? new float[]{x, y} : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void drawText(OverlayCanvas canvas, float cx, float bottom, String primary, String secondary, float alpha) {
      float gap = 6.0F;
      float mainWidth = OverlayCanvas.textWidth(primary, 17.0F);
      float smallWidth = secondary == null ? 0.0F : OverlayCanvas.textWidth(secondary, 14.0F);
      float x = (float)Math.round(cx - (mainWidth + (secondary == null ? 0.0F : gap + smallWidth)) * 0.5F);
      float y = (float)Math.round(bottom - 5.0F - 17.0F);
      canvas.addText(17.0F, x, y, fade(-1, alpha), primary, true);
      if (secondary != null) {
         canvas.addText(14.0F, (float)Math.round(x + mainWidth + gap), y + 2.4F, fade(-1315861, alpha), secondary, true);
      }
   }

   private static int fade(int abgr, float alpha) {
      int a = Math.round((float)(abgr >>> 24) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return a << 24 | abgr & 16777215;
   }
}
