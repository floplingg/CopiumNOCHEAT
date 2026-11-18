package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1656;
import net.minecraft.class_243;

public class FlySpeed extends Module {
   private static final float DEFAULT_FLY_SPEED = 0.05F;
   private final Setting<Boolean> elytra;
   private final Setting<Integer> elytraSpeed;
   private final Setting<Integer> acceleration;
   private final Setting<Boolean> holdForward;
   private final Setting<Boolean> creative;
   private final Setting<Integer> creativeSpeed;
   private boolean changedFlySpeed;

   public FlySpeed() {
      super("Fly Speed", "Fly faster with an elytra and in creative mode.", Category.MOVEMENT);
      this.group("Elytra");
      this.elytra = this.add(new BoolSetting("Elytra Boost", "Pushes you forward while gliding with an elytra.", true));
      this.elytraSpeed = this.add(new IntSetting("Elytra Speed", "Top speed while gliding, in blocks per second.", 45, 15, 150)).visible(this.elytra::get);
      this.acceleration = this.add(new IntSetting("Acceleration", "How fast you reach the top speed.", 6, 1, 30)).visible(this.elytra::get);
      this.holdForward = this.add(new BoolSetting("Hold Forward", "Only boosts while you hold the forward key.", true)).visible(this.elytra::get);
      this.group("Creative");
      this.creative = this.add(new BoolSetting("Creative Flight", "Faster flying in creative and spectator mode.", true));
      this.creativeSpeed = this.add(new IntSetting("Creative Speed", "Creative flying speed in percent.", 250, 100, 1000)).visible(this.creative::get);
   }

   @Override
   public void onDisable() {
      this.restoreFlySpeed();
   }

   @Override
   public void onGameLeft() {
      this.changedFlySpeed = false;
   }

   private void restoreFlySpeed() {
      if (this.changedFlySpeed && mc.field_1724 != null) {
         mc.field_1724.method_31549().method_7248(0.05F);
         this.changedFlySpeed = false;
      }
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null) {
         class_1656 abilities = mc.field_1724.method_31549();
         if (this.creative.get() && abilities.field_7478) {
            abilities.method_7248(0.05F * (float)this.creativeSpeed.get().intValue() / 100.0F);
            this.changedFlySpeed = true;
         } else {
            this.restoreFlySpeed();
         }

         if (this.elytra.get() && mc.field_1724.method_6128()) {
            if (!this.holdForward.get() || mc.field_1690.field_1894.method_1434()) {
               class_243 velocity = mc.field_1724.method_18798();
               double max = (double)this.elytraSpeed.get().intValue() / 20.0;
               double speed = velocity.method_1033();
               if (!(speed >= max)) {
                  class_243 boosted = velocity.method_1019(mc.field_1724.method_5720().method_1021((double)this.acceleration.get().intValue() / 100.0));
                  if (boosted.method_1033() > max) {
                     boosted = boosted.method_1029().method_1021(max);
                  }

                  mc.field_1724.method_18799(boosted);
               }
            }
         }
      }
   }

   @Override
   public String getInfoString() {
      return mc.field_1724 != null && mc.field_1724.method_6128() && this.elytra.get() ? this.elytraSpeed.get() + " bps" : null;
   }
}
