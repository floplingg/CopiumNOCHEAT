package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;

public final class Sprint extends BusModule {
   private static volatile Sprint active;

   public Sprint() {
      super("sprint", "Sprint", "Keeps you sprinting while you move forward, like vanilla Toggle Sprint.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      active = this;
   }

   @Override
   public void onDisable() {
      active = null;
   }

   public static boolean holding() {
      return active != null;
   }
}
