package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.KeySetting;
import dev.violet.setting.Setting;
import java.util.Locale;

public class Zoom extends Module {
   private final KeySetting key = (KeySetting)this.<Integer>add(
      new KeySetting("Zoom Key", "The key that zooms. Click and press a key. Right click removes it.", 67)
   );
   private final Setting<Zoom.Mode> mode = this.add(
      new EnumSetting<>("Mode", "Hold zooms while you hold the key. Toggle zooms in with one press and out with the next.", Zoom.Mode.Hold)
   );
   private final Setting<Integer> amount = this.add(new IntSetting("Zoom", "How far you zoom in, 4 shows things four times as big.", 4, 2, 30));
   private final Setting<Boolean> scroll = this.add(new BoolSetting("Scroll Zoom", "Use the mouse wheel while zooming to zoom further in or out.", true));
   private final Setting<Boolean> smooth = this.add(new BoolSetting("Smooth", "The zoom glides in and out instead of jumping.", true));
   private final Setting<Boolean> sensitivity = this.add(
      new BoolSetting("Lower Sensitivity", "Turns the mouse slower while zoomed, so aiming stays steady.", true)
   );
   private final Setting<Boolean> cinematic = this.add(new BoolSetting("Cinematic Camera", "Uses the smooth cinematic camera while zoomed.", false));
   private double factor = 1.0;
   private double shown;
   private boolean toggled;
   private boolean keyDown;
   private boolean zoomedBefore;
   private boolean cinematicSet;
   private boolean cinematicBefore;
   private long lastFrame;

   public Zoom() {
      super("Zoom", "Hold your zoom key (C) to zoom in like a spyglass. Scroll while zooming to zoom further.", Category.VISUALS);
      this.setEnabledSilently(true);
   }

   @Override
   public void onDisable() {
      this.toggled = false;
      this.restoreCinematic();
   }

   @Override
   public void onGameLeft() {
      this.toggled = false;
      this.restoreCinematic();
   }

   @Override
   public void onTick() {
      boolean down = this.key.isPressed();
      if (this.mode.get() == Zoom.Mode.Toggle && down && !this.keyDown) {
         this.toggled = !this.toggled;
      }

      this.keyDown = down;
      if (this.cinematic.get() && this.zooming()) {
         if (!this.cinematicSet) {
            this.cinematicBefore = mc.field_1690.field_1914;
            this.cinematicSet = true;
         }

         mc.field_1690.field_1914 = true;
      } else {
         this.restoreCinematic();
      }
   }

   private void restoreCinematic() {
      if (this.cinematicSet) {
         mc.field_1690.field_1914 = this.cinematicBefore;
         this.cinematicSet = false;
      }
   }

   public boolean zooming() {
      if (this.isActive() && mc.field_1724 != null) {
         return this.mode.get() == Zoom.Mode.Toggle ? this.toggled : this.key.isPressed();
      } else {
         return false;
      }
   }

   public double fovDivisor() {
      long now = System.nanoTime();
      double dt = this.lastFrame == 0L ? 0.0 : Math.min(0.1, (double)(now - this.lastFrame) / 1.0E9);
      this.lastFrame = now;
      boolean zoomed = this.zooming();
      if (zoomed && !this.zoomedBefore) {
         this.factor = (double)this.amount.get().intValue();
      }

      this.zoomedBefore = zoomed;
      double target = zoomed ? Math.log(this.factor) : 0.0;
      this.shown = this.smooth.get() ? this.shown + (target - this.shown) * (1.0 - Math.exp(-dt * 14.0)) : target;
      if (Math.abs(target - this.shown) < 1.0E-4) {
         this.shown = target;
      }

      return Math.exp(this.shown);
   }

   public double sensitivityScale() {
      return this.sensitivity.get() && this.shown > 0.001 ? 1.0 / Math.exp(this.shown) : 1.0;
   }

   public boolean scroll(double amount) {
      if (this.scroll.get() && this.zooming()) {
         this.factor = Math.clamp(this.factor * (amount > 0.0 ? 1.25 : 0.8), 1.5, 60.0);
         return true;
      } else {
         return false;
      }
   }

   public float hint() {
      return (float)Math.min(1.0, this.shown / Math.log(1.5));
   }

   @Override
   public String getInfoString() {
      return String.format(Locale.ROOT, "%.1fx", Math.exp(this.shown));
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   public static enum Mode {
      Hold,
      Toggle;
   }
}
