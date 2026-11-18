package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.gui.Backdrop;
import dev.violet.gui.ScreenStyle;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_437.class})
public abstract class ScreenMixin {
   @Shadow
   protected class_310 field_22787;
   @Shadow
   public int field_22789;
   @Shadow
   public int field_22790;
   @Unique
   private long violet$openedAt;

   private boolean violet$menu() {
      return this.field_22787 != null && this.field_22787.field_1687 == null && ScreenStyle.screens();
   }

   @Inject(
      method = {"method_25423(II)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void violet$opened(int width, int height, CallbackInfo ci) {
      if (this.violet$openedAt == 0L) {
         this.violet$openedAt = System.nanoTime();
      }
   }

   @WrapOperation(
      method = {"method_47413"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_437;method_25394(Lnet/minecraft/class_332;IIF)V"
      )},
      require = 0
   )
   private void violet$intro(class_437 screen, class_332 context, int mouseX, int mouseY, float delta, Operation<Void> original) {
      float p = ScreenStyle.intro(this.violet$openedAt, this.field_22787, screen);
      if (p >= 1.0F) {
         original.call(new Object[]{screen, context, mouseX, mouseY, delta});
      } else {
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(0.0F, 10.0F * (1.0F - p));

         try {
            original.call(new Object[]{screen, context, mouseX, mouseY, delta});
         } finally {
            matrices.popMatrix();
         }

         context.method_71048();
         ScreenStyle.introVeil(context, this.field_22789, this.field_22790, p);
      }
   }

   @Inject(
      method = {"method_57728"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$backdrop(class_332 context, float deltaTicks, CallbackInfo ci) {
      if (this.violet$menu() && !ScreenStyle.vanillaPlus()) {
         ScreenStyle.backdrop(context, this.field_22789, this.field_22790);
         if (Backdrop.requestMenu(this.field_22787)) {
            try {
               context.method_71048();
               context.method_71278();
            } catch (IllegalStateException var5) {
            }
         }

         ci.cancel();
      }
   }

   @Inject(
      method = {"method_57734"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$blur(class_332 context, CallbackInfo ci) {
      if (this.violet$menu() && !ScreenStyle.vanillaPlus()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_57736(Lnet/minecraft/class_332;IIII)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$darkening(class_332 context, int x, int y, int w, int h, CallbackInfo ci) {
      if (this.violet$menu()) {
         ScreenStyle.darkening(context, x, y, w, h);
         ci.cancel();
      }
   }
}
