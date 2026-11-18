package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.violet.render.Color;

public class ColorSetting extends Setting<Color> {
   private float hue = -1.0F;
   private float saturation;

   public ColorSetting(String name, String description, Color defaultValue) {
      super(name, description, defaultValue);
   }

   public void setChannel(int channel, int v) {
      Color c = this.value;
      int clamped = Math.max(0, Math.min(255, v));

      this.set(switch (channel) {
         case 0 -> new Color(clamped, c.g(), c.b(), c.a());
         case 1 -> new Color(c.r(), clamped, c.b(), c.a());
         case 2 -> new Color(c.r(), c.g(), clamped, c.a());
         default -> new Color(c.r(), c.g(), c.b(), clamped);
      });
   }

   public float[] hsv() {
      float[] hsv = this.value.toHsv();
      if (this.hue < 0.0F) {
         this.hue = hsv[0];
         this.saturation = hsv[1];
      }

      if (hsv[2] > 0.001F && hsv[1] > 0.001F) {
         this.hue = hsv[0];
         this.saturation = hsv[1];
      } else if (hsv[2] > 0.001F) {
         this.saturation = hsv[1];
      }

      return new float[]{this.hue, this.saturation, hsv[2]};
   }

   public void setHsv(float h, float s, float v) {
      this.hue = h;
      this.saturation = s;
      this.set(Color.fromHsv(h, s, v, this.value.a()));
   }

   public void setAlpha(int alpha) {
      this.set(this.value.withAlpha(alpha));
   }

   public boolean setHex(String text) {
      String hex = text.trim();
      if (hex.startsWith("#")) {
         hex = hex.substring(1);
      } else if (hex.regionMatches(true, 0, "0x", 0, 2)) {
         hex = hex.substring(2);
      }

      if (!hex.matches("[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8}")) {
         return false;
      } else {
         if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
         }

         int rgb = Integer.parseInt(hex.substring(0, 6), 16);
         int alpha = hex.length() == 8 ? Integer.parseInt(hex.substring(6), 16) : this.value.a();
         this.hue = -1.0F;
         this.set(new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha));
         return true;
      }
   }

   public int channel(int channel) {
      return switch (channel) {
         case 0 -> this.value.r();
         case 1 -> this.value.g();
         case 2 -> this.value.b();
         default -> this.value.a();
      };
   }

   @Override
   public String display() {
      return String.format("#%02X%02X%02X", this.value.r(), this.value.g(), this.value.b());
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value.packed());
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.value = Color.fromPacked(element.getAsInt());
      }
   }
}
