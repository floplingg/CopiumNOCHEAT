package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting<Boolean> {
   public BoolSetting(String name, String description, boolean defaultValue) {
      super(name, description, defaultValue);
   }

   public void toggle() {
      this.set(Boolean.valueOf(!this.value));
   }

   @Override
   public String display() {
      return this.value ? "On" : "Off";
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.value = element.getAsBoolean();
      }
   }
}
