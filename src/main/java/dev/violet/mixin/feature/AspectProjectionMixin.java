package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.modules.Aspect;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_757;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_757.class})
public abstract class AspectProjectionMixin {
   @Shadow
   @Final
   private class_310 field_4015;

   @WrapOperation(
      method = {"method_3188", "method_74751", "method_70778"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_757;method_22973(F)Lorg/joml/Matrix4f;"
      )}
   )
   private Matrix4f violet$aspectWorld(class_757 renderer, float fov, Operation<Matrix4f> original) {
      Matrix4f matrix = (Matrix4f)original.call(new Object[]{renderer, fov});
      class_1041 window = this.field_4015.method_22683();
      if (matrix != null && window != null && window.method_4506() > 0) {
         float screen = (float)window.method_4489() / (float)window.method_4506();
         float wanted = Aspect.aspect(screen);
         return wanted == screen ? matrix : matrix.scaleLocal(screen / wanted, 1.0F, 1.0F);
      } else {
         return matrix;
      }
   }
}
