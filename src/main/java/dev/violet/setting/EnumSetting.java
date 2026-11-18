package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
   private final E[] values;

   public EnumSetting(String name, String description, E defaultValue) {
      super(name, description, defaultValue);
      this.values = defaultValue.getDeclaringClass().getEnumConstants();
   }

   public void cycle(boolean forward) {
      int index = this.value.ordinal() + (forward ? 1 : -1);
      if (index < 0) {
         index = this.values.length - 1;
      }

      if (index >= this.values.length) {
         index = 0;
      }

      this.set(this.values[index]);
   }

   @Override
   public String display() {
      return this.value.toString();
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value.name());
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         String name = element.getAsString();

         for (E e : this.values) {
            if (e.name().equals(name)) {
               this.value = e;
            }
         }
      }
   }
}
