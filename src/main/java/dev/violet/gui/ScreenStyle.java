package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.MainMenu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5223;
import net.minecraft.class_5348;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public final class ScreenStyle {
   private static final int CARD = 2895926;
   private static final int CARD_HOVER = 3817287;
   private static final int DEEP = 1777187;
   private static final int ACCENT = 14014942;
   private static final Map<Object, float[]> HOVER = new WeakHashMap<>();
   private static final Set<Object> PRIMARY = Collections.newSetFromMap(new WeakHashMap<>());
   private static final Set<Object> FLAT = Collections.newSetFromMap(new WeakHashMap<>());
   private static int trackX;
   private static int trackY;
   private static int trackH;
   private static boolean labelDrawn;
   static final int VIOLET_R = 4;

   private ScreenStyle() {
   }

   private static MainMenu menu() {
      return SoberClient.MODULES.get(MainMenu.class);
   }

   public static boolean screens() {
      MainMenu menu = menu();
      return menu != null && menu.isActive() && menu.customScreens.get();
   }

   public static boolean widgets() {
      MainMenu menu = menu();
      return menu != null && menu.isActive() && menu.customWidgets.get();
   }

   public static int color() {
      return 14014942;
   }

   private static int tint() {
      MainMenu menu = menu();
      if (menu != null && menu.matchSky.get()) {
         int sky = menu.color();
         if (sky >= 0) {
            return mix(5923954, sky, 0.35F);
         }
      }

      return 5923954;
   }

   static int alpha(int color, float alpha) {
      return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | color & 16777215;
   }

   static int mix(int a, int b, float t) {
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

   private static float animate(Object key, boolean target) {
      long now = System.nanoTime();
      float[] state = HOVER.computeIfAbsent(key, k -> new float[]{0.0F, (float)now});
      float delta = Math.min(0.05F, (float)(now - (long)state[1]) / 1.0E9F);
      if (delta < 0.0F) {
         delta = 0.0F;
      }

      state[1] = (float)now;
      state[0] += ((target ? 1.0F : 0.0F) - state[0]) * (1.0F - (float)Math.exp((double)(-16.0F * delta)));
      return state[0];
   }

   public static void primary(Object widget) {
      PRIMARY.add(widget);
   }

   public static boolean glassMenu() {
      return Theme.liquidGlass();
   }

   public static boolean glass(class_332 context, int x, int y, int w, int h, int r, float opacity) {
      if (!Theme.liquidGlass()) {
         return false;
      } else {
         float bend = Math.max(2.0F, Math.min(13.0F, (float)Math.min(w, h) * 0.13F));
         return Backdrop.glass(context, x, y, w, h, r, opacity, bend, Theme.glassTint(), false);
      }
   }

   public static void shadow(class_332 context, int x, int y, int w, int h, int r, float opacity) {
      if (!(opacity <= 0.01F) && w > 0 && h > 0) {
         for (int k = 10; k >= 1; k--) {
            float t = (float)k / 10.0F;
            float grow = 1.5F + 9.0F * t;
            int color = alpha(1312286, 0.035F * (1.0F - t * 0.7F) * opacity);
            Smooth.rect(
               context,
               Math.round((float)x - grow * 0.6F),
               Math.round((float)y - grow * 0.2F + 5.0F),
               Math.round((float)w + grow * 1.2F),
               Math.round((float)h + grow * 1.2F),
               Math.round((float)r + grow),
               color
            );
         }
      }
   }

   public static void flat(Object widget) {
      FLAT.add(widget);
   }

   public static void crisp(class_332 context, String value, float x, float y, float size, int color, boolean heavy) {
      if (value != null && !value.isEmpty() && color >>> 24 != 0) {
         if (!formatted(value)) {
            String file = Theme.crispFile(heavy);
            if (!CrispText.canDisplay(value, file)) {
               Matrix3x2fStack matrices = context.method_51448();
               matrices.pushMatrix();
               matrices.translate(x, y);
               matrices.scale(size, size);
               context.method_51433(class_310.method_1551().field_1772, value, 0, 0, color, false);
               matrices.popMatrix();
            } else {
               float em = 9.4F * size * Theme.crispScale();
               float baseline = y + 7.6F * size;
               CrispText.draw(context, value, file, x, baseline, em, 0.0F, color);
            }
         } else {
            float cx = x;

            for (ScreenStyle.Run run : runs(class_5348.method_29430(value), color)) {
               crisp(context, run.text(), cx, y, size, run.color(), heavy);
               cx += crispWidth(run.text(), size, heavy);
            }
         }
      }
   }

   public static float crispWidth(String value, float size, boolean heavy) {
      if (value == null || value.isEmpty()) {
         return 0.0F;
      } else if (!formatted(value)) {
         String file = Theme.crispFile(heavy);
         return !CrispText.canDisplay(value, file)
            ? (float)class_310.method_1551().field_1772.method_1727(value) * size
            : CrispText.width(value, file, 9.4F * size * Theme.crispScale(), 0.0F);
      } else {
         float width = 0.0F;

         for (ScreenStyle.Run run : runs(class_5348.method_29430(value), -1)) {
            width += crispWidth(run.text(), size, heavy);
         }

         return width;
      }
   }

   public static void sdf(class_332 context, String value, float x, float y, float size, int color) {
      sdf(context, value, x, y, size, color, true);
   }

   public static void sdf(class_332 context, String value, float x, float y, float size, int color, boolean shadow) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(x, y);
      matrices.scale(size, size);
      if (shadow && Theme.liquidGlass()) {
         matrices.pushMatrix();
         matrices.translate(0.0F, 0.5F);
         SdfText.draw(context, value, 0, 0, Math.round((float)(color >>> 24) * 0.18F) << 24);
         matrices.popMatrix();
      }

      SdfText.draw(context, value, 0, 0, color);
      matrices.popMatrix();
   }

   public static List<ScreenStyle.Run> runs(class_5348 text, int base) {
      List<ScreenStyle.Run> out = new ArrayList<>();
      StringBuilder current = new StringBuilder();
      int[] color = new int[]{base};
      class_5223.method_27476(text, class_2583.field_24360, (index, style, codePoint) -> {
         int next = style.method_10973() != null ? base & 0xFF000000 | style.method_10973().method_27716() & 16777215 : base;
         if (next != color[0] && current.length() > 0) {
            out.add(new ScreenStyle.Run(current.toString(), color[0]));
            current.setLength(0);
         }

         color[0] = next;
         current.appendCodePoint(codePoint);
         return true;
      });
      if (current.length() > 0) {
         out.add(new ScreenStyle.Run(current.toString(), color[0]));
      }

      return out;
   }

   public static boolean formatted(String value) {
      return value.indexOf(167) >= 0;
   }

   public static boolean label(class_332 context, Object key, class_2561 message, int x, int y, int w, int h, boolean active, float opacity) {
      labelDrawn = false;
      if (message != null && !(opacity <= 0.01F)) {
         int base = 0xFF000000 | (PRIMARY.contains(key) ? 1707524 : (active ? 16777215 : 10526880));
         List<ScreenStyle.Run> parts = runs(message, base);
         StringBuilder plain = new StringBuilder();

         for (ScreenStyle.Run run : parts) {
            plain.append(run.text());
         }

         String value = plain.toString();
         if (value.isEmpty()) {
            return false;
         } else {
            String file = Theme.crispFile(false);
            if (!CrispText.canDisplay(value, file)) {
               return false;
            } else {
               float em = 9.0F;
               float width = CrispText.width(value, file, em, 0.0F);
               if (width > (float)(w - 6)) {
                  return false;
               } else {
                  float cx = (float)Math.round((float)x + ((float)w - width) / 2.0F);
                  float baseline = (float)y + (float)(h - 8) / 2.0F + 7.2F;

                  for (ScreenStyle.Run run : parts) {
                     if (!PRIMARY.contains(key)) {
                        CrispText.draw(context, run.text(), file, cx + 0.5F, baseline + 0.7F, em, 0.0F, alpha(0, 0.3F * opacity));
                     }

                     CrispText.draw(context, run.text(), file, cx, baseline, em, 0.0F, alpha(run.color(), opacity));
                     cx += CrispText.width(run.text(), file, em, 0.0F);
                  }

                  labelDrawn = true;
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean consumeLabel() {
      boolean drawn = labelDrawn;
      labelDrawn = false;
      return drawn;
   }

   public static int readable(int color) {
      if (!Theme.liquidGlass()) {
         return color;
      } else {
         int rgb = color & 16777215;
         if (rgb == 10330538 || rgb == 11120053) {
            return color & 0xFF000000 | 14343651;
         } else {
            return rgb == 7040888 ? color & 0xFF000000 | 12896463 : color;
         }
      }
   }

   public static void neon(class_332 context, int x, int y, int w, int h, int r, int color, float strength) {
      if (!(strength <= 0.01F) && w > 2 && h > 2) {
         int rgb = color & 16777215;
         if (Theme.liquidGlass()) {
            Smooth.rect(context, x, y, w, h, r, alpha(mix(rgb, 16777215, 0.7F), 0.1F * strength));
         } else {
            for (int k = 6; k >= 1; k--) {
               float t = (float)k / 6.0F;
               Smooth.outline(context, x - k, y - k, w + k * 2, h + k * 2, r + k, alpha(rgb, strength * (0.15F * (1.0F - t) * (1.0F - t) + 0.015F)));
            }

            Smooth.outline(context, x, y, w, h, r, alpha(rgb, 0.6F * strength));
            if (w > 4 && h > 4) {
               Smooth.outline(context, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), alpha(mix(rgb, 16777215, 0.55F), Math.min(1.0F, 0.9F * strength)));
            }
         }
      }
   }

   public static void topLight(class_332 context, int x, int y, int w, int r, float strength) {
      int left = x + r;
      int right = x + w - r;
      int mid = (left + right) / 2;
      if (!Theme.liquidGlass() && right - left >= 6 && !(strength <= 0.01F)) {
         Smooth.horizontalGradient(context, left, y, mid, y + 1, alpha(16777215, 0.0F), alpha(16777215, strength));
         Smooth.horizontalGradient(context, mid, y, right, y + 1, alpha(16777215, strength), alpha(16777215, 0.0F));
      }
   }

   public static boolean vanillaPlus() {
      MainMenu menu = menu();
      return menu != null && menu.style.get() == MainMenu.Style.VanillaPlus;
   }

   public static boolean home() {
      MainMenu menu = menu();
      return menu != null
         && (menu.style.get() == MainMenu.Style.Home || menu.style.get() == MainMenu.Style.VanillaPlus || menu.style.get() == MainMenu.Style.Violet);
   }

   public static boolean violetStyle() {
      MainMenu menu = menu();
      return menu != null && menu.style.get() == MainMenu.Style.Violet;
   }

   private static int accent() {
      if (violetStyle()) {
         return -1513234;
      } else {
         MainMenu menu = menu();
         return 0xFF000000 | (menu != null ? menu.color() : Theme.accent());
      }
   }

   public static void crispPanel(class_332 context, float x, float y, float w, float h, float r, int fill, int border) {
      if (!(w <= 0.0F) && !(h <= 0.0F)) {
         Matrix3x2fStack matrices = context.method_51448();
         class_1041 window = class_310.method_1551().method_22683();
         float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
         Vector2f a = matrices.transformPosition(x, y, new Vector2f());
         Vector2f b = matrices.transformPosition(x + w, y + h, new Vector2f());
         int x0 = Math.round(Math.min(a.x, b.x) * ratio);
         int y0 = Math.round(Math.min(a.y, b.y) * ratio);
         int x1 = Math.round(Math.max(a.x, b.x) * ratio);
         int y1 = Math.round(Math.max(a.y, b.y) * ratio);
         float zoom = (float)Math.sqrt((double)(matrices.m00() * matrices.m00() + matrices.m01() * matrices.m01()));
         int radius = Math.max(0, Math.round(r * zoom * ratio));
         matrices.pushMatrix();
         matrices.identity();
         matrices.scale(1.0F / ratio, 1.0F / ratio);
         if (fill >>> 24 != 0) {
            Smooth.rect(context, x0, y0, x1 - x0, y1 - y0, radius, fill);
         }

         if (border >>> 24 != 0) {
            Smooth.outline(context, x0, y0, x1 - x0, y1 - y0, Math.max(1, radius), border);
         }

         matrices.popMatrix();
      }
   }

   public static void violetPill(class_332 context, int x, int y, int w, int h, float hover, boolean primary, float opacity) {
      if (!(opacity <= 0.01F) && w > 0 && h > 0) {
         int r = Math.min(4, h / 2);
         if (primary) {
            crispPanel(context, (float)x, (float)y, (float)w, (float)h, (float)r, alpha(mix(15000807, 16777215, hover), opacity), 0);
         } else {
            crispPanel(
               context,
               (float)x,
               (float)y,
               (float)w,
               (float)h,
               (float)r,
               alpha(mix(1381655, 1973794, hover), opacity),
               alpha(mix(2763310, 4868690, hover), opacity)
            );
         }
      }
   }

   public static void violetPanel(class_332 context, int x, int y, int w, int h, int r, float hover, float selected, float opacity) {
      if (!(opacity <= 0.01F) && w > 0 && h > 0) {
         r = Math.min(r, 5);
         crispPanel(
            context,
            (float)x,
            (float)y,
            (float)w,
            (float)h,
            (float)r,
            alpha(mix(mix(1315862, 1644828, hover), 2039587, selected), (0.9F + 0.04F * selected) * opacity),
            alpha(16777215, (0.08F + 0.08F * hover + 0.4F * selected) * opacity)
         );
      }
   }

   public static float intro(long openedAt, class_310 client, class_437 screen) {
      if (openedAt != 0L
         && client != null
         && client.field_1687 == null
         && !(screen instanceof HomeScreen)
         && !(screen instanceof VanillaPlusScreen)
         && screens()
         && home()
         && !vanillaPlus()) {
         float p = Math.max(0.0F, Math.min(1.0F, (float)(System.nanoTime() - openedAt) / 1.0E9F / 0.5F));
         float inverse = 1.0F - p;
         return 1.0F - inverse * inverse * inverse * inverse * inverse;
      } else {
         return 1.0F;
      }
   }

   public static void introVeil(class_332 context, int width, int height, float p) {
      if (violetStyle()) {
         context.method_25294(0, 0, width, height, alpha(723724, 1.0F - p));
      } else if (home()) {
         homeBackdrop(context, width, height, 1.0F - p);
      } else {
         context.method_25294(0, 0, width, height, alpha(460811, 1.0F - p));
      }
   }

   private static void homeBackdrop(class_332 context, int width, int height, float cover) {
      class_310 client = class_310.method_1551();
      float mx = 0.0F;
      float my = 0.0F;
      if (client.method_22683() != null && width > 0 && height > 0) {
         mx = (float)(client.field_1729.method_68879(client.method_22683()) / (double)width) - 0.5F;
         my = (float)(client.field_1729.method_68883(client.method_22683()) / (double)height) - 0.5F;
      }

      HomeScreen.paintBackdrop(context, width, height, 1.0F, mx, my, cover);
   }

   public static void backdrop(class_332 context, int width, int height) {
      if (violetStyle()) {
         VioletScreen.paintSpace(context, width, height, 1.0F);
      } else if (home()) {
         homeBackdrop(context, width, height, 1.0F);
      } else {
         int tint = tint();
         double seconds = (double)System.currentTimeMillis() / 1000.0;
         context.method_25294(0, 0, width, height, -15921390);
         context.method_25296(0, 0, width, height, -15263201, -16053233);
         int big = Math.max(width, height);
         int gx = Math.round((float)width * 0.24F + (float)Math.sin(seconds * 0.13) * (float)width * 0.08F);
         int gy = Math.round((float)height * 0.3F + (float)Math.cos(seconds * 0.11) * (float)height * 0.1F);
         Smooth.glow(context, gx, gy, Math.round((float)big * 0.55F), alpha(tint, 0.22F));
         int hx = Math.round((float)width * 0.78F + (float)Math.cos(seconds * 0.09) * (float)width * 0.08F);
         int hy = Math.round((float)height * 0.74F + (float)Math.sin(seconds * 0.12) * (float)height * 0.08F);
         Smooth.glow(context, hx, hy, Math.round((float)big * 0.5F), alpha(mix(tint, 4165273, 0.35F), 0.14F));
         MainMenu menu = menu();
         if (menu == null || menu.stars.get()) {
            for (int i = 0; i < 90; i++) {
               float sx = hash(i * 3) * (float)width;
               float speed = 2.0F + hash(i * 3 + 1) * 6.0F;
               float sy = (hash(i * 3 + 2) * (float)(height + 20) - (float)(seconds * (double)speed)) % (float)(height + 20);
               if (sy < 0.0F) {
                  sy += (float)(height + 20);
               }

               sy -= 10.0F;
               float twinkle = 0.3F + 0.7F * (0.5F + 0.5F * (float)Math.sin(seconds * (0.8 + (double)hash(i) * 2.2) + (double)i));
               if (hash(i * 11) > 0.88F) {
                  Smooth.softDot(context, sx, sy, 4.0F, alpha(16777215, 0.28F * twinkle));
                  Smooth.softDot(context, sx, sy, 1.4F, alpha(16777215, 0.75F * twinkle));
               } else {
                  Smooth.softDot(context, sx, sy, 1.2F, alpha(15133165, 0.45F * twinkle));
               }
            }
         }

         int size = Math.round((float)Math.min(width, height) * 1.05F);
         int bob = Math.round((float)Math.sin(seconds * 0.5) * 6.0F);
         Smooth.logo(context, width / 2 - size / 2, height / 2 - size / 2 + bob, size, 134217727);
         context.method_25296(0, height / 2, width, height, 0, 1711276032);
      }
   }

   public static void serverIconOverlay(class_332 context, int x, int y) {
      Smooth.rect(context, x, y, 32, 32, 4, -1207235310);
      Smooth.outline(context, x, y, 32, 32, 4, alpha(accent(), 0.55F));
   }

   public static void serverIconSprite(class_332 context, int x, int y, String path) {
      boolean lit = path.endsWith("_highlighted");
      int accent = accent();
      Matrix3x2fStack matrices = context.method_51448();
      if (path.contains("join")) {
         float cx = (float)x + 20.0F;
         float cy = (float)y + 16.0F;
         if (lit) {
            Smooth.glow(context, Math.round(cx), Math.round(cy), 15, alpha(accent, 0.5F));
         }

         matrices.pushMatrix();
         matrices.translate(cx, cy);
         matrices.rotate(1.5707964F);
         Smooth.triangle(context, 0.0F, 0.0F, lit ? 13.0F : 11.0F, lit ? accent : -419430401);
         matrices.popMatrix();
      } else {
         boolean up = path.contains("move_up");
         float cx = (float)x + 7.0F;
         float cy = (float)y + (up ? 9.0F : 23.0F);
         if (lit) {
            Smooth.glow(context, Math.round(cx), Math.round(cy), 9, alpha(accent, 0.45F));
         }

         matrices.pushMatrix();
         matrices.translate(cx, cy);
         if (!up) {
            matrices.rotate(3.1415927F);
         }

         Smooth.triangle(context, 0.0F, 0.0F, lit ? 8.0F : 7.0F, lit ? accent : -1275068417);
         matrices.popMatrix();
      }
   }

   public static void darkening(class_332 context, int x, int y, int w, int h) {
      if (!Backdrop.menuActive()) {
         context.method_25294(x, y, x + w, y + h, 805306368);
      }
   }

   public static void button(class_332 context, Object key, int x, int y, int w, int h, boolean active, boolean hovered, float opacity) {
      if (vanillaPlus()) {
         plainButton(context, key, x, y, w, h, active, hovered, opacity);
      } else if (Theme.liquidGlass() && FLAT.contains(key)) {
         float hover = animate(key, active && hovered);
         if (hover > 0.01F) {
            Smooth.rect(context, x, y, w, h, h / 2, alpha(16777215, 0.12F * hover * opacity));
         }
      } else {
         if (Theme.liquidGlass() && !PRIMARY.contains(key)) {
            float hover = animate(key, active && hovered);
            int r = h / 2;
            if (glass(context, x, y, w, h, r, active ? opacity : opacity * 0.55F)) {
               if (active && hover > 0.01F) {
                  neon(context, x, y, w, h, r, accent(), hover * opacity);
               }

               return;
            }
         }

         if (violetStyle()) {
            float hover = animate(key, active && hovered);
            if (!active) {
               crispPanel(
                  context, (float)x, (float)y, (float)w, (float)h, (float)Math.min(4, h / 2), alpha(1118483, 0.75F * opacity), alpha(16777215, 0.06F * opacity)
               );
            } else {
               violetPill(context, x, y, w, h, hover, PRIMARY.contains(key), opacity);
            }
         } else if (home()) {
            homeButton(context, key, x, y, w, h, active, hovered, opacity);
         } else {
            int r = Math.min(h / 2, 8);
            float hover = animate(key, active && hovered);
            if (!active) {
               Smooth.rect(context, x, y, w, h, r, alpha(1777187, 0.7F * opacity));
            } else {
               Smooth.rect(context, x, y, w, h, r, alpha(mix(2895926, 3817287, hover), (0.78F + 0.1F * hover) * opacity));
               if (hover > 0.01F) {
                  Smooth.outline(context, x, y, w, h, r, alpha(16777215, 0.14F * hover * opacity));
               }
            }
         }
      }
   }

   private static void plainButton(class_332 context, Object key, int x, int y, int w, int h, boolean active, boolean hovered, float opacity) {
      float hover = animate(key, active && hovered);
      if (!active) {
         Smooth.rect(context, x, y, w, h, 3, alpha(0, 0.3F * opacity));
         Smooth.outline(context, x, y, w, h, 3, alpha(16777215, 0.08F * opacity));
      } else {
         boolean primary = PRIMARY.contains(key);
         Smooth.rect(context, x, y, w, h, 3, mixAlpha(1879048192, 788529151, hover, opacity));
         Smooth.outline(context, x, y, w, h, 3, mixAlpha(primary ? 1895825407 : 822083583, -788529153, hover, opacity));
      }
   }

   private static int mixAlpha(int from, int to, float t, float opacity) {
      int a = Math.round(((float)(from >>> 24) + (float)((to >>> 24) - (from >>> 24)) * t) * opacity);
      return Math.max(0, Math.min(255, a)) << 24 | mix(from & 16777215, to & 16777215, t);
   }

   private static void homeButton(class_332 context, Object key, int x, int y, int w, int h, boolean active, boolean hovered, float opacity) {
      int r = h / 2;
      float hover = animate(key, active && hovered);
      if (!active) {
         Smooth.rect(context, x, y, w, h, r, alpha(16777215, 0.04F * opacity));
         Smooth.outline(context, x, y, w, h, r, alpha(16777215, 0.06F * opacity));
      } else {
         int accent = accent();
         if (PRIMARY.contains(key)) {
            Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)Math.max(w, h) * 0.75F), alpha(accent, (0.3F + 0.15F * hover) * opacity));
            neon(context, x, y, w, h, r, accent, (0.7F + 0.3F * hover) * opacity);
            Smooth.rect(context, x, y, w, h, r, alpha(mix(accent, 16777215, 0.1F + 0.14F * hover), 0.95F * opacity));
            topLight(context, x, y, w, r, 0.55F * opacity);
         } else {
            if (hover > 0.01F) {
               Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)Math.max(w, h) * 0.6F), alpha(accent, 0.2F * hover * opacity));
            }

            Smooth.rect(context, x, y, w, h, r, alpha(16777215, (0.08F + 0.07F * hover) * opacity));
            Smooth.outline(context, x, y, w, h, r, alpha(16777215, 0.12F * (1.0F - hover) * opacity));
            topLight(context, x, y, w, r, 0.3F * opacity);
            neon(context, x, y, w, h, r, accent, hover * opacity);
         }
      }
   }

   public static void textField(class_332 context, Object key, int x, int y, int w, int h, boolean focused) {
      if (vanillaPlus()) {
         float focus = animate(key, focused);
         Smooth.rect(context, x, y, w, h, 3, alpha(0, 0.55F));
         Smooth.outline(context, x, y, w, h, 3, alpha(16777215, 0.25F + 0.6F * focus));
      } else {
         if (Theme.liquidGlass()) {
            float focus = animate(key, focused);
            int r = Math.min(h / 2, 8);
            if (glass(context, x, y, w, h, r, 1.0F)) {
               neon(context, x, y, w, h, r, accent(), focus);
               return;
            }
         }

         if (violetStyle()) {
            float focus = animate(key, focused);
            crispPanel(
               context,
               (float)x,
               (float)y,
               (float)w,
               (float)h,
               (float)Math.min(h / 2, 4),
               alpha(mix(1316377, 1711136, focus), 0.92F),
               alpha(16777215, 0.12F + 0.38F * focus)
            );
         } else if (home()) {
            float focus = animate(key, focused);
            int r = Math.min(h / 2, 8);
            Smooth.rect(context, x, y, w, h, r, alpha(16777215, 0.06F + 0.04F * focus));
            Smooth.outline(context, x, y, w, h, r, alpha(mix(16777215, accent(), focus), 0.14F + 0.5F * focus));
         } else {
            float focus = animate(key, focused);
            int r = Math.min(h / 2, 6);
            Smooth.rect(context, x, y, w, h, r, alpha(1777187, 0.9F));
            Smooth.outline(context, x, y, w, h, r, alpha(14014942, 0.12F + 0.45F * focus));
         }
      }
   }

   public static void sliderTrack(class_332 context, int x, int y, int w, int h, float opacity) {
      trackX = x;
      trackY = y;
      trackH = h;
      int r = Math.min(h / 2, 8);
      Smooth.rect(context, x, y, w, h, r, alpha(2895926, 0.78F * opacity));
   }

   public static void sliderHandle(class_332 context, int x, int y, int w, int h, boolean highlighted, float opacity) {
      int r = Math.min(trackH / 2, 8);
      if (y == trackY && x + w / 2 > trackX + 2) {
         Smooth.rect(context, trackX, trackY, x + w - trackX, trackH, r, alpha(14014942, 0.2F * opacity));
      }

      Smooth.rect(context, x + 1, y + 2, w - 2, h - 4, Math.min(3, (w - 2) / 2), alpha(highlighted ? 16777215 : 15790836, opacity));
   }

   public static void listBackground(class_332 context, int x, int y, int w, int h) {
      if (vanillaPlus()) {
         context.method_25294(x, y, x + w, y + h, 1073741824);
      } else if (home()) {
         context.method_25296(x, y, x + w, y + h, 436207616, 268435456);
      } else {
         context.method_25296(x, y, x + w, y + h, 939524096, 671088640);
      }
   }

   public static void separators(class_332 context, int x, int top, int bottom, int w) {
      if (vanillaPlus()) {
         context.method_25294(x, top - 1, x + w, top, 822083583);
         context.method_25294(x, bottom, x + w, bottom + 1, 822083583);
      } else {
         int quarter = w / 4;
         context.method_25294(x + quarter, top - 1, x + w - quarter, top, alpha(14014942, 0.3F));
         context.method_25294(x, top - 1, x + quarter, top, alpha(14014942, 0.1F));
         context.method_25294(x + w - quarter, top - 1, x + w, top, alpha(14014942, 0.1F));
         context.method_25294(x + quarter, bottom, x + w - quarter, bottom + 1, alpha(14014942, 0.3F));
         context.method_25294(x, bottom, x + quarter, bottom + 1, alpha(14014942, 0.1F));
         context.method_25294(x + w - quarter, bottom, x + w, bottom + 1, alpha(14014942, 0.1F));
      }
   }

   public static void selection(class_332 context, int x, int y, int w, int h, boolean focused) {
      if (vanillaPlus()) {
         Smooth.rect(context, x, y, w, h, 3, alpha(16777215, focused ? 0.1F : 0.06F));
         Smooth.outline(context, x, y, w, h, 3, alpha(16777215, focused ? 0.85F : 0.45F));
      } else if (violetStyle()) {
         crispPanel(context, (float)x, (float)y, (float)w, (float)h, 4.0F, alpha(16777215, focused ? 0.08F : 0.05F), alpha(16777215, focused ? 0.55F : 0.3F));
      } else if (home()) {
         int accent = accent();
         Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)w * 0.45F), alpha(accent, focused ? 0.12F : 0.07F));
         Smooth.rect(context, x, y, w, h, 10, alpha(16777215, focused ? 0.12F : 0.08F));
         Smooth.outline(context, x, y, w, h, 10, alpha(accent, focused ? 0.7F : 0.4F));
      } else {
         Smooth.rect(context, x, y, w, h, 6, alpha(3817287, focused ? 0.85F : 0.6F));
         Smooth.outline(context, x, y, w, h, 6, alpha(14014942, focused ? 0.45F : 0.2F));
      }
   }

   public static void scrollTrack(class_332 context, int x, int y, int w, int h) {
      Smooth.rect(context, x + w / 2 - 1, y, 2, h, 1, 352321535);
   }

   public static void scrollThumb(class_332 context, int x, int y, int w, int h) {
      if (vanillaPlus()) {
         Smooth.rect(context, x + 1, y, Math.max(2, w - 2), h, 2, -1711276033);
      } else {
         Smooth.rect(context, x + 1, y, Math.max(2, w - 2), h, 2, alpha(14014942, 0.7F));
      }
   }

   public static record Run(String text, int color) {
   }
}
