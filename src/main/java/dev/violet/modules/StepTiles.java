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
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2350.class_2351;

public class StepTiles extends Module {
   private final Setting<Integer> duration = this.add(new IntSetting("Duration", "How long a tile keeps glowing in milliseconds.", 1400, 300, 5000));
   private final Setting<StepTiles.ColorMode> colorMode = this.add(
      new EnumSetting<>("Color Mode", "One custom color, the menu accent or rainbow tiles.", StepTiles.ColorMode.Custom)
   );
   private final Setting<Color> color = this.add(new ColorSetting("Color", "Color of the tiles.", new Color(232, 122, 255, 255)))
      .visible(() -> this.colorMode.get() == StepTiles.ColorMode.Custom);
   private final Setting<Boolean> fill = this.add(new BoolSetting("Fill", "The whole tile glows, not only its edges.", true));
   private final Setting<Boolean> others = this.add(new BoolSetting("Others", "Tiles also light up under other players.", false));
   private final List<StepTiles.Tile> tiles = new ArrayList<>();
   private final Int2LongOpenHashMap lastPos = new Int2LongOpenHashMap();
   private final Int2DoubleOpenHashMap groundY = new Int2DoubleOpenHashMap();
   private double hue;

   public StepTiles() {
      super("Step Tiles", "The blocks you walk on light up as glowing tiles and slowly fade out.", Category.VISUALS);
   }

   @Override
   public void onDisable() {
      this.tiles.clear();
      this.lastPos.clear();
      this.groundY.clear();
   }

   @Override
   public void onTick() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();

         for (class_1657 player : mc.field_1687.method_18456()) {
            if ((player == mc.field_1724 || this.others.get() && !(player.method_5858(mc.field_1724) > 2304.0))
               && player.method_24828()
               && !player.method_7325()) {
               int id = player.method_5628();
               double y = player.method_23318();
               boolean fell = this.groundY.containsKey(id) && this.groundY.get(id) - y >= 2.0;
               this.groundY.put(id, y);
               class_2338 pos = class_2338.method_49637(player.method_23317(), y - 0.2, player.method_23321());
               long key = pos.method_10063();
               if (!this.lastPos.containsKey(id) || this.lastPos.get(id) != key) {
                  this.lastPos.put(id, key);
                  if (!fell) {
                     class_2680 state = mc.field_1687.method_8320(pos);
                     class_265 shape = state.method_26218(mc.field_1687, pos);
                     if (!shape.method_1110()) {
                        double top = (double)pos.method_10264() + shape.method_1105(class_2351.field_11052);
                        this.tiles.removeIf(tile -> tile.pos() == key);
                        if (this.tiles.size() >= 96) {
                           this.tiles.remove(0);
                        }

                        this.hue += 0.07;
                        this.tiles.add(new StepTiles.Tile(key, top, now, this.hue));
                     }
                  }
               }
            }
         }

         if (this.lastPos.size() > 128) {
            this.lastPos.clear();
            this.groundY.clear();
         }
      }
   }

   private Color colorFor(StepTiles.Tile tile) {
      return switch ((StepTiles.ColorMode)this.colorMode.get()) {
         case Custom -> this.color.get().withAlpha(255);
         case Theme -> Color.fromPacked(Theme.accent(tile.hue())).withAlpha(255);
         case Rainbow -> Color.fromHsv((float)(tile.hue() % 1.0), 0.65F, 1.0F, 255);
      };
   }

   @Override
   public void onRender(Render3D renderer) {
      if (!this.tiles.isEmpty()) {
         long now = System.currentTimeMillis();
         double life = (double)Math.max(1, this.duration.get());
         renderer.setDepthTest(true);
         Iterator<StepTiles.Tile> iterator = this.tiles.iterator();

         while (iterator.hasNext()) {
            StepTiles.Tile tile = iterator.next();
            double t = (double)(now - tile.start()) / life;
            if (t >= 1.0) {
               iterator.remove();
            } else {
               double appear = 1.0 - Math.pow(1.0 - Math.min(1.0, t * 6.0), 3.0);
               double fade = appear * (t < 0.35 ? 1.0 : 1.0 - Math.pow((t - 0.35) / 0.65, 1.5));
               class_2338 pos = class_2338.method_10092(tile.pos());
               double y = tile.top() + 0.004;
               double x0 = (double)pos.method_10263();
               double z0 = (double)pos.method_10260();
               double x1 = x0 + 1.0;
               double z1 = z0 + 1.0;
               double cx = x0 + 0.5;
               double cz = z0 + 0.5;
               Color base = this.colorFor(tile);
               Color clear = base.withAlpha(0);
               Color rim = base.lerp(Color.WHITE, 0.35);
               double inset = 0.04;
               double ax = x0 + inset;
               double az = z0 + inset;
               double bx = x1 - inset;
               double bz = z1 - inset;
               if (this.fill.get()) {
                  Color center = base.scaleAlpha(0.1 * fade);
                  Color edge = base.scaleAlpha(0.42 * fade);
                  renderer.quadGradient(cx, y, cz, cx, y, cz, bx, y, az, ax, y, az, center, center, edge, edge);
                  renderer.quadGradient(cx, y, cz, cx, y, cz, bx, y, bz, bx, y, az, center, center, edge, edge);
                  renderer.quadGradient(cx, y, cz, cx, y, cz, ax, y, bz, bx, y, bz, center, center, edge, edge);
                  renderer.quadGradient(cx, y, cz, cx, y, cz, ax, y, az, ax, y, bz, center, center, edge, edge);
               }

               double glow = 0.14;
               Color soft = base.scaleAlpha(0.35 * fade);
               renderer.quadGradient(ax, y, az, bx, y, az, bx + glow, y, az - glow, ax - glow, y, az - glow, soft, soft, clear, clear);
               renderer.quadGradient(bx, y, az, bx, y, bz, bx + glow, y, bz + glow, bx + glow, y, az - glow, soft, soft, clear, clear);
               renderer.quadGradient(bx, y, bz, ax, y, bz, ax - glow, y, bz + glow, bx + glow, y, bz + glow, soft, soft, clear, clear);
               renderer.quadGradient(ax, y, bz, ax, y, az, ax - glow, y, az - glow, ax - glow, y, bz + glow, soft, soft, clear, clear);
               renderer.setLineWidth(2.0F);
               Color line = rim.scaleAlpha(0.95 * fade);
               renderer.line(ax, y, az, bx, y, az, line);
               renderer.line(bx, y, az, bx, y, bz, line);
               renderer.line(bx, y, bz, ax, y, bz, line);
               renderer.line(ax, y, bz, ax, y, az, line);
               double pulse = 0.18 + 0.3 * (1.0 - Math.pow(1.0 - Math.min(1.0, t * 1.8), 2.0));
               Color inner = rim.scaleAlpha(0.5 * fade * (1.0 - Math.min(1.0, t * 1.4)));
               renderer.setLineWidth(1.2F);
               renderer.line(cx - pulse, y, cz - pulse, cx + pulse, y, cz - pulse, inner);
               renderer.line(cx + pulse, y, cz - pulse, cx + pulse, y, cz + pulse, inner);
               renderer.line(cx + pulse, y, cz + pulse, cx - pulse, y, cz + pulse, inner);
               renderer.line(cx - pulse, y, cz + pulse, cx - pulse, y, cz - pulse, inner);
            }
         }

         renderer.setLineWidth(2.0F);
         renderer.setDepthTest(false);
      }
   }

   public static enum ColorMode {
      Custom,
      Theme,
      Rainbow;
   }

   private static record Tile(long pos, double top, long start, double hue) {
   }
}
