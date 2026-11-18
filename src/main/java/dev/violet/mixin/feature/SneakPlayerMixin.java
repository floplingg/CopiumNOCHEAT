package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.violet.modules.Sneak;
import net.minecraft.class_10185;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_746.class})
public abstract class SneakPlayerMixin {
   @ModifyExpressionValue(
      method = {"method_5773"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_744;field_54155:Lnet/minecraft/class_10185;",
         opcode = 180
      )},
      require = 3,
      allow = 3
   )
   private class_10185 violet$packetSneak(class_10185 original) {
      Sneak sneak = Sneak.active;
      return sneak != null && !original.comp_3164() && sneak.doPacket()
         ? new class_10185(
            original.comp_3159(), original.comp_3160(), original.comp_3161(), original.comp_3162(), original.comp_3163(), true, original.comp_3165()
         )
         : original;
   }
}
