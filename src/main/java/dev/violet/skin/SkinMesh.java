package dev.violet.skin;

import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class SkinMesh {
   private static final class_2960 WHITE = class_2960.method_60655("violet", "skin/white");
   private static boolean whiteReady;
   private static int counter;
   public final List<SkinMesh.Part> parts;
   float neckHint = 0.0F / 0.0F;
   float headHint = 0.0F / 0.0F;
   private boolean uploaded;

   SkinMesh(List<SkinMesh.Part> parts) {
      this.parts = parts;
   }

   public int triangleCount() {
      int sum = 0;

      for (SkinMesh.Part part : this.parts) {
         sum += part.triangleCount();
      }

      return sum;
   }

   public void upload() {
      if (!this.uploaded) {
         this.uploaded = true;
         class_310 mc = class_310.method_1551();

         for (SkinMesh.Part part : this.parts) {
            if (part.image == null) {
               if (!whiteReady) {
                  class_1011 white = new class_1011(1, 1, false);
                  white.method_61941(0, 0, -1);
                  mc.method_1531().method_4616(WHITE, new class_1043(() -> "violet skin white", white));
                  whiteReady = true;
               }

               part.texture = WHITE;
            } else {
               class_2960 id = class_2960.method_60655("violet", "skin/texture_" + counter++);
               class_1011 image = part.image;
               part.image = null;
               mc.method_1531().method_4616(id, new class_1043(() -> "violet skin " + id, image));
               part.texture = id;
            }
         }
      }
   }

   public void release() {
      class_310 mc = class_310.method_1551();

      for (SkinMesh.Part part : this.parts) {
         if (part.image != null) {
            part.image.close();
            part.image = null;
         }

         if (part.texture != null && !part.texture.equals(WHITE)) {
            mc.method_1531().method_4615(part.texture);
         }
      }
   }

   public static final class Part {
      public final float[] positions;
      public final float[] uvs;
      public final float[] normals;
      public final int[] colors;
      public final int[] triangles;
      private class_1011 image;
      private class_2960 texture;

      Part(float[] positions, float[] uvs, float[] normals, int[] colors, int[] triangles, class_1011 image) {
         this.positions = positions;
         this.uvs = uvs;
         this.normals = normals;
         this.colors = colors;
         this.triangles = triangles;
         this.image = image;
      }

      public class_2960 texture() {
         return this.texture;
      }

      public int triangleCount() {
         return this.triangles.length / 3;
      }
   }
}
