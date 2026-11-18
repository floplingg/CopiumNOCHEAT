package dev.violet.mixin;

import dev.violet.sky.GlintManager;
import net.minecraft.class_12250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_12250.class})
public abstract class TextureTransformMixin {
   @ModifyVariable(
      method = {"method_76031"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private static float violet$glintScale(float scale) {
      return GlintManager.scale(scale);
   }
}
