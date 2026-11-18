package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.CreamyKeys;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_309.class})
public abstract class KeyboardMixin {
   @Inject(
      method = {"method_1466"},
      at = {@At("HEAD")},
      require = 0
   )
   private void violet$creamyKeys(long window, int action, class_11908 input, CallbackInfo ci) {
      if (window == class_310.method_1551().method_22683().method_4490()) {
         try {
            CreamyKeys keys = SoberClient.MODULES.get(CreamyKeys.class);
            if (keys != null && keys.isActive()) {
               keys.onKey(input.comp_4795(), action);
            }
         } catch (RuntimeException var7) {
            SoberClient.LOG.error("Creamy Keys failed", var7);
         }
      }
   }
}
