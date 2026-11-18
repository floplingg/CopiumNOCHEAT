package dev.violet.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import net.minecraft.class_1102;
import net.minecraft.class_1111;
import net.minecraft.class_1113;
import net.minecraft.class_1144;
import net.minecraft.class_1146;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3419;
import net.minecraft.class_4231;
import net.minecraft.class_1111.class_1112;
import net.minecraft.class_1113.class_1114;

public final class KeySynth {
   private static final int RATE = 44100;
   private static final AudioFormat FORMAT = new AudioFormat(44100.0F, 16, 1, true, false);
   private static final AudioFormat RECORDED_FORMAT = new AudioFormat(48000.0F, 16, 2, true, false);
   private static final Map<String, Integer> COUNTS = new ConcurrentHashMap<>();
   private static final Random RANDOM = new Random();
   private static final String PREFIX = "sounds/keys/";
   private static final class_1146 SET = new class_1146(class_2960.method_60655("violet", "keys"), null);
   private static final Map<Integer, Double> COLUMNS = new HashMap<>();

   private KeySynth() {
   }

   public static boolean handles(class_2960 location) {
      return location.method_12836().equals("violet") && location.method_12832().startsWith("sounds/keys/");
   }

   public static void play(KeySynth.Profile profile, int key, boolean release, float volume, boolean stereo) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.method_1483() != null && !(volume <= 0.0F)) {
         KeySynth.Kind kind = kind(key, release);
         boolean recorded = recorded(profile);
         KeySynth.Kind slot = recorded ? recordedSlot(profile, kind) : kind;
         int variants = recorded ? count(profile, slot) : kind.variants;
         if (variants > 0) {
            boolean realSpace = recorded && (slot == KeySynth.Kind.SPACE || slot == KeySynth.Kind.SPACE_RELEASE);
            int hash = mixKey(key);
            int variant = realSpace ? RANDOM.nextInt(variants) : Math.floorMod(hash, variants);
            double pan = stereo && !recorded ? pan(key) : 0.0;
            float pitch = realSpace ? 1.0F : 1.0F + ((float)Math.floorMod(hash >>> 8, 1000) / 1000.0F - 0.5F) * 0.07F;
            if (!recorded || realSpace || kind != KeySynth.Kind.SPACE && kind != KeySynth.Kind.SPACE_RELEASE) {
               if (recorded && (kind == KeySynth.Kind.STAB || kind == KeySynth.Kind.STAB_RELEASE)) {
                  pitch *= 0.95F;
               }
            } else {
               pitch *= 0.9F;
            }

            class_2960 id = class_2960.method_60655("violet", "keys/" + profile.name().toLowerCase() + "_" + slot.name().toLowerCase() + "_" + variant);
            mc.method_1483().method_4873(new KeySynth.KeySound(id, Math.min(1.0F, volume), pitch, pan));
         }
      }
   }

   public static void playMouse(float volume) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.method_1483() != null && !(volume <= 0.0F)) {
         mc.method_1483().method_4873(new KeySynth.KeySound(class_2960.method_60655("violet", "keys/mouse_click_0"), Math.min(1.0F, volume), 1.0F, 0.0));
      }
   }

   private static int mixKey(int key) {
      int h = key * -1640531535;
      h ^= h >>> 15;
      h *= -2048144789;
      return h ^ h >>> 13;
   }

   private static KeySynth.Kind kind(int key, boolean release) {
      if (key == 32) {
         return release ? KeySynth.Kind.SPACE_RELEASE : KeySynth.Kind.SPACE;
      } else {
         boolean stab = key == 257 || key == 335 || key == 259 || key == 340 || key == 344 || key == 258 || key == 280;
         if (stab) {
            return release ? KeySynth.Kind.STAB_RELEASE : KeySynth.Kind.STAB;
         } else {
            return release ? KeySynth.Kind.RELEASE : KeySynth.Kind.PRESS;
         }
      }
   }

   private static double pan(int key) {
      double column = COLUMNS.getOrDefault(key, 7.0);
      return Math.max(-1.0, Math.min(1.0, column / 14.5 * 2.0 - 1.0)) * 0.55;
   }

   public static class_4231 create(class_2960 location) {
      String name = location.method_12832().substring("sounds/keys/".length());
      if (name.endsWith(".ogg")) {
         name = name.substring(0, name.length() - 4);
      }

      if (name.startsWith("mouse_")) {
         ByteBuffer mouse = recorded("mouse/" + name.substring(6, name.lastIndexOf(95) + 1), 0);
         if (mouse != null) {
            return new class_4231(mouse, RECORDED_FORMAT);
         }
      }

      String[] parts = name.split("_");
      KeySynth.Profile profile = KeySynth.Profile.Creamy;

      for (KeySynth.Profile p : KeySynth.Profile.values()) {
         if (p.name().equalsIgnoreCase(parts[0])) {
            profile = p;
         }
      }

      int variant = 0;

      try {
         variant = Integer.parseInt(parts[parts.length - 1]);
      } catch (NumberFormatException var13) {
      }

      String kindName = name.substring(parts[0].length() + 1, name.length() - parts[parts.length - 1].length() - 1).toUpperCase();
      KeySynth.Kind kind = KeySynth.Kind.PRESS;

      for (KeySynth.Kind k : KeySynth.Kind.values()) {
         if (k.name().equals(kindName)) {
            kind = k;
         }
      }

      if (recorded(profile)) {
         ByteBuffer recorded = recorded(folder(profile) + prefix(recordedSlot(profile, kind)), variant);
         if (recorded != null) {
            return new class_4231(recorded, RECORDED_FORMAT);
         }

         profile = KeySynth.Profile.Creamy;
      }

      short[] samples = render(profile, kind, variant);
      ByteBuffer buffer = ByteBuffer.allocateDirect(samples.length * 2).order(ByteOrder.LITTLE_ENDIAN);

      for (short sample : samples) {
         buffer.putShort(sample);
      }

      buffer.flip();
      return new class_4231(buffer, FORMAT);
   }

   private static boolean recorded(KeySynth.Profile profile) {
      return profile == KeySynth.Profile.Recorded
         || profile == KeySynth.Profile.Fb67
         || profile == KeySynth.Profile.He60v2
         || profile == KeySynth.Profile.Frost65;
   }

   private static String folder(KeySynth.Profile profile) {
      return switch (profile) {
         case Fb67 -> "fb67/";
         case He60v2 -> "60he_v2/";
         case Frost65 -> "frost65/";
         default -> "";
      };
   }

   private static KeySynth.Kind recordedSlot(KeySynth.Profile profile, KeySynth.Kind kind) {
      if ((kind == KeySynth.Kind.SPACE || kind == KeySynth.Kind.SPACE_RELEASE) && count(profile, kind) > 0) {
         return kind;
      } else {
         return kind.release() ? KeySynth.Kind.RELEASE : KeySynth.Kind.PRESS;
      }
   }

   private static String prefix(KeySynth.Kind slot) {
      return slot.name().toLowerCase() + "_";
   }

   private static int count(KeySynth.Profile profile, KeySynth.Kind slot) {
      return COUNTS.computeIfAbsent(folder(profile) + prefix(slot), path -> {
         int n = 0;

         while (n < 64 && KeySynth.class.getResource("/assets/violet/keysamples/" + path + n + ".pcm") != null) {
            n++;
         }

         return n;
      });
   }

   private static ByteBuffer recorded(String prefix, int variant) {
      try {
         ByteBuffer var5;
         try (InputStream in = KeySynth.class.getResourceAsStream("/assets/violet/keysamples/" + prefix + variant + ".pcm")) {
            if (in == null) {
               return null;
            }

            byte[] data = in.readAllBytes();
            ByteBuffer buffer = ByteBuffer.allocateDirect(data.length).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(data);
            buffer.flip();
            var5 = buffer;
         }

         return var5;
      } catch (IOException var8) {
         return null;
      }
   }

   private static KeySynth.Mode[] modes(KeySynth.Profile profile) {
      return switch (profile) {
         case Recorded, Fb67, He60v2, Frost65, Creamy -> new KeySynth.Mode[]{
         new KeySynth.Mode(205.0, 0.02, 1.0),
         new KeySynth.Mode(520.0, 0.011, 0.55),
         new KeySynth.Mode(1240.0, 0.0055, 0.24),
         new KeySynth.Mode(2550.0, 0.0028, 0.09)
      };
         case Thocky -> new KeySynth.Mode[]{
         new KeySynth.Mode(150.0, 0.028, 1.0),
         new KeySynth.Mode(370.0, 0.015, 0.62),
         new KeySynth.Mode(880.0, 0.0075, 0.2),
         new KeySynth.Mode(1900.0, 0.003, 0.06)
      };
         case Clicky -> new KeySynth.Mode[]{
         new KeySynth.Mode(620.0, 0.009, 0.45),
         new KeySynth.Mode(2900.0, 0.0032, 0.75),
         new KeySynth.Mode(4300.0, 0.0022, 0.5),
         new KeySynth.Mode(6100.0, 0.0014, 0.25)
      };
         case Marble -> new KeySynth.Mode[]{
         new KeySynth.Mode(330.0, 0.014, 0.8),
         new KeySynth.Mode(1050.0, 0.0085, 0.75),
         new KeySynth.Mode(2150.0, 0.0045, 0.4),
         new KeySynth.Mode(3600.0, 0.0025, 0.18)
      };
      };
   }

   private static double noiseLevel(KeySynth.Profile profile) {
      return switch (profile) {
         case Recorded, Fb67, He60v2, Frost65, Creamy -> 0.28;
         case Thocky -> 0.22;
         case Clicky -> 0.55;
         case Marble -> 0.35;
      };
   }

   private static double cutoff(KeySynth.Profile profile) {
      return switch (profile) {
         case Recorded, Fb67, He60v2, Frost65, Creamy -> 4200.0;
         case Thocky -> 3200.0;
         case Clicky -> 9500.0;
         case Marble -> 6500.0;
      };
   }

   private static short[] render(KeySynth.Profile profile, KeySynth.Kind kind, int variant) {
      Random random = new Random((long)profile.ordinal() * 7919L + (long)kind.ordinal() * 104729L + (long)variant * 15485863L + 17L);
      boolean release = kind.release();
      boolean space = kind == KeySynth.Kind.SPACE || kind == KeySynth.Kind.SPACE_RELEASE;
      boolean stab = kind == KeySynth.Kind.STAB || kind == KeySynth.Kind.STAB_RELEASE;
      double freqScale = release ? 1.45 : 1.0;
      double decayScale = release ? 0.6 : 1.0;
      if (space) {
         freqScale *= 0.74;
         decayScale *= 1.65;
      } else if (stab) {
         freqScale *= 0.86;
         decayScale *= 1.3;
      }

      double duration = (release ? 0.055 : 0.085) * (space ? 1.6 : (stab ? 1.3 : 1.0));
      int n = (int)(44100.0 * duration);
      double[] out = new double[n];
      KeySynth.Mode[] modes = modes(profile);
      hit(out, 0, modes, freqScale, decayScale, 1.0, random, profile, release);
      if (space || stab) {
         int delay = (int)(44100.0 * (space ? 0.009 + random.nextDouble() * 0.005 : 0.006 + random.nextDouble() * 0.003));
         hit(out, delay, modes, freqScale * 1.12, decayScale * 0.7, space ? 0.28 : 0.16, random, profile, release);
      }

      if (profile == KeySynth.Profile.Clicky && !release) {
         int delay = (int)(44100.0 * (0.0045 + random.nextDouble() * 0.0015));
         hit(out, delay, modes, 1.08, 0.8, 0.6, random, profile, false);
      }

      double alpha = 1.0 - Math.exp(-6.283185307179586 * cutoff(profile) * (release ? 1.15 : 1.0) / 44100.0);
      double low = 0.0;
      double dc = 0.0;
      double peak = 1.0E-9;

      for (int i = 0; i < n; i++) {
         low += alpha * (out[i] - low);
         dc += 0.0025 * (low - dc);
         out[i] = low - dc;
         peak = Math.max(peak, Math.abs(out[i]));
      }

      double target = release ? 0.5 : (space ? 0.95 : 0.9);
      int fade = Math.min(n, 176);
      short[] samples = new short[n];

      for (int i = 0; i < n; i++) {
         double v = out[i] / peak * target;
         if (i >= n - fade) {
            v *= (double)(n - i) / (double)fade;
         }

         samples[i] = (short)((int)Math.round(Math.max(-1.0, Math.min(1.0, v)) * 32767.0));
      }

      return samples;
   }

   private static void hit(
      double[] out,
      int start,
      KeySynth.Mode[] modes,
      double freqScale,
      double decayScale,
      double gain,
      Random random,
      KeySynth.Profile profile,
      boolean release
   ) {
      int n = out.length;

      for (KeySynth.Mode mode : modes) {
         double freq = mode.freq() * freqScale * (1.0 + random.nextGaussian() * 0.05);
         double decay = mode.decay() * decayScale * (1.0 + random.nextGaussian() * 0.12);
         double amp = mode.amp() * gain * (1.0 + random.nextGaussian() * 0.08) * (release ? 0.7 : 1.0);
         double phase = random.nextDouble() * 3.141592653589793 * 2.0;
         double chirp = 0.06 + random.nextDouble() * 0.05;

         for (int i = start; i < n; i++) {
            double t = (double)(i - start) / 44100.0;
            double env = Math.exp(-t / Math.max(8.0E-4, decay)) * (1.0 - Math.exp(-t / 3.5E-4));
            if (env < 1.0E-5 && t > 0.003) {
               break;
            }

            double f = freq * (1.0 + chirp * Math.exp(-t / 0.004));
            phase += 6.283185307179586 * f / 44100.0;
            out[i] += amp * env * Math.sin(phase);
         }
      }

      double noise = noiseLevel(profile) * gain * (release ? 0.6 : 1.0);
      double noiseDecay = profile == KeySynth.Profile.Clicky ? 0.0022 : 0.0016;
      double filtered = 0.0;

      for (int i = start; i < n; i++) {
         double t = (double)(i - start) / 44100.0;
         double env = Math.exp(-t / noiseDecay);
         if (env < 1.0E-4) {
            break;
         }

         filtered += 0.45 * (random.nextGaussian() - filtered);
         out[i] += noise * env * filtered;
      }
   }

   static {
      String digits = "1234567890";

      for (int i = 0; i < digits.length(); i++) {
         COLUMNS.put(Integer.valueOf(digits.charAt(i)), 1.0 + (double)i);
      }

      String top = "QWERTYUIOP";
      String middle = "ASDFGHJKL";
      String bottom = "ZXCVBNM";

      for (int i = 0; i < top.length(); i++) {
         COLUMNS.put(Integer.valueOf(top.charAt(i)), 1.5 + (double)i);
      }

      for (int i = 0; i < middle.length(); i++) {
         COLUMNS.put(Integer.valueOf(middle.charAt(i)), 1.75 + (double)i);
      }

      for (int i = 0; i < bottom.length(); i++) {
         COLUMNS.put(Integer.valueOf(bottom.charAt(i)), 2.25 + (double)i);
      }

      COLUMNS.put(96, 0.0);
      COLUMNS.put(256, 0.0);
      COLUMNS.put(258, 0.5);
      COLUMNS.put(280, 0.8);
      COLUMNS.put(340, 1.0);
      COLUMNS.put(341, 0.6);
      COLUMNS.put(342, 2.4);
      COLUMNS.put(32, 6.5);
      COLUMNS.put(346, 9.5);
      COLUMNS.put(345, 13.0);
      COLUMNS.put(45, 11.0);
      COLUMNS.put(61, 12.0);
      COLUMNS.put(259, 13.5);
      COLUMNS.put(91, 11.5);
      COLUMNS.put(93, 12.5);
      COLUMNS.put(92, 13.75);
      COLUMNS.put(59, 10.75);
      COLUMNS.put(39, 11.75);
      COLUMNS.put(257, 13.4);
      COLUMNS.put(44, 9.25);
      COLUMNS.put(46, 10.25);
      COLUMNS.put(47, 11.25);
      COLUMNS.put(344, 13.2);
      COLUMNS.put(263, 15.5);
      COLUMNS.put(264, 16.5);
      COLUMNS.put(265, 16.5);
      COLUMNS.put(262, 17.5);

      for (int i = 0; i < 12; i++) {
         COLUMNS.put(290 + i, 1.0 + (double)i * 1.05);
      }
   }

   private static final class KeySound extends class_1102 {
      KeySound(class_2960 id, float volume, float pitch, double pan) {
         super(id, class_3419.field_61058, class_1113.method_43221());
         this.field_5442 = volume;
         this.field_5441 = pitch;
         this.field_18936 = true;
         this.field_5440 = class_1114.field_5478;
         this.field_5439 = pan;
         this.field_5450 = 0.0;
         this.field_5449 = pan == 0.0 ? 0.0 : -1.0;
      }

      public class_1146 method_4783(class_1144 soundManager) {
         this.field_5444 = new class_1111(this.field_5448, random -> 1.0F, random -> 1.0F, 1, class_1112.field_5474, false, false, 16);
         return KeySynth.SET;
      }
   }

   public static enum Kind {
      PRESS(8),
      RELEASE(6),
      SPACE(4),
      SPACE_RELEASE(3),
      STAB(4),
      STAB_RELEASE(3);

      final int variants;

      private Kind(int variants) {
         this.variants = variants;
      }

      boolean release() {
         return this == RELEASE || this == SPACE_RELEASE || this == STAB_RELEASE;
      }
   }

   private static record Mode(double freq, double decay, double amp) {
   }

   public static enum Profile {
      Recorded,
      Fb67,
      He60v2,
      Frost65,
      Creamy,
      Thocky,
      Clicky,
      Marble;
   }
}
