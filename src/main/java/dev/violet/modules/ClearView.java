package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.Setting;

public class ClearView extends Module {
   public final Setting<Boolean> rain = this.add(new BoolSetting("No Rain", "Removes rain and snow, their sounds, splashes and the gray rain fog.", true));
   public final Setting<Boolean> water = this.add(
      new BoolSetting("Clear Water", "Water becomes clear like glass: no blue fog and no overlay, you see as far as on land.", true)
   );
   public final Setting<Boolean> lava = this.add(new BoolSetting("Clear Lava", "Removes the thick orange fog when you are in lava.", false));

   public ClearView() {
      super("Clear View", "Removes rain and makes water clear when you are in it.", Category.VISUALS);
   }

   private static ClearView instance() {
      ClearView module = SoberClient.MODULES.get(ClearView.class);
      return module != null && module.isActive() ? module : null;
   }

   public static boolean noRain() {
      ClearView module = instance();
      return module != null && module.rain.get();
   }

   public static boolean clearWater() {
      ClearView module = instance();
      return module != null && module.water.get();
   }

   public static boolean clearLava() {
      ClearView module = instance();
      return module != null && module.lava.get();
   }
}
