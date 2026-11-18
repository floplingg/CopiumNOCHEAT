package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_3489;
import net.minecraft.class_3966;

public class ShieldBreaker extends Module {
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks between two shield breaks.", 4, 0, 20));
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to your weapon right after the hit.", true));
   private final Setting<Boolean> clicking = this.add(new BoolSetting("Only While Clicking", "Only breaks shields while you hold the attack key.", false));
   private int timer;
   private int previous = -1;

   public ShieldBreaker() {
      super("Shield Breaker", "Hits a player who blocks with a shield using an axe from your hotbar, which disables the shield.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.restore();
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null && !mc.field_1724.method_7325()) {
         if (this.previous >= 0) {
            this.restore();
         } else if (this.timer > 0) {
            this.timer--;
         } else if (!this.clicking.get() || mc.field_1690.field_1886.method_1434()) {
            if (!(mc.field_1765 instanceof class_3966 hit) || !(hit.method_17782() instanceof class_1657 target)) {
               return;
            }

            if (target.method_6039() && target.method_5805()) {
               int axe = CombatItems.hotbarSlot(stack -> stack.method_31573(class_3489.field_42612));
               if (axe >= 0) {
                  int held = mc.field_1724.method_31548().method_67532();
                  mc.field_1724.method_31548().method_61496(axe);
                  mc.field_1761.method_2918(mc.field_1724, target);
                  mc.field_1724.method_6104(class_1268.field_5808);
                  if (held != axe) {
                     this.previous = held;
                  }

                  this.timer = this.delay.get();
               }
            }
         }
      }
   }

   private void restore() {
      if (this.previous >= 0 && this.switchBack.get() && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.previous);
      }

      this.previous = -1;
   }
}
