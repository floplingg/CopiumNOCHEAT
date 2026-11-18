package dev.violet.util;

import dev.violet.SoberClient;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1937;
import net.minecraft.class_310;

public final class ChunkThreadGuard {
   private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

   private ChunkThreadGuard() {
   }

   public static boolean offThread(class_1937 world) {
      if (world != null && world.method_8608()) {
         class_310 mc = class_310.method_1551();
         return mc != null && !mc.method_18854();
      } else {
         return false;
      }
   }

   public static void defer(String what, Runnable action) {
      String thread = Thread.currentThread().getName();
      if (REPORTED.add(thread + "|" + what)) {
         SoberClient.LOG
            .warn(
               "[SoberClient] Thread \"{}\" called {} off the main thread. It now runs on the main thread so the game does not crash.",
               new Object[]{thread, what, new Throwable("caller")}
            );
      }

      class_310.method_1551().execute(action);
   }
}
