package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntSetting extends Setting<Integer> {
   public final int min;
   public final int max;

   public IntSetting(String name, String description, int defaultValue, int min, int max) {
      super(name, description, defaultValue);
      this.min = min;
      this.max = max;
   }

   protected Integer validate(Integer newValue) {
      return Math.max(this.min, Math.min(this.max, newValue));
   }

   @Override
   public String display() {
      return Integer.toString(this.value);
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.value = this.validate(element.getAsInt());
      }
   }
}
