package dev.violet.modules;

import dev.violet.event.Stage;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.module.ModuleRegistry;
import dev.violet.modules.entitycontrol.EntityControlVehicleHook;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_10255;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2338.class_2339;

public final class BoatNoclip extends BusModule {
   public static volatile BoatNoclip active;
   private final NumberSetting speed = this.addSetting("speed", new NumberSetting("Speed", 10.0F, 0.0F, 50.0F).step(0.5F));
   private final NumberSetting speedInsideBlocks = this.addSetting(
      "speed-inside-blocks", new NumberSetting("Speed Inside Blocks", 5.0F, 0.0F, 50.0F).step(0.5F)
   );
   private final NumberSetting verticalSpeed = this.addSetting("vertical-speed", new NumberSetting("Vertical Speed", 6.0F, 0.0F, 20.0F).step(0.5F));
   private final NumberSetting fallSpeed = this.addSetting("fall-speed", new NumberSetting("Fall Speed", 0.0F, 0.0F, 10.0F).step(0.1F));
   private class_10255 noclipBoat;

   public BoatNoclip() {
      super("boat-noclip", "Boat Noclip", "Fly your boat through blocks and entities.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      Boatfly fly = ModuleRegistry.get(Boatfly.class);
      if (fly != null) {
         fly.setEnabled(false);
      }

      active = this;
      EntityControlVehicleHook.refresh();
      this.on(TickEvent.TYPE, Stage.PRE, this::onPreTick);
   }

   @Override
   public void onDisable() {
      active = null;
      EntityControlVehicleHook.refresh();
      this.releaseBoat();
   }

   private void onPreTick(TickEvent event) {
      class_746 player = class_310.method_1551().field_1724;
      if (this.noclipBoat != null && (player == null || player.method_5854() != this.noclipBoat)) {
         this.releaseBoat();
      }
   }

   private void releaseBoat() {
      if (this.noclipBoat != null) {
         this.noclipBoat.field_5960 = false;
      }

      this.noclipBoat = null;
   }

   public class_243 boatMovement(class_10255 boat, class_746 player) {
      if (this.noclipBoat != boat) {
         this.releaseBoat();
         this.noclipBoat = boat;
      }

      boat.field_5960 = true;
      double blocksPerSecond = isInsideBlock(boat, player) ? (double)this.speedInsideBlocks.get() : (double)this.speed.get();
      class_243 horizontal = EntityControlVehicleHook.horizontalVelocity(player, blocksPerSecond);
      class_310 mc = class_310.method_1551();
      double velY = 0.0;
      if (EntityControlVehicleHook.isPhysicallyDown(mc.field_1690.field_1903)) {
         velY += (double)this.verticalSpeed.get() / 20.0;
      }

      if (EntityControlVehicleHook.isPhysicallyDown(mc.field_1690.field_1867)) {
         velY -= (double)this.verticalSpeed.get() / 20.0;
      } else {
         velY -= (double)this.fallSpeed.get() / 20.0;
      }

      return new class_243(horizontal.field_1352, velY, horizontal.field_1350);
   }

   private static boolean isInsideBlock(class_10255 boat, class_746 player) {
      class_638 world = class_310.method_1551().field_1687;
      if (world == null) {
         return false;
      } else {
         class_238 boatBox = boat.method_5829();
         class_238 box = boatBox.method_1002(0.0, 0.05, 0.0).method_1009(0.5, 0.0, 0.5);
         class_2339 pos = new class_2339();

         for (int x = class_3532.method_15357(box.field_1323); x <= class_3532.method_15357(box.field_1320); x++) {
            for (int y = class_3532.method_15357(box.field_1322); y <= class_3532.method_15357(box.field_1325); y++) {
               for (int z = class_3532.method_15357(box.field_1321); z <= class_3532.method_15357(box.field_1324); z++) {
                  class_2680 state = world.method_8320(pos.method_10103(x, y, z));
                  if (!state.method_26215()) {
                     class_265 shape = state.method_26194(world, pos, class_3726.method_16194());
                     if (!shape.method_1110() && boatBox.method_994(shape.method_1107().method_996(pos))) {
                        return true;
                     }
                  }
               }
            }
         }

         for (class_1297 other : world.method_8335(boat, box)) {
            if (other != player && !other.method_5626(boat) && !boat.method_5626(other) && other.method_5805() && other.method_5829().method_994(box)) {
               return true;
            }
         }

         return false;
      }
   }
}
