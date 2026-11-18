package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1802;

public class AutoDoubleHand extends Module {
   private final Setting<Integer> health = this.add(
      new IntSetting("Health", "Holds a totem in your main hand at or below this many hearts. 0 only reacts to pops.", 4, 0, 20) {
         @Override
         public String display() {
            return this.value == 0 ? "Off" : this.value + " hearts";
         }
      }
   );
   private final Setting<Boolean> onPop = this.add(new BoolSetting("On Pop", "Grabs a totem from your hotbar right after your offhand totem pops.", true));
   private final Setting<Boolean> switchBack = this.add(
      new BoolSetting("Switch Back", "Goes back to the slot you held once you are above the health again.", true)
   );
   private boolean hadOffhandTotem;
   private int previous = -1;

   public AutoDoubleHand() {
      super("Auto Double Hand", "Puts a totem in your main hand too when you are low or your offhand totem just popped.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.previous = -1;
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && !mc.field_1724.method_68878() && !mc.field_1724.method_7325() && mc.field_1755 == null) {
         boolean offhandTotem = mc.field_1724.method_6079().method_31574(class_1802.field_8288);
         boolean popped = this.hadOffhandTotem && !offhandTotem && mc.field_1724.field_6235 > 0;
         this.hadOffhandTotem = offhandTotem;
         float hp = mc.field_1724.method_6032() + mc.field_1724.method_6067();
         boolean low = this.health.get() > 0 && hp <= (float)this.health.get().intValue() * 2.0F;
         if ((low || this.onPop.get() && popped) && !mc.field_1724.method_6047().method_31574(class_1802.field_8288)) {
            int slot = CombatItems.hotbarSlot(stack -> stack.method_31574(class_1802.field_8288));
            if (slot >= 0) {
               if (this.previous < 0) {
                  this.previous = mc.field_1724.method_31548().method_67532();
               }

               mc.field_1724.method_31548().method_61496(slot);
            }
         } else {
            if (!low && !popped && this.previous >= 0 && mc.field_1724.field_6235 == 0) {
               if (this.switchBack.get()) {
                  mc.field_1724.method_31548().method_61496(this.previous);
               }

               this.previous = -1;
            }
         }
      }
   }
}
