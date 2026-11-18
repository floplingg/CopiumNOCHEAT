package dev.violet.gui;

public final class EgyptDraw {
   private EgyptDraw() {
   }

   public static int col(int argb, float alpha) {
      int a = Math.round((float)(argb >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return GuiPalette.abgr(a << 24 | argb & 16777215);
   }
}
