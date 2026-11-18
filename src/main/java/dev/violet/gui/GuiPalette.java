package dev.violet.gui;

public final class GuiPalette {
   private GuiPalette() {
   }

   public static int accent() {
      return abgr(Theme.accent() | 0xFF000000);
   }

   public static int abgr(int argb) {
      return argb & -16711936 | argb >> 16 & 0xFF | (argb & 0xFF) << 16;
   }
}
