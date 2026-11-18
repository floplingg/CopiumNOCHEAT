package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.Setting;

public final class InventoryHud extends Module {
   public final Setting<Boolean> emptySlots = this.add(new BoolSetting("Empty Slots", "Show subtle cells for empty slots.", true));

   public InventoryHud() {
      super("Inventory HUD", "Movable inventory grid for the HUD editor.", Category.CLIENT);
   }

   @Override
   public boolean showInHud() {
      return false;
   }
}
