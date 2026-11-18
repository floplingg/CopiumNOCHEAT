package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.violet.SoberClient;
import dev.violet.modules.CsPickaxe;
import dev.violet.modules.CustomModels;
import net.minecraft.class_10426;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_811;
import net.minecraft.class_989;
import net.minecraft.class_572.class_573;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_989.class})
public abstract class HeldItemFeatureRendererMixin {
   @WrapOperation(
      method = {"method_4192"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_10444;method_65604(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;III)V"
      )},
      require = 0
   )
   private void violet$customModel(
      class_10444 state,
      class_4587 matrices,
      class_11659 queue,
      int light,
      int overlay,
      int outline,
      Operation<Void> original,
      @Local(argsOnly = true) class_10426 entityState,
      @Local(argsOnly = true) class_1799 stack,
      @Local(argsOnly = true) class_1306 arm
   ) {
      try {
         if (CsPickaxe.renderThirdPerson(stack, arm == class_1306.field_6182, matrices, queue, light)) {
            return;
         }

         CustomModels models = SoberClient.MODULES.get(CustomModels.class);
         class_811 context = arm == class_1306.field_6182 ? class_811.field_4323 : class_811.field_4320;
         class_573 pose = arm == class_1306.field_6182 ? entityState.field_55306 : entityState.field_55304;
         if (models != null && models.render(stack, context, matrices, queue, light, pose == class_573.field_63542)) {
            return;
         }
      } catch (RuntimeException var14) {
         SoberClient.LOG.error("Custom model failed", var14);
      }

      original.call(new Object[]{state, matrices, queue, light, overlay, outline});
   }
}
