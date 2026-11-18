package dev.violet.modules;

import dev.violet.mixin.InteractionManagerAccessor;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_2338;

public class SpeedMine extends Module {
   private final Setting<SpeedMine.Mode> mode = this.add(
      new EnumSetting<>(
         "Mode",
         "Damage mines every block faster by the speed below. Early Finish breaks the block as soon as it reaches the progress below. Haste gives you a Haste effect that only you can see.",
         SpeedMine.Mode.Damage
      )
   );
   private final Setting<Integer> speed = this.add(new IntSetting("Speed", "How much faster you mine in percent. 100 is normal speed.", 140, 100, 300))
      .visible(() -> this.mode.get() == SpeedMine.Mode.Damage);
   private final Setting<Integer> finishAt = this.add(new IntSetting("Finish At", "Progress in percent at which the block breaks.", 75, 40, 100))
      .visible(() -> this.mode.get() == SpeedMine.Mode.Finish);
   private final Setting<Integer> haste = this.add(new IntSetting("Haste Level", "Level of the Haste effect.", 2, 1, 4))
      .visible(() -> this.mode.get() == SpeedMine.Mode.Haste);
   private final Setting<Boolean> noDelay = this.add(new BoolSetting("No Break Delay", "Removes the short pause before you can start on the next block.", true));
   private class_2338 lastPos;
   private float lastProgress;
   private boolean gaveHaste;

   public SpeedMine() {
      super("Speed Mine", "Mines blocks faster. Servers with an anticheat can flag it, so keep the speed low there.", Category.MOVEMENT);
   }

   @Override
   public void onDisable() {
      this.removeHaste();
      this.lastPos = null;
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         InteractionManagerAccessor manager = (InteractionManagerAccessor)mc.field_1761;
         if (this.noDelay.get()) {
            manager.violet$setBreakingCooldown(0);
         }

         if (this.mode.get() != SpeedMine.Mode.Haste) {
            this.removeHaste();
            if (!manager.violet$breaking()) {
               this.lastPos = null;
            } else {
               class_2338 pos = manager.violet$breakingPos();
               float progress = manager.violet$breakingProgress();
               if (this.mode.get() == SpeedMine.Mode.Finish) {
                  if (progress >= (float)this.finishAt.get().intValue() / 100.0F && progress < 1.0F) {
                     manager.violet$setBreakingProgress(1.0F);
                  }
               } else if (pos != null && pos.equals(this.lastPos) && progress > this.lastProgress) {
                  float boosted = progress + (progress - this.lastProgress) * ((float)this.speed.get().intValue() / 100.0F - 1.0F);
                  manager.violet$setBreakingProgress(Math.min(1.0F, boosted));
                  progress = Math.min(1.0F, boosted);
               }

               this.lastPos = pos == null ? null : pos.method_10062();
               this.lastProgress = progress;
            }
         } else {
            class_1293 current = mc.field_1724.method_6112(class_1294.field_5917);
            if (current == null || current.method_5584() < 40 && this.gaveHaste) {
               mc.field_1724.method_6092(new class_1293(class_1294.field_5917, 200, this.haste.get() - 1, false, false, false));
               this.gaveHaste = true;
            }
         }
      }
   }

   private void removeHaste() {
      if (this.gaveHaste && mc.field_1724 != null) {
         mc.field_1724.method_6016(class_1294.field_5917);
         this.gaveHaste = false;
      }
   }

   @Override
   public String getInfoString() {
      return switch ((SpeedMine.Mode)this.mode.get()) {
         case Damage -> this.speed.get() + "%";
         case Finish -> this.finishAt.get() + "%";
         case Haste -> "Haste " + this.haste.get();
      };
   }

   public static enum Mode {
      Damage("Damage"),
      Finish("Early Finish"),
      Haste("Haste");

      private final String title;

      private Mode(String title) {
         this.title = title;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
