package dev.violet.util;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_9848;

@Environment(EnvType.CLIENT)
public final class ChunkTileOverlay {
   public static final double MARKER_Y = 63.0;
   private double height = 63.0;

   public void setHeight(double height) {
      this.height = height;
   }

   public void drawFilled(LongSet chunks, int color) {
      if (!chunks.isEmpty() && color >>> 24 >= 3) {
         class_243[][] squares = new class_243[chunks.size()][];
         double y = this.height;
         int i = 0;
         LongIterator keys = chunks.iterator();

         while (keys.hasNext() && i < squares.length) {
            long key = keys.nextLong();
            double x0 = (double)(class_1923.method_8325(key) << 4);
            double z0 = (double)(class_1923.method_8332(key) << 4);
            squares[i++] = new class_243[]{
               new class_243(x0, y, z0), new class_243(x0 + 16.0, y, z0), new class_243(x0 + 16.0, y, z0 + 16.0), new class_243(x0, y, z0 + 16.0)
            };
         }

         class_12180.method_75553(new ChunkTileOverlay.Squares(squares, i, color)).method_75533();
      }
   }

   private static record Squares(class_243[][] squares, int count, int color) implements class_12175 {
      public void method_75531(class_12177 primitives, float alpha) {
         int faded = class_9848.method_61330(Math.round((float)class_9848.method_61320(this.color) * alpha), this.color);

         for (int i = 0; i < this.count; i++) {
            primitives.method_75478(this.squares[i], faded);
         }
      }
   }
}
