package dev.violet.modules;

import java.util.function.Predicate;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_3489;

final class CombatItems {
   private CombatItems() {
   }

   static boolean isWeapon(class_1799 stack) {
      return stack.method_31573(class_3489.field_42611)
         || stack.method_31573(class_3489.field_42612)
         || stack.method_31574(class_1802.field_49814)
         || stack.method_31574(class_1802.field_8547);
   }

   static int hotbarSlot(Predicate<class_1799> wanted) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (wanted.test(mc.field_1724.method_31548().method_5438(i))) {
               return i;
            }
         }

         return -1;
      }
   }
}
