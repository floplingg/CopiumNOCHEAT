package dev.violet.modules;

import dev.violet.event.Stage;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.ModeSetting;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_10255;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1941;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_310;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_3612;
import net.minecraft.class_4985;
import net.minecraft.class_5134;
import net.minecraft.class_746;

public final class Jesus extends BusModule {
   public static volatile Jesus active;
   private static final String SOLID = "Solid";
   private static final String IGNORE = "Ignore";
   private static final double ASCEND_SPEED = 0.11;
   private final BooleanSetting powderSnow = this.addSetting("powder-snow", new BooleanSetting("Powder Snow", true));
   private final ModeSetting waterMode = this.addSetting("water-mode", new ModeSetting("Water Mode", "Solid", "Solid", "Ignore"));
   private final BooleanSetting dipIfBurning = this.addSetting("dip-if-burning", new BooleanSetting("Dip If Burning", true));
   private final BooleanSetting dipOnSneakWater = this.addSetting("water-dip-on-sneak", new BooleanSetting("Water Dip On Sneak", true));
   private final BooleanSetting dipOnFallWater = this.addSetting("water-dip-on-fall", new BooleanSetting("Water Dip On Fall", true));
   private final NumberSetting dipFallHeightWater = this.addSetting(
      "water-dip-fall-height", new NumberSetting("Water Dip Fall Height", 4.0F, 1.0F, 255.0F).step(1.0F)
   );
   private final ModeSetting lavaMode = this.addSetting("lava-mode", new ModeSetting("Lava Mode", "Solid", "Solid", "Ignore"));
   private final BooleanSetting dipIfFireResistant = this.addSetting("dip-if-resistant", new BooleanSetting("Dip If Resistant", true));
   private final BooleanSetting dipOnSneakLava = this.addSetting("lava-dip-on-sneak", new BooleanSetting("Lava Dip On Sneak", true));
   private final BooleanSetting dipOnFallLava = this.addSetting("lava-dip-on-fall", new BooleanSetting("Lava Dip On Fall", true));
   private final NumberSetting dipFallHeightLava = this.addSetting(
      "lava-dip-fall-height", new NumberSetting("Lava Dip Fall Height", 4.0F, 1.0F, 255.0F).step(1.0F)
   );
   private int ascending = 10;
   private boolean inBubbleColumn;

   public Jesus() {
      super("jesus", "Jesus", "Walk on liquids and powder snow like Jesus.", ModuleCategory.MOVEMENT);
      this.dipIfBurning.visibleWhen(() -> this.waterMode.is("Solid"));
      this.dipOnSneakWater.visibleWhen(() -> this.waterMode.is("Solid"));
      this.dipOnFallWater.visibleWhen(() -> this.waterMode.is("Solid"));
      this.dipFallHeightWater.visibleWhen(() -> this.waterMode.is("Solid") && this.dipOnFallWater.get());
      this.dipIfFireResistant.visibleWhen(() -> this.lavaMode.is("Solid"));
      this.dipOnSneakLava.visibleWhen(() -> this.lavaMode.is("Solid"));
      this.dipOnFallLava.visibleWhen(() -> this.lavaMode.is("Solid"));
      this.dipFallHeightLava.visibleWhen(() -> this.lavaMode.is("Solid") && this.dipOnFallLava.get());
   }

   @Override
   public void onEnable() {
      this.inBubbleColumn = false;
      active = this;
      this.on(TickEvent.TYPE, Stage.POST, this::onPostTick);
   }

   @Override
   public void onDisable() {
      active = null;
      this.inBubbleColumn = false;
   }

   private void onPostTick(TickEvent event) {
      boolean bubbleColumn = this.inBubbleColumn;
      this.inBubbleColumn = false;
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (player != null
         && mc.field_1687 != null
         && !player.method_20232()
         && (!player.method_5799() || this.waterShouldBeSolid(player))
         && (!player.method_5771() || this.lavaShouldBeSolid(player))) {
         class_1297 moving = (class_1297)(player.method_5765() ? player.method_5854() : player);
         if (moving != null) {
            if (bubbleColumn) {
               if (mc.field_1690.field_1903.method_1434() && moving.method_18798().field_1351 < 0.11) {
                  setVelocityY(moving, 0.11);
               }
            } else if (!moving.method_5799() && !moving.method_5771()) {
               class_2680 below = mc.field_1687.method_8320(moving.method_24515().method_10074());
               boolean waterlogged = (Boolean)below.method_61767(class_2741.field_12508, false);
               if (this.ascending == 0) {
                  setVelocityY(moving, 0.11);
               } else if (this.ascending == 1 && (below.method_27852(class_2246.field_10382) || below.method_27852(class_2246.field_10164) || waterlogged)) {
                  setVelocityY(moving, 0.0);
               }

               this.ascending++;
            } else {
               setVelocityY(moving, 0.11);
               this.ascending = 0;
            }
         }
      }
   }

   private static void setVelocityY(class_1297 entity, double y) {
      class_243 v = entity.method_18798();
      entity.method_18800(v.field_1352, y, v.field_1350);
   }

   private boolean waterShouldBeSolid(class_746 player) {
      if (player.method_7325() || player.method_31549().field_7479) {
         return false;
      } else if (player.method_5854() instanceof class_10255) {
         return false;
      } else if (Fly.active != null) {
         return false;
      } else if (this.dipIfBurning.get() && player.method_5809()) {
         return false;
      } else if (this.dipOnSneakWater.get() && class_310.method_1551().field_1690.field_1832.method_1434()) {
         return false;
      } else {
         return this.dipOnFallWater.get() && player.field_6017 > (double)this.dipFallHeightWater.getInt() ? false : this.waterMode.is("Solid");
      }
   }

   private boolean lavaShouldBeSolid(class_746 player) {
      if (player.method_7325() || player.method_31549().field_7479) {
         return false;
      } else if (player.method_5854() instanceof class_4985) {
         return false;
      } else if (this.isLavaDangerous(player) && this.lavaMode.is("Solid")) {
         return true;
      } else if (this.dipOnSneakLava.get() && class_310.method_1551().field_1690.field_1832.method_1434()) {
         return false;
      } else {
         return this.dipOnFallLava.get() && player.field_6017 > (double)this.dipFallHeightLava.getInt() ? false : this.lavaMode.is("Solid");
      }
   }

   private boolean isLavaDangerous(class_746 player) {
      if (!this.dipIfFireResistant.get()) {
         return true;
      } else {
         class_1293 fireResistance = player.method_6112(class_1294.field_5918);
         return fireResistance == null ? true : !((double)fireResistance.method_5584() > 300.0 * player.method_45325(class_5134.field_51579));
      }
   }

   public boolean canWalkOnFluid(class_3610 fluid) {
      class_746 player = class_310.method_1551().field_1724;
      if (player != null && !player.method_5681()) {
         class_3611 type = fluid.method_15772();
         return (type == class_3612.field_15910 || type == class_3612.field_15909) && this.waterShouldBeSolid(player)
            ? true
            : (type == class_3612.field_15908 || type == class_3612.field_15907) && this.lavaShouldBeSolid(player);
      } else {
         return false;
      }
   }

   public class_265 collisionShape(class_1941 level, class_2680 state, class_2338 pos, class_265 shape) {
      class_3610 fluid = state.method_26227();
      if (fluid.method_15769()) {
         return shape;
      } else {
         class_310 mc = class_310.method_1551();
         if (level == mc.field_1687 && mc.method_18854()) {
            class_746 player = mc.field_1724;
            if (player == null) {
               return shape;
            } else if ((state.method_27852(class_2246.field_10382) || fluid.method_15772() == class_3612.field_15910)
               && !player.method_5799()
               && this.waterShouldBeSolid(player)
               && (double)pos.method_10264() <= player.method_23318() - 1.0) {
               return class_259.method_1077();
            } else {
               return state.method_27852(class_2246.field_10164)
                     && !player.method_5771()
                     && this.lavaShouldBeSolid(player)
                     && (this.isLavaDangerous(player) || (double)pos.method_10264() <= player.method_23318() - 1.0)
                  ? class_259.method_1077()
                  : shape;
            }
         } else {
            return shape;
         }
      }
   }

   public boolean canWalkOnPowderSnow() {
      return this.powderSnow.get();
   }

   public void markInBubbleColumn() {
      this.inBubbleColumn = true;
   }
}
