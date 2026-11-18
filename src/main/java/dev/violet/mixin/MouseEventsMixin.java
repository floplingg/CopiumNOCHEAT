package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.event.impl.MouseInputEvent;
import dev.violet.modules.CreamyKeys;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public abstract class MouseEventsMixin {
   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")},
      require = 0
   )
   private void violet$mouseClickSound(long window, class_11910 input, int action, CallbackInfo ci) {
      if (window == class_310.method_1551().method_22683().method_4490()) {
         try {
            CreamyKeys keys = SoberClient.MODULES.get(CreamyKeys.class);
            if (keys != null && keys.isActive()) {
               keys.onMouse(input.comp_4801(), action);
            }
         } catch (RuntimeException var7) {
            SoberClient.LOG.error("Creamy Keys failed", var7);
         }
      }
   }

   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$mouseButtonEvent(long window, class_11910 input, int action, CallbackInfo ci) {
      if (MouseInputEvent.dispatch(window, input.comp_4801(), action, input.comp_4797())) {
         ci.cancel();
      }
   }
}
