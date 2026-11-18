package dev.violet.modules;

import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_419;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_639;
import net.minecraft.class_642;

public class AutoReconnect extends Module {
   private static boolean skip;
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Seconds to wait before joining again.", 5, 1, 60));
   private final Setting<Integer> attempts = this.add(new IntSetting("Attempts", "How often it tries in a row before giving up. 0 tries forever.", 5, 0, 50) {
      @Override
      public String display() {
         return this.value == 0 ? "Forever" : String.valueOf(this.value);
      }
   });
   private class_642 server;
   private class_419 waitingOn;
   private long reconnectAt;
   private int tries;

   public AutoReconnect() {
      super("Auto Reconnect", "Joins the server again a few seconds after you were kicked or lost the connection.", Category.DONUT);
      ClientTickEvents.END_CLIENT_TICK.register(this::tickAlways);
   }

   public static void skipNext() {
      skip = true;
   }

   private void tickAlways(class_310 client) {
      if (client.field_1687 != null) {
         class_642 entry = client.method_1558();
         if (entry != null && !client.method_1542()) {
            this.server = entry;
         }

         if (client.field_1724 != null && client.field_1724.field_6012 > 100) {
            this.tries = 0;
         }

         this.waitingOn = null;
      } else if (client.field_1755 instanceof class_419 screen) {
         if (this.isActive() && this.server != null) {
            long now = System.currentTimeMillis();
            if (this.waitingOn != screen) {
               this.waitingOn = screen;
               if (skip) {
                  skip = false;
                  this.reconnectAt = -1L;
               } else if (this.attempts.get() > 0 && this.tries >= this.attempts.get()) {
                  Notifications.push("Auto Reconnect", "Gave up after " + this.tries + " tries.", Notifications.Type.WARNING);
                  this.reconnectAt = -1L;
               } else {
                  this.reconnectAt = now + (long)this.delay.get().intValue() * 1000L;
                  Notifications.push("Auto Reconnect", "Joining " + this.server.field_3761 + " again in " + this.delay.get() + " s.", Notifications.Type.INFO);
               }
            } else if (this.reconnectAt >= 0L && now >= this.reconnectAt) {
               this.reconnectAt = -1L;
               this.tries++;
               class_412.method_36877(new class_500(new class_442()), client, class_639.method_2950(this.server.field_3761), this.server, false, null);
            }
         }
      } else {
         this.waitingOn = null;
      }
   }

   @Override
   public String getInfoString() {
      return this.delay.get() + "s";
   }
}
