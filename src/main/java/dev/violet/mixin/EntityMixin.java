package dev.violet.mixin;

import dev.violet.modules.Boatfly;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class})
public class EntityMixin {
   @Inject(
      method = {"method_5784"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$move(class_1313 type, class_243 movement, CallbackInfo ci) {
      if (Boatfly.move((class_1297)this, type)) {
         ci.cancel();
      }
   }
}
