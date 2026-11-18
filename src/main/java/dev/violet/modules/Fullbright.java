package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;

public class Fullbright extends Module {
   public final Setting<Fullbright.Mode> mode = this.add(
      new EnumSetting<>(
         "Mode", "Gamma turns the brightness far above the normal maximum. Night Vision looks like the night vision potion.", Fullbright.Mode.Gamma
      )
   );
   public final Setting<Integer> brightness = this.add(new IntSetting("Brightness", "How bright the game gets, in percent.", 100, 10, 100))
      .visible(() -> this.mode.get() == Fullbright.Mode.Gamma);

   public Fullbright() {
      super("Fullbright", "Makes the whole game bright, even in caves and at night.", Category.VISUALS);
   }

   private static Fullbright instance() {
      Fullbright module = SoberClient.MODULES.get(Fullbright.class);
      return module != null && module.isActive() ? module : null;
   }

   public static float gamma(float value) {
      Fullbright module = instance();
      return module != null && module.mode.get() == Fullbright.Mode.Gamma
         ? Math.max(value, 1.0F + 15.0F * (float)module.brightness.get().intValue() / 100.0F)
         : value;
   }

   public static float nightVision(float value) {
      Fullbright module = instance();
      return module != null && module.mode.get() == Fullbright.Mode.NightVision ? 1.0F : value;
   }

   public static enum Mode {
      Gamma,
      NightVision;
   }
}
