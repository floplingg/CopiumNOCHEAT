package dev.violet.model;

import java.util.Arrays;

public final class WaterIconRaster {
   private static final int SAMPLES = 3;

   private WaterIconRaster() {
   }

   private static float clamp(float v) {
      return v < 0.0F ? 0.0F : Math.min(1.0F, v);
   }

   public static int[] render(WaterMesh mesh, int rgb, int size) {
      int big = size * 3;
      float[] p = mesh.positions;
      float[] n = mesh.normals;
      int vertices = mesh.quads * 4;
      float minX = 3.4028235E38F;
      float minY = 3.4028235E38F;
      float maxX = -3.4028235E38F;
      float maxY = -3.4028235E38F;

      for (int v = 0; v < vertices; v++) {
         minX = Math.min(minX, p[v * 3]);
         maxX = Math.max(maxX, p[v * 3]);
         minY = Math.min(minY, p[v * 3 + 1]);
         maxY = Math.max(maxY, p[v * 3 + 1]);
      }

      float extent = Math.max(maxX - minX, maxY - minY);
      float margin = (float)big * 0.08F;
      float scale = ((float)big - margin * 2.0F) / Math.max(1.0E-6F, extent);
      float offX = margin + ((float)big - margin * 2.0F - (maxX - minX) * scale) / 2.0F;
      float offY = margin + ((float)big - margin * 2.0F - (maxY - minY) * scale) / 2.0F;
      float br = (float)(rgb >> 16 & 0xFF) / 255.0F;
      float bg = (float)(rgb >> 8 & 0xFF) / 255.0F;
      float bb = (float)(rgb & 0xFF) / 255.0F;
      float lx = -0.4F;
      float ly = 0.7F;
      float lz = 0.6F;
      float ll = (float)Math.sqrt((double)(lx * lx + ly * ly + lz * lz));
      lx /= ll;
      ly /= ll;
      lz /= ll;
      float hz = lz + 1.0F;
      float hl = (float)Math.sqrt((double)(lx * lx + ly * ly + hz * hz));
      float hx = lx / hl;
      float hy = ly / hl;
      hz /= hl;
      float[] depth = new float[big * big];
      Arrays.fill(depth, -3.4028235E38F);
      int[] pixels = new int[big * big];
      float[] sx = new float[3];
      float[] sy = new float[3];
      float[] sz = new float[3];
      float[] nx = new float[3];
      float[] ny = new float[3];
      float[] nz = new float[3];
      int[][] tris = new int[][]{{0, 1, 2}, {0, 2, 3}};

      for (int q = 0; q < mesh.quads; q++) {
         for (int[] tri : tris) {
            for (int k = 0; k < 3; k++) {
               int v = q * 4 + tri[k];
               sx[k] = (p[v * 3] - minX) * scale + offX;
               sy[k] = (float)big - ((p[v * 3 + 1] - minY) * scale + offY);
               sz[k] = p[v * 3 + 2];
               nx[k] = n[v * 3];
               ny[k] = n[v * 3 + 1];
               nz[k] = n[v * 3 + 2];
            }

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
                           float ax = w0 * nx[0] + w1 * nx[1] + w2 * nx[2];
                           float ay = w0 * ny[0] + w1 * ny[1] + w2 * ny[2];
                           float az = w0 * nz[0] + w1 * nz[1] + w2 * nz[2];
                           float len = (float)Math.sqrt((double)(ax * ax + ay * ay + az * az));
                           if (!(len < 1.0E-6F)) {
                              ax /= len;
                              ay /= len;
                              az /= len;
                              if (az < 0.0F) {
                                 ax = -ax;
                                 ay = -ay;
                                 az = -az;
                              }

                              pixels[index] = shade(ax, ay, az, br, bg, bb, lx, ly, lz, hx, hy, hz);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      int[] small = new int[size * size];
      float[] mask = new float[size * size];
      int count = 9;

      for (int y = 0; y < size; y++) {
         for (int xx = 0; xx < size; xx++) {
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
            int sum = Math.max(1, a);
            small[y * size + xx] = alpha << 24 | Math.min(255, r * 255 / sum) << 16 | Math.min(255, g * 255 / sum) << 8 | Math.min(255, b * 255 / sum);
            mask[y * size + xx] = (float)alpha / 255.0F;
         }
      }

      float[] glow = blur(blur(mask, size, 2), size, 2);
      float gr = br + (1.0F - br) * 0.55F;
      float gg = bg + (1.0F - bg) * 0.55F;
      float gb = bb + (1.0F - bb) * 0.55F;
      int[] out = new int[size * size];

      for (int y = 0; y < size; y++) {
         for (int xx = 0; xx < size; xx++) {
            int i = y * size + xx;
            int c = small[i];
            float fa = (float)(c >>> 24) / 255.0F;
            float halo = clamp(glow[i] * 1.6F) * 0.75F * (1.0F - fa);
            float outA = fa + halo;
            if (outA <= 0.001F) {
               out[i] = 0;
            } else {
               float r = ((float)(c >> 16 & 0xFF) / 255.0F * fa + gr * halo) / outA;
               float g = ((float)(c >> 8 & 0xFF) / 255.0F * fa + gg * halo) / outA;
               float b = ((float)(c & 0xFF) / 255.0F * fa + gb * halo) / outA;
               out[i] = Math.round(clamp(outA) * 255.0F) << 24
                  | Math.round(clamp(r) * 255.0F) << 16
                  | Math.round(clamp(g) * 255.0F) << 8
                  | Math.round(clamp(b) * 255.0F);
            }
         }
      }

      return out;
   }

   private static int shade(float x, float y, float z, float br, float bg, float bb, float lx, float ly, float lz, float hx, float hy, float hz) {
      float ndv = clamp(z);
      float diffuse = clamp(x * lx + y * ly + z * lz);
      float mix = clamp(diffuse * 0.6F + ndv * 0.45F - 0.08F);
      float r = br * (0.18F + 0.5F * mix);
      float g = bg * (0.18F + 0.5F * mix);
      float b = bb * (0.18F + 0.5F * mix);
      float fres = (float)Math.pow((double)(1.0F - ndv), 3.0);
      r += 0.3F * fres * 0.9F;
      g += 0.65F * fres * 0.9F;
      b += 1.0F * fres * 0.9F;
      float edge = clamp((0.28F - ndv) / 0.28F);
      r += 0.6F * edge * 0.75F;
      g += 0.92F * edge * 0.75F;
      b += 1.0F * edge * 0.75F;
      float spec = (float)Math.pow((double)clamp(x * hx + y * hy + z * hz), 60.0) * 1.4F;
      r += spec;
      g += spec;
      b += spec;
      return 0xFF000000 | Math.round(clamp(r) * 255.0F) << 16 | Math.round(clamp(g) * 255.0F) << 8 | Math.round(clamp(b) * 255.0F);
   }

   private static float[] blur(float[] src, int size, int radius) {
      float[] tmp = new float[src.length];
      float[] out = new float[src.length];

      for (int y = 0; y < size; y++) {
         for (int x = 0; x < size; x++) {
            float sum = 0.0F;

            for (int k = -radius; k <= radius; k++) {
               int xx = Math.max(0, Math.min(size - 1, x + k));
               sum += src[y * size + xx];
            }

            tmp[y * size + x] = sum / (float)(radius * 2 + 1);
         }
      }

      for (int y = 0; y < size; y++) {
         for (int x = 0; x < size; x++) {
            float sum = 0.0F;

            for (int k = -radius; k <= radius; k++) {
               int yy = Math.max(0, Math.min(size - 1, y + k));
               sum += tmp[yy * size + x];
            }

            out[y * size + x] = sum / (float)(radius * 2 + 1);
         }
      }

      return out;
   }
}
