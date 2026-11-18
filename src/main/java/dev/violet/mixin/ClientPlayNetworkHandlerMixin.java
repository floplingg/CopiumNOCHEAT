package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.gui.HudMetrics;
import net.minecraft.class_2604;
import net.minecraft.class_2676;
import net.minecraft.class_2761;
import net.minecraft.class_310;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public abstract class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"method_11112"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$onEntitySpawn(class_2604 packet, CallbackInfo ci) {
      if (SoberClient.MODULES.onEntitySpawn(packet)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_11079"},
      at = {@At("HEAD")},
      require = 0
   )
   private void violet$onTime(class_2761 packet, CallbackInfo ci) {
      HudMetrics.onWorldTimeUpdate(packet);
   }

}
