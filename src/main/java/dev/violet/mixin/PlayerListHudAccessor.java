package dev.violet.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_355;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_355.class})
public interface PlayerListHudAccessor {
   @Accessor("field_2153")
   class_2561 violet$getHeader();

   @Accessor("field_2154")
   class_2561 violet$getFooter();
}
