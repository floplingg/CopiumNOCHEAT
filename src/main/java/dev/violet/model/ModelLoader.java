package dev.violet.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModelLoader {
   private static final int MAX_TRIANGLES = 300000;

   private ModelLoader() {
   }

   public static ModelMesh load(Path file) throws IOException {
      String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
      if (name.endsWith(".stl")) {
         return stl(Files.readAllBytes(file));
      } else if (name.endsWith(".obj")) {
         return obj(file);
      } else {
         throw new IOException("Unsupported model format: " + name);
      }
   }

   private static ModelMesh stl(byte[] bytes) throws IOException {
      if (bytes.length >= 84) {
         ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
         long count = (long)buf.getInt(80) & 4294967295L;
         if (84L + count * 50L == (long)bytes.length) {
            return binaryStl(buf, (int)Math.min(count, 300000L));
         }
      }

      return asciiStl(new String(bytes, StandardCharsets.US_ASCII));
   }

   private static ModelMesh binaryStl(ByteBuffer buf, int count) {
      boolean materialise = new String(buf.array(), 0, 80, StandardCharsets.US_ASCII).contains("COLOR=");
      float[] raw = new float[count * 9];
      int[] colors = new int[count];

      for (int t = 0; t < count; t++) {
         int base = 84 + t * 50 + 12;

         for (int i = 0; i < 9; i++) {
            raw[t * 9 + i] = buf.getFloat(base + i * 4);
         }

         int attr = buf.getShort(base + 36) & '\uffff';
         colors[t] = -1;
         if (attr != 0) {
            boolean valid = materialise ? (attr & 32768) == 0 : (attr & 32768) != 0;
            if (valid) {
               int r5 = materialise ? attr & 31 : attr >> 10 & 31;
               int b5 = materialise ? attr >> 10 & 31 : attr & 31;
               int g5 = attr >> 5 & 31;
               colors[t] = r5 * 255 / 31 << 16 | g5 * 255 / 31 << 8 | b5 * 255 / 31;
            }
         }
      }

      if (uniform(colors)) {
         Arrays.fill(colors, -1);
      }

      return ModelMesh.normalize(raw, colors, count);
   }

   private static ModelMesh asciiStl(String text) throws IOException {
      List<Float> values = new ArrayList<>();

      for (String line : text.split("\\R")) {
         String trimmed = line.trim();
         if (trimmed.startsWith("vertex")) {
            String[] parts = trimmed.split("\\s+");
            if (parts.length >= 4) {
               for (int i = 1; i <= 3; i++) {
                  values.add(Float.parseFloat(parts[i]));
               }

               if (values.size() >= 2700000) {
                  break;
               }
            }
         }
      }

      int triangles = values.size() / 9;
      if (triangles == 0) {
         throw new IOException("Empty model");
      } else {
         float[] raw = new float[triangles * 9];

         for (int i = 0; i < raw.length; i++) {
            raw[i] = values.get(i);
         }

         int[] colors = new int[triangles];
         Arrays.fill(colors, -1);
         return ModelMesh.normalize(raw, colors, triangles);
      }
   }

   private static boolean uniform(int[] colors) {
      for (int color : colors) {
         if (color != colors[0]) {
            return false;
         }
      }

      return true;
   }

   private static ModelMesh obj(Path file) throws IOException {
      Map<String, Integer> materials = new HashMap<>();
      List<float[]> vertices = new ArrayList<>();
      List<Float> raw = new ArrayList<>();
      List<Integer> colors = new ArrayList<>();
      int current = -1;

      for (String line : Files.readAllLines(file)) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            String[] parts = trimmed.split("\\s+");
            String var10 = parts[0];
            switch (var10) {
               case "mtllib":
                  Path mtl = file.resolveSibling(trimmed.substring(7).trim());
                  if (Files.exists(mtl)) {
                     materials.putAll(materials(mtl));
                  }
                  break;
               case "usemtl":
                  current = materials.getOrDefault(parts.length > 1 ? parts[1] : "", -1);
                  break;
               case "v":
                  vertices.add(new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])});
                  break;
               case "f":
                  int[] idx = new int[parts.length - 1];

                  for (int i = 1; i < parts.length; i++) {
                     int v = Integer.parseInt(parts[i].split("/")[0]);
                     idx[i - 1] = v < 0 ? vertices.size() + v : v - 1;
                  }

                  for (int i = 1; i + 1 < idx.length; i++) {
                     for (int k : new int[]{idx[0], idx[i], idx[i + 1]}) {
                        float[] p = vertices.get(k);
                        raw.add(p[0]);
                        raw.add(p[1]);
                        raw.add(p[2]);
                     }

                     colors.add(current);
                  }
            }

            if (colors.size() >= 300000) {
               break;
            }
         }
      }

      int triangles = colors.size();
      if (triangles == 0) {
         throw new IOException("Empty model");
      } else {
         float[] positions = new float[triangles * 9];

         for (int i = 0; i < positions.length; i++) {
            positions[i] = raw.get(i);
         }

         int[] colorArray = colors.stream().mapToInt(Integer::intValue).toArray();
         return ModelMesh.normalize(positions, colorArray, triangles);
      }
   }

   private static Map<String, Integer> materials(Path mtl) throws IOException {
      Map<String, Integer> result = new HashMap<>();
      String name = null;

      for (String line : Files.readAllLines(mtl)) {
         String[] parts = line.trim().split("\\s+");
         if (parts.length != 0) {
            if (parts[0].equals("newmtl") && parts.length > 1) {
               name = parts[1];
            }

            if (parts[0].equals("Kd") && parts.length > 3 && name != null) {
               int r = Math.round(Float.parseFloat(parts[1]) * 255.0F);
               int g = Math.round(Float.parseFloat(parts[2]) * 255.0F);
               int b = Math.round(Float.parseFloat(parts[3]) * 255.0F);
               result.put(name, Math.max(0, Math.min(255, r)) << 16 | Math.max(0, Math.min(255, g)) << 8 | Math.max(0, Math.min(255, b)));
            }
         }
      }

      return result;
   }
}
