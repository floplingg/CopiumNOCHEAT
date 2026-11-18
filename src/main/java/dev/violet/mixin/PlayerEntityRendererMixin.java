package dev.violet.mixin;

import dev.violet.modules.Capes;
import dev.violet.modules.NameTags;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1007.class})
public abstract class PlayerEntityRendererMixin {
   @Inject(
      method = {"method_74935(Lnet/minecraft/class_11890;D)Z"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void violet$ownTag(class_11890 entity, double distance, CallbackInfoReturnable<Boolean> cir) {
      if (!cir.getReturnValueZ() && NameTags.showOwnTag(entity)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(
      method = {"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"},
      at = {@At("TAIL")}
   )
   private void violet$fakeRole(class_11890 entity, class_10055 state, float tickProgress, CallbackInfo ci) {
      state.field_53337 = NameTags.decorate(entity, state.field_53337);
      Capes.apply(entity, state);
   }
}
