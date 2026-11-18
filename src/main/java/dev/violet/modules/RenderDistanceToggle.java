package dev.violet.modules;

import dev.violet.event.EventHandler;
import dev.violet.event.Stage;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_310;

public final class RenderDistanceToggle extends BusModule {
   private static final int RESTORE_AFTER_TICKS = 10;
   private static final int REARM_MARGIN = 2;
   private final NumberSetting lowDistance = this.addSetting("low-distance", new NumberSetting("Low Distance", 2.0F, 2.0F, 32.0F).step(1.0F));
   private final NumberSetting resetY = this.addSetting("reset-y", new NumberSetting("Reset Y", -1.0F, -64.0F, 320.0F).step(1.0F));
   private boolean armed = true;
   private boolean restoring;
   private int timer;
   private int savedDistance = -1;

   public RenderDistanceToggle() {
      super("render-distance-toggle", "Render Distance Toggle", "Reloads chunks when you go below a Y level.", ModuleCategory.BASE_RAID);
   }

   @Override
   public void onEnable() {
      this.armed = true;
      this.restoring = false;
      this.timer = 0;
      this.savedDistance = -1;
   }

   @Override
   public void onDisable() {
      if (this.restoring) {
         this.restore(class_310.method_1551());
      }
   }

   @EventHandler(
      stage = Stage.POST
   )
   public void onTick(TickEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int y = mc.field_1724.method_31478();
         if (!this.restoring && y > this.resetY.getInt() + 2) {
            this.armed = true;
         }

         if (this.armed && y <= this.resetY.getInt()) {
            this.armed = false;
            this.restoring = true;
            this.timer = 0;
            this.savedDistance = (Integer)mc.field_1690.method_42503().method_41753();
            apply(mc, this.lowDistance.getInt());
         } else if (this.restoring && ++this.timer >= 10) {
            this.restore(mc);
         }
      } else if (this.restoring) {
         this.restore(mc);
      }
   }

   private void restore(class_310 mc) {
      if (this.savedDistance >= 0) {
         apply(mc, this.savedDistance);
      }

      this.restoring = false;
   }

   private static void apply(class_310 mc, int chunks) {
      mc.field_1690.method_42503().method_41748(chunks);
      mc.field_1690.method_1643();
   }
}
