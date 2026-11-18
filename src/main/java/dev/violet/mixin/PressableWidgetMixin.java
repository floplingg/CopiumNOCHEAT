package dev.violet.mixin;

import dev.violet.gui.ScreenStyle;
import net.minecraft.class_12225;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_4264;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4264.class})
public abstract class PressableWidgetMixin extends class_339 {
   public PressableWidgetMixin(int x, int y, int width, int height, class_2561 message) {
      super(x, y, width, height, message);
   }

   @Inject(
      method = {"method_75794"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$button(class_332 context, CallbackInfo ci) {
      if (ScreenStyle.widgets()) {
         ScreenStyle.button(
            context,
            this,
            this.method_46426(),
            this.method_46427(),
            this.method_25368(),
            this.method_25364(),
            this.field_22763,
            this.method_25367(),
            this.field_22765
         );

         try {
            ScreenStyle.label(
               context,
               this,
               this.method_25369(),
               this.method_46426(),
               this.method_46427(),
               this.method_25368(),
               this.method_25364(),
               this.field_22763,
               this.field_22765
            );
         } catch (RuntimeException var4) {
            ScreenStyle.consumeLabel();
         }

         ci.cancel();
      }
   }

   @Inject(
      method = {"method_75793"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$label(class_12225 consumer, CallbackInfo ci) {
      if (ScreenStyle.consumeLabel()) {
         ci.cancel();
      }
   }
}
