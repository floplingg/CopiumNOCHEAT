package dev.violet.mixin;

import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_4970.class})
public interface BlockCollisionAccessor {
   @Accessor("field_23159")
   boolean violet$elytraFlyHasCollision();
}
