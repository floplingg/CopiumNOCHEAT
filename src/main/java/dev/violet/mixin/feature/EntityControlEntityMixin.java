package dev.violet.mixin.feature;

import dev.violet.modules.entitycontrol.EntityControlVehicleHook;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_1297.class})
public abstract class EntityControlEntityMixin {
   @ModifyVariable(
      method = {"method_5784"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private class_243 violet$entityControlVehicleMove(class_243 movement, class_1313 type) {
      return !EntityControlVehicleHook.anyActive ? movement : EntityControlVehicleHook.onEntityMove((class_1297)this, type, movement);
   }
}
