package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class AimAssist extends Module {
   private final Setting<Integer> range = this.add(new IntSetting("Range", "Players within this many blocks are aimed at.", 5, 2, 8));
   private final Setting<Integer> fov = this.add(new IntSetting("FOV", "Only players inside this angle around your crosshair, in degrees.", 60, 10, 180));
   private final Setting<Integer> speed = this.add(
      new IntSetting("Speed", "How fast your view is pulled to the target. Low feels like a soft nudge.", 30, 1, 100)
   );
   private final Setting<Boolean> clicking = this.add(new BoolSetting("Only While Clicking", "Only aims while you hold the attack key.", true));
   private final Setting<Boolean> vertical = this.add(new BoolSetting("Vertical", "Also moves your view up and down, not only sideways.", true));
   private final Setting<Boolean> walls = this.add(new BoolSetting("Through Walls", "Also aims at players you cannot see.", false));
   private final Setting<Boolean> weaponOnly = this.add(new BoolSetting("Weapons Only", "Only aims while you hold a sword, axe, mace or trident.", false));
   private long lastFrame;
   private class_1657 target;

   public AimAssist() {
      super("Aim Assist", "Smoothly pulls your view onto the closest player in front of you.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.target = null;
      this.lastFrame = 0L;
   }

   @Override
   public void onRender(Render3D renderer) {
      long now = System.nanoTime();
      float dt = this.lastFrame == 0L ? 0.0F : Math.min(0.05F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      this.target = null;
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null && !mc.field_1724.method_7325()) {
         if (!SoberClient.MODULES.get(Freecam.class).isActive() && !SoberClient.MODULES.get(Freelook.class).isActive()) {
            if (!this.clicking.get() || mc.field_1690.field_1886.method_1434()) {
               if (!this.weaponOnly.get() || CombatItems.isWeapon(mc.field_1724.method_6047())) {
                  this.target = this.findTarget();
                  if (this.target != null && !(dt <= 0.0F)) {
                     class_243 eye = mc.field_1724.method_33571();
                     class_243 aim = aimPoint(this.target.method_5829(), eye);
                     double dx = aim.field_1352 - eye.field_1352;
                     double dy = aim.field_1351 - eye.field_1351;
                     double dz = aim.field_1350 - eye.field_1350;
                     float wantYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
                     float wantPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
                     float pull = 1.0F - (float)Math.exp((double)((float)(-this.speed.get()) * 0.25F * dt));
                     float yawStep = class_3532.method_15393(wantYaw - mc.field_1724.method_36454()) * pull;
                     float pitchStep = this.vertical.get() ? (wantPitch - mc.field_1724.method_36455()) * pull : 0.0F;
                     mc.field_1724.method_5872((double)yawStep / 0.15, (double)pitchStep / 0.15);
                  }
               }
            }
         }
      }
   }

   private class_1657 findTarget() {
      class_1657 best = null;
      double bestAngle = (double)this.fov.get().intValue() / 2.0;
      double max = (double)(this.range.get() * this.range.get());
      class_243 eye = mc.field_1724.method_33571();

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724
            && player.method_5805()
            && !player.method_7325()
            && !player.method_5767()
            && !(player.method_5858(mc.field_1724) > max)
            && (this.walls.get() || mc.field_1724.method_6057(player))) {
            class_243 aim = aimPoint(player.method_5829(), eye);
            float yaw = (float)Math.toDegrees(Math.atan2(aim.field_1350 - eye.field_1350, aim.field_1352 - eye.field_1352)) - 90.0F;
            double angle = (double)Math.abs(class_3532.method_15393(yaw - mc.field_1724.method_36454()));
            if (angle < bestAngle) {
               bestAngle = angle;
               best = player;
            }
         }
      }

      return best;
   }

   private static class_243 aimPoint(class_238 box, class_243 eye) {
      class_238 inner = box.method_35580(box.method_17939() * 0.2, box.method_17940() * 0.15, box.method_17941() * 0.2);
      return new class_243(
         class_3532.method_15350(eye.field_1352, inner.field_1323, inner.field_1320),
         class_3532.method_15350(eye.field_1351, inner.field_1322, inner.field_1325),
         class_3532.method_15350(eye.field_1350, inner.field_1321, inner.field_1324)
      );
   }

   @Override
   public String getInfoString() {
      return this.target == null ? null : this.target.method_7334().name();
   }
}
