package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_3532;
import net.minecraft.class_5498;

public class Freelook extends Module {
   private static final double ENTER = 0.28;
   private static final double EXIT = 0.22;
   private static final double HEAD = 0.7;
   private static final double VANILLA_DISTANCE = 4.0;
   private final Setting<Integer> distance = this.add(
      new IntSetting("Distance", "Camera distance in blocks. Use the mouse wheel to change it while active.", 4, 1, 50)
   );
   private final Setting<Boolean> throughWalls = this.add(
      new BoolSetting("Through Walls", "The camera goes through blocks instead of getting pushed in front of them.", true)
   );
   private final Setting<Integer> smoothness = this.add(new IntSetting("Smoothness", "How smooth the zoom with the mouse wheel feels.", 6, 0, 10));
   private final Setting<Boolean> animation = this.add(
      new BoolSetting("Animation", "The camera glides smoothly out of your head when Freelook turns on, and back when it turns off.", true)
   );
   private float yaw;
   private float pitch;
   private double shownDistance;
   private long lastFrame;
   private class_5498 previous;
   private long enterAt;
   private double enterFrom;
   private boolean exiting;
   private long exitAt;
   private float exitYaw;
   private float exitPitch;
   private double exitFrom;

   public Freelook() {
      super("Freelook", "Look around your player without turning. The camera goes through walls and zooms with the mouse wheel.", Category.VISUALS);
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 == null) {
         this.setEnabledSilently(false);
      } else {
         if (this.exiting) {
            this.exiting = false;
            this.enterFrom = this.shownDistance;
         } else {
            this.yaw = mc.field_1724.method_36454();
            this.pitch = mc.field_1724.method_36455();
            this.previous = mc.field_1690.method_31044();
            this.enterFrom = this.previous == class_5498.field_26664 ? 0.0 : 4.0;
         }

         this.enterAt = System.nanoTime();
         this.lastFrame = 0L;
         if (!this.animation.get()) {
            this.shownDistance = (double)this.distance.get().intValue();
            mc.field_1690.method_31043(class_5498.field_26665);
         } else {
            this.shownDistance = this.enterFrom;
            if (this.enterFrom >= 0.7) {
               mc.field_1690.method_31043(class_5498.field_26665);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.animation.get() && mc.field_1724 != null && mc.field_1687 != null && this.previous != null) {
         this.exiting = true;
         this.exitAt = System.nanoTime();
         this.exitYaw = this.yaw;
         this.exitPitch = this.pitch;
         this.exitFrom = this.shownDistance;
      } else {
         this.finishExit();
      }
   }

   private void finishExit() {
      this.exiting = false;
      if (this.previous != null) {
         mc.field_1690.method_31043(this.previous);
      }

      this.previous = null;
   }

   @Override
   public void onGameLeft() {
      this.setEnabled(false);
      if (this.exiting) {
         this.finishExit();
      }
   }

   public boolean exiting() {
      if (this.exiting && (mc.field_1724 == null || mc.field_1687 == null)) {
         this.finishExit();
      }

      return this.exiting;
   }

   public void turn(double dx, double dy) {
      this.yaw += (float)dx * 0.15F;
      this.pitch = class_3532.method_15363(this.pitch + (float)dy * 0.15F, -90.0F, 90.0F);
   }

   public void scroll(double amount) {
      this.distance.set(this.distance.get() - (int)Math.signum(amount));
   }

   private static double easeOut(double t) {
      double f = 1.0 - Math.max(0.0, Math.min(1.0, t));
      return 1.0 - f * f * f;
   }

   private static double easeInOut(double t) {
      t = Math.max(0.0, Math.min(1.0, t));
      return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
   }

   private double exitProgress() {
      return easeInOut((double)(System.nanoTime() - this.exitAt) / 1.0E9 / 0.22);
   }

   public double distance() {
      long now = System.nanoTime();
      double dt = this.lastFrame == 0L ? 0.0 : Math.min(0.1, (double)(now - this.lastFrame) / 1.0E9);
      this.lastFrame = now;
      if (this.exiting) {
         double p = this.exitProgress();
         double end = this.previous == class_5498.field_26664 ? 0.0 : 4.0;
         this.shownDistance = class_3532.method_16436(p, this.exitFrom, end);
         if (this.previous == class_5498.field_26664 && this.shownDistance < 0.7) {
            mc.field_1690.method_31043(class_5498.field_26664);
         }

         if (p >= 1.0) {
            this.finishExit();
         }

         return this.shownDistance;
      } else {
         double target = (double)this.distance.get().intValue();
         double age = (double)(now - this.enterAt) / 1.0E9;
         if (this.animation.get() && age < 0.28) {
            this.shownDistance = class_3532.method_16436(easeOut(age / 0.28), this.enterFrom, target);
         } else if (this.smoothness.get() == 0) {
            this.shownDistance = target;
         } else {
            this.shownDistance = this.shownDistance
               + (target - this.shownDistance) * (1.0 - Math.exp(-dt * (22.0 - (double)this.smoothness.get().intValue() * 2.0)));
         }

         if (this.shownDistance >= 0.7 && mc.field_1690.method_31044() == class_5498.field_26664) {
            mc.field_1690.method_31043(class_5498.field_26665);
         }

         return this.shownDistance;
      }
   }

   public boolean throughWalls() {
      return this.throughWalls.get();
   }

   public float yaw() {
      return this.exiting && mc.field_1724 != null ? class_3532.method_17821((float)this.exitProgress(), this.exitYaw, mc.field_1724.method_36454()) : this.yaw;
   }

   public float pitch() {
      return this.exiting && mc.field_1724 != null
         ? class_3532.method_16439((float)this.exitProgress(), this.exitPitch, mc.field_1724.method_36455())
         : this.pitch;
   }

   public float hint() {
      if (!this.exiting && this.isActive()) {
         double age = (double)(System.nanoTime() - this.enterAt) / 1.0E9;
         return age < 0.2 ? (float)easeOut(age / 0.2) : 1.0F;
      } else {
         return 0.0F;
      }
   }

   @Override
   public String getInfoString() {
      return this.distance.get() + "m";
   }
}
