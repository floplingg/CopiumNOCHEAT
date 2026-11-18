package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.violet.modules.CustomCrosshair;
import dev.violet.modules.FakeStats;
import dev.violet.modules.MediaHider;
import dev.violet.modules.NameTags;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public abstract class InGameHudMixin {
   @Unique
   private int violet$drawIndex;
   @Unique
   private int violet$widthIndex;

   @WrapOperation(
      method = {"method_1736"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"
      )},
      require = 0
   )
   private void violet$crosshair(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, Operation<Void> original) {
      if (!sprite.method_12832().equals("hud/crosshair") || !CustomCrosshair.render(context)) {
         if (!sprite.method_12832().startsWith("hud/crosshair_attack_indicator") || !CustomCrosshair.hidesVanillaIndicator()) {
            original.call(new Object[]{context, pipeline, sprite, x, y, width, height});
         }
      }
   }

   @WrapOperation(
      method = {"method_1736"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_70846(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIIIIII)V"
      )},
      require = 0
   )
   private void violet$crosshairProgress(
      class_332 context,
      RenderPipeline pipeline,
      class_2960 sprite,
      int textureWidth,
      int textureHeight,
      int u,
      int v,
      int x,
      int y,
      int width,
      int height,
      Operation<Void> original
   ) {
      if (!sprite.method_12832().startsWith("hud/crosshair_attack_indicator") || !CustomCrosshair.hidesVanillaIndicator()) {
         original.call(new Object[]{context, pipeline, sprite, textureWidth, textureHeight, u, v, x, y, width, height});
      }
   }

   @Inject(
      method = {"method_1736"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$freeCameraCrosshair(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      class_310 client = class_310.method_1551();
      if (!client.field_1690.method_31044().method_31034() && CustomCrosshair.showsInFreeCamera()) {
         if (CustomCrosshair.render(context)) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_1749"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$hideHeldName(class_332 context, CallbackInfo ci) {
      if (MediaHider.hidesHeldName()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"},
      at = {@At("HEAD")}
   )
   private void violet$sidebarStart(class_332 context, class_266 objective, CallbackInfo ci) {
      this.violet$drawIndex = 0;
      this.violet$widthIndex = 0;
      FakeStats.beginSidebar();
   }

   @WrapOperation(
      method = {"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V"
      )}
   )
   private void violet$sidebarRole(class_332 context, class_327 renderer, class_2561 text, int x, int y, int color, boolean shadow, Operation<Void> original) {
      int index = this.violet$drawIndex++;
      if (index == 0) {
         original.call(new Object[]{context, renderer, NameTags.decorateText(text), x, y, color, shadow});
      } else {
         int line = (index - 1) / 2 + 1;
         if ((index - 1) % 2 == 1) {
            class_2561 score = FakeStats.applyScore(line, text);
            int shift = score == text ? 0 : renderer.method_27525(text) - renderer.method_27525(score);
            original.call(new Object[]{context, renderer, score, x + shift, y, color, shadow});
         } else {
            original.call(new Object[]{context, renderer, NameTags.decorateText(FakeStats.applyLine(line, text)), x, y, color, shadow});
         }
      }
   }

   @WrapOperation(
      method = {"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_327;method_27525(Lnet/minecraft/class_5348;)I"
      )}
   )
   private int violet$sidebarWidth(class_327 renderer, class_5348 text, Operation<Integer> original) {
      int index = this.violet$widthIndex++;
      if (text instanceof class_2561 value) {
         return index == 0
            ? (Integer)original.call(new Object[]{renderer, NameTags.decorateText(value)})
            : (Integer)original.call(new Object[]{renderer, NameTags.decorateText(FakeStats.applyLine(index, value))});
      } else {
         return (Integer)original.call(new Object[]{renderer, text});
      }
   }
}
