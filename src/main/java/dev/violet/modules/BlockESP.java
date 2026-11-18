package dev.violet.modules;

import dev.violet.esp.BlockLayer;
import dev.violet.esp.ChunkScanner;
import dev.violet.esp.EspStyle;
import dev.violet.event.EventHandler;
import dev.violet.event.impl.Render3DEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.BlockColorListSetting;
import dev.violet.setting.lite.BooleanSetting;
import java.util.Set;
import java.util.function.LongPredicate;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_310;

public final class BlockESP extends BusModule implements ChunkScanner.Consumer {
   private final EspStyle style = new EspStyle((x$0, x$1) -> (S)this.addSetting(x$0, x$1), "Outline", 0.2F, 2048.0F);
   private final BooleanSetting sTracers = this.addSetting("tracers", new BooleanSetting("Tracers", false));
   private final BlockColorListSetting sBlockColors = this.addSetting(BlockColorListSetting.createDefault());
   private final BlockLayer layer = new BlockLayer(new BlockLayer.Palette() {
      @Override
      public int color(Object kind) {
         return BlockESP.this.sBlockColors.colorArgb((class_2248)kind);
      }

      @Override
      public int tracerColor(Object kind) {
         return BlockESP.this.sBlockColors.tracerColorArgb((class_2248)kind);
      }
   }, () -> class_310.method_1551().field_1687 == null ? null : class_310.method_1551().field_1687.method_27983());
   private Set<String> scannedIds = Set.of();

   public BlockESP() {
      super("block-esp", "Block ESP", "Highlights chosen blocks; touching blocks merge into one shape.", ModuleCategory.RENDER);
      this.sBlockColors.onChange(this::rescan);
   }

   private void rescan() {
      if (this.isEnabled()) {
         Set<String> ids = this.sBlockColors.enabledIds();
         if (!ids.equals(this.scannedIds)) {
            boolean removed = !ids.containsAll(this.scannedIds);
            this.scannedIds = ids;
            if (removed) {
               this.layer.clear();
            }

            ChunkScanner.rescan();
         }
      }
   }

   @Override
   public void onEnable() {
      this.scannedIds = this.sBlockColors.enabledIds();
      ChunkScanner.register(this);
   }

   @Override
   public void onDisable() {
      ChunkScanner.unregister(this);
      this.layer.clear();
   }

   @Override
   public boolean wants(class_2680 state) {
      return this.sBlockColors.isEnabled(state.method_26204());
   }

   @Override
   public void found(long pos, class_2680 state) {
      this.layer.put(pos, state.method_26204());
   }

   @Override
   public void removed(long pos) {
      this.layer.remove(pos);
   }

   @Override
   public void reset() {
      this.layer.clear();
   }

   @Override
   public void chunkScanned(long chunkKey, LongPredicate stillThere) {
      this.layer.retainChunk(chunkKey, stillThere);
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      this.layer.render(event, this.style);
      if (this.sTracers.get() && this.sBlockColors.hasAnyTracerEnabled()) {
         this.layer.renderTracers(this.style);
      }
   }
}
