package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1802;

public class AutoTotem extends Module {
   private final Setting<Integer> health = this.add(
      new IntSetting("Health", "Only puts a totem in your offhand at or below this many hearts. 0 always keeps one there.", 0, 0, 20) {
         @Override
         public String display() {
            return this.value == 0 ? "Always" : this.value + " hearts";
         }
      }
   );
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks to wait between two swaps.", 0, 0, 20));
   private final Setting<Boolean> hotbar = this.add(new BoolSetting("Use Hotbar", "Also takes totems from your hotbar when your inventory has none.", true));
   private int timer;

   public AutoTotem() {
      super("Auto Totem", "Keeps a totem of undying in your offhand.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1761 != null && !mc.field_1724.method_68878() && !mc.field_1724.method_7325()) {
         if (mc.field_1724.field_7512 == mc.field_1724.field_7498) {
            if (!mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
               if (this.health.get() <= 0 || !(mc.field_1724.method_6032() + mc.field_1724.method_6067() > (float)this.health.get().intValue() * 2.0F)) {
                  if (this.timer > 0) {
                     this.timer--;
                  } else {
                     int slot = totemSlot(this.hotbar.get());
                     if (slot >= 0) {
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slot, 40, class_1713.field_7791, mc.field_1724);
                        this.timer = this.delay.get();
                     }
                  }
               }
            }
         }
      }
   }

   private static int totemSlot(boolean hotbar) {
      class_1661 inventory = mc.field_1724.method_31548();

      for (int i = 9; i < 36; i++) {
         if (inventory.method_5438(i).method_31574(class_1802.field_8288)) {
            return i;
         }
      }

      if (hotbar) {
         for (int ix = 0; ix < 9; ix++) {
            if (inventory.method_5438(ix).method_31574(class_1802.field_8288)) {
               return 36 + ix;
            }
         }
      }

      return -1;
   }

   @Override
   public String getInfoString() {
      if (mc.field_1724 == null) {
         return null;
      } else {
         int count = 0;
         class_1661 inventory = mc.field_1724.method_31548();

         for (int i = 0; i < 36; i++) {
            if (inventory.method_5438(i).method_31574(class_1802.field_8288)) {
               count += inventory.method_5438(i).method_7947();
            }
         }

         if (mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
            count++;
         }

         return String.valueOf(count);
      }
   }
}
