package dev.violet.mixin;

import net.minecraft.class_8573;
import net.minecraft.class_4267.class_4270;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_4270.class})
public interface ServerEntryAccessor {
   @Accessor("field_19123")
   class_8573 violet$icon();

   @Accessor("field_42874")
   byte[] violet$favicon();
}
