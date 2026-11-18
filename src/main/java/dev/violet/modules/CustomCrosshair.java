package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.CrosshairRenderer;
import dev.violet.gui.Theme;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1309;
import net.minecraft.class_332;

public class CustomCrosshair extends Module {
   private final Setting<CustomCrosshair.Style> style = this.add(
      new EnumSetting<>(
         "Style",
         "Palm is the SoberClient logo, Island Sun a dot with rays, Wave a wavy ring, SoberClient Y three arms, plus classic Cross, Ring, Brackets and Dot.",
         CustomCrosshair.Style.Palm
      )
   );
   private final Setting<CustomCrosshair.Tint> tint = this.add(
      new EnumSetting<>("Color Mode", "Theme uses your GUI accent color, also rainbow and galaxy. Custom uses your own color.", CustomCrosshair.Tint.Theme)
   );
   private final Setting<Color> color = this.add(new ColorSetting("Color", "Color of the crosshair.", new Color(95, 227, 238, 255)))
      .visible(() -> this.tint.get() == CustomCrosshair.Tint.Custom);
   private final Setting<Integer> size = this.add(new IntSetting("Size", "Size of the crosshair in screen pixels.", 9, 3, 30));
   private final Setting<Integer> thickness = this.add(new IntSetting("Thickness", "Line thickness in screen pixels.", 2, 1, 6));
   private final Setting<Integer> gap = this.add(new IntSetting("Gap", "Space between the center and the lines.", 3, 0, 15))
      .visible(() -> this.style.get() != CustomCrosshair.Style.Palm && this.style.get() != CustomCrosshair.Style.Dot);
   private final Setting<Integer> opacity = this.add(new IntSetting("Opacity", "How visible the crosshair is in percent.", 100, 20, 100));
   private final Setting<Boolean> outline = this.add(new BoolSetting("Outline", "Thin dark border so the crosshair is visible on any background.", true));
   private final Setting<Boolean> glow = this.add(new BoolSetting("Glow", "Soft glow in the crosshair color.", true));
   private final Setting<Boolean> dynamic = this.add(new BoolSetting("Dynamic", "The lines spread a little while you move and right after you attack.", false))
      .visible(() -> this.style.get() != CustomCrosshair.Style.Palm && this.style.get() != CustomCrosshair.Style.Dot);
   private final Setting<CustomCrosshair.Indicator> indicator = this.add(
      new EnumSetting<>(
         "Attack Indicator",
         "Animated draws a small clean bar under the crosshair that fills up while your sword or axe recharges and glows when a full hit is ready. Vanilla keeps the Minecraft bar.",
         CustomCrosshair.Indicator.Animated
      )
   );
   private final Setting<Boolean> hitAnimation = this.add(
      new BoolSetting("Hit Animation", "The crosshair pops and an X flies outwards when you hit a player or mob.", true)
   );
   private final Setting<Boolean> freeCamera = this.add(
      new BoolSetting("Freecam & Freelook", "Also shows the crosshair while Freecam or Freelook is on.", true)
   );
   private float spread;
   private float lastCooldown = 1.0F;
   private long hitAt;
   private long readyAt;
   private boolean wasCharging;
   private float barProgress = 1.0F;
   private float barShown;
   private long lastFrame = System.nanoTime();

   public CustomCrosshair() {
      super(
         "Custom Crosshair",
         "Replaces the Minecraft crosshair with SoberClient designs in your theme color, with an animated attack indicator.",
         Category.VISUALS
      );
   }

   private static CustomCrosshair instance() {
      return SoberClient.MODULES.get(CustomCrosshair.class);
   }

   public static boolean render(class_332 context) {
      CustomCrosshair crosshair = instance();
      if (crosshair != null && crosshair.isActive() && mc.field_1724 != null) {
         crosshair.draw(context);
         return true;
      } else {
         return false;
      }
   }

   public static boolean showsInFreeCamera() {
      CustomCrosshair crosshair = instance();
      if (crosshair != null && crosshair.isActive() && crosshair.freeCamera.get()) {
         Freecam freecam = SoberClient.MODULES.get(Freecam.class);
         Freelook freelook = SoberClient.MODULES.get(Freelook.class);
         return freecam != null && freecam.isActive() || freelook != null && freelook.isActive();
      } else {
         return false;
      }
   }

   public static boolean hidesVanillaIndicator() {
      CustomCrosshair crosshair = instance();
      return crosshair != null && crosshair.isActive() && crosshair.indicator.get() != CustomCrosshair.Indicator.Vanilla;
   }

   private int baseColor() {
      return switch ((CustomCrosshair.Tint)this.tint.get()) {
         case Theme -> Theme.accent();
         case Custom -> 0xFF000000 | this.color.get().packed();
         case White -> -1;
      };
   }

   private void draw(class_332 context) {
      long now = System.nanoTime();
      float delta = Math.min(0.1F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      float cooldown = mc.field_1724.method_7261(mc.method_61966().method_60637(true));
      float target = 0.0F;
      if (this.dynamic.get()) {
         double speed = Math.hypot(mc.field_1724.method_18798().field_1352, mc.field_1724.method_18798().field_1350) * 20.0;
         target += (float)Math.min(4.0, speed * 0.6);
         target += (1.0F - cooldown) * 4.0F;
         if (!mc.field_1724.method_24828()) {
            target += 2.0F;
         }
      }

      this.spread = this.spread + (target - this.spread) * (1.0F - (float)Math.exp((double)(-delta * 14.0F)));
      int color = this.baseColor();
      float visible = (float)this.opacity.get().intValue() / 100.0F;
      long millis = System.currentTimeMillis();
      long sinceHit = millis - this.hitAt;
      int shown = this.size.get();
      if (this.hitAnimation.get() && sinceHit < 220L) {
         float t = 1.0F - (float)sinceHit / 220.0F;
         shown = Math.round((float)this.size.get().intValue() * (1.0F + 0.35F * t * t));
      }

      CrosshairRenderer.draw(
         context,
         this.style.get().shape,
         shown,
         this.thickness.get(),
         this.gap.get() + Math.round(this.spread),
         this.outline.get(),
         this.glow.get(),
         color,
         visible
      );
      if (this.indicator.get() == CustomCrosshair.Indicator.Animated) {
         this.drawIndicator(context, cooldown, color, visible, millis, delta);
      }

      if (this.hitAnimation.get() && sinceHit < 300L) {
         float t = (float)sinceHit / 300.0F;
         float ease = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
         CrosshairRenderer.draw(
            context,
            CrosshairRenderer.Shape.HIT,
            this.size.get() + 3 + Math.round(ease * 7.0F),
            Math.max(1, this.thickness.get()),
            0,
            this.outline.get(),
            false,
            -1,
            visible * (1.0F - t)
         );
      }
   }

   private void drawIndicator(class_332 context, float cooldown, int color, float visible, long millis, float delta) {
      int offset = Math.max(
         9, Math.round((float)this.size.get().intValue() * 0.9F + (float)this.gap.get().intValue() + (float)this.thickness.get().intValue() + 5.0F)
      );
      int width = Math.max(16, Math.round((float)this.size.get().intValue() * 2.2F + (float)this.gap.get().intValue() * 2.0F + 4.0F));
      int height = Math.max(2, Math.min(4, this.thickness.get() + 1));
      boolean charging = cooldown < 1.0F;
      if (charging && !this.wasCharging) {
         this.barProgress = cooldown;
      }

      this.barProgress = this.barProgress + (cooldown - this.barProgress) * (1.0F - (float)Math.exp((double)(-delta * 30.0F)));
      if (cooldown < this.barProgress) {
         this.barProgress = cooldown;
      }

      boolean var10000;
      label68: {
         if (mc.field_1692 instanceof class_1309 living && living.method_5805()) {
            var10000 = true;
            break label68;
         }

         var10000 = false;
      }

      boolean target = var10000;
      float show = !charging && !target ? 0.0F : 1.0F;
      this.barShown = this.barShown + (show - this.barShown) * (1.0F - (float)Math.exp((double)(-delta * (show > this.barShown ? 22.0F : 9.0F))));
      if (charging) {
         this.wasCharging = true;
      } else if (this.wasCharging) {
         this.wasCharging = false;
         this.readyAt = millis;
      }

      long sinceReady = millis - this.readyAt;
      float flash = sinceReady < 320L ? 1.0F - (float)sinceReady / 320.0F : 0.0F;
      float shown = Math.max(this.barShown, flash);
      if (!(shown <= 0.01F)) {
         float halo = charging ? 0.0F : Math.max(flash, target ? 0.45F + 0.3F * (float)Math.sin((double)millis / 160.0) : 0.0F);
         CrosshairRenderer.bar(context, offset, width, height, charging ? this.barProgress : 1.0F, color, this.outline.get(), visible * shown, halo);
      }
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null) {
         float cooldown = mc.field_1724.method_7261(0.0F);
         if (this.lastCooldown > 0.85F && cooldown < 0.3F && mc.field_1692 != null) {
            this.hitAt = System.currentTimeMillis();
         }

         this.lastCooldown = cooldown;
      }
   }

   @Override
   public String getInfoString() {
      return this.style.get().toString();
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   public static enum Indicator {
      Animated,
      Vanilla,
      Off;
   }

   public static enum Style {
      Palm("Palm", CrosshairRenderer.Shape.PALM),
      Sun("Island Sun", CrosshairRenderer.Shape.SUN),
      Wave("Wave", CrosshairRenderer.Shape.WAVE),
      Y("SoberClient Y", CrosshairRenderer.Shape.Y),
      Cross("Cross", CrosshairRenderer.Shape.CROSS),
      Ring("Ring", CrosshairRenderer.Shape.RING),
      Brackets("Brackets", CrosshairRenderer.Shape.BRACKETS),
      Dot("Dot", CrosshairRenderer.Shape.DOT);

      private final String title;
      private final CrosshairRenderer.Shape shape;

      private Style(String title, CrosshairRenderer.Shape shape) {
         this.title = title;
         this.shape = shape;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }

   public static enum Tint {
      Theme,
      Custom,
      White;
   }
}
