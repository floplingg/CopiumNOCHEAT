package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.NumberSetting;

public final class Aspect extends BusModule {
   private static volatile float ratio;
   private final NumberSetting setting = this.addSetting("ratio", new NumberSetting("Ratio", 1.6F, 0.5F, 8.0F).step(0.1F));

   public Aspect() {
      super(
         "aspect",
         "Aspect",
         "Changes the aspect ratio of the gameplay only. Lower ratios stretch the world wider, higher ratios squeeze it. Your hands, menus, the HUD and the ClickGUI stay normal.",
         ModuleCategory.VISUALS
      );
      this.setting.onChange(this::update);
   }

   @Override
   public void onEnable() {
      this.update();
   }

   @Override
   public void onDisable() {
      ratio = 0.0F;
   }

   private void update() {
      ratio = this.isEnabled() ? Math.max(0.1F, this.setting.get()) : 0.0F;
   }

   public static float aspect(float original) {
      float value = ratio;
      return value > 0.0F ? value : original;
   }
}
