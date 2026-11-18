package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.violet.event.impl.PlayerMoveEvent;
import net.minecraft.class_1313;
import net.minecraft.class_243;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({class_746.class})
public abstract class PlayerMoveEventsMixin {
   @WrapMethod(
      method = {"method_5784"}
   )
   private void violet$onMove(class_1313 type, class_243 movement, Operation<Void> original) {
      if (!PlayerMoveEvent.TYPE.hasListeners()) {
         original.call(new Object[]{type, movement});
      } else {
         PlayerMoveEvent.move(movement, changed -> original.call(new Object[]{type, changed}));
      }
   }
}
