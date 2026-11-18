package dev.violet.mixin;

import dev.violet.sky.ItemAuraRenderer;
import net.minecraft.class_11684;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_11684.class})
public abstract class RenderDispatcherMixin {
   @Inject(
      method = {"method_73002"},
      at = {@At("TAIL")}
   )
   private void violet$afterFeatures(CallbackInfo ci) {
      try {
         ItemAuraRenderer.afterFeatures();
      } catch (RuntimeException var3) {
      }
   }
}
