package dev.violet.modules;

import dev.violet.event.Stage;
import dev.violet.event.impl.PacketReceiveNettyEvent;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.module.ModuleRegistry;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.ModeSetting;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_1656;
import net.minecraft.class_243;
import net.minecraft.class_2696;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class Fly extends BusModule {
   public static volatile Fly active;
   private static final float VANILLA_FLYING_SPEED = 0.05F;
   private final ModeSetting mode = this.addSetting("fly-mode", new ModeSetting("Mode", "Abilities", "Abilities", "Velocity"));
   private final NumberSetting speed = this.addSetting("fly-speed", new NumberSetting("Speed", 0.1F, 0.0F, 10.0F).step(0.05F));
   private final BooleanSetting verticalSpeedMatch = this.addSetting("vertical-speed-match", new BooleanSetting("Vertical Speed Match", false));
   private final BooleanSetting noSneak = this.addSetting("no-sneak", new BooleanSetting("No Sneak", false));

   public Fly() {
      super("fly", "Fly", "Lets you fly.", ModuleCategory.MOVEMENT);
      this.noSneak.visibleWhen(() -> this.mode.is("Velocity"));
      this.mode.onChange(() -> {
         class_746 player = class_310.method_1551().field_1724;
         if (this.isEnabled() && player != null) {
            abilitiesOff(player);
         }
      });
   }

   @Override
   public void onEnable() {
      class_746 player = class_310.method_1551().field_1724;
      if (player == null) {
         this.requestDisable();
      } else {
         active = this;
         if (this.mode.is("Abilities") && !player.method_7325()) {
            class_1656 abilities = player.method_31549();
            abilities.field_7479 = true;
            if (!abilities.field_7477) {
               abilities.field_7478 = true;
            }
         }

         this.on(TickEvent.TYPE, Stage.POST, this::onPostTick);
         this.on(PacketReceiveNettyEvent.TYPE, this::onPacketReceiveNetty);
      }
   }

   @Override
   public void onDisable() {
      active = null;
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && this.mode.is("Abilities")) {
         abilitiesOff(player);
      }
   }

   private void onPostTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (player != null) {
         if (this.mode.is("Velocity")) {
            if (!player.method_7325()) {
               player.method_31549().field_7479 = false;
            }

            class_243 velocity = class_243.field_1353;
            if (!isFreecamActive()) {
               double vertical = (double)this.speed.get() * (double)(this.verticalSpeedMatch.get() ? 10.0F : 5.0F);
               if (mc.field_1690.field_1903.method_1434()) {
                  velocity = velocity.method_1031(0.0, vertical, 0.0);
               }

               if (mc.field_1690.field_1832.method_1434()) {
                  velocity = velocity.method_1023(0.0, vertical, 0.0);
               }
            }

            player.method_18799(velocity);
            if (this.noSneak.get()) {
               player.method_24830(false);
            }
         } else if (!player.method_7325()) {
            class_1656 abilities = player.method_31549();
            abilities.method_7248(this.speed.get());
            abilities.field_7479 = true;
            if (!abilities.field_7477) {
               abilities.field_7478 = true;
            }
         }
      }
   }

   private void onPacketReceiveNetty(PacketReceiveNettyEvent event) {
      if (event.packet instanceof class_2696 packet && this.mode.is("Abilities")) {
         event.setCancelled(true);
         class_310 mc = class_310.method_1551();
         mc.execute(() -> {
            class_746 player = mc.field_1724;
            if (player != null) {
               class_1656 abilities = player.method_31549();
               abilities.field_7480 = packet.method_11695();
               abilities.field_7477 = packet.method_11696();
               abilities.method_7250(packet.method_11691());
            }
         });
         return;
      }
   }

   private static boolean isFreecamActive() {
      Freecam freecam = ModuleRegistry.get(Freecam.class);
      return freecam != null && freecam.isActive();
   }

   private static void abilitiesOff(class_746 player) {
      if (!player.method_7325()) {
         class_1656 abilities = player.method_31549();
         abilities.field_7479 = false;
         abilities.method_7248(0.05F);
         if (!abilities.field_7477) {
            abilities.field_7478 = false;
         }
      }
   }

   public float getOffGroundSpeed(class_746 player) {
      return this.isEnabled() && this.mode.is("Velocity") ? this.speed.get() * (player.method_5624() ? 15.0F : 10.0F) : -1.0F;
   }

   public boolean noSneak() {
      return this.isEnabled() && this.mode.is("Velocity") && this.noSneak.get();
   }

   public boolean suppressFlightVerticalInput() {
      return this.isEnabled() && isFreecamActive();
   }
}
