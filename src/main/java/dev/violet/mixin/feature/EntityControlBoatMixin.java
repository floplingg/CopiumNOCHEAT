package dev.violet.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.violet.modules.entitycontrol.EntityControlVehicleHook;
import net.minecraft.class_10255;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_10255.class})
public abstract class EntityControlBoatMixin {
   @ModifyExpressionValue(
      method = {"method_64482"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_10255;field_54444:Z",
         opcode = 180
      )}
   )
   private boolean violet$entityControlInputLeft(boolean original) {
      return original && !EntityControlVehicleHook.locksBoatTurning((class_10255)this);
   }

   @ModifyExpressionValue(
      method = {"method_64482"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_10255;field_54428:Z",
         opcode = 180
      )}
   )
   private boolean violet$entityControlInputRight(boolean original) {
      return original && !EntityControlVehicleHook.locksBoatTurning((class_10255)this);
   }
}
