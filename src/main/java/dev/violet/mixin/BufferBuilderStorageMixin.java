package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.sky.GlintCover;
import java.util.SequencedMap;
import net.minecraft.class_1921;
import net.minecraft.class_4599;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4599.class})
public abstract class BufferBuilderStorageMixin {
   @Shadow
   @Final
   private class_4598 field_46901;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")},
      require = 0
   )
   private void violet$glintBuffers(int threads, CallbackInfo ci) {
      try {
         SequencedMap<class_1921, class_9799> buffers = ((ImmediateAccessor)this.field_46901).violet$layerBuffers();

         for (class_1921 layer : GlintCover.layers()) {
            buffers.putIfAbsent(layer, new class_9799(layer.method_22722()));
         }
      } catch (RuntimeException var6) {
         SoberClient.LOG.warn("Could not reserve buffers for the covering glint", var6);
      }
   }
}
