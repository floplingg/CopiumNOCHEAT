package dev.violet.setting.lite;

import dev.violet.setting.BoolSetting;
import dev.violet.setting.Setting;

public final class BooleanSetting extends LiteSetting {
   private final boolean initial;
   private BoolSetting setting;

   public BooleanSetting(String name, boolean value) {
      super(name);
      this.initial = value;
   }

   @Override
   protected Setting<?> create() {
      this.setting = new BoolSetting(this.name(), "", this.initial);
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public boolean get() {
      return this.setting == null ? this.initial : this.setting.get();
   }

   public void set(boolean value) {
      if (this.setting != null) {
         this.setting.set(Boolean.valueOf(value));
      }
   }

   public BooleanSetting.Value value() {
      return new BooleanSetting.Value(this);
   }

   public static record Value(BooleanSetting setting) {
      public boolean get() {
         return this.setting.get();
      }
   }
}
