package dev.violet.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_636.class})
public interface InteractionManagerAccessor {
   @Invoker("method_2911")
   void violet$elytraFlySyncSelected();

   @Accessor("field_3715")
   float violet$breakingProgress();

   @Accessor("field_3715")
   void violet$setBreakingProgress(float var1);

   @Accessor("field_3716")
   void violet$setBreakingCooldown(int var1);

   @Accessor("field_3714")
   class_2338 violet$breakingPos();

   @Accessor("field_3717")
   boolean violet$breaking();
}
