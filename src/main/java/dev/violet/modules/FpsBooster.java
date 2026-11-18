package dev.violet.modules;

import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.module.ModuleRegistry;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.NumberSetting;
import dev.violet.util.MobilePerformance;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_4063;
import net.minecraft.class_4066;

public final class FpsBooster extends BusModule {
   private static volatile boolean optimizing;
   private static volatile boolean mobileEffectsOptimizing;
   private static volatile boolean lowEndActive;
   private final BooleanSetting optimize = this.addSetting("optimize-client", new BooleanSetting("Optimize Client", true));
   private final BooleanSetting mobileEffects = this.addSetting("mobile-effects", new BooleanSetting("Optimize Mobile Effects", true));
   private final BooleanSetting lowEnd = this.addSetting("low-end-mode", new BooleanSetting("Low-End Mode", false));
   private final BooleanSetting frustumCulling = this.addSetting("frustum-culling", new BooleanSetting("Frustum Culling", true));
   private final NumberSetting scanBudget = this.addSetting("scan-budget-ms", new NumberSetting("Scan Budget ms", 2.0F, 0.5F, 8.0F).step(0.5F));
   private FpsBooster.Saved saved;

   public FpsBooster() {
      super(
         "fps-booster",
         "FPS Booster",
         "Reduces background work and image memory while keeping effects and features. Low-End Mode makes the game run on weak PCs. Disable to compare FPS.",
         ModuleCategory.CLIENT
      );
      this.optimize.onChange(this::updateOptimizationState);
      this.mobileEffects.onChange(this::updateOptimizationState);
      this.lowEnd.onChange(this::updateOptimizationState);
      this.mobileEffects.visibleWhen(() -> MobilePerformance.isAndroid() && this.optimize.get());
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> this.restoreForShutdown(client));
   }

   public static boolean optimizationsEnabled() {
      return optimizing;
   }

   public static boolean mobileEffectsEnabled() {
      return mobileEffectsOptimizing;
   }

   public static boolean lowEnd() {
      return lowEndActive;
   }

   @Override
   public boolean enabledByDefault() {
      return MobilePerformance.isAndroid();
   }

   @Override
   public void onEnable() {
      this.updateOptimizationState();
   }

   @Override
   public void onDisable() {
      optimizing = false;
      mobileEffectsOptimizing = false;
      this.updateLowEnd(false);
   }

   private void updateOptimizationState() {
      optimizing = this.isEnabled() && this.optimize.get();
      mobileEffectsOptimizing = optimizing && this.mobileEffects.get();
      this.updateLowEnd(this.isEnabled() && this.lowEnd.get());
   }

   private void updateLowEnd(boolean wanted) {
      class_310 client = class_310.method_1551();
      if (client != null && client.field_1690 != null) {
         client.execute(() -> this.applyLowEnd(client, wanted));
      }
   }

   private void applyLowEnd(class_310 client, boolean wanted) {
      if (wanted != lowEndActive) {
         class_315 options = client.field_1690;
         if (wanted) {
            this.saved = FpsBooster.Saved.of(options);
            options.method_42528().method_41748(class_4063.field_18162);
            options.method_42475().method_41748(class_4066.field_18199);
            options.method_42435().method_41748(false);
            options.method_41792().method_41748(false);
            options.method_41805().method_41748(0);
            options.method_42517().method_41748(Math.min(this.saved.entityDistance(), 0.75));
         } else if (this.saved != null) {
            this.saved.restore(options);
            this.saved = null;
         }

         lowEndActive = wanted;
         options.method_1640();
      }
   }

   private void restoreForShutdown(class_310 client) {
      if (lowEndActive && this.saved != null && client.field_1690 != null) {
         this.saved.restore(client.field_1690);
         client.field_1690.method_1640();
      }
   }

   public static boolean frustumCulling() {
      FpsBooster module = ModuleRegistry.get(FpsBooster.class);
      return module == null || module.frustumCulling.get();
   }

   public static long scanBudgetNanos() {
      FpsBooster module = ModuleRegistry.get(FpsBooster.class);
      return module == null ? 2000000L : (long)(module.scanBudget.get() * 1000000.0F);
   }

   private static record Saved(class_4063 clouds, class_4066 particles, boolean entityShadows, boolean smoothLighting, int biomeBlend, double entityDistance) {
      static FpsBooster.Saved of(class_315 options) {
         return new FpsBooster.Saved(
            (class_4063)options.method_42528().method_41753(),
            (class_4066)options.method_42475().method_41753(),
            (Boolean)options.method_42435().method_41753(),
            (Boolean)options.method_41792().method_41753(),
            (Integer)options.method_41805().method_41753(),
            (Double)options.method_42517().method_41753()
         );
      }

      void restore(class_315 options) {
         options.method_42528().method_41748(this.clouds);
         options.method_42475().method_41748(this.particles);
         options.method_42435().method_41748(this.entityShadows);
         options.method_41792().method_41748(this.smoothLighting);
         options.method_41805().method_41748(this.biomeBlend);
         options.method_42517().method_41748(this.entityDistance);
      }
   }
}
