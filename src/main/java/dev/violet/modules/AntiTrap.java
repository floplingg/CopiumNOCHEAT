package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_2604;
import net.minecraft.class_1297.class_5529;

public class AntiTrap extends Module {
   public AntiTrap() {
      super("Anti Trap", "Removes armor stands and chest minecarts client side to escape Polish traps.", Category.COMBAT);
   }

   @Override
   public void onEnable() {
      if (mc.field_1687 != null) {
         List<class_1297> traps = new ArrayList<>();

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (isTrap(entity.method_5864())) {
               traps.add(entity);
            }
         }

         for (class_1297 entityx : traps) {
            mc.field_1687.method_2945(entityx.method_5628(), class_5529.field_26999);
         }
      }
   }

   @Override
   public boolean onEntitySpawn(class_2604 packet) {
      return isTrap(packet.method_11169());
   }

   private static boolean isTrap(class_1299<?> type) {
      return type == class_1299.field_6131 || type == class_1299.field_6126;
   }
}
