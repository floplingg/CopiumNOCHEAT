package dev.violet.util;

import dev.violet.gui.RegionMap;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_746;

public final class ShardTracker {
   private static final int OFF_MAP = -2147483648;
   private class_638 level;
   private class_746 self;
   private String brand;
   private int region = -2147483648;

   public boolean moved(class_310 mc) {
      class_638 nowLevel = mc.field_1687;
      class_746 nowSelf = mc.field_1724;
      if (nowLevel != null && nowSelf != null) {
         class_634 connection = mc.method_1562();
         String nowBrand = connection == null ? null : connection.method_52790();
         int nowRegion = region(nowLevel, nowSelf);
         boolean moved = this.level != null
            && (
               nowLevel != this.level
                  || nowSelf != this.self && !this.self.method_29504()
                  || nowRegion != this.region
                  || nowBrand != null && this.brand != null && !nowBrand.equals(this.brand)
            );
         this.level = nowLevel;
         this.self = nowSelf;
         this.brand = nowBrand;
         this.region = nowRegion;
         return moved;
      } else {
         this.clear();
         return false;
      }
   }

   public void clear() {
      this.level = null;
      this.self = null;
      this.brand = null;
      this.region = -2147483648;
   }

   private static int region(class_638 level, class_746 self) {
      if (level.method_27983() == class_1937.field_25179 && DonutUtils.onDonut()) {
         RegionMap.Region region = RegionMap.at(self.method_23317(), self.method_23321());
         return region == null ? -2147483648 : region.row() * 1000 + region.col();
      } else {
         return -2147483648;
      }
   }
}
