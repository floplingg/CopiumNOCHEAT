package dev.violet.mixin;

import dev.violet.SoberClient;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_638.class})
public abstract class ClientWorldMixin {
   @Inject(
      method = {"method_41928"},
      at = {@At("HEAD")}
   )
   private void violet$onBlockUpdate(class_2338 pos, class_2680 state, int flags, CallbackInfo ci) {
      class_2680 old = ((class_638)(Object)this).method_8320(pos);
      if (old != state) {
         SoberClient.MODULES.onBlockUpdate(pos.method_10062(), old, state);
      }
   }
}
