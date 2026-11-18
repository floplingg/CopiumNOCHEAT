package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.Predicate;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class ItemSetting extends Setting<class_1792> {
   private final Predicate<class_1792> filter;

   public ItemSetting(String name, String description, class_1792 defaultValue) {
      this(name, description, defaultValue, item -> true);
   }

   public ItemSetting(String name, String description, class_1792 defaultValue, Predicate<class_1792> filter) {
      super(name, description, defaultValue);
      this.filter = filter;
   }

   public boolean accepts(class_1792 item) {
      return this.filter.test(item);
   }

   public boolean setFromId(String text) {
      String id = text.trim().toLowerCase().replace(' ', '_');
      if (id.isEmpty()) {
         return false;
      } else {
         class_2960 identifier = class_2960.method_12829(id.contains(":") ? id : "minecraft:" + id);
         if (identifier != null && class_7923.field_41178.method_10250(identifier)) {
            class_1792 item = (class_1792)class_7923.field_41178.method_63535(identifier);
            if (item == class_1802.field_8162 && !id.endsWith("air")) {
               return false;
            } else if (!this.filter.test(item)) {
               return false;
            } else {
               this.set(item);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   protected class_1792 validate(class_1792 newValue) {
      return this.filter.test(newValue) ? newValue : null;
   }

   public String id() {
      return class_7923.field_41178.method_10221(this.value).method_12832();
   }

   @Override
   public String display() {
      return this.value.method_63680().getString();
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(class_7923.field_41178.method_10221(this.value).toString());
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         class_2960 id = class_2960.method_12829(element.getAsString());
         if (id != null && class_7923.field_41178.method_10250(id)) {
            class_1792 item = (class_1792)class_7923.field_41178.method_63535(id);
            if (this.filter.test(item)) {
               this.value = item;
            }
         }
      }
   }
}
