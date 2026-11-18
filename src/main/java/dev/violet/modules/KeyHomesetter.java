package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.DonutUtils;

public class KeyHomesetter extends Module {
   private final Setting<Boolean> cycle = this.add(
      new BoolSetting("Cycle Homes", "Each press sets the next home number, from 1 up to Homes, then starts at 1 again.", false)
   );
   private final Setting<Integer> homes = this.add(new IntSetting("Homes", "How many home numbers Cycle Homes goes through.", 3, 1, 10))
      .visible(this.cycle::get);
   private final Setting<String> home = this.add(new StringSetting("Home", "Name or number of the home that gets set.", "1")).visible(() -> !this.cycle.get());
   private int next;

   public KeyHomesetter() {
      super("Key Homesetter", "Sets a home where you stand each time you press the key.", Category.DONUT);
   }

   @Override
   public void onEnable() {
      String name = this.name();
      if (mc.field_1724 != null && !name.isEmpty()) {
         DonutUtils.command("sethome " + name);
         this.info("Home %s set here.", new Object[]{name});
      } else if (name.isEmpty()) {
         this.warning("Set a home name first.", new Object[0]);
      }

      this.setEnabled(false);
   }

   private String name() {
      if (this.cycle.get()) {
         this.next = this.next % this.homes.get() + 1;
         return String.valueOf(this.next);
      } else {
         String text = this.home.get().trim();
         return text.startsWith("/") ? text.substring(1).trim() : text;
      }
   }
}
