package dev.violet.mixin;

import dev.violet.render.GlassGear;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_12249.class})
public abstract class GlassGearMixin {
   @Inject(
      method = {"method_75966(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75978(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75982(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75990(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_75994(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", "method_76000(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$glassGear(class_2960 texture, CallbackInfoReturnable<class_1921> cir) {
      class_1921 layer = GlassGear.layer(texture);
      if (layer != null) {
         cir.setReturnValue(layer);
      }
   }

   @Inject(
      method = {"method_75969(Lnet/minecraft/class_2960;Z)Lnet/minecraft/class_1921;", "method_75980(Lnet/minecraft/class_2960;Z)Lnet/minecraft/class_1921;"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$glassGearOutline(class_2960 texture, boolean affectsOutline, CallbackInfoReturnable<class_1921> cir) {
      class_1921 layer = GlassGear.layer(texture);
      if (layer != null) {
         cir.setReturnValue(layer);
      }
   }
}
