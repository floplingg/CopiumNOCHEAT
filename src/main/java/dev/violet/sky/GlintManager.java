package dev.violet.sky;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.violet.SoberClient;
import dev.violet.mixin.DualConsumerAccessor;
import dev.violet.mixin.OverlayConsumerAccessor;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1044;
import net.minecraft.class_1059;
import net.minecraft.class_1060;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_918;

public final class GlintManager {
   private static final float ITEM_SCALE = 8.0F;
   private static final float ENTITY_SCALE = 0.5F;
   private static final float ARMOR_SCALE = 0.16F;
   private static final float REFERENCE_ATLAS = 1024.0F;
   private static final float SPRITE_SPAN = 0.015625F;
   private static GlintType current = GlintType.VANILLA;
   private static boolean fitted;
   private static GpuTexture cachedAtlas;
   private static float cachedWidth = 1024.0F;

   private GlintManager() {
   }

   public static GlintType current() {
      return current;
   }

   public static class_4588 fit(class_4588 consumer) {
      if (current != GlintType.VANILLA && consumer instanceof DualConsumerAccessor dual) {
         try {
            class_4588 glint = dual.violet$first();
            if (glint instanceof OverlayConsumerAccessor overlay) {
               glint = overlay.violet$delegate();
            }

            class_4588 base = dual.violet$second();
            if (glint != null && base != null) {
               fitted = true;
               return new GlintFit(glint, base);
            } else {
               return consumer;
            }
         } catch (RuntimeException var4) {
            return consumer;
         }
      } else {
         return consumer;
      }
   }

   public static float scale(float vanilla) {
      if (current != GlintType.VANILLA && current.shader() == null) {
         try {
            float scale = vanilla * current.scale();
            if (vanilla == 8.0F) {
               return fitted ? scale * 0.015625F : scale * atlasWidth() / 1024.0F;
            } else if (vanilla == 0.5F) {
               return scale * 4.0F;
            } else {
               return vanilla == 0.16F ? scale * 6.0F : scale;
            }
         } catch (RuntimeException var2) {
            return vanilla;
         }
      } else {
         return vanilla;
      }
   }

   public static void ensure() {
      if (current.texture() != null) {
         class_1060 textures = class_310.method_1551().method_1531();
         if (textures != null) {
            if (!(textures.method_4619(class_918.field_43087) instanceof GlintManager.GlintTexture)
               || !(textures.method_4619(class_918.field_43086) instanceof GlintManager.GlintTexture)) {
               GlintType wanted = current;
               current = GlintType.VANILLA;

               try {
                  apply(wanted);
               } catch (RuntimeException var3) {
                  SoberClient.LOG.warn("Could not restore glint {}", wanted, var3);
               }
            }
         }
      }
   }

   public static float atlasWidth() {
      class_1044 atlas = class_310.method_1551().method_1531().method_4619(class_1059.field_64467);

      try {
         GpuTexture texture = atlas.method_68004();
         if (texture != cachedAtlas) {
            cachedAtlas = texture;
            cachedWidth = (float)texture.getWidth(0);
         }
      } catch (IllegalStateException var2) {
         cachedAtlas = null;
         cachedWidth = 1024.0F;
      }

      return cachedWidth;
   }

   public static void apply(GlintType wanted) {
      class_310 mc = class_310.method_1551();
      class_1060 textures = mc.method_1531();
      if (textures != null) {
         if (wanted.texture() == null) {
            if (current.texture() != null) {
               textures.method_4615(class_918.field_43087);
               textures.method_4615(class_918.field_43086);
            }

            current = wanted;
         } else {
            class_1011 item = load(wanted);
            class_1011 armor = load(wanted);
            if (item != null && armor != null) {
               textures.method_4616(class_918.field_43087, new GlintManager.GlintTexture(item));
               textures.method_4616(class_918.field_43086, new GlintManager.GlintTexture(armor));
               current = wanted;
            } else {
               if (item != null) {
                  item.close();
               }

               if (armor != null) {
                  armor.close();
               }
            }
         }
      }
   }

   private static class_1011 load(GlintType type) {
      Optional<Path> path = FabricLoader.getInstance()
         .getModContainer("soberclient")
         .flatMap(mod -> mod.findPath("assets/violet/textures/glint/" + type.texture() + ".png"));
      if (path.isEmpty()) {
         return null;
      } else {
         try {
            class_1011 var3;
            try (InputStream in = Files.newInputStream(path.get())) {
               var3 = class_1011.method_4309(in);
            }

            return var3;
         } catch (Exception var7) {
            SoberClient.LOG.warn("Could not load glint {}", type, var7);
            return null;
         }
      }
   }

   private static final class GlintTexture extends class_1044 {
      GlintTexture(class_1011 image) {
         int levels = Math.max(1, 31 - Integer.numberOfLeadingZeros(Math.min(image.method_4307(), image.method_4323())) - 2);
         GpuDevice device = RenderSystem.getDevice();
         this.field_56974 = device.createTexture(() -> "violet_glint", 5, TextureFormat.RGBA8, image.method_4307(), image.method_4323(), 1, levels);
         this.field_60597 = device.createTextureView(this.field_56974);
         this.field_63613 = RenderSystem.getSamplerCache().method_75293(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, true);
         CommandEncoder encoder = device.createCommandEncoder();
         class_1011 level = image;

         for (int i = 0; i < levels; i++) {
            encoder.writeToTexture(this.field_56974, level, i, 0, 0, 0, level.method_4307(), level.method_4323(), 0, 0);
            if (i + 1 < levels) {
               class_1011 next = half(level);
               level.close();
               level = next;
            }
         }

         level.close();
      }

      private static class_1011 half(class_1011 src) {
         int w = Math.max(1, src.method_4307() / 2);
         int h = Math.max(1, src.method_4323() / 2);
         class_1011 out = new class_1011(w, h, false);

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               int a = src.method_61940(x * 2, y * 2);
               int b = src.method_61940(x * 2 + 1, y * 2);
               int c = src.method_61940(x * 2, y * 2 + 1);
               int d = src.method_61940(x * 2 + 1, y * 2 + 1);
               int color = 0;

               for (int shift = 0; shift <= 24; shift += 8) {
                  int sum = (a >>> shift & 0xFF) + (b >>> shift & 0xFF) + (c >>> shift & 0xFF) + (d >>> shift & 0xFF);
                  color |= (sum + 2) / 4 << shift;
               }

               out.method_61941(x, y, color);
            }
         }

         return out;
      }
   }
}
