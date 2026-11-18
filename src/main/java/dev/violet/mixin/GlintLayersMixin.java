package dev.violet.mixin;

import dev.violet.sky.GlintCover;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_12249.class})
public abstract class GlintLayersMixin {
   @Inject(
      method = {"method_75995"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$glint(CallbackInfoReturnable<class_1921> cir) {
      class_1921 layer = GlintCover.glint();
      if (layer != null) {
         cir.setReturnValue(layer);
      }
   }

   @Inject(
      method = {"method_75993"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$glintTranslucent(CallbackInfoReturnable<class_1921> cir) {
      class_1921 layer = GlintCover.translucent();
      if (layer != null) {
         cir.setReturnValue(layer);
      }
   }

   @Inject(
      method = {"method_75997"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$entityGlint(CallbackInfoReturnable<class_1921> cir) {
      class_1921 layer = GlintCover.entity();
      if (layer != null) {
         cir.setReturnValue(layer);
      }
   }

   @Inject(
      method = {"method_75989"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void violet$armorEntityGlint(CallbackInfoReturnable<class_1921> cir) {
      class_1921 layer = GlintCover.armor();
      if (layer != null) {
         cir.setReturnValue(layer);
      }
   }
}
