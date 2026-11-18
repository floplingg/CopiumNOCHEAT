package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1657;
import net.minecraft.class_2561;

public class AutoLog extends Module {
   private final Setting<Integer> range = this.add(
      new IntSetting("Range", "Logs out when another player comes within this many blocks. 0 turns it off.", 32, 0, 128) {
         @Override
         public String display() {
            return this.value == 0 ? "Off" : this.value + " blocks";
         }
      }
   );
   private final Setting<Integer> health = this.add(new IntSetting("Health", "Also logs out at or below this many hearts. 0 turns it off.", 0, 0, 20) {
      @Override
      public String display() {
         return this.value == 0 ? "Off" : this.value + " hearts";
      }
   });
   private final Setting<Boolean> autoOff = this.add(new BoolSetting("Auto Off", "Turns Auto Log off after it logged you out, so you can join again.", true));

   public AutoLog() {
      super("Auto Log", "Leaves the server when a player comes near you or your health gets low.", Category.DONUT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.method_1562() != null && !mc.method_1542() && mc.field_1724.field_6012 >= 40) {
         String reason = null;
         if (this.health.get() > 0 && mc.field_1724.method_6032() + mc.field_1724.method_6067() <= (float)this.health.get().intValue() * 2.0F) {
            reason = "Your health went down to " + Math.round(mc.field_1724.method_6032() / 2.0F) + " hearts.";
         } else if (this.range.get() > 0) {
            class_1657 near = this.nearest();
            if (near != null) {
               reason = near.method_7334().name() + " came within " + Math.round(near.method_5739(mc.field_1724)) + " blocks.";
            }
         }

         if (reason != null) {
            if (this.autoOff.get()) {
               this.setEnabledSilently(false);
               SoberClient.CONFIG.save();
            }

            AutoReconnect.skipNext();
            mc.method_1562().method_48296().method_10747(class_2561.method_43470("[Auto Log] " + reason));
         }
      }
   }

   private class_1657 nearest() {
      class_1657 best = null;
      double bestDistance = (double)(this.range.get() * this.range.get());

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && !player.method_7325() && mc.method_1562().method_2871(player.method_5667()) != null) {
            double distance = player.method_5858(mc.field_1724);
            if (distance <= bestDistance) {
               bestDistance = distance;
               best = player;
            }
         }
      }

      return best;
   }

   @Override
   public String getInfoString() {
      return this.range.get() > 0 ? this.range.get() + "m" : null;
   }
}
