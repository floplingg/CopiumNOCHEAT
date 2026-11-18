package dev.violet.mixin;

import dev.violet.modules.NameTags;
import net.minecraft.class_5223;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_5223.class})
public abstract class TextVisitFactoryMixin {
   @ModifyVariable(
      method = {"method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0,
      require = 0
   )
   private static String violet$customName(String text) {
      return NameTags.replace(text);
   }
}
