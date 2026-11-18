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
import dev.violet.util.Shafts;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_2818;

public class TunnelHoleESP extends Module {
   private final Setting<Integer> minDepth = this.add(new IntSetting("Min Depth", "Minimum height of a shaft to be shown.", 4, 1, 20))
      .onChanged(v -> this.rescan());
   private final Setting<Boolean> holes1x1 = this.add(new BoolSetting("1x1 Holes", "Shows 1x1 shafts.", true)).onChanged(v -> this.rescan());
   private final Setting<Boolean> holes3x1 = this.add(new BoolSetting("3x1 Holes", "Shows 3x1 shafts.", true)).onChanged(v -> this.rescan());
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Color> color1x1;
   private final Setting<Color> color3x1;
   private final Map<class_1923, List<Shafts.Shaft>> holes;
   private int ticks;
   private final ChunkScanner scanner;

   public TunnelHoleESP() {
      super("Tunnel Hole ESP", "Shows long vertical 1x1 and 3x1 shafts to check if someone already found a base.", Category.BASE_FINDING);
      this.group("Render");
      this.shapeMode = this.add(new EnumSetting<>("Shape Mode", "How shafts are drawn.", ShapeMode.Both));
      this.color1x1 = this.add(new ColorSetting("1x1 Color", "Color of 1x1 shafts.", new Color(255, 0, 0, 150)));
      this.color3x1 = this.add(new ColorSetting("3x1 Color", "Color of 3x1 shafts.", new Color(255, 165, 0, 150)));
      this.holes = new ConcurrentHashMap<>();
      this.scanner = new ChunkScanner("TunnelHoleESP", this::scanChunk);
   }

   @Override
   public void onEnable() {
      this.holes.clear();
      this.scanner.start();
      if (mc.field_1687 != null) {
         this.scanner.submitLoaded();
      }
   }

   @Override
   public void onDisable() {
      this.scanner.stop();
      this.holes.clear();
   }

   private void rescan() {
      if (this.isActive()) {
         this.onEnable();
      }
   }

   @Override
   public void onGameLeft() {
      this.holes.clear();
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.scanner.submit(chunk);
   }

   @Override
   public void onTick() {
      if (++this.ticks % 100 == 0 && mc.field_1724 != null && mc.field_1687 != null) {
         class_1923 here = mc.field_1724.method_31476();
         int keep = mc.field_1690.method_38521() + 2;
         this.holes
            .keySet()
            .removeIf(
               p -> (Math.abs(p.field_9181 - here.field_9181) > keep || Math.abs(p.field_9180 - here.field_9180) > keep)
                     && !mc.field_1687.method_2935().method_12123(p.field_9181, p.field_9180)
            );
      }
   }

   private void scanChunk(class_2818 chunk) {
      if (mc.field_1687 != null && this.isActive()) {
         class_1923 chunkPos = chunk.method_12004();
         List<Shafts.Shaft> found = Shafts.find(mc.field_1687, chunkPos, this.minDepth.get(), this.holes1x1.get(), this.holes3x1.get());
         if (found.isEmpty()) {
            this.holes.remove(chunkPos);
         } else {
            this.holes.put(chunkPos, found);
         }
      }
   }

   @Override
   public void onRender(Render3D renderer) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         int playerChunkX = mc.field_1724.method_31477() >> 4;
         int playerChunkZ = mc.field_1724.method_31479() >> 4;
         int viewDistance = mc.field_1690.method_38521();

         for (Entry<class_1923, List<Shafts.Shaft>> entry : this.holes.entrySet()) {
            class_1923 pos = entry.getKey();
            if (Math.abs(pos.field_9181 - playerChunkX) <= viewDistance && Math.abs(pos.field_9180 - playerChunkZ) <= viewDistance) {
               for (Shafts.Shaft hole : entry.getValue()) {
                  Color color = hole.shape() == Shafts.Shape.ONE ? this.color1x1.get() : this.color3x1.get();
                  renderer.box(
                     (double)hole.x(),
                     (double)hole.minY(),
                     (double)hole.z(),
                     (double)(hole.x() + hole.shape().sizeX),
                     (double)hole.maxY(),
                     (double)(hole.z() + hole.shape().sizeZ),
                     color,
                     color,
                     this.shapeMode.get()
                  );
               }
            }
         }
      }
   }
}
