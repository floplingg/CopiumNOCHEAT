package dev.violet.mixin;

import dev.violet.modules.NameTags;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_355.class})
public abstract class PlayerListHudMixin {
   @Inject(
      method = {"method_1918"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void violet$fakeRole(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
      if (NameTags.isOwn(entry)) {
         cir.setReturnValue(NameTags.decorateText((class_2561)cir.getReturnValue()));
      }
   }
}
