package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.Fly;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_746.class})
public abstract class FlyPlayerMixin {
   @ModifyReturnValue(
      method = {"method_5715"},
      at = {@At("RETURN")}
   )
   private boolean violet$flyNoSneak(boolean original) {
      if (!original) {
         return false;
      } else {
         Fly fly = Fly.active;
         return fly == null || !fly.noSneak();
      }
   }

   @ModifyExpressionValue(
      method = {"method_6007"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1656;method_7252()F"
      )}
   )
   private float violet$flyFreecamVertical(float original) {
      Fly fly = Fly.active;
      return fly != null && fly.suppressFlightVerticalInput() ? 0.0F : original;
   }
}
