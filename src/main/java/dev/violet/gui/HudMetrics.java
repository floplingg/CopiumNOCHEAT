package dev.violet.gui;

import net.minecraft.class_2761;
import net.minecraft.class_310;

public final class HudMetrics {
   private static double lastX;
   private static double lastZ;
   private static boolean hasPosition;
   private static double bps;
   private static long lastPacketNanos;
   private static long lastServerTick;
   private static double tps = 20.0;
   private static boolean hasTps;

   private HudMetrics() {
   }

   public static void tick(class_310 client) {
      if (client.field_1724 != null && client.field_1687 != null) {
         double x = client.field_1724.method_23317();
         double z = client.field_1724.method_23321();
         if (hasPosition) {
            double distance = Math.hypot(x - lastX, z - lastZ);
            if (distance < 8.0) {
               bps = bps + (distance * 20.0 - bps) * 0.25;
            } else {
               bps = 0.0;
            }
         }

         lastX = x;
         lastZ = z;
         hasPosition = true;
      } else {
         hasPosition = false;
         bps = 0.0;
      }
   }

   public static void onWorldTimeUpdate(class_2761 packet) {
      long now = System.nanoTime();
      long ticks = packet.comp_3219();
      if (lastPacketNanos != 0L && ticks > lastServerTick) {
         double elapsed = (double)(now - lastPacketNanos) / 1.0E9;
         if (elapsed > 0.1 && elapsed < 10.0) {
            double sample = Math.max(0.0, Math.min(20.0, (double)(ticks - lastServerTick) / elapsed));
            tps = hasTps ? tps * 0.7 + sample * 0.3 : sample;
            hasTps = true;
         }
      }

      lastPacketNanos = now;
      lastServerTick = ticks;
   }

   public static void reset() {
      hasPosition = false;
      bps = 0.0;
      lastPacketNanos = 0L;
      hasTps = false;
      tps = 20.0;
   }

   public static double bps() {
      return bps;
   }

   public static double tps() {
      return tps;
   }

   public static boolean hasTps() {
      return hasTps && System.nanoTime() - lastPacketNanos < 5000000000L;
   }
}
