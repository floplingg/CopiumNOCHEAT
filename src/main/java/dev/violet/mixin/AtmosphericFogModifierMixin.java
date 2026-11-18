package dev.violet.mixin;

import dev.violet.modules.ClearView;
import net.minecraft.class_11398;
import net.minecraft.class_12206;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_11398.class})
public abstract class AtmosphericFogModifierMixin {
   @Inject(
      method = {"method_42591"},
      at = {@At("RETURN")},
      require = 0
   )
   private void violet$noRainFog(class_7285 data, class_4184 camera, class_638 world, float viewDistance, class_9779 counter, CallbackInfo ci) {
      try {
         if (!ClearView.noRain() || world.method_8430(1.0F) <= 0.0F) {
            return;
         }

         if (class_310.method_1551().field_1705.method_1740().method_1800()) {
            return;
         }

         float tick = counter.method_60637(false);
         data.field_60582 = (Float)camera.method_75756().method_75689(class_12206.field_64120, tick);
         data.field_60584 = (Float)camera.method_75756().method_75689(class_12206.field_64121, tick);
      } catch (RuntimeException var8) {
      }
   }
}
