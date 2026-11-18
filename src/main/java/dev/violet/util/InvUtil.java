package dev.violet.util;

import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_310;

public final class InvUtil {
   private static final class_310 mc = class_310.method_1551();

   private InvUtil() {
   }

   public static int count(class_1792 item) {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i < 36; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_31574(item)) {
               count += stack.method_7947();
            }
         }

         return count;
      }
   }

   public static boolean has(class_1792 item) {
      return find(item, 0, 35) != -1;
   }

   public static int findHotbar(class_1792 item) {
      return find(item, 0, 8);
   }

   public static int findMain(class_1792 item) {
      return find(item, 9, 35);
   }

   private static int find(class_1792 item, int from, int to) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = from; i <= to; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_31574(item)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static void select(int hotbarSlot) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(hotbarSlot);
      }
   }

   public static void moveToHotbar(int inventorySlot, int hotbarSlot) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         int screenSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
         mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, screenSlot, hotbarSlot, class_1713.field_7791, mc.field_1724);
      }
   }
}
