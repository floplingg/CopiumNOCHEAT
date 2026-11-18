package dev.violet.mixin;

import net.minecraft.class_10185;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_746.class})
public interface ClientPlayerStateAccessor {
   @Accessor("field_3919")
   boolean violet$wasSprinting();

   @Accessor("field_54156")
   class_10185 violet$lastSentInput();
}
