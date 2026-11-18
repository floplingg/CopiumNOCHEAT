package dev.violet.modules;

import dev.violet.gui.Theme;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_1657;

public class JumpCircles extends Module {
   private final Setting<JumpCircles.Style> style = this.add(
      new EnumSetting<>(
         "Style",
         "Sober Badge is a dark disc with the dripping chrome Sober logo in a chrome ring. Chrome Drip is only the chrome ring with liquid drops. Chrome Bottle is the SoberClient bottle. Glow Ring is a plain glowing ring.",
         JumpCircles.Style.Badge
      )
   );
   private final Setting<Integer> size = this.add(new IntSetting("Size", "Size of the circle at its biggest, in tenths of a block.", 24, 6, 60));
   private final Setting<Integer> duration = this.add(new IntSetting("Duration", "How long a circle stays in milliseconds.", 1100, 300, 4000));
   private final Setting<Boolean> spin = this.add(new BoolSetting("Spin", "The circle turns while it grows.", true));
   private final Setting<JumpCircles.ColorMode> colorMode = this.add(
      new EnumSetting<>("Color Mode", "Original keeps the colors of the logo. Custom, Theme and Rainbow tint it.", JumpCircles.ColorMode.Original)
   );
   private final Setting<Color> color = this.add(new ColorSetting("Color", "Tint of the circles.", new Color(230, 230, 236, 255)))
      .visible(() -> this.colorMode.get() == JumpCircles.ColorMode.Custom);
   private final Setting<Boolean> landing = this.add(new BoolSetting("On Landing", "Also shows a circle when you land after a jump or a fall.", false));
   private final Setting<Boolean> others = this.add(new BoolSetting("Others", "Shows circles for other players near you too.", false));
   private final List<JumpCircles.Circle> circles = new ArrayList<>();
   private final Int2IntOpenHashMap air = new Int2IntOpenHashMap();
   private final Int2DoubleOpenHashMap groundY = new Int2DoubleOpenHashMap();
   private double hue;
   private int turn = 1;

   public JumpCircles() {
      super("Jump Circles", "A dripping chrome SoberClient circle spreads out on the ground under you every time you jump.", Category.VISUALS);
   }

   @Override
   public void onDisable() {
      this.circles.clear();
      this.air.clear();
      this.groundY.clear();
   }

   @Override
   public void onTick() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player == mc.field_1724 || this.others.get() && !(player.method_5858(mc.field_1724) > 2304.0)) {
               if (!player.method_7325() && !player.method_31549().field_7479 && !player.method_5765()) {
                  int id = player.method_5628();
                  boolean ground = player.method_24828();
                  int ticksInAir = this.air.getOrDefault(id, 0);
                  if (ground) {
                     if (ticksInAir > 3 && this.landing.get()) {
                        this.spawn(player.method_23317(), player.method_23318(), player.method_23321(), now);
                     }

                     this.air.put(id, 0);
                     this.groundY.put(id, player.method_23318());
                  } else {
                     if (ticksInAir == 0
                        && this.groundY.containsKey(id)
                        && player.method_23318() > this.groundY.get(id) + 0.05
                        && player.method_18798().field_1351 > 0.1) {
                        this.spawn(player.method_23317(), this.groundY.get(id), player.method_23321(), now);
                     }

                     this.air.put(id, ticksInAir + 1);
                  }
               } else {
                  this.air.put(player.method_5628(), 0);
               }
            }
         }

         if (this.air.size() > 128) {
            this.air.clear();
            this.groundY.clear();
         }
      }
   }

   private void spawn(double x, double y, double z, long now) {
      if (this.circles.size() >= 48) {
         this.circles.remove(0);
      }

      this.hue += 0.09;
      this.turn = -this.turn;
      this.circles.add(new JumpCircles.Circle(x, y, z, now, this.hue, this.turn));
   }

   private Color colorFor(JumpCircles.Circle circle) {
      return switch ((JumpCircles.ColorMode)this.colorMode.get()) {
         case Original -> this.style.get() == JumpCircles.Style.Ring ? new Color(232, 232, 238, 255) : Color.WHITE;
         case Custom -> this.color.get().withAlpha(255);
         case Theme -> Color.fromPacked(Theme.accent(circle.hue())).withAlpha(255);
         case Rainbow -> Color.fromHsv((float)(circle.hue() % 1.0), 0.6F, 1.0F, 255);
      };
   }

   @Override
   public void onRender(Render3D renderer) {
      if (!this.circles.isEmpty()) {
         long now = System.currentTimeMillis();
         double life = (double)Math.max(1, this.duration.get());
         double max = (double)this.size.get().intValue() / 10.0;
         String texture = this.style.get().texture;
         Iterator<JumpCircles.Circle> iterator = this.circles.iterator();

         while (iterator.hasNext()) {
            JumpCircles.Circle circle = iterator.next();
            double t = (double)(now - circle.start()) / life;
            if (t >= 1.0) {
               iterator.remove();
            } else {
               double grow = 1.0 - Math.pow(1.0 - t, 3.0);
               double fade = t < 0.12 ? t / 0.12 : Math.pow(1.0 - (t - 0.12) / 0.88, 1.6);
               double scale = max * (0.3 + 0.7 * grow);
               double rotation = this.spin.get() ? (double)circle.turn() * grow * 2.4 : 0.0;
               renderer.decal(texture, circle.x(), circle.y() + 0.015, circle.z(), scale, rotation, this.colorFor(circle).scaleAlpha(fade));
            }
         }
      }
   }

   private static record Circle(double x, double y, double z, long start, double hue, int turn) {
   }

   public static enum ColorMode {
      Original,
      Custom,
      Theme,
      Rainbow;
   }

   public static enum Style {
      Badge("Sober Badge", "effects/jump_badge"),
      Drip("Chrome Drip", "effects/jump_drip"),
      Bottle("Chrome Bottle", "effects/jump_bottle"),
      Ring("Glow Ring", "effects/jump_ring");

      private final String title;
      final String texture;

      private Style(String title, String texture) {
         this.title = title;
         this.texture = texture;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
