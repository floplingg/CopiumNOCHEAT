package dev.violet.setting.lite;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.Setting;
import java.util.List;

public final class ModeSetting extends LiteSetting {
   private final String initial;
   private List<String> modes;
   private String pending;
   private ModeSetting.Choice setting;

   public ModeSetting(String name, String defaultMode, String... modes) {
      super(name);
      this.initial = defaultMode;
      this.modes = List.of(modes);
   }

   @Override
   protected Setting<?> create() {
      this.setting = new ModeSetting.Choice(this.name(), ModeSetting.Slot.values()[Math.max(0, this.modes.indexOf(this.initial))]);
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public String get() {
      if (this.setting == null) {
         return this.initial;
      } else {
         int index = this.setting.get().ordinal();
         return index < this.modes.size() ? this.modes.get(index) : this.initial;
      }
   }

   public String value() {
      return this.get();
   }

   public boolean is(String mode) {
      return this.get().equals(mode);
   }

   public void set(String mode) {
      int index = this.modes.indexOf(mode);
      if (this.setting != null && index >= 0) {
         this.setting.set(ModeSetting.Slot.values()[index]);
      }
   }

   public List<String> modes() {
      return this.modes;
   }

   public void setModes(List<String> next) {
      String current = this.pending != null ? this.pending : this.get();
      this.modes = List.copyOf(next.size() > ModeSetting.Slot.values().length ? next.subList(0, ModeSetting.Slot.values().length) : next);
      int index = this.modes.indexOf(current);
      if (index >= 0) {
         this.pending = null;
      }

      if (this.setting != null) {
         this.setting.set(ModeSetting.Slot.values()[Math.max(0, index)]);
      }
   }

   private final class Choice extends EnumSetting<ModeSetting.Slot> {
      Choice(String name, ModeSetting.Slot value) {
         super(name, "", value);
      }

      @Override
      public void cycle(boolean forward) {
         int index = this.value.ordinal() + (forward ? 1 : -1);
         if (index < 0) {
            index = ModeSetting.this.modes.size() - 1;
         }

         if (index >= ModeSetting.this.modes.size()) {
            index = 0;
         }

         ModeSetting.this.pending = null;
         this.set(ModeSetting.Slot.values()[index]);
      }

      @Override
      public String display() {
         int index = this.value.ordinal();
         return index < ModeSetting.this.modes.size() ? ModeSetting.this.modes.get(index) : "";
      }

      @Override
      public JsonElement save() {
         return new JsonPrimitive(this.display());
      }

      @Override
      public void load(JsonElement element) {
         if (element.isJsonPrimitive()) {
            int index = ModeSetting.this.modes.indexOf(element.getAsString());
            if (index >= 0) {
               this.value = ModeSetting.Slot.values()[index];
            } else {
               ModeSetting.this.pending = element.getAsString();
            }
         }
      }
   }

   public static enum Slot {
      S0,
      S1,
      S2,
      S3,
      S4,
      S5,
      S6,
      S7,
      S8,
      S9,
      S10,
      S11,
      S12,
      S13,
      S14,
      S15,
      S16,
      S17,
      S18,
      S19,
      S20,
      S21,
      S22,
      S23,
      S24,
      S25,
      S26,
      S27,
      S28,
      S29,
      S30,
      S31;
   }
}
