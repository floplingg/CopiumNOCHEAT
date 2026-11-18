package dev.violet.util;

import dev.violet.modules.FpsBooster;

public final class PerformanceBudget {
   private static final TickWorkBudget WORK = new TickWorkBudget(System::nanoTime);
   private static long lastTick;

   private PerformanceBudget() {
   }

   public static void beginTick() {
      lastTick = WORK.usedNanos();
      WORK.reset(FpsBooster.scanBudgetNanos());
   }

   public static long acquire(long requestedNanos) {
      return FpsBooster.optimizationsEnabled() ? WORK.acquire(requestedNanos) : System.nanoTime() + Math.max(0L, requestedNanos);
   }

   public static void record(long startedAt) {
      WORK.record(startedAt);
   }

   public static long lastTickNanos() {
      return lastTick;
   }
}
