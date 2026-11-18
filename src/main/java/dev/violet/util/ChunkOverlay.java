package dev.violet.util;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_9848;

public final class ChunkOverlay {
   public static final double MARKER_Y = 63.0;
   private static final float EDGE_SHADE = 0.77F;
   private static final float LINE_WIDTH = 1.5F;
   private static final double CORNER_RADIUS = 2.0;
   private static final int ARC_SEGMENTS = 4;
   private static final float GROW_SECONDS = 0.3F;
   private static final float SHRINK_SECONDS = 0.25F;
   private static final float SETTLE_SECONDS = 0.25F;
   private static final float MAX_WAIT_SECONDS = 1.0F;
   private static final float RESET_GAP_SECONDS = 0.5F;
   private final Long2FloatOpenHashMap shown = new Long2FloatOpenHashMap();
   private final LongOpenHashSet pending = new LongOpenHashSet();
   private final LongOpenHashSet done = new LongOpenHashSet();
   private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
   private final LongArrayList group = new LongArrayList();
   private final ChunkTileGeometry.Cache mobileTiles = new ChunkTileGeometry.Cache();
   private long lastNanos;
   private long pendingSince;
   private long lastAdded;
   private final boolean neon;
   private final boolean square;

   public ChunkOverlay() {
      this(false);
   }

   public ChunkOverlay(boolean neon) {
      this(neon, false);
   }

   public ChunkOverlay(boolean neon, boolean square) {
      this.neon = neon;
      this.square = square;
   }

   private ChunkOverlay.Look look(int fill) {
      if (!this.neon) {
         int edge = class_9848.method_61330(Math.min(255, class_9848.method_61320(fill) * 3 / 2), class_9848.method_61321(fill, 0.77F));
         return new ChunkOverlay.Look(fill, edge, 0, 0, 0, false);
      } else {
         return new ChunkOverlay.Look(
            class_9848.method_61330(Math.round((float)class_9848.method_61320(fill) * 0.55F), fill),
            class_9848.method_61330(255, whiten(fill, 0.55F)),
            class_9848.method_61330(112, whiten(fill, 0.3F)),
            class_9848.method_61330(70, whiten(fill, 0.3F)),
            class_9848.method_61330(64, whiten(fill, 0.35F)),
            true
         );
      }
   }

   private static int whiten(int color, float amount) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      return Math.round((float)r + (float)(255 - r) * amount) << 16
         | Math.round((float)g + (float)(255 - g) * amount) << 8
         | Math.round((float)b + (float)(255 - b) * amount);
   }

   public void draw(LongSet chunks, int fill, float partialTick) {
      boolean mobile = MobilePerformance.active();
      if (mobile) {
         this.mobileTiles.beginFrame();
      } else {
         this.mobileTiles.clear();
      }

      long now = System.nanoTime();
      float dt = this.lastNanos == 0L ? 0.0F : (float)(now - this.lastNanos) / 1.0E9F;
      this.lastNanos = now;
      if (dt > 0.5F) {
         this.shown.clear();
         this.pending.clear();
         this.mobileTiles.clear();
      }

      this.collect(chunks, now);
      this.step(chunks, dt);
      if (this.shown.isEmpty()) {
         this.mobileTiles.clear();
      } else {
         ChunkOverlay.Look look = this.look(fill);
         FoglessLines lines = FoglessLines.forFrame(partialTick);
         this.done.clear();
         LongIterator keys = this.shown.keySet().iterator();

         while (keys.hasNext()) {
            long start = keys.nextLong();
            if (this.done.add(start)) {
               this.drawGroup(lines, chunks, start, look);
            }
         }
      }
   }

   private void collect(LongSet chunks, long now) {
      LongIterator keys = chunks.iterator();

      while (keys.hasNext()) {
         long key = keys.nextLong();
         if (!this.shown.containsKey(key) && !this.pending.contains(key)) {
            if (this.pending.isEmpty()) {
               this.pendingSince = now;
            }

            this.pending.add(key);
            this.lastAdded = now;
         }
      }

      keys = this.pending.iterator();

      while (keys.hasNext()) {
         if (!chunks.contains(keys.nextLong())) {
            keys.remove();
         }
      }

      if (!this.pending.isEmpty() && (!((float)(now - this.lastAdded) / 1.0E9F < 0.25F) || !((float)(now - this.pendingSince) / 1.0E9F < 1.0F))) {
         keys = this.pending.iterator();

         while (keys.hasNext()) {
            this.shown.put(keys.nextLong(), 0.0F);
         }

         this.pending.clear();
      }
   }

   private void step(LongSet chunks, float dt) {
      ObjectIterator<Entry> entries = this.shown.long2FloatEntrySet().fastIterator();

      while (entries.hasNext()) {
         Entry entry = (Entry)entries.next();
         if (chunks.contains(entry.getLongKey())) {
            entry.setValue(Math.min(1.0F, entry.getFloatValue() + dt / 0.3F));
         } else {
            float left = entry.getFloatValue() - dt / 0.25F;
            if (left <= 0.0F) {
               entries.remove();
            } else {
               entry.setValue(left);
            }
         }
      }
   }

   private void drawGroup(FoglessLines lines, LongSet chunks, long start, ChunkOverlay.Look look) {
      float progress = this.shown.get(start);
      boolean present = chunks.contains(start);
      this.group.clear();
      this.queue.enqueue(start);
      double sumX = 0.0;
      double sumZ = 0.0;

      while (!this.queue.isEmpty()) {
         long key = this.queue.dequeueLong();
         this.group.add(key);
         int cx = class_1923.method_8325(key);
         int cz = class_1923.method_8332(key);
         sumX += (double)(cx << 4) + 8.0;
         sumZ += (double)(cz << 4) + 8.0;
         this.visit(chunks, class_1923.method_8331(cx, cz - 1), progress, present);
         this.visit(chunks, class_1923.method_8331(cx, cz + 1), progress, present);
         this.visit(chunks, class_1923.method_8331(cx - 1, cz), progress, present);
         this.visit(chunks, class_1923.method_8331(cx + 1, cz), progress, present);
      }

      float size = easeOut(progress);
      if (!(size <= 0.001F)) {
         double mx = sumX / (double)this.group.size();
         double mz = sumZ / (double)this.group.size();
         ChunkOverlay.Look groupLook = size >= 1.0F ? look : look.fade(size);

         for (int i = 0; i < this.group.size(); i++) {
            long key = this.group.getLong(i);
            int cx = class_1923.method_8325(key);
            int cz = class_1923.method_8332(key);
            double x0 = mx + ((double)(cx << 4) - mx) * (double)size;
            double z0 = mz + ((double)(cz << 4) - mz) * (double)size;
            double x1 = x0 + 16.0 * (double)size;
            double z1 = z0 + 16.0 * (double)size;
            boolean north = !this.joined(chunks, class_1923.method_8331(cx, cz - 1), progress, present);
            boolean south = !this.joined(chunks, class_1923.method_8331(cx, cz + 1), progress, present);
            boolean west = !this.joined(chunks, class_1923.method_8331(cx - 1, cz), progress, present);
            boolean east = !this.joined(chunks, class_1923.method_8331(cx + 1, cz), progress, present);
            if (MobilePerformance.active() && size >= 1.0F) {
               int mask = (north ? 1 : 0) | (south ? 2 : 0) | (west ? 4 : 0) | (east ? 8 : 0);
               ChunkTileGeometry geometry = this.mobileTiles.get(key, x0, z0, x1, z1, this.square ? 0.0 : 2.0 * (double)size, mask);

               for (int edgeIndex = 0; edgeIndex < geometry.curves.length; edgeIndex += 2) {
                  stroke(lines, geometry.curves[edgeIndex], geometry.curves[edgeIndex + 1], groupLook);
               }

               class_12180.method_75553(new ChunkOverlay.Plane(geometry.outline, groupLook.fill())).method_75533();
               grid(lines, x0, z0, x1, z1, groupLook);

               for (int edgeIndex = 0; edgeIndex < geometry.edges.length; edgeIndex += 2) {
                  stroke(lines, geometry.edges[edgeIndex], geometry.edges[edgeIndex + 1], groupLook);
               }
            } else {
               tile(lines, x0, z0, x1, z1, this.square ? 0.0 : 2.0 * (double)size, north, south, west, east, groupLook);
            }
         }
      }
   }

   private void visit(LongSet chunks, long key, float progress, boolean present) {
      if (this.joined(chunks, key, progress, present) && this.done.add(key)) {
         this.queue.enqueue(key);
      }
   }

   private boolean joined(LongSet chunks, long key, float progress, boolean present) {
      return this.shown.containsKey(key) && this.shown.get(key) == progress && chunks.contains(key) == present;
   }

   private static void tile(
      FoglessLines lines,
      double x0,
      double z0,
      double x1,
      double z1,
      double radius,
      boolean north,
      boolean south,
      boolean west,
      boolean east,
      ChunkOverlay.Look look
   ) {
      double nw = north && west ? radius : 0.0;
      double ne = north && east ? radius : 0.0;
      double se = south && east ? radius : 0.0;
      double sw = south && west ? radius : 0.0;
      List<class_243> outline = new ArrayList<>(20);
      corner(outline, lines, x0 + nw, z0 + nw, nw, 180.0, look);
      corner(outline, lines, x1 - ne, z0 + ne, ne, 270.0, look);
      corner(outline, lines, x1 - se, z1 - se, se, 0.0, look);
      corner(outline, lines, x0 + sw, z1 - sw, sw, 90.0, look);
      class_12180.method_75553(new ChunkOverlay.Plane(outline.toArray(new class_243[0]), look.fill())).method_75533();
      if (north) {
         edge(lines, x0 + nw, z0, x1 - ne, z0, look);
      }

      if (south) {
         edge(lines, x0 + sw, z1, x1 - se, z1, look);
      }

      if (west) {
         edge(lines, x0, z0 + nw, x0, z1 - sw, look);
      }

      if (east) {
         edge(lines, x1, z0 + ne, x1, z1 - se, look);
      }

      grid(lines, x0, z0, x1, z1, look);
   }

   private static void corner(List<class_243> outline, FoglessLines lines, double cx, double cz, double radius, double startDegrees, ChunkOverlay.Look look) {
      if (radius <= 0.0) {
         outline.add(new class_243(cx, 63.0, cz));
      } else {
         class_243 previous = null;

         for (int i = 0; i <= 4; i++) {
            double angle = Math.toRadians(startDegrees + 90.0 * (double)i / 4.0);
            class_243 point = new class_243(cx + Math.cos(angle) * radius, 63.0, cz + Math.sin(angle) * radius);
            outline.add(point);
            if (previous != null) {
               stroke(lines, previous, point, look);
            }

            previous = point;
         }
      }
   }

   private static float easeOut(float t) {
      float inv = 1.0F - t;
      return 1.0F - inv * inv * inv;
   }

   private static int fade(int color, float amount) {
      return class_9848.method_61330(Math.round((float)class_9848.method_61320(color) * amount), color);
   }

   private static void edge(FoglessLines lines, double ax, double az, double bx, double bz, ChunkOverlay.Look look) {
      stroke(lines, new class_243(ax, 63.0, az), new class_243(bx, 63.0, bz), look);
   }

   private static void stroke(FoglessLines lines, class_243 from, class_243 to, ChunkOverlay.Look look) {
      if (look.neon()) {
         lines.line(from, to, look.halo(), 7.0F);
         lines.line(from, to, look.glow(), 3.5F);
         lines.line(from, to, look.edge(), 1.6F);
      } else {
         lines.line(from, to, look.edge(), 1.5F);
      }
   }

   private static void grid(FoglessLines lines, double x0, double z0, double x1, double z1, ChunkOverlay.Look look) {
      if (look.neon() && class_9848.method_61320(look.grid()) != 0) {
         double insetX = (x1 - x0) * 0.025;
         double insetZ = (z1 - z0) * 0.025;

         for (int k = 1; k < 4; k++) {
            double x = x0 + (x1 - x0) * (double)k / 4.0;
            double z = z0 + (z1 - z0) * (double)k / 4.0;
            lines.line(x, 63.0, z0 + insetZ, x, 63.0, z1 - insetZ, look.grid(), 1.0F);
            lines.line(x0 + insetX, 63.0, z, x1 - insetX, 63.0, z, look.grid(), 1.0F);
         }
      }
   }

   private static record Look(int fill, int edge, int glow, int halo, int grid, boolean neon) {
      ChunkOverlay.Look fade(float amount) {
         return new ChunkOverlay.Look(
            ChunkOverlay.fade(this.fill, amount),
            ChunkOverlay.fade(this.edge, amount),
            ChunkOverlay.fade(this.glow, amount),
            ChunkOverlay.fade(this.halo, amount),
            ChunkOverlay.fade(this.grid, amount),
            this.neon
         );
      }
   }

   private static record Plane(class_243[] outline, int color) implements class_12175 {
      public void method_75531(class_12177 primitives, float alpha) {
         primitives.method_75478(this.outline, ChunkOverlay.fade(this.color, alpha));
      }
   }
}
