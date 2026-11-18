package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.ClickGui;
import dev.violet.modules.CsPickaxe;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_304.class})
public abstract class KeyBindingMixin {
   @Inject(
      method = {"method_1420"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void violet$overridePress(class_306 key, CallbackInfo ci) {
      if (violet$blocks(key)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1416"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void violet$overrideHold(class_306 key, boolean pressed, CallbackInfo ci) {
      if (pressed && violet$blocks(key)) {
         ci.cancel();
      }
   }

   private static boolean violet$blocks(class_306 key) {
      if (key == null || key.method_1442() != class_307.field_1668) {
         return false;
      } else if (class_310.method_1551().field_1755 != null) {
         return false;
      } else if (CsPickaxe.blocks(key.method_1444())) {
         return true;
      } else {
         ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
         return gui != null && gui.overrideKeys.get() ? SoberClient.MODULES.isBound(key.method_1444()) : false;
      }
   }
}
