package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.Hud;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5481;

public final class Notifications {
   private static final int WIDTH = 220;
   private static final int MAX_VISIBLE = 4;
   private static final float ENTER = 0.45F;
   private static final float EXIT = 0.45F;
   private static final List<Notifications.Entry> ENTRIES = new ArrayList<>();
   private static long lastSound;
   private static long lastFrame = System.nanoTime();

   private Notifications() {
   }

   public static void push(String title, String message, Notifications.Type type) {
      class_310 mc = class_310.method_1551();
      mc.execute(() -> {
         Hud hud = SoberClient.MODULES.get(Hud.class);
         float duration = hud != null ? (float)hud.notificationTime.get().intValue() / 10.0F : 4.5F;

         for (Notifications.Entry entry : ENTRIES) {
            if (entry.title.equals(title) && entry.message.equals(message) && entry.age < duration - 0.45F) {
               entry.age = Math.min(entry.age, 0.45F);
               entry.count++;
               return;
            }
         }

         ENTRIES.add(0, new Notifications.Entry(title, message, type, duration));
         if (ENTRIES.size() > 12) {
            ENTRIES.remove(ENTRIES.size() - 1);
         }

         long now = System.currentTimeMillis();
         boolean quiet = now - lastSound < 350L || UiSounds.playedWithin(400L);
         if (hud != null && hud.notificationSound.get() && mc.method_1483() != null && !quiet) {
            lastSound = now;
            Hud.SoundType sound = hud.soundType.get();
            float volume = (float)hud.notificationVolume.get().intValue() / 100.0F * sound.volume;

            float shift = switch (type) {
               case INFO, SUCCESS -> 1.0F;
               case WARNING -> 0.9F;
               case ERROR -> 0.8F;
            };
            mc.method_1483().method_4873(class_1109.method_4757(sound.event(), sound.pitch * shift, volume));
         }
      });
   }

   private static float easeOutCubic(float t) {
      float f = 1.0F - t;
      return 1.0F - f * f * f;
   }

   private static float easeInCubic(float t) {
      return t * t * t;
   }

   private static int withAlpha(int color, float alpha) {
      int a = Math.round((float)(color >>> 24) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return a << 24 | color & 16777215;
   }

   public static void render(class_332 context) {
      render(context, false);
   }

   public static void render(class_332 context, boolean bottomRight) {
      long now = System.nanoTime();
      float delta = Math.min(0.05F, (float)(now - lastFrame) / 1.0E9F);
      lastFrame = now;
      if (!ENTRIES.isEmpty()) {
         class_310 mc = class_310.method_1551();
         class_327 font = mc.field_1772;
         float scale = HudRenderer.uiScale(context);
         int screenWidth = Math.round((float)context.method_51421() / scale);
         int screenHeight = Math.round((float)context.method_51443() / scale);
         context.method_51448().pushMatrix();
         context.method_51448().scale(scale, scale);
         float y = bottomRight ? 6.0F : (float)HudRenderer.topClearance((screenWidth - 220) / 2, (screenWidth + 220) / 2);
         int shown = 0;
         Iterator<Notifications.Entry> it = ENTRIES.iterator();

         while (it.hasNext()) {
            Notifications.Entry entry = it.next();
            entry.age += delta;
            if (entry.age >= entry.duration) {
               it.remove();
            } else if (shown < 4) {
               shown++;
               float in = easeOutCubic(Math.min(1.0F, entry.age / 0.45F));
               float out = easeInCubic(Math.max(0.0F, (entry.age - (entry.duration - 0.45F)) / 0.45F));
               float visible = in * (1.0F - out);
               List<class_5481> lines = font.method_1728(Theme.text(entry.message), 178);
               if (lines.size() > 3) {
                  lines = lines.subList(0, 3);
               }

               int height = 22 + lines.size() * 10;
               entry.shownY = entry.shownY + (y - entry.shownY) * (1.0F - (float)Math.exp((double)(-14.0F * delta)));
               if (entry.age < 0.05F) {
                  entry.shownY = y;
               }

               int x = bottomRight ? screenWidth - 220 - 6 : (screenWidth - 220) / 2;
               int iy = bottomRight
                  ? screenHeight - Math.round(entry.shownY) - height + Math.round((1.0F - in) * 14.0F + out * 10.0F)
                  : Math.round(entry.shownY - (1.0F - in) * 14.0F - out * 10.0F);
               int barColor = entry.type == Notifications.Type.INFO ? 16053493 : entry.type.color;
               if (Theme.liquidGlass()) {
                  HudRenderer.glass(context, x, iy, 220, height, 12, visible);
               } else {
                  Smooth.rect(context, x, iy, 220, height, 3, withAlpha(HudPalette.card(), visible));
                  Smooth.outline(context, x, iy, 220, height, 3, withAlpha(-13750733, visible));
               }

               Smooth.rect(context, x + 1, iy + 4, 2, height - 8, 1, withAlpha(0xFF000000 | barColor, visible));
               HudRenderer.logo(context, (float)(x + 8), (float)(iy + 10), 10.0F, withAlpha(-1, visible));
               HudRenderer.label(context, entry.title.toUpperCase(Locale.ROOT), (float)(x + 33), (float)iy + 5.6F, withAlpha(-2565922, visible));
               if (entry.count > 1) {
                  String count = "x" + entry.count;
                  HudRenderer.label(context, count, (float)(x + 220 - 9) - HudRenderer.labelWidth(count), (float)iy + 5.6F, withAlpha(-5329226, visible));
               }

               int ly = iy + 19;

               for (class_5481 line : lines) {
                  context.method_51430(font, line, x + 33, ly, withAlpha(-723723, visible), false);
                  ly += 10;
               }

               float remaining = 1.0F - Math.min(1.0F, entry.age / entry.duration);
               int barWidth = Math.round(204.0F * remaining);
               context.method_25294(x + 8, iy + height - 3, x + 220 - 8, iy + height - 2, withAlpha(653587701, visible));
               if (barWidth > 0) {
                  context.method_25294(x + 8, iy + height - 3, x + 8 + barWidth, iy + height - 2, withAlpha(0xFF000000 | barColor, visible));
               }

               y += (float)(height + 5) * visible;
            }
         }

         context.method_51448().popMatrix();
      }
   }

   private static final class Entry {
      final String title;
      final String message;
      final Notifications.Type type;
      final float duration;
      float age;
      float shownY = 6.0F;
      int count = 1;

      Entry(String title, String message, Notifications.Type type, float duration) {
         this.title = title;
         this.message = message;
         this.type = type;
         this.duration = duration;
      }
   }

   public static enum Type {
      INFO(0),
      SUCCESS(-11746422),
      WARNING(-1525686),
      ERROR(-2075307);

      final int color;

      private Type(int color) {
         this.color = color;
      }
   }
}
