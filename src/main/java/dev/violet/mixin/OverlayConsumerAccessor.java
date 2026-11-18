package dev.violet.mixin;

import net.minecraft.class_4583;
import net.minecraft.class_4588;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_4583.class})
public interface OverlayConsumerAccessor {
   @Accessor("field_20866")
   class_4588 violet$delegate();
}
