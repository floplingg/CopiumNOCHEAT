package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting<String> {
   private final boolean hidden;

   public StringSetting(String name, String description, String defaultValue) {
      this(name, description, defaultValue, false);
   }

   public StringSetting(String name, String description, String defaultValue, boolean hidden) {
      super(name, description, defaultValue);
      this.hidden = hidden;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   @Override
   public String display() {
      return this.hidden && !this.value.isEmpty() ? "*".repeat(Math.min(12, this.value.length())) : this.value;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.value = element.getAsString();
      }
   }
}
