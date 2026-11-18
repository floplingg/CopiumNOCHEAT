package dev.violet.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.Supplier;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_307;

public class KeySetting extends Setting<Integer> {
   private final Supplier<class_304> fallback;

   public KeySetting(String name, String description, Supplier<class_304> fallback) {
      super(name, description, -1);
      this.fallback = fallback;
   }

   public KeySetting(String name, String description, int defaultKey) {
      super(name, description, defaultKey);
      this.fallback = null;
   }

   public boolean isPressed() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1755 != null) {
         return false;
      } else if (this.value >= 0) {
         return class_3675.method_15987(mc.method_22683(), this.value);
      } else {
         class_304 binding = this.fallback == null ? null : this.fallback.get();
         return binding != null && binding.method_1434();
      }
   }

   public void bind(int key) {
      this.set(Integer.valueOf(key < 0 ? -1 : key));
   }

   @Override
   public String display() {
      if (this.value >= 0) {
         return class_307.field_1668.method_1447(this.value).method_27445().getString();
      } else {
         class_304 binding = this.fallback == null ? null : this.fallback.get();
         return binding == null ? "None" : "Game (" + binding.method_16007().getString() + ")";
      }
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.value = element.getAsInt() < 0 ? -1 : element.getAsInt();
      }
   }
}
