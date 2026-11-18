package dev.violet.mixin;

import dev.violet.event.impl.Render2DEvent;
import dev.violet.gui.OverlayCanvas;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public abstract class HudEventsMixin {
   @Inject(
      method = {"method_1753"},
      at = {@At("TAIL")},
      require = 0
   )
   private void violet$renderHudEvent(class_332 context, class_9779 counter, CallbackInfo ci) {
      Render2DEvent.dispatch(context, counter.method_60637(false));
      OverlayCanvas.renderProviders(context);
   }
}
