package dev.violet.setting.lite;

import dev.violet.setting.Setting;

public final class KeySetting extends LiteSetting {
   private final int initial;
   private dev.violet.setting.KeySetting setting;

   public KeySetting(String name, int defaultKey) {
      super(name);
      this.initial = defaultKey;
   }

   @Override
   protected Setting<?> create() {
      this.setting = new dev.violet.setting.KeySetting(this.name(), "", () -> null);
      this.setting.bind(this.initial);
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public int value() {
      if (this.setting == null) {
         return this.initial;
      } else {
         Integer key = this.setting.get();
         return key == null ? -1 : key;
      }
   }

   public void setValue(int key) {
      if (this.setting != null) {
         this.setting.bind(key);
      }
   }
}
