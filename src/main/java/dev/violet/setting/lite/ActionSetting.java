package dev.violet.setting.lite;

import dev.violet.setting.BoolSetting;
import dev.violet.setting.Setting;

public final class ActionSetting extends LiteSetting {
   private final Runnable action;
   private BoolSetting setting;

   public ActionSetting(String name, Runnable action) {
      super(name);
      this.action = action;
   }

   @Override
   protected Setting<?> create() {
      this.setting = new BoolSetting(this.name(), "", false);
      this.setting.onChanged(pressed -> {
         if (pressed) {
            this.setting.set(Boolean.valueOf(false));
            this.run();
         }
      });
      return this.setting;
   }

   public void run() {
      this.action.run();
      this.notifyChanged();
   }
}
