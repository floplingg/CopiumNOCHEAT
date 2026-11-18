package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.EntityControl;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_746.class})
public abstract class EntityControlPlayerMixin {
   @ModifyReturnValue(
      method = {"method_3151"},
      at = {@At("RETURN")}
   )
   private float violet$maxMountJump(float original) {
      return EntityControl.maxJump() ? 1.0F : original;
   }
}
