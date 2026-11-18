package dev.violet.modules;

import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.util.OwnAreas;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_1657;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_3481;
import net.minecraft.class_6606;
import net.minecraft.class_742;

public class ActivityInspector extends Module implements Presets {
   private static final String[] DIRECTIONS = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
   private final Setting<Boolean> players;
   private final Setting<Boolean> ignoreNpcs;
   private final Setting<Integer> remember;
   private final Setting<Boolean> boxes;
   private final Setting<Boolean> trails;
   private final Setting<Integer> trailLength;
   private final Setting<Boolean> lastSeenBeams;
   private final Setting<Boolean> notifyPlayers;
   private final Setting<Color> playerColor;
   private final Setting<Color> dangerColor;
   private final Setting<Boolean> underground;
   private final Setting<Integer> belowY;
   private final Setting<Integer> minHits;
   private final Setting<Integer> window;
   private final Setting<Integer> ownRadius;
   private final Setting<Boolean> ignoreOwn;
   private final Setting<Boolean> notifyUnderground;
   private final Setting<Boolean> squares;
   private final Setting<Integer> renderY;
   private final Setting<Boolean> beams;
   private final Setting<Integer> beamCount;
   private final Setting<Color> lowColor;
   private final Setting<Color> highColor;
   private final Setting<Boolean> tracer;
   private final Map<UUID, ActivityInspector.Seen> seen;
   private final Map<class_1923, ArrayDeque<long[]>> events;
   private final Map<class_1923, Long> loadedAt;
   private final Set<class_1923> notifiedSpots;
   private List<ActivityInspector.Hotspot> hotspots;
   private List<Object> rows;
   private Object target;
   private long joinedAt;
   private long lastNotify;
   private int ticks;

   public ActivityInspector() {
      super(
         "Activity Inspector",
         "Finds other players: everyone in view with what they do, and places with very frequent activity deep underground.",
         Category.BASE_FINDING
      );
      this.group("Players");
      this.players = this.add(new BoolSetting("Players", "Lists and marks other players around you.", true));
      this.ignoreNpcs = this.add(new BoolSetting("Ignore NPCs", "Skips players that are not in the tab list, like spawn NPCs.", true))
         .visible(this.players::get);
      this.remember = this.add(new IntSetting("Remember", "Minutes a player stays in the list after leaving your view.", 5, 1, 30)).visible(this.players::get);
      this.boxes = this.add(new BoolSetting("Boxes", "Box around every player.", true)).visible(this.players::get);
      this.trails = this.add(new BoolSetting("Trails", "Line where players walked or flew.", true)).visible(this.players::get);
      this.trailLength = this.add(new IntSetting("Trail Length", "Seconds of trail that stay visible.", 30, 5, 120))
         .visible(() -> this.players.get() && this.trails.get());
      this.lastSeenBeams = this.add(new BoolSetting("Last Seen Beams", "Beam where a player was last seen.", true)).visible(this.players::get);
      this.notifyPlayers = this.add(new BoolSetting("Notify Players", "Notification when a player comes into view.", true)).visible(this.players::get);
      this.playerColor = this.add(new ColorSetting("Player Color", "Color of players.", new Color(90, 200, 255, 255))).visible(this.players::get);
      this.dangerColor = this.add(new ColorSetting("Approaching Color", "Color of players that come towards you.", new Color(255, 80, 80, 255)))
         .visible(this.players::get);
      this.group("Underground");
      this.underground = this.add(
         new BoolSetting("Underground", "Finds chunks with very frequent player activity deep down, from block and light changes.", true)
      );
      this.belowY = this.add(new IntSetting("Below Y", "Only changes below this height count.", 0, -64, 64)).visible(this.underground::get);
      this.minHits = this.add(new IntSetting("Min Hits", "Changes an area needs within the window to show up.", 8, 2, 100)).visible(this.underground::get);
      this.window = this.add(new IntSetting("Window", "Minutes the changes are counted for.", 5, 1, 30)).visible(this.underground::get);
      this.ownRadius = this.add(new IntSetting("Own Radius", "Ignores chunks this close to you, your own mining counts there.", 2, 0, 6))
         .visible(this.underground::get);
      this.ignoreOwn = this.add(new BoolSetting("Ignore Own Areas", "Skips places where you stayed for a while.", true)).visible(this.underground::get);
      this.notifyUnderground = this.add(new BoolSetting("Notify Underground", "Notification when a new active area is found.", true))
         .visible(this.underground::get);
      this.squares = this.add(new BoolSetting("Chunk Squares", "Colored squares on active chunks.", true)).visible(this.underground::get);
      this.renderY = this.add(new IntSetting("Render Y", "Height the squares are drawn at.", 63, -64, 320)).visible(this.underground::get);
      this.beams = this.add(new BoolSetting("Beams", "Beams on the most active areas.", true)).visible(this.underground::get);
      this.beamCount = this.add(new IntSetting("Beam Count", "How many areas get a beam.", 3, 1, 10)).visible(() -> this.underground.get() && this.beams.get());
      this.lowColor = this.add(new ColorSetting("Low Color", "Color of less active areas.", new Color(70, 140, 255, 255))).visible(this.underground::get);
      this.highColor = this.add(new ColorSetting("High Color", "Color of the most active areas.", new Color(255, 70, 60, 255))).visible(this.underground::get);
      this.group("Tracking");
      this.tracer = this.add(new BoolSetting("Tracer", "Line to the player or area you track.", true));
      this.seen = new LinkedHashMap<>();
      this.events = new HashMap<>();
      this.loadedAt = new HashMap<>();
      this.notifiedSpots = new HashSet<>();
      this.hotspots = List.of();
      this.rows = List.of();
      this.setEnabledSilently(true);
   }

   @Override
   public void onDisable() {
      this.reset();
   }

   @Override
   public void onGameLeft() {
      this.reset();
   }

   private void reset() {
      this.seen.clear();
      this.events.clear();
      this.loadedAt.clear();
      this.notifiedSpots.clear();
      this.hotspots = List.of();
      this.rows = List.of();
      this.target = null;
      this.joinedAt = 0L;
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.loadedAt.put(chunk.method_12004(), System.currentTimeMillis());
   }

   private static boolean empty(class_2680 state) {
      return state.method_26215() || !state.method_26227().method_15769() || state.method_27852(class_2246.field_10036);
   }

   private static boolean solid(class_2680 state) {
      return state.method_26216()
         && !state.method_26164(class_3481.field_15503)
         && !state.method_27852(class_2246.field_10255)
         && !state.method_27852(class_2246.field_10102)
         && !state.method_27852(class_2246.field_37568)
         && !state.method_27852(class_2246.field_10092)
         && !state.method_27852(class_2246.field_10540);
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
      if (this.underground.get() && mc.field_1724 != null && pos.method_10264() < this.belowY.get()) {
         if (oldState.method_26204() != newState.method_26204()) {
            boolean player = empty(oldState) && solid(newState)
               || solid(oldState) && empty(newState)
               || empty(oldState) && newState.method_26213() > 0 && newState.method_26227().method_15769();
            if (player) {
               this.record(new class_1923(pos), 2);
            }
         }
      }
   }

   public void onLightUpdate(int chunkX, int chunkZ, class_6606 data) {
      if (this.isActive() && this.underground.get() && mc.field_1687 != null && mc.field_1724 != null) {
         BitSet changed = (BitSet)data.method_38608().clone();
         changed.or(data.method_38601());
         int bottom = mc.field_1687.method_32891() - 1;

         for (int i = changed.nextSetBit(0); i >= 0; i = changed.nextSetBit(i + 1)) {
            if ((bottom + i) * 16 + 15 < this.belowY.get()) {
               this.record(new class_1923(chunkX, chunkZ), 1);
               return;
            }
         }
      }
   }

   private void record(class_1923 chunk, int weight) {
      long now = System.currentTimeMillis();
      Long loaded = this.loadedAt.get(chunk);
      if (loaded == null || now - loaded >= 2500L) {
         class_1923 here = mc.field_1724.method_31476();
         int own = this.ownRadius.get();
         if (Math.abs(here.field_9181 - chunk.field_9181) > own || Math.abs(here.field_9180 - chunk.field_9180) > own) {
            this.events.computeIfAbsent(chunk, k -> new ArrayDeque<>()).addLast(new long[]{now, (long)weight});
         }
      }
   }

   @Override
   public void onTick() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         if (this.joinedAt == 0L) {
            this.joinedAt = now;
         }

         if (this.players.get()) {
            this.updatePlayers(now);
         } else {
            this.seen.clear();
         }

         if (++this.ticks % 20 == 0) {
            if (this.underground.get()) {
               this.updateUnderground(now);
            } else {
               this.hotspots = List.of();
            }

            if (this.ticks % 100 == 0) {
               this.loadedAt.keySet().removeIf(pos -> !mc.field_1687.method_8393(pos.field_9181, pos.field_9180));
            }
         }

         if (this.ticks % 10 == 0) {
            this.buildRows();
         }
      }
   }

   private void updatePlayers(long now) {
      Set<UUID> present = new HashSet<>();

      for (class_742 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && (!this.ignoreNpcs.get() || mc.method_1562() != null && mc.method_1562().method_2871(player.method_5667()) != null)) {
            UUID id = player.method_5667();
            present.add(id);
            ActivityInspector.Seen entry = this.seen.computeIfAbsent(id, ActivityInspector.Seen::new);
            boolean fresh = !entry.visible && (entry.lastSeen == 0L || now - entry.lastSeen > 120000L);
            class_243 pos = new class_243(player.method_23317(), player.method_23318(), player.method_23321());
            double speed = entry.lastTickPos == null
               ? 0.0
               : Math.hypot(pos.field_1352 - entry.lastTickPos.field_1352, pos.field_1350 - entry.lastTickPos.field_1350) * 20.0;
            entry.lastTickPos = pos;
            entry.name = player.method_5477().getString();
            entry.pos = pos;
            entry.visible = true;
            entry.lastSeen = now;
            if (player.method_6128()) {
               entry.state = "flying";
            } else if (player.field_6252) {
               entry.state = "building or fighting";
            } else if (player.method_5715()) {
               entry.state = "sneaking";
            } else if (speed > 5.5) {
               entry.state = "running";
            } else if (speed > 0.6) {
               entry.state = "walking";
            } else {
               entry.state = "standing";
            }

            double distance = (double)player.method_5739(mc.field_1724);
            entry.distances.addLast(new double[]{(double)now, distance});

            while (!entry.distances.isEmpty() && (double)now - ((double[])entry.distances.peekFirst())[0] > 2500.0) {
               entry.distances.pollFirst();
            }

            entry.approaching = !entry.distances.isEmpty() && entry.distances.peekFirst()[1] - distance > 4.0 && distance < 160.0;
            double[] last = entry.trail.peekLast();
            if (last == null || Math.abs(last[0] - pos.field_1352) + Math.abs(last[1] - pos.field_1351) + Math.abs(last[2] - pos.field_1350) > 0.6) {
               entry.trail.addLast(new double[]{pos.field_1352, pos.field_1351 + 0.1, pos.field_1350, (double)now});
            }

            if (fresh && this.notifyPlayers.get() && now - this.joinedAt > 5000L && now - this.lastNotify > 1500L) {
               this.lastNotify = now;
               Notifications.push(
                  "Activity",
                  entry.name
                     + "  "
                     + Math.round(distance)
                     + "m "
                     + direction(pos.field_1352 - mc.field_1724.method_23317(), pos.field_1350 - mc.field_1724.method_23321())
                     + "  "
                     + entry.state,
                  entry.approaching ? Notifications.Type.WARNING : Notifications.Type.INFO
               );
            }
         }
      }

      long keep = (long)this.remember.get().intValue() * 60000L;
      long trailKeep = (long)this.trailLength.get().intValue() * 1000L;
      Iterator<ActivityInspector.Seen> iterator = this.seen.values().iterator();

      while (iterator.hasNext()) {
         ActivityInspector.Seen entryx = iterator.next();
         if (!present.contains(entryx.id)) {
            entryx.visible = false;
            entryx.approaching = false;
            entryx.lastTickPos = null;
         }

         while (!entryx.trail.isEmpty() && now - (long)((double[])entryx.trail.peekFirst())[3] > trailKeep) {
            entryx.trail.pollFirst();
         }

         if (!entryx.visible && now - entryx.lastSeen > keep && !entryx.id.equals(this.target)) {
            iterator.remove();
         }
      }
   }

   private void updateUnderground(long now) {
      long limit = now - (long)this.window.get().intValue() * 60000L;
      Map<class_1923, Float> scores = new HashMap<>();
      Map<class_1923, Long> latest = new HashMap<>();
      Iterator<Entry<class_1923, ArrayDeque<long[]>>> iterator = this.events.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<class_1923, ArrayDeque<long[]>> entry = iterator.next();
         ArrayDeque<long[]> list = entry.getValue();

         while (!list.isEmpty() && ((long[])list.peekFirst())[0] < limit) {
            list.pollFirst();
         }

         if (list.isEmpty()) {
            iterator.remove();
         } else {
            float total = 0.0F;

            for (long[] event : list) {
               total += (float)event[1];
            }

            scores.put(entry.getKey(), total);
            latest.put(entry.getKey(), list.peekLast()[0]);
         }
      }

      String ownKey = this.ignoreOwn.get() ? OwnAreas.currentKey() : null;
      List<ActivityInspector.Hotspot> found = new ArrayList<>();

      for (Entry<class_1923, Float> entry : scores.entrySet()) {
         class_1923 pos = entry.getKey();
         float area = 0.0F;
         long last = 0L;
         boolean peak = true;

         for (int dx = -1; dx <= 1 && peak; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               class_1923 other = new class_1923(pos.field_9181 + dx, pos.field_9180 + dz);
               float value = scores.getOrDefault(other, 0.0F);
               if ((dx != 0 || dz != 0) && (value > entry.getValue() || value == entry.getValue() && (dx < 0 || dx == 0 && dz < 0))) {
                  peak = false;
                  break;
               }

               area += value;
               last = Math.max(last, latest.getOrDefault(other, 0L));
            }
         }

         if (peak && !(area < (float)this.minHits.get().intValue()) && (ownKey == null || !OwnAreas.isNear(ownKey, pos, 1))) {
            found.add(new ActivityInspector.Hotspot(pos, area, now - last < 20000L));
         }
      }

      found.sort((a, b) -> Float.compare(b.score(), a.score()));
      if (found.size() > 10) {
         found = new ArrayList<>(found.subList(0, 10));
      }

      this.hotspots = found;
      if (this.notifyUnderground.get()) {
         for (ActivityInspector.Hotspot spot : found) {
            if (now - this.lastNotify < 2000L) {
               break;
            }

            if (this.notifiedSpots.add(spot.pos())) {
               this.lastNotify = now;
               Notifications.push(
                  "Activity",
                  "Underground activity at X " + spot.pos().method_33940() + " Z " + spot.pos().method_33942() + " (" + Math.round(spot.score()) + " changes)",
                  Notifications.Type.WARNING
               );
            }
         }
      }
   }

   private void buildRows() {
      List<Object> list = new ArrayList<>();
      List<ActivityInspector.Seen> visible = new ArrayList<>();
      List<ActivityInspector.Seen> gone = new ArrayList<>();

      for (ActivityInspector.Seen entry : this.seen.values()) {
         (entry.visible ? visible : gone).add(entry);
      }

      visible.sort((a, b) -> Double.compare(this.distanceTo(a.pos), this.distanceTo(b.pos)));
      gone.sort((a, b) -> Long.compare(b.lastSeen, a.lastSeen));
      list.addAll(visible);
      list.addAll(this.hotspots);
      list.addAll(gone);
      this.rows = list.size() > 16 ? List.copyOf(list.subList(0, 16)) : List.copyOf(list);
   }

   private double distanceTo(class_243 pos) {
      return mc.field_1724 == null ? 0.0 : Math.hypot(pos.field_1352 - mc.field_1724.method_23317(), pos.field_1350 - mc.field_1724.method_23321());
   }

   private static String direction(double dx, double dz) {
      double angle = Math.toDegrees(Math.atan2(dx, -dz));
      return DIRECTIONS[Math.floorMod(Math.round(angle / 45.0), 8)];
   }

   private static String ago(long millis) {
      long seconds = millis / 1000L;
      return seconds < 60L ? seconds + "s" : seconds / 60L + "m";
   }

   private Color heat(float score) {
      float t = Math.max(0.0F, Math.min(1.0F, (score - (float)this.minHits.get().intValue()) / Math.max(1.0F, (float)this.minHits.get().intValue() * 3.0F)));
      float[] low = this.lowColor.get().toHsv();
      float[] high = this.highColor.get().toHsv();
      float hue = high[0] - low[0];
      if (hue > 0.5F) {
         hue--;
      }

      if (hue < -0.5F) {
         hue++;
      }

      return Color.fromHsv(low[0] + hue * t, low[1] + (high[1] - low[1]) * t, low[2] + (high[2] - low[2]) * t, 255);
   }

   private Color colorOf(ActivityInspector.Seen entry) {
      Color base = entry.approaching ? this.dangerColor.get() : this.playerColor.get();
      if (entry.visible) {
         return base;
      } else {
         float[] hsv = base.toHsv();
         return Color.fromHsv(hsv[0], hsv[1] * 0.45F, hsv[2] * 0.85F, 255);
      }
   }

   @Override
   public void onRender(Render3D renderer) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         float tick = mc.method_61966().method_60637(true);
         float pulse = (float)(0.5 + 0.5 * Math.sin((double)now / 1000.0 * 2.5));
         renderer.setDepthTest(false);
         List<ActivityInspector.Hotspot> spots = this.hotspots;
         double y = (double)this.renderY.get().intValue() + 0.05;

         for (int i = 0; i < spots.size(); i++) {
            ActivityInspector.Hotspot spot = spots.get(i);
            Color color = this.heat(spot.score());
            double x1 = (double)spot.pos().method_8326();
            double z1 = (double)spot.pos().method_8328();
            boolean tracked = spot.pos().equals(this.target);
            if (this.squares.get()) {
               renderer.quad(x1, y, z1, x1 + 16.0, y, z1, x1 + 16.0, y, z1 + 16.0, x1, y, z1 + 16.0, color.withAlpha(Math.round(40.0F + 30.0F * pulse)));
               renderer.setLineWidth(tracked ? 3.0F : 2.0F);
               renderer.boxLines(x1, y, z1, x1 + 16.0, y, z1 + 16.0, color.withAlpha(tracked ? 255 : 200));
            }

            if (this.beams.get() && i < this.beamCount.get() || tracked) {
               renderer.beam(x1 + 8.0, z1 + 8.0, y, y + 80.0, tracked ? 0.6 : 0.4, color, 0.75 + 0.25 * (double)pulse);
            }

            if (tracked && this.tracer.get()) {
               renderer.tracer(x1 + 8.0, y + 1.0, z1 + 8.0, color, 2.5F, true);
            }
         }

         long keep = (long)this.remember.get().intValue() * 60000L;
         long trailKeep = (long)this.trailLength.get().intValue() * 1000L;

         for (ActivityInspector.Seen entry : this.seen.values()) {
            Color colorx = this.colorOf(entry);
            boolean trackedx = entry.id.equals(this.target);
            class_243 pos = entry.pos;
            if (entry.visible) {
               class_1657 player = mc.field_1687.method_18470(entry.id);
               if (player != null) {
                  pos = player.method_30950(tick);
                  if (this.boxes.get()) {
                     double w = (double)(player.method_17681() / 2.0F) + 0.05;
                     double h = (double)player.method_17682() + 0.05;
                     renderer.boxSides(
                        pos.field_1352 - w,
                        pos.field_1351,
                        pos.field_1350 - w,
                        pos.field_1352 + w,
                        pos.field_1351 + h,
                        pos.field_1350 + w,
                        colorx.scaleAlpha(0.12 + 0.06 * (double)pulse)
                     );
                     renderer.setLineWidth(trackedx ? 3.0F : 2.0F);
                     renderer.boxLines(
                        pos.field_1352 - w,
                        pos.field_1351,
                        pos.field_1350 - w,
                        pos.field_1352 + w,
                        pos.field_1351 + h,
                        pos.field_1350 + w,
                        colorx.scaleAlpha(0.9)
                     );
                  }
               }
            } else if (this.lastSeenBeams.get() || trackedx) {
               double fade = Math.max(0.2, 1.0 - (double)(now - entry.lastSeen) / (double)keep);
               renderer.beam(
                  pos.field_1352,
                  pos.field_1350,
                  pos.field_1351,
                  pos.field_1351 + 40.0,
                  trackedx ? 0.45 : 0.3,
                  colorx.scaleAlpha(fade),
                  0.75 + 0.25 * (double)pulse
               );
            }

            if (this.trails.get() && entry.trail.size() >= 2) {
               renderer.setLineWidth(2.0F);
               double[] previous = null;

               for (double[] point : entry.trail) {
                  if (previous != null) {
                     double a1 = Math.max(0.0, 1.0 - ((double)now - previous[3]) / (double)trailKeep);
                     double a2 = Math.max(0.0, 1.0 - ((double)now - point[3]) / (double)trailKeep);
                     renderer.lineGradient(
                        previous[0], previous[1], previous[2], point[0], point[1], point[2], colorx.scaleAlpha(0.8 * a1), colorx.scaleAlpha(0.8 * a2)
                     );
                  }

                  previous = point;
               }
            }

            if (trackedx && this.tracer.get()) {
               renderer.tracer(pos.field_1352, pos.field_1351 + 1.0, pos.field_1350, colorx, 2.5F, true);
            }
         }

         renderer.setLineWidth(2.0F);
      }
   }

   @Override
   public int presetCount() {
      return Math.max(1, this.rows.size());
   }

   @Override
   public String presetName(int index) {
      List<Object> list = this.rows;
      if (!list.isEmpty()) {
         if (index < list.size() && mc.field_1724 != null) {
            Object row = list.get(index);
            if (row instanceof ActivityInspector.Seen entry) {
               double dx = entry.pos.field_1352 - mc.field_1724.method_23317();
               double dz = entry.pos.field_1350 - mc.field_1724.method_23321();
               String where = Math.round(Math.hypot(dx, dz)) + "m " + direction(dx, dz);
               return entry.visible
                  ? entry.name + "  " + where + "  " + entry.state + (entry.approaching ? "  approaching" : "")
                  : entry.name + "  seen " + ago(System.currentTimeMillis() - entry.lastSeen) + " ago  " + where;
            } else {
               ActivityInspector.Hotspot spot = (ActivityInspector.Hotspot)row;
               double dx = (double)spot.pos().method_33940() - mc.field_1724.method_23317();
               double dz = (double)spot.pos().method_33942() - mc.field_1724.method_23321();
               return (spot.live() ? "LIVE " : "")
                  + "below Y "
                  + this.belowY.get()
                  + "  "
                  + Math.round(spot.score())
                  + " changes  "
                  + Math.round(Math.hypot(dx, dz))
                  + "m "
                  + direction(dx, dz);
            }
         } else {
            return "";
         }
      } else {
         return !this.players.get() && !this.underground.get() ? "Players and Underground are off" : "Nobody around yet";
      }
   }

   @Override
   public int presetColor(int index) {
      List<Object> list = this.rows;
      if (index >= list.size()) {
         return -9801856;
      } else {
         Object row = list.get(index);
         return row instanceof ActivityInspector.Seen entry
            ? 0xFF000000 | this.colorOf(entry).packed() & 16777215
            : 0xFF000000 | this.heat(((ActivityInspector.Hotspot)row).score()).packed() & 16777215;
      }
   }

   private static Object key(Object row) {
      return row instanceof ActivityInspector.Seen entry ? entry.id : ((ActivityInspector.Hotspot)row).pos();
   }

   @Override
   public boolean presetActive(int index) {
      List<Object> list = this.rows;
      return index < list.size() && key(list.get(index)).equals(this.target);
   }

   @Override
   public void selectPreset(int index) {
      List<Object> list = this.rows;
      if (index < list.size()) {
         Object key = key(list.get(index));
         this.target = key.equals(this.target) ? null : key;
      }
   }

   @Override
   public String presetDescription() {
      return "Click a player or area to track it with a beam and tracer. Click again to stop.";
   }

   @Override
   public String getInfoString() {
      int count = 0;

      for (ActivityInspector.Seen entry : this.seen.values()) {
         if (entry.visible) {
            count++;
         }
      }

      return count == 0 ? null : String.valueOf(count);
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   @Override
   public boolean bindable() {
      return false;
   }

   public static record Hotspot(class_1923 pos, float score, boolean live) {
   }

   private static final class Seen {
      final UUID id;
      String name = "";
      class_243 pos = class_243.field_1353;
      class_243 lastTickPos;
      long lastSeen;
      boolean visible;
      boolean approaching;
      String state = "standing";
      final ArrayDeque<double[]> trail = new ArrayDeque<>();
      final ArrayDeque<double[]> distances = new ArrayDeque<>();

      Seen(UUID id) {
         this.id = id;
      }
   }
}
