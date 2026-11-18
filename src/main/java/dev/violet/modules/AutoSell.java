package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.ItemSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.DonutUtils;
import dev.violet.util.InvUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class AutoSell extends Module {
   private final Setting<AutoSell.Mode> mode = this.add(
      new EnumSetting<>("Mode", "Sell: /sell GUI. Order: deliver to the best paying /order.", AutoSell.Mode.Sell)
   );
   private final Setting<class_1792> item = this.add(new ItemSetting("Item", "Item to sell (item id, e.g. sea_pickle).", class_1802.field_17498));
   private final Setting<String> targetPlayer = this.add(new StringSetting("Order Player", "Only deliver to orders of this player (empty = anyone).", ""))
      .visible(() -> this.mode.get() == AutoSell.Mode.Order);
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Delay in ticks between moving stacks.", 2, 0, 20));
   private final Setting<Boolean> quickDeliver = this.add(new BoolSetting("Quick Deliver", "Move all matching stacks in a single tick.", true));
   private int wait;

   public AutoSell() {
      super("Auto Sell", "Automatically sells the chosen item via /sell or /order.", Category.DONUT);
   }

   @Override
   public void onEnable() {
      this.wait = 0;
   }

   @Override
   public void onTick() {
      if (this.wait > 0) {
         this.wait--;
      } else if (mc.field_1724.field_7512 instanceof class_1707 container) {
         switch (container.method_17388()) {
            case 3:
               if (this.mode.get() == AutoSell.Mode.Order) {
                  DonutUtils.click(container.field_7763, 15, 1, class_1713.field_7794);
                  this.wait = 10;
               }
               break;
            case 4:
               this.depositItems(container);
            case 5:
            default:
               break;
            case 6:
               if (this.mode.get() == AutoSell.Mode.Order) {
                  this.pickOrder(container);
               }
         }
      } else {
         this.openGui();
      }
   }

   private void openGui() {
      if (!InvUtil.has(this.item.get())) {
         this.wait = 20;
      } else {
         if (this.mode.get() == AutoSell.Mode.Sell) {
            DonutUtils.command("sell");
         } else {
            String name = this.item.get() == class_1802.field_8606 ? "Bones" : this.item.get().method_63680().getString();
            if (!this.targetPlayer.get().isBlank()) {
               name = name + " " + this.targetPlayer.get().trim();
            }

            DonutUtils.command("order " + name);
         }

         this.wait = 20;
      }
   }

   private void depositItems(class_1707 container) {
      int containerSize = container.method_17388() * 9;
      List<Integer> slots = new ArrayList<>();
      class_1799 reference = class_1799.field_8037;

      for (int slot = containerSize; slot < container.field_7761.size(); slot++) {
         class_1799 stack = container.method_7611(slot).method_7677();
         if (!stack.method_7960() && stack.method_7909() == this.item.get()) {
            if (reference.method_7960()) {
               reference = stack;
            }

            if (class_1799.method_31577(reference, stack)) {
               slots.add(slot);
            }
         }
      }

      if (slots.isEmpty()) {
         mc.field_1724.method_7346();
         this.wait = 10;
      } else {
         if (!this.quickDeliver.get() && this.delay.get() != 0) {
            DonutUtils.click(container.field_7763, slots.getFirst(), 0, class_1713.field_7794);
            this.wait = this.delay.get();
         } else {
            for (int slotx : slots) {
               DonutUtils.click(container.field_7763, slotx, 0, class_1713.field_7794);
            }

            this.wait = 5;
         }
      }
   }

   private void pickOrder(class_1707 container) {
      class_1799 sortButton = container.method_7611(47).method_7677();
      if (sortButton.method_7960()) {
         this.wait = 2;
      } else if (!DonutUtils.isSortActive(sortButton, "Most Money Per Item")) {
         DonutUtils.click(container.field_7763, 47, 1, class_1713.field_7794);
         this.wait = 5;
      } else {
         for (int slot = 0; slot < 45; slot++) {
            if (container.method_7611(slot).method_7677().method_31574(this.item.get())) {
               DonutUtils.click(container.field_7763, slot, 1, class_1713.field_7794);
               this.wait = 10;
               return;
            }
         }

         mc.field_1724.method_7346();
         this.wait = 40;
      }
   }

   public static enum Mode {
      Sell,
      Order;
   }
}
