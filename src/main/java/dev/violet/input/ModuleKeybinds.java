package dev.violet.input;

public final class ModuleKeybinds {
   public static final int MOUSE_OFFSET = 1000;

   private ModuleKeybinds() {
   }

   public static boolean isBindable(int key) {
      return key >= 32 && key <= 348 || key >= 1000 && key < 1008;
   }
}
