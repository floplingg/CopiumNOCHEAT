package dev.violet.mixin;

import dev.violet.sky.SkyRenderer;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_758;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_758.class})
public abstract class FogRendererMixin {
   @Inject(
      method = {"method_62185"},
      at = {@At("RETURN")}
   )
   private void violet$matchSkyFog(CallbackInfoReturnable<Vector4f> cir) {
      float[] fog;
      try {
         fog = SkyRenderer.fogColor();
      } catch (RuntimeException var7) {
         return;
      }

      if (fog != null) {
         class_4184 camera = class_310.method_1551().field_1773.method_19418();
         class_5636 fluid = camera.method_19334();
         if (fluid != class_5636.field_27886 && fluid != class_5636.field_27885 && fluid != class_5636.field_27887) {
            if (camera.method_19331() instanceof class_1309 living && (living.method_6059(class_1294.field_5919) || living.method_6059(class_1294.field_38092))
               )
             {
               return;
            }

            Vector4f color = (Vector4f)cir.getReturnValue();
            color.set(fog[0], fog[1], fog[2], color.w);
         }
      }
   }
}
