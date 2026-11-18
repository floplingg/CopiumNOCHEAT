package dev.violet.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_310.class})
public interface MinecraftClientAccessor {
   @Accessor("field_1752")
   int violet$getItemUseCooldown();

   @Accessor("field_1752")
   void violet$setItemUseCooldown(int var1);

   @Invoker("method_1536")
   boolean violet$doAttack();
}
