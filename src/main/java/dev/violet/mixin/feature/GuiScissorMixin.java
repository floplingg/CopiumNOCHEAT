package dev.violet.mixin.feature;

import com.mojang.blaze3d.systems.RenderPass;
import dev.violet.util.FractionalGuiScale;
import net.minecraft.class_11228;
import net.minecraft.class_310;
import net.minecraft.class_8030;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_11228.class})
public abstract class GuiScissorMixin {
   @Inject(
      method = {"method_70884(Lnet/minecraft/class_8030;Lcom/mojang/blaze3d/systems/RenderPass;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$fractionalScissor(class_8030 rect, RenderPass pass, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      double scale = FractionalGuiScale.uncorrectedScale(mc);
      if (!(scale <= 0.0)) {
         int height = mc.method_22683().method_4506();
         int left = (int)Math.round((double)rect.method_49620() * scale);
         int bottom = (int)Math.round((double)height - (double)rect.method_49619() * scale);
         int width = Math.max(0, (int)Math.round((double)rect.comp_1196() * scale));
         int tall = Math.max(0, (int)Math.round((double)rect.comp_1197() * scale));
         pass.enableScissor(left, bottom, width, tall);
         ci.cancel();
      }
   }
}
