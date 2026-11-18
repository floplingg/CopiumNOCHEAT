package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.NumberSetting;
import java.util.concurrent.ThreadLocalRandom;

public final class NoJumpDelay extends BusModule {
   public static volatile NoJumpDelay active;
   private final NumberSetting cooldown = this.addSetting("cooldown", new NumberSetting("Cooldown", 0.0F, 0.0F, 9.0F).step(1.0F));
   private final NumberSetting variance = this.addSetting("variance", new NumberSetting("Variance", 0.0F, 0.0F, 4.0F).step(1.0F));

   public NoJumpDelay() {
      super("no-jump-delay", "No Jump Delay", "Removes the delay between jumps.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      active = this;
   }

   @Override
   public void onDisable() {
      active = null;
   }

   public int rearm(int vanillaTicks) {
      int ticks = this.cooldown.getInt();
      int spread = this.variance.getInt();
      if (spread > 0) {
         ticks += ThreadLocalRandom.current().nextInt(spread + 1);
      }

      return Math.min(vanillaTicks, ticks);
   }
}
