package dev.violet.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2338.class_2339;

public final class Shafts {
   private Shafts() {
   }

   public static List<Shafts.Shaft> find(class_1937 world, class_1923 chunkPos, int minDepth, boolean oneByOne, boolean threeByOne) {
      List<Shafts.Shaft> found = new ArrayList<>();
      if (!oneByOne && !threeByOne) {
         return found;
      } else {
         int bottomY = world.method_31607();
         int topY = world.method_31600();
         class_2339 m = new class_2339();

         for (int x = chunkPos.method_8326(); x < chunkPos.method_8326() + 16; x++) {
            for (int z = chunkPos.method_8328(); z < chunkPos.method_8328() + 16; z++) {
               if (oneByOne) {
                  scanColumn(world, found, m, x, z, bottomY, topY, minDepth, Shafts.Shape.ONE);
               }

               if (threeByOne) {
                  scanColumn(world, found, m, x, z, bottomY, topY, minDepth, Shafts.Shape.THREE_X);
                  scanColumn(world, found, m, x, z, bottomY, topY, minDepth, Shafts.Shape.THREE_Z);
               }
            }
         }

         return found;
      }
   }

   private static void scanColumn(class_1937 world, List<Shafts.Shaft> out, class_2339 m, int x, int z, int bottomY, int topY, int depth, Shafts.Shape shape) {
      int run = 0;

      for (int y = bottomY; y <= topY + 1; y++) {
         if (y <= topY && matches(world, m, x, y, z, shape)) {
            run++;
         } else {
            if (run >= depth) {
               out.add(new Shafts.Shaft(x, y - run, z, y, shape));
            }

            run = 0;
         }
      }
   }

   private static boolean matches(class_1937 w, class_2339 m, int x, int y, int z, Shafts.Shape shape) {
      return switch (shape) {
         case ONE -> open(w, m, x, y, z) && !open(w, m, x - 1, y, z) && !open(w, m, x + 1, y, z) && !open(w, m, x, y, z - 1) && !open(w, m, x, y, z + 1);
         case THREE_X -> open(w, m, x, y, z)
         && open(w, m, x + 1, y, z)
         && open(w, m, x + 2, y, z)
         && !open(w, m, x - 1, y, z)
         && !open(w, m, x + 3, y, z)
         && !open(w, m, x, y, z - 1)
         && !open(w, m, x, y, z + 1)
         && !open(w, m, x + 1, y, z - 1)
         && !open(w, m, x + 1, y, z + 1)
         && !open(w, m, x + 2, y, z - 1)
         && !open(w, m, x + 2, y, z + 1);
         case THREE_Z -> open(w, m, x, y, z)
         && open(w, m, x, y, z + 1)
         && open(w, m, x, y, z + 2)
         && !open(w, m, x, y, z - 1)
         && !open(w, m, x, y, z + 3)
         && !open(w, m, x - 1, y, z)
         && !open(w, m, x + 1, y, z)
         && !open(w, m, x - 1, y, z + 1)
         && !open(w, m, x + 1, y, z + 1)
         && !open(w, m, x - 1, y, z + 2)
         && !open(w, m, x + 1, y, z + 2);
      };
   }

   private static boolean open(class_1937 world, class_2339 m, int x, int y, int z) {
      m.method_10103(x, y, z);
      return world.method_8320(m).method_26220(world, m).method_1110();
   }

   public static record Shaft(int x, int minY, int z, int maxY, Shafts.Shape shape) {
   }

   public static enum Shape {
      ONE(1, 1),
      THREE_X(3, 1),
      THREE_Z(1, 3);

      public final int sizeX;
      public final int sizeZ;

      private Shape(int sizeX, int sizeZ) {
         this.sizeX = sizeX;
         this.sizeZ = sizeZ;
      }
   }
}
