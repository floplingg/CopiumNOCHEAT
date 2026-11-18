package dev.violet.gui;

import dev.violet.util.NameFilter;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class VanishAlert {
   private static final float IN = 0.35F;
   private static final float STAY = 3.6F;
   private static final float OUT = 1.1F;
   private static final String BOLD = "montserrat-bold.ttf";
   private static final String BLACK = "montserrat-black.ttf";
   private static String title;
   private static long startNanos;
   private static boolean active;

   private VanishAlert() {
   }

   public static void watching(UUID id, String name, int others) {
      if (class_310.method_1551().field_1687 != null && name != null) {
         String shown = NameFilter.apply(name);
         title = (others > 0 ? shown + " +" + others : shown).toUpperCase(Locale.ROOT) + " IS WATCHING YOU";
         startNanos = System.nanoTime();
         active = true;
      }
   }

   public static void render(class_332 context) {
      if (active) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 == null) {
            active = false;
         } else {
            float t = (float)(System.nanoTime() - startNanos) / 1.0E9F;
            if (t > 5.0499997F) {
               active = false;
            } else {
               float fade = t < 0.35F ? MenuKit.quint(t / 0.35F) : (t > 3.9499998F ? 1.0F - MenuKit.quint((t - 0.35F - 3.6F) / 1.1F) : 1.0F);
               float pop = t < 0.35F
                  ? 0.6F + 0.4F * MenuKit.quint(t / 0.35F) + 0.08F * (float)Math.sin((double)Math.min(1.0F, t / 0.35F) * 3.141592653589793)
                  : 1.0F;
               float pulse = 0.5F + 0.5F * (float)Math.sin((double)t * 5.0);
               int width = context.method_51421();
               int height = context.method_51443();
               int edge = MenuKit.alpha(-723723, fade * (0.1F + 0.06F * pulse));
               context.method_25296(0, 0, width, height / 5, edge, 16053493);
               context.method_25296(0, height - height / 5, width, height, 16053493, edge);
               float cx = (float)width / 2.0F;
               float cy = (float)height * 0.36F;
               Smooth.glow(context, Math.round(cx), Math.round(cy), Math.round(70.0F * pop), MenuKit.alpha(-723723, fade * (0.18F + 0.1F * pulse)));
               float logo = 64.0F * pop;
               BrandTextures.Sprite orbit = BrandTextures.fit("orbit_glow", logo * 1.6F * Smooth.pixelsPerUnit(context));
               if (orbit != null) {
                  float w = logo * 1.6F;
                  float h = w * (float)orbit.height() / (float)orbit.width();
                  VioletScreen.image(context, orbit, cx - w / 2.0F, cy - h / 2.0F, w, h, MenuKit.alpha(-1, fade));
               }

               float titleEm = 8.5F;
               float luckEm = 22.0F * (0.9F + 0.1F * pop);
               float titleW = CrispText.width(title, "montserrat-bold.ttf", titleEm, 0.8F);
               float luckW = CrispText.width("Good Luck!", "montserrat-black.ttf", luckEm, 0.2F);
               float textTop = cy + logo / 2.0F + 12.0F;
               int panelW = Math.round(Math.max(titleW, luckW) + 40.0F);
               int panelH = Math.round(luckEm + titleEm + 34.0F);
               Smooth.rect(context, Math.round(cx - (float)panelW / 2.0F), Math.round(textTop - 6.0F), panelW, panelH, 3, MenuKit.alpha(-15198181, fade * 0.9F));
               CrispText.draw(context, title, "montserrat-bold.ttf", cx - titleW / 2.0F, textTop + titleEm + 4.0F, titleEm, 0.8F, MenuKit.alpha(-7434602, fade));
               CrispText.draw(
                  context,
                  "Good Luck!",
                  "montserrat-black.ttf",
                  cx - luckW / 2.0F,
                  textTop + titleEm + luckEm + 10.0F,
                  luckEm,
                  0.2F,
                  MenuKit.alpha(-723723, fade)
               );
               String tag = "SOBERCLIENT  ·  ANTI VANISH";
               float tagW = CrispText.width(tag, "montserrat-bold.ttf", 5.6F, 1.2F);
               CrispText.draw(context, tag, "montserrat-bold.ttf", cx - tagW / 2.0F, textTop + (float)panelH + 4.0F, 5.6F, 1.2F, MenuKit.alpha(-10724252, fade));
            }
         }
      }
   }
}
