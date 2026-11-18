package dev.violet.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.violet.event.impl.Render3DEvent;
import dev.violet.util.ShaderOverlay;
import dev.violet.util.TracerEngine;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import net.minecraft.class_5636;
import net.minecraft.class_761;
import net.minecraft.class_12180.class_12181;
import net.minecraft.class_758.class_4596;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_761.class})
public abstract class WorldEventsMixin {
   @ModifyArg(
      method = {"method_22710"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_863;method_23099(Lnet/minecraft/class_4604;DDDF)V"
      ),
      index = 0,
      require = 0
   )
   private class_4604 violet$renderWorldEvent(
      class_4604 frustum, @Local(argsOnly = true,ordinal = 0) Matrix4f viewRotation, @Local(argsOnly = true,ordinal = 1) Matrix4f projection
   ) {
      TracerEngine.beginFrame(viewRotation, projection);
      class_12181 capture = ShaderOverlay.capture();

      try {
         Render3DEvent.dispatch(class_310.method_1551().method_61966().method_60637(false), frustum);
      } catch (Throwable var8) {
         if (capture != null) {
            try {
               capture.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (capture != null) {
         capture.close();
      }

      return frustum;
   }

   @Inject(
      method = {"method_22710"},
      at = {@At("TAIL")},
      require = 0
   )
   private void violet$overlayAfterShaderPack(
      CallbackInfo ci,
      @Local(argsOnly = true) GpuBufferSlice fog,
      @Local(argsOnly = true) class_4184 camera,
      @Local(argsOnly = true,ordinal = 0) Matrix4f viewRotation,
      @Local(argsOnly = true,ordinal = 1) Matrix4f projection
   ) {
      if (ShaderOverlay.pending()) {
         ShaderOverlay.render(camera, viewRotation, projection, fog, violet$onTopFog(fog, camera));
      }
   }

   @Unique
   private static GpuBufferSlice violet$onTopFog(GpuBufferSlice fog, class_4184 camera) {
      return camera.method_19334() == class_5636.field_27888
         ? fog
         : ((GameRendererAccessor)class_310.method_1551().field_1773).violet$getFogRenderer().method_71109(class_4596.field_60101);
   }
}
