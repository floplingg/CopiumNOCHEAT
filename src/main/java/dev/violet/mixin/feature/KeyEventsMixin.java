package dev.violet.mixin.feature;

import dev.violet.event.impl.KeyInputEvent;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_309.class})
public abstract class KeyEventsMixin {
   @Inject(
      method = {"method_1466"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$onKeyPress(long window, int action, class_11908 event, CallbackInfo ci) {
      if (KeyInputEvent.dispatch(event.comp_4795(), event.comp_4796(), action, event.comp_4797())) {
         ci.cancel();
      }
   }
}
