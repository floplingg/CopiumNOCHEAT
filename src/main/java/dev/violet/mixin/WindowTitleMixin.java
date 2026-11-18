package dev.violet.mixin;

import dev.violet.gui.BrandWindow;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_310.class})
public abstract class WindowTitleMixin {
   @Inject(
      method = {"method_24287"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void violet$title(CallbackInfoReturnable<String> cir) {
      cir.setReturnValue(BrandWindow.title((String)cir.getReturnValue()));
   }
}
