package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4174;
import net.minecraft.class_9334;

public class AutoEat extends Module {
   private final Setting<Integer> hunger = this.add(
      new IntSetting("Hunger", "Starts eating when your hunger bar is at or below this many points (20 is full).", 14, 1, 19)
   );
   private final Setting<Integer> health = this.add(
      new IntSetting("Health", "Also eats at or below this many hearts, even when you are not hungry. 0 turns it off.", 0, 0, 20) {
         @Override
         public String display() {
            return this.value == 0 ? "Off" : this.value + " hearts";
         }
      }
   );
   private final Setting<Boolean> gapples = this.add(new BoolSetting("Golden Apples", "Golden apples count as food too.", false));
   private final Setting<Boolean> pauseCombat = this.add(new BoolSetting("Pause In Combat", "Waits while you hold the attack key.", true));
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to the slot you held before eating.", true));
   private boolean eating;
   private int previous = -1;

   public AutoEat() {
      super("Auto Eat", "Picks the best food in your hotbar and eats when your hunger or health gets low.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.stop();
   }

   @Override
   public void onGameLeft() {
      this.eating = false;
      this.previous = -1;
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1761 != null && !mc.field_1724.method_68878() && !mc.field_1724.method_7325()) {
         if (mc.field_1755 == null && (!this.pauseCombat.get() || !mc.field_1690.field_1886.method_1434())) {
            if (this.eating) {
               if (this.isFood(mc.field_1724.method_6047()) && this.needsFood()) {
                  mc.field_1690.field_1904.method_23481(true);
               } else {
                  this.stop();
               }
            } else if (this.needsFood() && !mc.field_1724.method_6115()) {
               int slot = this.bestFood();
               if (slot >= 0) {
                  this.previous = mc.field_1724.method_31548().method_67532();
                  mc.field_1724.method_31548().method_61496(slot);
                  mc.field_1690.field_1904.method_23481(true);
                  this.eating = true;
               }
            }
         } else {
            this.stop();
         }
      }
   }

   private boolean needsFood() {
      boolean hungry = mc.field_1724.method_7344().method_7586() <= this.hunger.get();
      boolean hurt = this.health.get() > 0
         && mc.field_1724.method_6032() <= (float)this.health.get().intValue() * 2.0F
         && mc.field_1724.method_7344().method_7586() < 20;
      return hungry || hurt;
   }

   private void stop() {
      if (this.eating) {
         this.eating = false;
         mc.field_1690.field_1904.method_23481(false);
         if (this.switchBack.get() && this.previous >= 0 && mc.field_1724 != null) {
            mc.field_1724.method_31548().method_61496(this.previous);
         }

         this.previous = -1;
      }
   }

   private int bestFood() {
      int best = -1;
      int bestValue = 0;

      for (int i = 0; i < 9; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (this.isFood(stack)) {
            int value = ((class_4174)stack.method_58694(class_9334.field_50075)).comp_2491();
            if (value > bestValue) {
               bestValue = value;
               best = i;
            }
         }
      }

      return best;
   }

   private boolean isFood(class_1799 stack) {
      class_4174 food = (class_4174)stack.method_58694(class_9334.field_50075);
      if (food == null) {
         return false;
      } else if (stack.method_31574(class_1802.field_8511)
         || stack.method_31574(class_1802.field_8680)
         || stack.method_31574(class_1802.field_8635)
         || stack.method_31574(class_1802.field_8323)
         || stack.method_31574(class_1802.field_8233)
         || stack.method_31574(class_1802.field_8766)) {
         return false;
      } else {
         return !stack.method_31574(class_1802.field_8463) && !stack.method_31574(class_1802.field_8367) ? true : this.gapples.get();
      }
   }

   @Override
   public String getInfoString() {
      return this.eating ? "Eating" : null;
   }
}
