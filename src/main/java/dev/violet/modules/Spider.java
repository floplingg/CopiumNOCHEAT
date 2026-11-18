package dev.violet.modules;

import dev.violet.event.Stage;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class Spider extends BusModule {
   private static final double CLIMB_VELOCITY_THRESHOLD = 0.2;
   private final NumberSetting climbSpeed = this.addSetting("climb-speed", new NumberSetting("Climb Speed", 0.2F, 0.0F, 10.0F).step(0.1F));

   public Spider() {
      super("spider", "Spider", "Allows you to climb walls like a spider.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      this.on(TickEvent.TYPE, Stage.POST, this::onPostTick);
   }

   private void onPostTick(TickEvent event) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && player.field_5976) {
         class_243 velocity = player.method_18798();
         if (!(velocity.field_1351 >= 0.2)) {
            player.method_18800(velocity.field_1352, (double)this.climbSpeed.get(), velocity.field_1350);
         }
      }
   }
}
