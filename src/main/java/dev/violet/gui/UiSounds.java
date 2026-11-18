package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.ClickGui;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_437;
import net.minecraft.class_1113.class_1114;

public final class UiSounds {
   private static long lastHover;
   private static int burst;
   private static long lastPlayed;

   private UiSounds() {
   }

   private static ClickGui gui() {
      return SoberClient.MODULES.get(ClickGui.class);
   }

   public static boolean playedWithin(long millis) {
      return System.currentTimeMillis() - lastPlayed < millis;
   }

   private static boolean silk() {
      return gui().soundPack.get() == ClickGui.SoundPack.Silk;
   }

   private static boolean pulse() {
      return gui().soundPack.get() == ClickGui.SoundPack.Pulse || glide();
   }

   private static boolean glide() {
      return gui().soundPack.get() == ClickGui.SoundPack.Glide;
   }

   private static void play(class_3414 sound, float pitch, float volume) {
      class_310 mc = class_310.method_1551();
      float master = (float)gui().volume.get().intValue() / 100.0F;
      if (!(master <= 0.0F) && mc.method_1483() != null) {
         mc.method_1483().method_4873(class_1109.method_4757(sound, pitch, volume * master));
      }
   }

   private static void silk(String name, float pitch, float volume) {
      class_310 mc = class_310.method_1551();
      float master = (float)gui().volume.get().intValue() / 100.0F;
      if (!(master <= 0.0F) && mc.method_1483() != null) {
         class_2960 id = class_2960.method_60655("violet", "ui." + name);
         mc.method_1483()
            .method_4873(
               new class_1109(
                  id, class_3419.field_61058, volume * master, pitch, class_1113.method_43221(), false, 0, class_1114.field_5478, 0.0, 0.0, 0.0, true
               )
            );
      }
   }

   private static float jitter(float amount) {
      return 1.0F + (float)(Math.random() * (double)amount * 2.0 - (double)amount);
   }

   public static void open() {
      if (gui().openSound.get()) {
         if (glide()) {
            silk("glide_open", 1.0F, 0.45F);
         } else if (pulse()) {
            silk("pulse_open", 1.0F, 0.3F);
         } else if (silk()) {
            silk("silk_open", 1.0F, 0.55F);
         } else {
            play(class_3417.field_26980, 1.35F, 0.8F);
         }
      }
   }

   public static void close() {
      if (gui().openSound.get()) {
         if (glide()) {
            silk("glide_close", 1.0F, 0.45F);
         } else if (pulse()) {
            silk("pulse_close", 1.0F, 0.3F);
         } else if (silk()) {
            silk("silk_close", 1.0F, 0.45F);
         } else {
            play(class_3417.field_26980, 0.95F, 0.6F);
         }
      }
   }

   public static void hover() {
      long now = System.currentTimeMillis();
      if (gui().hoverSound.get()) {
         if (now - lastHover > 400L) {
            burst = 0;
         }

         if (now - lastHover >= 90L && burst < 4) {
            lastHover = now;
            burst++;
            hoverSound();
         } else {
            lastHover = now;
         }
      }
   }

   public static void previewHover() {
      class_437 screen = class_310.method_1551().field_1755;
      if (screen instanceof ModuleMenu) {
         lastHover = System.currentTimeMillis();
         hoverSound();
      }
   }

   private static void hoverSound() {
      float jitter = (float)(Math.random() * 0.08 - 0.04);
      switch ((ClickGui.HoverStyle)gui().hoverStyle.get()) {
         case Glide:
            silk("glide_hover", jitter(0.02F), 0.2F);
            break;
         case Silk:
            silk("silk_hover", jitter(0.03F), 0.35F);
            break;
         case Bubble:
            play(class_3417.field_54891, 1.45F + jitter, 0.45F);
            break;
         case Crystal:
            play(class_3417.field_26982, 1.7F + jitter, 0.3F);
            break;
         case Wood:
            play(class_3417.field_40067, 1.75F + jitter, 0.22F);
            break;
         case Tick:
            play((class_3414)class_3417.field_15204.comp_349(), 1.8F + jitter, 0.18F);
            break;
         case Classic:
            play((class_3414)class_3417.field_15015.comp_349(), 1.9F + jitter * 1.5F, 0.2F);
      }
   }

   public static void key(float volume) {
      if (glide()) {
         silk("glide_type", jitter(0.02F), 0.22F * volume + 0.1F);
      } else if (!silk() && !pulse()) {
         float pitch = 1.9F + (float)(Math.random() * 0.12 - 0.06);
         play((class_3414)class_3417.field_15015.comp_349(), pitch, 0.2F * volume);
      } else {
         silk("silk_type", jitter(0.025F), 0.5F * volume + 0.15F);
      }
   }

   public static void click() {
      lastPlayed = System.currentTimeMillis();
      if (gui().clickSound.get()) {
         if (glide()) {
            silk("glide_click", 1.0F, 0.4F);
         } else if (pulse()) {
            silk("pulse_swish", 1.0F, 1.0F);
         } else if (silk()) {
            silk("silk_nav", 1.0F, 0.55F);
         } else {
            play((class_3414)class_3417.field_15015.comp_349(), 1.6F, 0.25F);
         }
      }
   }

   public static void select() {
      lastPlayed = System.currentTimeMillis();
      if (gui().clickSound.get()) {
         if (glide()) {
            silk("glide_click", 1.19F, 0.4F);
         } else if (pulse()) {
            silk("pulse_swish", 1.06F, 1.0F);
         } else if (silk()) {
            silk("silk_select", 1.0F, 0.5F);
         } else {
            play((class_3414)class_3417.field_15015.comp_349(), 1.75F, 0.25F);
         }
      }
   }

   public static void keybind(boolean on) {
      lastPlayed = System.currentTimeMillis();
      if (gui().hoverSound.get()) {
         if (glide()) {
            silk("glide_click", on ? 1.12F : 0.94F, 0.35F);
         } else if (pulse()) {
            silk("pulse_swish", on ? 1.05F : 0.95F, 0.9F);
         } else if (silk()) {
            silk("silk_select", on ? 1.06F : 0.94F, 0.45F);
         } else {
            play((class_3414)class_3417.field_15015.comp_349(), on ? 1.9F : 1.75F, 0.2F);
         }
      }
   }

   public static void moduleToggle(boolean on) {
      lastPlayed = System.currentTimeMillis();
      if (gui().moduleSound.get()) {
         if (glide()) {
            silk(on ? "glide_on" : "glide_off", 1.0F, 0.6F);
         } else if (pulse()) {
            silk(on ? "pulse_on" : "pulse_off", 1.0F, 0.6F);
         } else if (silk()) {
            silk(on ? "silk_on" : "silk_off", 1.0F, 0.5F);
         } else {
            play((class_3414)class_3417.field_14622.comp_349(), on ? 1.7F : 1.2F, 0.3F);
         }
      }
   }

   public static void intro() {
      play(class_3417.field_14703, 1.5F, 0.7F);
   }

   public static void reveal() {
      play(class_3417.field_26980, 1.2F, 0.9F);
   }

   public static void toggle(boolean on) {
      lastPlayed = System.currentTimeMillis();
      if (gui().clickSound.get()) {
         if (glide()) {
            silk(on ? "glide_check_on" : "glide_check_off", 1.0F, 0.45F);
         } else if (pulse()) {
            silk("pulse_check", on ? 1.04F : 0.94F, 0.6F);
         } else if (silk()) {
            silk(on ? "silk_on" : "silk_off", 1.0F, 0.5F);
         } else {
            play((class_3414)class_3417.field_14622.comp_349(), on ? 1.7F : 1.2F, 0.3F);
         }
      }
   }
}
