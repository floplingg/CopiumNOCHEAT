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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1501;
import net.minecraft.class_1604;
import net.minecraft.class_1641;
import net.minecraft.class_1646;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2393;
import net.minecraft.class_2541;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3989;
import net.minecraft.class_4776;
import net.minecraft.class_4777;
import net.minecraft.class_4950;
import net.minecraft.class_4951;
import net.minecraft.class_2350.class_2351;

public class SuspiciousESP extends Module {
   private final Setting<Boolean> wanderingTraders;
   private final Setting<Boolean> villagers;
   private final Setting<Boolean> llamas;
   private final Setting<Boolean> pillagers;
   private final Setting<Boolean> deepslate;
   private final Setting<Boolean> rotatedDeepslate;
   private final Setting<Integer> deepslateMinY;
   private final Setting<Integer> deepslateMaxY;
   private final Setting<Boolean> vines;
   private final Setting<Boolean> amethyst;
   private final Setting<Boolean> kelp;
   private final Setting<Integer> kelpThreshold;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Boolean> tracers;
   private final Setting<Color> traderColor;
   private final Setting<Color> villagerColor;
   private final Setting<Color> llamaColor;
   private final Setting<Color> deepslateColor;
   private final Setting<Color> rotatedColor;
   private final Setting<Color> vineColor;
   private final Setting<Color> amethystColor;
   private final Setting<Color> kelpColor;
   private final Map<class_1923, Map<class_2338, SuspiciousESP.BlockType>> blocks;
   private final Set<class_1923> kelpChunks;
   private int ticks;
   private final ChunkScanner scanner;

   public SuspiciousESP() {
      super("Suspicious ESP", "Highlights suspicious entities and blocks.", Category.BASE_FINDING);
      this.group("Entities");
      this.wanderingTraders = this.add(new BoolSetting("Wandering Traders", "Highlights wandering traders.", true));
      this.villagers = this.add(new BoolSetting("Villagers", "Villagers and zombie villagers.", true));
      this.llamas = this.add(new BoolSetting("Llamas", "Highlights llamas.", true));
      this.pillagers = this.add(new BoolSetting("Pillagers", "Highlights pillagers.", true));
      this.group("Blocks");
      this.deepslate = this.add(new BoolSetting("Deepslate", "Deepslate inside the Y range (doesn't generate above Y 8).", true)).onChanged(v -> this.rescan());
      this.rotatedDeepslate = this.add(new BoolSetting("Rotated Deepslate", "Sideways placed deepslate inside the Y range.", true))
         .onChanged(v -> this.rescan());
      this.deepslateMinY = this.add(new IntSetting("Deepslate Min Y", "Lower end of the deepslate Y range.", 16, -64, 320))
         .visible(() -> this.deepslate.get() || this.rotatedDeepslate.get())
         .onChanged(v -> this.rescan());
      this.deepslateMaxY = this.add(new IntSetting("Deepslate Max Y", "Upper end of the deepslate Y range.", 128, -64, 320))
         .visible(() -> this.deepslate.get() || this.rotatedDeepslate.get())
         .onChanged(v -> this.rescan());
      this.vines = this.add(new BoolSetting("Vines", "Vines, twisting and weeping vines.", true)).onChanged(v -> this.rescan());
      this.amethyst = this.add(new BoolSetting("Amethyst Clusters", "Highlights amethyst clusters.", true)).onChanged(v -> this.rescan());
      this.kelp = this.add(new BoolSetting("Kelp Chunks", "Chunks with lots of fully grown kelp.", true)).onChanged(v -> this.rescan());
      this.kelpThreshold = this.add(new IntSetting("Kelp Threshold", "Fully grown kelp needed to mark a chunk.", 10, 1, 50))
         .visible(this.kelp::get)
         .onChanged(v -> this.rescan());
      this.group("Render");
      this.shapeMode = this.add(new EnumSetting<>("Shape Mode", "How boxes are drawn.", ShapeMode.Both));
      this.tracers = this.add(new BoolSetting("Tracers", "Draws lines to everything found.", false));
      this.traderColor = this.add(new ColorSetting("Trader Color", "Wandering traders and pillagers.", new Color(255, 0, 0, 150)));
      this.villagerColor = this.add(new ColorSetting("Villager Color", "Villagers and zombie villagers.", new Color(0, 255, 0, 150)));
      this.llamaColor = this.add(new ColorSetting("Llama Color", "Llamas.", new Color(255, 165, 0, 150)));
      this.deepslateColor = this.add(new ColorSetting("Deepslate Color", "Deepslate.", new Color(0, 200, 255, 150)));
      this.rotatedColor = this.add(new ColorSetting("Rotated Color", "Rotated deepslate.", new Color(255, 0, 255, 150)));
      this.vineColor = this.add(new ColorSetting("Vine Color", "Vines.", new Color(0, 255, 0, 150)));
      this.amethystColor = this.add(new ColorSetting("Amethyst Color", "Amethyst clusters.", new Color(180, 0, 255, 150)));
      this.kelpColor = this.add(new ColorSetting("Kelp Color", "Kelp chunks.", new Color(0, 255, 255, 150)));
      this.blocks = new ConcurrentHashMap<>();
      this.kelpChunks = ConcurrentHashMap.newKeySet();
      this.scanner = new ChunkScanner("SuspiciousESP", this::scanChunk);
   }

   @Override
   public void onEnable() {
      this.blocks.clear();
      this.kelpChunks.clear();
      this.scanner.start();
      if (mc.field_1687 != null) {
         this.scanner.submitLoaded();
      }
   }

   @Override
   public void onDisable() {
      this.scanner.stop();
      this.blocks.clear();
      this.kelpChunks.clear();
   }

   private void rescan() {
      if (this.isActive()) {
         this.onEnable();
      }
   }

   @Override
   public void onGameLeft() {
      this.blocks.clear();
      this.kelpChunks.clear();
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.scanner.submit(chunk);
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
      SuspiciousESP.BlockType type = this.classify(newState, pos.method_10264());
      class_1923 chunk = new class_1923(pos);
      if (type != null) {
         this.blocks.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>()).put(pos.method_10062(), type);
      } else {
         Map<class_2338, SuspiciousESP.BlockType> map = this.blocks.get(chunk);
         if (map != null) {
            map.remove(pos);
         }
      }
   }

   @Override
   public void onTick() {
      if (++this.ticks % 100 == 0 && mc.field_1724 != null && mc.field_1687 != null) {
         class_1923 here = mc.field_1724.method_31476();
         int keep = mc.field_1690.method_38521() + 2;
         this.blocks
            .keySet()
            .removeIf(
               p -> (Math.abs(p.field_9181 - here.field_9181) > keep || Math.abs(p.field_9180 - here.field_9180) > keep)
                     && !mc.field_1687.method_2935().method_12123(p.field_9181, p.field_9180)
            );
         this.kelpChunks
            .removeIf(
               p -> (Math.abs(p.field_9181 - here.field_9181) > keep || Math.abs(p.field_9180 - here.field_9180) > keep)
                     && !mc.field_1687.method_2935().method_12123(p.field_9181, p.field_9180)
            );
      }
   }

   private void scanChunk(class_2818 chunk) {
      if (mc.field_1687 != null) {
         class_1923 chunkPos = chunk.method_12004();
         class_2826[] sections = chunk.method_12006();
         int bottomSection = chunk.method_32891();
         Map<class_2338, SuspiciousESP.BlockType> found = new HashMap<>();
         int grownKelp = 0;

         for (int i = 0; i < sections.length; i++) {
            class_2826 section = sections[i];
            if (section != null && !section.method_38292()) {
               int sectionY = bottomSection + i << 4;
               if (section.method_19523(s -> this.isRelevant(s.method_26204()))) {
                  for (int x = 0; x < 16; x++) {
                     for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                           class_2680 state = section.method_12254(x, y, z);
                           int blockY = sectionY + y;
                           SuspiciousESP.BlockType type = this.classify(state, blockY);
                           if (type != null) {
                              found.put(new class_2338(chunkPos.method_8326() + x, blockY, chunkPos.method_8328() + z), type);
                           }

                           if (this.kelp.get() && state.method_27852(class_2246.field_9993) && (Integer)state.method_11654(class_2393.field_22509) == 25) {
                              grownKelp++;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (found.isEmpty()) {
            this.blocks.remove(chunkPos);
         } else {
            this.blocks.put(chunkPos, new ConcurrentHashMap<>(found));
         }

         if (this.kelp.get() && grownKelp >= this.kelpThreshold.get()) {
            this.kelpChunks.add(chunkPos);
         } else {
            this.kelpChunks.remove(chunkPos);
         }
      }
   }

   private boolean isRelevant(class_2248 block) {
      return block == class_2246.field_9993 || block == class_2246.field_27161 || isVine(block) || isDeepslateType(block);
   }

   private SuspiciousESP.BlockType classify(class_2680 state, int y) {
      class_2248 block = state.method_26204();
      if (this.amethyst.get() && block == class_2246.field_27161) {
         return SuspiciousESP.BlockType.AMETHYST;
      } else if (this.vines.get() && isVine(block)) {
         return SuspiciousESP.BlockType.VINE;
      } else {
         boolean inRange = y >= this.deepslateMinY.get() && y <= this.deepslateMaxY.get();
         if (!inRange) {
            return null;
         } else if (this.rotatedDeepslate.get()
            && isDeepslateType(block)
            && state.method_28498(class_2741.field_12496)
            && state.method_11654(class_2741.field_12496) != class_2351.field_11052) {
            return SuspiciousESP.BlockType.ROTATED_DEEPSLATE;
         } else {
            return this.deepslate.get() && block == class_2246.field_28888 ? SuspiciousESP.BlockType.DEEPSLATE : null;
         }
      }
   }

   private static boolean isDeepslateType(class_2248 block) {
      return block == class_2246.field_28888
         || block == class_2246.field_28892
         || block == class_2246.field_28900
         || block == class_2246.field_28896
         || block == class_2246.field_28904;
   }

   private static boolean isVine(class_2248 block) {
      return block instanceof class_2541
         || block instanceof class_4777
         || block instanceof class_4950
         || block instanceof class_4776
         || block instanceof class_4951;
   }

   @Override
   public void onRender(Render3D renderer) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         int playerChunkX = mc.field_1724.method_31477() >> 4;
         int playerChunkZ = mc.field_1724.method_31479() >> 4;
         int viewDistance = mc.field_1690.method_38521();

         for (Entry<class_1923, Map<class_2338, SuspiciousESP.BlockType>> chunk : this.blocks.entrySet()) {
            class_1923 chunkPos = chunk.getKey();
            if (Math.abs(chunkPos.field_9181 - playerChunkX) <= viewDistance && Math.abs(chunkPos.field_9180 - playerChunkZ) <= viewDistance) {
               for (Entry<class_2338, SuspiciousESP.BlockType> entry : chunk.getValue().entrySet()) {
                  class_2338 pos = entry.getKey();
                  Color color = this.blockColor(entry.getValue());
                  renderer.box(pos, color, color, this.shapeMode.get());
                  if (this.tracers.get()) {
                     renderer.tracer((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5, color);
                  }
               }
            }
         }

         if (this.kelp.get()) {
            for (class_1923 pos : this.kelpChunks) {
               if (Math.abs(pos.field_9181 - playerChunkX) <= viewDistance && Math.abs(pos.field_9180 - playerChunkZ) <= viewDistance) {
                  renderer.box(
                     (double)pos.method_8326(),
                     63.0,
                     (double)pos.method_8328(),
                     (double)(pos.method_8326() + 16),
                     63.1,
                     (double)(pos.method_8328() + 16),
                     this.kelpColor.get(),
                     this.kelpColor.get(),
                     this.shapeMode.get()
                  );
               }
            }
         }

         for (class_1297 entity : mc.field_1687.method_18112()) {
            Color color = this.entityColor(entity);
            if (color != null) {
               class_238 box = entity.method_5829();
               renderer.box(box, color, color, this.shapeMode.get());
               if (this.tracers.get()) {
                  renderer.tracer(box.method_1005().field_1352, box.method_1005().field_1351, box.method_1005().field_1350, color);
               }
            }
         }
      }
   }

   private Color entityColor(class_1297 entity) {
      if (this.wanderingTraders.get() && entity instanceof class_3989) {
         return this.traderColor.get();
      } else if (this.pillagers.get() && entity instanceof class_1604) {
         return this.traderColor.get();
      } else if (!this.villagers.get() || !(entity instanceof class_1646) && !(entity instanceof class_1641)) {
         return this.llamas.get() && entity instanceof class_1501 ? this.llamaColor.get() : null;
      } else {
         return this.villagerColor.get();
      }
   }

   private Color blockColor(SuspiciousESP.BlockType type) {
      return switch (type) {
         case DEEPSLATE -> (Color)this.deepslateColor.get();
         case ROTATED_DEEPSLATE -> (Color)this.rotatedColor.get();
         case VINE -> (Color)this.vineColor.get();
         case AMETHYST -> (Color)this.amethystColor.get();
      };
   }

   private static enum BlockType {
      DEEPSLATE,
      ROTATED_DEEPSLATE,
      VINE,
      AMETHYST;
   }
}
