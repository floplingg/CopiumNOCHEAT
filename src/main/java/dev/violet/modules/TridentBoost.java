package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.NumberSetting;

public final class TridentBoost extends BusModule {
   public static volatile TridentBoost active;
   private final NumberSetting boost = this.addSetting("boost", new NumberSetting("Boost", 2.0F, 0.1F, 10.0F).step(0.1F));
   private final BooleanSetting outOfWater = this.addSetting("out-of-water", new BooleanSetting("Out Of Water", true));

   public TridentBoost() {
      super("trident-boost", "Trident Boost", "Boosts you when using riptide with a trident.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      active = this;
   }

   @Override
   public void onDisable() {
      active = null;
   }

   public static double riptideMultiplier() {
      TridentBoost module = active;
      return module != null ? (double)module.boost.get() : 1.0;
   }

   public static boolean allowOutOfWater() {
      TridentBoost module = active;
      return module != null && module.outOfWater.get();
   }
}
