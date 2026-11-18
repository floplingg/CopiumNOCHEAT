package dev.violet.modules;

import dev.violet.event.ListenerHandle;
import dev.violet.event.impl.PacketReceiveNettyEvent;
import dev.violet.gui.Notifications;
import dev.violet.gui.OverlayCanvas;
import dev.violet.gui.WorldLabel;
import dev.violet.module.Category;
import dev.violet.module.HudOverlayProvider;
import dev.violet.module.Module;
import dev.violet.render.ChunkHighlight;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_1299;
import net.minecraft.class_1923;
import net.minecraft.class_2211;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2258;
import net.minecraft.class_2261;
import net.minecraft.class_2266;
import net.minecraft.class_2279;
import net.minecraft.class_2281;
import net.minecraft.class_2282;
import net.minecraft.class_2283;
import net.minecraft.class_2309;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2344;
import net.minecraft.class_2360;
import net.minecraft.class_2363;
import net.minecraft.class_2386;
import net.minecraft.class_2397;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2449;
import net.minecraft.class_2480;
import net.minecraft.class_2488;
import net.minecraft.class_2500;
import net.minecraft.class_2523;
import net.minecraft.class_2541;
import net.minecraft.class_2542;
import net.minecraft.class_2596;
import net.minecraft.class_2604;
import net.minecraft.class_2623;
import net.minecraft.class_2665;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2767;
import net.minecraft.class_3419;
import net.minecraft.class_3708;
import net.minecraft.class_4481;
import net.minecraft.class_4770;
import net.minecraft.class_4863;
import net.minecraft.class_5542;
import net.minecraft.class_5635;
import net.minecraft.class_5689;
import net.minecraft.class_5801;
import net.minecraft.class_5955;
import net.minecraft.class_7124;
import net.minecraft.class_8042;

public class MotionScanner extends Module implements HudOverlayProvider {
   private final Setting<Integer> threshold;
   private final Setting<Integer> memory;
   private final Setting<Integer> ignoreNear;
   private final Setting<Boolean> containers;
   private final Setting<Boolean> doors;
   private final Setting<Boolean> furnaces;
   private final Setting<Boolean> building;
   private final Setting<Boolean> redstone;
   private final Setting<Boolean> entities;
   private final Setting<Boolean> sounds;
   private final Setting<Boolean> notify;
   private final Setting<Integer> cooldown;
   private final Setting<Boolean> chat;
   private final Setting<Color> color;
   private final Setting<Color> hotColor;
   private final Setting<Boolean> labels;
   private final Setting<Boolean> beams;
   private final Setting<Boolean> tracers;
   private final ConcurrentLinkedQueue<class_2596<?>> incoming;
   private final ConcurrentLinkedQueue<MotionScanner.Event> events;
   private final Long2ObjectOpenHashMap<MotionScanner.Spot> spots;
   private final Long2LongOpenHashMap notified;
   private final List<MotionScanner.Label> shown;
   private ListenerHandle<PacketReceiveNettyEvent> listener;
   private long lastDecay;

   public MotionScanner() {
      super(
         "Motion Scanner",
         "Lights up chunks where something is happening right now: chests and doors opening, furnaces running, blocks being placed or broken, items, carts and block sounds.",
         Category.BASE_FINDING
      );
      this.group("Find");
      this.threshold = this.add(
         new IntSetting("Sensitivity", "Activity points a chunk needs before it lights up. Lower finds more, higher only shows busy chunks.", 12, 2, 80)
      );
      this.memory = this.add(new IntSetting("Memory", "Seconds until old activity has faded away.", 60, 10, 600));
      this.ignoreNear = this.add(
         new IntSetting("Ignore Near Me", "Activity closer to you than this many blocks is ignored, so your own building does not count.", 24, 0, 96)
      );
      this.containers = this.add(new BoolSetting("Containers", "Chests, barrels and shulker boxes opening.", true));
      this.doors = this.add(new BoolSetting("Doors", "Doors, trapdoors and fence gates opening and closing.", true));
      this.furnaces = this.add(new BoolSetting("Furnaces", "Furnaces, smokers and blast furnaces turning on.", true));
      this.building = this.add(new BoolSetting("Building", "Blocks placed or broken. Growing plants, water, lava, fire and leaves are ignored.", true));
      this.redstone = this.add(new BoolSetting("Redstone", "Pistons, lamps, repeaters and other redstone parts switching.", true));
      this.entities = this.add(new BoolSetting("Items & Carts", "Dropped items, experience, minecarts and TNT showing up.", true));
      this.sounds = this.add(new BoolSetting("Sounds", "Block sounds the server sends you, for example someone mining out of sight.", true));
      this.group("Alerts");
      this.notify = this.add(new BoolSetting("Notify", "Notification when a chunk lights up.", true));
      this.cooldown = this.add(new IntSetting("Cooldown", "Seconds before the same chunk can notify again.", 120, 10, 900)).visible(this.notify::get);
      this.chat = this.add(new BoolSetting("Chat Message", "Also writes the find into the chat.", false)).visible(this.notify::get);
      this.group("Look");
      this.color = this.add(new ColorSetting("Color", "Color of chunks with some activity. Pick any color on the wheel.", new Color(120, 200, 255, 200)));
      this.hotColor = this.add(new ColorSetting("Busy Color", "Color of chunks with a lot of activity.", new Color(255, 80, 120, 230)));
      this.labels = this.add(new BoolSetting("Labels", "Shows what happened above every chunk.", true));
      this.beams = this.add(new BoolSetting("Beams", "A beam above every active chunk.", true));
      this.tracers = this.add(new BoolSetting("Tracers", "Lines to every active chunk.", false));
      this.incoming = new ConcurrentLinkedQueue<>();
      this.events = new ConcurrentLinkedQueue<>();
      this.spots = new Long2ObjectOpenHashMap();
      this.notified = new Long2LongOpenHashMap();
      this.shown = new ArrayList<>();
   }

   @Override
   public void onEnable() {
      this.spots.clear();
      this.notified.clear();
      this.incoming.clear();
      this.events.clear();
      this.lastDecay = System.currentTimeMillis();
      this.listener = PacketReceiveNettyEvent.TYPE.listen(this, event -> {
         class_2596<?> packet = event.packet;
         if (packet instanceof class_2623 || packet instanceof class_2604 || packet instanceof class_2767 || packet instanceof class_8042) {
            this.incoming.add(packet);
         }
      });
   }

   @Override
   public void onDisable() {
      if (this.listener != null) {
         PacketReceiveNettyEvent.TYPE.unregister(this.listener);
      }

      this.listener = null;
      this.spots.clear();
      this.shown.clear();
      this.incoming.clear();
      this.events.clear();
   }

   @Override
   public void onGameLeft() {
      this.spots.clear();
      this.notified.clear();
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
      if (oldState != newState) {
         MotionScanner.Reason reason = this.classify(oldState, newState);
         if (reason != null) {
            this.events.add(new MotionScanner.Event(reason, (double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.method_10260() + 0.5));
         }
      }
   }

   private MotionScanner.Reason classify(class_2680 oldState, class_2680 newState) {
      class_2248 oldBlock = oldState.method_26204();
      class_2248 newBlock = newState.method_26204();
      if (natural(oldBlock) || natural(newBlock)) {
         return null;
      } else if (oldBlock == newBlock) {
         if (newState.method_28498(class_2741.field_12537) && oldState.method_11654(class_2741.field_12537) != newState.method_11654(class_2741.field_12537)) {
            return newBlock instanceof class_3708
               ? (this.containers.get() ? MotionScanner.Reason.CONTAINER : null)
               : (this.doors.get() ? MotionScanner.Reason.DOOR : null);
         } else if (newBlock instanceof class_2363 && newState.method_28498(class_2741.field_12548)) {
            return this.furnaces.get() && newState.method_11654(class_2741.field_12548) ? MotionScanner.Reason.FURNACE : null;
         } else if (!newState.method_28498(class_2741.field_12484)
            && !newState.method_28498(class_2741.field_12548)
            && !newState.method_28498(class_2741.field_12511)
            && !newState.method_28498(class_2741.field_12552)) {
            return null;
         } else {
            return this.redstone.get() ? MotionScanner.Reason.REDSTONE : null;
         }
      } else {
         return this.building.get() ? MotionScanner.Reason.BUILD : null;
      }
   }

   private static boolean natural(class_2248 block) {
      return block instanceof class_2404
         || block instanceof class_4770
         || block instanceof class_2397
         || block instanceof class_2261
         || block instanceof class_4863
         || block instanceof class_2500
         || block instanceof class_2344
         || block instanceof class_2488
         || block instanceof class_2386
         || block instanceof class_2360
         || block instanceof class_2541
         || block instanceof class_2523
         || block instanceof class_2266
         || block instanceof class_2211
         || block instanceof class_5542
         || block instanceof class_5955
         || block instanceof class_2282
         || block instanceof class_2279
         || block instanceof class_2283
         || block instanceof class_5689
         || block instanceof class_2309
         || block instanceof class_2542
         || block instanceof class_7124
         || block instanceof class_4481
         || block instanceof class_2258
         || block instanceof class_5635
         || block == class_2246.field_10566
         || block instanceof class_2449
         || block instanceof class_5801;
   }

   private void handle(class_2596<?> packet) {
      if (packet instanceof class_8042 bundle) {
         for (class_2596<?> inner : bundle.method_48324()) {
            this.handle(inner);
         }
      } else if (packet instanceof class_2623 blockEvent) {
         class_2248 block = blockEvent.method_11295();
         class_2338 pos = blockEvent.method_11298();
         if (!(block instanceof class_2281) && !(block instanceof class_2336) && !(block instanceof class_2480)) {
            if (block instanceof class_2665 && this.redstone.get()) {
               this.events
                  .add(
                     new MotionScanner.Event(
                        MotionScanner.Reason.REDSTONE, (double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.method_10260() + 0.5
                     )
                  );
            }
         } else if (this.containers.get() && blockEvent.method_11294() == 1 && blockEvent.method_11296() > 0) {
            this.events
               .add(
                  new MotionScanner.Event(
                     MotionScanner.Reason.CONTAINER, (double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.method_10260() + 0.5
                  )
               );
         }
      } else {
         if (packet instanceof class_2604 spawn && this.entities.get()) {
            class_1299<?> type = spawn.method_11169();
            MotionScanner.Reason reason = type == class_1299.field_6052 || type == class_1299.field_6044
               ? MotionScanner.Reason.ITEMS
               : (
                  type != class_1299.field_6063
                        && type != class_1299.field_6089
                        && type != class_1299.field_6096
                        && type != class_1299.field_6126
                        && type != class_1299.field_6058
                        && type != class_1299.field_6053
                        && type != class_1299.field_6080
                     ? null
                     : MotionScanner.Reason.VEHICLE
               );
            if (reason != null) {
               this.events.add(new MotionScanner.Event(reason, spawn.method_11175(), spawn.method_11174(), spawn.method_11176()));
            }

            return;
         }

         if (packet instanceof class_2767 sound && this.sounds.get() && sound.method_11888() == class_3419.field_15245) {
            this.events.add(new MotionScanner.Event(MotionScanner.Reason.SOUND, sound.method_11890(), sound.method_11889(), sound.method_11893()));
         }
      }
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null) {
         class_2596<?> packet;
         while ((packet = this.incoming.poll()) != null) {
            this.handle(packet);
         }

         long now = System.currentTimeMillis();
         double near = (double)this.ignoreNear.get().intValue() * (double)this.ignoreNear.get().intValue();

         MotionScanner.Event event;
         while ((event = this.events.poll()) != null) {
            if (!(mc.field_1724.method_5649(event.x(), mc.field_1724.method_23318(), event.z()) < near)) {
               long key = class_1923.method_8331((int)Math.floor(event.x()) >> 4, (int)Math.floor(event.z()) >> 4);
               MotionScanner.Spot spot = (MotionScanner.Spot)this.spots.computeIfAbsent(key, k -> new MotionScanner.Spot());
               spot.score = spot.score + event.reason().weight;
               spot.ySum = spot.ySum + event.y() * event.reason().weight;
               spot.yWeight = spot.yWeight + event.reason().weight;
               spot.last = now;
               spot.reasons.merge(event.reason(), Integer.valueOf(1), Integer::sum);
               if (this.notify.get()
                  && spot.score >= (double)this.threshold.get().intValue()
                  && now - this.notified.getOrDefault(key, 0L) > (long)this.cooldown.get().intValue() * 1000L) {
                  this.notified.put(key, now);
                  this.announce(key, spot);
               }
            }
         }

         double factor = Math.exp((double)(-(now - this.lastDecay)) / ((double)this.memory.get().intValue() * 1000.0 / 3.0));
         this.lastDecay = now;
         this.spots.values().removeIf(spotx -> {
            spotx.score *= factor;
            return spotx.score < 0.3;
         });
      }
   }

   private void announce(long key, MotionScanner.Spot spot) {
      class_1923 pos = new class_1923(key);
      int x = pos.method_8326() + 8;
      int z = pos.method_8328() + 8;
      int distance = (int)Math.round(Math.sqrt(mc.field_1724.method_5649((double)x, mc.field_1724.method_23318(), (double)z)));
      String message = "Activity at "
         + x
         + " "
         + Math.round(spot.ySum / Math.max(1.0E-6, spot.yWeight))
         + " "
         + z
         + " ("
         + distance
         + "m): "
         + summary(spot, 3);
      Notifications.push("Motion Scanner", message, Notifications.Type.WARNING);
      if (this.chat.get()) {
         this.info(message, new Object[0]);
      }
   }

   private static String summary(MotionScanner.Spot spot, int max) {
      List<Entry<MotionScanner.Reason, Integer>> entries = new ArrayList<>(spot.reasons.entrySet());
      entries.sort((a, b) -> Double.compare((double)b.getValue().intValue() * b.getKey().weight, (double)a.getValue().intValue() * a.getKey().weight));
      StringBuilder text = new StringBuilder();

      for (int i = 0; i < entries.size() && i < max; i++) {
         if (i > 0) {
            text.append(" · ");
         }

         text.append(entries.get(i).getValue()).append(' ').append(entries.get(i).getKey().label);
      }

      return text.toString();
   }

   @Override
   public void onRender(Render3D renderer) {
      this.shown.clear();
      if (!this.spots.isEmpty() && mc.field_1724 != null) {
         int limit = this.threshold.get();
         ObjectIterator var3 = this.spots.long2ObjectEntrySet().iterator();

         while (var3.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<MotionScanner.Spot> entry = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<MotionScanner.Spot>)var3.next();
            MotionScanner.Spot spot = (MotionScanner.Spot)entry.getValue();
            if (!(spot.score < (double)limit * 0.5)) {
               class_1923 pos = new class_1923(entry.getLongKey());
               double heat = Math.min(1.0, Math.max(0.0, (spot.score - (double)limit * 0.5) / ((double)limit * 2.5)));
               double y = spot.ySum / Math.max(1.0E-6, spot.yWeight) + 0.05;
               Color c = this.color.get().lerp(this.hotColor.get(), heat);
               ChunkHighlight.draw(renderer, pos.field_9181, pos.field_9180, y, c, this.beams.get() && spot.score >= (double)limit);
               if (this.tracers.get() && spot.score >= (double)limit) {
                  ChunkHighlight.tracer(renderer, pos.field_9181, pos.field_9180, y, c.scaleAlpha(0.8));
               }

               if (this.labels.get()) {
                  this.shown
                     .add(
                        new MotionScanner.Label(
                           new class_243((double)(pos.method_8326() + 8), y + 1.5, (double)(pos.method_8328() + 8)),
                           "Activity " + Math.round(spot.score),
                           summary(spot, 3)
                        )
                     );
               }
            }
         }
      }
   }

   @Override
   public boolean isHudOverlayEnabled() {
      return this.isActive() && this.labels.get() && !this.shown.isEmpty();
   }

   @Override
   public void renderHudOverlay(OverlayCanvas canvas, float screenW, float screenH) {
      for (MotionScanner.Label label : this.shown) {
         float[] screen = WorldLabel.project(mc, label.pos(), screenW, screenH);
         if (screen != null) {
            WorldLabel.drawText(canvas, screen[0], screen[1], label.primary(), label.secondary(), 1.0F);
         }
      }
   }

   @Override
   public String getInfoString() {
      int count = 0;
      ObjectIterator var2 = this.spots.values().iterator();

      while (var2.hasNext()) {
         MotionScanner.Spot spot = (MotionScanner.Spot)var2.next();
         if (spot.score >= (double)this.threshold.get().intValue()) {
            count++;
         }
      }

      return count == 0 ? null : String.valueOf(count);
   }

   private static record Event(MotionScanner.Reason reason, double x, double y, double z) {
   }

   private static record Label(class_243 pos, String primary, String secondary) {
   }

   private static enum Reason {
      CONTAINER("containers opened", 6.0),
      DOOR("doors used", 4.0),
      FURNACE("furnaces running", 3.0),
      BUILD("blocks changed", 3.0),
      REDSTONE("redstone", 1.0),
      VEHICLE("minecarts or TNT", 2.0),
      ITEMS("items dropped", 0.5),
      SOUND("block sounds", 1.0);

      final String label;
      final double weight;

      private Reason(String label, double weight) {
         this.label = label;
         this.weight = weight;
      }
   }

   private static final class Spot {
      double score;
      double ySum;
      double yWeight;
      long last;
      final EnumMap<MotionScanner.Reason, Integer> reasons = new EnumMap<>(MotionScanner.Reason.class);
   }
}
