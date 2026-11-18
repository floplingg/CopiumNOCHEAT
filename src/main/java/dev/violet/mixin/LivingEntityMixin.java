package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.SwingAnimations;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1309.class})
public abstract class LivingEntityMixin {
   @Inject(
      method = {"method_6028"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void violet$swingDuration(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == class_310.method_1551().field_1724) {
         SwingAnimations module = SoberClient.MODULES.get(SwingAnimations.class);
         if (module != null && module.isActive()) {
            cir.setReturnValue(module.duration(cir.getReturnValueI()));
         }
      }
   }
}
