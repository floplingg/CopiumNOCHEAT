package dev.violet;

import dev.violet.modules.ClickGui;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;

public final class ClickGuiKey {
   private static class_304 binding;
   private static class_306 lastBound;
   private static int lastModuleKey;

   private ClickGuiKey() {
   }

   public static void register() {
      binding = KeyBindingHelper.registerKeyBinding(
         new class_304("key.violet.clickgui", class_307.field_1668, 344, class_11900.method_74698(class_2960.method_60655("violet", "main")))
      );
   }

   public static void sync(class_310 mc) {
      ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
      if (binding != null && gui != null) {
         push(mc, gui.getKey());
      }
   }

   public static void tick(class_310 mc) {
      ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
      if (binding != null && gui != null) {
         if (lastBound == null) {
            push(mc, gui.getKey());
         } else {
            class_306 bound = KeyBindingHelper.getBoundKeyOf(binding);
            if (!bound.equals(lastBound)) {
               lastBound = bound;
               lastModuleKey = bound.method_1442() == class_307.field_1668 ? bound.method_1444() : -1;
               if (gui.getKey() != lastModuleKey) {
                  gui.setKey(lastModuleKey);
                  SoberClient.CONFIG.save();
               }
            } else if (gui.getKey() != lastModuleKey) {
               push(mc, gui.getKey());
            }

            boolean mouse = lastBound.method_1442() == class_307.field_1672;

            while (binding.method_1436()) {
               if (mouse && mc.field_1755 == null) {
                  gui.toggle();
               }
            }
         }
      }
   }

   private static void push(class_310 mc, int code) {
      class_306 key = code < 0 ? class_3675.field_16237 : class_307.field_1668.method_1447(code);
      lastModuleKey = code;
      lastBound = key;
      if (!KeyBindingHelper.getBoundKeyOf(binding).equals(key)) {
         binding.method_1422(key);
         class_304.method_1426();
         if (mc.field_1690 != null) {
            mc.field_1690.method_1640();
         }
      }
   }
}
