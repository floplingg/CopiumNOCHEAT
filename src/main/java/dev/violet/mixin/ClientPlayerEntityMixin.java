package dev.violet.mixin;

import dev.violet.modules.Boatfly;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_746.class})
public class ClientPlayerEntityMixin {
   @Inject(
      method = {"method_6007"},
      at = {@At("HEAD")}
   )
   private void violet$phase(CallbackInfo ci) {
      if (Boatfly.phasing()) {
         ((class_746)this).field_5960 = true;
      }
   }
}
