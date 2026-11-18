package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.CsPickaxe;
import dev.violet.modules.CustomModels;
import dev.violet.modules.SwingAnimations;
import net.minecraft.class_11659;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_759.class})
public abstract class HeldItemRendererMixin {
   @Shadow
   private class_1799 field_4047;
   @Shadow
   private float field_4043;
   @Shadow
   private float field_4053;

   @Inject(
      method = {"method_3220"},
      at = {@At("TAIL")},
      require = 0
   )
   private void violet$instantKnife(CallbackInfo ci) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null) {
         class_1799 held = player.method_6047();
         if (CsPickaxe.instantSwap(held)) {
            this.field_4047 = held;
            this.field_4043 = 1.0F;
            this.field_4053 = 1.0F;
         }
      }
   }

   private static SwingAnimations violet$module() {
      SwingAnimations module = SoberClient.MODULES.get(SwingAnimations.class);
      return module != null && module.isActive() ? module : null;
   }

   @Inject(
      method = {"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"},
      at = {@At("HEAD")},
      require = 0
   )
   private void violet$handsBegin(float tickProgress, class_4587 matrices, class_11659 queue, class_746 player, int light, CallbackInfo ci) {
      CsPickaxe.handsBegin(matrices);
   }

   @Inject(
      method = {"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$customModel(class_1309 entity, class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      try {
         if (CsPickaxe.renderFirstPerson(entity, stack, context, matrices, queue, light)) {
            ci.cancel();
            return;
         }

         CustomModels models = SoberClient.MODULES.get(CustomModels.class);
         if (models != null && models.render(stack, context, matrices, queue, light)) {
            ci.cancel();
         }
      } catch (RuntimeException var9) {
         SoberClient.LOG.error("Custom model failed", var9);
      }
   }

   @Inject(
      method = {"method_65816"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$swing(float swingProgress, class_4587 matrices, int side, class_1306 arm, CallbackInfo ci) {
      SwingAnimations module = violet$module();
      if (module != null && module.swing(matrices, swingProgress, side)) {
         ci.cancel();
      }
   }

   @ModifyVariable(
      method = {"method_3224"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0,
      require = 0
   )
   private float violet$equip(float equipProgress) {
      SwingAnimations module = violet$module();
      return module != null && module.noEquip.get() ? 0.0F : equipProgress;
   }

   @Inject(
      method = {"method_3224"},
      at = {@At("TAIL")},
      require = 0
   )
   private void violet$offset(class_4587 matrices, class_1306 arm, float equipProgress, CallbackInfo ci) {
      SwingAnimations module = violet$module();
      if (module != null) {
         module.offset(matrices);
      }
   }
}
