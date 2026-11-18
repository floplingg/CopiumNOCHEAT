package dev.violet.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_332;

final class TypedText {
   private static final float IN = 0.16F;
   private static final float OUT = 0.14F;
   private static final float RISE = 3.0F;
   private final List<TypedText.Glyph> glyphs = new ArrayList<>();
   private final List<TypedText.Ghost> ghosts = new ArrayList<>();
   private String value = "";
   private float caretX = 0.0F / 0.0F;
   private float placeholder = 1.0F;
   private long lastFrame = System.nanoTime();
   private long typedAt;

   void sync(String next, TypedText.Measure measure) {
      if (!next.equals(this.value)) {
         int prefix = 0;

         while (prefix < this.value.length() && prefix < next.length() && this.value.charAt(prefix) == next.charAt(prefix)) {
            prefix++;
         }

         int suffix = 0;

         while (
            suffix < this.value.length() - prefix
               && suffix < next.length() - prefix
               && this.value.charAt(this.value.length() - 1 - suffix) == next.charAt(next.length() - 1 - suffix)
         ) {
            suffix++;
         }

         long now = System.nanoTime();

         for (int i = prefix; i < this.value.length() - suffix; i++) {
            this.ghosts.add(new TypedText.Ghost(String.valueOf(this.value.charAt(i)), measure.width(this.value.substring(0, i)), now));
         }

         List<TypedText.Glyph> next0 = new ArrayList<>(this.glyphs.subList(0, prefix));

         for (int i = prefix; i < next.length() - suffix; i++) {
            next0.add(new TypedText.Glyph(String.valueOf(next.charAt(i)), now));
         }

         next0.addAll(this.glyphs.subList(this.value.length() - suffix, this.value.length()));
         this.glyphs.clear();
         this.glyphs.addAll(next0);
         this.value = next;
         this.typedAt = now;
      }
   }

   private static int fade(int color, float amount) {
      return MenuKit.alpha(color, Math.max(0.0F, Math.min(1.0F, amount)));
   }

   private static float out(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
   }

   void draw(
      class_332 context,
      float x,
      float y,
      float maxWidth,
      float lineHeight,
      boolean focused,
      String hint,
      int textColor,
      int hintColor,
      int caretColor,
      float opacity,
      TypedText.Measure measure,
      TypedText.Draw draw
   ) {
      long now = System.nanoTime();
      float delta = Math.min(0.06F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      float total = measure.width(this.value);
      float shift = Math.max(0.0F, total - maxWidth);
      this.placeholder = MenuKit.approach(this.placeholder, this.value.isEmpty() ? 1.0F : 0.0F, 18.0F, delta);
      if (this.placeholder > 0.01F && hint != null && !hint.isEmpty()) {
         draw.text(context, hint, x + (1.0F - this.placeholder) * 6.0F, y, fade(hintColor, this.placeholder));
      }

      for (int i = 0; i < this.glyphs.size(); i++) {
         TypedText.Glyph glyph = this.glyphs.get(i);
         float gx = measure.width(this.value.substring(0, i)) - shift;
         if (!(gx < -0.5F)) {
            float shown = out((float)(now - glyph.born()) / 1.0E9F / 0.16F);
            draw.text(context, glyph.value(), x + gx, y + (1.0F - shown) * 3.0F, fade(textColor, shown));
         }
      }

      for (int ix = this.ghosts.size() - 1; ix >= 0; ix--) {
         TypedText.Ghost ghost = this.ghosts.get(ix);
         float t = (float)(now - ghost.died()) / 1.0E9F / 0.14F;
         if (t >= 1.0F) {
            this.ghosts.remove(ix);
         } else {
            float gx = ghost.x() - shift;
            if (!(gx < -0.5F)) {
               draw.text(context, ghost.value(), x + gx, y + t * 2.0F, fade(textColor, 1.0F - out(t)));
            }
         }
      }

      float target = x + total - shift;
      this.caretX = Float.isNaN(this.caretX) ? target : this.caretX + (target - this.caretX) * (1.0F - (float)Math.exp((double)(-delta * 30.0F)));
      if (focused) {
         float since = (float)(now - this.typedAt) / 1.0E9F;
         float blink = since < 0.5F ? 1.0F : 0.5F + 0.5F * (float)Math.cos((double)(since - 0.5F) * 3.141592653589793 / 0.550000011920929);
         int cx = Math.round(this.caretX + 0.5F);
         context.method_25294(cx, Math.round(y - 1.0F), cx + 1, Math.round(y + lineHeight), MenuKit.alpha(fade(caretColor, blink), opacity));
      }
   }

   static final class Arrivals {
      private final Map<Object, Long> since = new HashMap<>();
      private Set<Object> seen = new HashSet<>();
      private int fresh;

      void begin() {
         this.seen = new HashSet<>();
         this.fresh = 0;
      }

      float alpha(Object key) {
         this.seen.add(key);
         long now = System.nanoTime();
         Long at = this.since.get(key);
         if (at == null) {
            at = now + (long)(this.fresh++) * 25000000L;
            this.since.put(key, at);
         }

         return MenuKit.quint(Math.max(0.0F, (float)(now - at) / 1.0E9F / 0.28F));
      }

      void end() {
         this.since.keySet().retainAll(this.seen);
      }

      void reset() {
         this.since.clear();
      }
   }

   interface Draw {
      void text(class_332 var1, String var2, float var3, float var4, int var5);
   }

   private static record Ghost(String value, float x, long died) {
   }

   private static record Glyph(String value, long born) {
   }

   interface Measure {
      float width(String var1);
   }
}
