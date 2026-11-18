package dev.violet.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public final class FriendStore {
   private static final FriendStore INSTANCE = new FriendStore();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Pattern VALID_NAME = Pattern.compile("[.*]?[A-Za-z0-9_]{1,16}");
   private final Map<String, String> friends = new LinkedHashMap<>();
   private boolean loaded;

   private FriendStore() {
   }

   public static FriendStore getInstance() {
      return INSTANCE;
   }

   public static boolean isValidName(String name) {
      return name != null && VALID_NAME.matcher(name).matches();
   }

   public synchronized boolean isFriend(String name) {
      this.ensureLoaded();
      return name != null && this.friends.containsKey(name.toLowerCase(Locale.ROOT));
   }

   public synchronized List<String> all() {
      this.ensureLoaded();
      return List.copyOf(this.friends.values());
   }

   public synchronized boolean add(String name) {
      this.ensureLoaded();
      String trimmed = name == null ? "" : name.trim();
      if (isValidName(trimmed) && this.friends.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed) == null) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean remove(String name) {
      this.ensureLoaded();
      if (name != null && this.friends.remove(name.toLowerCase(Locale.ROOT)) != null) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized void replaceAll(Collection<String> names) {
      this.ensureLoaded();
      this.friends.clear();

      for (String name : names) {
         String trimmed = name == null ? "" : name.trim();
         if (isValidName(trimmed)) {
            this.friends.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
         }
      }

      this.save();
   }

   private void ensureLoaded() {
      if (!this.loaded) {
         this.loaded = true;
         this.friends.clear();
         Path path = path();
         if (Files.isRegularFile(path)) {
            try {
               try (Reader reader = Files.newBufferedReader(path)) {
                  JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                  if (root.has("friends") && root.get("friends").isJsonArray()) {
                     for (JsonElement element : root.getAsJsonArray("friends")) {
                        if (element.isJsonPrimitive()) {
                           String name = element.getAsString().trim();
                           if (isValidName(name)) {
                              this.friends.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
                           }
                        }
                     }

                     return;
                  }
               }
            } catch (IOException | RuntimeException var9) {
               this.friends.clear();
            }
         }
      }
   }

   private void save() {
      JsonObject root = new JsonObject();
      root.addProperty("version", 1);
      JsonArray list = new JsonArray();

      for (String name : this.friends.values()) {
         list.add(name);
      }

      root.add("friends", list);
      Path path = path();

      try {
         Files.createDirectories(path.getParent());
         Path tmp = path.resolveSibling(path.getFileName() + ".tmp");

         try (Writer writer = Files.newBufferedWriter(tmp)) {
            GSON.toJson(root, writer);
         }

         Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException var10) {
      }
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("violet").resolve("friends.json");
   }
}
