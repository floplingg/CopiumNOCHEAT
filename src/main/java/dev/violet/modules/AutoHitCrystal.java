package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1511;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class AutoHitCrystal extends Module {
   private final Setting<Boolean> obsidian = this.add(new BoolSetting("Place Obsidian", "Also places obsidian first when you look at another block.", true));
   private final Setting<Integer> placeDelay = this.add(new IntSetting("Place Delay", "Ticks between placing obsidian and the crystal.", 1, 0, 10));
   private final Setting<Integer> breakDelay = this.add(new IntSetting("Break Delay", "Ticks between placing the crystal and hitting it.", 1, 0, 10));
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to the slot you held before when you let go.", true));
   private int timer;
   private int previous = -1;

   public AutoHitCrystal() {
      super("Auto Hit Crystal", "Hold right click: places obsidian, puts an end crystal on it and hits the crystal, again and again.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.restore();
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null && !mc.field_1724.method_7325()) {
         if (mc.field_1755 != null || !mc.field_1690.field_1904.method_1434()) {
            this.restore();
         } else if (this.timer > 0) {
            this.timer--;
         } else {
            class_239 hit = mc.field_1765;
            if (hit instanceof class_3966 entityHit && entityHit.method_17782() instanceof class_1511 crystal) {
               mc.field_1761.method_2918(mc.field_1724, crystal);
               mc.field_1724.method_6104(class_1268.field_5808);
               this.timer = this.placeDelay.get();
               return;
            }

            if (hit instanceof class_3965 blockHit && hit.method_17783() == class_240.field_1332) {
               class_2338 pos = blockHit.method_17777();
               class_2680 state = mc.field_1687.method_8320(pos);
               if (!state.method_27852(class_2246.field_10540) && !state.method_27852(class_2246.field_9987)) {
                  if (this.obsidian.get() && this.use(stack -> stack.method_31574(class_1802.field_8281), blockHit)) {
                     this.timer = this.placeDelay.get();
                  }

                  return;
               }

               if (!this.freeAbove(pos)) {
                  return;
               }

               if (this.use(stack -> stack.method_31574(class_1802.field_8301), blockHit)) {
                  this.timer = this.breakDelay.get();
               }

               return;
            }
         }
      }
   }

   private boolean freeAbove(class_2338 pos) {
      class_2338 above = pos.method_10084();
      if (!mc.field_1687.method_22347(above)) {
         return false;
      } else {
         class_238 space = new class_238(
            (double)above.method_10263(),
            (double)above.method_10264(),
            (double)above.method_10260(),
            (double)(above.method_10263() + 1),
            (double)(above.method_10264() + 2),
            (double)(above.method_10260() + 1)
         );
         return mc.field_1687.method_8335(null, space).isEmpty();
      }
   }

   private boolean use(Predicate<class_1799> item, class_3965 hit) {
      int slot = CombatItems.hotbarSlot(item);
      if (slot < 0) {
         return false;
      } else {
         if (this.previous < 0) {
            this.previous = mc.field_1724.method_31548().method_67532();
         }

         mc.field_1724.method_31548().method_61496(slot);
         mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
         mc.field_1724.method_6104(class_1268.field_5808);
         return true;
      }
   }

   private void restore() {
      if (this.previous >= 0 && this.switchBack.get() && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.previous);
      }

      this.previous = -1;
   }
}
