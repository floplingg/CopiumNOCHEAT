package dev.violet.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.function.Supplier;
import net.minecraft.class_1044;
import net.minecraft.class_380;
import net.minecraft.class_8538;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_380.class})
public abstract class GlyphAtlasTextureMixin extends class_1044 {
   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void violet$smoothFonts(Supplier<String> nameSupplier, class_8538 textRenderLayers, boolean hasColor, CallbackInfo ci) {
      String name = nameSupplier.get();
      if (name != null && name.startsWith("violet:")) {
         this.field_63613 = RenderSystem.getSamplerCache()
            .method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
      }
   }
}
