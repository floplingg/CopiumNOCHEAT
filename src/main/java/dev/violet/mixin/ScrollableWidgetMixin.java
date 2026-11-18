package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.violet.gui.ScreenStyle;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_7528;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_7528.class})
public abstract class ScrollableWidgetMixin {
   @WrapOperation(
      method = {"method_44396"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"
      )},
      require = 0
   )
   private void violet$scrollbar(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, Operation<Void> original) {
      if (!ScreenStyle.widgets()) {
         original.call(new Object[]{context, pipeline, sprite, x, y, width, height});
      } else {
         if (sprite.method_12832().contains("background")) {
            ScreenStyle.scrollTrack(context, x, y, width, height);
         } else {
            ScreenStyle.scrollThumb(context, x, y, width, height);
         }
      }
   }
}
