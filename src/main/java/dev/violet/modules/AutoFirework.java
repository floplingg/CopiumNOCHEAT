package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.KeySetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1268;
import net.minecraft.class_1802;

public class AutoFirework extends Module {
   private final KeySetting key = (KeySetting)this.<Integer>add(
      new KeySetting("Firework Key", "Press it to fire a rocket from your hotbar. Click and press a key. Right click removes it.", () -> null)
   );
   private final Setting<Boolean> glidingOnly = this.add(new BoolSetting("Only While Flying", "Only fires while you fly with an elytra.", true));
   private final Setting<Boolean> switchBack = this.add(new BoolSetting("Switch Back", "Goes back to the slot you held before.", true));
   private boolean keyDown;
   private int previous = -1;

   public AutoFirework() {
      super("Auto Firework", "Switches to a firework rocket, uses it and switches back when you press your firework key.", Category.COMBAT);
   }

   @Override
   public void onEnable() {
      this.keyDown = this.key.isPressed();
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.previous >= 0) {
            if (this.switchBack.get()) {
               mc.field_1724.method_31548().method_61496(this.previous);
            }

            this.previous = -1;
         }

         boolean down = this.key.isPressed();
         boolean pressed = down && !this.keyDown;
         this.keyDown = down;
         if (pressed && (!this.glidingOnly.get() || mc.field_1724.method_6128())) {
            int slot = CombatItems.hotbarSlot(stack -> stack.method_31574(class_1802.field_8639));
            if (slot < 0) {
               this.warning("No firework rockets in your hotbar.", new Object[0]);
            } else {
               int held = mc.field_1724.method_31548().method_67532();
               mc.field_1724.method_31548().method_61496(slot);
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               mc.field_1724.method_6104(class_1268.field_5808);
               if (held != slot) {
                  this.previous = held;
               }
            }
         }
      }
   }

   @Override
   public String getInfoString() {
      return this.key.display();
   }
}
