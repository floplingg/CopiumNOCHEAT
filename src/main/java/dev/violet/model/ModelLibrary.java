package dev.violet.model;

import dev.violet.render.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModelLibrary {
   public static final String PREFIX = "builtin:";
   private static final float STEP = 0.5F;
   private static final float BEVEL = 0.8F;
   private static final float MIN_X = -6.0F;
   private static final float MAX_X = 38.0F;
   private static final float MIN_Y = -9.0F;
   private static final float MAX_Y = 42.0F;
   private static final Map<String, ModelLibrary.Design> DESIGNS = new LinkedHashMap<>();

   private ModelLibrary() {
   }

   public static List<String> names() {
      List<String> names = new ArrayList<>();

      for (String id : DESIGNS.keySet()) {
         names.add("builtin:" + id);
      }

      return names;
   }

   public static boolean has(String name) {
      return name != null && name.startsWith("builtin:") && DESIGNS.containsKey(name.substring("builtin:".length()));
   }

   public static String title(String name) {
      ModelLibrary.Design design = has(name) ? DESIGNS.get(name.substring("builtin:".length())) : null;
      return design == null ? name : design.title();
   }

   public static int swatch(String name) {
      ModelLibrary.Design design = has(name) ? DESIGNS.get(name.substring("builtin:".length())) : null;
      return design == null ? -1 : design.swatch();
   }

   public static ModelMesh build(String name) {
      ModelLibrary.Design design = DESIGNS.get(name.substring("builtin:".length()));
      ModelLibrary.Builder builder = new ModelLibrary.Builder();

      for (ModelLibrary.Part part : design.parts()) {
         extrude(part, builder);
      }

      return ModelMesh.normalize(Arrays.copyOf(builder.positions, builder.size * 9), Arrays.copyOf(builder.colors, builder.size), builder.size);
   }

   private static float zOf(float depth, float f) {
      float inside = Math.max(0.0F, Math.min(1.0F, -f / 0.8F));
      float s = inside * inside * (3.0F - 2.0F * inside);
      return depth * (0.55F + 0.45F * s);
   }

   private static void extrude(ModelLibrary.Part part, ModelLibrary.Builder out) {
      int nx = Math.round(88.0F);
      int ny = Math.round(102.0F);
      float[][] values = new float[nx + 1][ny + 1];

      for (int i = 0; i <= nx; i++) {
         for (int j = 0; j <= ny; j++) {
            values[i][j] = part.field().at(-6.0F + (float)i * 0.5F, -9.0F + (float)j * 0.5F);
         }
      }

      float depth = part.depth();
      float edge = zOf(depth, 0.0F);
      int[][] corners = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};
      float[] px = new float[8];
      float[] py = new float[8];
      float[] pf = new float[8];
      boolean[] crossing = new boolean[8];

      for (int i = 0; i < nx; i++) {
         for (int j = 0; j < ny; j++) {
            boolean any = false;

            for (int[] c : corners) {
               if (values[i + c[0]][j + c[1]] < 0.0F) {
                  any = true;
               }
            }

            if (any) {
               int n = 0;

               for (int k = 0; k < 4; k++) {
                  int[] a = corners[k];
                  int[] b = corners[(k + 1) % 4];
                  float fa = values[i + a[0]][j + a[1]];
                  float fb = values[i + b[0]][j + b[1]];
                  float ax = -6.0F + (float)(i + a[0]) * 0.5F;
                  float ay = -9.0F + (float)(j + a[1]) * 0.5F;
                  float bx = -6.0F + (float)(i + b[0]) * 0.5F;
                  float by = -9.0F + (float)(j + b[1]) * 0.5F;
                  if (fa < 0.0F) {
                     px[n] = ax;
                     py[n] = ay;
                     pf[n] = fa;
                     crossing[n++] = false;
                  }

                  if (fa < 0.0F != fb < 0.0F) {
                     float t = fa / (fa - fb);
                     px[n] = ax + (bx - ax) * t;
                     py[n] = ay + (by - ay) * t;
                     pf[n] = 0.0F;
                     crossing[n++] = true;
                  }
               }

               if (n >= 3) {
                  float cx = 0.0F;
                  float cy = 0.0F;

                  for (int k = 0; k < n; k++) {
                     cx += px[k];
                     cy += py[k];
                  }

                  cx /= (float)n;
                  cy /= (float)n;
                  int color = part.paint().at(cx, cy, part.field().at(cx, cy));

                  for (int k = 1; k + 1 < n; k++) {
                     out.triangle(px[0], py[0], zOf(depth, pf[0]), px[k], py[k], zOf(depth, pf[k]), px[k + 1], py[k + 1], zOf(depth, pf[k + 1]), color);
                     out.triangle(px[0], py[0], -zOf(depth, pf[0]), px[k + 1], py[k + 1], -zOf(depth, pf[k + 1]), px[k], py[k], -zOf(depth, pf[k]), color);
                  }

                  int side = mix(color, 0, 0.28F);

                  for (int k = 0; k < n; k++) {
                     int q = (k + 1) % n;
                     if (crossing[k] && crossing[q]) {
                        out.triangle(px[k], py[k], -edge, px[q], py[q], -edge, px[q], py[q], edge, side);
                        out.triangle(px[k], py[k], -edge, px[q], py[q], edge, px[k], py[k], edge, side);
                     }
                  }
               }
            }
         }
      }
   }

   private static float clamp01(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static int mix(int a, int b, float t) {
      t = clamp01(t);
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
   }

   private static int hsv(float h, float s, float v) {
      return Color.fromHsv(h, s, v, 255).packed() & 16777215;
   }

   private static float segment(float px, float py, float ax, float ay, float bx, float by, float r) {
      float ex = bx - ax;
      float ey = by - ay;
      float wx = px - ax;
      float wy = py - ay;
      float t = clamp01((wx * ex + wy * ey) / (ex * ex + ey * ey));
      float dx = wx - ex * t;
      float dy = wy - ey * t;
      return (float)Math.sqrt((double)(dx * dx + dy * dy)) - r;
   }

   private static float circle(float px, float py, float cx, float cy, float r) {
      float dx = px - cx;
      float dy = py - cy;
      return (float)Math.sqrt((double)(dx * dx + dy * dy)) - r;
   }

   private static float box(float px, float py, float cx, float cy, float hw, float hh, float r) {
      float qx = Math.abs(px - cx) - hw + r;
      float qy = Math.abs(py - cy) - hh + r;
      float ox = Math.max(qx, 0.0F);
      float oy = Math.max(qy, 0.0F);
      return (float)Math.sqrt((double)(ox * ox + oy * oy)) + Math.min(Math.max(qx, qy), 0.0F) - r;
   }

   private static float polygon(float px, float py, float... v) {
      int n = v.length / 2;
      float best = 3.4028235E38F;
      boolean inside = false;
      int i = 0;

      for (int j = n - 1; i < n; j = i++) {
         float ax = v[j * 2];
         float ay = v[j * 2 + 1];
         float bx = v[i * 2];
         float by = v[i * 2 + 1];
         float ex = bx - ax;
         float ey = by - ay;
         float wx = px - ax;
         float wy = py - ay;
         float t = clamp01((wx * ex + wy * ey) / (ex * ex + ey * ey));
         float dx = wx - ex * t;
         float dy = wy - ey * t;
         best = Math.min(best, dx * dx + dy * dy);
         if (ay > py != by > py && px < ax + (py - ay) * ex / ey) {
            inside = !inside;
         }
      }

      float distance = (float)Math.sqrt((double)best);
      return inside ? -distance : distance;
   }

   private static float[] star(float cx, float cy, float outer, float inner) {
      float[] points = new float[20];

      for (int i = 0; i < 10; i++) {
         double angle = 1.5707963267948966 + (double)i * 3.141592653589793 / 5.0;
         float r = i % 2 == 0 ? outer : inner;
         points[i * 2] = cx + (float)Math.cos(angle) * r;
         points[i * 2 + 1] = cy + (float)Math.sin(angle) * r;
      }

      return points;
   }

   private static ModelLibrary.Part handle(float top, float radius, int base, int wrap, float depth) {
      return new ModelLibrary.Part((x, y) -> segment(x, y, 16.0F, -4.0F, 16.0F, top, radius), (x, y, f) -> {
         boolean grip = y > -3.5F && y < 7.0F && Math.floorMod((int)Math.floor((double)((y + 4.0F) / 1.4F)), 2) == 0;
         int color = grip ? wrap : mix(base, 16777215, 0.08F * (y + 4.0F) / (top + 4.0F));
         return f > -0.3F ? mix(color, 16777215, 0.12F) : color;
      }, depth);
   }

   private static ModelLibrary.Part gem(float cx, float cy, float r, int color, float depth) {
      return new ModelLibrary.Part(
         (x, y) -> circle(x, y, cx, cy, r), (x, y, f) -> mix(color, 16777215, f > -0.35F ? 0.45F : clamp01((y - cy + r) / (2.0F * r)) * 0.35F), depth
      );
   }

   private static float headT(float x, float y) {
      double angle = Math.atan2((double)(y - 14.0F), (double)(x - 16.0F));
      double a0 = Math.toRadians(20.0);
      double a1 = Math.toRadians(160.0);
      return clamp01((float)((angle - a0) / (a1 - a0)));
   }

   private static float pickHead(float px, float py) {
      float cx = 16.0F;
      float cy = 14.0F;
      float radius = 12.0F;
      double a0 = Math.toRadians(20.0);
      double a1 = Math.toRadians(160.0);
      float dx = px - cx;
      float dy = py - cy;
      double angle = Math.atan2((double)dy, (double)dx);
      float t;
      float distance;
      if (angle >= a0 && angle <= a1) {
         t = (float)((angle - a0) / (a1 - a0));
         distance = Math.abs((float)Math.sqrt((double)(dx * dx + dy * dy)) - radius);
      } else {
         float e0x = cx + (float)Math.cos(a0) * radius;
         float e0y = cy + (float)Math.sin(a0) * radius;
         float e1x = cx + (float)Math.cos(a1) * radius;
         float e1y = cy + (float)Math.sin(a1) * radius;
         float d0 = (float)Math.hypot((double)(px - e0x), (double)(py - e0y));
         float d1 = (float)Math.hypot((double)(px - e1x), (double)(py - e1y));
         t = d0 < d1 ? 0.0F : 1.0F;
         distance = Math.min(d0, d1);
      }

      float middle = 1.0F - Math.abs(t - 0.5F) * 2.0F;
      return distance - (0.3F + 2.45F * (float)Math.pow((double)middle, 0.55));
   }

   private static ModelLibrary.Design pick(String title, ModelLibrary.Palette p) {
      ModelLibrary.Part head = new ModelLibrary.Part(ModelLibrary::pickHead, (x, y, f) -> {
         float t = headT(x, y);
         float middle = 1.0F - Math.abs(t - 0.5F) * 2.0F;
         int color = mix(p.headA(), p.headB(), 1.0F - middle);
         if (f > -0.45F) {
            return mix(color, p.rim(), 0.7F);
         } else {
            float vein = Math.abs((float)Math.hypot((double)(x - 16.0F), (double)(y - 14.0F)) - 12.0F);
            return vein < 0.35F ? mix(color, p.rim(), 0.35F) : color;
         }
      }, 1.5F);
      return new ModelLibrary.Design(
         title,
         p.headA(),
         List.of(handle(24.0F, 1.1F, p.handle(), p.wrap(), 1.0F), head, gem(16.0F, 25.6F, 2.3F, p.gem(), 1.9F), gem(16.0F, -4.6F, 1.6F, p.gem(), 1.3F))
      );
   }

   private static ModelLibrary.Design scythe() {
      ModelLibrary.Part blade = new ModelLibrary.Part(
         (x, y) -> Math.max(Math.max(circle(x, y, 7.0F, 26.0F, 10.0F), -circle(x, y, 5.0F, 23.5F, 9.5F)), 26.0F - y), (x, y, f) -> {
            int color = mix(14708991, 6962431, clamp01((x + 3.0F) / 20.0F));
            return f > -0.5F ? mix(color, 16049919, 0.75F) : color;
         }, 0.75F
      );
      return new ModelLibrary.Design(
         "Void Scythe",
         11559167,
         List.of(handle(30.0F, 1.0F, 2827328, 12103888, 0.9F), blade, gem(16.0F, 29.6F, 1.9F, 4255999, 1.4F), gem(16.0F, -4.6F, 1.5F, 4255999, 1.2F))
      );
   }

   private static ModelLibrary.Design axe() {
      ModelLibrary.Part head = new ModelLibrary.Part(
         (x, y) -> Math.min(
               Math.max(circle(x, y, 14.0F, 26.0F, 14.5F), polygon(x, y, 16.0F, 21.0F, 30.0F, 15.0F, 30.0F, 37.0F, 16.0F, 31.0F)),
               Math.max(circle(x, y, 18.0F, 26.0F, 14.5F), polygon(x, y, 16.0F, 21.0F, 16.0F, 31.0F, 2.0F, 37.0F, 2.0F, 15.0F))
            ),
         (x, y, f) -> {
            int color = hsv(clamp01((y - 15.0F) / 22.0F) * 0.8F, 0.62F, 1.0F);
            return f > -0.45F ? mix(color, 16777215, 0.55F) : color;
         },
         1.1F
      );
      return new ModelLibrary.Design(
         "Prism Axe",
         16747224,
         List.of(handle(28.0F, 1.1F, 15132400, 6970061, 1.0F), head, gem(16.0F, 26.0F, 2.6F, 16316671, 1.6F), gem(16.0F, -4.6F, 1.6F, 10128639, 1.3F))
      );
   }

   private static ModelLibrary.Design hammer() {
      float[] star = star(16.0F, 28.5F, 3.3F, 1.35F);
      ModelLibrary.Part head = new ModelLibrary.Part((x, y) -> box(x, y, 16.0F, 28.5F, 8.5F, 4.2F, 1.5F), (x, y, f) -> {
         int color = mix(14263584, 16769658, clamp01((y - 24.3F) / 8.4F));
         return f > -0.45F ? mix(color, 16773808, 0.7F) : color;
      }, 2.2F);
      ModelLibrary.Part emblem = new ModelLibrary.Part((x, y) -> polygon(x, y, star), (x, y, f) -> f > -0.3F ? 16774864 : 16777215, 2.5F);
      return new ModelLibrary.Design(
         "Star Hammer", 15911244, List.of(handle(25.0F, 1.25F, 3947600, 15911244, 1.1F), head, emblem, gem(16.0F, -4.6F, 1.7F, 15911244, 1.4F))
      );
   }

   static {
      DESIGNS.put("frost_pick", pick("Frost Pick", new ModelLibrary.Palette(3424348, 10475775, 7129343, 14743551, 16777215, 3844351)));
      DESIGNS.put("ember_pick", pick("Ember Pick", new ModelLibrary.Palette(2759188, 16742958, 16726815, 16763210, 16773312, 16769146)));
      DESIGNS.put("amethyst_pick", pick("Amethyst Pick", new ModelLibrary.Palette(3023936, 13148415, 9063935, 14723327, 16181503, 16743136)));
      DESIGNS.put("toxic_pick", pick("Toxic Pick", new ModelLibrary.Palette(1976862, 10288983, 3070064, 14221162, 16056288, 6750160)));
      DESIGNS.put("void_scythe", scythe());
      DESIGNS.put("prism_axe", axe());
      DESIGNS.put("star_hammer", hammer());
   }

   private static final class Builder {
      float[] positions = new float[36864];
      int[] colors = new int[4096];
      int size;

      void triangle(float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, int color) {
         if (this.size == this.colors.length) {
            this.colors = Arrays.copyOf(this.colors, this.size * 2);
            this.positions = Arrays.copyOf(this.positions, this.size * 18);
         }

         int o = this.size * 9;
         this.positions[o] = ax;
         this.positions[o + 1] = ay;
         this.positions[o + 2] = az;
         this.positions[o + 3] = bx;
         this.positions[o + 4] = by;
         this.positions[o + 5] = bz;
         this.positions[o + 6] = cx;
         this.positions[o + 7] = cy;
         this.positions[o + 8] = cz;
         this.colors[this.size++] = color & 16777215;
      }
   }

   private static record Design(String title, int swatch, List<ModelLibrary.Part> parts) {
   }

   private interface Field {
      float at(float var1, float var2);
   }

   private interface Paint {
      int at(float var1, float var2, float var3);
   }

   private static record Palette(int handle, int wrap, int headA, int headB, int rim, int gem) {
   }

   private static record Part(ModelLibrary.Field field, ModelLibrary.Paint paint, float depth) {
   }
}
