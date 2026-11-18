package dev.violet.mixin;

import dev.violet.event.EventBus;
import dev.violet.event.impl.TickEvent;
import dev.violet.util.PerformanceBudget;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_310.class})
public abstract class TickEventsMixin {
   @Inject(
      method = {"method_1574"},
      at = {@At("HEAD")},
      require = 0
   )
   private void violet$tickStart(CallbackInfo ci) {
      EventBus.INSTANCE.drainAsync();
      PerformanceBudget.beginTick();
      TickEvent.dispatch();
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("RETURN")},
      require = 0
   )
   private void violet$tickEnd(CallbackInfo ci) {
      TickEvent.dispatchPost();
   }
}
