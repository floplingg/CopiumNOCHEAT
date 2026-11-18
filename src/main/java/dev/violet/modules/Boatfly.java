package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.KeySetting;
import dev.violet.setting.Setting;
import net.minecraft.class_10255;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_243;

public class Boatfly extends Module {
   private final Setting<Integer> speed = this.add(new IntSetting("Speed", "Horizontal flying speed, in blocks per second.", 20, 1, 100));
   private final Setting<Integer> verticalSpeed = this.add(
      new IntSetting("Vertical Speed", "How fast you rise or sink with the height key, the up key or the down key, in blocks per second.", 10, 1, 60)
   );
   private final Setting<Boolean> phase = this.add(
      new BoolSetting(
         "Phase", "Flies through blocks with the boat. Only works on servers that do not check vehicle movement, otherwise the boat gets pulled back.", false
      )
   );
   private final Setting<Boolean> turn = this.add(new BoolSetting("Face Look Direction", "Turns the boat to where you are looking.", true));
   private final Setting<Boolean> allVehicles = this.add(
      new BoolSetting("All Vehicles", "Also flies with horses, pigs, striders and other rideable mobs, not only boats.", false)
   );
   private final KeySetting forwardKey;
   private final KeySetting backKey;
   private final KeySetting leftKey;
   private final KeySetting rightKey;
   private final KeySetting heightKey;
   private final KeySetting upKey;
   private final KeySetting downKey;
   private final KeySetting exitKey;
   private static boolean moving;
   private static class_1297 phased;

   public Boatfly() {
      super(
         "Boatfly",
         "Fly with a boat. WASD moves, hold the sprint key (Ctrl) and look up or down to change height, jump (Space) gets you out. All keys can be changed in the settings. Servers that do not allow flying can kick you after a few seconds in the air.",
         Category.MOVEMENT
      );
      this.group("Keys");
      this.forwardKey = (KeySetting)this.<Integer>add(
         new KeySetting("Forward Key", "Flies forward. Click and press a key. Right click resets it to your Minecraft key.", () -> mc.field_1690.field_1894)
      );
      this.backKey = (KeySetting)this.<Integer>add(
         new KeySetting("Back Key", "Flies backwards. Click and press a key. Right click resets it to your Minecraft key.", () -> mc.field_1690.field_1881)
      );
      this.leftKey = (KeySetting)this.<Integer>add(
         new KeySetting("Left Key", "Flies to the left. Click and press a key. Right click resets it to your Minecraft key.", () -> mc.field_1690.field_1913)
      );
      this.rightKey = (KeySetting)this.<Integer>add(
         new KeySetting("Right Key", "Flies to the right. Click and press a key. Right click resets it to your Minecraft key.", () -> mc.field_1690.field_1849)
      );
      this.heightKey = (KeySetting)this.<Integer>add(
         new KeySetting("Height Key", "Hold it and look up to rise or look down to sink. Default is your sprint key (Ctrl).", () -> mc.field_1690.field_1867)
      );
      this.upKey = (KeySetting)this.<Integer>add(new KeySetting("Up Key", "Optional key that always rises while held. Right click removes it.", () -> null));
      this.downKey = (KeySetting)this.<Integer>add(new KeySetting("Down Key", "Optional key that always sinks while held. Right click removes it.", () -> null));
      this.exitKey = (KeySetting)this.<Integer>add(
         new KeySetting("Exit Key", "Gets you out of the boat. Default is your jump key (Space).", () -> mc.field_1690.field_1903)
      );
   }

   private static Boatfly instance() {
      return SoberClient.MODULES.get(Boatfly.class);
   }

   public static boolean controlling() {
      if (mc.field_1724 != null && mc.field_1724.method_5854() != null) {
         Boatfly fly = instance();
         return fly != null && fly.isActive() ? mc.field_1724.method_5854() instanceof class_10255 || fly.allVehicles.get() : false;
      } else {
         return false;
      }
   }

   public static boolean exitPressed() {
      Boatfly fly = instance();
      return fly != null && fly.exitKey.isPressed();
   }

   public static boolean move(class_1297 entity, class_1313 type) {
      if (!moving && type == class_1313.field_6308 && mc.field_1724 != null && entity == mc.field_1724.method_5854()) {
         Boatfly fly = instance();
         if (fly == null || !fly.isActive()) {
            return false;
         } else if (!(entity instanceof class_10255) && !fly.allVehicles.get()) {
            return false;
         } else {
            double vertical = 0.0;
            if (fly.heightKey.isPressed()) {
               double pitch = Math.sin(Math.toRadians((double)mc.field_1724.method_36455()));
               if (Math.abs(pitch) > 0.15) {
                  vertical = -pitch;
               }
            }

            boolean up = fly.upKey.isPressed();
            boolean down = fly.downKey.isPressed();
            if (up != down) {
               vertical = up ? 1.0 : -1.0;
            }

            class_243 velocity = fly.horizontal(mc.field_1724.method_36454(), (double)fly.speed.get().intValue() / 20.0)
               .method_1031(0.0, vertical * (double)fly.verticalSpeed.get().intValue() / 20.0, 0.0);
            if (fly.turn.get()) {
               entity.method_36456(mc.field_1724.method_36454());
            }

            entity.method_18799(velocity);
            if (fly.phase.get()) {
               entity.field_5960 = true;
               phased = entity;
               entity.method_5814(
                  entity.method_23317() + velocity.field_1352, entity.method_23318() + velocity.field_1351, entity.method_23321() + velocity.field_1350
               );
               return true;
            } else {
               moving = true;

               try {
                  entity.method_5784(type, velocity);
               } finally {
                  moving = false;
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean phasing() {
      return phased != null && mc.field_1724 != null && phased == mc.field_1724.method_5854();
   }

   private void release(boolean always) {
      if (phased != null) {
         if (always || !this.phase.get() || !phasing()) {
            phased.field_5960 = false;
            phased = null;
         }
      }
   }

   @Override
   public void onTick() {
      this.release(false);
   }

   @Override
   public void onDisable() {
      this.release(true);
   }

   @Override
   public void onGameLeft() {
      phased = null;
   }

   private class_243 horizontal(float yaw, double speed) {
      double forward = 0.0;
      double side = 0.0;
      if (this.forwardKey.isPressed()) {
         forward++;
      }

      if (this.backKey.isPressed()) {
         forward--;
      }

      if (this.leftKey.isPressed()) {
         side++;
      }

      if (this.rightKey.isPressed()) {
         side--;
      }

      if (forward == 0.0 && side == 0.0) {
         return class_243.field_1353;
      } else {
         double length = Math.sqrt(forward * forward + side * side);
         forward /= length;
         side /= length;
         double radians = Math.toRadians((double)yaw);
         double sin = Math.sin(radians);
         double cos = Math.cos(radians);
         return new class_243((-sin * forward + cos * side) * speed, 0.0, (cos * forward + sin * side) * speed);
      }
   }

   @Override
   public String getInfoString() {
      return this.phase.get() ? this.speed.get() + " bps, Phase" : this.speed.get() + " bps";
   }
}
