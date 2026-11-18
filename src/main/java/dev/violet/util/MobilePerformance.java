package dev.violet.util;

public final class MobilePerformance {
   private static final boolean ANDROID = false;

   private MobilePerformance() {
   }

   public static boolean isAndroid() {
      return false;
   }

   public static boolean active() {
      return false;
   }

   public static int segments(int original, int minimum) {
      return reduce(original, minimum);
   }

   public static int particles(int original, int minimum) {
      return reduce(original, minimum);
   }

   public static int trailSamples(int original, int minimum) {
      return reduce(original, minimum);
   }

   public static int uiSegments(int original, float radiusPixels) {
      if (active() && original > 12 && Float.isFinite(radiusPixels) && !(radiusPixels <= 0.0F)) {
         double angle = Math.acos(Math.max(-1.0, 1.0 - 0.35 / (double)radiusPixels));
         if (angle <= 0.0) {
            return original;
         } else {
            int needed = (int)Math.ceil(3.141592653589793 / angle);
            return needed >= original ? original : Math.min(original, Math.max(12, (needed + 3) / 4 * 4));
         }
      } else {
         return original;
      }
   }

   private static int reduce(int original, int minimum) {
      return active() && original > 0 ? Math.min(original, Math.max(minimum, original / 2 + original % 2)) : original;
   }
}
