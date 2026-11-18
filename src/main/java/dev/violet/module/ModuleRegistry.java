package dev.violet.module;

import dev.violet.SoberClient;
import java.util.ArrayList;
import java.util.List;

public final class ModuleRegistry {
   private ModuleRegistry() {
   }

   public static <T extends Module> T get(Class<T> type) {
      return SoberClient.MODULES.get(type);
   }

   public static List<BusModule> all() {
      List<BusModule> out = new ArrayList<>();

      for (Module module : SoberClient.MODULES.all()) {
         if (module instanceof BusModule bus) {
            out.add(bus);
         }
      }

      return out;
   }
}
