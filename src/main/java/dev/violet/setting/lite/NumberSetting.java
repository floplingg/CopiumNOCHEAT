package dev.violet.setting.lite;

import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.Locale;

public final class NumberSetting extends LiteSetting {
   private final float initial;
   private final float min;
   private final float max;
   private float step;
   private NumberSetting.Grid setting;

   public NumberSetting(String name, float value, float min, float max) {
      super(name);
      this.initial = value;
      this.min = min;
      this.max = max;
   }

   public NumberSetting step(float step) {
      this.step = step;
      return this;
   }

   public float step() {
      return this.step;
   }

   private float unit() {
      if (this.step > 0.0F) {
         return this.step;
      } else {
         return switch (this.decimals()) {
            case 0 -> 1.0F;
            case 1 -> 0.1F;
            default -> 0.01F;
         };
      }
   }

   public int decimals() {
      if (this.step > 0.0F) {
         return Math.max(decimalsOf(this.step), decimalsOf(this.min));
      } else {
         float range = this.max - this.min;
         if ((double)this.min == Math.rint((double)this.min) && (double)this.max == Math.rint((double)this.max) && range >= 20.0F) {
            return 0;
         } else {
            return range <= 2.0F ? 2 : 1;
         }
      }
   }

   private static int decimalsOf(float value) {
      String text = Float.toString(Math.abs(value));
      if (text.contains("E")) {
         return 4;
      } else {
         int dot = text.indexOf(46);
         if (dot < 0) {
            return 0;
         } else {
            String fraction = text.substring(dot + 1).replaceAll("0+$", "");
            return Math.min(4, fraction.length());
         }
      }
   }

   @Override
   protected Setting<?> create() {
      float unit = this.unit();
      int steps = Math.max(1, Math.round((this.max - this.min) / unit));
      this.setting = new NumberSetting.Grid(this.name(), Math.round((this.initial - this.min) / unit), steps);
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public float get() {
      return this.setting == null ? this.initial : Math.max(this.min, Math.min(this.max, this.min + (float)this.setting.get().intValue() * this.unit()));
   }

   public int getInt() {
      return Math.round(this.get());
   }

   public void set(float value) {
      if (this.setting != null) {
         this.setting.set(Integer.valueOf(Math.round((value - this.min) / this.unit())));
      }
   }

   public float[] value() {
      return new float[]{this.get()};
   }

   public float min() {
      return this.min;
   }

   public float max() {
      return this.max;
   }

   public String formatted() {
      int decimals = this.decimals();
      return decimals == 0 ? Integer.toString(Math.round(this.get())) : String.format(Locale.ROOT, "%." + decimals + "f", this.get());
   }

   private final class Grid extends IntSetting {
      Grid(String name, int value, int steps) {
         super(name, "", Math.max(0, Math.min(steps, value)), 0, steps);
      }

      @Override
      public String display() {
         return NumberSetting.this.formatted();
      }
   }
}
