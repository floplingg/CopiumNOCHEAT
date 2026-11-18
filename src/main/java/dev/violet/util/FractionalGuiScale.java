package dev.violet.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_408;

public final class FractionalGuiScale {
   private static final MethodHandle SCALE_FOR = find();
   private static boolean failed;

   private FractionalGuiScale() {
   }

   private static MethodHandle find() {
      if (!FabricLoader.getInstance().isModLoaded("precise-gui-scale")) {
         return null;
      } else {
         try {
            Class<?> controller = Class.forName("com.scale.preciseguiscale.ScaleController");
            return MethodHandles.publicLookup().findStatic(controller, "scaleFor", MethodType.methodType(double.class, class_310.class));
         } catch (LinkageError | ReflectiveOperationException var1) {
            return null;
         }
      }
   }

   public static double uncorrectedScale(class_310 mc) {
      if (SCALE_FOR == null || failed) {
         return 0.0;
      } else if (mc.field_1755 != null && !(mc.field_1755 instanceof class_408)) {
         return 0.0;
      } else {
         double scale;
         try {
            scale = (double)SCALE_FOR.invokeExact((class_310)mc);
         } catch (Throwable var4) {
            failed = true;
            return 0.0;
         }

         if (!(scale > 0.0)) {
            return 0.0;
         } else {
            scale = Math.max(0.5, Math.min(8.0, scale));
            return Math.abs(scale - (double)mc.method_22683().method_4495()) > 0.001 ? scale : 0.0;
         }
      }
   }
}
