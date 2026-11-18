package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.violet.modules.ElytraFly;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1309.class})
public abstract class ElytraFlyMixin {
   @ModifyReturnValue(
      method = {"method_6128"},
      at = {@At("RETURN")}
   )
   private boolean violet$elytraFlyIsFallFlying(boolean original) {
      ElytraFly module = ElytraFly.active;
      if (module == null) {
         return original;
      } else {
         class_310 mc = class_310.method_1551();
         class_746 player = mc.field_1724;
         return this == player && mc.method_18854() ? module.modifyFallFlying(player, original) : original;
      }
   }
}
