package dev.violet.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1060;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class ModelIcon {
   public static final int SIZE = 64;
   private static final int SAMPLES = 3;
   private static final class_2960 ID = class_2960.method_60655("violet", "dynamic/model_icon");
   private static int lastKey;
   private static boolean registered;

   private ModelIcon() {
   }

   public static class_2960 get(ModelMesh mesh, Supplier<int[]> colors, boolean flip, int key) {
      if (!registered || key != lastKey) {
         class_1011 image = render(mesh, colors.get(), flip);
         class_1060 manager = class_310.method_1551().method_1531();
         if (registered) {
            manager.method_4615(ID);
         }

         manager.method_4616(ID, new ModelIcon.LinearTexture(image));
         registered = true;
         lastKey = key;
      }

      return ID;
   }

   private static class_1011 render(ModelMesh mesh, int[] colors, boolean flip) {
      int big = 192;
      float[] p = mesh.positions;
      float minX = 3.4028235E38F;
      float minY = 3.4028235E38F;
      float maxX = -3.4028235E38F;
      float maxY = -3.4028235E38F;

      for (int v = 0; v < mesh.triangles * 3; v++) {
         if (!(mesh.along[v] < 0.3F)) {
            float x = flip ? 1.0F - p[v * 3] : p[v * 3];
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, p[v * 3 + 1]);
            maxY = Math.max(maxY, p[v * 3 + 1]);
         }
      }

      float extent = Math.max(maxX - minX, maxY - minY);
      float margin = (float)big * 0.04F;
      float scale = ((float)big - margin * 2.0F) / Math.max(1.0E-6F, extent);
      float offX = margin + ((float)big - margin * 2.0F - (maxX - minX) * scale) / 2.0F;
      float offY = margin + ((float)big - margin * 2.0F - (maxY - minY) * scale) / 2.0F;
      float[] depth = new float[big * big];
      Arrays.fill(depth, -3.4028235E38F);
      int[] pixels = new int[big * big];
      float lx = 0.35F;
      float ly = 0.55F;
      float lz = 0.76F;
      float[] sx = new float[3];
      float[] sy = new float[3];
      float[] sz = new float[3];

      for (int t = 0; t < mesh.triangles; t++) {
         for (int k = 0; k < 3; k++) {
            int vx = t * 3 + k;
            float x = flip ? 1.0F - p[vx * 3] : p[vx * 3];
            sx[k] = (x - minX) * scale + offX;
            sy[k] = (float)big - ((p[vx * 3 + 1] - minY) * scale + offY);
            sz[k] = p[vx * 3 + 2];
         }

         float nx = mesh.normals[t * 3] * (float)(flip ? -1 : 1);
         float ny = mesh.normals[t * 3 + 1];
         float nz = mesh.normals[t * 3 + 2];
         float shade = 0.5F + 0.5F * Math.abs(nx * lx + ny * ly + nz * lz);
         int base = colors[t * 3];
         int r = Math.min(255, Math.round((float)(base >> 16 & 0xFF) * shade));
         int g = Math.min(255, Math.round((float)(base >> 8 & 0xFF) * shade));
         int b = Math.min(255, Math.round((float)(base & 0xFF) * shade));
         int argb = 0xFF000000 | r << 16 | g << 8 | b;
         float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sx[2] - sx[0]) * (sy[1] - sy[0]);
         if (!(Math.abs(area) < 1.0E-6F)) {
            int x0 = Math.max(0, (int)Math.floor((double)Math.min(sx[0], Math.min(sx[1], sx[2]))));
            int x1 = Math.min(big - 1, (int)Math.ceil((double)Math.max(sx[0], Math.max(sx[1], sx[2]))));
            int y0 = Math.max(0, (int)Math.floor((double)Math.min(sy[0], Math.min(sy[1], sy[2]))));
            int y1 = Math.min(big - 1, (int)Math.ceil((double)Math.max(sy[0], Math.max(sy[1], sy[2]))));

            for (int y = y0; y <= y1; y++) {
               for (int x = x0; x <= x1; x++) {
                  float px = (float)x + 0.5F;
                  float py = (float)y + 0.5F;
                  float w0 = ((sx[1] - px) * (sy[2] - py) - (sx[2] - px) * (sy[1] - py)) / area;
                  float w1 = ((sx[2] - px) * (sy[0] - py) - (sx[0] - px) * (sy[2] - py)) / area;
                  float w2 = 1.0F - w0 - w1;
                  if (!(w0 < 0.0F) && !(w1 < 0.0F) && !(w2 < 0.0F)) {
                     float z = w0 * sz[0] + w1 * sz[1] + w2 * sz[2];
                     int index = y * big + x;
                     if (!(z <= depth[index])) {
                        depth[index] = z;
                        pixels[index] = argb;
                     }
                  }
               }
            }
         }
      }

      class_1011 image = new class_1011(64, 64, false);
      int count = 9;

      for (int y = 0; y < 64; y++) {
         for (int xx = 0; xx < 64; xx++) {
            int a = 0;
            int r = 0;
            int g = 0;
            int b = 0;

            for (int dy = 0; dy < 3; dy++) {
               for (int dx = 0; dx < 3; dx++) {
                  int c = pixels[(y * 3 + dy) * big + xx * 3 + dx];
                  int ca = c >>> 24;
                  a += ca;
                  r += (c >> 16 & 0xFF) * ca / 255;
                  g += (c >> 8 & 0xFF) * ca / 255;
                  b += (c & 0xFF) * ca / 255;
               }
            }

            int alpha = a / count;
            if (alpha == 0) {
               image.method_61941(xx, y, 0);
            } else {
               int sum = Math.max(1, a);
               image.method_61941(xx, y, alpha << 24 | Math.min(255, r * 255 / sum) << 16 | Math.min(255, g * 255 / sum) << 8 | Math.min(255, b * 255 / sum));
            }
         }
      }

      return image;
   }

   private static final class LinearTexture extends class_1043 {
      LinearTexture(class_1011 image) {
         super(ModelIcon.ID::toString, image);
      }

      public class_12137 method_75484() {
         return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
      }
   }
}
