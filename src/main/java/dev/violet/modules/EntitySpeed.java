package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.modules.entitycontrol.EntityControlVehicleHook;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.NumberSetting;
import net.minecraft.class_1297;

public final class EntitySpeed extends BusModule {
   public static volatile EntitySpeed active;
   private final NumberSetting horizontalSpeed = this.addSetting("horizontal-speed", new NumberSetting("Horizontal Speed", 10.0F, 0.0F, 50.0F).step(0.5F));
   private final BooleanSetting onlyOnGround = this.addSetting("only-on-ground", new BooleanSetting("Only On Ground", false));
   private final BooleanSetting inWater = this.addSetting("in-water", new BooleanSetting("In Water", true));

   public EntitySpeed() {
      super("entity-speed", "Entity Speed", "Makes you go faster horizontally when riding entities.", ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      active = this;
      EntityControlVehicleHook.refresh();
   }

   @Override
   public void onDisable() {
      active = null;
      EntityControlVehicleHook.refresh();
   }

   public boolean appliesTo(class_1297 vehicle) {
      return (!this.onlyOnGround.get() || vehicle.method_24828() || vehicle.method_70987()) && (this.inWater.get() || !vehicle.method_5799());
   }

   public double horizontalSpeed() {
      return (double)this.horizontalSpeed.get();
   }
}
