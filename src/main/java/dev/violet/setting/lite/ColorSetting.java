package dev.violet.setting.lite;

import dev.violet.render.Color;
import dev.violet.setting.Setting;

public final class ColorSetting extends LiteSetting {
   private final int initial;
   private dev.violet.setting.ColorSetting setting;

   public ColorSetting(String name, int argb) {
      super(name);
      this.initial = argb;
   }

   @Override
   protected Setting<?> create() {
      this.setting = new dev.violet.setting.ColorSetting(this.name(), "", Color.fromPacked(this.initial));
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public int argb() {
      return this.setting == null ? this.initial : this.setting.get().packed();
   }

   public int rgb() {
      return this.argb() & 16777215;
   }

   public int argbWithAlpha(int alpha) {
      return (alpha & 0xFF) << 24 | this.rgb();
   }

   public void setArgb(int argb) {
      if (this.setting != null) {
         this.setting.set(Color.fromPacked(argb));
      }
   }

   public Color color() {
      return Color.fromPacked(this.argb());
   }
}
