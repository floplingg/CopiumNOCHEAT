package dev.violet.setting.lite;

import dev.violet.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public final class StringListSetting extends LiteSetting {
   public static final int MAX_ENTRIES = 64;
   private static final String SEPARATOR = " | ";
   private final String initial;
   private dev.violet.setting.StringSetting setting;

   public StringListSetting(String name, String... defaults) {
      super(name);
      this.initial = String.join(" | ", defaults);
   }

   @Override
   protected Setting<?> create() {
      this.setting = new dev.violet.setting.StringSetting(this.name(), "Entries separated by |", this.initial);
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public List<String> get() {
      String raw = this.setting == null ? this.initial : this.setting.get();
      List<String> entries = new ArrayList<>();

      for (String part : raw.split("\\|")) {
         String entry = part.trim();
         if (!entry.isEmpty() && entries.size() < 64) {
            entries.add(entry);
         }
      }

      return entries;
   }

   public int size() {
      return this.get().size();
   }

   public StringListSetting itemPicker() {
      return this;
   }

   public boolean isItemPicker() {
      return false;
   }

   public boolean canAdd() {
      return this.size() < 64;
   }

   public boolean addValue(String value) {
      if (value != null && !value.isBlank() && this.canAdd()) {
         List<String> entries = this.get();
         entries.add(value.trim());
         this.store(entries);
         return true;
      } else {
         return false;
      }
   }

   public boolean removeValue(String value) {
      List<String> entries = this.get();

      for (int i = 0; i < entries.size(); i++) {
         if (entries.get(i).equalsIgnoreCase(value)) {
            entries.remove(i);
            this.store(entries);
            return true;
         }
      }

      return false;
   }

   public void clear() {
      if (this.size() > 0) {
         this.store(List.of());
      }
   }

   private void store(List<String> entries) {
      if (this.setting != null) {
         this.setting.set(String.join(" | ", entries));
      }
   }
}
