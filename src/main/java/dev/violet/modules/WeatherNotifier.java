package dev.violet.modules;

import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.Setting;

public class WeatherNotifier extends Module {
   private final Setting<Boolean> rain = this.add(new BoolSetting("Rain", "Notifies when it starts raining.", true));
   private final Setting<Boolean> thunder = this.add(new BoolSetting("Thunder", "Notifies when a thunderstorm starts.", true));
   private final Setting<Boolean> clear = this.add(new BoolSetting("Clear", "Notifies when the weather clears up.", true));
   private int state = -1;
   private int settle;

   public WeatherNotifier() {
      super("Weather Notifier", "Notifies you when the weather changes.", Category.VISUALS);
   }

   @Override
   public void onEnable() {
      this.state = -1;
   }

   @Override
   public void onGameLeft() {
      this.state = -1;
   }

   @Override
   public void onTick() {
      if (mc.field_1687 != null) {
         int now = mc.field_1687.method_8546() ? 2 : (mc.field_1687.method_8419() ? 1 : 0);
         if (this.state < 0) {
            this.state = now;
            this.settle = 40;
         } else if (this.settle > 0) {
            this.settle--;
            this.state = now;
         } else if (now != this.state) {
            int previous = this.state;
            this.state = now;
            if (now == 2 && this.thunder.get()) {
               Notifications.push("Weather", "A thunderstorm started.", Notifications.Type.WARNING);
            } else if (now == 1 && previous == 0 && this.rain.get()) {
               Notifications.push("Weather", "It started raining.", Notifications.Type.INFO);
            } else if (now == 0 && this.clear.get()) {
               Notifications.push("Weather", "The weather cleared up.", Notifications.Type.SUCCESS);
            }
         }
      }
   }

   @Override
   public String getInfoString() {
      return this.state == 2 ? "Thunder" : (this.state == 1 ? "Rain" : (this.state == 0 ? "Clear" : null));
   }
}
