package dev.violet.gui;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

final class CardChrome {
   static final int TEXT_VALUE = GuiPalette.abgr(-723723);
   static final int TEXT_NAME = GuiPalette.abgr(-723723);
   static final int TEXT_LABEL = GuiPalette.abgr(-5329226);
   static final float ROUNDING = 8.0F;
   private static final float FONT_PX = 9.0F;
   private static final int GUARD = -1946157056;
   private static final int BORDER = GuiPalette.abgr(-13750733);

   private CardChrome() {
   }

   static int accent() {
      return GuiPalette.accent();
   }

   static int px(float base, float scale) {
      return Math.max(6, Math.round(base * scale));
   }

   static int scaleAlpha(int color, float alphaScale) {
      int alpha = color >>> 24 & 0xFF;
      int scaled = Math.round((float)alpha * Math.max(0.0F, Math.min(1.0F, alphaScale)));
      return scaled << 24 | color & 16777215;
   }

   static int lerpColor(int c1, int c2, float t) {
      int r1 = c1 & 0xFF;
      int r2 = c2 & 0xFF;
      int g1 = c1 >> 8 & 0xFF;
      int g2 = c2 >> 8 & 0xFF;
      int b1 = c1 >> 16 & 0xFF;
      int b2 = c2 >> 16 & 0xFF;
      int a1 = c1 >>> 24;
      int a2 = c2 >>> 24;
      return (int)((float)a1 + (float)(a2 - a1) * t) << 24
         | (int)((float)b1 + (float)(b2 - b1) * t) << 16
         | (int)((float)g1 + (float)(g2 - g1) * t) << 8
         | (int)((float)r1 + (float)(r2 - r1) * t);
   }

   static float width(CardFont font, int size, String text) {
      return text.isEmpty()
         ? 0.0F
         : (float)(class_310.method_1551().field_1772.method_27525(class_2561.method_43470(text).method_10862(font.style())) * size) / 9.0F;
   }

   static float ascent(CardFont font, int size) {
      return (float)size * font.ascent();
   }

   static float baselineOffset(CardFont big, int bigSize, CardFont small, int smallSize) {
      return (float)Math.round(ascent(big, bigSize) - ascent(small, smallSize));
   }

   static float tabularWidth(CardFont font, int size, String text) {
      return width(font, size, text);
   }

   static void tabular(OverlayCanvas dl, CardFont font, int size, float x, float y, int color, String text, boolean guard) {
      text(dl, font, size, x, y, color, text, guard);
   }

   static void text(OverlayCanvas dl, CardFont font, int size, float x, float y, int color, String text, boolean guard) {
      if (!text.isEmpty() && (color >>> 24 & 0xFF) != 0) {
         if (guard) {
            raw(dl.context(), font, size, x + 1.0F, y + 1.0F, scaleAlpha(-1946157056, (float)(color >>> 24 & 0xFF) / 255.0F), text);
         }

         raw(dl.context(), font, size, x, y, color, text);
      }
   }

   private static void raw(class_332 context, CardFont font, int size, float x, float y, int abgr, String text) {
      float scale = (float)size / 9.0F;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(x, y);
      matrices.scale(scale, scale);
      context.method_51439(class_310.method_1551().field_1772, class_2561.method_43470(text).method_10862(font.style()), 0, 0, OverlayCanvas.argb(abgr), false);
      matrices.popMatrix();
   }

   static void card(OverlayCanvas dl, float x, float y, float w, float h, float r, float alpha, boolean shadow, float scale) {
      if (Theme.liquidGlass()) {
         Matrix3x2fStack matrices = dl.context().method_51448();
         matrices.pushMatrix();
         matrices.scale(2.0F, 2.0F);
         Smooth.liquid(dl.context(), Math.round(x / 2.0F), Math.round(y / 2.0F), Math.round(w / 2.0F), Math.round(h / 2.0F), Math.round(r), alpha, false);
         matrices.popMatrix();
      } else {
         dl.addRectFilled(x, y, x + w, y + h, scaleAlpha(GuiPalette.abgr(HudPalette.card()), alpha), r);
         dl.addRect(x, y, x + w, y + h, scaleAlpha(BORDER, alpha), r, 0, 1.0F);
      }
   }
}
