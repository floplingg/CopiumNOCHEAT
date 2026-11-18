package dev.violet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.gui.HudLayout;
import dev.violet.module.Module;
import dev.violet.modules.AuctionSniper;
import dev.violet.modules.AutoSell;
import dev.violet.modules.AutoShop;
import dev.violet.modules.ClickGui;
import dev.violet.modules.Configs;
import dev.violet.modules.PanicPay;
import dev.violet.modules.RtpMethod;
import dev.violet.setting.Setting;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigSlots {
   public static final int COUNT = 5;
   public static final String FORMAT = "soberclient-config";
   public static final String PREFIX = "SC-";
   private static final String OLD_PREFIX = "YC-";
   private static final List<Class<? extends Module>> SENSITIVE = List.of(AuctionSniper.class, AutoSell.class, AutoShop.class, RtpMethod.class, PanicPay.class);
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("soberclient-configs.json");
   private static final String[] NAMES = new String[5];
   private static final long[] SAVED_AT = new long[5];
   private static final JsonObject[] STATES = new JsonObject[5];
   private static final boolean[] IMPORTED = new boolean[5];
   private static int active = -1;
   private static boolean loaded;

   private ConfigSlots() {
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;

         for (int i = 0; i < 5; i++) {
            NAMES[i] = "Config " + (i + 1);
         }

         if (Files.exists(FILE)) {
            try {
               JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
               active = root.has("active") ? root.get("active").getAsInt() : -1;
               if (root.has("slots")) {
                  JsonArray slots = root.getAsJsonArray("slots");

                  for (int i = 0; i < Math.min(5, slots.size()); i++) {
                     JsonObject slot = slots.get(i).getAsJsonObject();
                     if (slot.has("name")) {
                        NAMES[i] = slot.get("name").getAsString();
                     }

                     SAVED_AT[i] = slot.has("savedAt") ? slot.get("savedAt").getAsLong() : 0L;
                     IMPORTED[i] = slot.has("imported") && slot.get("imported").getAsBoolean();
                     STATES[i] = slot.has("state") && slot.get("state").isJsonObject() ? slot.getAsJsonObject("state") : null;
                  }
               }
            } catch (Exception var4) {
               SoberClient.LOG.error("Could not load configs", var4);
            }
         }
      }
   }

   private static void write() {
      JsonObject root = new JsonObject();
      root.addProperty("active", active);
      JsonArray slots = new JsonArray();

      for (int i = 0; i < 5; i++) {
         JsonObject slot = new JsonObject();
         slot.addProperty("name", NAMES[i]);
         slot.addProperty("savedAt", SAVED_AT[i]);
         slot.addProperty("imported", IMPORTED[i]);
         if (STATES[i] != null) {
            slot.add("state", STATES[i]);
         }

         slots.add(slot);
      }

      root.add("slots", slots);

      try {
         Files.createDirectories(FILE.getParent());
         Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (Exception var4) {
         SoberClient.LOG.error("Could not save configs", var4);
      }
   }

   public static String name(int slot) {
      ensureLoaded();
      return NAMES[slot];
   }

   public static boolean empty(int slot) {
      ensureLoaded();
      return STATES[slot] == null;
   }

   public static int active() {
      ensureLoaded();
      return active;
   }

   public static long savedAt(int slot) {
      ensureLoaded();
      return SAVED_AT[slot];
   }

   public static int firstEmpty() {
      ensureLoaded();

      for (int i = 0; i < 5; i++) {
         if (STATES[i] == null) {
            return i;
         }
      }

      return -1;
   }

   public static int enabledCount(int slot) {
      ensureLoaded();
      JsonObject state = STATES[slot];
      if (state != null && state.has("modules") && state.get("modules").isJsonObject()) {
         int count = 0;

         for (Entry<String, JsonElement> entry : state.getAsJsonObject("modules").entrySet()) {
            if (entry.getValue().isJsonObject() && flag(entry.getValue().getAsJsonObject(), "enabled")) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public static String pack(String json) {
      Deflater deflater = new Deflater(9, true);
      deflater.setInput(json.getBytes(StandardCharsets.UTF_8));
      deflater.finish();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];

      while (!deflater.finished()) {
         out.write(buffer, 0, deflater.deflate(buffer));
      }

      deflater.end();
      return "Y1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
   }

   public static String unpack(String payload) {
      if (!payload.startsWith("Y1:")) {
         throw new IllegalArgumentException("This code does not belong to a SoberClient config.");
      } else {
         try {
            return decode(payload.substring(3));
         } catch (Exception var2) {
            throw new IllegalArgumentException("This shared config is broken.");
         }
      }
   }

   public static void delete(int slot) {
      ensureLoaded();
      STATES[slot] = null;
      SAVED_AT[slot] = 0L;
      IMPORTED[slot] = false;
      NAMES[slot] = "Config " + (slot + 1);
      if (active == slot) {
         active = -1;
      }

      write();
   }

   public static void rename(int slot, String name) {
      ensureLoaded();
      String clean = name == null ? "" : name.strip();
      if (!clean.isEmpty() && !clean.equals(NAMES[slot])) {
         NAMES[slot] = clean.length() > 32 ? clean.substring(0, 32) : clean;
         write();
      }
   }

   private static JsonElement clean(JsonElement data) {
      if (!data.isJsonObject()) {
         return data;
      } else {
         JsonObject copy = data.getAsJsonObject().deepCopy();
         if (copy.has("settings") && copy.get("settings").isJsonObject()) {
            JsonObject settings = copy.getAsJsonObject("settings");
            settings.remove("Saved Options");

            for (String key : List.copyOf(settings.keySet())) {
               if (key.endsWith(" Version")) {
                  settings.remove(key);
               }
            }
         }

         return copy;
      }
   }

   private static boolean sensitive(Module module) {
      for (Class<? extends Module> type : SENSITIVE) {
         if (type.isInstance(module)) {
            return true;
         }
      }

      return false;
   }

   public static void save(int slot) {
      ensureLoaded();
      JsonObject modules = new JsonObject();

      for (Entry<String, JsonElement> entry : SoberClient.CONFIG.toJson().entrySet()) {
         Module module = module(entry.getKey());
         if (module != null && !(module instanceof Configs)) {
            modules.add(module.name + "@" + module.category.name(), clean(entry.getValue()));
         }
      }

      JsonObject state = new JsonObject();
      state.add("modules", modules);
      state.add("hud", HudLayout.toJson());
      STATES[slot] = state;
      SAVED_AT[slot] = System.currentTimeMillis();
      IMPORTED[slot] = false;
      write();
   }

   public static boolean activate(int slot) {
      ensureLoaded();
      JsonObject state = STATES[slot];
      if (state == null) {
         return false;
      } else {
         JsonObject modules = new JsonObject();
         if (state.has("modules")) {
            for (Entry<String, JsonElement> entry : state.getAsJsonObject("modules").entrySet()) {
               String key = entry.getKey();
               int at = key.lastIndexOf(64);
               modules.add(at > 0 ? key.substring(0, at) : key, clean(entry.getValue()));
            }
         }

         boolean imported = IMPORTED[slot];
         SoberClient.CONFIG.apply(modules, module -> module instanceof Configs || imported && sensitive(module), module -> module instanceof ClickGui);
         if (state.has("hud") && state.get("hud").isJsonObject()) {
            HudLayout.fromJson(state.getAsJsonObject("hud"));
         }

         active = slot;
         write();
         SoberClient.CONFIG.save();
         HudLayout.save();
         return true;
      }
   }

   public static boolean imported(int slot) {
      ensureLoaded();
      return IMPORTED[slot];
   }

   public static String export(int slot) {
      ensureLoaded();
      JsonObject state = STATES[slot];
      if (state == null) {
         return null;
      } else {
         JsonObject saved = state.has("modules") && state.get("modules").isJsonObject() ? state.getAsJsonObject("modules") : new JsonObject();
         JsonObject modules = new JsonObject();

         for (Entry<String, JsonElement> entry : saved.entrySet()) {
            String key = entry.getKey();
            int at = key.lastIndexOf(64);
            Module module = module(at > 0 ? key.substring(0, at) : key);
            if (module != null && !(module instanceof Configs) && entry.getValue().isJsonObject()) {
               JsonObject data = clean(entry.getValue()).getAsJsonObject();
               JsonObject out = new JsonObject();
               out.addProperty("enabled", flag(data, "enabled"));
               out.addProperty("keybind", number(data, "key", number(data, "keybind", -1)));
               out.add("settings", (JsonElement)(data.has("settings") && data.get("settings").isJsonObject() ? data.get("settings") : new JsonObject()));
               modules.add(module.name + "@" + module.category.name(), out);
            }
         }

         JsonObject exported = new JsonObject();
         exported.add("modules", modules);
         if (state.has("hud") && state.get("hud").isJsonObject()) {
            exported.add("hud", state.get("hud"));
         }

         JsonObject root = new JsonObject();
         root.addProperty("format", "soberclient-config");
         root.addProperty("version", 1);
         root.addProperty("name", NAMES[slot]);
         root.addProperty("savedAt", SAVED_AT[slot]);
         root.addProperty("client", version());
         root.add("state", exported);
         return GSON.toJson(root);
      }
   }

   private static String version() {
      return FabricLoader.getInstance()
         .getModContainer("soberclient")
         .map(container -> container.getMetadata().getVersion().getFriendlyString())
         .orElse("1.0.0");
   }

   private static boolean flag(JsonObject data, String key) {
      try {
         return data.has(key) && data.get(key).getAsBoolean();
      } catch (Exception var3) {
         return false;
      }
   }

   private static int number(JsonObject data, String key, int fallback) {
      try {
         return data.has(key) ? data.get(key).getAsInt() : fallback;
      } catch (Exception var4) {
         return fallback;
      }
   }

   private static long stamp(JsonObject data, String key) {
      try {
         return data.has(key) ? data.get(key).getAsLong() : 0L;
      } catch (Exception var3) {
         return 0L;
      }
   }

   private static String text(JsonObject data, String key) {
      try {
         return data.has(key) ? data.get(key).getAsString().strip() : "";
      } catch (Exception var3) {
         return "";
      }
   }

   private static String store(int slot, JsonObject byName, JsonElement hud, String name, long savedAt) {
      JsonObject modules = new JsonObject();

      for (Module module : SoberClient.MODULES.all()) {
         if (!(module instanceof Configs)) {
            JsonObject source = byName.has(module.name) && byName.get(module.name).isJsonObject() ? byName.getAsJsonObject(module.name) : new JsonObject();
            JsonObject given = source.has("settings") && source.get("settings").isJsonObject() ? source.getAsJsonObject("settings") : new JsonObject();
            JsonObject settings = new JsonObject();

            for (Setting<?> setting : module.getSettings()) {
               settings.add(setting.name, given.has(setting.name) ? given.get(setting.name) : setting.defaultJson());
            }

            JsonObject data = new JsonObject();
            data.addProperty("enabled", flag(source, "enabled"));
            data.addProperty("key", number(source, "key", number(source, "keybind", -1)));
            data.add("settings", settings);
            modules.add(module.name + "@" + module.category.name(), data);
         }
      }

      JsonObject state = new JsonObject();
      state.add("modules", modules);
      if (hud != null && hud.isJsonObject()) {
         state.add("hud", hud);
      }

      STATES[slot] = state;
      SAVED_AT[slot] = savedAt > 0L ? savedAt : System.currentTimeMillis();
      IMPORTED[slot] = true;
      if (!name.isEmpty()) {
         NAMES[slot] = name.length() > 32 ? name.substring(0, 32) : name;
      }

      if (active == slot) {
         active = -1;
      }

      write();
      return NAMES[slot];
   }

   private static String decode(String code) throws DataFormatException {
      byte[] data = Base64.getUrlDecoder().decode(code);
      Inflater inflater = new Inflater(true);
      inflater.setInput(Arrays.copyOf(data, data.length + 1));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];

      try {
         while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
               break;
            }

            out.write(buffer, 0, count);
            if (out.size() > 4000000) {
               throw new DataFormatException("too large");
            }
         }
      } finally {
         inflater.end();
      }

      return out.toString(StandardCharsets.UTF_8);
   }

   private static String importCode(int slot, String body) {
      JsonObject root;
      try {
         root = JsonParser.parseString(decode(body)).getAsJsonObject();
      } catch (Exception var9) {
         throw new IllegalArgumentException("This SC- code is broken or incomplete.");
      }

      JsonObject coded = root.has("m") && root.get("m").isJsonObject() ? root.getAsJsonObject("m") : new JsonObject();
      JsonObject byName = new JsonObject();

      for (Entry<String, JsonElement> entry : coded.entrySet()) {
         if (entry.getValue().isJsonObject()) {
            JsonObject compact = entry.getValue().getAsJsonObject();
            JsonObject data = new JsonObject();
            data.addProperty("enabled", number(compact, "e", 0) == 1);
            data.addProperty("key", number(compact, "k", -1));
            if (compact.has("s") && compact.get("s").isJsonObject()) {
               data.add("settings", compact.get("s"));
            }

            byName.add(entry.getKey(), data);
         }
      }

      return store(slot, byName, root.get("h"), text(root, "n"), stamp(root, "t"));
   }

   public static String importJson(int slot, String text) {
      ensureLoaded();
      if (text != null && !text.isBlank()) {
         String compact = text.replaceAll("\\s+", "");
         if (!compact.startsWith("SC-") && !compact.startsWith("YC-")) {
            JsonObject root;
            try {
               root = JsonParser.parseString(text.strip()).getAsJsonObject();
            } catch (Exception var10) {
               throw new IllegalArgumentException("This is not a SoberClient config. Copy the whole text from the first { to the last }.");
            }

            if (!"soberclient-config".equals(text(root, "format"))) {
               throw new IllegalArgumentException("This is not a SoberClient config.");
            } else {
               JsonObject state = root.has("state") && root.get("state").isJsonObject() ? root.getAsJsonObject("state") : null;
               if (state != null && state.has("modules") && state.get("modules").isJsonObject()) {
                  JsonObject byName = new JsonObject();

                  for (Entry<String, JsonElement> entry : state.getAsJsonObject("modules").entrySet()) {
                     if (entry.getValue().isJsonObject()) {
                        String key = entry.getKey();
                        int at = key.lastIndexOf(64);
                        byName.add(at > 0 ? key.substring(0, at) : key, entry.getValue());
                     }
                  }

                  return store(slot, byName, state.get("hud"), text(root, "name"), stamp(root, "savedAt"));
               } else {
                  throw new IllegalArgumentException("This config has no settings in it.");
               }
            }
         } else {
            return importCode(slot, compact.substring("SC-".length()));
         }
      } else {
         throw new IllegalArgumentException("Your clipboard is empty.");
      }
   }

   private static Module module(String name) {
      for (Module module : SoberClient.MODULES.all()) {
         if (module.name.equals(name)) {
            return module;
         }
      }

      return null;
   }
}
