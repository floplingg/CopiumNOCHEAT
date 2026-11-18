package dev.violet.mixin;

import java.util.Map;
import net.minecraft.class_1007;
import net.minecraft.class_742;
import net.minecraft.class_7920;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_898.class})
public interface EntityRenderManagerAccessor {
   @Accessor("field_4687")
   Map<class_7920, class_1007<class_742>> violet$skinChangePlayerRenderers();
}
