package dev.violet.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class SpriteModel {
   private static final int SIZE = 512;
   private static final int GRID = 256;
   private static final int ICON_SIZE = 64;
   private static final float THICKNESS = 0.041666668F;
   private static final float CUTOFF = 0.1F;
   private static final float SOFTNESS = 2.2F;
   private static final float SIMPLIFY = 0.3F;
   private static final int SIDE = -3552046;
   private static int counter;
   public final class_2960 texture;
   public final class_2960 icon;
   public final int quads;
   public final float[] positions;
   public final float[] uvs;
   public final float[] normals;
   public final int[] colors;

   private SpriteModel(class_2960 texture, class_2960 icon, SpriteModel.Geometry geometry) {
      this.texture = texture;
      this.icon = icon;
      this.quads = geometry.count;
      this.positions = Arrays.copyOf(geometry.p, geometry.count * 12);
      this.uvs = Arrays.copyOf(geometry.t, geometry.count * 8);
      this.normals = Arrays.copyOf(geometry.n, geometry.count * 12);
      this.colors = Arrays.copyOf(geometry.c, geometry.count);
   }

   static SpriteModel.Prepared prepare(Path file) throws IOException {
      SpriteModel.Prepared var2;
      try (InputStream in = Files.newInputStream(file)) {
         var2 = prepare(in);
      }

      return var2;
   }

   static SpriteModel.Prepared prepare(InputStream in) throws IOException {
      class_1011 source = class_1011.method_4309(in);
      int sw = source.method_4307();
      int sh = source.method_4323();
      int[] pixels = new int[sw * sh];

      for (int y = 0; y < sh; y++) {
         for (int x = 0; x < sw; x++) {
            pixels[y * sw + x] = source.method_61940(x, y);
         }
      }

      source.close();
      bleed(pixels, sw, sh);
      float aspect = (float)sw / (float)sh;
      int w = aspect >= 1.0F ? 512 : Math.max(1, Math.round(512.0F * aspect));
      int h = aspect >= 1.0F ? Math.max(1, Math.round(512.0F / aspect)) : 512;
      int[] main = sw <= w && sh <= h ? upscale(pixels, sw, sh, w, h) : downscale(pixels, sw, sh, w, h);
      bleed(main, w, h);
      float[] alpha = new float[w * h];

      for (int i = 0; i < alpha.length; i++) {
         alpha[i] = (float)(main[i] >>> 24) / 255.0F;
      }

      alpha = soften(alpha, w, h, 2.2F * (float)Math.max(w, h) / 256.0F);

      for (int i = 0; i < alpha.length; i++) {
         main[i] = Math.round(alpha[i] * 255.0F) << 24 | main[i] & 16777215;
      }

      int gw = Math.max(1, w * 256 / 512);
      int gh = Math.max(1, h * 256 / 512);
      float[] grid = shrink(alpha, w, h, gw, gh);
      SpriteModel.Geometry geometry = new SpriteModel.Geometry(Math.max(64, gw * gh / 6));
      float front = 0.5208333F;
      float back = 0.47916666F;
      geometry.quad(
         0.0F,
         1.0F,
         front,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         front,
         0.0F,
         1.0F,
         1.0F,
         0.0F,
         front,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         front,
         1.0F,
         0.0F,
         0.0F,
         0.0F,
         1.0F,
         0.0F,
         0.0F,
         1.0F,
         -1
      );
      geometry.quad(
         1.0F,
         1.0F,
         back,
         1.0F,
         0.0F,
         1.0F,
         0.0F,
         back,
         1.0F,
         1.0F,
         0.0F,
         0.0F,
         back,
         0.0F,
         1.0F,
         0.0F,
         1.0F,
         back,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         -1.0F,
         0.0F,
         0.0F,
         -1.0F,
         -1
      );
      contour(geometry, grid, gw, gh, front, back);
      int[] iconPixels = downscale(main, w, h, 64, 64);
      bleed(iconPixels, 64, 64);
      return new SpriteModel.Prepared(main, w, h, iconPixels, geometry);
   }

   static SpriteModel upload(SpriteModel.Prepared prepared) {
      class_310 mc = class_310.method_1551();
      int id = counter++;
      class_2960 texture = class_2960.method_60655("violet", "sprite/model_" + id);
      class_2960 icon = class_2960.method_60655("violet", "sprite/icon_" + id);
      mc.method_1531().method_4616(texture, new SmoothTexture(() -> "violet sprite " + texture, image(prepared.main, prepared.width, prepared.height)));
      mc.method_1531().method_4616(icon, new SmoothTexture(() -> "violet sprite icon " + icon, image(prepared.icon, 64, 64)));
      return new SpriteModel(texture, icon, prepared.geometry);
   }

   private static class_1011 image(int[] pixels, int w, int h) {
      class_1011 image = new class_1011(w, h, false);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            image.method_61941(x, y, pixels[y * w + x]);
         }
      }

      return image;
   }

   private static int channel(int c, int shift) {
      return c >> shift & 0xFF;
   }

   private static int[] upscale(int[] source, int sw, int sh, int w, int h) {
      int[] out = new int[w * h];

      for (int y = 0; y < h; y++) {
         float fy = ((float)y + 0.5F) * (float)sh / (float)h - 0.5F;
         int y0 = Math.max(0, Math.min(sh - 1, (int)Math.floor((double)fy)));
         int y1 = Math.min(sh - 1, y0 + 1);
         float ty = Math.max(0.0F, Math.min(1.0F, fy - (float)y0));

         for (int x = 0; x < w; x++) {
            float fx = ((float)x + 0.5F) * (float)sw / (float)w - 0.5F;
            int x0 = Math.max(0, Math.min(sw - 1, (int)Math.floor((double)fx)));
            int x1 = Math.min(sw - 1, x0 + 1);
            float tx = Math.max(0.0F, Math.min(1.0F, fx - (float)x0));
            int a = source[y0 * sw + x0];
            int b = source[y0 * sw + x1];
            int c = source[y1 * sw + x0];
            int d = source[y1 * sw + x1];
            int value = 0;

            for (int shift = 0; shift <= 24; shift += 8) {
               float top = (float)channel(a, shift) * (1.0F - tx) + (float)channel(b, shift) * tx;
               float bottom = (float)channel(c, shift) * (1.0F - tx) + (float)channel(d, shift) * tx;
               value |= Math.max(0, Math.min(255, Math.round(top * (1.0F - ty) + bottom * ty))) << shift;
            }

            out[y * w + x] = value;
         }
      }

      return out;
   }

   private static int[] downscale(int[] source, int sw, int sh, int w, int h) {
      int[] out = new int[w * h];

      for (int y = 0; y < h; y++) {
         int y0 = y * sh / h;
         int y1 = Math.max(y0 + 1, (y + 1) * sh / h);

         for (int x = 0; x < w; x++) {
            int x0 = x * sw / w;
            int x1 = Math.max(x0 + 1, (x + 1) * sw / w);
            long a = 0L;
            long r = 0L;
            long g = 0L;
            long b = 0L;
            int count = 0;

            for (int yy = y0; yy < y1; yy++) {
               for (int xx = x0; xx < x1; xx++) {
                  int c = source[yy * sw + xx];
                  int alpha = c >>> 24;
                  a += (long)alpha;
                  r += (long)channel(c, 16) * (long)alpha;
                  g += (long)channel(c, 8) * (long)alpha;
                  b += (long)channel(c, 0) * (long)alpha;
                  count++;
               }
            }

            int alpha = (int)(a / (long)Math.max(1, count));
            int red = a == 0L ? 0 : (int)(r / a);
            int green = a == 0L ? 0 : (int)(g / a);
            int blue = a == 0L ? 0 : (int)(b / a);
            out[y * w + x] = alpha << 24 | red << 16 | green << 8 | blue;
         }
      }

      return out;
   }

   private static void bleed(int[] pixels, int w, int h) {
      boolean[] known = new boolean[w * h];

      for (int i = 0; i < pixels.length; i++) {
         known[i] = pixels[i] >>> 24 > 8;
      }

      for (int pass = 0; pass < 12; pass++) {
         boolean[] next = (boolean[])known.clone();
         boolean changed = false;

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               int i = y * w + x;
               if (!known[i]) {
                  int r = 0;
                  int g = 0;
                  int b = 0;
                  int n = 0;

                  for (int dy = -1; dy <= 1; dy++) {
                     for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && ny >= 0 && nx < w && ny < h && known[ny * w + nx]) {
                           int c = pixels[ny * w + nx];
                           r += channel(c, 16);
                           g += channel(c, 8);
                           b += channel(c, 0);
                           n++;
                        }
                     }
                  }

                  if (n != 0) {
                     pixels[i] = pixels[i] & 0xFF000000 | r / n << 16 | g / n << 8 | b / n;
                     next[i] = true;
                     changed = true;
                  }
               }
            }
         }

         known = next;
         if (!changed) {
            break;
         }
      }
   }

   private static float[] soften(float[] alpha, int w, int h, float sigma) {
      int radius = Math.max(1, (int)Math.ceil((double)(sigma * 2.5F)));
      float[] kernel = new float[radius * 2 + 1];
      float sum = 0.0F;

      for (int i = -radius; i <= radius; i++) {
         kernel[i + radius] = (float)Math.exp((double)((float)(-(i * i)) / (2.0F * sigma * sigma)));
         sum += kernel[i + radius];
      }

      for (int i = 0; i < kernel.length; i++) {
         kernel[i] /= sum;
      }

      float[] temp = new float[w * h];
      float[] out = new float[w * h];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            float value = 0.0F;

            for (int k = -radius; k <= radius; k++) {
               int xx = x + k;
               if (xx >= 0 && xx < w) {
                  value += alpha[y * w + xx] * kernel[k + radius];
               }
            }

            temp[y * w + x] = value;
         }
      }

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            float value = 0.0F;

            for (int kx = -radius; kx <= radius; kx++) {
               int yy = y + kx;
               if (yy >= 0 && yy < h) {
                  value += temp[yy * w + x] * kernel[kx + radius];
               }
            }

            float t = Math.max(0.0F, Math.min(1.0F, (value - 0.38F) / 0.24F));
            out[y * w + x] = t * t * (3.0F - 2.0F * t);
         }
      }

      return out;
   }

   private static float[] shrink(float[] source, int sw, int sh, int w, int h) {
      if (sw == w && sh == h) {
         return source;
      } else {
         float[] out = new float[w * h];

         for (int y = 0; y < h; y++) {
            int y0 = y * sh / h;
            int y1 = Math.max(y0 + 1, (y + 1) * sh / h);

            for (int x = 0; x < w; x++) {
               int x0 = x * sw / w;
               int x1 = Math.max(x0 + 1, (x + 1) * sw / w);
               float sum = 0.0F;

               for (int yy = y0; yy < y1; yy++) {
                  for (int xx = x0; xx < x1; xx++) {
                     sum += source[yy * sw + xx];
                  }
               }

               out[y * w + x] = sum / (float)((y1 - y0) * (x1 - x0));
            }
         }

         return out;
      }
   }

   private static float sample(float[] alpha, int w, int h, int x, int y) {
      return x >= 0 && y >= 0 && x < w && y < h ? alpha[y * w + x] : 0.0F;
   }

   private static float sampleBilinear(float[] alpha, int w, int h, float x, float y) {
      float fx = x - 0.5F;
      float fy = y - 0.5F;
      int x0 = (int)Math.floor((double)fx);
      int y0 = (int)Math.floor((double)fy);
      float tx = fx - (float)x0;
      float ty = fy - (float)y0;
      float a = sample(alpha, w, h, x0, y0);
      float b = sample(alpha, w, h, x0 + 1, y0);
      float c = sample(alpha, w, h, x0, y0 + 1);
      float d = sample(alpha, w, h, x0 + 1, y0 + 1);
      return (a * (1.0F - tx) + b * tx) * (1.0F - ty) + (c * (1.0F - tx) + d * tx) * ty;
   }

   private static void contour(SpriteModel.Geometry geometry, float[] alpha, int w, int h, float front, float back) {
      SpriteModel.Segments segments = new SpriteModel.Segments();

      for (int cy = -1; cy < h; cy++) {
         for (int cx = -1; cx < w; cx++) {
            float tl = sample(alpha, w, h, cx, cy);
            float tr = sample(alpha, w, h, cx + 1, cy);
            float br = sample(alpha, w, h, cx + 1, cy + 1);
            float bl = sample(alpha, w, h, cx, cy + 1);
            int index = (tl >= 0.1F ? 8 : 0) | (tr >= 0.1F ? 4 : 0) | (br >= 0.1F ? 2 : 0) | (bl >= 0.1F ? 1 : 0);
            if (index != 0 && index != 15) {
               float px = (float)cx + 0.5F;
               float py = (float)cy + 0.5F;
               float topX = px + cross(tl, tr);
               float rightX = px + 1.0F;
               float rightY = py + cross(tr, br);
               float bottomX = px + cross(bl, br);
               float bottomY = py + 1.0F;
               float leftY = py + cross(tl, bl);
               boolean center = (tl + tr + br + bl) / 4.0F >= 0.1F;
               switch (index) {
                  case 1:
                  case 14:
                     segments.add(px, leftY, bottomX, bottomY);
                     break;
                  case 2:
                  case 13:
                     segments.add(bottomX, bottomY, rightX, rightY);
                     break;
                  case 3:
                  case 12:
                     segments.add(px, leftY, rightX, rightY);
                     break;
                  case 4:
                  case 11:
                     segments.add(topX, py, rightX, rightY);
                     break;
                  case 5:
                     if (center) {
                        segments.add(px, leftY, topX, py);
                        segments.add(bottomX, bottomY, rightX, rightY);
                     } else {
                        segments.add(px, leftY, bottomX, bottomY);
                        segments.add(topX, py, rightX, rightY);
                     }
                     break;
                  case 6:
                  case 9:
                     segments.add(topX, py, bottomX, bottomY);
                     break;
                  case 7:
                  case 8:
                     segments.add(px, leftY, topX, py);
                     break;
                  case 10:
                     if (center) {
                        segments.add(topX, py, rightX, rightY);
                        segments.add(px, leftY, bottomX, bottomY);
                     } else {
                        segments.add(px, leftY, topX, py);
                        segments.add(bottomX, bottomY, rightX, rightY);
                     }
               }
            }
         }
      }

      for (float[] chain : segments.chains()) {
         float[] points = simplify(chain, 0.3F);

         for (int i = 0; i + 3 < points.length; i += 2) {
            wall(geometry, alpha, w, h, points[i], points[i + 1], points[i + 2], points[i + 3], front, back);
         }
      }
   }

   private static float cross(float a, float b) {
      return Math.abs(b - a) < 1.0E-6F ? 0.5F : Math.max(0.0F, Math.min(1.0F, (0.1F - a) / (b - a)));
   }

   private static void wall(SpriteModel.Geometry geometry, float[] alpha, int w, int h, float x1, float y1, float x2, float y2, float front, float back) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float length = (float)Math.sqrt((double)(dx * dx + dy * dy));
      if (!(length < 1.0E-5F)) {
         float nx = dy / length;
         float ny = -dx / length;
         float mx = (x1 + x2) / 2.0F;
         float my = (y1 + y2) / 2.0F;
         if (sampleBilinear(alpha, w, h, mx + nx * 0.6F, my + ny * 0.6F) >= 0.1F) {
            nx = -nx;
            ny = -ny;
         }

         float inset = 1.5F;
         float u1 = Math.max(0.5F, Math.min((float)w - 0.5F, x1 - nx * inset)) / (float)w;
         float v1 = Math.max(0.5F, Math.min((float)h - 0.5F, y1 - ny * inset)) / (float)h;
         float u2 = Math.max(0.5F, Math.min((float)w - 0.5F, x2 - nx * inset)) / (float)w;
         float v2 = Math.max(0.5F, Math.min((float)h - 0.5F, y2 - ny * inset)) / (float)h;
         float ax = x1 / (float)w;
         float ay = 1.0F - y1 / (float)h;
         float bx = x2 / (float)w;
         float by = 1.0F - y2 / (float)h;
         float sx = nx * 0.8F;
         float sy = -ny * 0.8F;
         float sz = 0.6F;
         geometry.quad(ax, ay, back, u1, v1, ax, ay, front, u1, v1, bx, by, front, u2, v2, bx, by, back, u2, v2, sx, sy, -sz, sx, sy, sz, -3552046);
      }
   }

   private static float[] simplify(float[] points, float epsilon) {
      int n = points.length / 2;
      if (n <= 2) {
         return points;
      } else {
         boolean[] keep = new boolean[n];
         keep[0] = true;
         keep[n - 1] = true;
         ArrayDeque<int[]> stack = new ArrayDeque<>();
         boolean closed = points[0] == points[(n - 1) * 2] && points[1] == points[(n - 1) * 2 + 1];
         if (closed && n > 3) {
            int far = 1;
            float best = -1.0F;

            for (int i = 1; i < n - 1; i++) {
               float dx = points[i * 2] - points[0];
               float dy = points[i * 2 + 1] - points[1];
               float d = dx * dx + dy * dy;
               if (d > best) {
                  best = d;
                  far = i;
               }
            }

            keep[far] = true;
            stack.push(new int[]{0, far});
            stack.push(new int[]{far, n - 1});
         } else {
            stack.push(new int[]{0, n - 1});
         }

         while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int a = range[0];
            int b = range[1];
            if (b - a >= 2) {
               float ax = points[a * 2];
               float ay = points[a * 2 + 1];
               float bx = points[b * 2];
               float by = points[b * 2 + 1];
               float dx = bx - ax;
               float dy = by - ay;
               float length = dx * dx + dy * dy;
               int index = -1;
               float max = epsilon * epsilon;

               for (int ix = a + 1; ix < b; ix++) {
                  float px = points[ix * 2] - ax;
                  float py = points[ix * 2 + 1] - ay;
                  float t = length > 1.0E-12F ? Math.max(0.0F, Math.min(1.0F, (px * dx + py * dy) / length)) : 0.0F;
                  float ex = px - dx * t;
                  float ey = py - dy * t;
                  float d = ex * ex + ey * ey;
                  if (d > max) {
                     max = d;
                     index = ix;
                  }
               }

               if (index >= 0) {
                  keep[index] = true;
                  stack.push(new int[]{a, index});
                  stack.push(new int[]{index, b});
               }
            }
         }

         int kept = 0;

         for (boolean k : keep) {
            if (k) {
               kept++;
            }
         }

         float[] out = new float[kept * 2];
         int ixx = 0;

         for (int o = 0; ixx < n; ixx++) {
            if (keep[ixx]) {
               out[o++] = points[ixx * 2];
               out[o++] = points[ixx * 2 + 1];
            }
         }

         return out;
      }
   }

   void release() {
      class_310 mc = class_310.method_1551();
      mc.method_1531().method_4615(this.texture);
      mc.method_1531().method_4615(this.icon);
   }

   private static final class FloatBuffer {
      private float[] values = new float[64];
      private int size;

      void add(float x, float y) {
         if (this.size * 2 + 2 > this.values.length) {
            this.values = Arrays.copyOf(this.values, this.values.length * 2);
         }

         this.values[this.size * 2] = x;
         this.values[this.size * 2 + 1] = y;
         this.size++;
      }
   }

   private static final class Geometry {
      float[] p;
      float[] t;
      float[] n;
      int[] c;
      int count;

      Geometry(int capacity) {
         this.p = new float[capacity * 12];
         this.t = new float[capacity * 8];
         this.n = new float[capacity * 12];
         this.c = new int[capacity];
      }

      void quad(
         float ax,
         float ay,
         float az,
         float au,
         float av,
         float bx,
         float by,
         float bz,
         float bu,
         float bv,
         float cx,
         float cy,
         float cz,
         float cu,
         float cv,
         float dx,
         float dy,
         float dz,
         float du,
         float dv,
         float n1x,
         float n1y,
         float n1z,
         float n2x,
         float n2y,
         float n2z,
         int color
      ) {
         if (this.count * 12 + 12 > this.p.length) {
            int capacity = this.count * 2 + 16;
            this.p = Arrays.copyOf(this.p, capacity * 12);
            this.t = Arrays.copyOf(this.t, capacity * 8);
            this.n = Arrays.copyOf(this.n, capacity * 12);
            this.c = Arrays.copyOf(this.c, capacity);
         }

         int o = this.count * 12;
         int u = this.count * 8;
         this.p[o] = ax;
         this.p[o + 1] = ay;
         this.p[o + 2] = az;
         this.p[o + 3] = bx;
         this.p[o + 4] = by;
         this.p[o + 5] = bz;
         this.p[o + 6] = cx;
         this.p[o + 7] = cy;
         this.p[o + 8] = cz;
         this.p[o + 9] = dx;
         this.p[o + 10] = dy;
         this.p[o + 11] = dz;
         this.t[u] = au;
         this.t[u + 1] = av;
         this.t[u + 2] = bu;
         this.t[u + 3] = bv;
         this.t[u + 4] = cu;
         this.t[u + 5] = cv;
         this.t[u + 6] = du;
         this.t[u + 7] = dv;
         float l1 = (float)Math.sqrt((double)(n1x * n1x + n1y * n1y + n1z * n1z));
         float l2 = (float)Math.sqrt((double)(n2x * n2x + n2y * n2y + n2z * n2z));
         n1x /= l1;
         n1y /= l1;
         n1z /= l1;
         n2x /= l2;
         n2y /= l2;
         n2z /= l2;
         this.n[o] = n1x;
         this.n[o + 1] = n1y;
         this.n[o + 2] = n1z;
         this.n[o + 3] = n2x;
         this.n[o + 4] = n2y;
         this.n[o + 5] = n2z;
         this.n[o + 6] = n2x;
         this.n[o + 7] = n2y;
         this.n[o + 8] = n2z;
         this.n[o + 9] = n1x;
         this.n[o + 10] = n1y;
         this.n[o + 11] = n1z;
         this.c[this.count] = color;
         this.count++;
      }
   }

   static final class Prepared {
      private final int[] main;
      private final int width;
      private final int height;
      private final int[] icon;
      private final SpriteModel.Geometry geometry;

      private Prepared(int[] main, int width, int height, int[] icon, SpriteModel.Geometry geometry) {
         this.main = main;
         this.width = width;
         this.height = height;
         this.icon = icon;
         this.geometry = geometry;
      }
   }

   private static final class Segments {
      private float[] data = new float[4096];
      private int count;

      void add(float x1, float y1, float x2, float y2) {
         if (this.count * 4 + 4 > this.data.length) {
            this.data = Arrays.copyOf(this.data, this.data.length * 2);
         }

         int o = this.count * 4;
         this.data[o] = x1;
         this.data[o + 1] = y1;
         this.data[o + 2] = x2;
         this.data[o + 3] = y2;
         this.count++;
      }

      private static long key(float x, float y) {
         return (long)Float.floatToIntBits(x) << 32 | (long)Float.floatToIntBits(y) & 4294967295L;
      }

      private void link(Map<Long, int[]> ends, long key, int segment) {
         int[] slots = ends.computeIfAbsent(key, k -> new int[]{-1, -1, -1, -1});

         for (int i = 0; i < slots.length; i++) {
            if (slots[i] < 0) {
               slots[i] = segment;
               return;
            }
         }
      }

      List<float[]> chains() {
         Map<Long, int[]> ends = new HashMap<>();

         for (int s = 0; s < this.count; s++) {
            this.link(ends, key(this.data[s * 4], this.data[s * 4 + 1]), s);
            this.link(ends, key(this.data[s * 4 + 2], this.data[s * 4 + 3]), s);
         }

         boolean[] used = new boolean[this.count];
         List<float[]> chains = new ArrayList<>();

         for (int s = 0; s < this.count; s++) {
            if (!used[s]) {
               used[s] = true;
               SpriteModel.FloatBuffer forward = this.walk(ends, used, s, this.data[s * 4 + 2], this.data[s * 4 + 3]);
               SpriteModel.FloatBuffer backward = this.walk(ends, used, s, this.data[s * 4], this.data[s * 4 + 1]);
               float[] chain = new float[(backward.size + forward.size) * 2 + 4];
               int o = 0;

               for (int i = backward.size - 1; i >= 0; i--) {
                  chain[o++] = backward.values[i * 2];
                  chain[o++] = backward.values[i * 2 + 1];
               }

               chain[o++] = this.data[s * 4];
               chain[o++] = this.data[s * 4 + 1];
               chain[o++] = this.data[s * 4 + 2];
               chain[o++] = this.data[s * 4 + 3];

               for (int i = 0; i < forward.size; i++) {
                  chain[o++] = forward.values[i * 2];
                  chain[o++] = forward.values[i * 2 + 1];
               }

               chains.add(chain);
            }
         }

         return chains;
      }

      private SpriteModel.FloatBuffer walk(Map<Long, int[]> ends, boolean[] used, int start, float x, float y) {
         SpriteModel.FloatBuffer points = new SpriteModel.FloatBuffer();
         int current = start;

         while (true) {
            int[] slots = ends.get(key(x, y));
            int next = -1;
            if (slots != null) {
               for (int candidate : slots) {
                  if (candidate >= 0 && candidate != current && !used[candidate]) {
                     next = candidate;
                     break;
                  }
               }
            }

            if (next < 0) {
               return points;
            }

            used[next] = true;
            int o = next * 4;
            boolean fromStart = this.data[o] == x && this.data[o + 1] == y;
            x = fromStart ? this.data[o + 2] : this.data[o];
            y = fromStart ? this.data[o + 3] : this.data[o + 1];
            points.add(x, y);
            current = next;
         }
      }
   }
}
