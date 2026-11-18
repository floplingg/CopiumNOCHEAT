package dev.violet.esp;

import dev.violet.util.BlockBoxHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.function.LongFunction;
import net.minecraft.class_2338;
import net.minecraft.class_238;

public final class BlockBoxCache {
   private final int capacity;
   private final LongFunction<class_238> factory;
   private final Long2ObjectLinkedOpenHashMap<BlockBoxCache.Entry> boxes = new Long2ObjectLinkedOpenHashMap();
   private float size = 0.0F / 0.0F;
   private long frame;

   BlockBoxCache(int capacity) {
      this(capacity, null);
   }

   public BlockBoxCache(int capacity, LongFunction<class_238> factory) {
      this.capacity = Math.max(1, capacity);
      this.factory = factory;
   }

   public void beginFrame(float size) {
      if (Float.floatToIntBits(this.size) != Float.floatToIntBits(size)) {
         this.boxes.clear();
         this.size = size;
      }

      this.frame++;
   }

   public class_238 box(long packed) {
      BlockBoxCache.Entry hit = (BlockBoxCache.Entry)this.boxes.getAndMoveToLast(packed);
      if (hit != null) {
         hit.frame = this.frame;
         return hit.box;
      } else {
         class_238 made = this.factory == null ? BlockBoxHelper.scaledBlockBox(class_2338.method_10092(packed), this.size) : this.factory.apply(packed);
         if (this.boxes.size() == this.capacity) {
            if (((BlockBoxCache.Entry)this.boxes.get(this.boxes.firstLongKey())).frame == this.frame) {
               return made;
            }

            this.boxes.removeFirst();
         }

         this.boxes.put(packed, new BlockBoxCache.Entry(made, this.frame));
         return made;
      }
   }

   int size() {
      return this.boxes.size();
   }

   private static final class Entry {
      final class_238 box;
      long frame;

      Entry(class_238 box, long frame) {
         this.box = box;
         this.frame = frame;
      }
   }
}
