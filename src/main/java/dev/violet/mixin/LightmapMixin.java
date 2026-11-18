package dev.violet.mixin;

import dev.violet.modules.Fullbright;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({class_765.class})
public abstract class LightmapMixin {
   @ModifyArg(
      method = {"method_3313"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
         ordinal = 3
      ),
      require = 0
   )
   private float violet$nightVision(float value) {
      return Fullbright.nightVision(value);
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
         ordinal = 6
      ),
      require = 0
   )
   private float violet$gamma(float value) {
      return Fullbright.gamma(value);
   }
}
