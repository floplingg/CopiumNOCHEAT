package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.violet.gui.ScreenStyle;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4267.class_4270;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_4270.class})
public abstract class ServerEntryMixin {
   @WrapOperation(
      method = {"method_25343"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_25294(IIIII)V"
      )},
      require = 0
   )
   private void violet$overlay(class_332 context, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
      if (ScreenStyle.screens() && x2 - x1 == 32 && y2 - y1 == 32) {
         ScreenStyle.serverIconOverlay(context, x1, y1);
      } else {
         original.call(new Object[]{context, x1, y1, x2, y2, color});
      }
   }

   @WrapOperation(
      method = {"method_25343"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"
      )},
      require = 0
   )
   private void violet$sprites(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, Operation<Void> original) {
      String path = sprite.method_12832();
      if (ScreenStyle.screens() && width == 32 && path.startsWith("server_list/") && (path.contains("join") || path.contains("move_"))) {
         ScreenStyle.serverIconSprite(context, x, y, path);
      } else {
         original.call(new Object[]{context, pipeline, sprite, x, y, width, height});
      }
   }
}
