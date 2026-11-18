package dev.violet.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.SoberClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1923;
import net.minecraft.class_310;

public final class OwnAreas {
   private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("soberclient-areas.json");
   private static final Gson GSON = new Gson();
   private static final Map<String, Set<Long>> AREAS = new HashMap<>();
   private static boolean loaded;

   private OwnAreas() {
   }

   public static String currentKey() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null) {
         return null;
      } else {
         String server;
         if (mc.method_1558() != null) {
            server = mc.method_1558().field_3761.toLowerCase();
         } else if (mc.method_1576() != null) {
            server = "singleplayer:" + mc.method_1576().method_27728().method_150();
         } else {
            server = "unknown";
         }

         return server + "|" + mc.field_1687.method_27983().method_29177();
      }
   }

   public static synchronized Set<Long> get(String key) {
      load();
      return AREAS.computeIfAbsent(key, k -> new HashSet<>());
   }

   public static synchronized void add(String key, class_1923 pos) {
      if (get(key).add(pos.method_8324())) {
         save();
      }
   }

   public static synchronized void clear(String key) {
      if (!get(key).isEmpty()) {
         AREAS.get(key).clear();
         save();
      }
   }

   public static synchronized boolean isNear(String key, class_1923 pos, int radius) {
      for (long packed : get(key)) {
         int x = class_1923.method_8325(packed);
         int z = class_1923.method_8332(packed);
         if (Math.abs(x - pos.field_9181) <= radius && Math.abs(z - pos.field_9180) <= radius) {
            return true;
         }
      }

      return false;
   }

   private static void load() {
      if (!loaded) {
         loaded = true;
         if (Files.exists(FILE)) {
            try {
               JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();

               for (Entry<String, JsonElement> entry : root.entrySet()) {
                  Set<Long> set = new HashSet<>();

                  for (JsonElement element : entry.getValue().getAsJsonArray()) {
                     set.add(element.getAsLong());
                  }

                  AREAS.put(entry.getKey(), set);
               }
            } catch (Exception var6) {
               SoberClient.LOG.error("Could not load own areas", var6);
            }
         }
      }
   }

   private static void save() {
      JsonObject root = new JsonObject();

      for (Entry<String, Set<Long>> entry : AREAS.entrySet()) {
         if (!entry.getValue().isEmpty()) {
            JsonArray array = new JsonArray();

            for (long packed : entry.getValue()) {
               array.add(packed);
            }

            root.add(entry.getKey(), array);
         }
      }

      try {
         Files.createDirectories(FILE.getParent());
         Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (Exception var7) {
         SoberClient.LOG.error("Could not save own areas", var7);
      }
   }
}
