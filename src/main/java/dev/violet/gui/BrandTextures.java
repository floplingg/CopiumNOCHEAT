package dev.violet.gui;

import dev.violet.SoberClient;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class BrandTextures {
   private static final Map<String, List<BrandTextures.Sprite>> CACHE = new HashMap<>();

   private BrandTextures() {
   }

   public static BrandTextures.Sprite get(String name) {
      List<BrandTextures.Sprite> levels = levels(name);
      return levels.isEmpty() ? null : levels.get(0);
   }

   public static BrandTextures.Sprite fit(String name, float pixels) {
      List<BrandTextures.Sprite> levels = levels(name);
      if (levels.isEmpty()) {
         return null;
      } else {
         BrandTextures.Sprite best = levels.get(0);

         for (BrandTextures.Sprite sprite : levels) {
            if ((float)Math.max(sprite.width(), sprite.height()) >= pixels) {
               best = sprite;
            }
         }

         return best;
      }
   }

   private static List<BrandTextures.Sprite> levels(String name) {
      List<BrandTextures.Sprite> cached = CACHE.get(name);
      if (cached != null) {
         return cached;
      } else {
         List<BrandTextures.Sprite> levels = new ArrayList<>();

         try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655("violet", "textures/brand/" + name + ".png"))) {
            class_1011 image = class_1011.method_4309(stream);
            int level = 0;

            while (true) {
               class_2960 id = class_2960.method_60655("violet", "dynamic/brand_" + name + "_" + level);
               levels.add(new BrandTextures.Sprite(id, image.method_4307(), image.method_4323()));
               class_1011 next = image.method_4307() >= 48 && image.method_4323() >= 48 && level < 4 ? half(image) : null;
               Smooth.register(id, image);
               if (next == null) {
                  break;
               }

               image = next;
               level++;
            }
         } catch (Exception var10) {
            SoberClient.LOG.warn("Could not load Copium Client texture {}", name, var10);
         }

         CACHE.put(name, levels);
         return levels;
      }
   }

   private static class_1011 half(class_1011 src) {
      int w = Math.max(1, src.method_4307() / 2);
      int h = Math.max(1, src.method_4323() / 2);
      class_1011 out = new class_1011(w, h, false);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            long a = 0L;
            long r = 0L;
            long g = 0L;
            long b = 0L;

            for (int dy = 0; dy < 2; dy++) {
               for (int dx = 0; dx < 2; dx++) {
                  int c = src.method_61940(Math.min(src.method_4307() - 1, x * 2 + dx), Math.min(src.method_4323() - 1, y * 2 + dy));
                  int ca = c >>> 24;
                  a += (long)ca;
                  r += (long)(c >> 16 & 0xFF) * (long)ca;
                  g += (long)(c >> 8 & 0xFF) * (long)ca;
                  b += (long)(c & 0xFF) * (long)ca;
               }
            }

            int outA = (int)(a / 4L);
            int outR = a == 0L ? 0 : (int)(r / a);
            int outG = a == 0L ? 0 : (int)(g / a);
            int outB = a == 0L ? 0 : (int)(b / a);
            out.method_61941(x, y, outA << 24 | outR << 16 | outG << 8 | outB);
         }
      }

      return out;
   }

   public static record Sprite(class_2960 id, int width, int height) {
   }
}
