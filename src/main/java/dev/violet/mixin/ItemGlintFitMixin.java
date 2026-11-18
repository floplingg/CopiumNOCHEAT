package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.violet.sky.GlintManager;
import java.util.List;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_777;
import net.minecraft.class_918;
import net.minecraft.class_10444.class_10445;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_918.class})
public abstract class ItemGlintFitMixin {
   @WrapOperation(
      method = {"method_62476"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_918;method_23180(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;Ljava/util/List;[III)V"
      )},
      require = 0
   )
   private static void violet$fitGlint(
      class_4587 matrices,
      class_4588 consumer,
      List<class_777> quads,
      int[] tints,
      int light,
      int overlay,
      Operation<Void> original,
      @Local(argsOnly = true) class_10445 glint
   ) {
      original.call(new Object[]{matrices, glint == class_10445.field_55341 ? consumer : GlintManager.fit(consumer), quads, tints, light, overlay});
   }
}
