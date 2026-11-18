package dev.violet.modules;

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
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2806;
import net.minecraft.class_2818;

public class BlockEntityDebug extends Module implements HudOverlayProvider {
   private static final Map<class_2591<?>, BlockEntityDebug.Group> GROUPS = new IdentityHashMap<>();
   private final Setting<Boolean> storage;
   private final Setting<Boolean> utility;
   private final Setting<Boolean> furnaces;
   private final Setting<Boolean> signs;
   private final Setting<Boolean> beds;
   private final Setting<Boolean> banners;
   private final Setting<Boolean> heads;
   private final Setting<Boolean> workshop;
   private final Setting<Boolean> natural;
   private final Setting<Integer> minCount;
   private final Setting<Integer> range;
   private final Setting<Boolean> labels;
   private final Setting<Boolean> notify;
   private final Setting<Boolean> chat;
   private final Setting<Color> color;
   private final Setting<Color> hotColor;
   private final Setting<Boolean> beams;
   private final Setting<Boolean> tracers;
   private final Long2ObjectOpenHashMap<BlockEntityDebug.Found> found;
   private final LongOpenHashSet announced;
   private final List<BlockEntityDebug.Label> shown;
   private int rescan;

   private static void put(BlockEntityDebug.Group group, class_2591<?>... types) {
      for (class_2591<?> type : types) {
         GROUPS.put(type, group);
      }
   }

   public BlockEntityDebug() {
      super(
         "Block Entity Debug",
         "Counts chests, furnaces, signs and other block entities per chunk and lights up chunks where someone built a lot.",
         Category.BASE_FINDING
      );
      this.group("Count");
      this.storage = this.add(new BoolSetting("Storage", "Chests, trapped chests, barrels, shulker boxes and ender chests.", true));
      this.utility = this.add(new BoolSetting("Hoppers & Droppers", "Hoppers, droppers, dispensers and crafters.", true));
      this.furnaces = this.add(new BoolSetting("Furnaces", "Furnaces, blast furnaces and smokers.", true));
      this.signs = this.add(new BoolSetting("Signs", "Signs and hanging signs.", true));
      this.beds = this.add(new BoolSetting("Beds", "Beds.", true));
      this.banners = this.add(new BoolSetting("Banners", "Banners.", true));
      this.heads = this.add(new BoolSetting("Heads", "Player and mob heads.", true));
      this.workshop = this.add(
         new BoolSetting(
            "Workshop", "Brewing stands, enchanting tables, beacons, lecterns, jukeboxes, campfires, pots, bookshelves, shelves and redstone parts.", true
         )
      );
      this.natural = this.add(
         new BoolSetting(
            "Natural",
            "Spawners, vaults, bee nests, bells, sculk and other blocks that also generate naturally. Off by default so villages and dungeons do not show up.",
            false
         )
      );
      this.group("Find");
      this.minCount = this.add(new IntSetting("Min Count", "Block entities a chunk needs before it lights up.", 6, 1, 64));
      this.range = this.add(new IntSetting("Label Range", "Chunks closer than this many blocks get a label with what is inside.", 160, 16, 512));
      this.labels = this.add(new BoolSetting("Labels", "Shows the counts above every found chunk.", true));
      this.notify = this.add(new BoolSetting("Notify", "Notification when a new chunk lights up.", true));
      this.chat = this.add(new BoolSetting("Chat Message", "Also writes the find into the chat.", false)).visible(this.notify::get);
      this.group("Look");
      this.color = this.add(new ColorSetting("Color", "Color of the chunk tiles. Pick any color on the wheel.", new Color(167, 139, 250, 200)));
      this.hotColor = this.add(new ColorSetting("Busy Color", "Color of chunks with three times the minimum or more.", new Color(255, 92, 168, 220)));
      this.beams = this.add(new BoolSetting("Beams", "A beam above every found chunk.", false));
      this.tracers = this.add(new BoolSetting("Tracers", "Lines to every found chunk.", false));
      this.found = new Long2ObjectOpenHashMap();
      this.announced = new LongOpenHashSet();
      this.shown = new ArrayList<>();
   }

   @Override
   public void onEnable() {
      this.found.clear();
      this.announced.clear();
      this.rescan = 0;
   }

   @Override
   public void onDisable() {
      this.found.clear();
      this.shown.clear();
   }

   @Override
   public void onGameLeft() {
      this.found.clear();
      this.announced.clear();
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.scan(chunk);
   }

   @Override
   public void onTick() {
      if (mc.field_1687 != null && mc.field_1724 != null && --this.rescan <= 0) {
         this.rescan = 40;
         class_1923 center = mc.field_1724.method_31476();
         int radius = mc.field_1690.method_38521() + 1;
         LongOpenHashSet seen = new LongOpenHashSet();

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               class_2818 chunk = mc.field_1687.method_2935().method_2857(center.field_9181 + dx, center.field_9180 + dz, class_2806.field_12803, false);
               if (chunk != null) {
                  seen.add(chunk.method_12004().method_8324());
                  this.scan(chunk);
               }
            }
         }

         this.found.keySet().removeIf(key -> !seen.contains(key));
      }
   }

   private boolean counts(BlockEntityDebug.Group group) {
      return switch (group) {
         case STORAGE -> this.storage.get();
         case UTILITY -> this.utility.get();
         case FURNACE -> this.furnaces.get();
         case SIGN -> this.signs.get();
         case BED -> this.beds.get();
         case BANNER -> this.banners.get();
         case HEAD -> this.heads.get();
         case WORKSHOP -> this.workshop.get();
         case NATURAL -> this.natural.get();
      };
   }

   private void scan(class_2818 chunk) {
      EnumMap<BlockEntityDebug.Group, Integer> counts = new EnumMap<>(BlockEntityDebug.Group.class);
      int total = 0;
      double ySum = 0.0;

      for (Entry<class_2338, class_2586> entry : chunk.method_12214().entrySet()) {
         BlockEntityDebug.Group group = GROUPS.get(entry.getValue().method_11017());
         if (group != null && this.counts(group)) {
            counts.merge(group, Integer.valueOf(1), Integer::sum);
            total++;
            ySum += (double)entry.getKey().method_10264();
         }
      }

      long key = chunk.method_12004().method_8324();
      if (total < this.minCount.get()) {
         this.found.remove(key);
      } else {
         BlockEntityDebug.Found result = new BlockEntityDebug.Found(total, ySum / (double)total + 1.05, counts);
         this.found.put(key, result);
         if (this.announced.add(key) && this.notify.get() && mc.field_1724 != null) {
            int x = chunk.method_12004().method_8326() + 8;
            int z = chunk.method_12004().method_8328() + 8;
            int distance = (int)Math.round(Math.sqrt(mc.field_1724.method_5649((double)x, mc.field_1724.method_23318(), (double)z)));
            String message = total + " block entities at " + x + " " + Math.round(result.y()) + " " + z + " (" + distance + "m): " + summary(counts, 3);
            Notifications.push("Block Entities", message, Notifications.Type.SUCCESS);
            if (this.chat.get()) {
               this.info(message, new Object[0]);
            }
         }
      }
   }

   private static String summary(EnumMap<BlockEntityDebug.Group, Integer> counts, int max) {
      List<Entry<BlockEntityDebug.Group, Integer>> entries = new ArrayList<>(counts.entrySet());
      entries.sort((a, b) -> b.getValue() - a.getValue());
      StringBuilder text = new StringBuilder();

      for (int i = 0; i < entries.size() && i < max; i++) {
         if (i > 0) {
            text.append(" · ");
         }

         text.append(entries.get(i).getValue()).append(' ').append(entries.get(i).getKey().label);
      }

      return text.toString();
   }

   private Color colorFor(BlockEntityDebug.Found result) {
      double heat = Math.min(1.0, (double)(result.total() - this.minCount.get()) / (double)Math.max(1, this.minCount.get() * 2));
      return this.color.get().lerp(this.hotColor.get(), heat);
   }

   @Override
   public void onRender(Render3D renderer) {
      this.shown.clear();
      if (!this.found.isEmpty() && mc.field_1724 != null) {
         double maxSq = (double)this.range.get().intValue() * (double)this.range.get().intValue();
         ObjectIterator var4 = this.found.long2ObjectEntrySet().iterator();

         while (var4.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<BlockEntityDebug.Found> entry = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<BlockEntityDebug.Found>)var4.next();
            class_1923 pos = new class_1923(entry.getLongKey());
            BlockEntityDebug.Found result = (BlockEntityDebug.Found)entry.getValue();
            Color c = this.colorFor(result);
            ChunkHighlight.draw(renderer, pos.field_9181, pos.field_9180, result.y(), c, this.beams.get());
            if (this.tracers.get()) {
               ChunkHighlight.tracer(renderer, pos.field_9181, pos.field_9180, result.y(), c.scaleAlpha(0.8));
            }

            class_243 center = new class_243((double)(pos.method_8326() + 8), result.y() + 1.2, (double)(pos.method_8328() + 8));
            if (this.labels.get() && mc.field_1724.method_5707(center) < maxSq) {
               this.shown.add(new BlockEntityDebug.Label(center, result.total() + " block entities", summary(result.counts(), 4)));
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
      for (BlockEntityDebug.Label label : this.shown) {
         float[] screen = WorldLabel.project(mc, label.pos(), screenW, screenH);
         if (screen != null) {
            WorldLabel.drawText(canvas, screen[0], screen[1], label.primary(), label.secondary(), 1.0F);
         }
      }
   }

   @Override
   public String getInfoString() {
      return this.found.isEmpty() ? null : String.valueOf(this.found.size());
   }

   static {
      put(
         BlockEntityDebug.Group.STORAGE, class_2591.field_11914, class_2591.field_11891, class_2591.field_16411, class_2591.field_11896, class_2591.field_11901
      );
      put(BlockEntityDebug.Group.UTILITY, class_2591.field_11888, class_2591.field_11899, class_2591.field_11887, class_2591.field_46808);
      put(BlockEntityDebug.Group.FURNACE, class_2591.field_11903, class_2591.field_16415, class_2591.field_16414);
      put(BlockEntityDebug.Group.SIGN, class_2591.field_11911, class_2591.field_40330);
      put(BlockEntityDebug.Group.BED, class_2591.field_11910);
      put(BlockEntityDebug.Group.BANNER, class_2591.field_11905);
      put(BlockEntityDebug.Group.HEAD, class_2591.field_11913);
      put(
         BlockEntityDebug.Group.WORKSHOP,
         class_2591.field_11894,
         class_2591.field_11912,
         class_2591.field_11890,
         class_2591.field_11902,
         class_2591.field_16412,
         class_2591.field_11907,
         class_2591.field_17380,
         class_2591.field_42781,
         class_2591.field_40329,
         class_2591.field_11908,
         class_2591.field_11900,
         class_2591.field_61437
      );
      put(
         BlockEntityDebug.Group.NATURAL,
         class_2591.field_11889,
         class_2591.field_47352,
         class_2591.field_48859,
         class_2591.field_20431,
         class_2591.field_16413,
         class_2591.field_42780,
         class_2591.field_28117,
         class_2591.field_37648,
         class_2591.field_37647,
         class_2591.field_43258,
         class_2591.field_11906,
         class_2591.field_11898,
         class_2591.field_11897
      );
   }

   private static record Found(int total, double y, EnumMap<BlockEntityDebug.Group, Integer> counts) {
   }

   private static enum Group {
      STORAGE("Storage"),
      UTILITY("Hoppers & Droppers"),
      FURNACE("Furnaces"),
      SIGN("Signs"),
      BED("Beds"),
      BANNER("Banners"),
      HEAD("Heads"),
      WORKSHOP("Workshop"),
      NATURAL("Natural");

      final String label;

      private Group(String label) {
         this.label = label;
      }
   }

   private static record Label(class_243 pos, String primary, String secondary) {
   }
}
