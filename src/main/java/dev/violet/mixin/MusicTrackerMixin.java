package dev.violet.mixin;

import dev.violet.gui.MenuAudio;
import net.minecraft.class_1142;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1142.class})
public class MusicTrackerMixin {
   @Inject(
      method = {"method_18669"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$menuAudio(CallbackInfo ci) {
      if (MenuAudio.playing()) {
         ci.cancel();
      }
   }
}
