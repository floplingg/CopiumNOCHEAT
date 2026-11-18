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
import dev.violet.setting.StringSetting;
import dev.violet.util.ChunkScanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2818;
import net.minecraft.class_6574;
import net.minecraft.class_6677;
import net.minecraft.class_2338.class_2339;

public class SeedChunkFinder extends Module {
   private final Setting<String> seed = this.add(new StringSetting("Server Seed", "The numeric world seed of the server. Re-enable after changing it.", ""));
   private final Setting<Boolean> notify = this.add(new BoolSetting("Notify", "Shows a notification with the coordinates of new suspicious chunks.", true));
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Color> color;
   private final Setting<Integer> renderY;
   private final Set<class_1923> suspiciousChunks;
   private final ChunkScanner scanner;
   private volatile class_6574 deepslateRandom;

   public SeedChunkFinder() {
      super("Seed Chunk Finder", "Finds player placed deepslate using the server seed.", Category.BASE_FINDING);
      this.group("Render");
      this.shapeMode = this.add(new EnumSetting<>("Shape Mode", "How chunks are drawn.", ShapeMode.Both));
      this.color = this.add(new ColorSetting("Color", "Color of suspicious chunks.", new Color(0, 255, 0, 150)));
      this.renderY = this.add(new IntSetting("Render Y", "Y level the chunk markers are drawn at.", 63, -64, 320));
      this.suspiciousChunks = ConcurrentHashMap.newKeySet();
      this.scanner = new ChunkScanner("SeedChunkFinder", this::analyzeChunk);
   }

   @Override
   public void onEnable() {
      long worldSeed;
      try {
         worldSeed = Long.parseLong(this.seed.get().trim());
      } catch (NumberFormatException var4) {
         this.error("Set a valid numeric server seed first.", new Object[0]);
         this.setEnabled(false);
         return;
      }

      this.deepslateRandom = new class_6677(worldSeed).method_38421().method_38995("minecraft:deepslate").method_38421();
      this.suspiciousChunks.clear();
      this.scanner.start();
      if (mc.field_1687 != null) {
         this.scanner.submitLoaded();
      }
   }

   @Override
   public void onDisable() {
      this.scanner.stop();
      this.suspiciousChunks.clear();
   }

   @Override
   public void onGameLeft() {
      this.suspiciousChunks.clear();
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.scanner.submit(chunk);
   }

   private boolean shouldBeDeepslate(int x, int y, int z) {
      if (y <= 0) {
         return true;
      } else if (y >= 8) {
         return false;
      } else {
         double chance = 1.0 - (double)y / 8.0;
         return (double)this.deepslateRandom.method_38418(x, y, z).method_43057() < chance;
      }
   }

   private void analyzeChunk(class_2818 chunk) {
      if (mc.field_1687 != null && mc.field_1687.method_27983() == class_1937.field_25179) {
         class_1923 chunkPos = chunk.method_12004();
         class_2339 pos = new class_2339();

         for (int x = chunkPos.method_8326(); x < chunkPos.method_8326() + 16; x++) {
            for (int z = chunkPos.method_8328(); z < chunkPos.method_8328() + 16; z++) {
               for (int y = 1; y < 8; y++) {
                  pos.method_10103(x, y, z);
                  if (chunk.method_8320(pos).method_27852(class_2246.field_28888) && !this.shouldBeDeepslate(x, y, z)) {
                     if (this.suspiciousChunks.add(chunkPos) && this.notify.get()) {
                        this.info("Suspicious chunk at X: %d Z: %d", new Object[]{chunkPos.method_8326(), chunkPos.method_8328()});
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   @Override
   public void onRender(Render3D renderer) {
      if (mc.field_1687 != null) {
         this.suspiciousChunks.removeIf(p -> !mc.field_1687.method_2935().method_12123(p.field_9181, p.field_9180));
         double y = (double)this.renderY.get().intValue();

         for (class_1923 pos : this.suspiciousChunks) {
            renderer.box(
               (double)pos.method_8326(),
               y,
               (double)pos.method_8328(),
               (double)(pos.method_8326() + 16),
               y + 0.1,
               (double)(pos.method_8328() + 16),
               this.color.get(),
               this.color.get(),
               this.shapeMode.get()
            );
         }
      }
   }
}
