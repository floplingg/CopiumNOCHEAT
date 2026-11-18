package dev.violet.modules;

import dev.violet.event.Stage;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.ModeSetting;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class AutoJump extends BusModule {
   private final ModeSetting mode = this.addSetting("mode", new ModeSetting("Mode", "Jump", "Jump", "LowHop"));
   private final ModeSetting jumpIf = this.addSetting("jump-if", new ModeSetting("Jump If", "Always", "Sprinting", "Walking", "Always"));
   private final NumberSetting velocityHeight = this.addSetting("velocity-height", new NumberSetting("Velocity Height", 0.25F, 0.0F, 2.0F).step(0.05F));

   public AutoJump() {
      super("auto-jump", "Auto Jump", "Automatically jumps.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      this.on(TickEvent.TYPE, Stage.PRE, this::onPreTick);
   }

   private void onPreTick(TickEvent event) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && player.method_24828() && !player.method_5715() && this.shouldJump(player)) {
         if (this.mode.is("Jump")) {
            player.method_6043();
         } else {
            class_243 velocity = player.method_18798();
            player.method_18800(velocity.field_1352, (double)this.velocityHeight.get(), velocity.field_1350);
         }
      }
   }

   private boolean shouldJump(class_746 player) {
      boolean moving = player.field_6250 != 0.0F || player.field_6212 != 0.0F;
      String var3 = this.jumpIf.get();

      return switch (var3) {
         case "Sprinting" -> player.method_5624() && moving;
         case "Walking" -> moving;
         default -> true;
      };
   }
}
