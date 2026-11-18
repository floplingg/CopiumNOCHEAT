package dev.violet.input;

import net.minecraft.class_1041;
import net.minecraft.class_3675;

public final class VioletKeyPolling {
   private VioletKeyPolling() {
   }

   public static boolean isKeyDown(class_1041 window, int key) {
      return class_3675.method_15987(window, key);
   }
}
