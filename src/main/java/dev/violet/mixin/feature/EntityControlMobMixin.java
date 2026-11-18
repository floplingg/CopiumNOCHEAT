package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.EntityControl;
import net.minecraft.class_1308;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1308.class})
public abstract class EntityControlMobMixin {
   @ModifyReturnValue(
      method = {"method_66672"},
      at = {@At("RETURN")}
   )
   private boolean violet$entityControlSpoofSaddle(boolean original) {
      return original || EntityControl.spoofSaddle();
   }
}
