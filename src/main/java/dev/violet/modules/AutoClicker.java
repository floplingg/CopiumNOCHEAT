package dev.violet.modules;

import dev.violet.mixin.MinecraftClientAccessor;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_239;
import net.minecraft.class_239.class_240;

public class AutoClicker extends Module {
   private final Setting<Integer> minCps = this.add(new IntSetting("Min CPS", "Fewest clicks per second.", 9, 1, 20));
   private final Setting<Integer> maxCps = this.add(
      new IntSetting("Max CPS", "Most clicks per second. Every click waits a random time between the two.", 13, 1, 20)
   );
   private final Setting<Boolean> cooldown = this.add(
      new BoolSetting("Respect Cooldown", "Waits until your weapon is fully charged instead of clicking at the CPS. Best for 1.9+ combat.", false)
   );
   private final Setting<Boolean> entitiesOnly = this.add(new BoolSetting("Entities Only", "Only clicks while your crosshair is on a player or mob.", false));
   private final Setting<Boolean> weaponOnly = this.add(new BoolSetting("Weapons Only", "Only clicks while you hold a sword, axe, mace or trident.", false));
   private long nextClick;

   public AutoClicker() {
      super("Auto Clicker", "Keeps clicking while you hold the attack key. Never clicks on blocks, so mining stays normal.", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.nextClick = 0L;
   }

   @Override
   public void onRender(Render3D renderer) {
      if (mc.field_1724 != null && mc.field_1755 == null && !mc.field_1724.method_6115() && mc.field_1690.field_1886.method_1434()) {
         long now = System.nanoTime();
         if (this.nextClick == 0L) {
            this.nextClick = now + this.interval();
         } else {
            class_239 hit = mc.field_1765;
            if (hit == null || hit.method_17783() != class_240.field_1332) {
               if (!this.entitiesOnly.get() || hit != null && hit.method_17783() == class_240.field_1331) {
                  if (!this.weaponOnly.get() || CombatItems.isWeapon(mc.field_1724.method_6047())) {
                     if (this.cooldown.get()) {
                        if (mc.field_1724.method_7261(0.5F) < 0.95F) {
                           return;
                        }
                     } else if (now < this.nextClick) {
                        return;
                     }

                     ((MinecraftClientAccessor)mc).violet$doAttack();
                     this.nextClick = now + this.interval();
                  }
               }
            }
         }
      } else {
         this.nextClick = 0L;
      }
   }

   private long interval() {
      int low = Math.min(this.minCps.get(), this.maxCps.get());
      int high = Math.max(this.minCps.get(), this.maxCps.get());
      double cps = low == high ? (double)low : ThreadLocalRandom.current().nextDouble((double)low, (double)high + 1.0E-9);
      return (long)(1.0E9 / cps);
   }

   @Override
   public String getInfoString() {
      return this.cooldown.get()
         ? "Cooldown"
         : (this.minCps.get().equals(this.maxCps.get()) ? this.minCps.get() + " CPS" : this.minCps.get() + "-" + this.maxCps.get() + " CPS");
   }
}
