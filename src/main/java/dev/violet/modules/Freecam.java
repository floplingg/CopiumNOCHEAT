package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_10185;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_5498;

public class Freecam extends Module {
   private static final class_10185 SNEAKING = new class_10185(false, false, false, false, false, true, false);
   private final Setting<Integer> speed = this.add(
      new IntSetting("Speed", "Flying speed in blocks per second. Use the mouse wheel to change it while active.", 20, 1, 200)
   );
   private final Setting<Boolean> scrollSpeed = this.add(new BoolSetting("Scroll Speed", "Change the speed with the mouse wheel while flying.", true));
   private final Setting<Boolean> lookDirection = this.add(new BoolSetting("Fly Where You Look", "Moving forward also goes up or down with your view.", false));
   private final Setting<Integer> smoothness = this.add(new IntSetting("Smoothness", "0 stops instantly, higher values glide a little.", 0, 0, 10));
   private final Setting<Boolean> showAll = this.add(
      new BoolSetting("Show All Chunks", "Renders every loaded chunk, also behind walls, so nothing pops in while flying.", true)
   );
   private final Setting<Boolean> sneak = this.add(new BoolSetting("Sneak", "Your player sneaks while the camera flies around.", true));
   private final Setting<Boolean> hands = this.add(
      new BoolSetting("Show Hands", "Keeps your hands and what you hold, like totems, on screen while the camera flies.", true)
   );
   private class_243 pos = class_243.field_1353;
   private class_243 velocity = class_243.field_1353;
   private float yaw;
   private float pitch;
   private long lastFrame;
   private long enabledAt;
   private class_5498 previous;
   private boolean previousCulling = true;

   public Freecam() {
      super("Freecam", "Fly the camera freely while your player stays where it is.", Category.VISUALS);
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 == null) {
         this.setEnabledSilently(false);
      } else {
         this.pos = mc.field_1724.method_33571();
         this.velocity = class_243.field_1353;
         this.lastFrame = 0L;
         this.enabledAt = System.nanoTime();
         this.yaw = mc.field_1724.method_36454();
         this.pitch = mc.field_1724.method_36455();
         this.previous = mc.field_1690.method_31044();
         mc.field_1690.method_31043(class_5498.field_26665);
         if (this.showAll.get()) {
            this.previousCulling = mc.field_1730;
            mc.field_1730 = false;
         }

         if (mc.field_1769 != null) {
            mc.field_1769.method_3292();
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.previous != null) {
         mc.field_1690.method_31043(this.previous);
      }

      this.previous = null;
      if (this.showAll.get()) {
         mc.field_1730 = this.previousCulling;
      }

      if (mc.field_1769 != null) {
         mc.field_1769.method_3292();
      }
   }

   @Override
   public void onGameLeft() {
      this.setEnabled(false);
   }

   public class_243 update() {
      long now = System.nanoTime();
      double dt = this.lastFrame == 0L ? 0.0 : Math.min(0.1, (double)(now - this.lastFrame) / 1.0E9);
      this.lastFrame = now;
      class_243 target = class_243.field_1353;
      if (mc.field_1755 == null) {
         double forward = axis(mc.field_1690.field_1894.method_1434(), mc.field_1690.field_1881.method_1434());
         double strafe = axis(mc.field_1690.field_1913.method_1434(), mc.field_1690.field_1849.method_1434());
         double vertical = axis(mc.field_1690.field_1903.method_1434(), mc.field_1690.field_1832.method_1434());
         double yawRad = Math.toRadians((double)this.yaw);
         double sin = Math.sin(yawRad);
         double cos = Math.cos(yawRad);
         class_243 wish;
         if (this.lookDirection.get()) {
            double pitchRad = Math.toRadians((double)this.pitch);
            double flat = Math.cos(pitchRad);
            wish = new class_243(-sin * flat * forward + cos * strafe, -Math.sin(pitchRad) * forward + vertical, cos * flat * forward + sin * strafe);
         } else {
            wish = new class_243(-sin * forward + cos * strafe, vertical, cos * forward + sin * strafe);
         }

         if (wish.method_1027() > 1.0) {
            wish = wish.method_1029();
         }

         double blocksPerSecond = (double)this.speed.get().intValue();
         if (mc.field_1690.field_1867.method_1434()) {
            blocksPerSecond *= 2.0;
         }

         target = wish.method_1021(blocksPerSecond);
      }

      if (this.smoothness.get() == 0) {
         this.velocity = target;
      } else {
         double rate = 30.0 / (double)this.smoothness.get().intValue();
         this.velocity = this.velocity.method_1019(target.method_1020(this.velocity).method_1021(1.0 - Math.exp(-rate * dt)));
         if (target.method_1027() == 0.0 && this.velocity.method_1027() < 1.0E-4) {
            this.velocity = class_243.field_1353;
         }
      }

      this.pos = this.pos.method_1019(this.velocity.method_1021(dt));
      return this.pos;
   }

   private static double axis(boolean positive, boolean negative) {
      return positive == negative ? 0.0 : (positive ? 1.0 : -1.0);
   }

   public void turn(double dx, double dy) {
      this.yaw += (float)dx * 0.15F;
      this.pitch = class_3532.method_15363(this.pitch + (float)dy * 0.15F, -90.0F, 90.0F);
   }

   public boolean scroll(double amount) {
      if (!this.scrollSpeed.get()) {
         return false;
      } else {
         int step = this.speed.get() >= 40 ? 5 : (this.speed.get() >= 10 ? 2 : 1);
         this.speed.set(this.speed.get() + (int)Math.signum(amount) * step);
         return true;
      }
   }

   public float hint() {
      if (!this.isActive()) {
         return 0.0F;
      } else {
         float t = Math.min(1.0F, (float)(System.nanoTime() - this.enabledAt) / 1.0E9F / 0.2F);
         float f = 1.0F - t;
         return 1.0F - f * f * f;
      }
   }

   public class_10185 frozenInput() {
      return this.sneak.get() ? SNEAKING : class_10185.field_54098;
   }

   public boolean showsHands() {
      return this.isActive() && this.hands.get();
   }

   public float yaw() {
      return this.yaw;
   }

   public float pitch() {
      return this.pitch;
   }

   @Override
   public String getInfoString() {
      return this.speed.get() + " b/s";
   }
}
