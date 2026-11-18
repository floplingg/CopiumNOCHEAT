package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.concurrent.ThreadLocalRandom;

public class AutoJumpReset extends Module {
   private final Setting<Integer> chance = this.add(new IntSetting("Chance", "How often it jumps when you get hit, in percent.", 100, 1, 100));
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks to wait after the hit before jumping.", 0, 0, 3));
   private boolean holding;

   public AutoJumpReset() {
      super("Auto Jump Reset", "Jumps right when you take a hit, so you take less knockback.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.release();
   }

   @Override
   public void onTick() {
      if (mc.field_1724 == null || mc.field_1755 != null) {
         this.release();
      } else if (this.holding) {
         this.release();
      } else if (mc.field_1724.field_6235 != 0 && mc.field_1724.field_6235 == mc.field_1724.field_6254 - 1 - this.delay.get()) {
         if (mc.field_1724.method_24828() && !mc.field_1724.method_5799() && !mc.field_1724.method_5771()) {
            if (ThreadLocalRandom.current().nextInt(100) < this.chance.get()) {
               mc.field_1690.field_1903.method_23481(true);
               this.holding = true;
            }
         }
      }
   }

   private void release() {
      if (this.holding) {
         this.holding = false;
         mc.field_1690.field_1903.method_23481(false);
      }
   }
}
