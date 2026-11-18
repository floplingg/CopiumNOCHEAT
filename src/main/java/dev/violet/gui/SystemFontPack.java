package dev.violet.gui;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_5352;
import net.minecraft.class_7367;
import net.minecraft.class_7677;
import net.minecraft.class_9224;
import net.minecraft.class_3262.class_7664;

public final class SystemFontPack implements class_3262 {
   private static final String NAMESPACE = "violet";
   private static final Map<String, String> FILES = Map.of("font/consolas-regular.ttf", "consola.ttf", "font/consolas-bold.ttf", "consolab.ttf");
   private static final class_9224 INFO = new class_9224(
      "violet_system_fonts", class_2561.method_43470("SoberClient system fonts"), class_5352.field_25348, Optional.empty()
   );
   private final Map<String, Path> found = new LinkedHashMap<>();

   public SystemFontPack() {
      for (Entry<String, String> entry : FILES.entrySet()) {
         Path path = locate(entry.getValue());
         if (path != null) {
            this.found.put(entry.getKey(), path);
         }
      }
   }

   public boolean isEmpty() {
      return this.found.isEmpty();
   }

   private static Path locate(String file) {
      String[] roots = new String[]{
         System.getenv("WINDIR") == null ? null : System.getenv("WINDIR") + "\\Fonts",
         "C:\\Windows\\Fonts",
         System.getenv("LOCALAPPDATA") == null ? null : System.getenv("LOCALAPPDATA") + "\\Microsoft\\Windows\\Fonts"
      };

      for (String root : roots) {
         if (root != null) {
            try {
               Path path = Path.of(root, file);
               if (Files.isRegularFile(path)) {
                  return path;
               }
            } catch (RuntimeException var7) {
            }
         }
      }

      return null;
   }

   public class_7367<InputStream> method_14410(String... segments) {
      return null;
   }

   public class_7367<InputStream> method_14405(class_3264 type, class_2960 id) {
      if (type == class_3264.field_14188 && "violet".equals(id.method_12836())) {
         Path path = this.found.get(id.method_12832());
         return path == null ? null : class_7367.create(path);
      } else {
         return null;
      }
   }

   public void method_14408(class_3264 type, String namespace, String prefix, class_7664 consumer) {
      if (type == class_3264.field_14188 && "violet".equals(namespace)) {
         String start = prefix.endsWith("/") ? prefix : prefix + "/";

         for (Entry<String, Path> entry : this.found.entrySet()) {
            if (entry.getKey().startsWith(start)) {
               consumer.accept(class_2960.method_60655("violet", entry.getKey()), class_7367.create(entry.getValue()));
            }
         }
      }
   }

   public Set<String> method_14406(class_3264 type) {
      return type == class_3264.field_14188 && !this.found.isEmpty() ? Set.of("violet") : Set.of();
   }

   public <T> T method_14407(class_7677<T> metadataSerializer) {
      return null;
   }

   public class_9224 method_56926() {
      return INFO;
   }

   public void close() {
   }
}
