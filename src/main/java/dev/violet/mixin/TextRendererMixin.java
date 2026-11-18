package dev.violet.mixin;

import dev.violet.gui.Theme;
import net.minecraft.class_11719;
import net.minecraft.class_327;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_327.class})
public abstract class TextRendererMixin {
   @ModifyVariable(
      method = {"method_72732"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private class_11719 violet$gameFont(class_11719 source) {
      return Theme.gameFont(source);
   }
}
