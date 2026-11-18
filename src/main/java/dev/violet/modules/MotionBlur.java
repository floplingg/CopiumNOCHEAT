package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_276;
import net.minecraft.class_279;
import net.minecraft.class_2960;
import net.minecraft.class_9922;
import net.minecraft.class_9960;

public class MotionBlur extends Module {
   private static final class_2960 EFFECT = class_2960.method_60655("violet", "motion_blur");
   private static final class_2960 STRENGTH = class_2960.method_60655("violet", "textures/effect/motion_strength.png");
   private final Setting<Integer> strength = this.add(
      new IntSetting("Strength", "How long the motion trail lasts. Higher is blurrier. 30 to 50 is subtle, 100 is very strong.", 40, 1, 100)
   );
   private final Setting<Boolean> fpsCompensated = this.add(
      new BoolSetting("FPS Compensated", "The trail lasts the same time at any FPS. Off makes the blur stronger at low FPS and weaker at high FPS.", true)
   );
   private class_1043 texture;
   private int frames;
   private int lastWidth;
   private int lastHeight;
   private long lastFrame;

   public MotionBlur() {
      super("Motion Blur", "Smooth motion blur on the world when you move or turn. The HUD and menus stay sharp.", Category.VISUALS);
   }

   @Override
   public void onEnable() {
      this.frames = 0;
   }

   public static void apply(class_9922 pool) {
      MotionBlur blur = SoberClient.MODULES.get(MotionBlur.class);
      if (blur != null && !FpsBooster.lowEnd()) {
         if (!blur.isActive()) {
            blur.frames = 0;
         } else {
            blur.render(pool);
         }
      }
   }

   private void render(class_9922 pool) {
      class_276 framebuffer = mc.method_1522();
      if (framebuffer.field_1482 != this.lastWidth || framebuffer.field_1481 != this.lastHeight) {
         this.lastWidth = framebuffer.field_1482;
         this.lastHeight = framebuffer.field_1481;
         this.frames = 0;
      }

      if (this.texture == null) {
         this.texture = new class_1043(() -> "violet motion strength", new class_1011(1, 1, false));
         mc.method_1531().method_4616(STRENGTH, this.texture);
      }

      long now = System.nanoTime();
      float delta = this.lastFrame == 0L ? 0.016666668F : Math.max(0.001F, Math.min(0.25F, (float)(now - this.lastFrame) / 1.0E9F));
      this.lastFrame = now;
      float base = 0.75F * (float)Math.pow((double)this.strength.get().intValue() / 100.0, 1.3);
      float keep = this.fpsCompensated.get() ? (float)Math.pow((double)base, (double)(delta * 60.0F)) : base;
      if (this.frames < 2) {
         keep = 0.0F;
      }

      this.frames++;
      int value = Math.round(Math.max(0.0F, Math.min(1.0F, keep)) * 255.0F);
      this.texture.method_4525().method_61941(0, 0, 0xFF000000 | value << 16 | value << 8 | value);
      this.texture.method_4524();
      class_279 processor = mc.method_62887().method_62941(EFFECT, class_9960.field_53902);
      if (processor != null) {
         processor.method_1258(framebuffer, pool);
      }
   }

   @Override
   public String getInfoString() {
      return this.strength.get() + "%";
   }
}
