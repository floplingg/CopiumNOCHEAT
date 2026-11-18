package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.Jesus;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_5635;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_5635.class})
public abstract class JesusPowderSnowMixin {
   @ModifyReturnValue(
      method = {"method_32355"},
      at = {@At("RETURN")}
   )
   private static boolean violet$jesusWalkOnPowderSnow(boolean original, class_1297 entity) {
      if (original) {
         return true;
      } else {
         Jesus jesus = Jesus.active;
         return jesus != null && entity == class_310.method_1551().field_1724 && jesus.canWalkOnPowderSnow();
      }
   }
}
