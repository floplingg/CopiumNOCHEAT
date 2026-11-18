package dev.violet.setting.lite;

import dev.violet.setting.BlockListSetting;
import dev.violet.setting.Setting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class BlockColorListSetting extends LiteSetting {
   private final List<String> defaults;
   private BlockListSetting setting;

   private BlockColorListSetting(String name, List<String> defaults) {
      super(name);
      this.defaults = defaults;
   }

   public static BlockColorListSetting blockList(String name, List<String> defaults) {
      return new BlockColorListSetting(name, List.copyOf(defaults));
   }

   @Override
   protected Setting<?> create() {
      List<class_2248> blocks = new ArrayList<>();

      for (String id : this.defaults) {
         class_2960 key = class_2960.method_12829(id);
         if (key != null && class_7923.field_41175.method_10250(key)) {
            blocks.add((class_2248)class_7923.field_41175.method_63535(key));
         }
      }

      this.setting = new BlockListSetting(this.name(), "", blocks.toArray(new class_2248[0]));
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public Set<class_2248> enabledBlocks() {
      return this.setting == null ? Set.of() : Set.copyOf(this.setting.get());
   }

   public boolean isEnabled(class_2248 block) {
      return this.setting != null && this.setting.contains(block);
   }

   public boolean hasAnyEnabled() {
      return this.setting != null && !this.setting.get().isEmpty();
   }

   public int colorArgb(class_2248 block) {
      Integer rgb = this.setting == null ? null : this.setting.color(block);
      return 0xFF000000 | (rgb == null ? 16777215 : rgb);
   }

   public static BlockColorListSetting createDefault() {
      return new BlockColorListSetting("Block Colors", List.of());
   }

   public Set<String> enabledIds() {
      if (this.setting == null) {
         return Set.of();
      } else {
         Set<String> ids = new HashSet<>();

         for (class_2248 block : this.setting.get()) {
            ids.add(class_7923.field_41175.method_10221(block).toString());
         }

         return Set.copyOf(ids);
      }
   }

   public int tracerColorArgb(class_2248 block) {
      return this.isEnabled(block) ? this.colorArgb(block) : 0;
   }

   public boolean hasAnyTracerEnabled() {
      return this.hasAnyEnabled();
   }
}
