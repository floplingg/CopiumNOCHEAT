package dev.violet.mixin;

import dev.violet.modules.ClearView;
import net.minecraft.class_12077;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_9976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_9976.class})
public abstract class WeatherRenderingMixin {
   @Inject(
      method = {"method_62315"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$noRain(class_1937 world, int ticks, float tickProgress, class_243 cameraPos, class_12077 state, CallbackInfo ci) {
      try {
         if (!ClearView.noRain()) {
            return;
         }
      } catch (RuntimeException var8) {
         return;
      }

      state.method_74938();
      ci.cancel();
   }

   @Inject(
      method = {"method_62319"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$noRainSound(CallbackInfo ci) {
      try {
         if (ClearView.noRain()) {
            ci.cancel();
         }
      } catch (RuntimeException var3) {
      }
   }
}
