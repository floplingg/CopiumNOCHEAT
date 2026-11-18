package dev.violet.mixin.feature;

import dev.violet.modules.Sneak;
import net.minecraft.class_2824;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_2824.class})
public abstract class SneakInteractMixin {
   @ModifyVariable(
      method = {"<init>(IZLnet/minecraft/class_2824$class_5906;)V"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private static boolean violet$packetSneak(boolean usingSecondaryAction) {
      if (usingSecondaryAction) {
         return true;
      } else {
         Sneak sneak = Sneak.active;
         return sneak != null && sneak.doPacket();
      }
   }
}
