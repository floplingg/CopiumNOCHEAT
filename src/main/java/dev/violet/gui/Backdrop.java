package dev.violet.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.violet.SoberClient;
import java.util.OptionalInt;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_6367;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class Backdrop {
   private static final class_2960 ID = class_2960.method_60655("violet", "dynamic/backdrop");
   private static final float LENS = 0.94F;
   private static final int DOWN_STEPS = 2;
   private static final int UP_STEPS = 1;
   private static final RenderPipeline GLASS = RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
      .withLocation(class_2960.method_60655("violet", "pipeline/liquid_glass"))
      .withFragmentShader(class_2960.method_60655("violet", "core/liquid_glass"))
      .build();
   private static final RenderPipeline GLASS_DENSE = RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
      .withLocation(class_2960.method_60655("violet", "pipeline/liquid_glass_dense"))
      .withFragmentShader(class_2960.method_60655("violet", "core/liquid_glass"))
      .withShaderDefine("YS_DENSE")
      .build();
   private static int glassState;
   private static final class_6367[] down = new class_6367[2];
   private static final class_6367[] up = new class_6367[1];
   private static final GuiBatch BATCH = new GuiBatch();
   private static Backdrop.Texture texture;
   private static boolean ready;
   private static boolean broken;
   private static boolean menuRequested;
   private static int menuMisses;

   private Backdrop() {
   }

   public static void capture(class_310 mc) {
      ready = false;
      if (!broken && Theme.liquidGlass() && mc.field_1687 != null && !mc.field_1690.field_1842) {
         if (chain(mc)) {
            ready = true;
         }
      }
   }

   public static boolean requestMenu(class_310 mc) {
      if (!broken && menuMisses <= 5 && mc.field_1687 == null && Theme.liquidGlass() && glassReady()) {
         if (menuRequested) {
            menuMisses++;
         }

         if (texture == null && !chain(mc)) {
            return false;
         } else {
            menuRequested = true;
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean menuActive() {
      return menuRequested;
   }

   public static boolean captureMenu() {
      if (!menuRequested) {
         return false;
      } else {
         menuRequested = false;
         menuMisses = 0;
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null) {
            return false;
         } else {
            chain(mc);
            return true;
         }
      }
   }

   private static boolean chain(class_310 mc) {
      try {
         class_276 main = mc.method_1522();
         if (main != null && main.method_71639() != null) {
            GpuTextureView source = main.method_71639();

            for (int i = 0; i < 2; i++) {
               int shift = i + 1;
               down[i] = sized(down[i], "Violet backdrop down " + shift, Math.max(1, main.field_1482 >> shift), Math.max(1, main.field_1481 >> shift));
               blit(source, down[i].method_71639());
               source = down[i].method_71639();
            }

            class_6367 result = down[1];

            for (int i = 0; i < 1; i++) {
               int shift = 1 - i;
               up[i] = sized(up[i], "Violet backdrop up " + shift, Math.max(1, main.field_1482 >> shift), Math.max(1, main.field_1481 >> shift));
               blit(source, up[i].method_71639());
               source = up[i].method_71639();
               result = up[i];
            }

            if (texture == null) {
               texture = new Backdrop.Texture();
               texture.use(result.method_30277(), result.method_71639());
               mc.method_1531().method_4616(ID, texture);
            }

            texture.use(result.method_30277(), result.method_71639());
            return true;
         } else {
            return false;
         }
      } catch (RuntimeException var6) {
         broken = true;
         SoberClient.LOG.warn("Liquid Glass backdrop is not available on this system, the glass stays plain", var6);
         return false;
      }
   }

   private static class_6367 sized(class_6367 framebuffer, String name, int width, int height) {
      if (framebuffer == null) {
         return new class_6367(name, width, height, false);
      } else {
         if (framebuffer.field_1482 != width || framebuffer.field_1481 != height) {
            framebuffer.method_1234(width, height);
         }

         return framebuffer;
      }
   }

   private static void blit(GpuTextureView from, GpuTextureView to) {
      RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Violet backdrop", to, OptionalInt.empty());

      try {
         pass.setPipeline(class_10799.field_57928);
         RenderSystem.bindDefaultUniforms(pass);
         pass.bindTexture("InSampler", from, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
         pass.draw(0, 3);
      } catch (Throwable var6) {
         if (pass != null) {
            try {
               pass.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (pass != null) {
         pass.close();
      }
   }

   private static boolean glassReady() {
      if (glassState == 0) {
         try {
            glassState = RenderSystem.getDevice().precompilePipeline(GLASS).isValid() && RenderSystem.getDevice().precompilePipeline(GLASS_DENSE).isValid()
               ? 1
               : -1;
         } catch (RuntimeException var1) {
            glassState = -1;
         }

         if (glassState < 0) {
            SoberClient.LOG.warn("Liquid Glass shader is not available, using the classic glass");
         }
      }

      return glassState > 0;
   }

   public static boolean glass(class_332 context, int x, int y, int w, int h, int r, float opacity, float bend, int tint) {
      return glass(context, x, y, w, h, r, opacity, bend, tint, false);
   }

   public static boolean glass(class_332 context, int x, int y, int w, int h, int r, float opacity, float bend, int tint, boolean dense) {
      boolean menu = class_310.method_1551().field_1687 == null;
      if ((menu ? menuRequested : ready) && texture != null && w > 0 && h > 0 && glassReady()) {
         int alpha = Math.round(255.0F * Math.max(0.0F, Math.min(1.0F, opacity)));
         if (alpha <= 0) {
            return true;
         } else {
            class_310 mc = class_310.method_1551();
            float pixel = (float)mc.method_22683().method_4489() / (float)Math.max(1, context.method_51421());
            Matrix3x2f matrix = context.method_51448();
            Vector2f start = matrix.transformPosition((float)x, (float)y, new Vector2f());
            Vector2f end = matrix.transformPosition((float)(x + w), (float)y, new Vector2f());
            float scale = Math.abs(end.x - start.x) * pixel / (float)w;
            if (scale <= 0.0F) {
               return true;
            } else {
               float radius = (float)Math.floor((double)Math.min(1000.0F, (float)Math.max(0, r) * scale * 1.35F));
               float band = (float)Math.floor((double)Math.min(1000.0F, Math.max(1.0F, bend * scale * 2.5F)));
               GuiBatch batch = BATCH.reset();
               batch.quad(x, y, x + w, y + h, radius * 4.0F + 1.5F, band * 4.0F + 1.5F, radius * 4.0F + 2.5F, band * 4.0F + 2.5F);
               batch.submitTextured(context, dense ? GLASS_DENSE : GLASS, ID, alpha << 24 | tint & 16777215);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean draw(class_332 context, int x, int y, int w, int h, int r, float opacity) {
      return draw(context, x, y, w, h, r, opacity, 0.0F);
   }

   public static boolean draw(class_332 context, int x, int y, int w, int h, int r, float opacity, float bend) {
      if (ready && texture != null && w > 0 && h > 0) {
         class_310 mc = class_310.method_1551();
         int frameWidth = mc.method_22683().method_4489();
         int frameHeight = mc.method_22683().method_4506();
         float pixelX = (float)frameWidth / (float)Math.max(1, context.method_51421());
         float pixelY = (float)frameHeight / (float)Math.max(1, context.method_51443());
         Matrix3x2f matrix = context.method_51448();
         Vector2f topLeft = matrix.transformPosition((float)x, (float)y, new Vector2f());
         Vector2f bottomRight = matrix.transformPosition((float)(x + w), (float)(y + h), new Vector2f());
         float left = topLeft.x * pixelX;
         float top = topLeft.y * pixelY;
         float spanX = (bottomRight.x - topLeft.x) * pixelX;
         float spanY = (bottomRight.y - topLeft.y) * pixelY;
         if (!(spanX < 1.0F) && !(spanY < 1.0F)) {
            int width = Math.max(1, Math.round(spanX));
            int height = Math.max(1, Math.round(spanY));
            float lensX = left + spanX * 0.060000002F / 2.0F;
            float lensY = top + spanY * 0.060000002F / 2.0F;
            float perX = spanX * 0.94F / (float)width;
            float perY = spanY * 0.94F / (float)height;
            int alpha = Math.round(255.0F * Math.max(0.0F, Math.min(1.0F, opacity)));
            if (alpha <= 0) {
               return false;
            } else {
               int color = alpha << 24 | 12763842;
               float radius = Math.max(0.0F, Math.min((float)Math.max(0, r) * spanX / (float)w, (float)Math.min(width, height) / 2.0F));
               float band = Math.max(0.0F, Math.min(bend * spanX / (float)w, (float)Math.min(width, height) * 0.3F));
               Backdrop.Mapping map = new Backdrop.Mapping(
                  lensX, lensY, perX, perY, frameWidth, frameHeight, width, height, band, Math.max(1, Math.round(band / 6.0F))
               );
               GuiBatch batch = BATCH.reset();
               float zone = Math.max(radius, band);
               int middleTop = -1;

               for (int row = 0; row < height; row++) {
                  float center = (float)row + 0.5F;
                  if (center >= zone && center <= (float)height - zone) {
                     if (middleTop < 0) {
                        middleTop = row;
                     }
                  } else {
                     if (middleTop >= 0) {
                        map.span(batch, 0, width, middleTop, row, 0);
                        middleTop = -1;
                     }

                     float dy = center < radius ? radius - center : (center > (float)height - radius ? center - ((float)height - radius) : 0.0F);
                     int start = 0;

                     for (int half = (width + 1) / 2; dy > 0.0F && start < half; start++) {
                        float dx = Math.max(0.0F, radius - ((float)start + 0.5F));
                        float coverage = Math.max(0.0F, Math.min(1.0F, radius - (float)Math.sqrt((double)(dx * dx + dy * dy)) + 0.5F));
                        if (coverage >= 0.996F) {
                           break;
                        }

                        int edge = Math.round((float)alpha * coverage);
                        if (edge > 0) {
                           int tint = edge << 24 | 12763842;
                           map.span(batch, start, start + 1, row, row + 1, tint);
                           if (width - 1 - start != start) {
                              map.span(batch, width - 1 - start, width - start, row, row + 1, tint);
                           }
                        }
                     }

                     if (width - start * 2 > 0) {
                        map.span(batch, start, width - start, row, row + 1, 0);
                     }
                  }
               }

               if (middleTop >= 0) {
                  map.span(batch, 0, width, middleTop, height, 0);
               }

               Matrix3x2fStack matrices = context.method_51448();
               matrices.pushMatrix();
               matrices.translate((float)x, (float)y);
               matrices.scale((float)w / spanX, (float)h / spanY);
               batch.submitTextured(context, class_10799.field_56883, ID, color);
               matrices.popMatrix();
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static record Mapping(float lensX, float lensY, float perX, float perY, int frameWidth, int frameHeight, int width, int height, float band, int step) {
      private static final float STRENGTH = 0.42F;

      private float bent(float distance) {
         if (distance >= this.band) {
            return distance;
         } else {
            float t = 1.0F - distance / this.band;
            return distance + 0.42F * this.band * t * t;
         }
      }

      private float u(float px) {
         float mapped = this.band <= 0.5F
            ? px
            : (px < this.band ? this.bent(px) : (px > (float)this.width - this.band ? (float)this.width - this.bent((float)this.width - px) : px));
         return (this.lensX + mapped * this.perX) / (float)this.frameWidth;
      }

      private float v(float py) {
         float mapped = this.band <= 0.5F
            ? py
            : (py < this.band ? this.bent(py) : (py > (float)this.height - this.band ? (float)this.height - this.bent((float)this.height - py) : py));
         return ((float)this.frameHeight - (this.lensY + mapped * this.perY)) / (float)this.frameHeight;
      }

      void span(GuiBatch batch, int x1, int x2, int y1, int y2, int tint) {
         float v1 = this.v((float)y1);
         float v2 = this.v((float)y2);
         int cursor = x1;
         int inner = (int)Math.ceil((double)this.band);
         int outer = (int)Math.floor((double)((float)this.width - this.band));

         while (cursor < x2) {
            int next;
            if (this.band > 0.5F && cursor < inner) {
               next = Math.min(Math.min(x2, inner), cursor + this.step);
            } else if (this.band > 0.5F && cursor >= outer) {
               next = Math.min(x2, cursor + this.step);
            } else {
               next = this.band > 0.5F ? Math.min(x2, Math.max(cursor + 1, outer)) : x2;
            }

            batch.quad(cursor, y1, next, y2, this.u((float)cursor), v1, this.u((float)next), v2, tint);
            cursor = next;
         }
      }
   }

   private static final class Texture extends class_1044 {
      void use(GpuTexture gpu, GpuTextureView view) {
         this.field_56974 = gpu;
         this.field_60597 = view;
         this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      }

      public void close() {
      }
   }
}
