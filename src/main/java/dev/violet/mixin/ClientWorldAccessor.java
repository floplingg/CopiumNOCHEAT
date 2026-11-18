package dev.violet.mixin;

import net.minecraft.class_638;
import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_638.class})
public interface ClientWorldAccessor {
   @Accessor("field_21526")
   int violet$atmosphereSkyFlash();

   @Accessor("field_37951")
   class_7202 violet$blockStatePredictionHandler();
}
