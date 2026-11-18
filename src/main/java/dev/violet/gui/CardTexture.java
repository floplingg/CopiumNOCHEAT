package dev.violet.gui;

import java.nio.ByteBuffer;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;

final class CardTexture {
   private static int counter;
   private final class_2960 id;

   private CardTexture(class_2960 id) {
      this.id = id;
   }

   class_2960 id() {
      return this.id;
   }

   static CardTexture fromPixels(ByteBuffer rgba, int size) {
      class_1011 image = new class_1011(size, size, false);

      for (int y = 0; y < size; y++) {
         for (int x = 0; x < size; x++) {
            int i = (y * size + x) * 4;
            int r = rgba.get(i) & 255;
            int g = rgba.get(i + 1) & 255;
            int b = rgba.get(i + 2) & 255;
            int a = rgba.get(i + 3) & 255;
            image.method_61941(x, y, a << 24 | r << 16 | g << 8 | b);
         }
      }

      class_2960 id = class_2960.method_60655("violet", "spotify/cover_" + counter++);
      class_310.method_1551().method_1531().method_4616(id, new class_1043(() -> "violet spotify cover", image));
      return new CardTexture(id);
   }

   void release() {
      class_310.method_1551().method_1531().method_4615(this.id);
   }
}
