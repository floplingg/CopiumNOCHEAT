package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_124;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_418;

public class AutoRespawn extends Module {
   private final Setting<Integer> delay = this.add(new IntSetting("Delay", "Ticks to wait on the death screen before respawning.", 0, 0, 60));
   private final Setting<Boolean> coords = this.add(new BoolSetting("Death Coords", "Writes where you died into your chat.", true));
   private int waited;
   private boolean dead;

   public AutoRespawn() {
      super("Auto Respawn", "Respawns you right away when you die.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null) {
         if (!(mc.field_1755 instanceof class_418)) {
            this.waited = 0;
            this.dead = false;
         } else {
            if (!this.dead) {
               this.dead = true;
               if (this.coords.get()) {
                  class_2338 pos = mc.field_1724.method_24515();
                  mc.field_1705
                     .method_1743()
                     .method_1812(
                        class_2561.method_43470("[Auto Respawn] ")
                           .method_27692(class_124.field_1080)
                           .method_10852(
                              class_2561.method_43470("You died at " + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260())
                                 .method_27692(class_124.field_1068)
                           )
                     );
               }
            }

            if (this.waited++ >= this.delay.get()) {
               mc.field_1724.method_7331();
               mc.method_1507(null);
            }
         }
      }
   }
}
