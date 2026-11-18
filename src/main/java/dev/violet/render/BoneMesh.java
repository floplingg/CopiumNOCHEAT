package dev.violet.render;

import java.util.Arrays;

public final class BoneMesh {
   public static final int CORE = 0;
   public static final int DARK = 1;
   public static final int GLOW = 2;
   public float[] positions = new float[3072];
   public float[] normals = new float[3072];
   public byte[] kinds = new byte[256];
   public int quads;

   private void ensure(int more) {
      int need = (this.quads + more) * 12;
      if (need > this.positions.length) {
         int size = Math.max(need, this.positions.length * 2);
         this.positions = Arrays.copyOf(this.positions, size);
         this.normals = Arrays.copyOf(this.normals, size);
         this.kinds = Arrays.copyOf(this.kinds, size / 12);
      }
   }

   public BoneMesh trim() {
      this.positions = Arrays.copyOf(this.positions, this.quads * 12);
      this.normals = Arrays.copyOf(this.normals, this.quads * 12);
      this.kinds = Arrays.copyOf(this.kinds, this.quads);
      return this;
   }

   private void vertex(int q, int k, double x, double y, double z, double nx, double ny, double nz) {
      int o = q * 12 + k * 3;
      this.positions[o] = (float)x;
      this.positions[o + 1] = (float)y;
      this.positions[o + 2] = (float)z;
      double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
      if (length < 1.0E-6) {
         length = 1.0;
      }

      this.normals[o] = (float)(nx / length);
      this.normals[o + 1] = (float)(ny / length);
      this.normals[o + 2] = (float)(nz / length);
   }

   public void ellipsoid(double cx, double cy, double cz, double rx, double ry, double rz, int kind, int segments, int rings) {
      this.ensure(segments * rings);

      for (int r = 0; r < rings; r++) {
         double t0 = 3.141592653589793 * (double)r / (double)rings;
         double t1 = 3.141592653589793 * (double)(r + 1) / (double)rings;

         for (int s = 0; s < segments; s++) {
            double p0 = 6.283185307179586 * (double)s / (double)segments;
            double p1 = 6.283185307179586 * (double)(s + 1) / (double)segments;
            double[][] corners = new double[][]{{t0, p0}, {t1, p0}, {t1, p1}, {t0, p1}};

            for (int k = 0; k < 4; k++) {
               double ux = Math.sin(corners[k][0]) * Math.cos(corners[k][1]);
               double uy = Math.cos(corners[k][0]);
               double uz = Math.sin(corners[k][0]) * Math.sin(corners[k][1]);
               this.vertex(this.quads, k, cx + ux * rx, cy + uy * ry, cz + uz * rz, ux / rx, uy / ry, uz / rz);
            }

            this.kinds[this.quads++] = (byte)kind;
         }
      }
   }

   public void sphere(double cx, double cy, double cz, double r, int kind, int segments, int rings) {
      this.ellipsoid(cx, cy, cz, r, r, r, kind, segments, rings);
   }

   public void tube(double[][] path, double radius, int kind, int segments) {
      this.tube(path, radius, radius, kind, segments);
   }

   public void tube(double[][] path, double start, double end, int kind, int segments) {
      int n = path.length;
      this.ensure((n - 1) * segments);
      double[][] ringU = new double[n][];
      double[][] ringV = new double[n][];
      double[] previous = null;

      for (int i = 0; i < n; i++) {
         double[] a = path[Math.max(0, i - 1)];
         double[] b = path[Math.min(n - 1, i + 1)];
         double[] d = normalize(b[0] - a[0], b[1] - a[1], b[2] - a[2]);
         double[] u = previous == null ? perpendicular(d) : normalize(cross(cross(d, previous), d));
         if (Double.isNaN(u[0])) {
            u = perpendicular(d);
         }

         double[] v = cross(d, u);
         ringU[i] = u;
         ringV[i] = v;
         previous = u;
      }

      for (int i = 0; i < n - 1; i++) {
         double r0 = start + (end - start) * (double)i / (double)(n - 1);
         double r1 = start + (end - start) * (double)(i + 1) / (double)(n - 1);

         for (int s = 0; s < segments; s++) {
            double a0 = 6.283185307179586 * (double)s / (double)segments;
            double a1 = 6.283185307179586 * (double)(s + 1) / (double)segments;
            double[][] corners = new double[][]{{(double)i, a0, r0}, {(double)(i + 1), a0, r1}, {(double)(i + 1), a1, r1}, {(double)i, a1, r0}};

            for (int k = 0; k < 4; k++) {
               int p = (int)corners[k][0];
               double c = Math.cos(corners[k][1]);
               double sn = Math.sin(corners[k][1]);
               double r = corners[k][2];
               double nx = ringU[p][0] * c + ringV[p][0] * sn;
               double ny = ringU[p][1] * c + ringV[p][1] * sn;
               double nz = ringU[p][2] * c + ringV[p][2] * sn;
               this.vertex(this.quads, k, path[p][0] + nx * r, path[p][1] + ny * r, path[p][2] + nz * r, nx, ny, nz);
            }

            this.kinds[this.quads++] = (byte)kind;
         }
      }
   }

   public void box(double x0, double y0, double z0, double x1, double y1, double z1, int kind) {
      this.ensure(6);
      double[][][] faces = new double[][][]{
         {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {0.0, 0.0, -1.0}},
         {{x1, y0, z1}, {x0, y0, z1}, {x0, y1, z1}, {x1, y1, z1}, {0.0, 0.0, 1.0}},
         {{x0, y0, z1}, {x0, y0, z0}, {x0, y1, z0}, {x0, y1, z1}, {-1.0, 0.0, 0.0}},
         {{x1, y0, z0}, {x1, y0, z1}, {x1, y1, z1}, {x1, y1, z0}, {1.0, 0.0, 0.0}},
         {{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}, {0.0, -1.0, 0.0}},
         {{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}, {0.0, 1.0, 0.0}}
      };

      for (double[][] face : faces) {
         for (int k = 0; k < 4; k++) {
            this.vertex(this.quads, k, face[k][0], face[k][1], face[k][2], face[4][0], face[4][1], face[4][2]);
         }

         this.kinds[this.quads++] = (byte)kind;
      }
   }

   public void ring(double cx, double cy, double cz, double radius, double thickness, int kind, int segments) {
      double[][] path = new double[segments + 1][];

      for (int i = 0; i <= segments; i++) {
         double a = 6.283185307179586 * (double)i / (double)segments;
         path[i] = new double[]{cx + Math.cos(a) * radius, cy + Math.sin(a) * radius, cz};
      }

      this.tube(path, thickness, kind, 6);
   }

   private static double[] normalize(double x, double y, double z) {
      double l = Math.sqrt(x * x + y * y + z * z);
      return l < 1.0E-9 ? new double[]{0.0, 1.0, 0.0} : new double[]{x / l, y / l, z / l};
   }

   private static double[] normalize(double[] v) {
      return normalize(v[0], v[1], v[2]);
   }

   private static double[] cross(double[] a, double[] b) {
      return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
   }

   private static double[] perpendicular(double[] d) {
      double[] axis = Math.abs(d[0]) < 0.8 ? new double[]{1.0, 0.0, 0.0} : new double[]{0.0, 0.0, 1.0};
      return normalize(cross(cross(d, axis), d));
   }

   public static BoneMesh[] skeleton() {
      return new BoneMesh[]{head().trim(), body().trim(), arm(-1).trim(), arm(1).trim(), leg().trim(), leg().trim()};
   }

   private static BoneMesh head() {
      BoneMesh m = new BoneMesh();
      m.ellipsoid(0.0, -4.7, 0.4, 3.45, 3.25, 3.55, 0, 16, 10);
      m.ellipsoid(0.0, -3.1, -2.4, 2.65, 1.85, 1.45, 0, 14, 8);
      m.ellipsoid(0.0, -1.45, -1.9, 2.25, 1.0, 1.7, 0, 14, 6);

      for (int side = -1; side <= 1; side += 2) {
         m.ellipsoid((double)side * 1.38, -3.85, -3.15, 1.02, 1.0, 0.55, 1, 12, 6);
         m.ring((double)side * 1.38, -3.85, -3.62, 1.08, 0.24, 0, 14);
         m.ellipsoid((double)side * 2.85, -2.9, -1.3, 0.55, 0.8, 0.7, 0, 8, 5);
      }

      m.ellipsoid(0.0, -2.75, -3.55, 0.38, 0.55, 0.3, 1, 8, 5);
      m.box(-1.75, -2.25, -3.42, 1.75, -1.95, -3.0, 1);

      for (int i = 0; i < 6; i++) {
         double x = -1.5 + (double)i * 0.6;
         m.box(x - 0.22, -2.1, -3.62, x + 0.22, -1.55, -3.2, 0);
         m.box(x - 0.22, -2.65, -3.62, x + 0.22, -2.2, -3.25, 0);
      }

      for (int i = 0; i < 2; i++) {
         m.box(-0.5, -0.3 + (double)i * 0.45 - 0.45, 0.2, 0.5, -0.05 + (double)i * 0.45 - 0.45, 1.2, 0);
      }

      return m;
   }

   private static BoneMesh body() {
      BoneMesh m = new BoneMesh();

      for (int i = 0; i < 14; i++) {
         double y = 0.25 + (double)i * 0.78;
         m.box(-0.55, y, 0.3, 0.55, y + 0.55, 1.35, 0);
         m.box(-0.18, y + 0.1, 1.3, 0.18, y + 0.45, 1.75, 0);
      }

      for (int side = -1; side <= 1; side += 2) {
         m.tube(new double[][]{{(double)side * 0.4, 0.75, -1.25}, {(double)side * 2.0, 0.35, -1.0}, {(double)side * 3.65, 0.55, -0.2}}, 0.32, 0, 8);
         m.sphere((double)side * 3.6, 0.6, -0.15, 0.5, 0, 10, 6);
         m.tube(
            new double[][]{{(double)side * 1.0, 1.0, 1.3}, {(double)side * 2.8, 1.2, 1.25}, {(double)side * 2.3, 3.8, 1.2}, {(double)side * 1.0, 2.3, 1.3}},
            0.32,
            0,
            6
         );
      }

      m.tube(new double[][]{{0.0, 1.0, -1.65}, {0.0, 3.0, -1.75}, {0.0, 5.6, -1.65}}, 0.5, 0.35, 0, 8);

      for (int i = 0; i < 7; i++) {
         double y = 1.4 + (double)i * 0.95;
         double rx = 3.35 - (double)Math.max(0, i - 4) * 0.25 - (i == 0 ? 0.5 : 0.0);
         double reach = i < 5 ? 0.93 : 0.72;

         for (int side = -1; side <= 1; side += 2) {
            double[][] path = new double[10][];

            for (int k = 0; k < 10; k++) {
               double t = (double)k / 9.0;
               double theta = 3.141592653589793 * reach * t;
               double x = (double)side * (0.5 + rx * Math.sin(theta));
               double z = 1.1 * Math.cos(theta) * 1.25 - 0.1;
               double dy = 0.15 + 0.85 * t * t;
               path[k] = new double[]{x, y + dy, z};
            }

            m.tube(path, 0.26, 0, 6);
         }
      }

      for (int side = -1; side <= 1; side += 2) {
         m.ellipsoid((double)side * 2.0, 10.4, 0.3, 1.75, 1.25, 0.85, 0, 12, 7);
         m.tube(new double[][]{{(double)side * 1.9, 11.2, -0.2}, {(double)side * 1.2, 11.6, -1.0}, {0.0, 11.65, -1.2}}, 0.36, 0, 6);
      }

      m.ellipsoid(0.0, 10.6, 0.8, 0.9, 1.35, 0.6, 0, 10, 6);
      return m;
   }

   private static BoneMesh arm(int side) {
      BoneMesh m = new BoneMesh();
      double c = (double)side;
      m.sphere(c, -0.7, 0.0, 1.15, 0, 12, 8);
      m.tube(new double[][]{{c, -0.4, 0.0}, {c, 2.2, 0.05}, {c, 4.5, 0.0}}, 0.62, 0.5, 0, 8);
      m.sphere(c, 4.85, 0.0, 0.88, 0, 12, 7);
      m.tube(new double[][]{{c - 0.42, 5.4, 0.0}, {c - 0.45, 7.3, -0.08}, {c - 0.4, 9.0, -0.1}}, 0.32, 0.26, 0, 6);
      m.tube(new double[][]{{c + 0.42, 5.4, 0.0}, {c + 0.42, 7.3, 0.08}, {c + 0.38, 9.0, 0.1}}, 0.3, 0.24, 0, 6);
      m.ellipsoid(c, 9.3, 0.0, 0.8, 0.45, 0.6, 0, 10, 6);

      for (int k = 0; k < 4; k++) {
         double fx = c - 0.72 + (double)k * 0.48;
         m.tube(new double[][]{{fx, 9.6, -0.1}, {fx, 10.2, -0.25}, {fx + ((double)k - 1.5) * 0.05, 10.4, -0.35}}, 0.17, 0.13, 0, 5);
      }

      return m;
   }

   private static BoneMesh leg() {
      BoneMesh m = new BoneMesh();
      m.sphere(0.0, 0.5, 0.0, 0.95, 0, 12, 7);
      m.tube(new double[][]{{0.0, 0.8, 0.0}, {0.05, 3.0, -0.1}, {0.0, 5.2, 0.0}}, 0.68, 0.56, 0, 8);
      m.ellipsoid(0.0, 5.75, 0.0, 1.0, 0.75, 0.9, 0, 12, 7);
      m.sphere(0.0, 5.8, -0.95, 0.55, 0, 10, 6);
      m.tube(new double[][]{{-0.28, 6.4, 0.0}, {-0.3, 8.6, -0.05}, {-0.25, 10.8, 0.0}}, 0.5, 0.4, 0, 7);
      m.tube(new double[][]{{0.55, 6.6, 0.2}, {0.55, 8.6, 0.2}, {0.5, 10.6, 0.15}}, 0.24, 0.2, 0, 5);
      m.sphere(0.0, 11.05, 0.0, 0.62, 0, 10, 6);
      m.tube(new double[][]{{0.0, 11.4, 0.4}, {0.0, 11.6, -0.6}, {0.0, 11.75, -1.75}}, 0.42, 0.3, 0, 6);

      for (int k = 0; k < 3; k++) {
         m.tube(new double[][]{{-0.5 + (double)k * 0.5, 11.7, -1.6}, {-0.5 + (double)k * 0.5, 11.8, -1.95}}, 0.16, 0, 5);
      }

      return m;
   }
}
