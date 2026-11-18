package dev.violet.modules;

import dev.violet.mixin.MinecraftClientAccessor;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1802;

public class FastUse extends Module {
   private final Setting<FastUse.Mode> mode = this.add(
      new EnumSetting<>(
         "Mode", "All places and throws every item as fast as possible. Xp Bottles only speeds up throwing experience bottles.", FastUse.Mode.All
      )
   );

   public FastUse() {
      super("Fast Use", "Removes the delay between placing blocks and throwing items while you hold right click.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null) {
         MinecraftClientAccessor client = (MinecraftClientAccessor)mc;
         if (client.violet$getItemUseCooldown() > 0) {
            if (this.mode.get() != FastUse.Mode.XpBottles
               || mc.field_1724.method_6047().method_31574(class_1802.field_8287)
               || mc.field_1724.method_6079().method_31574(class_1802.field_8287)) {
               client.violet$setItemUseCooldown(0);
            }
         }
      }
   }

   @Override
   public String getInfoString() {
      return this.mode.get().toString();
   }

   public static enum Mode {
      All("All"),
      XpBottles("Xp Bottles");

      private final String title;

      private Mode(String title) {
         this.title = title;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
