package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.CustomModels;
import net.minecraft.class_10799;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_332.class})
public abstract class DrawContextMixin {
   @Inject(
      method = {"method_51425(Lnet/minecraft/class_1309;Lnet/minecraft/class_1937;Lnet/minecraft/class_1799;III)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$modelIcon(class_1309 entity, class_1937 world, class_1799 stack, int x, int y, int seed, CallbackInfo ci) {
      if (!stack.method_7960()) {
         try {
            CustomModels models = SoberClient.MODULES.get(CustomModels.class);
            class_2960 icon = models == null ? null : models.icon(stack);
            if (icon == null) {
               return;
            }

            class_332 context = (class_332)this;
            context.method_25302(class_10799.field_56883, icon, x, y, 0.0F, 0.0F, 16, 16, 64, 64, 64, 64);
            ci.cancel();
         } catch (RuntimeException var11) {
            SoberClient.LOG.error("Custom model icon failed", var11);
         }
      }
   }
}
