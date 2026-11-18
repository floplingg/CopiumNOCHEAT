package dev.violet.setting.lite;

import dev.violet.setting.Setting;
import java.util.List;

public final class StringSetting extends LiteSetting {
   private final String initial;
   private dev.violet.setting.StringSetting setting;
   private String hint = "";
   private List<StringSetting.Preset> presets = List.of();
   private StringSetting enchantItem;
   private StringSetting enchantSetting;

   public StringSetting(String name, String defaultValue) {
      super(name);
      this.initial = defaultValue;
   }

   @Override
   protected Setting<?> create() {
      this.setting = new dev.violet.setting.StringSetting(this.name(), this.hint, this.initial);
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   public String get() {
      return this.setting == null ? this.initial : this.setting.get();
   }

   public String getValue() {
      return this.get();
   }

   public void set(String value) {
      if (this.setting != null && value != null) {
         this.setting.set(value);
      }
   }

   public StringSetting hint(String hint) {
      this.hint = hint == null ? "" : hint;
      return this;
   }

   public String hint() {
      return this.hint;
   }

   public StringSetting itemPicker() {
      return this;
   }

   public boolean isItemPicker() {
      return false;
   }

   public StringSetting presets(List<StringSetting.Preset> presets) {
      this.presets = List.copyOf(presets);
      return this;
   }

   public List<StringSetting.Preset> presets() {
      return this.presets;
   }

   public StringSetting enchantsFor(StringSetting item) {
      this.enchantItem = item;
      item.enchantSetting = this;
      return this;
   }

   public StringSetting enchantItem() {
      return this.enchantItem;
   }

   public StringSetting enchantSetting() {
      return this.enchantSetting;
   }

   public static record Preset(String value, String label, String icon, boolean glint) {
   }
}
