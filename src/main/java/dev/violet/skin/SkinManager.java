package dev.violet.skin;

import dev.violet.SoberClient;
import dev.violet.gui.Notifications;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public final class SkinManager {
   public static final String CAT = "builtin/violet_cat.glb";
   public static final String BOT = "builtin/violet_bot.glb";
   public static final String BANANA = "builtin/violet_banana.glb";
   private static final Map<String, String> BUILTIN = builtin();
   private static final Map<String, String> FAILED = new HashMap<>();
   private static volatile List<String> files = List.copyOf(BUILTIN.keySet());
   private static volatile long lastScan;
   private static volatile boolean scanning;
   private static Path folder;
   private static String currentKey;
   private static SkinMesh current;
   private static CompletableFuture<SkinMesh> pending;
   private static String pendingKey;

   private SkinManager() {
   }

   private static Map<String, String> builtin() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("builtin/violet_cat.glb", "Violet Cat");
      map.put("builtin/violet_bot.glb", "Violet Bot");
      map.put("builtin/violet_banana.glb", "Banana Buddy");
      return Collections.unmodifiableMap(map);
   }

   public static Path folder() {
      if (folder != null) {
         return folder;
      } else {
         Path path = FabricLoader.getInstance().getConfigDir().resolve("violet").resolve("skins");

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
      List<String> found = new ArrayList<>(BUILTIN.keySet());

      try (Stream<Path> stream = Files.list(folder())) {
         stream.filter(x$0 -> Files.isRegularFile(x$0))
            .map(p -> p.getFileName().toString())
            .filter(SkinManager::supported)
            .filter(name -> !BUILTIN.containsKey("builtin/" + name))
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .forEach(found::add);
      } catch (IOException var6) {
      }

      files = List.copyOf(found);
   }

   public static void reload() {
      FAILED.clear();
      if (current != null) {
         current.release();
      }

      current = null;
      currentKey = null;
      pending = null;
      pendingKey = null;
      rescan();
   }

   private static boolean supported(String name) {
      String lower = name.toLowerCase(Locale.ROOT);
      return lower.endsWith(".glb") || lower.endsWith(".gltf") || lower.endsWith(".obj") || lower.endsWith(".stl");
   }

   public static String title(String file) {
      if (BUILTIN.containsKey(file)) {
         return BUILTIN.get(file);
      } else {
         int dot = file.lastIndexOf(46);
         String base = dot > 0 ? file.substring(0, dot) : file;
         return base.replace('_', ' ').replace('-', ' ');
      }
   }

   public static String error(String name) {
      return FAILED.get(name);
   }

   public static boolean loading() {
      return pending != null;
   }

   public static SkinMesh get(String name, int budget) {
      if (name != null && !name.isEmpty()) {
         String key = name + "#" + budget;
         if (key.equals(currentKey) && current != null) {
            current.upload();
            return current;
         } else if (FAILED.containsKey(name)) {
            return current;
         } else {
            if (pending != null && pending.isDone()) {
               String doneKey = pendingKey;
               CompletableFuture<SkinMesh> done = pending;
               pending = null;
               pendingKey = null;

               try {
                  SkinMesh mesh = done.join();
                  if (key.equals(doneKey)) {
                     if (current != null) {
                        current.release();
                     }

                     current = mesh;
                     currentKey = key;
                     mesh.upload();
                     Notifications.push("Custom Skin", title(name) + " loaded with " + mesh.triangleCount() + " triangles.", Notifications.Type.SUCCESS);
                     return mesh;
                  }

                  mesh.release();
               } catch (RuntimeException var8) {
                  Throwable cause = (Throwable)(var8.getCause() != null ? var8.getCause() : var8);
                  String skin = doneKey.substring(0, doneKey.lastIndexOf(35));
                  FAILED.put(skin, String.valueOf(cause.getMessage()));
                  SoberClient.LOG.error("Could not load skin {}", skin, cause);
                  Notifications.push("Custom Skin", "Could not load " + skin + ": " + cause.getMessage(), Notifications.Type.ERROR);
                  if (skin.equals(name)) {
                     return null;
                  }
               }
            }

            if (pending == null && BUILTIN.containsKey(name)) {
               pendingKey = key;
               pending = CompletableFuture.supplyAsync(() -> {
                  try {
                     SkinMesh t$;
                     try (InputStream stream = SkinManager.class.getResourceAsStream("/assets/violet/skins/" + name.substring(name.indexOf(47) + 1))) {
                        if (stream == null) {
                           throw new IOException("built in skin is missing");
                        }

                        t$ = SkinLoader.loadGlb(stream.readAllBytes(), budget);
                     }

                     return t$;
                  } catch (IOException var7x) {
                     throw new RuntimeException(var7x.getMessage(), var7x);
                  }
               });
            }

            if (pending == null) {
               Path file = folder().resolve(name);
               if (!Files.isRegularFile(file)) {
                  FAILED.put(name, "file not found");
                  return null;
               }

               pendingKey = key;
               pending = CompletableFuture.supplyAsync(() -> {
                  try {
                     return SkinLoader.load(file, budget);
                  } catch (IOException var3x) {
                     throw new RuntimeException(var3x.getMessage(), var3x);
                  }
               });
            }

            return current != null && currentKey != null && currentKey.startsWith(name + "#") ? current : null;
         }
      } else {
         return null;
      }
   }
}
