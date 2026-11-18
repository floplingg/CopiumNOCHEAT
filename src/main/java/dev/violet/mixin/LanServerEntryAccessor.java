package dev.violet.mixin;

import net.minecraft.class_1131;
import net.minecraft.class_4267.class_4269;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_4269.class})
public interface LanServerEntryAccessor {
   @Accessor("field_19114")
   class_1131 violet$server();
}
