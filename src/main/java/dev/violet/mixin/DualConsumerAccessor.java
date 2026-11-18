package dev.violet.mixin;

import net.minecraft.class_4588;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   targets = {"net/minecraft/class_4720$class_4589"}
)
public interface DualConsumerAccessor {
   @Accessor("field_21685")
   class_4588 violet$first();

   @Accessor("field_21686")
   class_4588 violet$second();
}
