package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class KeyPearl extends Module {
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to the slot you held before.", true));
   private final Setting<Integer> delay = this.add(new IntSetting("Switch Delay", "Ticks to wait before switching back.", 1, 0, 10))
      .visible(this.switchBack::get);
   private int previous = -1;
   private int timer;

   public KeyPearl() {
      super("Key Pearl", "Throws an ender pearl from your hotbar with one key press.", Category.COMBAT);
   }

   @Override
   public void onEnable() {
      this.previous = -1;
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (mc.field_1724.method_6079().method_31574(class_1802.field_8634)) {
            if (!this.cooling(mc.field_1724.method_6079())) {
               this.throwPearl(class_1268.field_5810);
            }

            this.setEnabled(false);
         } else {
            class_1661 inventory = mc.field_1724.method_31548();
            int slot = -1;

            for (int i = 0; i < 9; i++) {
               if (inventory.method_5438(i).method_31574(class_1802.field_8634)) {
                  slot = i;
                  break;
               }
            }

            if (slot < 0) {
               this.warning("No ender pearl in your hotbar.", new Object[0]);
               this.setEnabled(false);
            } else if (this.cooling(inventory.method_5438(slot))) {
               this.setEnabled(false);
            } else {
               if (inventory.method_67532() != slot) {
                  this.previous = inventory.method_67532();
                  inventory.method_61496(slot);
               }

               this.throwPearl(class_1268.field_5808);
               this.timer = this.delay.get();
               if (!this.switchBack.get() || this.previous < 0 || this.timer == 0) {
                  this.setEnabled(false);
               }
            }
         }
      } else {
         this.setEnabled(false);
      }
   }

   @Override
   public void onDisable() {
      if (this.previous >= 0 && this.switchBack.get() && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.previous);
      }

      this.previous = -1;
   }

   @Override
   public void onTick() {
      if (this.timer-- <= 0) {
         this.setEnabled(false);
      }
   }

   private boolean cooling(class_1799 stack) {
      return mc.field_1724.method_7357().method_7904(stack);
   }

   private void throwPearl(class_1268 hand) {
      mc.field_1761.method_2919(mc.field_1724, hand);
      mc.field_1724.method_6104(hand);
   }
}
