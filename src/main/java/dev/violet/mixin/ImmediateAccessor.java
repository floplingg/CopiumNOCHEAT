package dev.violet.mixin;

import java.util.SequencedMap;
import net.minecraft.class_1921;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_4598.class})
public interface ImmediateAccessor {
   @Accessor("field_20953")
   SequencedMap<class_1921, class_9799> violet$layerBuffers();
}
