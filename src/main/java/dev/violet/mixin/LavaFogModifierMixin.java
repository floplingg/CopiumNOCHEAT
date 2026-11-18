package dev.violet.mixin;

import dev.violet.modules.ClearView;
import net.minecraft.class_11401;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_11401.class})
public abstract class LavaFogModifierMixin {
   @Inject(
      method = {"method_42591"},
      at = {@At("RETURN")},
      require = 0
   )
   private void violet$clearLava(class_7285 data, class_4184 camera, class_638 world, float viewDistance, class_9779 counter, CallbackInfo ci) {
      try {
         if (!ClearView.clearLava()) {
            return;
         }
      } catch (RuntimeException var8) {
         return;
      }

      data.field_60582 = viewDistance * 0.5F;
      data.field_60584 = viewDistance;
      data.field_60099 = viewDistance;
      data.field_60100 = viewDistance;
   }
}
