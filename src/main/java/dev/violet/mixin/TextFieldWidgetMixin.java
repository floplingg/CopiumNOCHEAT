package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.violet.gui.ScreenStyle;
import dev.violet.modules.AnimatedTyping;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_5481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_342.class})
public abstract class TextFieldWidgetMixin {
   @Shadow
   private String field_2092;
   @Shadow
   private int field_2103;
   @Shadow
   private int field_2102;
   @Unique
   private String violet$lastText;
   @Unique
   private long[] violet$times;
   @Unique
   private float violet$caret = 0.0F / 0.0F;
   @Unique
   private long violet$caretFrame;

   @WrapOperation(
      method = {"method_48579"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"
      )},
      require = 0
   )
   private void violet$field(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, Operation<Void> original) {
      if (!ScreenStyle.widgets()) {
         original.call(new Object[]{context, pipeline, sprite, x, y, width, height});
      } else {
         ScreenStyle.textField(context, this, x, y, width, height, ((class_342)this).method_25370());
      }
   }

   @Inject(
      method = {"method_48579"},
      at = {@At("HEAD")},
      require = 0
   )
   private void violet$trackTyping(class_332 context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
      this.violet$times = AnimatedTyping.track(this.violet$lastText, this.field_2092, this.violet$times, AnimatedTyping.applies((class_342)this));
      this.violet$lastText = this.field_2092;
   }

   @WrapOperation(
      method = {"method_48579"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_51430(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;IIIZ)V",
         ordinal = 0
      )},
      require = 0
   )
   private void violet$typedBefore(class_332 context, class_327 renderer, class_5481 value, int x, int y, int color, boolean shadow, Operation<Void> original) {
      if (this.violet$times != null && AnimatedTyping.applies((class_342)this)) {
         AnimatedTyping.draw(context, renderer, value, x, y, color, shadow, this.violet$times, this.field_2103);
      } else {
         original.call(new Object[]{context, renderer, value, x, y, color, shadow});
      }
   }

   @WrapOperation(
      method = {"method_48579"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_51430(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;IIIZ)V",
         ordinal = 1
      )},
      require = 0
   )
   private void violet$typedAfter(class_332 context, class_327 renderer, class_5481 value, int x, int y, int color, boolean shadow, Operation<Void> original) {
      if (this.violet$times != null && AnimatedTyping.applies((class_342)this)) {
         AnimatedTyping.draw(context, renderer, value, x, y, color, shadow, this.violet$times, this.field_2102);
      } else {
         original.call(new Object[]{context, renderer, value, x, y, color, shadow});
      }
   }

   @WrapOperation(
      method = {"method_48579"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_25294(IIIII)V"
      )},
      require = 0
   )
   private void violet$cursorBar(class_332 context, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
      float offset = this.violet$glide(x1);
      if (offset == 0.0F) {
         original.call(new Object[]{context, x1, y1, x2, y2, color});
      } else {
         context.method_51448().pushMatrix();
         context.method_51448().translate(offset, 0.0F);
         original.call(new Object[]{context, x1, y1, x2, y2, color});
         context.method_51448().popMatrix();
      }
   }

   @WrapOperation(
      method = {"method_48579"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_51433(Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)V",
         ordinal = 1
      )},
      require = 0
   )
   private void violet$cursorEnd(class_332 context, class_327 renderer, String value, int x, int y, int color, boolean shadow, Operation<Void> original) {
      float offset = this.violet$glide(x);
      if (offset == 0.0F) {
         original.call(new Object[]{context, renderer, value, x, y, color, shadow});
      } else {
         context.method_51448().pushMatrix();
         context.method_51448().translate(offset, 0.0F);
         original.call(new Object[]{context, renderer, value, x, y, color, shadow});
         context.method_51448().popMatrix();
      }
   }

   @Unique
   private float violet$glide(int target) {
      if (AnimatedTyping.applies((class_342)this) && AnimatedTyping.smoothCursor()) {
         long now = class_156.method_658();
         this.violet$caret = AnimatedTyping.glide(this.violet$caret, (float)target, this.violet$caretFrame, now);
         this.violet$caretFrame = now;
         return this.violet$caret - (float)target;
      } else {
         this.violet$caret = 0.0F / 0.0F;
         return 0.0F;
      }
   }
}
