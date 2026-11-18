package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.Freecam;
import dev.violet.modules.Freelook;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class})
public abstract class EntityLookMixin {
   @Inject(
      method = {"method_5872"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$cameraLook(double dx, double dy, CallbackInfo ci) {
      if (this == class_310.method_1551().field_1724) {
         Freecam freecam = SoberClient.MODULES.get(Freecam.class);
         if (freecam != null && freecam.isActive()) {
            freecam.turn(dx, dy);
            ci.cancel();
         } else {
            Freelook freelook = SoberClient.MODULES.get(Freelook.class);
            if (freelook != null && freelook.isActive()) {
               freelook.turn(dx, dy);
               ci.cancel();
            }
         }
      }
   }
}
