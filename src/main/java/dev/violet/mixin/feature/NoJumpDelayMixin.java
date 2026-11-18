package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.violet.modules.NoJumpDelay;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1309.class})
public abstract class NoJumpDelayMixin {
   @Shadow
   private int field_6228;

   @WrapOperation(
      method = {"method_6007"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_1309;field_6228:I",
         opcode = 181
      )}
   )
   private void violet$noJumpDelay(class_1309 self, int ticks, Operation<Void> original) {
      NoJumpDelay module = NoJumpDelay.active;
      if (module != null && ticks > this.field_6228 && self == class_310.method_1551().field_1724) {
         ticks = module.rearm(ticks);
      }

      original.call(new Object[]{self, ticks});
   }
}
