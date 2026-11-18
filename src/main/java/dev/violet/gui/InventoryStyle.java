package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.CustomGlint;
import dev.violet.modules.CustomInventory;
import dev.violet.render.Color;
import java.util.Locale;
import net.minecraft.class_10799;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class InventoryStyle {
   private static Runnable pendingEntity;
   private static int[] entityBox;

   private InventoryStyle() {
   }

   private static CustomInventory module() {
      CustomInventory module = SoberClient.MODULES.get(CustomInventory.class);
      return module != null && module.styled() ? module : null;
   }

   private static int alpha(int color, int alpha) {
      return (Math.max(0, Math.min(255, alpha)) & 0xFF) << 24 | color & 16777215;
   }

   private static int mix(int a, int b, float t) {
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
   }

   private static float hash(int n) {
      int h = n * 374761393 + 668265263;
      h = (h ^ h >>> 13) * 1274126177;
      return (float)((h ^ h >>> 16) & 16777215) / 1.6777215E7F;
   }

   private static int hue(int color, float shift) {
      float[] hsv = Color.fromPacked(0xFF000000 | color).toHsv();
      return Color.fromHsv((hsv[0] + shift + 1.0F) % 1.0F, Math.max(0.45F, hsv[1]), Math.max(0.7F, hsv[2]), 255).packed() & 16777215;
   }

   public static boolean lightTitles() {
      CustomInventory module = module();
      return module == null ? false : module.style.get() != CustomInventory.Style.Tinted && module.strength.get() >= 35;
   }

   public static int titleColor(int original) {
      return !lightTitles() ? original : -1645840;
   }

   public static boolean deferEntity(int x1, int y1, int x2, int y2, Runnable draw) {
      CustomInventory module = module();
      if (module != null && module.style.get() != CustomInventory.Style.Tinted) {
         pendingEntity = draw;
         entityBox = new int[]{x1, y1, x2, y2};
         return true;
      } else {
         return false;
      }
   }

   public static void behind(class_332 context, int screenWidth, int screenHeight, int x, int y, int w, int h, boolean creative) {
      pendingEntity = null;
      CustomInventory module = module();
      if (module != null) {
         int color = module.color();
         double time = (double)System.currentTimeMillis() / 1000.0;
         float pulse = (float)(0.5 + 0.5 * Math.sin(time * 1.6));
         int cx = x + w / 2;
         int cy = y + h / 2;
         if (module.background.get()) {
            context.method_25296(0, 0, screenWidth, screenHeight, alpha(mix(color, 0, 0.55F), 40), alpha(mix(color, 328714, 0.85F), 120));
            Smooth.glow(context, cx, cy, Math.round((float)Math.max(w, h) * 1.1F), alpha(color, 56));
            int size = Math.round((float)Math.min(screenWidth, screenHeight) * 0.9F);
            Smooth.logo(context, cx - size / 2, cy - size / 2, size, 150994943);
         }

         if (module.glow.get()) {
            int[] spread = new int[]{9, 6, 3};
            int[] shade = new int[]{16, 28, 42};

            for (int i = 0; i < spread.length; i++) {
               int s = spread[i];
               Smooth.rect(context, x - s, y - s + 3, w + s * 2, h + s * 2, 6 + s, shade[i] << 24);
            }

            Smooth.glow(context, cx, y, Math.round((float)w * 0.6F), alpha(color, Math.round(48.0F + 32.0F * pulse)));
            Smooth.outline(context, x - 2, y - 2, w + 4, h + 4, 6, alpha(color, Math.round(144.0F + 64.0F * pulse)));
         }

         if (module.logo.get() && !creative && y > 30) {
            int size = 20;
            int ly = y - size - 8 + Math.round((float)Math.sin(time * 1.2) * 1.5F);
            Smooth.glow(context, cx, ly + size / 2, 22, alpha(color, Math.round(96.0F + 48.0F * pulse)));
            Smooth.logo(context, cx - size / 2, ly, size, -1);
         }

         context.method_71048();
      }
   }

   public static void over(class_332 context, int x, int y, int w, int h, class_1703 handler, boolean creative) {
      CustomInventory module = module();
      if (module == null) {
         flushEntity();
      } else {
         int color = module.color();
         float strength = (float)module.strength.get().intValue() / 100.0F;
         context.method_71048();
         int panel = Theme.panelSolid(0) & 16777215;
         switch ((CustomInventory.Style)module.style.get()) {
            case Tinted:
               Smooth.rect(context, x, y, w, h, 4, alpha(color, Math.round(144.0F * strength)));
               break;
            case Glass:
               Smooth.rect(context, x, y, w, h, 5, alpha(mix(panel, color, 0.06F), Math.round(244.0F * strength)));
               Smooth.rect(context, x, y, w, h, 5, alpha(color, Math.round(18.0F * strength)));
               Smooth.rect(context, x + 5, y + 1, w - 10, 1, 1, alpha(16777215, Math.round(38.0F * strength)));
               break;
            case Midnight:
               Smooth.rect(context, x, y, w, h, 5, alpha(461071, Math.round(255.0F * strength)));
               Smooth.rect(context, x + 5, y + 1, w - 10, 1, 1, alpha(color, Math.round(144.0F * strength)));
         }

         if (module.style.get() != CustomInventory.Style.Tinted && module.animation.get() != CustomInventory.Animation.None) {
            context.method_44379(x + 2, y + 2, x + w - 2, y + h - 2);
            animate(context, module, x, y, w, h, color, strength);
            context.method_44380();
         }

         if (module.slots.get() && module.style.get() != CustomInventory.Style.Tinted && handler != null && strength > 0.05F) {
            float s = Math.min(1.0F, strength * 1.3F);
            int fill = alpha(mix(1053728, color, 0.12F), Math.round(176.0F * s));
            int edge = alpha(mix(16777215, color, 0.35F), Math.round(52.0F * s));

            for (class_1735 slot : handler.field_7761) {
               if (slot.method_7682()) {
                  int sx = x + slot.field_7873 - 1;
                  int sy = y + slot.field_7872 - 1;
                  Smooth.rect(context, sx, sy, 18, 18, 4, fill);
                  Smooth.outline(context, sx, sy, 18, 18, 4, edge);
               }
            }
         }

         if (pendingEntity != null && entityBox != null) {
            int ex = entityBox[0];
            int ey = entityBox[1];
            int ew = entityBox[2] - ex;
            int eh = entityBox[3] - ey;
            Smooth.rect(context, ex, ey, ew, eh, 5, alpha(mix(1053728, color, 0.18F), 192));
            Smooth.glow(context, ex + ew / 2, ey + eh - 6, Math.max(ew, eh) / 2, alpha(color, 64));
            Smooth.outline(context, ex, ey, ew, eh, 5, alpha(mix(16777215, color, 0.4F), 64));
         }

         flushEntity();
         if (module.logo.get() && !creative && w >= 120) {
            Smooth.logo(context, x + w - 14, y + 4, 9, alpha(16777215, lightTitles() ? 176 : 128));
         }
      }
   }

   private static void flushEntity() {
      Runnable draw = pendingEntity;
      pendingEntity = null;
      if (draw != null) {
         draw.run();
      }
   }

   private static void animate(class_332 context, CustomInventory module, int x, int y, int w, int h, int color, float strength) {
      double time = (double)System.currentTimeMillis() / 1000.0 * (double)module.animationSpeed.get().intValue() / 100.0;
      float k = Math.max(0.2F, strength);
      switch ((CustomInventory.Animation)module.animation.get()) {
         case Stars:
            for (int i = 0; i < 40; i++) {
               float sx = (float)x + hash(i * 3) * (float)w;
               float speed = 2.0F + hash(i * 3 + 1) * 6.0F;
               float sy = (hash(i * 3 + 2) * (float)(h + 10) - (float)(time * (double)speed)) % (float)(h + 10);
               if (sy < 0.0F) {
                  sy += (float)(h + 10);
               }

               float twinkle = 0.3F + 0.7F * (0.5F + 0.5F * (float)Math.sin(time * (0.8 + (double)hash(i) * 2.2) + (double)i));
               int star = mix(16777215, color, hash(i * 7) * 0.6F);
               float py = (float)(y - 5) + sy;
               if (hash(i * 11) > 0.8F) {
                  Smooth.softDot(context, sx, py, 4.5F, alpha(star, Math.round(112.0F * twinkle * k)));
               }

               Smooth.softDot(context, sx, py, 1.4F, alpha(star, Math.round(224.0F * twinkle * k)));
            }
            break;
         case Aurora:
            for (int i = 0; i < 4; i++) {
               float phase = (float)(time * (0.18 + (double)i * 0.05) + (double)i * 1.7);
               int gx = x + Math.round((0.5F + 0.45F * (float)Math.sin((double)phase)) * (float)w);
               int gy = y + Math.round((0.3F + 0.4F * (float)Math.cos((double)(phase * 0.8F + (float)i))) * (float)h);
               int tint = hue(color, (float)i * 0.08F - 0.08F);
               Smooth.glow(context, gx, gy, Math.round((float)Math.max(w, h) * (0.45F + 0.1F * (float)i)), alpha(tint, Math.round(52.0F * k)));
            }
            break;
         case Glint:
            CustomGlint glint = SoberClient.MODULES.get(CustomGlint.class);
            String name = glint != null && glint.glint.get() != CustomGlint.Glint.Vanilla ? glint.glint.get().name().toLowerCase(Locale.ROOT) : "galaxy";
            class_2960 texture = class_2960.method_60655("violet", "textures/glint/" + name + ".png");
            int tile = 128;
            int ox = (int)Math.round(time * 12.0) % tile;
            int oy = (int)Math.round(time * 7.0) % tile;
            int tint = alpha(16777215, Math.round(80.0F * k));

            for (int tx = x - tile + ox; tx < x + w; tx += tile) {
               for (int ty = y - tile + oy; ty < y + h; ty += tile) {
                  context.method_25291(class_10799.field_56883, texture, tx, ty, 0.0F, 0.0F, tile, tile, tile, tile, tint);
               }
            }
            break;
         case Shimmer:
            float cycle = (float)(time * 0.35 % 1.0);
            int band = Math.round((float)(-w) * 0.4F + cycle * (float)w * 1.8F);
            int shine = mix(16777215, color, 0.3F);
            int peak = alpha(shine, Math.round(40.0F * k));
            int left = Math.max(x, x + band - 12);
            int center = x + band;
            int right = Math.min(x + w, x + band + 12);
            if (center > left) {
               Smooth.horizontalGradient(context, left, y, Math.min(center, x + w), y + h, alpha(shine, 0), peak);
            }

            if (right > center) {
               Smooth.horizontalGradient(context, Math.max(center, x), y, right, y + h, peak, alpha(shine, 0));
            }

            Smooth.glow(context, x + band, y + h / 2, Math.round((float)h * 0.6F), alpha(color, Math.round(34.0F * k)));
      }
   }
}
