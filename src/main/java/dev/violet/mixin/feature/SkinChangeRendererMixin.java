package dev.violet.mixin.feature;

import dev.violet.module.ModuleRegistry;
import dev.violet.modules.SkinChange;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1007.class})
public abstract class SkinChangeRendererMixin {
   @Inject(
      method = {"method_62604"},
      at = {@At("TAIL")}
   )
   private void violet$applySkin(class_11890 entity, class_10055 state, float partialTick, CallbackInfo ci) {
      SkinChange module = ModuleRegistry.get(SkinChange.class);
      if (module != null && module.isEnabled()) {
         state.field_53520 = module.skinFor(state.field_53528, state.field_53520);
      }
   }
}
