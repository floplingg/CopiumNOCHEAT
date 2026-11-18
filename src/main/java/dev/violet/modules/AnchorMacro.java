package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_4969;
import net.minecraft.class_239.class_240;

public class AnchorMacro extends Module {
   private final Setting<Boolean> useKey = this.add(new BoolSetting("Use Key", "Only works while you hold right click on the anchor.", true));
   private final Setting<Integer> explodeSlot = this.add(
      new IntSetting("Explode Slot", "Hotbar slot used to blow up the anchor. Auto takes an empty slot, a totem or another safe item.", 0, 0, 9) {
         @Override
         public String display() {
            return this.value == 0 ? "Auto" : String.valueOf(this.value);
         }
      }
   );
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to the slot you held before.", true));
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks between charging and blowing up.", 2, 0, 10));
   private int previous = -1;
   private int timer;

   public AnchorMacro() {
      super("Anchor Macro", "Charges the respawn anchor you look at with glowstone and blows it up right after.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.restore();
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.useKey.get() && !mc.field_1690.field_1904.method_1434()) {
            this.restore();
         } else if (this.timer > 0) {
            this.timer--;
         } else {
            if (mc.field_1765 instanceof class_3965 hit && hit.method_17783() == class_240.field_1332) {
               class_2680 state = mc.field_1687.method_8320(hit.method_17777());
               if (!state.method_27852(class_2246.field_23152)) {
                  this.restore();
                  return;
               }

               class_1661 inventory = mc.field_1724.method_31548();
               if ((Integer)state.method_11654(class_4969.field_23153) == 0) {
                  for (int i = 0; i < 9; i++) {
                     if (inventory.method_5438(i).method_31574(class_1802.field_8801)) {
                        this.use(i, hit);
                        return;
                     }
                  }

                  return;
               }

               if (mc.field_1687.method_27983() == class_1937.field_25180) {
                  return;
               }

               int slot = this.explodeSlot(inventory);
               if (slot >= 0) {
                  this.use(slot, hit);
               }

               return;
            }

            this.restore();
         }
      }
   }

   private void use(int slot, class_3965 hit) {
      class_1661 inventory = mc.field_1724.method_31548();
      if (this.previous < 0) {
         this.previous = inventory.method_67532();
      }

      inventory.method_61496(slot);
      mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
      mc.field_1724.method_6104(class_1268.field_5808);
      this.timer = this.delay.get();
   }

   private int explodeSlot(class_1661 inventory) {
      int chosen = this.explodeSlot.get() - 1;
      if (chosen >= 0) {
         return inventory.method_5438(chosen).method_31574(class_1802.field_8801) ? -1 : chosen;
      } else {
         for (int i = 0; i < 9; i++) {
            if (inventory.method_5438(i).method_7960()) {
               return i;
            }
         }

         for (int ix = 0; ix < 9; ix++) {
            if (inventory.method_5438(ix).method_31574(class_1802.field_8288)) {
               return ix;
            }
         }

         for (int ixx = 0; ixx < 9; ixx++) {
            class_1799 stack = inventory.method_5438(ixx);
            if (!stack.method_31574(class_1802.field_8801) && !(stack.method_7909() instanceof class_1747)) {
               return ixx;
            }
         }

         return -1;
      }
   }

   private void restore() {
      if (this.previous >= 0 && this.switchBack.get() && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.previous);
      }

      this.previous = -1;
   }
}
