package dev.violet.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.violet.modules.Glass;
import net.minecraft.class_1921;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_918.class})
public abstract class ItemGlassMixin {
   @ModifyVariable(
      method = {"method_62476"},
      at = @At("HEAD"),
      argsOnly = true,
      require = 0
   )
   private static class_1921 violet$glass(class_1921 layer, @Local(argsOnly = true) class_811 displayContext) {
      return Glass.item(layer, displayContext);
   }
}
