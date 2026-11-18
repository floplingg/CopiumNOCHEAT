package dev.violet.render;

import java.util.Arrays;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class AccessoryMesh {
   public static final int GOLD = -3885571;
   public static final int GOLD_DARK = -7639072;
   public static final int FEATHER_A = -14276819;
   public static final int FEATHER_B = -15066336;
   public static final int FEATHER_BACK = -15724267;
   public static final int BONE = -13750216;
   private static final int[][] FACES = new int[][]{{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {4, 5, 1, 0}, {3, 2, 6, 7}};
   private static final float[][] NORMALS = new float[][]{
      {0.0F, 0.0F, -1.0F}, {0.0F, 0.0F, 1.0F}, {-1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, 0.0F}, {0.0F, -1.0F, 0.0F}, {0.0F, 1.0F, 0.0F}
   };
   private static final float[] LENGTHS = new float[]{8.5F, 10.5F, 12.0F, 13.2F, 13.6F, 12.6F, 10.6F};
   private static final float[] ALONG = new float[]{2.2F, 4.8F, 7.2F, 2.0F, 5.0F, 1.4F, 4.2F};
   public float[] positions = new float[768];
   public float[] normals = new float[192];
   public int[] colors = new int[64];
   public boolean[] glow = new boolean[64];
   public int quads;
   private final Vector3f corner = new Vector3f();
   private final Vector3f normal = new Vector3f();

   public AccessoryMesh reset() {
      this.quads = 0;
      return this;
   }

   private void ensure(int more) {
      if (this.quads + more > this.colors.length) {
         int size = Math.max(this.colors.length * 2, this.quads + more);
         this.positions = Arrays.copyOf(this.positions, size * 12);
         this.normals = Arrays.copyOf(this.normals, size * 3);
         this.colors = Arrays.copyOf(this.colors, size);
         this.glow = Arrays.copyOf(this.glow, size);
      }
   }

   public void box(Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int color, boolean bright) {
      this.ensure(6);
      float[][] c = new float[][]{{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
      float[] p = new float[24];

      for (int i = 0; i < 8; i++) {
         m.transformPosition(this.corner.set(c[i][0], c[i][1], c[i][2]));
         p[i * 3] = this.corner.x;
         p[i * 3 + 1] = this.corner.y;
         p[i * 3 + 2] = this.corner.z;
      }

      for (int f = 0; f < 6; f++) {
         for (int k = 0; k < 4; k++) {
            int v = FACES[f][k];
            System.arraycopy(p, v * 3, this.positions, this.quads * 12 + k * 3, 3);
         }

         m.transformDirection(this.normal.set(NORMALS[f][0], NORMALS[f][1], NORMALS[f][2])).normalize();
         this.normals[this.quads * 3] = this.normal.x;
         this.normals[this.quads * 3 + 1] = this.normal.y;
         this.normals[this.quads * 3 + 2] = this.normal.z;
         this.colors[this.quads] = color;
         this.glow[this.quads] = bright;
         this.quads++;
      }
   }

   public void wing(Matrix4f body, int side, float spread, float flap, float size, int accent) {
      Matrix4f m = new Matrix4f(body).scale((float)side, 1.0F, 1.0F).translate(1.6F, 2.4F, 2.3F).scale(size);
      m.rotateY(-(0.5F - 0.42F * spread) - flap).rotateX(0.1F * (1.0F - spread));
      float lift = -(0.6F - 0.32F * spread);
      Matrix4f gem = new Matrix4f(m).translate(0.4F, 0.6F, 0.5F);
      this.box(gem, -2.1F, -2.1F, -0.7F, 2.1F, 2.1F, 0.7F, -7639072, false);
      this.box(new Matrix4f(gem).rotateZ(0.7853982F), -1.25F, -1.25F, -0.95F, 1.25F, 1.25F, 0.95F, accent, true);
      Matrix4f bar = new Matrix4f(m).rotateZ(lift);
      this.box(bar, 1.2F, -1.1F, -0.8F, 8.0F, 1.1F, 0.8F, -13750216, false);
      Matrix4f bar2 = new Matrix4f(bar).translate(8.0F, 0.0F, 0.0F).rotateZ(0.2F);
      this.box(bar2, -0.3F, -1.0F, -0.7F, 7.0F, 1.0F, 0.7F, -13750216, false);
      Matrix4f bar3 = new Matrix4f(bar2).translate(7.0F, 0.0F, 0.0F).rotateZ(0.28F);
      this.box(bar3, -0.3F, -0.85F, -0.6F, 5.2F, 0.85F, 0.6F, -13750216, false);
      this.box(new Matrix4f(bar3).translate(5.2F, 0.0F, 0.0F).rotateZ(-1.2F), -0.4F, -0.7F, -0.55F, 2.6F, 0.7F, 0.55F, -3885571, false);
      Matrix4f[] frames = new Matrix4f[]{bar, bar, bar, bar2, bar2, bar3, bar3};
      Matrix4f inverse = new Matrix4f(m).invert();

      for (int i = 0; i < 7; i++) {
         float fold = (float)Math.toRadians((double)(100.0F - (float)i * 10.5F));
         float open = (float)Math.toRadians((double)(82.0F - (float)i * 10.5F));
         float angle = fold + (open - fold) * spread;
         Vector3f local = inverse.transformPosition(frames[i].transformPosition(new Vector3f(ALONG[i], 0.6F, 0.0F)));
         Matrix4f feather = new Matrix4f(m).translate(local.x, local.y, local.z + 0.12F * (float)i).rotateZ(angle);
         float length = LENGTHS[i];
         this.box(feather, 0.0F, -1.3F, -0.35F, length - 2.4F, 1.3F, 0.35F, i % 2 == 0 ? -14276819 : -15066336, false);
         this.box(feather, length - 2.4F, -1.1F, -0.3F, length, 1.1F, 0.3F, accent, true);
         this.box(feather, -0.4F, -1.45F, -0.5F, 1.0F, 1.45F, 0.5F, -3885571, false);
         if (i < 6) {
            Matrix4f back = new Matrix4f(m).translate(local.x + 0.6F, local.y + 0.4F, local.z + 0.55F).rotateZ(angle - 0.09F);
            this.box(back, 0.0F, -1.05F, -0.3F, length * 0.6F, 1.05F, 0.3F, -15724267, false);
         }
      }
   }

   public void crown(Matrix4f head, int accent, int metal) {
      int dark = metal == -3885571
         ? -7639072
         : 0xFF000000
            | Math.round((float)(metal >> 16 & 0xFF) * 0.8F) << 16
            | Math.round((float)(metal >> 8 & 0xFF) * 0.8F) << 8
            | Math.round((float)(metal & 0xFF) * 0.8F);
      Matrix4f m = new Matrix4f(head).translate(0.0F, -8.4F, 0.0F).rotateZ(0.05F);
      float r = 4.6F;
      float t = 0.8F;
      float top = -1.2F;
      float bottom = 0.9F;
      this.box(m, -r - t, top, -r - t, r + t, bottom, -r, metal, false);
      this.box(m, -r - t, top, r, r + t, bottom, r + t, metal, false);
      this.box(m, -r - t, top, -r, -r, bottom, r, metal, false);
      this.box(m, r, top, -r, r + t, bottom, r, metal, false);

      for (int side = 0; side < 4; side++) {
         Matrix4f s = new Matrix4f(m).rotateY((float)(1.5707963267948966 * (double)side));
         this.box(s, -0.75F, top - 3.6F, -r - t, 0.75F, top, -r, metal, false);
         this.box(s, -0.6F, top - 4.8F, -r - t + 0.05F, 0.6F, top - 3.6F, -r - 0.05F, accent, true);

         for (int k = -1; k <= 1; k += 2) {
            this.box(s, (float)k * 2.6F - 0.55F, top - 1.9F, -r - t, (float)k * 2.6F + 0.55F, top, -r, dark, false);
         }

         this.box(s, -r - t - 0.2F, top - 2.4F, -r - t - 0.2F, -r + 0.2F, bottom + 0.2F, -r + 0.2F, metal, false);
         this.box(s, -r - t + 0.05F, top - 3.0F, -r - t + 0.05F, -r - 0.05F, top - 2.4F, -r - 0.05F, dark, false);
      }

      Matrix4f gem = new Matrix4f(m).translate(0.0F, (top + bottom) / 2.0F, -r - t - 0.2F).rotateZ(0.7853982F);
      this.box(gem, -1.15F, -1.15F, -0.4F, 1.15F, 1.15F, 0.4F, accent, true);
   }
}
