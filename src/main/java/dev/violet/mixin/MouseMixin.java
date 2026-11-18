package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.SoberClient;
import dev.violet.modules.Zoom;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public abstract class MouseMixin {
   @WrapOperation(
      method = {"method_1606"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_5872(DD)V"
      )},
      require = 0
   )
   private void violet$look(class_746 player, double dx, double dy, Operation<Void> original) {
      Zoom zoom = SoberClient.MODULES.get(Zoom.class);
      if (zoom != null) {
         double slow = zoom.sensitivityScale();
         dx *= slow;
         dy *= slow;
      }

      original.call(new Object[]{player, dx, dy});
   }

   @Inject(
      method = {"method_1598"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1755 == null && mc.field_1724 != null && vertical != 0.0) {
         Zoom zoom = SoberClient.MODULES.get(Zoom.class);
         if (zoom != null && zoom.scroll(vertical)) {
            ci.cancel();
         }
      }
   }
}
