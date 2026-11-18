package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.module.ModuleRegistry;

public final class Hotkeys extends BusModule {
   public Hotkeys() {
      super("hotkeys", "Hotkeys", "Lists the modules you have bound to a key, and their keys.", ModuleCategory.CLIENT);
   }

   public static Hotkeys get() {
      return ModuleRegistry.get(Hotkeys.class);
   }

   public static boolean shouldShowHud() {
      Hotkeys module = get();
      return module != null && module.isEnabled();
   }
}
