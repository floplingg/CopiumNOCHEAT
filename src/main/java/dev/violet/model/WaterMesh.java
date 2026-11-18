package dev.violet.model;

import java.util.Arrays;

public final class WaterMesh {
   public final float[] positions;
   public final float[] normals;
   public final int quads;
   private static final float PICK_TOP = 1.12F;
   private static final int PICK_SEGMENTS = 24;
   private static final float[][] FRONT = profile(0.34F, 0.25F, 0.007F);
   private static final float[][] BACK = profile(0.2F, 0.13F, 0.022F);
   private static final int CHAIN = 16;
   private static final float[][] GUARD = guard();
   private static final float[][] PRONG = prong();

   WaterMesh(float[] positions, float[] normals) {
      this.positions = positions;
      this.normals = normals;
      this.quads = positions.length / 12;
   }

   private static float clamp(float v, float lo, float hi) {
      return v < lo ? lo : Math.min(v, hi);
   }

   private static float length(float x, float y) {
      return (float)Math.sqrt((double)(x * x + y * y));
   }

   private static float length(float x, float y, float z) {
      return (float)Math.sqrt((double)(x * x + y * y + z * z));
   }

   private static float smin(float a, float b, float k) {
      float h = Math.max(k - Math.abs(a - b), 0.0F) / k;
      return Math.min(a, b) - h * h * k * 0.25F;
   }

   private static float smax(float a, float b, float k) {
      return -smin(-a, -b, k);
   }

   private static float roundBox(float x, float y, float z, float hx, float hy, float hz, float r) {
      float qx = Math.abs(x) - hx + r;
      float qy = Math.abs(y) - hy + r;
      float qz = Math.abs(z) - hz + r;
      float outside = length(Math.max(qx, 0.0F), Math.max(qy, 0.0F), Math.max(qz, 0.0F));
      return outside + Math.min(Math.max(qx, Math.max(qy, qz)), 0.0F) - r;
   }

   private static float cone(float px, float py, float pz, float ax, float ay, float az, float bx, float by, float bz, float ra, float rb) {
      float bax = bx - ax;
      float bay = by - ay;
      float baz = bz - az;
      float pax = px - ax;
      float pay = py - ay;
      float paz = pz - az;
      float t = clamp((pax * bax + pay * bay + paz * baz) / (bax * bax + bay * bay + baz * baz), 0.0F, 1.0F);
      return length(pax - bax * t, pay - bay * t, paz - baz * t) - (ra + (rb - ra) * t);
   }

   private static float ring(float axial, float radial, float major, float minor) {
      return length(radial - major, axial) - minor;
   }

   private static float[][] profile(float span, float drop, float tip) {
      float[][] out = new float[3][25];

      for (int i = 0; i <= 24; i++) {
         float f = (float)i / 24.0F;
         out[0][i] = 1.05F - drop * (float)Math.pow((double)f, 2.3) + 0.016F * (float)Math.sin(3.141592653589793 * (double)f) * (1.0F - f);
         out[1][i] = f * span;
         out[2][i] = tip + (0.078F - tip) * (float)Math.pow((double)(1.0F - f), 0.6) + 0.008F * (float)Math.exp(-Math.pow((double)((f - 0.12F) / 0.14F), 2.0));
      }

      return out;
   }

   private static float pickaxeField(float s, float l, float z) {
      float radial = length(l, z);
      float handle = cone(s, l, z, 0.035F, 0.0F, 0.0F, 1.02F, 0.0F, 0.0F, 0.026F, 0.027F);
      float d = smin(handle, ring(s - 0.47F, radial, 0.028F, 0.008F), 0.008F);
      d = smin(d, ring(s - 0.455F, radial, 0.027F, 0.006F), 0.006F);
      float pommel = roundBox(s - 0.02F, l, z, 0.03F, 0.033F, 0.033F, 0.026F);
      d = smin(d, pommel, 0.016F);
      d = smin(d, ring(s - 0.062F, radial, 0.03F, 0.0075F), 0.006F);
      d = smin(d, cone(s, l, z, 0.92F, 0.0F, 0.0F, 0.98F, 0.0F, 0.0F, 0.028F, 0.04F), 0.012F);
      float al = Math.abs(l);
      float hz = z / 0.68F;
      float head = 3.4028235E38F;
      float[][] side = l >= 0.0F ? FRONT : BACK;

      for (int i = 0; i < 24; i++) {
         float seg = cone(s, al, hz, side[0][i], side[1][i], 0.0F, side[0][i + 1], side[1][i + 1], 0.0F, side[2][i], side[2][i + 1]);
         if (seg < head) {
            head = seg;
         }
      }

      head *= 0.8F;
      d = smin(d, head, 0.022F);
      float cap = roundBox(s - 1.122F, l, z, 0.017F, 0.055F, 0.042F, 0.009F);
      d = smin(d, cap, 0.006F);
      return smin(d, ring(s - 0.945F, radial, 0.031F, 0.008F), 0.006F);
   }

   private static float totemField(float x, float y, float z) {
      float cx = x - 0.5F;
      float cz = z - 0.5F;
      float side = Math.abs(cx);
      float d = roundBox(cx, y - 0.805F, cz, 0.148F, 0.17F, 0.128F, 0.055F);
      float eye = roundBox(side - 0.064F, y - 0.795F, cz - 0.128F, 0.028F, 0.03F, 0.013F, 0.011F);
      d = smax(d, -eye, 0.012F);
      float torso = roundBox(cx, y - 0.565F, cz, 0.118F, 0.09F, 0.1F, 0.055F);
      d = smin(d, torso, 0.03F);
      float arm = cone(side, y, cz, 0.06F, 0.605F, 0.0F, 0.305F, 0.6F, -0.035F, 0.05F, 0.058F);
      float sleeve = roundBox(side - 0.2F, y - 0.54F, cz + 0.012F, 0.105F, 0.058F, 0.017F, 0.012F);
      float teeth = 0.49F + 0.034F * Math.abs((side - 0.1F) / 0.042F % 1.0F - 0.5F) * 2.0F;
      sleeve = Math.max(sleeve, teeth - y);
      arm = smin(arm, sleeve, 0.02F);
      d = smin(d, arm, 0.028F);
      float belt = ring(y - 0.465F, length(cx, cz / 0.85F), 0.11F, 0.019F);
      belt = smin(belt, length(cx + 0.02F, y - 0.46F, cz - 0.098F) - 0.024F, 0.01F);
      belt = smin(belt, length(cx - 0.105F, y - 0.47F, cz - 0.02F) - 0.022F, 0.01F);
      d = smin(d, belt, 0.012F);
      float angle = (float)Math.atan2((double)cz, (double)cx);
      float t = clamp((0.47F - y) / 0.44F, 0.0F, 1.0F);
      float folds = 0.05F * t * (float)Math.sin((double)(angle * 6.0F + 0.7F)) + 0.025F * t * (float)Math.sin((double)(angle * 11.0F + 2.1F));
      float radius = (0.104F + 0.135F * (float)Math.pow((double)t, 1.8)) * (1.0F + folds);
      float robeSide = (length(cx, cz / 0.8F) - radius) * 0.72F;
      float wave = 0.5F + 0.5F * (float)Math.cos((double)(angle * 5.0F) - 1.5707963267948966);
      float edge = clamp(length(cx, cz / 0.8F) / radius, 0.0F, 1.0F);
      float hem = 0.025F + 0.065F * (1.0F - wave * wave * wave) * edge * edge * edge;
      float robe = Math.max(robeSide, Math.max(y - 0.48F, hem - y));
      return smin(d, robe, 0.035F);
   }

   private static float chain(float s, float l, float z, float[] cs, float[] cl, float[] cr) {
      float best = 3.4028235E38F;

      for (int i = 0; i + 1 < cs.length; i++) {
         float d = cone(s, l, z, cs[i], cl[i], 0.0F, cs[i + 1], cl[i + 1], 0.0F, cr[i], cr[i + 1]);
         if (d < best) {
            best = d;
         }
      }

      return best;
   }

   private static float[][] guard() {
      float[][] out = new float[3][17];

      for (int i = 0; i <= 16; i++) {
         float f = (float)i / 16.0F;
         out[0][i] = 0.29F + 0.05F * f * f;
         out[1][i] = 0.165F * f;
         out[2][i] = 0.024F - 0.006F * f;
      }

      return out;
   }

   private static float[][] prong() {
      float[][] out = new float[3][17];

      for (int i = 0; i <= 16; i++) {
         float f = (float)i / 16.0F;
         out[0][i] = 0.975F + 0.27F * f;
         out[1][i] = 0.135F + 0.035F * (float)Math.sin(3.141592653589793 * (double)f * 0.85);
         out[2][i] = 0.019F - 0.0155F * (float)Math.pow((double)f, 1.6);
      }

      return out;
   }

   private static float swordField(float s, float l, float z) {
      float radial = length(l, z);
      float d = length(s - 0.036F, l, z) - 0.036F;
      d = smin(d, cone(s, l, z, 0.05F, 0.0F, 0.0F, 0.085F, 0.0F, 0.0F, 0.028F, 0.02F), 0.012F);
      d = smin(d, cone(s, l, z, 0.08F, 0.0F, 0.0F, 0.28F, 0.0F, 0.0F, 0.023F, 0.026F), 0.008F);

      for (int i = 0; i < 3; i++) {
         d = smin(d, ring(s - (0.125F + (float)i * 0.05F), radial, 0.025F, 0.006F), 0.005F);
      }

      float al = Math.abs(l);
      float guard = chain(s, al, z, GUARD[0], GUARD[1], GUARD[2]);
      guard = smin(guard, length(s - 0.342F, al - 0.17F, z) - 0.03F, 0.014F);
      guard = smin(guard, roundBox(s - 0.295F, l, z, 0.032F, 0.042F, 0.032F, 0.016F), 0.012F);
      d = smin(d, guard, 0.012F);
      float t = clamp((s - 0.3F) / 0.92F, 0.0F, 1.0F);
      float width = 0.076F * (1.0F - (float)Math.pow((double)t, 2.4)) * (1.0F + 0.12F * (float)Math.sin(3.141592653589793 * (double)t * 0.85)) + 0.004F;
      float thick = 0.024F * (1.0F - (float)Math.pow((double)t, 1.6)) + 0.006F;
      float ridge = 1.0F - 0.35F * Math.min(1.0F, al / width);
      float blade = (length(al / width, Math.abs(z) / (thick * ridge)) - 1.0F) * Math.min(width, thick * ridge) * 0.85F;
      blade = Math.max(blade, Math.max(0.3F - s, s - 1.22F));
      return smin(d, blade, 0.01F);
   }

   private static float shovelField(float s, float l, float z) {
      float radial = length(l, z);
      float d = cone(s, l, z, 0.035F, 0.0F, 0.0F, 0.8F, 0.0F, 0.0F, 0.025F, 0.026F);
      d = smin(d, ring(s - 0.4F, radial, 0.027F, 0.008F), 0.008F);
      d = smin(d, ring(s - 0.385F, radial, 0.026F, 0.006F), 0.006F);
      d = smin(d, roundBox(s - 0.02F, l, z, 0.03F, 0.033F, 0.033F, 0.026F), 0.016F);
      d = smin(d, ring(s - 0.062F, radial, 0.03F, 0.0075F), 0.006F);
      d = smin(d, cone(s, l, z, 0.7F, 0.0F, 0.0F, 0.81F, 0.0F, 0.0F, 0.027F, 0.04F), 0.01F);
      float es = (s - 0.95F) / 0.19F;
      float el = l / 0.118F;
      float point = Math.max(0.0F, es) * 0.35F;
      float shape = ((float)Math.sqrt((double)(es * es + el * el / ((1.0F - point) * (1.0F - point)))) - 1.0F) * 0.11F;
      float dish = 0.032F * el * el - 0.012F;
      float plate = Math.abs(z - dish) - 0.016F;
      float blade = Math.max(shape, plate);
      blade = smin(blade, cone(s, l, z + 0.006F, 0.79F, 0.0F, 0.0F, 1.07F, 0.0F, 0.0F, 0.022F, 0.008F), 0.012F);
      return smin(d, blade, 0.014F);
   }

   private static float tridentField(float s, float l, float z) {
      float radial = length(l, z);
      float d = cone(s, l, z, 0.025F, 0.0F, 0.0F, 0.93F, 0.0F, 0.0F, 0.0185F, 0.02F);
      d = smin(d, length(s - 0.03F, l, z) - 0.026F, 0.01F);
      d = smin(d, cone(s, l, z, 0.03F, 0.0F, 0.0F, -0.035F, 0.0F, 0.0F, 0.018F, 0.004F), 0.006F);

      for (int i = 0; i < 3; i++) {
         d = smin(d, ring(s - (0.26F + (float)i * 0.24F), radial, 0.02F, 0.006F), 0.005F);
      }

      float al = Math.abs(l);
      float bar = chain(s, al, z, new float[]{0.92F, 0.935F, 0.975F}, new float[]{0.0F, 0.08F, 0.14F}, new float[]{0.024F, 0.022F, 0.02F});
      bar = smin(bar, roundBox(s - 0.9F, l, z, 0.032F, 0.036F, 0.03F, 0.014F), 0.012F);
      d = smin(d, bar, 0.012F);
      d = smin(d, cone(s, l, z, 0.92F, 0.0F, 0.0F, 1.34F, 0.0F, 0.0F, 0.022F, 0.003F), 0.01F);
      d = smin(d, cone(s, al, z, 1.215F, 0.004F, 0.0F, 1.17F, 0.05F, 0.0F, 0.011F, 0.003F), 0.006F);
      d = smin(d, chain(s, al, z, PRONG[0], PRONG[1], PRONG[2]), 0.012F);
      return smin(d, cone(s, al, z, 1.15F, 0.158F, 0.0F, 1.11F, 0.118F, 0.0F, 0.01F, 0.003F), 0.006F);
   }

   static WaterMesh contour(WaterMesh.Field field, WaterMesh.Place place, float x0, float y0, float z0, float x1, float y1, float z1, float cell) {
      int nx = (int)Math.ceil((double)((x1 - x0) / cell)) + 1;
      int ny = (int)Math.ceil((double)((y1 - y0) / cell)) + 1;
      int nz = (int)Math.ceil((double)((z1 - z0) / cell)) + 1;
      float[] values = new float[nx * ny * nz];

      for (int k = 0; k < nz; k++) {
         for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
               values[(k * ny + j) * nx + i] = field.at(x0 + (float)i * cell, y0 + (float)j * cell, z0 + (float)k * cell);
            }
         }
      }

      int cx = nx - 1;
      int cy = ny - 1;
      int cz = nz - 1;
      int[] index = new int[cx * cy * cz];
      Arrays.fill(index, -1);
      float[] verts = new float[12288];
      float[] norms = new float[12288];
      int count = 0;
      int[][] edges = new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
      float[] corner = new float[8];
      float h = cell * 0.35F;

      for (int k = 0; k < cz; k++) {
         for (int j = 0; j < cy; j++) {
            for (int i = 0; i < cx; i++) {
               int mask = 0;

               for (int c = 0; c < 8; c++) {
                  int ci = i + (c & 1);
                  int cj = j + (c >> 1 & 1);
                  int ck = k + (c >> 2 & 1);
                  corner[c] = values[(ck * ny + cj) * nx + ci];
                  if (corner[c] < 0.0F) {
                     mask |= 1 << c;
                  }
               }

               if (mask != 0 && mask != 255) {
                  float sx = 0.0F;
                  float sy = 0.0F;
                  float sz = 0.0F;
                  int crossings = 0;

                  for (int[] e : edges) {
                     float a = corner[e[0]];
                     float b = corner[e[1]];
                     if (a < 0.0F != b < 0.0F) {
                        float t = a / (a - b);
                        float ax = (float)(e[0] & 1);
                        float ay = (float)(e[0] >> 1 & 1);
                        float az = (float)(e[0] >> 2 & 1);
                        float bx = (float)(e[1] & 1);
                        float by = (float)(e[1] >> 1 & 1);
                        float bz = (float)(e[1] >> 2 & 1);
                        sx += ax + (bx - ax) * t;
                        sy += ay + (by - ay) * t;
                        sz += az + (bz - az) * t;
                        crossings++;
                     }
                  }

                  float px = x0 + ((float)i + sx / (float)crossings) * cell;
                  float py = y0 + ((float)j + sy / (float)crossings) * cell;
                  float pz = z0 + ((float)k + sz / (float)crossings) * cell;
                  float gx = 0.0F;
                  float gy = 0.0F;
                  float gz = 0.0F;

                  for (int step = 0; step < 3; step++) {
                     float f = field.at(px, py, pz);
                     gx = (field.at(px + h, py, pz) - field.at(px - h, py, pz)) / (2.0F * h);
                     gy = (field.at(px, py + h, pz) - field.at(px, py - h, pz)) / (2.0F * h);
                     gz = (field.at(px, py, pz + h) - field.at(px, py, pz - h)) / (2.0F * h);
                     if (step == 2) {
                        break;
                     }

                     float g2 = gx * gx + gy * gy + gz * gz;
                     if (g2 < 1.0E-8F) {
                        break;
                     }

                     float mx = clamp(-f * gx / g2, -cell * 0.5F, cell * 0.5F);
                     float my = clamp(-f * gy / g2, -cell * 0.5F, cell * 0.5F);
                     float mz = clamp(-f * gz / g2, -cell * 0.5F, cell * 0.5F);
                     px += mx;
                     py += my;
                     pz += mz;
                  }

                  float gl = length(gx, gy, gz);
                  if (gl < 1.0E-6F) {
                     gl = 1.0F;
                  }

                  if (count * 3 + 3 > verts.length) {
                     verts = Arrays.copyOf(verts, verts.length * 2);
                     norms = Arrays.copyOf(norms, norms.length * 2);
                  }

                  verts[count * 3] = px;
                  verts[count * 3 + 1] = py;
                  verts[count * 3 + 2] = pz;
                  norms[count * 3] = gx / gl;
                  norms[count * 3 + 1] = gy / gl;
                  norms[count * 3 + 2] = gz / gl;
                  index[(k * cy + j) * cx + i] = count++;
               }
            }
         }
      }

      WaterMesh.Builder builder = new WaterMesh.Builder();
      float[] p = new float[3];
      float[] n = new float[3];
      int[] cellAt = new int[4];

      for (int axis = 0; axis < 3; axis++) {
         int b = (axis + 1) % 3;
         int cxx = (axis + 2) % 3;

         for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
               for (int i = 0; i < nx; i++) {
                  int[] g = new int[]{i, j, k};
                  int[] size = new int[]{nx, ny, nz};
                  if (g[axis] + 1 < size[axis] && g[b] >= 1 && g[cxx] >= 1 && g[b] < size[b] - 1 && g[cxx] < size[cxx] - 1) {
                     int[] next = (int[])g.clone();
                     next[axis]++;
                     float v0 = values[(g[2] * ny + g[1]) * nx + g[0]];
                     float v1 = values[(next[2] * ny + next[1]) * nx + next[0]];
                     if (v0 < 0.0F != v1 < 0.0F) {
                        int[][] offsets = new int[][]{{-1, -1}, {0, -1}, {0, 0}, {-1, 0}};
                        boolean ok = true;

                        for (int q = 0; q < 4; q++) {
                           int[] cc = (int[])g.clone();
                           cc[b] += offsets[q][0];
                           cc[cxx] += offsets[q][1];
                           int id = index[(cc[2] * cy + cc[1]) * cx + cc[0]];
                           if (id < 0) {
                              ok = false;
                              break;
                           }

                           cellAt[q] = id;
                        }

                        if (ok) {
                           boolean forward = v0 < 0.0F;

                           for (int q = 0; q < 4; q++) {
                              int id = cellAt[forward ? q : 3 - q];
                              p[0] = verts[id * 3];
                              p[1] = verts[id * 3 + 1];
                              p[2] = verts[id * 3 + 2];
                              n[0] = norms[id * 3];
                              n[1] = norms[id * 3 + 1];
                              n[2] = norms[id * 3 + 2];
                              if (place != null) {
                                 place.apply(p, n);
                              }

                              builder.vertex(p, n);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return builder.build();
   }

   public static WaterMesh pickaxe() {
      return pickaxe(0.0F, 0.0105F);
   }

   public static WaterMesh pickaxeGlow() {
      return pickaxe(0.016F, 0.02F);
   }

   public static WaterMesh sword() {
      return diagonal(WaterMesh::swordField, 0.0F, 0.0095F, 0.06F, -0.01F, 1.24F, 0.21F, 0.06F);
   }

   public static WaterMesh swordGlow() {
      return diagonal(WaterMesh::swordField, 0.016F, 0.02F, 0.06F, -0.01F, 1.24F, 0.21F, 0.06F);
   }

   public static WaterMesh shovel() {
      return diagonal(WaterMesh::shovelField, 0.0F, 0.0105F, 0.06F, -0.01F, 1.16F, 0.14F, 0.07F);
   }

   public static WaterMesh shovelGlow() {
      return diagonal(WaterMesh::shovelField, 0.016F, 0.02F, 0.06F, -0.01F, 1.16F, 0.14F, 0.07F);
   }

   public static WaterMesh trident() {
      return diagonal(WaterMesh::tridentField, 0.0F, 0.009F, 0.02F, -0.05F, 1.36F, 0.2F, 0.04F);
   }

   public static WaterMesh tridentGlow() {
      return diagonal(WaterMesh::tridentField, 0.016F, 0.02F, 0.02F, -0.05F, 1.36F, 0.2F, 0.04F);
   }

   public static WaterMesh totem() {
      return totem(0.0F, 0.015F);
   }

   public static WaterMesh totemGlow() {
      return totem(0.016F, 0.026F);
   }

   private static WaterMesh diagonal(WaterMesh.Field field, float grow, float cell, float origin, float s0, float s1, float halfWidth, float halfDepth) {
      float root = (float)Math.sqrt(0.5);
      WaterMesh.Place place = (p, n) -> {
         float s = p[0];
         float l = p[1];
         float z = p[2];
         p[0] = origin + root * s - root * l;
         p[1] = origin + root * s + root * l;
         p[2] = 0.5F + z;
         float ns = n[0];
         float nl = n[1];
         float nz = n[2];
         n[0] = root * ns - root * nl;
         n[1] = root * ns + root * nl;
         n[2] = nz;
      };
      float pad = grow + cell;
      return contour(
         (s, l, z) -> field.at(s, l, z) - grow, place, s0 - pad, -halfWidth - pad, -halfDepth - pad, s1 + pad, halfWidth + pad, halfDepth + pad, cell
      );
   }

   private static WaterMesh pickaxe(float grow, float cell) {
      float root = (float)Math.sqrt(0.5);
      WaterMesh.Place place = (p, n) -> {
         float s = p[0];
         float l = p[1];
         float z = p[2];
         p[0] = 0.05F + root * s - root * l;
         p[1] = 0.05F + root * s + root * l;
         p[2] = 0.5F + z;
         float ns = n[0];
         float nl = n[1];
         float nz = n[2];
         n[0] = root * ns - root * nl;
         n[1] = root * ns + root * nl;
         n[2] = nz;
      };
      float pad = grow + cell;
      return contour((s, l, z) -> pickaxeField(s, l, z) - grow, place, -0.04F - pad, -0.25F - pad, -0.08F - pad, 1.15F + pad, 0.37F + pad, 0.08F + pad, cell);
   }

   private static WaterMesh totem(float grow, float cell) {
      float pad = grow + cell;
      return contour((x, y, z) -> totemField(x, y, z) - grow, null, 0.1F - pad, 0.0F - pad, 0.28F - pad, 0.9F + pad, 1.0F + pad, 0.72F + pad, cell);
   }

   private static final class Builder {
      private float[] pos = new float[12288];
      private float[] nrm = new float[12288];
      private int size;

      void vertex(float[] p, float[] n) {
         if (this.size + 3 > this.pos.length) {
            this.pos = Arrays.copyOf(this.pos, this.pos.length * 2);
            this.nrm = Arrays.copyOf(this.nrm, this.nrm.length * 2);
         }

         System.arraycopy(p, 0, this.pos, this.size, 3);
         System.arraycopy(n, 0, this.nrm, this.size, 3);
         this.size += 3;
      }

      WaterMesh build() {
         return new WaterMesh(Arrays.copyOf(this.pos, this.size), Arrays.copyOf(this.nrm, this.size));
      }
   }

   interface Field {
      float at(float var1, float var2, float var3);
   }

   interface Place {
      void apply(float[] var1, float[] var2);
   }
}
