package dev.violet.mixin;

import dev.violet.modules.ClearView;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4603.class})
public abstract class InGameOverlayRendererMixin {
   @Inject(
      method = {"method_23069"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$clearWaterOverlay(class_310 client, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
      try {
         if (ClearView.clearWater()) {
            ci.cancel();
         }
      } catch (RuntimeException var5) {
      }
   }
}
