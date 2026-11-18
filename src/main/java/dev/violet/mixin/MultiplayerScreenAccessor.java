package dev.violet.mixin;

import net.minecraft.class_437;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_500.class})
public interface MultiplayerScreenAccessor {
   @Accessor("field_3049")
   class_437 violet$parent();

   @Invoker("method_2534")
   void violet$refresh();
}
