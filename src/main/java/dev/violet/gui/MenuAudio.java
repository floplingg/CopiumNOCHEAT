package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.MainMenu;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3419;
import net.minecraft.class_1113.class_1114;

public final class MenuAudio {
   private static final class_2960 THEME = class_2960.method_60655("violet", "menu_theme");
   private static class_1113 current;
   private static boolean pending = true;

   private MenuAudio() {
   }

   public static void menuShown() {
      if (pending) {
         pending = false;
         MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
         if (menu != null && menu.audio.get()) {
            class_310 client = class_310.method_1551();
            if (client.method_1483() != null) {
               client.method_1538().method_4859();
               current = new class_1109(
                  THEME,
                  class_3419.field_61058,
                  (float)menu.audioVolume.get().intValue() / 100.0F,
                  1.0F,
                  class_1113.method_43221(),
                  false,
                  0,
                  class_1114.field_5478,
                  0.0,
                  0.0,
                  0.0,
                  true
               );
               client.method_1483().method_4873(current);
            }
         }
      }
   }

   public static boolean playing() {
      if (current == null) {
         return false;
      } else if (class_310.method_1551().method_1483().method_4877(current)) {
         return true;
      } else {
         current = null;
         return false;
      }
   }

   public static void stop() {
      if (current != null) {
         class_310.method_1551().method_1483().method_4870(current);
      }

      current = null;
   }

   public static void tick(class_310 client) {
      if (client.field_1687 != null) {
         pending = false;
         if (current != null) {
            stop();
         }
      }
   }
}
