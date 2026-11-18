package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.ModeSetting;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class Sneak extends BusModule {
   public static volatile Sneak active;
   private final ModeSetting mode = this.addSetting("mode", new ModeSetting("Mode", "Vanilla", "Packet", "Vanilla"));

   public Sneak() {
      super("sneak", "Sneak", "Sneaks for you.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      active = this;
   }

   @Override
   public void onDisable() {
      active = null;
   }

   public boolean doPacket() {
      return this.isEnabled() && this.mode.is("Packet") && !isFlying();
   }

   public boolean doVanilla() {
      return this.isEnabled() && this.mode.is("Vanilla") && !isFlying();
   }

   private static boolean isFlying() {
      class_746 player = class_310.method_1551().field_1724;
      return player == null || player.method_31549().field_7479;
   }
}
