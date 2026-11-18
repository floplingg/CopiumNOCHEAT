package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.Fly;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1297.class})
public abstract class FlyEntityMixin {
   @ModifyReturnValue(
      method = {"method_5799"},
      at = {@At("RETURN")}
   )
   private boolean violet$flyIsInWater(boolean original) {
      return original && !this.violet$flyAffects();
   }

   @ModifyReturnValue(
      method = {"method_5771"},
      at = {@At("RETURN")}
   )
   private boolean violet$flyIsInLava(boolean original) {
      return original && !this.violet$flyAffects();
   }

   @ModifyExpressionValue(
      method = {"method_5790"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_5869()Z"
      )}
   )
   private boolean violet$flyIsUnderWater(boolean original) {
      return original && !this.violet$flyAffects();
   }

   @Unique
   private boolean violet$flyAffects() {
      return Fly.active != null && this == class_310.method_1551().field_1724;
   }
}
