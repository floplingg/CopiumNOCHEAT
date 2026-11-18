package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.SoberClient;
import dev.violet.modules.MotionBlur;
import dev.violet.modules.Zoom;
import dev.violet.render.Render3D;
import dev.violet.sky.ItemAuraRenderer;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import net.minecraft.class_757;
import net.minecraft.class_9920;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_757.class})
public abstract class GameRendererMixin {
   @Shadow
   @Final
   private class_9920 field_53066;

   @Inject(
      method = {"method_3192"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_761;method_3254()V",
         shift = Shift.AFTER
      )},
      require = 0
   )
   private void violet$motionBlur(CallbackInfo ci) {
      MotionBlur.apply(this.field_53066);
   }

   @Inject(
      method = {"method_3172"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V",
         shift = Shift.AFTER
      )}
   )
   private void violet$handSubmitted(CallbackInfo ci) {
      ItemAuraRenderer.onHandSubmitted(class_310.method_1551().field_1724);
   }

   @Inject(
      method = {"method_3196"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void violet$zoom(class_4184 camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
      if (changingFov) {
         Zoom zoom = SoberClient.MODULES.get(Zoom.class);
         if (zoom != null) {
            double divisor = zoom.fovDivisor();
            if (divisor > 1.0001) {
               cir.setReturnValue((float)((double)cir.getReturnValueF() / divisor));
            }
         }
      }
   }


}
