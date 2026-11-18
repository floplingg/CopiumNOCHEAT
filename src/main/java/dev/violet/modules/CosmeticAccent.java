package dev.violet.modules;

import dev.violet.gui.Theme;

final class CosmeticAccent {
   private CosmeticAccent() {
   }

   static int argb() {
      return 0xFF000000 | Theme.accent();
   }
}
