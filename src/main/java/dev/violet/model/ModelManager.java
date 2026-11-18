package dev.violet.model;

import dev.violet.SoberClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public final class ModelManager {
   private static final Map<String, ModelMesh> CACHE = new HashMap<>();
   private static final Map<String, String> FAILED = new HashMap<>();
   private static final Map<String, SpriteModel> SPRITES = new HashMap<>();
   private static final Map<String, CompletableFuture<SpriteModel.Prepared>> PENDING = new HashMap<>();
   private static final ExecutorService LOADER = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Violet Sprite Loader");
      thread.setDaemon(true);
      thread.setPriority(1);
      return thread;
   });
   public static final String WATERY = "Watery/";
   private static final Map<String, String> BUILTIN_PACKS = Map.of("Fortnite Pack/", "/assets/violet/models/fortnite_pack/");
   private static final Map<String, Set<String>> BUILTIN_ITEMS = builtinItems();
   private static volatile List<String> files = initial();
   private static volatile Map<String, Set<String>> packs = Map.of();
   private static volatile long lastScan;
   private static volatile boolean scanning;
   private static Path folder;

   private ModelManager() {
   }

   private static List<String> initial() {
      List<String> list = new ArrayList<>(ModelLibrary.names());
      list.addAll(BUILTIN_PACKS.keySet());
      list.add("Watery/");
      return List.copyOf(list);
   }

   private static Map<String, Set<String>> builtinItems() {
      Map<String, Set<String>> result = new HashMap<>();

      for (Entry<String, String> pack : BUILTIN_PACKS.entrySet()) {
         Set<String> items = new HashSet<>();

         try (InputStream in = ModelManager.class.getResourceAsStream(pack.getValue() + "items.txt")) {
            if (in != null) {
               for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                  if (!line.isBlank()) {
                     items.add(line.trim().toLowerCase(Locale.ROOT));
                  }
               }
            }
         } catch (IOException var11) {
         }

         result.put(pack.getKey(), Set.copyOf(items));
      }

      return Map.copyOf(result);
   }

   public static Path folder() {
      if (folder != null) {
         return folder;
      } else {
         Path path = FabricLoader.getInstance().getConfigDir().resolve("violet").resolve("models");

         try {
            Files.createDirectories(path);
         } catch (IOException var2) {
         }

         folder = path;
         return path;
      }
   }

   public static List<String> files() {
      long now = System.currentTimeMillis();
      if (now - lastScan > 5000L && !scanning) {
         lastScan = now;
         scanning = true;
         CompletableFuture.runAsync(() -> {
            try {
               rescan();
            } finally {
               scanning = false;
            }
         });
      }

      return files;
   }

   public static void rescan() {
      lastScan = System.currentTimeMillis();
      List<String> found = new ArrayList<>(initial());
      List<String> local = new ArrayList<>();
      Map<String, Set<String>> foundPacks = new HashMap<>();

      try (Stream<Path> stream = Files.list(folder())) {
         for (Path path : (Iterable<Path>)stream::iterator) {
            String name = path.getFileName().toString();
            if (Files.isDirectory(path)) {
               if (!BUILTIN_PACKS.containsKey(name + "/") && !"Watery/".equals(name + "/")) {
                  Set<String> items = packItems(path);
                  if (!items.isEmpty()) {
                     foundPacks.put(name + "/", items);
                     local.add(name + "/");
                  }
               }
            } else if (Files.isRegularFile(path) && supported(name)) {
               local.add(name);
            }
         }
      } catch (IOException var10) {
      }

      local.sort(String.CASE_INSENSITIVE_ORDER);
      found.addAll(local);
      packs = Map.copyOf(foundPacks);
      files = List.copyOf(found);
   }

   private static Set<String> packItems(Path directory) {
      Set<String> items = new HashSet<>();

      try (Stream<Path> stream = Files.list(directory)) {
         stream.filter(x$0 -> Files.isRegularFile(x$0))
            .map(p -> p.getFileName().toString())
            .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".png"))
            .forEach(n -> items.add(n.substring(0, n.length() - 4).toLowerCase(Locale.ROOT)));
      } catch (IOException var7) {
      }

      return items;
   }

   public static void reload() {
      CACHE.clear();
      FAILED.clear();

      for (SpriteModel sprite : SPRITES.values()) {
         if (sprite != null) {
            sprite.release();
         }
      }

      SPRITES.clear();
      PENDING.clear();
      rescan();
   }

   private static boolean supported(String name) {
      String lower = name.toLowerCase(Locale.ROOT);
      return lower.endsWith(".stl") || lower.endsWith(".obj") || lower.endsWith(".png");
   }

   public static boolean isPack(String name) {
      return name != null && name.endsWith("/");
   }

   public static boolean isSprite(String name) {
      return name != null && name.toLowerCase(Locale.ROOT).endsWith(".png");
   }

   public static boolean isWatery(String name) {
      return "Watery/".equals(name);
   }

   public static boolean packHas(String pack, String item) {
      if ("Watery/".equals(pack)) {
         return WaterModels.kind(item) != null;
      } else {
         Set<String> builtin = BUILTIN_ITEMS.get(pack);
         if (builtin != null) {
            return builtin.contains(item);
         } else {
            Set<String> items = packs.get(pack);
            return items != null && items.contains(item);
         }
      }
   }

   public static SpriteModel sprite(String name) {
      return loadSprite(name, folder().resolve(name), null);
   }

   public static SpriteModel packSprite(String pack, String item) {
      if (!"Watery/".equals(pack) && packHas(pack, item)) {
         String resource = BUILTIN_PACKS.get(pack);
         return resource != null
            ? loadSprite(pack + item, null, resource + item + ".png")
            : loadSprite(pack + item, folder().resolve(pack.substring(0, pack.length() - 1)).resolve(item + ".png"), null);
      } else {
         return null;
      }
   }

   private static SpriteModel loadSprite(String key, Path file, String resource) {
      if (FAILED.containsKey(key)) {
         return null;
      } else {
         SpriteModel sprite = SPRITES.get(key);
         if (sprite != null) {
            return sprite;
         } else {
            CompletableFuture<SpriteModel.Prepared> pending = PENDING.get(key);
            if (pending == null) {
               PENDING.put(key, CompletableFuture.supplyAsync(() -> prepare(file, resource), LOADER));
               return null;
            } else if (!pending.isDone()) {
               return null;
            } else {
               PENDING.remove(key);

               try {
                  sprite = SpriteModel.upload(pending.join());
                  SPRITES.put(key, sprite);
                  return sprite;
               } catch (RuntimeException var7) {
                  Throwable cause = (Throwable)(var7 instanceof CompletionException && var7.getCause() != null ? var7.getCause() : var7);
                  FAILED.put(key, String.valueOf(cause.getMessage()));
                  SoberClient.LOG.error("Could not load sprite {}", key, cause);
                  return null;
               }
            }
         }
      }
   }

   private static SpriteModel.Prepared prepare(Path file, String resource) {
      try {
         if (resource == null) {
            return SpriteModel.prepare(file);
         } else {
            SpriteModel.Prepared var3;
            try (InputStream in = ModelManager.class.getResourceAsStream(resource)) {
               if (in == null) {
                  throw new IOException("missing " + resource);
               }

               var3 = SpriteModel.prepare(in);
            }

            return var3;
         }
      } catch (IOException var7) {
         throw new UncheckedIOException(var7);
      }
   }

   public static ModelMesh get(String name) {
      if (name != null && !name.isEmpty() && !isPack(name) && !isSprite(name) && !FAILED.containsKey(name)) {
         ModelMesh mesh = CACHE.get(name);
         if (mesh != null) {
            return mesh;
         } else if (ModelLibrary.has(name)) {
            try {
               mesh = ModelLibrary.build(name);
               CACHE.put(name, mesh);
               return mesh;
            } catch (RuntimeException var4) {
               FAILED.put(name, String.valueOf(var4.getMessage()));
               SoberClient.LOG.error("Could not build model {}", name, var4);
               return null;
            }
         } else {
            Path file = folder().resolve(name);
            if (!Files.isRegularFile(file)) {
               FAILED.put(name, "file not found");
               return null;
            } else {
               try {
                  mesh = ModelLoader.load(file);
                  CACHE.put(name, mesh);
                  return mesh;
               } catch (RuntimeException | IOException var5) {
                  FAILED.put(name, String.valueOf(var5.getMessage()));
                  SoberClient.LOG.error("Could not load model {}", name, var5);
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public static String error(String name) {
      return FAILED.get(name);
   }

   public static String title(String file) {
      if (ModelLibrary.has(file)) {
         return ModelLibrary.title(file);
      } else if (isPack(file)) {
         return file.substring(0, file.length() - 1);
      } else {
         int dot = file.lastIndexOf(46);
         String base = dot > 0 ? file.substring(0, dot) : file;
         return base.replace('_', ' ').replace('-', ' ');
      }
   }
}
