package dev.violet.mixin;

import net.minecraft.class_339;
import net.minecraft.class_9110;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_339.class})
public interface ClickableWidgetAccessor {
   @Accessor("field_41095")
   class_9110 violet$tooltip();
}
