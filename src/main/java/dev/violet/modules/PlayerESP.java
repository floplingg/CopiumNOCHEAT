package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.render.ShapeMode;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_640;

public class PlayerESP extends Module {
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Color> color;
   private final Setting<Boolean> distanceColor;
   private final Setting<Boolean> tracers;
   private final Setting<Boolean> throughWalls;
   private final Setting<Boolean> notify;
   private final Setting<Boolean> notifyLeave;
   private final Setting<Integer> cooldown;
   private final Set<UUID> present;
   private final Map<UUID, Long> lastAlert;

   public PlayerESP() {
      super("Player ESP", "Highlights other players and notifies you when someone shows up.", Category.BASE_FINDING);
      this.group("Render");
      this.shapeMode = this.add(new EnumSetting<>("Shape Mode", "How players are drawn.", ShapeMode.Both));
      this.color = this.add(new ColorSetting("Color", "Color of the player boxes.", new Color(176, 140, 240, 255)));
      this.distanceColor = this.add(new BoolSetting("Distance Color", "Close players turn red, far ones stay in your color.", true));
      this.tracers = this.add(new BoolSetting("Tracers", "Line from your crosshair to every player.", false));
      this.throughWalls = this.add(new BoolSetting("Through Walls", "Boxes are visible through blocks.", true));
      this.group("Alerts");
      this.notify = this.add(new BoolSetting("Notify", "Notification when a player comes into render distance.", true));
      this.notifyLeave = this.add(new BoolSetting("Notify Leave", "Notification when a player leaves render distance.", false));
      this.cooldown = this.add(new IntSetting("Cooldown", "Seconds before the same player can notify again.", 30, 0, 300));
      this.present = new HashSet<>();
      this.lastAlert = new HashMap<>();
   }

   @Override
   public void onEnable() {
      this.present.clear();
      if (mc.field_1687 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != mc.field_1724) {
               this.present.add(player.method_5667());
            }
         }
      }
   }

   @Override
   public void onGameLeft() {
      this.present.clear();
   }

   @Override
   public void onTick() {
      Set<UUID> now = new HashSet<>();
      long time = System.currentTimeMillis();

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724) {
            UUID id = player.method_5667();
            now.add(id);
            if (!this.present.contains(id) && this.notify.get() && time - this.lastAlert.getOrDefault(id, 0L) >= (long)this.cooldown.get().intValue() * 1000L) {
               this.lastAlert.put(id, time);
               this.info(
                  "%s spotted, %d blocks away.", new Object[]{player.method_5477().getString(), Math.round(Math.sqrt(player.method_5858(mc.field_1724)))}
               );
            }
         }
      }

      if (this.notifyLeave.get()) {
         for (UUID id : this.present) {
            if (!now.contains(id)) {
               class_640 entry = mc.method_1562() == null ? null : mc.method_1562().method_2871(id);
               this.info("%s left your render distance.", new Object[]{entry != null ? entry.method_2966().name() : "A player"});
            }
         }
      }

      this.present.clear();
      this.present.addAll(now);
   }

   @Override
   public void onRender(Render3D renderer) {
      float delta = mc.method_61966().method_60637(true);
      renderer.setDepthTest(!this.throughWalls.get());

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && (!player.method_5767() || player.method_5851())) {
            class_243 offset = player.method_30950(delta).method_1020(player.method_73189());
            class_238 box = player.method_5829().method_997(offset).method_1014(0.05);
            Color c = this.color.get();
            if (this.distanceColor.get()) {
               double t = Math.min(1.0, Math.sqrt(player.method_5858(mc.field_1724)) / 48.0);
               c = new Color(255, 70, 90, c.a()).lerp(c, t);
            }

            renderer.box(box, c, c, this.shapeMode.get());
            if (this.tracers.get()) {
               class_243 center = box.method_1005();
               renderer.tracer(center.field_1352, center.field_1351, center.field_1350, c.scaleAlpha(0.8));
            }
         }
      }

      renderer.setDepthTest(false);
   }

   @Override
   public String getInfoString() {
      return this.present.isEmpty() ? null : String.valueOf(this.present.size());
   }
}
