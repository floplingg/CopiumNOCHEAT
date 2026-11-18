package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.DonutUtils;

public class HomeReset extends Module {
   private final Setting<String> home = this.add(new StringSetting("Home", "Name or number of the home that gets reset.", "1"));
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks between /delhome and /sethome.", 10, 0, 60));
   private int timer;

   public HomeReset() {
      super("Home Reset", "Deletes a home and sets it again where you stand, with one key press.", Category.DONUT);
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 != null && !this.name().isEmpty()) {
         DonutUtils.command("delhome " + this.name());
         this.timer = this.delay.get();
      } else {
         if (this.name().isEmpty()) {
            this.warning("Set a home name first.", new Object[0]);
         }

         this.setEnabled(false);
      }
   }

   @Override
   public void onTick() {
      if (mc.field_1724 == null) {
         this.setEnabled(false);
      } else if (this.timer-- <= 0) {
         DonutUtils.command("sethome " + this.name());
         this.info("Home %s was reset here.", new Object[]{this.name()});
         this.setEnabled(false);
      }
   }

   private String name() {
      String text = this.home.get().trim();
      return text.startsWith("/") ? text.substring(1).trim() : text;
   }
}
