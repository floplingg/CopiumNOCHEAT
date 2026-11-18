package dev.violet.modules;

import dev.violet.esp.BlockLayer;
import dev.violet.esp.ChunkScanner;
import dev.violet.esp.EspStyle;
import dev.violet.event.EventHandler;
import dev.violet.event.impl.Render3DEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.ColorSetting;
import java.util.function.LongPredicate;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2331;
import net.minecraft.class_2336;
import net.minecraft.class_2363;
import net.minecraft.class_2377;
import net.minecraft.class_2480;
import net.minecraft.class_2496;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3708;

public final class StorageESP extends BusModule implements ChunkScanner.Consumer {
   private final EspStyle style = new EspStyle((x$0, x$1) -> (S)this.addSetting(x$0, x$1), "Outline", 0.25F, 256.0F);
   private final BooleanSetting sTracers = this.addSetting("tracers", new BooleanSetting("Tracers", false));
   private final StorageESP.Type chest = new StorageESP.Type("Chest", true, -6530304, "chests", "chest-color", "chest-tracers", "chest-tracer-color");
   private final StorageESP.Type enderChest = new StorageESP.Type(
      "Ender Chest", true, -9109249, "ender-chests", "ender-chest-color", "ender-chest-tracers", "ender-chest-tracer-color"
   );
   private final StorageESP.Type shulker = new StorageESP.Type(
      "Shulker Box", true, -7995234, "shulker-boxes", "shulker-box-color", "shulker-tracers", "shulker-tracer-color"
   );
   private final StorageESP.Type spawner = new StorageESP.Type(
      "Spawner", true, -7700826, "spawners", "spawner-color", "spawner-tracers", "spawner-tracer-color"
   );
   private final StorageESP.Type furnace = new StorageESP.Type(
      "Furnace", false, -10197916, "furnaces", "furnace-color", "furnace-tracers", "furnace-tracer-color"
   );
   private final StorageESP.Type barrel = new StorageESP.Type("Barrel", true, -10204366, "barrels", "barrel-color", "barrel-tracers", "barrel-tracer-color");
   private final StorageESP.Type enchanting = new StorageESP.Type(
      "Enchanting Table", true, -3670016, "enchanting-tables", "enchanting-table-color", "enchanting-tracers", "enchanting-tracer-color"
   );
   private final StorageESP.Type hopper = new StorageESP.Type("Hopper", false, -11513776, "hoppers", "hopper-color", "hopper-tracers", "hopper-tracer-color");
   private final StorageESP.Type[] types = new StorageESP.Type[]{
      this.chest, this.enderChest, this.shulker, this.spawner, this.furnace, this.barrel, this.enchanting, this.hopper
   };
   private final BlockLayer layer = new BlockLayer(new BlockLayer.Palette() {
      @Override
      public int color(Object kind) {
         return ((StorageESP.Type)kind).color.argb();
      }

      @Override
      public int tracerColor(Object kind) {
         StorageESP.Type t = (StorageESP.Type)kind;
         return t.tracers.get() ? t.tracerColor.argb() : 0;
      }
   }, () -> class_310.method_1551().field_1687 == null ? null : class_310.method_1551().field_1687.method_27983());

   public StorageESP() {
      super("storage-esp", "Storage ESP", "Highlights storage blocks and spawners with per-type colours.", ModuleCategory.RENDER);
   }

   private StorageESP.Type typeOf(class_2248 block) {
      if (block instanceof class_2281) {
         return this.chest;
      } else if (block instanceof class_2336) {
         return this.enderChest;
      } else if (block instanceof class_2480) {
         return this.shulker;
      } else if (block instanceof class_2496) {
         return this.spawner;
      } else if (block instanceof class_2363) {
         return this.furnace;
      } else if (block instanceof class_3708) {
         return this.barrel;
      } else if (block instanceof class_2331) {
         return this.enchanting;
      } else {
         return block instanceof class_2377 ? this.hopper : null;
      }
   }

   private void rescan() {
      if (this.isEnabled()) {
         this.layer.clear();
         ChunkScanner.rescan();
      }
   }

   private boolean anyTracer() {
      if (!this.sTracers.get()) {
         return false;
      } else {
         for (StorageESP.Type type : this.types) {
            if (type.enabled.get() && type.tracers.get()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void onEnable() {
      ChunkScanner.register(this);
   }

   @Override
   public void onDisable() {
      ChunkScanner.unregister(this);
      this.layer.clear();
   }

   @Override
   public boolean wants(class_2680 state) {
      StorageESP.Type type = this.typeOf(state.method_26204());
      return type != null && type.enabled.get();
   }

   @Override
   public void found(long pos, class_2680 state) {
      this.layer.put(pos, this.typeOf(state.method_26204()));
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
      if (this.anyTracer()) {
         this.layer.renderTracers(this.style);
      }
   }

   private final class Type {
      final BooleanSetting enabled;
      final BooleanSetting tracers;
      final ColorSetting color;
      final ColorSetting tracerColor;

      Type(String label, boolean on, int argb, String idOn, String idColor, String idTracers, String idTracerColor) {
         this.enabled = StorageESP.this.addSetting(idOn, new BooleanSetting(label, on));
         this.color = StorageESP.this.addSetting(idColor, new ColorSetting(label + " Color", argb));
         this.tracers = StorageESP.this.addSetting(idTracers, new BooleanSetting(label + " Tracers", false));
         this.tracerColor = StorageESP.this.addSetting(idTracerColor, new ColorSetting(label + " Tracer Color", argb));
         this.color.visibleWhen(this.enabled::get);
         this.tracers.visibleWhen(() -> StorageESP.this.sTracers.get() && this.enabled.get());
         this.tracerColor.visibleWhen(() -> StorageESP.this.sTracers.get() && this.enabled.get() && this.tracers.get());
         this.enabled.onChange(StorageESP.this::rescan);
      }
   }
}
