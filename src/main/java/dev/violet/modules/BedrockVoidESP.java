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
import dev.violet.util.ChunkScanner;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2338.class_2339;

public class BedrockVoidESP extends Module {
   private final Setting<Boolean> onlyVoid = this.add(
         new BoolSetting(
            "Only Void",
            "Only shows real openings you can look or fall through. Off also shows any other block that replaced bedrock in the bottom layer.",
            true
         )
      )
      .onChanged(v -> this.rescan());
   private final Setting<Boolean> netherRoof = this.add(new BoolSetting("Nether Roof", "Also shows holes in the top bedrock layer of the Nether roof.", false))
      .onChanged(v -> this.rescan());
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Color> color;
   private final Setting<Boolean> beam;
   private final Setting<Integer> beamHeight;
   private final Setting<Boolean> throughWalls;
   private final Setting<Boolean> tracers;
   private final Setting<Boolean> notify;
   private final Map<class_2338, Long> holes;
   private final ArrayDeque<class_1923> queue;
   private int perTick;
   private int ticks;
   private int pendingAlerts;
   private class_2338 pendingPos;

   public BedrockVoidESP() {
      super("Bedrock Void ESP", "Shows holes in the bedrock floor where you can look or fall into the void.", Category.BASE_FINDING);
      this.group("Render");
      this.shapeMode = this.add(new EnumSetting<>("Shape Mode", "How the holes are drawn.", ShapeMode.Both));
      this.color = this.add(new ColorSetting("Color", "Color of the holes.", new Color(160, 90, 255, 140)));
      this.beam = this.add(new BoolSetting("Beam", "A beam above the holes so you see them from far away. Holes close together share one beam.", true));
      this.beamHeight = this.add(new IntSetting("Beam Height", "Height of the beam in blocks.", 80, 10, 320)).visible(this.beam::get);
      this.throughWalls = this.add(new BoolSetting("Through Walls", "Holes are visible through blocks.", true));
      this.tracers = this.add(new BoolSetting("Tracers", "Line from your crosshair to every hole.", false));
      this.group("Alerts");
      this.notify = this.add(new BoolSetting("Notify", "Chat message when a new hole is found.", true));
      this.holes = new HashMap<>();
      this.queue = new ArrayDeque<>();
      this.perTick = 16;
   }

   @Override
   public void onEnable() {
      this.holes.clear();
      this.queue.clear();
      this.ticks = 0;

      for (class_2818 chunk : ChunkScanner.loadedChunks()) {
         this.scan(chunk, false);
      }
   }

   @Override
   public void onDisable() {
      this.holes.clear();
      this.queue.clear();
   }

   @Override
   public void onGameLeft() {
      this.holes.clear();
      this.queue.clear();
      this.pendingAlerts = 0;
      this.pendingPos = null;
   }

   private void rescan() {
      if (this.isActive()) {
         this.onEnable();
      }
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.scan(chunk, true);
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
      if (mc.field_1687 != null && this.watched(pos.method_10264())) {
         if (this.isHole(newState)) {
            this.found(pos.method_10062(), true);
         } else {
            this.holes.remove(pos);
         }
      }
   }

   private boolean scannable() {
      return mc.field_1687 != null && mc.field_1687.method_27983() != class_1937.field_25181;
   }

   private int floorY() {
      return mc.field_1687.method_31607();
   }

   private int roofY() {
      return mc.field_1687.method_8597().comp_653() - 1;
   }

   private boolean roofActive() {
      return this.netherRoof.get() && mc.field_1687.method_27983() == class_1937.field_25180;
   }

   private boolean watched(int y) {
      return !this.scannable() ? false : y == this.floorY() || this.roofActive() && y == this.roofY();
   }

   private boolean isHole(class_2680 state) {
      if (state.method_27852(class_2246.field_9987)) {
         return false;
      } else {
         return !this.onlyVoid.get() ? true : state.method_26215() || state.method_45474();
      }
   }

   private void scan(class_2818 chunk, boolean alert) {
      if (this.scannable()) {
         this.scanLayer(chunk, this.floorY(), alert);
         if (this.roofActive()) {
            this.scanLayer(chunk, this.roofY(), alert);
         }
      }
   }

   private void scanLayer(class_2818 chunk, int y, boolean alert) {
      int index = chunk.method_31602(y);
      if (index >= 0 && index < chunk.method_12006().length) {
         class_2826 section = chunk.method_12006()[index];
         class_1923 chunkPos = chunk.method_12004();
         class_2339 pos = new class_2339();
         if (section != null && !section.method_38292()) {
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  pos.method_10103(chunkPos.method_8326() + x, y, chunkPos.method_8328() + z);
                  if (this.isHole(chunk.method_8320(pos))) {
                     this.found(pos.method_10062(), alert);
                  } else {
                     this.holes.remove(pos);
                  }
               }
            }
         } else {
            for (int x = 0; x < 16; x++) {
               for (int zx = 0; zx < 16; zx++) {
                  this.holes.remove(pos.method_10103(chunkPos.method_8326() + x, y, chunkPos.method_8328() + zx));
               }
            }
         }
      }
   }

   private void found(class_2338 pos, boolean alert) {
      if (this.holes.putIfAbsent(pos, System.currentTimeMillis()) == null) {
         if (alert && this.notify.get()) {
            this.pendingAlerts++;
            if (this.pendingPos == null
               || mc.field_1724 != null && mc.field_1724.method_5707(pos.method_46558()) < mc.field_1724.method_5707(this.pendingPos.method_46558())) {
               this.pendingPos = pos;
            }
         }
      }
   }

   private void flushAlerts() {
      if (this.pendingAlerts != 0 && this.pendingPos != null) {
         if (this.pendingAlerts == 1) {
            this.info(
               "Bedrock hole found at %d, %d, %d", new Object[]{this.pendingPos.method_10263(), this.pendingPos.method_10264(), this.pendingPos.method_10260()}
            );
         } else {
            this.info(
               "%d bedrock holes found, closest at %d, %d, %d",
               new Object[]{this.pendingAlerts, this.pendingPos.method_10263(), this.pendingPos.method_10264(), this.pendingPos.method_10260()}
            );
         }

         this.pendingAlerts = 0;
         this.pendingPos = null;
      }
   }

   @Override
   public void onTick() {
      if (this.ticks % 60 == 0) {
         this.flushAlerts();
      }

      this.ticks++;
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.queue.isEmpty() && this.ticks % 200 == 0) {
            for (class_2818 chunk : ChunkScanner.loadedChunks()) {
               this.queue.add(chunk.method_12004());
            }

            this.perTick = Math.max(16, (this.queue.size() + 37) / 38);
         }

         for (int i = 0; i < this.perTick && !this.queue.isEmpty(); i++) {
            class_1923 pos = this.queue.poll();
            if (mc.field_1687.method_2935().method_12123(pos.field_9181, pos.field_9180)) {
               this.scan(mc.field_1687.method_8497(pos.field_9181, pos.field_9180), true);
            }
         }

         if (this.ticks % 40 == 0) {
            class_1923 center = mc.field_1724.method_31476();
            int range = mc.field_1690.method_38521() + 2;
            Iterator<class_2338> it = this.holes.keySet().iterator();

            while (it.hasNext()) {
               class_2338 pos = it.next();
               if (Math.abs((pos.method_10263() >> 4) - center.field_9181) > range || Math.abs((pos.method_10260() >> 4) - center.field_9180) > range) {
                  it.remove();
               } else if (!this.watched(pos.method_10264())) {
                  it.remove();
               }
            }
         }
      }
   }

   @Override
   public void onRender(Render3D renderer) {
      if (!this.holes.isEmpty() && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         renderer.setDepthTest(!this.throughWalls.get());
         Color base = this.color.get();
         Set<Long> beamed = new HashSet<>();

         for (Entry<class_2338, Long> entry : this.holes.entrySet()) {
            class_2338 pos = entry.getKey();
            double appear = Math.min(1.0, (double)(now - entry.getValue()) / 700.0);
            appear = 1.0 - Math.pow(1.0 - appear, 3.0);
            Color shown = base.scaleAlpha(appear);
            renderer.box(
               (double)pos.method_10263(),
               (double)pos.method_10264(),
               (double)pos.method_10260(),
               (double)(pos.method_10263() + 1),
               (double)(pos.method_10264() + 1),
               (double)(pos.method_10260() + 1),
               shown,
               base.withAlpha(255).scaleAlpha(appear),
               this.shapeMode.get()
            );
            double cx = (double)pos.method_10263() + 0.5;
            double cz = (double)pos.method_10260() + 0.5;
            if (this.beam.get()
               && beamed.add((long)(pos.method_10263() >> 3) << 32 ^ (long)(pos.method_10260() >> 3) & 4294967295L ^ (long)pos.method_10264() << 58)) {
               double phase = ((double)pos.method_10263() * 0.37 + (double)pos.method_10260() * 0.61) % 6.283185307179586;
               double wave = 0.5 + 0.5 * Math.sin((double)now / 1000.0 * 3.141592653589793 * 2.0 / 2.4 + phase);
               double bottom = (double)(pos.method_10264() + 1);
               double top = bottom + (double)this.beamHeight.get().intValue() * appear;
               renderer.beam(cx, cz, bottom, top, 0.2 * (0.9 + 0.1 * wave), base.withAlpha(255).scaleAlpha(appear), 0.75 + 0.25 * wave);
            }

            if (this.tracers.get()) {
               renderer.tracer(cx, (double)pos.method_10264() + 0.5, cz, base.withAlpha(255).scaleAlpha(0.8 * appear));
            }
         }

         renderer.setDepthTest(false);
      }
   }

   @Override
   public String getInfoString() {
      return this.holes.isEmpty() ? null : String.valueOf(this.holes.size());
   }
}
