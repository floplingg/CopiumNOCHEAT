package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_490;

public class AutoInvTotem extends Module {
   private final Setting<Boolean> offhand = this.add(new BoolSetting("Offhand", "Puts a totem in your offhand when you open your inventory.", true));
   private final Setting<Integer> hotbarSlot = this.add(
      new IntSetting("Hotbar Slot", "Hotbar slot that gets refilled with a totem when you open your inventory.", 9, 0, 9) {
         @Override
         public String display() {
            return this.value == 0 ? "Off" : String.valueOf(this.value);
         }
      }
   );
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks to wait between two moves.", 1, 0, 10));
   private final Setting<Boolean> closeAfter = this.add(new BoolSetting("Close After", "Closes the inventory again once everything is filled.", false));
   private int timer;
   private boolean closed;

   public AutoInvTotem() {
      super("Auto Inv Totem", "Refills your offhand and a hotbar slot with totems as soon as you open your inventory.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (!(mc.field_1755 instanceof class_490)) {
            this.closed = false;
         } else if (this.timer > 0) {
            this.timer--;
         } else {
            class_1661 inventory = mc.field_1724.method_31548();
            int sync = mc.field_1724.field_7498.field_7763;
            int target = this.hotbarSlot.get() - 1;
            if (this.offhand.get() && !mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
               int slot = source(inventory, target);
               if (slot >= 0) {
                  mc.field_1761.method_2906(sync, slot, 40, class_1713.field_7791, mc.field_1724);
                  this.timer = this.delay.get();
                  return;
               }
            }

            if (target >= 0 && !inventory.method_5438(target).method_31574(class_1802.field_8288)) {
               for (int i = 9; i < 36; i++) {
                  if (inventory.method_5438(i).method_31574(class_1802.field_8288)) {
                     mc.field_1761.method_2906(sync, i, target, class_1713.field_7791, mc.field_1724);
                     this.timer = this.delay.get();
                     return;
                  }
               }
            }

            if (this.closeAfter.get() && !this.closed) {
               this.closed = true;
               mc.field_1724.method_7346();
            }
         }
      }
   }

   private static int source(class_1661 inventory, int keep) {
      for (int i = 9; i < 36; i++) {
         if (inventory.method_5438(i).method_31574(class_1802.field_8288)) {
            return i;
         }
      }

      for (int ix = 0; ix < 9; ix++) {
         if (ix != keep && inventory.method_5438(ix).method_31574(class_1802.field_8288)) {
            return 36 + ix;
         }
      }

      return -1;
   }
}
