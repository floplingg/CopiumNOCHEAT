package dev.violet.model;

public final class ModelMesh {
   public final int triangles;
   public final float[] positions;
   public final float[] normals;
   public final float[] along;
   public final int[] colors;
   public final float depth;

   ModelMesh(int triangles, float[] positions, float[] normals, float[] along, int[] colors) {
      this.triangles = triangles;
      float deepest = 1.0E-6F;

      for (int v = 0; v < triangles * 3; v++) {
         deepest = Math.max(deepest, Math.abs(positions[v * 3 + 2] - 0.5F));
      }

      this.depth = deepest;
      this.positions = positions;
      this.normals = normals;
      this.along = along;
      this.colors = colors;
   }

   public boolean hasColors() {
      for (int color : this.colors) {
         if (color != -1) {
            return true;
         }
      }

      return false;
   }

   static ModelMesh normalize(float[] raw, int[] colors, int triangles) {
      float[] min = new float[]{3.4028235E38F, 3.4028235E38F, 3.4028235E38F};
      float[] max = new float[]{-3.4028235E38F, -3.4028235E38F, -3.4028235E38F};

      for (int i = 0; i < triangles * 9; i++) {
         int axis = i % 3;
         min[axis] = Math.min(min[axis], raw[i]);
         max[axis] = Math.max(max[axis], raw[i]);
      }

      float[] extent = new float[]{max[0] - min[0], max[1] - min[1], max[2] - min[2]};
      int longAxis = 0;

      for (int a = 1; a < 3; a++) {
         if (extent[a] > extent[longAxis]) {
            longAxis = a;
         }
      }

      int wideAxis = -1;
      int thinAxis = -1;

      for (int ax = 0; ax < 3; ax++) {
         if (ax != longAxis) {
            if (wideAxis >= 0 && !(extent[ax] > extent[wideAxis])) {
               thinAxis = ax;
            } else {
               if (wideAxis >= 0) {
                  thinAxis = wideAxis;
               }

               wideAxis = ax;
            }
         }
      }

      float length = Math.max(1.0E-6F, extent[longAxis]);
      float lowMin = 3.4028235E38F;
      float lowMax = -3.4028235E38F;
      float highMin = 3.4028235E38F;
      float highMax = -3.4028235E38F;

      for (int v = 0; v < triangles * 3; v++) {
         float t = (raw[v * 3 + longAxis] - min[longAxis]) / length;
         float w = raw[v * 3 + wideAxis];
         if (t < 0.25F) {
            lowMin = Math.min(lowMin, w);
            lowMax = Math.max(lowMax, w);
         } else if (t > 0.75F) {
            highMin = Math.min(highMin, w);
            highMax = Math.max(highMax, w);
         }
      }

      float lowWidth = lowMax > lowMin ? lowMax - lowMin : 0.0F;
      float highWidth = highMax > highMin ? highMax - highMin : 0.0F;
      boolean headHigh = highWidth >= lowWidth;
      float cx = (min[0] + max[0]) / 2.0F;
      float cy = (min[1] + max[1]) / 2.0F;
      float cz = (min[2] + max[2]) / 2.0F;
      float[] center = new float[]{cx, cy, cz};
      float scale = 1.3F / length;
      float cos = (float)Math.cos(Math.toRadians(-45.0));
      float sin = (float)Math.sin(Math.toRadians(-45.0));
      float[] positions = new float[triangles * 9];
      float[] along = new float[triangles * 3];

      for (int vx = 0; vx < triangles * 3; vx++) {
         float ly = (raw[vx * 3 + longAxis] - center[longAxis]) * scale;
         float lx = (raw[vx * 3 + wideAxis] - center[wideAxis]) * scale;
         float lz = (raw[vx * 3 + thinAxis] - center[thinAxis]) * scale;
         if (!headHigh) {
            ly = -ly;
            lx = -lx;
         }

         positions[vx * 3] = lx * cos - ly * sin + 0.5F;
         positions[vx * 3 + 1] = lx * sin + ly * cos + 0.5F;
         positions[vx * 3 + 2] = lz + 0.5F;
         along[vx] = ly / 1.3F + 0.5F;
      }

      float[] normals = new float[triangles * 3];

      for (int t = 0; t < triangles; t++) {
         int o = t * 9;
         float axx = positions[o + 3] - positions[o];
         float ay = positions[o + 4] - positions[o + 1];
         float az = positions[o + 5] - positions[o + 2];
         float bx = positions[o + 6] - positions[o];
         float by = positions[o + 7] - positions[o + 1];
         float bz = positions[o + 8] - positions[o + 2];
         float nx = ay * bz - az * by;
         float ny = az * bx - axx * bz;
         float nz = axx * by - ay * bx;
         float len = (float)Math.sqrt((double)(nx * nx + ny * ny + nz * nz));
         if (len < 1.0E-9F) {
            normals[t * 3 + 2] = 1.0F;
         } else {
            normals[t * 3] = nx / len;
            normals[t * 3 + 1] = ny / len;
            normals[t * 3 + 2] = nz / len;
         }
      }

      return new ModelMesh(triangles, positions, normals, along, colors);
   }
}
