package dev.violet.mixin;

import dev.violet.sky.SkyRenderer;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_9975.class})
public abstract class SkyRenderingMixin {
   @Inject(
      method = {"method_62302"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$renderSky(CallbackInfo ci) {
      try {
         if (SkyRenderer.renderSky()) {
            ci.cancel();
         }
      } catch (RuntimeException var3) {
      }
   }

   @Inject(
      method = {"method_62306"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$skipSunrise(CallbackInfo ci) {
      if (SkyRenderer.drewThisFrame()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_62307"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$skipCelestials(CallbackInfo ci) {
      if (SkyRenderer.drewThisFrame()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_62305"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$skipDarkDisc(CallbackInfo ci) {
      if (SkyRenderer.drewThisFrame()) {
         ci.cancel();
      }
   }
}
