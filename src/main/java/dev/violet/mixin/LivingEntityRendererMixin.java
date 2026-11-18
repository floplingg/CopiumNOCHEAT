package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.modules.CustomSkin;
import dev.violet.modules.Glass;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1058;
import net.minecraft.class_11659;
import net.minecraft.class_1921;
import net.minecraft.class_3879;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import net.minecraft.class_979;
import net.minecraft.class_989;
import net.minecraft.class_11683.class_11792;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_922.class})
public abstract class LivingEntityRendererMixin {
   @WrapOperation(
      method = {"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"
      )}
   )
   private void violet$customSkin(
      class_11659 queue,
      class_3879<?> model,
      Object state,
      class_4587 matrices,
      class_1921 layer,
      int light,
      int overlay,
      int tint,
      class_1058 sprite,
      int outline,
      class_11792 crumbling,
      Operation<Void> original
   ) {
      if (state instanceof class_10055 player && Glass.applies(player)) {
         original.call(new Object[]{queue, model, state, matrices, Glass.layer(player), light, overlay, Glass.tint(), sprite, outline, crumbling});
         return;
      }

      if (state instanceof class_10055 player && CustomSkin.render(player, matrices, queue, overlay)) {
         return;
      }

      original.call(new Object[]{queue, model, state, matrices, layer, light, overlay, tint, sprite, outline, crumbling});
   }

   @WrapOperation(
      method = {"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_922;method_62483(Lnet/minecraft/class_10042;)Z"
      )}
   )
   private boolean violet$skinFeatures(class_922<?, ?, ?> renderer, class_10042 state, Operation<Boolean> original) {
      return CustomSkin.hidesFeatures(state) && !CustomSkin.showsElytra() ? false : (Boolean)original.call(new Object[]{renderer, state});
   }

   @WrapOperation(
      method = {"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_3887;method_4199(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10017;FF)V"
      )}
   )
   private void violet$skinFeature(
      class_3887<?, ?> feature,
      class_4587 matrices,
      class_11659 queue,
      int light,
      class_10017 state,
      float limbAngle,
      float limbDistance,
      Operation<Void> original
   ) {
      boolean glass = !(feature instanceof class_989) && Glass.glassGear(state);
      Glass.gear(glass);

      try {
         if (!CustomSkin.hidesFeatures(state)) {
            original.call(new Object[]{feature, matrices, queue, light, state, limbAngle, limbDistance});
            return;
         }

         if (!(feature instanceof class_979) || !CustomSkin.showsElytra()) {
            return;
         }

         matrices.method_22903();
         CustomSkin.elytraOffset(matrices);
         original.call(new Object[]{feature, matrices, queue, light, state, limbAngle, limbDistance});
         matrices.method_22909();
      } finally {
         if (glass) {
            Glass.gear(false);
         }
      }
   }
}
