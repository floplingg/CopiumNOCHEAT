package dev.violet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.gui.InventoryStyle;
import net.minecraft.class_1309;
import net.minecraft.class_332;
import net.minecraft.class_481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({class_481.class})
public class CreativeInventoryScreenMixin {
   @ModifyArg(
      method = {"method_2388"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V"
      ),
      index = 4,
      require = 0
   )
   private int violet$titleColor(int color) {
      return InventoryStyle.titleColor(color);
   }

   @WrapOperation(
      method = {"method_2389"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_490;method_2486(Lnet/minecraft/class_332;IIIIIFFFLnet/minecraft/class_1309;)V"
      )},
      require = 0
   )
   private void violet$deferEntity(
      class_332 context, int x1, int y1, int x2, int y2, int size, float scale, float mouseX, float mouseY, class_1309 entity, Operation<Void> original
   ) {
      Runnable draw = () -> original.call(new Object[]{context, x1, y1, x2, y2, size, scale, mouseX, mouseY, entity});
      if (!InventoryStyle.deferEntity(x1, y1, x2, y2, draw)) {
         draw.run();
      }
   }
}
