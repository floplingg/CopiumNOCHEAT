package dev.violet.gui;

public final class WorldText {
   public static final int NAME = -1;
   public static final int LABEL = -4541521;
   private static final float NAME_PX = 17.0F;
   private static final float DETAIL_PX = 14.0F;

   private WorldText() {
   }

   public static int hudAccent() {
      return GuiPalette.accent();
   }

   public static void draw(OverlayCanvas canvas, float cx, float bottom, String name, String[] runs, int[] colors, float alpha) {
      if (!(alpha <= 0.001F)) {
         boolean detail = runs.length > 0;
         float y = (float)Math.round(bottom - 2.0F - (detail ? 15.0F : 0.0F) - 17.0F);
         float nameWidth = OverlayCanvas.textWidth(name, 17.0F);
         canvas.addText(17.0F, (float)Math.round(cx - nameWidth * 0.5F), y, scaleAlpha(-1, alpha), name, true);
         if (detail) {
            float detailWidth = 0.0F;

            for (String run : runs) {
               detailWidth += OverlayCanvas.textWidth(run, 14.0F);
            }

            float x = (float)Math.round(cx - detailWidth * 0.5F);
            float detailY = (float)Math.round(y + 17.0F + 1.0F);

            for (int i = 0; i < runs.length; i++) {
               canvas.addText(14.0F, (float)Math.round(x), detailY, scaleAlpha(colors[i], alpha), runs[i], true);
               x += OverlayCanvas.textWidth(runs[i], 14.0F);
            }
         }
      }
   }

   private static int scaleAlpha(int abgr, float alpha) {
      int a = Math.round((float)(abgr >>> 24) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return a << 24 | abgr & 16777215;
   }
}
