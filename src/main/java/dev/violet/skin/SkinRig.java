package dev.violet.skin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SkinRig {
   public static final int BODY = 0;
   public static final int HEAD = 1;
   public static final int RIGHT_ARM = 2;
   public static final int LEFT_ARM = 3;
   public static final int RIGHT_LEG = 4;
   public static final int LEFT_LEG = 5;
   private static final int H = 160;
   private static final float CELL = 0.00625F;
   public final SkinMesh mesh;
   public final int turn;
   public final float[][] positions;
   public final float[][] normals;
   public final byte[][] bones;
   public final float[][] weights;
   public final float[][] upper;
   public final float[] hip = new float[3];
   public final float[] neck = new float[3];
   public final float[][] armPivot = new float[2][3];
   public final float[][] legPivot = new float[2][3];
   public final float[] armRest = new float[2];
   public final boolean[] arms = new boolean[2];
   public boolean legs;
   public float headStrength = 1.0F;
   public float halfWidth;
   public float wingY;
   public float backZ;
   private float x0;
   private int width;
   private boolean[] grid;
   private int centerCol;

   private SkinRig(SkinMesh mesh, int turn) {
      this.mesh = mesh;
      this.turn = turn;
      int count = mesh.parts.size();
      this.positions = new float[count][];
      this.normals = new float[count][];
      this.bones = new byte[count][];
      this.weights = new float[count][];
      this.upper = new float[count][];
   }

   public static SkinRig build(SkinMesh mesh, int turn, boolean relax) {
      SkinRig rig = new SkinRig(mesh, turn);
      rig.rotate();
      rig.analyze(relax);
      return rig;
   }

   private void rotate() {
      double a = Math.toRadians((double)this.turn);
      float c = (float)Math.cos(a);
      float s = (float)Math.sin(a);

      for (int i = 0; i < this.mesh.parts.size(); i++) {
         SkinMesh.Part part = this.mesh.parts.get(i);
         float[] p = (float[])part.positions.clone();
         float[] n = (float[])part.normals.clone();

         for (int v = 0; v < p.length; v += 3) {
            float x = p[v];
            float z = p[v + 2];
            p[v] = c * x + s * z;
            p[v + 2] = -s * x + c * z;
            x = n[v];
            z = n[v + 2];
            n[v] = c * x + s * z;
            n[v + 2] = -s * x + c * z;
            this.halfWidth = Math.max(this.halfWidth, Math.abs(p[v]));
         }

         this.positions[i] = p;
         this.normals[i] = n;
         int vertices = p.length / 3;
         this.bones[i] = new byte[vertices];
         this.weights[i] = new float[vertices];
         this.upper[i] = new float[vertices];
      }
   }

   private static float smooth(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t * t * (3.0F - 2.0F * t);
   }

   private int col(float x) {
      return Math.max(0, Math.min(this.width - 1, (int)Math.floor((double)((x - this.x0) / 0.00625F))));
   }

   private static int row(float y) {
      return Math.max(0, Math.min(159, (int)Math.floor((double)(y / 0.00625F))));
   }

   private float colX(int c) {
      return this.x0 + ((float)c + 0.5F) * 0.00625F;
   }

   private void mark(float x, float y) {
      this.grid[row(y) * this.width + this.col(x)] = true;
   }

   private void rasterize() {
      float minX = 3.4028235E38F;
      float maxX = -3.4028235E38F;

      for (float[] p : this.positions) {
         for (int v = 0; v < p.length; v += 3) {
            minX = Math.min(minX, p[v]);
            maxX = Math.max(maxX, p[v]);
         }
      }

      this.x0 = minX - 0.00625F;
      this.width = Math.max(3, Math.min(800, (int)Math.ceil((double)((maxX - minX) / 0.00625F)) + 3));
      this.grid = new boolean[this.width * 160];

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int[] t = this.mesh.parts.get(i).triangles;

         for (int k = 0; k + 2 < t.length; k += 3) {
            int a = t[k] * 3;
            int b = t[k + 1] * 3;
            int c = t[k + 2] * 3;
            this.triangle(p[a], p[a + 1], p[b], p[b + 1], p[c], p[c + 1]);
         }
      }
   }

   private void edge(float ax, float ay, float bx, float by) {
      float length = (float)Math.sqrt((double)((bx - ax) * (bx - ax) + (by - ay) * (by - ay)));
      int steps = Math.max(1, (int)Math.ceil((double)(length / 0.003125F)));

      for (int i = 0; i <= steps; i++) {
         float t = (float)i / (float)steps;
         this.mark(ax + (bx - ax) * t, ay + (by - ay) * t);
      }
   }

   private void triangle(float ax, float ay, float bx, float by, float cx, float cy) {
      this.edge(ax, ay, bx, by);
      this.edge(bx, by, cx, cy);
      this.edge(cx, cy, ax, ay);
      float area = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
      if (!(Math.abs(area) < 1.0E-9F)) {
         int c0 = this.col(Math.min(ax, Math.min(bx, cx)));
         int c1 = this.col(Math.max(ax, Math.max(bx, cx)));
         int r0 = row(Math.min(ay, Math.min(by, cy)));
         int r1 = row(Math.max(ay, Math.max(by, cy)));

         for (int r = r0; r <= r1; r++) {
            float py = ((float)r + 0.5F) * 0.00625F;

            for (int c = c0; c <= c1; c++) {
               float px = this.colX(c);
               float w0 = ((bx - px) * (cy - py) - (by - py) * (cx - px)) / area;
               float w1 = ((cx - px) * (ay - py) - (cy - py) * (ax - px)) / area;
               float w2 = 1.0F - w0 - w1;
               if (w0 >= 0.0F && w1 >= 0.0F && w2 >= 0.0F) {
                  this.grid[r * this.width + c] = true;
               }
            }
         }
      }
   }

   private List<int[]> runs(int r) {
      List<int[]> list = new ArrayList<>();
      int start = -1;
      int last = -10;

      for (int c = 0; c < this.width; c++) {
         if (this.grid[r * this.width + c]) {
            if (start >= 0 && c - last <= 2) {
               last = c;
            } else {
               if (start >= 0) {
                  list.add(new int[]{start, last});
               }

               start = c;
               last = c;
            }
         }
      }

      if (start >= 0) {
         list.add(new int[]{start, last});
      }

      return list;
   }

   private void analyze(boolean relax) {
      this.rasterize();
      float sum = 0.0F;
      int filled = 0;

      for (int r = row(0.35F); r <= row(0.85F); r++) {
         for (int c = 0; c < this.width; c++) {
            if (this.grid[r * this.width + c]) {
               sum += this.colX(c);
               filled++;
            }
         }
      }

      float cx = filled > 0 ? sum / (float)filled : 0.0F;
      this.centerCol = this.col(cx);
      List<List<int[]>> rows = new ArrayList<>();

      for (int r = 0; r < 160; r++) {
         rows.add(this.runs(r));
      }

      int bottom = 0;

      while (bottom < 160 && rows.get(bottom).isEmpty()) {
         bottom++;
      }

      int firstGap = -1;
      int lastGap = -1;
      int misses = 0;

      for (int r = bottom; (float)r < 112.0F; r++) {
         if (this.gapAt(rows.get(r))) {
            if (firstGap < 0) {
               firstGap = r;
            }

            lastGap = r;
            misses = 0;
         } else if (firstGap >= 0 ? ++misses > 2 : (float)(r - bottom) > 24.0F) {
            break;
         }
      }

      this.legs = firstGap >= 0 && (float)(lastGap - firstGap) >= 12.799999F && (float)(firstGap - bottom) <= 24.0F;
      int crotchRow = this.legs ? lastGap : -1;
      float crotchY = this.legs ? (float)(lastGap + 1) * 0.00625F : 0.45F;
      int[][] inner = new int[2][160];
      int[][] outer = new int[2][160];
      int[][] core = new int[2][160];

      for (int[] a : inner) {
         Arrays.fill(a, -1);
      }

      for (int rx = 0; rx < 160; rx++) {
         List<int[]> list = rows.get(rx);
         if (!list.isEmpty()) {
            int coreLeft;
            int coreRight;
            if (this.legs && rx <= crotchRow) {
               int gap = this.gapIndex(list);
               if (gap < 0) {
                  continue;
               }

               coreLeft = list.get(gap)[0];
               coreRight = list.get(gap + 1)[1];
            } else {
               int[] best = null;
               int bestDistance = 2147483647;

               for (int[] run : list) {
                  int distance = run[0] <= this.centerCol && run[1] >= this.centerCol
                     ? 0
                     : Math.min(Math.abs(run[0] - this.centerCol), Math.abs(run[1] - this.centerCol));
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = run;
                  }
               }

               coreLeft = best[0];
               coreRight = best[1];
            }

            core[0][rx] = coreLeft;
            core[1][rx] = coreRight;
            float y = ((float)rx + 0.5F) * 0.00625F;
            if (!(y < 0.12F) && !(y > 0.92F)) {
               for (int[] runx : list) {
                  if (runx[1] < coreLeft && (inner[0][rx] < 0 || runx[0] < outer[0][rx])) {
                     inner[0][rx] = runx[1];
                     outer[0][rx] = runx[0];
                  }

                  if (runx[0] > coreRight && (inner[1][rx] < 0 || runx[1] > outer[1][rx])) {
                     inner[1][rx] = runx[0];
                     outer[1][rx] = runx[1];
                  }
               }
            }
         }
      }

      float minArmTop = this.legs ? crotchY + 0.1F : 0.35F;

      for (int s = 0; s < 2; s++) {
         int sign = s == 0 ? -1 : 1;
         int bestTop = -1;
         int bestBottom = -1;
         int rxx = 159;

         while (rxx >= 0) {
            if (inner[s][rxx] < 0) {
               rxx--;
            } else {
               int top = rxx;
               int low = rxx;
               int gap = 0;

               while (true) {
                  label257: {
                     if (rxx >= 0) {
                        if (inner[s][rxx] >= 0) {
                           low = rxx;
                           gap = 0;
                           break label257;
                        }

                        if (++gap <= 2) {
                           break label257;
                        }
                     }

                     if ((float)(top + 1) * 0.00625F >= minArmTop && low > row(0.13F) + 1 && top - low > bestTop - bestBottom) {
                        bestTop = top;
                        bestBottom = low;
                     }
                     break;
                  }

                  rxx--;
               }
            }
         }

         if (bestTop >= 0 && (float)(bestTop - bestBottom) >= 9.599999F) {
            this.separatedArm(s, sign, bestTop, bestBottom, inner, outer, core);
         } else {
            this.horizontalArm(s, sign, cx, crotchY, rows, core);
         }
      }

      if (this.legs) {
         this.legs(cx, crotchRow, crotchY, rows);
      }

      float hipY = this.legs ? crotchY + 0.02F : 0.45F;
      this.hip[0] = cx;
      this.hip[1] = hipY;
      this.hip[2] = this.meanZ(hipY - 0.05F, hipY + 0.05F, -1);
      float shoulderY = 0.7F;
      if (this.arms[0] || this.arms[1]) {
         shoulderY = Math.max(this.arms[0] ? this.armPivot[0][1] : 0.0F, this.arms[1] ? this.armPivot[1][1] : 0.0F);
      }

      this.head(cx, shoulderY, core);

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int v = 0;

         for (int k = 0; v < p.length; k++) {
            if (this.bones[i][k] != 4 && this.bones[i][k] != 5) {
               this.upper[i][k] = smooth((p[v + 1] - (hipY - 0.03F)) / 0.08F);
            }

            v += 3;
         }
      }

      if (relax) {
         for (int s = 0; s < 2; s++) {
            if (this.arms[s]) {
               this.armRest[s] = this.rest(s);
            }
         }
      }

      this.wingY = !this.arms[0] && !this.arms[1]
         ? Math.max(0.5F, this.neck[1] - 0.04F)
         : Math.max(this.arms[0] ? this.armPivot[0][1] : 0.0F, this.arms[1] ? this.armPivot[1][1] : 0.0F);
      this.backZ = 0.0F;

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int v = 0;

         for (int k = 0; v < p.length; k++) {
            if (!(p[v + 1] < this.wingY - 0.1F) && !(p[v + 1] > this.wingY + 0.05F) && this.bones[i][k] != 2 && this.bones[i][k] != 3) {
               this.backZ = Math.min(this.backZ, p[v + 2]);
            }

            v += 3;
         }
      }

      this.grid = null;
   }

   private boolean gapAt(List<int[]> list) {
      return this.gapIndex(list) >= 0;
   }

   private int gapIndex(List<int[]> list) {
      int tolerance = Math.max(2, Math.round(6.3999996F));
      int best = -1;
      int bestDistance = 2147483647;

      for (int i = 0; i + 1 < list.size(); i++) {
         int start = list.get(i)[1] + 1;
         int end = list.get(i + 1)[0] - 1;
         int distance = this.centerCol < start ? start - this.centerCol : (this.centerCol > end ? this.centerCol - end : 0);
         if (distance <= tolerance && distance < bestDistance) {
            bestDistance = distance;
            best = i;
         }
      }

      return best;
   }

   private void separatedArm(int s, int sign, int top, int bottom, int[][] inner, int[][] outer, int[][] core) {
      float[] sep = new float[160];
      float last = 0.0F / 0.0F;

      for (int r = bottom; r <= top; r++) {
         if (inner[s][r] >= 0) {
            last = (this.colX(inner[s][r]) + this.colX(sign < 0 ? core[0][r] : core[1][r])) / 2.0F;
         }

         sep[r] = last;
      }

      float armWidth = 0.0F;
      float centerX = 0.0F;
      int samples = 0;

      for (int r = top; r >= Math.max(bottom, top - 4); r--) {
         if (inner[s][r] >= 0) {
            armWidth += (float)(Math.abs(outer[s][r] - inner[s][r]) + 1) * 0.00625F;
            centerX += (this.colX(outer[s][r]) + this.colX(inner[s][r])) / 2.0F;
            samples++;
         }
      }

      if (samples != 0) {
         armWidth /= (float)samples;
         centerX /= (float)samples;
         float armpitY = (float)(top + 1) * 0.00625F;
         float handY = (float)bottom * 0.00625F - 0.01F;
         int columnFrom = Math.min(inner[s][top], outer[s][top]);
         int columnTo = Math.max(inner[s][top], outer[s][top]);
         int topRow = top;
         int limit = Math.min(159, top + Math.round(armWidth * 2.5F / 0.00625F));

         for (int rx = top + 1; rx <= limit; topRow = rx++) {
            boolean any = false;

            for (int c = columnFrom; c <= columnTo && !any; c++) {
               any = this.grid[rx * this.width + c];
            }

            if (!any) {
               break;
            }
         }

         float shoulderTop = (float)(topRow + 1) * 0.00625F;
         float pivotY = shoulderTop - armWidth * 0.5F;
         float sepTop = sep[top];
         int bone = s == 0 ? 2 : 3;

         for (int i = 0; i < this.positions.length; i++) {
            float[] p = this.positions[i];
            int v = 0;

            for (int k = 0; v < p.length; k++) {
               float x = p[v];
               float y = p[v + 1];
               float w = 0.0F;
               if (y >= handY && y <= armpitY) {
                  float line = sep[Math.max(bottom, Math.min(top, row(y)))];
                  if (!Float.isNaN(line) && (float)sign * (x - line) > 0.0F) {
                     w = 1.0F;
                  }
               } else if (y > armpitY && y < shoulderTop + 0.01F) {
                  float d = (float)sign * (x - sepTop);
                  float fade = y <= pivotY ? 1.0F : 1.0F - smooth((y - pivotY) / Math.max(0.005F, shoulderTop + 0.01F - pivotY));
                  w = smooth(d / Math.max(0.005F, armWidth * 0.35F)) * fade;
               }

               if (w > this.weights[i][k]) {
                  this.bones[i][k] = (byte)bone;
                  this.weights[i][k] = w;
               }

               v += 3;
            }
         }

         float sy = 0.0F;
         float sx = 0.0F;
         float syy = 0.0F;
         float sxy = 0.0F;
         int fit = 0;

         for (int rx = Math.max(bottom, top - (top - bottom) / 2); rx <= top; rx++) {
            if (inner[s][rx] >= 0) {
               float yx = ((float)rx + 0.5F) * 0.00625F;
               float xx = (this.colX(outer[s][rx]) + this.colX(inner[s][rx])) / 2.0F;
               sy += yx;
               sx += xx;
               syy += yx * yx;
               sxy += xx * yx;
               fit++;
            }
         }

         float slope = 0.0F;
         float var56;
         if (fit >= 3) {
            float denominator = (float)fit * syy - sy * sy;
            if (Math.abs(denominator) > 1.0E-9F) {
               slope = Math.max(-3.0F, Math.min(3.0F, ((float)fit * sxy - sx * sy) / denominator));
               var56 = (sx - slope * sy) / (float)fit;
            } else {
               var56 = sx / (float)fit;
            }
         } else {
            var56 = centerX - slope * armpitY;
         }

         float armZ = this.meanZ(armpitY - 0.06F, armpitY + 0.005F, bone);

         for (int i = 0; i < this.positions.length; i++) {
            float[] p = this.positions[i];
            int v = 0;

            for (int k = 0; v < p.length; k++) {
               float yx = p[v + 1];
               if (!(yx <= armpitY) && !(yx > shoulderTop + 0.01F) && !((float)sign * (p[v] - centerX) < -armWidth * 1.5F)) {
                  float distance = Math.abs(p[v] - (var56 + slope * yx));
                  float along = 1.0F - smooth((distance - armWidth * 0.5F) / Math.max(0.004F, armWidth * 0.5F));
                  float depth = 1.0F - smooth((Math.abs(p[v + 2] - armZ) - armWidth * 0.6F) / Math.max(0.004F, armWidth * 0.6F));
                  float fade = yx <= pivotY ? 1.0F : 1.0F - smooth((yx - pivotY) / Math.max(0.005F, shoulderTop + 0.01F - pivotY));
                  float wx = along * depth * fade;
                  if (wx > this.weights[i][k] && (this.bones[i][k] == 0 || this.bones[i][k] == bone)) {
                     this.bones[i][k] = (byte)bone;
                     this.weights[i][k] = wx;
                  }
               }

               v += 3;
            }
         }

         this.arms[s] = true;
         this.armPivot[s][0] = var56 + slope * pivotY;
         this.armPivot[s][1] = pivotY;
         this.armPivot[s][2] = armZ;
      }
   }

   private void horizontalArm(int s, int sign, float cx, float crotchY, List<List<int[]>> rows, int[][] core) {
      int from = row(crotchY + 0.05F);
      int to = row(crotchY + 0.2F);
      List<Float> halves = new ArrayList<>();

      for (int r = from; r <= to; r++) {
         if (!rows.get(r).isEmpty()) {
            halves.add((float)sign * (this.colX(s == 0 ? core[0][r] : core[1][r]) - cx));
         }
      }

      if (!halves.isEmpty()) {
         halves.sort(Float::compare);
         float torsoHalf = Math.max(0.03F, halves.get(halves.size() / 2));
         int wideLow = -1;
         int wideHigh = -1;
         float reach = 0.0F;

         for (int rx = to + 1; rx < row(0.95F); rx++) {
            if (!rows.get(rx).isEmpty()) {
               float extent = (float)sign * (this.colX(s == 0 ? core[0][rx] : core[1][rx]) - cx);
               if (extent > torsoHalf * 1.7F) {
                  if (wideLow < 0) {
                     wideLow = rx;
                  }

                  wideHigh = rx;
                  reach = Math.max(reach, extent);
               }
            }
         }

         if (wideLow >= 0 && wideHigh - wideLow >= 1 && !(reach < torsoHalf * 2.2F)) {
            float shoulderX = cx + (float)sign * torsoHalf * 1.1F;
            float low = (float)wideLow * 0.00625F - 0.06F;
            float high = (float)(wideHigh + 1) * 0.00625F + 0.06F;
            int bone = s == 0 ? 2 : 3;

            for (int i = 0; i < this.positions.length; i++) {
               float[] p = this.positions[i];
               int v = 0;

               for (int k = 0; v < p.length; k++) {
                  float y = p[v + 1];
                  if (!(y < low) && !(y > high)) {
                     float w = smooth((float)sign * (p[v] - shoulderX) / 0.04F);
                     if (w > this.weights[i][k]) {
                        this.bones[i][k] = (byte)bone;
                        this.weights[i][k] = w;
                     }
                  }

                  v += 3;
               }
            }

            this.arms[s] = true;
            this.armPivot[s][0] = shoulderX;
            this.armPivot[s][1] = (float)(wideLow + wideHigh + 1) * 0.5F * 0.00625F;
            this.armPivot[s][2] = this.meanZ(low, high, bone);
         }
      }
   }

   private void legs(float cx, int crotchRow, float crotchY, List<List<int[]>> rows) {
      int sampleRow = Math.max(0, crotchRow - 3);
      float[] half = new float[]{0.04F, 0.04F};

      for (int[] run : rows.get(sampleRow)) {
         if (run[1] < this.centerCol) {
            half[0] = Math.max(half[0], (float)(run[1] - run[0] + 1) * 0.00625F / 2.0F);
         }

         if (run[0] > this.centerCol) {
            half[1] = Math.max(half[1], (float)(run[1] - run[0] + 1) * 0.00625F / 2.0F);
         }
      }

      float[] sumX = new float[2];
      float[] sumZ = new float[2];
      int[] count = new int[2];

      for (float[] p : this.positions) {
         for (int v = 0; v < p.length; v += 3) {
            float y = p[v + 1];
            if (!(y < crotchY - 0.1F) && !(y > crotchY)) {
               int s = p[v] < cx ? 0 : 1;
               sumX[s] += p[v];
               sumZ[s] += p[v + 2];
               count[s]++;
            }
         }
      }

      for (int s = 0; s < 2; s++) {
         int sign = s == 0 ? -1 : 1;
         this.legPivot[s][0] = count[s] > 0 ? sumX[s] / (float)count[s] : cx + (float)sign * half[s];
         this.legPivot[s][1] = crotchY + 0.02F;
         this.legPivot[s][2] = count[s] > 0 ? sumZ[s] / (float)count[s] : 0.0F;
      }

      float band = 0.06F;

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int vx = 0;

         for (int k = 0; vx < p.length; k++) {
            if (!(this.weights[i][k] > 0.5F)) {
               float x = p[vx];
               float y = p[vx + 1];
               if (!(y > crotchY + band)) {
                  int s = x < cx ? 0 : 1;
                  if (!(Math.abs(x - this.legPivot[s][0]) > half[s] * 2.4F + 0.02F)) {
                     float w = y <= crotchY ? 1.0F : 1.0F - smooth((y - crotchY) / band);
                     if (y > crotchY - 0.02F) {
                        w *= Math.min(1.0F, Math.abs(x - cx) / Math.max(0.01F, half[s]));
                     }

                     if (!(w <= 0.0F)) {
                        this.bones[i][k] = (byte)(s == 0 ? 4 : 5);
                        this.weights[i][k] = w;
                     }
                  }
               }
            }

            vx += 3;
         }
      }
   }

   private void head(float cx, float shoulderY, int[][] core) {
      int from = row(shoulderY);
      int to = row(0.94F);
      int shoulderWidth = 0;

      for (int r = Math.max(0, from - 8); r <= from; r++) {
         shoulderWidth = Math.max(shoulderWidth, core[1][r] - core[0][r]);
      }

      int narrow = -1;
      int narrowWidth = 2147483647;

      for (int r = from; r <= to; r++) {
         int w = core[1][r] - core[0][r];
         if (w > 0 && w < narrowWidth) {
            narrowWidth = w;
            narrow = r;
         }
      }

      float band = 0.05F;
      float neckY;
      if (!Float.isNaN(this.mesh.neckHint)) {
         neckY = this.mesh.neckHint;
         this.headStrength = Float.isNaN(this.mesh.headHint) ? 1.0F : this.mesh.headHint;
         band = 0.14F;
      } else if (narrow >= 0 && shoulderWidth > 0 && (float)narrowWidth < (float)shoulderWidth * 0.8F && narrow < to - 2) {
         neckY = (float)narrow * 0.00625F;
         this.headStrength = 1.0F;
      } else {
         neckY = 0.78F;
         this.headStrength = 0.5F;
      }

      this.neck[0] = cx;
      this.neck[1] = neckY;
      this.neck[2] = this.meanZ(neckY - 0.03F, neckY + 0.03F, -1);

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int v = 0;

         for (int k = 0; v < p.length; k++) {
            float w = smooth((p[v + 1] - (neckY - 0.02F)) / band);
            label54:
            if (!(w <= 0.0F)) {
               if (this.bones[i][k] != 0) {
                  if (this.weights[i][k] >= 0.5F) {
                     break label54;
                  }

                  w *= 1.0F - this.weights[i][k];
               }

               this.bones[i][k] = 1;
               this.weights[i][k] = w;
            }

            v += 3;
         }
      }
   }

   private float meanZ(float low, float high, int bone) {
      float sum = 0.0F;
      int count = 0;

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int v = 0;

         for (int k = 0; v < p.length; k++) {
            float y = p[v + 1];
            if (!(y < low) && !(y > high) && (bone < 0 || this.bones[i][k] == bone && !(this.weights[i][k] < 0.8F))) {
               sum += p[v + 2];
               count++;
            }

            v += 3;
         }
      }

      return count > 0 ? sum / (float)count : 0.0F;
   }

   private float rest(int s) {
      int bone = s == 0 ? 2 : 3;
      float[] pivot = this.armPivot[s];
      List<float[]> points = new ArrayList<>();

      for (int i = 0; i < this.positions.length; i++) {
         float[] p = this.positions[i];
         int v = 0;

         for (int k = 0; v < p.length; k++) {
            if (this.bones[i][k] == bone && !(this.weights[i][k] < 0.9F)) {
               float dx = p[v] - pivot[0];
               float dy = p[v + 1] - pivot[1];
               points.add(new float[]{dx, dy, dx * dx + dy * dy});
            }

            v += 3;
         }
      }

      if (points.size() < 3) {
         return 0.0F;
      } else {
         points.sort((a, b) -> Float.compare(b[2], a[2]));
         int take = Math.min(points.size(), Math.max(3, points.size() / 7));
         float dx = 0.0F;
         float dy = 0.0F;

         for (int i = 0; i < take; i++) {
            dx += ((float[])points.get(i))[0];
            dy += ((float[])points.get(i))[1];
         }

         int sign = s == 0 ? -1 : 1;
         double outward = Math.atan2((double)((float)sign * dx), (double)(-dy));
         if (outward < 0.45) {
            return 0.0F;
         } else {
            double target = 0.17;
            double tx = (double)sign * Math.sin(target);
            double ty = -Math.cos(target);
            double angle = Math.atan2(ty, tx) - Math.atan2((double)dy, (double)dx);

            while (angle > 3.141592653589793) {
               angle -= 6.283185307179586;
            }

            while (angle < -3.141592653589793) {
               angle += 6.283185307179586;
            }

            return (float)angle;
         }
      }
   }
}
