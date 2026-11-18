package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.mixin.ChunkLightProviderAccessor;
import dev.violet.mixin.ChunkToNibbleArrayMapAccessor;
import dev.violet.mixin.LightStorageAccessor;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.render.ShapeMode;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1944;
import net.minecraft.class_2804;
import net.minecraft.class_3556;
import net.minecraft.class_3558;
import net.minecraft.class_4076;

public class LightFinder extends Module {
   private final Setting<Integer> maxY = this.add(new IntSetting("Max Y", "Only show light below this Y level.", 0, -64, 320));
   private final Setting<Integer> minLight = this.add(new IntSetting("Min Light", "Minimum block light level to show.", 1, 1, 15));
   private final Setting<Integer> range = this.add(new IntSetting("Range", "Horizontal range in chunks.", 8, 1, 32));
   private final Setting<Integer> maxBlocks = this.add(new IntSetting("Max Blocks", "Limit of rendered blocks to keep the framerate up.", 20000, 100, 100000));
   private final Setting<Integer> updateInterval = this.add(new IntSetting("Update Interval", "Ticks between light scans.", 20, 1, 200));
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Color> dimColor;
   private final Setting<Color> brightColor;
   private volatile List<LightFinder.LitBlock> litBlocks;
   private int timer;
   private long[] pendingKeys;
   private int pendingIndex;
   private int perTick;
   private List<LightFinder.LitBlock> building;
   private boolean first;

   public LightFinder() {
      super("Light Finder", "Renders block light sections for base finding.", Category.BASE_FINDING);
      this.group("Render");
      this.shapeMode = this.add(new EnumSetting<>("Shape Mode", "How lit blocks are drawn.", ShapeMode.Lines));
      this.dimColor = this.add(new ColorSetting("Dim Color", "Color for light level 1.", new Color(40, 40, 50, 80)));
      this.brightColor = this.add(new ColorSetting("Bright Color", "Color for light level 15.", new Color(255, 255, 50, 120)));
      this.litBlocks = List.of();
      this.perTick = 1;
   }

   @Override
   public void onEnable() {
      this.timer = 0;
      this.litBlocks = List.of();
      this.pendingKeys = null;
      this.building = null;
      this.first = true;
   }

   @Override
   public void onDisable() {
      this.litBlocks = List.of();
      this.pendingKeys = null;
      this.building = null;
   }

   @Override
   public void onTick() {
      try {
         if (this.pendingKeys == null) {
            if (this.timer-- > 0) {
               return;
            }

            long[] keys = this.arrays() == null ? new long[0] : this.arrays().keySet().toLongArray();
            if (this.first) {
               this.first = false;
               this.pendingKeys = keys;
               this.pendingIndex = 0;
               this.building = new ArrayList<>();
               this.collectStep(keys.length);
               return;
            }

            this.pendingKeys = keys;
            this.pendingIndex = 0;
            this.building = new ArrayList<>();
            this.perTick = Math.max(1, (keys.length + this.updateInterval.get() - 1) / Math.max(1, this.updateInterval.get()));
         }

         this.collectStep(this.perTick);
      } catch (Exception var2) {
         SoberClient.LOG.error("LightFinder failed to read light data", var2);
         this.litBlocks = List.of();
         this.pendingKeys = null;
         this.timer = this.updateInterval.get();
      }
   }

   private Long2ObjectMap<class_2804> arrays() {
      if (mc.field_1687.method_22336().method_15562(class_1944.field_9282) instanceof class_3558<?, ?> provider) {
         class_3556<? extends class_3556<?>> storage = (class_3556<? extends class_3556<?>>)((LightStorageAccessor)((ChunkLightProviderAccessor)provider)
               .violet$getLightStorage())
            .violet$getStorage();
         return ((ChunkToNibbleArrayMapAccessor)storage).violet$getArrays();
      } else {
         return null;
      }
   }

   private void collectStep(int count) {
      Long2ObjectMap<class_2804> arrays = this.arrays();
      int playerChunkX = mc.field_1724.method_31476().field_9181;
      int playerChunkZ = mc.field_1724.method_31476().field_9180;
      int chunkRange = this.range.get();
      int yLimit = this.maxY.get();
      int min = this.minLight.get();
      int limit = this.maxBlocks.get();
      List<LightFinder.LitBlock> result = this.building;
      int end = Math.min(this.pendingKeys.length, this.pendingIndex + count);
      boolean full = false;

      label81:
      while (this.pendingIndex < end && !full && arrays != null) {
         long key = this.pendingKeys[this.pendingIndex++];
         class_2804 array = (class_2804)arrays.get(key);
         if (array != null && !array.method_51380()) {
            class_4076 section = class_4076.method_18677(key);
            if (section.method_19528() < yLimit
               && Math.abs(section.method_18674() - playerChunkX) <= chunkRange
               && Math.abs(section.method_18687() - playerChunkZ) <= chunkRange) {
               for (int x = 0; x < 16; x++) {
                  for (int y = 0; y < 16; y++) {
                     int worldY = section.method_19528() + y;
                     if (worldY >= yLimit) {
                        break;
                     }

                     for (int z = 0; z < 16; z++) {
                        int level = array.method_12139(x, y, z);
                        if (level >= min) {
                           result.add(new LightFinder.LitBlock(section.method_19527() + x, worldY, section.method_19529() + z, level));
                           if (result.size() >= limit) {
                              full = true;
                              continue label81;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (full || arrays == null || this.pendingIndex >= this.pendingKeys.length) {
         this.litBlocks = result;
         this.pendingKeys = null;
         this.building = null;
         this.timer = 0;
      }
   }

   @Override
   public void onRender(Render3D renderer) {
      Color dim = this.dimColor.get();
      Color bright = this.brightColor.get();
      Color[] palette = new Color[16];

      for (int level = 1; level < 16; level++) {
         palette[level] = dim.lerp(bright, (double)(level - 1) / 14.0);
      }

      for (LightFinder.LitBlock block : this.litBlocks) {
         Color color = palette[Math.max(1, Math.min(15, block.level))];
         renderer.box(
            (double)block.x,
            (double)block.y,
            (double)block.z,
            (double)(block.x + 1),
            (double)(block.y + 1),
            (double)(block.z + 1),
            color,
            color,
            this.shapeMode.get()
         );
      }
   }

   private static record LitBlock(int x, int y, int z, int level) {
   }
}
