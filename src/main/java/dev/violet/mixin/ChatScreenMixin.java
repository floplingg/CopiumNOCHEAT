package dev.violet.mixin;

import dev.violet.gui.HudRenderer;
import net.minecraft.class_11909;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_408.class})
public abstract class ChatScreenMixin {
   @Inject(
      method = {"method_25402"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_437;method_25402(Lnet/minecraft/class_11909;Z)Z"
      )},
      cancellable = true
   )
   private void violet$dragHud(class_11909 click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      if (HudRenderer.chatPress(click.comp_4798(), click.comp_4799(), click.method_74245())) {
         cir.setReturnValue(true);
      }
   }

   @Inject(
      method = {"method_25401"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$scaleHud(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
      if (HudRenderer.chatScroll(mouseX, mouseY, verticalAmount)) {
         cir.setReturnValue(true);
      }
   }
}
