package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.BoatNoclip;
import net.minecraft.class_10255;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_10255.class})
public abstract class BoatNoclipMixin {
   @ModifyReturnValue(
      method = {"method_30949"},
      at = {@At("RETURN")}
   )
   private boolean violet$boatNoclipCollide(boolean original) {
      if (original && BoatNoclip.active != null) {
         class_10255 boat = (class_10255)this;
         return !boat.method_73183().method_8608() || boat.method_5642() != class_310.method_1551().field_1724;
      } else {
         return original;
      }
   }
}
