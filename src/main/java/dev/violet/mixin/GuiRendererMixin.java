package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.gui.Backdrop;
import net.minecraft.class_11228;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_11228.class})
public abstract class GuiRendererMixin {
   @WrapOperation(
      method = {"method_71291"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_757;method_57796()V"
      )},
      require = 0
   )
   private void violet$menuGlass(class_757 renderer, Operation<Void> original) {
      if (!Backdrop.captureMenu()) {
         original.call(new Object[]{renderer});
      }
   }
}
