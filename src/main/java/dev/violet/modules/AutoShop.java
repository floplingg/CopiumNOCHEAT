package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.ItemSetting;
import dev.violet.setting.Setting;
import dev.violet.util.DonutUtils;
import dev.violet.util.InvUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_490;

public class AutoShop extends Module {
   private static final Map<class_1792, int[]> SHOP_SLOTS = new LinkedHashMap<>();
   private final Setting<class_1792> item = this.add(
      new ItemSetting("Item", "Item to buy from /shop (item id, e.g. ender_pearl).", class_1802.field_8545, SHOP_SLOTS::containsKey)
   );
   private final Setting<AutoShop.Mode> mode = this.add(
      new EnumSetting<>("Mode", "Drop: throw the bought items. Order: deliver them to the best paying /order.", AutoShop.Mode.Drop)
   );
   private final Setting<Integer> amount = this.add(new IntSetting("Amount", "Stacks of 64 to buy before emptying the inventory.", 36, 1, 36));
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks between actions.", 2, 0, 20));
   private final Setting<Integer> dropDelay = this.add(new IntSetting("Drop Delay", "Ticks between dropped stacks.", 1, 0, 20))
      .visible(() -> this.mode.get() == AutoShop.Mode.Drop);
   private int wait;
   private int bought;
   private boolean buying;
   private boolean emptying;
   private int categorySyncId = -1;
   private int orderSyncId = -1;

   public AutoShop() {
      super("Auto Shop", "Automatically buys the chosen item from /shop.", Category.DONUT);
   }

   @Override
   public void onEnable() {
      this.wait = 0;
      this.bought = 0;
      this.buying = false;
      this.emptying = false;
      this.categorySyncId = -1;
      this.orderSyncId = -1;
   }

   @Override
   public void onTick() {
      if (this.wait > 0) {
         this.wait--;
      } else {
         int[] slots = SHOP_SLOTS.get(this.item.get());
         if (slots != null) {
            if (this.emptying) {
               if (this.mode.get() == AutoShop.Mode.Drop) {
                  this.dropItems();
               } else {
                  this.deliverItems();
               }
            } else if (mc.field_1724.field_7512 instanceof class_1707 container) {
               if (container.method_17388() != 3) {
                  mc.field_1724.method_7346();
                  this.wait = 10;
               } else if (this.buying) {
                  if (container.field_7763 == this.categorySyncId) {
                     this.wait = 1;
                  } else if (!container.method_7611(slots[1]).method_7677().method_31574(this.item.get())) {
                     DonutUtils.click(container.field_7763, 17, 0, class_1713.field_7790);
                     DonutUtils.click(container.field_7763, 23, 0, class_1713.field_7790);
                     this.wait = this.delay.get();
                     if (++this.bought >= this.amount.get()) {
                        this.bought = 0;
                        this.buying = false;
                        this.emptying = true;
                     }
                  } else {
                     this.buying = false;
                  }
               } else if (container.method_7611(slots[1]).method_7677().method_31574(this.item.get())) {
                  DonutUtils.click(container.field_7763, slots[1], 0, class_1713.field_7790);
                  this.buying = true;
                  this.categorySyncId = container.field_7763;
                  this.wait = this.delay.get();
               } else {
                  DonutUtils.click(container.field_7763, slots[0], 0, class_1713.field_7790);
                  this.wait = this.delay.get();
               }
            } else {
               this.buying = false;
               this.categorySyncId = -1;
               DonutUtils.command("shop");
               this.wait = 20;
            }
         }
      }
   }

   private void dropItems() {
      if (mc.field_1724.field_7512 instanceof class_1707) {
         mc.field_1724.method_7346();
         this.wait = 3;
      } else if (mc.field_1755 == null) {
         mc.method_1507(new class_490(mc.field_1724));
         this.wait = 1;
      } else {
         for (int i = 0; i < 36; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_31574(this.item.get())) {
               int slot = i < 9 ? i + 36 : i;
               DonutUtils.click(mc.field_1724.field_7498.field_7763, slot, 1, class_1713.field_7795);
               if (this.dropDelay.get() > 0) {
                  this.wait = this.dropDelay.get();
                  return;
               }
            }
         }

         mc.method_1507(null);
         this.emptying = false;
      }
   }

   private void deliverItems() {
      if (!InvUtil.has(this.item.get())) {
         if (mc.field_1724.field_7512 instanceof class_1707) {
            mc.field_1724.method_7346();
         }

         this.emptying = false;
         this.orderSyncId = -1;
         this.wait = 10;
      } else if (!(mc.field_1724.field_7512 instanceof class_1707 container)) {
         DonutUtils.command("order " + this.item.get().method_63680().getString());
         this.wait = 20;
      } else {
         switch (container.method_17388()) {
            case 3:
               DonutUtils.click(container.field_7763, 15, 0, class_1713.field_7790);
               this.orderSyncId = -1;
               this.wait = 10;
               break;
            case 4:
               int start = container.method_17388() * 9;

               for (int slot = start; slot < container.field_7761.size(); slot++) {
                  if (container.method_7611(slot).method_7677().method_31574(this.item.get())) {
                     DonutUtils.click(container.field_7763, slot, 1, class_1713.field_7794);
                     this.wait = this.delay.get();
                     return;
                  }
               }

               mc.field_1724.method_7346();
               this.wait = 10;
               break;
            case 5:
            default:
               mc.field_1724.method_7346();
               this.wait = 10;
               break;
            case 6:
               this.pickOrder(container);
         }
      }
   }

   private void pickOrder(class_1707 container) {
      if (container.field_7763 == this.orderSyncId) {
         this.wait = 2;
      } else {
         class_1799 sortButton = container.method_7611(47).method_7677();
         if (sortButton.method_7960()) {
            mc.field_1724.method_7346();
            this.emptying = false;
            this.wait = 5;
         } else if (!DonutUtils.isSortActive(sortButton, "Most Money Per Item")) {
            DonutUtils.click(container.field_7763, 47, 1, class_1713.field_7794);
            this.wait = 2;
         } else {
            for (int slot = 0; slot < 45; slot++) {
               if (container.method_7611(slot).method_7677().method_31574(this.item.get())) {
                  DonutUtils.click(container.field_7763, slot, 1, class_1713.field_7794);
                  this.orderSyncId = container.field_7763;
                  this.wait = 2;
                  return;
               }
            }

            mc.field_1724.method_7346();
            this.emptying = false;
            this.wait = 10;
         }
      }
   }

   static {
      SHOP_SLOTS.put(class_1802.field_8466, new int[]{11, 9});
      SHOP_SLOTS.put(class_1802.field_8634, new int[]{11, 10});
      SHOP_SLOTS.put(class_1802.field_20399, new int[]{11, 11});
      SHOP_SLOTS.put(class_1802.field_8613, new int[]{11, 12});
      SHOP_SLOTS.put(class_1802.field_8056, new int[]{11, 13});
      SHOP_SLOTS.put(class_1802.field_8233, new int[]{11, 14});
      SHOP_SLOTS.put(class_1802.field_8882, new int[]{11, 15});
      SHOP_SLOTS.put(class_1802.field_8815, new int[]{11, 16});
      SHOP_SLOTS.put(class_1802.field_8545, new int[]{11, 17});
      SHOP_SLOTS.put(class_1802.field_8894, new int[]{12, 9});
      SHOP_SLOTS.put(class_1802.field_8790, new int[]{12, 10});
      SHOP_SLOTS.put(class_1802.field_8601, new int[]{12, 11});
      SHOP_SLOTS.put(class_1802.field_8135, new int[]{12, 12});
      SHOP_SLOTS.put(class_1802.field_8070, new int[]{12, 13});
      SHOP_SLOTS.put(class_1802.field_8155, new int[]{12, 14});
      SHOP_SLOTS.put(class_1802.field_8067, new int[]{12, 15});
      SHOP_SLOTS.put(class_1802.field_8354, new int[]{12, 16});
      SHOP_SLOTS.put(class_1802.field_22421, new int[]{12, 17});
      SHOP_SLOTS.put(class_1802.field_8281, new int[]{13, 9});
      SHOP_SLOTS.put(class_1802.field_8301, new int[]{13, 10});
      SHOP_SLOTS.put(class_1802.field_23141, new int[]{13, 11});
      SHOP_SLOTS.put(class_1802.field_8801, new int[]{13, 12});
      SHOP_SLOTS.put(class_1802.field_8288, new int[]{13, 13});
      SHOP_SLOTS.put(class_1802.field_8463, new int[]{13, 15});
      SHOP_SLOTS.put(class_1802.field_8287, new int[]{13, 16});
      SHOP_SLOTS.put(class_1802.field_8087, new int[]{13, 17});
      SHOP_SLOTS.put(class_1802.field_8567, new int[]{14, 9});
      SHOP_SLOTS.put(class_1802.field_16998, new int[]{14, 10});
      SHOP_SLOTS.put(class_1802.field_8497, new int[]{14, 11});
      SHOP_SLOTS.put(class_1802.field_8179, new int[]{14, 12});
      SHOP_SLOTS.put(class_1802.field_8279, new int[]{14, 13});
      SHOP_SLOTS.put(class_1802.field_8544, new int[]{14, 14});
      SHOP_SLOTS.put(class_1802.field_8176, new int[]{14, 15});
      SHOP_SLOTS.put(class_1802.field_8071, new int[]{14, 16});
      SHOP_SLOTS.put(class_1802.field_8367, new int[]{14, 17});
   }

   public static enum Mode {
      Drop,
      Order;
   }
}
