package dev.violet.modules.entitycontrol;

import dev.violet.module.ModuleRegistry;
import dev.violet.modules.BoatNoclip;
import dev.violet.modules.EntityControl;
import dev.violet.modules.EntitySpeed;
import dev.violet.modules.Freecam;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_10185;
import net.minecraft.class_10255;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_746;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;
import net.minecraft.class_4970.class_4971;
import org.lwjgl.glfw.GLFW;

public final class EntityControlVehicleHook {
   private static final double DIAGONAL = 1.0 / Math.sqrt(2.0);
   public static volatile boolean anyActive;

   private EntityControlVehicleHook() {
   }

   public static void refresh() {
      anyActive = EntityControl.active != null || EntitySpeed.active != null || BoatNoclip.active != null;
   }

   public static class_243 onEntityMove(class_1297 entity, class_1313 type, class_243 movement) {
      if (type == class_1313.field_6308 && entity.method_73183().method_8608()) {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null && player.method_5854() == entity && entity.method_5642() == player) {
            class_243 velocity = entity.method_18798();
            BoatNoclip noclip = BoatNoclip.active;
            if (noclip != null && entity instanceof class_10255 boat) {
               class_243 result = noclip.boatMovement(boat, player);
               entity.method_36456(player.method_36454());
               if (movement == velocity) {
                  entity.method_18799(result);
               }

               return result;
            } else {
               EntityControl control = EntityControl.active;
               EntitySpeed speed = EntitySpeed.active;
               double velX = velocity.field_1352;
               double velY = velocity.field_1351;
               double velZ = velocity.field_1350;
               boolean overridden = false;
               if (speed != null && speed.appliesTo(entity)) {
                  class_243 horizontal = horizontalVelocity(player, speed.horizontalSpeed());
                  velX = horizontal.field_1352;
                  velZ = horizontal.field_1350;
                  overridden = true;
               }

               if (control != null && control.locksYaw(entity)) {
                  entity.method_36456(player.method_36454());
               }

               if (!overridden) {
                  return movement;
               } else {
                  class_243 result = new class_243(velX, velY, velZ);
                  if (movement == velocity) {
                     entity.method_18799(result);
                  }

                  return result;
               }
            }
         } else {
            return movement;
         }
      } else {
         return movement;
      }
   }

   public static boolean locksBoatTurning(class_10255 boat) {
      if (!anyActive || !boat.method_73183().method_8608()) {
         return false;
      } else if (BoatNoclip.active != null) {
         return true;
      } else {
         EntityControl control = EntityControl.active;
         return control != null && control.locksYaw(boat);
      }
   }

   public static class_243 horizontalVelocity(class_746 player, double bps) {
      float yaw = player.method_36454();
      class_243 forward = class_243.method_1030(0.0F, yaw);
      class_243 right = class_243.method_1030(0.0F, yaw + 90.0F);
      class_10185 input = player.field_3913.field_54155;
      double velX = 0.0;
      double velZ = 0.0;
      boolean longitudinal = false;
      if (input.comp_3159()) {
         velX += forward.field_1352 / 20.0 * bps;
         velZ += forward.field_1350 / 20.0 * bps;
         longitudinal = true;
      }

      if (input.comp_3160()) {
         velX -= forward.field_1352 / 20.0 * bps;
         velZ -= forward.field_1350 / 20.0 * bps;
         longitudinal = true;
      }

      boolean lateral = false;
      if (input.comp_3162()) {
         velX += right.field_1352 / 20.0 * bps;
         velZ += right.field_1350 / 20.0 * bps;
         lateral = true;
      }

      if (input.comp_3161()) {
         velX -= right.field_1352 / 20.0 * bps;
         velZ -= right.field_1350 / 20.0 * bps;
         lateral = true;
      }

      if (longitudinal && lateral) {
         velX *= DIAGONAL;
         velZ *= DIAGONAL;
      }

      return new class_243(velX, 0.0, velZ);
   }

   public static boolean isPhysicallyDown(class_304 mapping) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1755 == null && !mapping.method_1415() && !isFreecamActive()) {
         class_306 key = KeyBindingHelper.getBoundKeyOf(mapping);
         class_307 type = key.method_1442();
         if (type == class_307.field_1668) {
            return class_3675.method_15987(mc.method_22683(), key.method_1444());
         } else {
            return type == class_307.field_1672 ? GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), key.method_1444()) == 1 : mapping.method_1434();
         }
      } else {
         return false;
      }
   }

   public static boolean isOnAir(class_1297 entity) {
      return entity.method_73183().method_29546(entity.method_5829().method_1014(0.0625).method_1012(0.0, -0.55, 0.0)).allMatch(class_4971::method_26215);
   }

   private static boolean isFreecamActive() {
      Freecam freecam = ModuleRegistry.get(Freecam.class);
      return freecam != null && freecam.isActive();
   }
}
