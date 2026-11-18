package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1268;
import net.minecraft.class_1280;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_5134;
import net.minecraft.class_9892;
import net.minecraft.class_2338.class_2339;

public class AutoCrystal extends Module {
   private final Setting<Integer> targetRange;
   private final Setting<Integer> minDamage;
   private final Setting<Integer> maxSelfDamage;
   private final Setting<Boolean> antiSuicide;
   private final Setting<Boolean> place;
   private final Setting<Integer> placeRange;
   private final Setting<Integer> placeDelay;
   private final Setting<Boolean> autoSwitch;
   private final Setting<Boolean> breakCrystals;
   private final Setting<Integer> breakRange;
   private final Setting<Integer> breakDelay;
   private final Setting<Boolean> pauseOnEat;
   private int placeTimer;
   private int breakTimer;
   private class_1657 target;

   public AutoCrystal() {
      super("Auto Crystal", "Places and breaks end crystals around the closest player.", Category.COMBAT);
      this.group("Targets");
      this.targetRange = this.add(new IntSetting("Target Range", "Players within this many blocks get targeted.", 12, 4, 20));
      this.minDamage = this.add(new IntSetting("Min Damage", "Least damage a crystal has to deal to the target, in half hearts.", 6, 0, 36));
      this.maxSelfDamage = this.add(new IntSetting("Max Self Damage", "Most damage a crystal may deal to you, in half hearts.", 8, 0, 36));
      this.antiSuicide = this.add(new BoolSetting("Anti Suicide", "Never places or breaks a crystal that would kill you.", true));
      this.group("Place");
      this.place = this.add(new BoolSetting("Place", "Places crystals on obsidian and bedrock near the target.", true));
      this.placeRange = this.add(tenths("Place Range", "How far away crystals get placed.", 45));
      this.placeDelay = this.add(new IntSetting("Place Delay", "Ticks between two placements.", 1, 0, 10));
      this.autoSwitch = this.add(new BoolSetting("Auto Switch", "Switches to crystals in your hotbar.", true));
      this.group("Break");
      this.breakCrystals = this.add(new BoolSetting("Break", "Hits crystals near the target.", true));
      this.breakRange = this.add(tenths("Break Range", "How far away crystals get hit.", 45));
      this.breakDelay = this.add(new IntSetting("Break Delay", "Ticks between two hits.", 1, 0, 10));
      this.pauseOnEat = this.add(new BoolSetting("Pause On Eat", "Does nothing while you eat or drink.", true));
   }

   private static IntSetting tenths(String name, String description, int value) {
      return new IntSetting(name, description, value, 20, 60) {
         @Override
         public String display() {
            return this.value / 10 + "." + this.value % 10 + " blocks";
         }
      };
   }

   @Override
   public void onDisable() {
      this.target = null;
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null && !mc.field_1724.method_7325()) {
         if (!this.pauseOnEat.get() || !mc.field_1724.method_6115()) {
            this.target = this.findTarget();
            if (this.target != null) {
               if (this.breakTimer > 0) {
                  this.breakTimer--;
               }

               if (this.placeTimer > 0) {
                  this.placeTimer--;
               }

               if (this.breakCrystals.get() && this.breakTimer <= 0 && this.breakCrystal()) {
                  this.breakTimer = this.breakDelay.get();
               } else {
                  if (this.place.get() && this.placeTimer <= 0 && this.placeCrystal()) {
                     this.placeTimer = this.placeDelay.get();
                  }
               }
            }
         }
      }
   }

   private class_1657 findTarget() {
      class_1657 best = null;
      double bestDistance = (double)(this.targetRange.get() * this.targetRange.get());

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && player.method_5805() && !player.method_7325() && !player.method_68878()) {
            double distance = player.method_5858(mc.field_1724);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = player;
            }
         }
      }

      return best;
   }

   private boolean safe(float self) {
      return self > (float)this.maxSelfDamage.get().intValue()
         ? false
         : !this.antiSuicide.get() || self < mc.field_1724.method_6032() + mc.field_1724.method_6067() - 1.0F;
   }

   private boolean breakCrystal() {
      double range = (double)this.breakRange.get().intValue() / 10.0;
      class_243 eye = mc.field_1724.method_33571();
      class_1511 best = null;
      float bestDamage = 0.0F;

      for (class_1511 crystal : mc.field_1687.method_8390(class_1511.class, mc.field_1724.method_5829().method_1014(range + 1.0), e -> e.method_5805())) {
         if (!(crystal.method_73189().method_1025(eye) > range * range)) {
            class_243 pos = crystal.method_73189();
            float damage = this.damage(this.target, pos);
            if (!(damage < (float)this.minDamage.get().intValue()) && this.safe(this.damage(mc.field_1724, pos)) && damage > bestDamage) {
               bestDamage = damage;
               best = crystal;
            }
         }
      }

      if (best == null) {
         return false;
      } else {
         mc.field_1761.method_2918(mc.field_1724, best);
         mc.field_1724.method_6104(class_1268.field_5808);
         return true;
      }
   }

   private boolean placeCrystal() {
      class_1268 hand;
      if (mc.field_1724.method_6079().method_31574(class_1802.field_8301)) {
         hand = class_1268.field_5810;
      } else if (mc.field_1724.method_6047().method_31574(class_1802.field_8301)) {
         hand = class_1268.field_5808;
      } else {
         if (!this.autoSwitch.get()) {
            return false;
         }

         int slot = -1;

         for (int i = 0; i < 9; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8301)) {
               slot = i;
               break;
            }
         }

         if (slot < 0) {
            return false;
         }

         mc.field_1724.method_31548().method_61496(slot);
         hand = class_1268.field_5808;
      }

      double range = (double)this.placeRange.get().intValue() / 10.0;
      int reach = (int)Math.ceil(range);
      class_243 eye = mc.field_1724.method_33571();
      class_2338 origin = mc.field_1724.method_24515();
      class_2338 best = null;
      float bestScore = -3.4028235E38F;
      class_2339 cursor = new class_2339();

      for (int x = -reach; x <= reach; x++) {
         for (int y = -reach; y <= reach; y++) {
            for (int z = -reach; z <= reach; z++) {
               cursor.method_10103(origin.method_10263() + x, origin.method_10264() + y, origin.method_10260() + z);
               if (!(class_243.method_24953(cursor).method_1025(eye) > range * range)) {
                  class_2680 state = mc.field_1687.method_8320(cursor);
                  if (state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_9987)) {
                     class_2338 above = cursor.method_10084();
                     if (mc.field_1687.method_22347(above)) {
                        class_238 space = new class_238(
                           (double)above.method_10263(),
                           (double)above.method_10264(),
                           (double)above.method_10260(),
                           (double)(above.method_10263() + 1),
                           (double)(above.method_10264() + 2),
                           (double)(above.method_10260() + 1)
                        );
                        if (mc.field_1687.method_8335(null, space).isEmpty()) {
                           class_243 pos = new class_243(
                              (double)cursor.method_10263() + 0.5, (double)(cursor.method_10264() + 1), (double)cursor.method_10260() + 0.5
                           );
                           float damage = this.damage(this.target, pos);
                           if (!(damage < (float)this.minDamage.get().intValue())) {
                              float self = this.damage(mc.field_1724, pos);
                              if (this.safe(self)) {
                                 float score = damage - self * 0.5F;
                                 if (score > bestScore) {
                                    bestScore = score;
                                    best = cursor.method_10062();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (best == null) {
         return false;
      } else {
         class_3965 hit = new class_3965(
            new class_243((double)best.method_10263() + 0.5, (double)best.method_10264() + 1.0, (double)best.method_10260() + 0.5),
            class_2350.field_11036,
            best,
            false
         );
         mc.field_1761.method_2896(mc.field_1724, hand, hit);
         mc.field_1724.method_6104(hand);
         return true;
      }
   }

   private float damage(class_1309 entity, class_243 pos) {
      double distance = Math.sqrt(entity.method_5707(pos)) / 12.0;
      if (distance > 1.0) {
         return 0.0F;
      } else {
         double exposure = (double)class_9892.method_61731(pos, entity);
         double impact = (1.0 - distance) * exposure;
         float damage = (float)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
         if (entity instanceof class_1657) {
            damage = switch (mc.field_1687.method_8407()) {
               case field_5801 -> 0.0F;
               case field_5805 -> Math.min(damage / 2.0F + 1.0F, damage);
               case field_5807 -> damage * 1.5F;
               default -> damage;
            };
         }

         class_1282 source = mc.field_1687.method_48963().method_48819(null, null);
         damage = class_1280.method_5496(entity, damage, source, (float)entity.method_6096(), (float)entity.method_45325(class_5134.field_23725));
         return Math.max(0.0F, damage);
      }
   }

   @Override
   public String getInfoString() {
      return this.target == null ? null : this.target.method_7334().name();
   }
}
