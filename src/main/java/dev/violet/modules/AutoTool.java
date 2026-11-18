package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2378;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_7924;
import net.minecraft.class_239.class_240;

public class AutoTool extends Module {
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to your old slot when you stop mining.", true));
   private final Setting<Boolean> antiBreak = this.add(new BoolSetting("Anti Break", "Never uses tools that are about to break.", true));
   private final Setting<Integer> minDurability = this.add(new IntSetting("Min Durability", "Durability a tool needs to still be used.", 10, 1, 100))
      .visible(this.antiBreak::get);
   private final Setting<Boolean> preferSilk = this.add(new BoolSetting("Prefer Silk Touch", "Takes silk touch tools when they mine just as fast.", false));
   private int previousSlot = -1;

   public AutoTool() {
      super("Auto Tool", "Switches to the best tool in your hotbar while mining.", Category.DONUT);
   }

   @Override
   public void onDisable() {
      this.restore();
   }

   @Override
   public void onGameLeft() {
      this.previousSlot = -1;
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         boolean mining = mc.field_1690.field_1886.method_1434()
            && mc.field_1755 == null
            && mc.field_1765 != null
            && mc.field_1765.method_17783() == class_240.field_1332;
         if (mining && !mc.field_1724.method_68878()) {
            class_2680 state = mc.field_1687.method_8320(((class_3965)mc.field_1765).method_17777());
            if (!state.method_26215()) {
               int current = mc.field_1724.method_31548().method_67532();
               int best = current;
               double bestScore = this.score(mc.field_1724.method_31548().method_5438(current), state);

               for (int i = 0; i < 9; i++) {
                  double score = this.score(mc.field_1724.method_31548().method_5438(i), state);
                  if (score > bestScore + 0.001) {
                     bestScore = score;
                     best = i;
                  }
               }

               if (best != current) {
                  if (this.previousSlot == -1) {
                     this.previousSlot = current;
                  }

                  mc.field_1724.method_31548().method_61496(best);
               }
            }
         } else {
            this.restore();
         }
      }
   }

   private double score(class_1799 stack, class_2680 state) {
      if (this.antiBreak.get() && stack.method_7963() && stack.method_7936() - stack.method_7919() <= this.minDurability.get()) {
         return -1.0;
      } else {
         double score = (double)stack.method_7924(state);
         if (score <= 1.0) {
            return stack.method_7963() ? 0.5 : 1.0;
         } else {
            if (stack.method_7951(state)) {
               score += 1000.0;
            }

            class_2378<class_1887> registry = mc.field_1687.method_30349().method_30530(class_7924.field_41265);
            int efficiency = class_1890.method_8225(registry.method_46747(class_1893.field_9131), stack);
            if (efficiency > 0) {
               score += (double)(efficiency * efficiency + 1);
            }

            if (this.preferSilk.get() && class_1890.method_8225(registry.method_46747(class_1893.field_9099), stack) > 0) {
               score += 0.5;
            }

            return score;
         }
      }
   }

   private void restore() {
      if (this.previousSlot != -1 && this.switchBack.get() && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.previousSlot);
      }

      this.previousSlot = -1;
   }
}
