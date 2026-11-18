package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.mixin.ChatScreenAccessor;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_156;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_5481;
import org.joml.Matrix3x2fStack;

public class AnimatedTyping extends Module {
   private final Setting<Integer> speed = this.add(new IntSetting("Speed", "How long a new letter takes to appear, in milliseconds.", 180, 60, 500));
   private final Setting<Integer> slide = this.add(new IntSetting("Slide", "How far new letters slide up into place, in pixels.", 4, 0, 10));
   private final Setting<Boolean> pop = this.add(new BoolSetting("Pop", "New letters grow from a little smaller to full size.", true));
   private final Setting<Boolean> wave = this.add(
      new BoolSetting("Wave", "Pasted text and chat history roll in letter by letter instead of all at once.", true)
   );
   private final Setting<Boolean> smoothCursor = this.add(new BoolSetting("Smooth Cursor", "The text cursor glides to its new spot instead of jumping.", true));
   private final Setting<Boolean> allFields = this.add(
      new BoolSetting("All Text Fields", "Also animates search boxes and other text fields, not only the chat.", false)
   );

   public AnimatedTyping() {
      super("Animated Typing", "Letters fade, slide and pop in while you type in the chat, and the cursor glides smoothly.", Category.CLIENT);
   }

   private static AnimatedTyping instance() {
      return SoberClient.MODULES.get(AnimatedTyping.class);
   }

   public static boolean applies(class_342 field) {
      AnimatedTyping typing = instance();
      if (typing == null || !typing.isActive()) {
         return false;
      } else if (typing.allFields.get()) {
         return true;
      } else {
         if (mc.field_1755 instanceof class_408 chat && ((ChatScreenAccessor)chat).violet$chatField() == field) {
            return true;
         }

         return false;
      }
   }

   public static boolean smoothCursor() {
      AnimatedTyping typing = instance();
      return typing != null && typing.smoothCursor.get();
   }

   public static long[] track(String last, String now, long[] times, boolean animate) {
      if (now == null) {
         return new long[0];
      } else if (last == null || times == null || times.length != last.length()) {
         return new long[now.length()];
      } else if (last.equals(now)) {
         return times;
      } else {
         int max = Math.min(last.length(), now.length());
         int prefix = 0;

         while (prefix < max && last.charAt(prefix) == now.charAt(prefix)) {
            prefix++;
         }

         int suffix = 0;

         while (suffix < max - prefix && last.charAt(last.length() - 1 - suffix) == now.charAt(now.length() - 1 - suffix)) {
            suffix++;
         }

         long[] out = new long[now.length()];
         System.arraycopy(times, 0, out, 0, prefix);
         System.arraycopy(times, last.length() - suffix, out, now.length() - suffix, suffix);
         int inserted = now.length() - suffix - prefix;
         if (animate && inserted > 0) {
            AnimatedTyping typing = instance();
            long stamp = class_156.method_658();
            long step = typing != null && typing.wave.get() && inserted > 1 ? (long)Math.max(4, Math.min(18, 360 / inserted)) : 0L;

            for (int i = 0; i < inserted; i++) {
               out[prefix + i] = stamp + (long)i * step;
            }
         }

         return out;
      }
   }

   public static void draw(class_332 context, class_327 renderer, class_5481 text, int x, int y, int color, boolean shadow, long[] times, int start) {
      AnimatedTyping typing = instance();
      long now = class_156.method_658();
      float duration = typing == null ? 180.0F : (float)typing.speed.get().intValue();
      float rise = typing == null ? 4.0F : (float)typing.slide.get().intValue();
      boolean grow = typing == null || typing.pop.get();
      float[] cursor = new float[]{(float)x};
      int[] index = new int[]{start};
      Matrix3x2fStack matrices = context.method_51448();
      text.accept((ignored, style, codePoint) -> {
         class_5481 glyph = class_5481.method_30741(codePoint, style);
         int width = renderer.method_30880(glyph);
         int at = index[0];
         index[0] += Character.charCount(codePoint);
         long stamp = at >= 0 && at < times.length ? times[at] : 0L;
         float progress = stamp == 0L ? 1.0F : Math.max(0.0F, Math.min(1.0F, (float)(now - stamp) / duration));
         if (progress >= 1.0F) {
            context.method_51430(renderer, glyph, Math.round(cursor[0]), y, color, shadow);
         } else if (progress > 0.0F) {
            float eased = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
            int alpha = Math.round((float)(color >>> 24) * eased);
            if (alpha > 3) {
               float scale = grow ? 0.7F + 0.3F * eased : 1.0F;
               matrices.pushMatrix();
               matrices.translate(cursor[0] + (float)width / 2.0F, (float)y + 4.0F + rise * (1.0F - eased));
               matrices.scale(scale, scale);
               context.method_51430(renderer, glyph, -width / 2, -4, alpha << 24 | color & 16777215, shadow);
               matrices.popMatrix();
            }
         }

         cursor[0] += (float)width;
         return true;
      });
   }

   public static float glide(float current, float target, long last, long now) {
      if (!Float.isNaN(current) && last != 0L && now - last <= 700L && !(Math.abs(target - current) > 400.0F)) {
         float dt = (float)(now - last) / 1000.0F;
         float value = current + (target - current) * (1.0F - (float)Math.exp((double)(-dt * 28.0F)));
         return Math.abs(value - target) < 0.05F ? target : value;
      } else {
         return target;
      }
   }

   @Override
   public boolean showInHud() {
      return false;
   }
}
