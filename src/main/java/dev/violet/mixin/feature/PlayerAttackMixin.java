package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.event.impl.PlayerAttackEntityEvent;
import dev.violet.modules.Fly;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1657.class})
public abstract class PlayerAttackMixin {
   @Inject(
      method = {"method_7324"},
      at = {@At("HEAD")}
   )
   private void violet$onAttack(class_1297 target, CallbackInfo ci) {
      if (this == class_310.method_1551().field_1724) {
         PlayerAttackEntityEvent.dispatch(target);
      }
   }

   @ModifyReturnValue(
      method = {"method_49484"},
      at = {@At("RETURN")}
   )
   private float violet$flySpeed(float original) {
      Fly fly = Fly.active;
      if (fly == null) {
         return original;
      } else {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null && this == player) {
            float override = fly.getOffGroundSpeed(player);
            return override != -1.0F ? override : original;
         } else {
            return original;
         }
      }
   }
}
