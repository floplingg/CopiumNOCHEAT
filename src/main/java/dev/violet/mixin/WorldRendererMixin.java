package dev.violet.mixin;

import dev.violet.sky.SkyRenderer;
import dev.violet.sky.SkySettings;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_761.class})
public abstract class WorldRendererMixin {
   @Inject(
      method = {"method_62204"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$hideClouds(CallbackInfo ci) {
      if (SkySettings.hideVanillaClouds && SkyRenderer.isCustomSkySelected()) {
         ci.cancel();
      }
   }
}
