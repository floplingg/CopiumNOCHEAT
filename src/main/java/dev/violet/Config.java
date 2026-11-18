package dev.violet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.module.Module;
import dev.violet.module.ModuleManager;
import dev.violet.setting.Setting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public final class Config {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Map<String, String> LEGACY = Map.of("Relog Method", "Y Relog", "SusChunks", "Better Sus Chunk");
   private static final int BACKUPS = 5;
   private final Path file = FabricLoader.getInstance().getConfigDir().resolve("copium.json");
   private final Path backups = FabricLoader.getInstance().getConfigDir().resolve("violet").resolve("backups");
   private final ModuleManager modules;
   private final Map<String, JsonObject> unread = new HashMap<>();
   private boolean readable = true;

   public Config(ModuleManager modules) {
      this.modules = modules;
   }

   public void load() {
      if (Files.exists(this.file)) {
         JsonObject root;
         try {
            root = JsonParser.parseString(Files.readString(this.file, StandardCharsets.UTF_8)).getAsJsonObject();
         } catch (Exception var12) {
            this.readable = false;
            SoberClient.LOG.error("Could not read config, it will not be overwritten", var12);
            return;
         }

         this.backup();

         for (Module module : this.modules.all()) {
            String key = root.has(module.name) ? module.name : LEGACY.get(module.name);

            try {
               if (key != null && root.has(key)) {
                  JsonObject data = root.getAsJsonObject(key);
                  if (data.has("key")) {
                     module.setKey(data.get("key").getAsInt());
                  }

                  if (data.has("settings")) {
                     JsonObject settings = data.getAsJsonObject("settings");

                     for (Setting<?> setting : module.getSettings()) {
                        JsonElement value = settings.get(setting.name);
                        if (value != null) {
                           try {
                              setting.load(value);
                           } catch (Exception var11) {
                           }
                        }
                     }
                  }

                  if (data.has("enabled") && data.get("enabled").getAsBoolean()) {
                     module.setEnabled(true);
                  }
               }
            } catch (Exception var13) {
               if (key != null && root.has(key) && root.get(key).isJsonObject()) {
                  this.unread.put(module.name, root.getAsJsonObject(key));
               }

               SoberClient.LOG.error("Could not load the config of {}, its saved settings are kept", module.name, var13);
            }
         }
      }
   }

   public boolean loadedCleanly(Module module) {
      return !this.unread.containsKey(module.name);
   }

   private void backup() {
      try {
         Files.createDirectories(this.backups);
         String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
         Files.copy(this.file, this.backups.resolve("copium_" + stamp + ".json"), StandardCopyOption.REPLACE_EXISTING);

         try (Stream<Path> stream = Files.list(this.backups)) {
            for (Path path : stream.filter(pathx -> pathx.getFileName().toString().startsWith("copium_"))
               .sorted(Comparator.reverseOrder())
               .skip(5L)
               .toList()) {
               Files.deleteIfExists(path);
            }
         }
      } catch (IOException var8) {
         SoberClient.LOG.warn("Could not back up the config", var8);
      }
   }

   public JsonObject toJson() {
      JsonObject root = new JsonObject();

      for (Module module : this.modules.all()) {
         JsonObject kept = this.unread.get(module.name);
         if (kept != null) {
            root.add(module.name, kept.deepCopy());
         } else {
            JsonObject data = new JsonObject();
            data.addProperty("enabled", module.isActive());
            data.addProperty("key", module.getKey());
            JsonObject settings = new JsonObject();

            for (Setting<?> setting : module.getSettings()) {
               settings.add(setting.name, setting.save());
            }

            data.add("settings", settings);
            root.add(module.name, data);
         }
      }

      return root;
   }

   public void apply(JsonObject root, Predicate<Module> skip, Predicate<Module> keepEnabled) {
      for (Module module : this.modules.all()) {
         if (!skip.test(module)) {
            String key = root.has(module.name) ? module.name : LEGACY.get(module.name);
            if (key != null && root.has(key)) {
               try {
                  JsonObject data = root.getAsJsonObject(key);
                  if (data.has("key")) {
                     module.setKey(data.get("key").getAsInt());
                  }

                  if (data.has("settings")) {
                     JsonObject settings = data.getAsJsonObject("settings");

                     for (Setting<?> setting : module.getSettings()) {
                        JsonElement value = settings.get(setting.name);
                        if (value != null) {
                           try {
                              setting.apply(value);
                           } catch (Exception var13) {
                           }
                        }
                     }
                  }

                  if (!keepEnabled.test(module)) {
                     module.setEnabled(data.has("enabled") && data.get("enabled").getAsBoolean());
                  }

                  this.unread.remove(module.name);
               } catch (Exception var14) {
                  SoberClient.LOG.error("Could not apply config for {}", module.name, var14);
               }
            }
         }
      }
   }

   public void save() {
      if (this.readable) {
         JsonObject root = this.toJson();

         try {
            Files.createDirectories(this.file.getParent());
            Files.writeString(this.file, GSON.toJson(root), StandardCharsets.UTF_8);
         } catch (IOException var3) {
            SoberClient.LOG.error("Could not save config", var3);
         }
      }
   }
}
