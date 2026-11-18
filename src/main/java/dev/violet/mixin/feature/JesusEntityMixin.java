package dev.violet.mixin.feature;

import dev.violet.modules.Jesus;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class})
public abstract class JesusEntityMixin {
   @Inject(
      method = {"method_5700"},
      at = {@At("HEAD")}
   )
   private void violet$jesusAboveBubbleColumn(CallbackInfo ci) {
      this.violet$jesusMarkBubbleColumn();
   }

   @Inject(
      method = {"method_5764"},
      at = {@At("HEAD")}
   )
   private void violet$jesusInsideBubbleColumn(CallbackInfo ci) {
      this.violet$jesusMarkBubbleColumn();
   }

   @Unique
   private void violet$jesusMarkBubbleColumn() {
      Jesus jesus = Jesus.active;
      if (jesus != null && this == class_310.method_1551().field_1724) {
         jesus.markInBubbleColumn();
      }
   }
}
