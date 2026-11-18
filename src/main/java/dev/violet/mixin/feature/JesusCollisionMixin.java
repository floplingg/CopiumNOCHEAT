package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.modules.Jesus;
import net.minecraft.class_1941;
import net.minecraft.class_2338;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3726;
import net.minecraft.class_5329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_5329.class})
public abstract class JesusCollisionMixin {
   @WrapOperation(
      method = {"computeNext"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_3726;method_62878(Lnet/minecraft/class_2680;Lnet/minecraft/class_1941;Lnet/minecraft/class_2338;)Lnet/minecraft/class_265;"
      )}
   )
   private class_265 violet$jesusCollisionShape(class_3726 context, class_2680 state, class_1941 level, class_2338 pos, Operation<class_265> original) {
      class_265 shape = (class_265)original.call(new Object[]{context, state, level, pos});
      Jesus jesus = Jesus.active;
      return jesus == null ? shape : jesus.collisionShape(level, state, pos, shape);
   }
}
