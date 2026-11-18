package dev.violet.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class Setting<T> {
   public final String name;
   public final String description;
   protected final T defaultValue;
   protected T value;
   private String group = "General";
   private BooleanSupplier visible = () -> true;
   private Consumer<T> onChanged;

   protected Setting(String name, String description, T defaultValue) {
      this.name = name;
      this.description = description;
      this.defaultValue = defaultValue;
      this.value = defaultValue;
   }

   public T get() {
      return this.value;
   }

   public void set(T newValue) {
      if (newValue != null) {
         T validated = this.validate(newValue);
         if (validated != null && !validated.equals(this.value)) {
            this.value = validated;
            if (this.onChanged != null) {
               this.onChanged.accept(this.value);
            }
         }
      }
   }

   public void reset() {
      this.set(this.defaultValue);
   }

   public JsonElement defaultJson() {
      T current = this.value;
      this.value = this.defaultValue;

      JsonElement var2;
      try {
         var2 = this.save();
      } finally {
         this.value = current;
      }

      return var2;
   }

   public void apply(JsonElement element) {
      T before = this.value;
      this.load(element);
      if (this.onChanged != null && this.value != null && !this.value.equals(before)) {
         this.onChanged.accept(this.value);
      }
   }

   protected T validate(T newValue) {
      return newValue;
   }

   public Setting<T> visible(BooleanSupplier visible) {
      this.visible = visible;
      return this;
   }

   public Setting<T> onChanged(Consumer<T> onChanged) {
      this.onChanged = onChanged;
      return this;
   }

   public void setGroup(String group) {
      this.group = group;
   }

   public String getGroup() {
      return this.group;
   }

   public boolean isVisible() {
      return this.visible.getAsBoolean();
   }

   public abstract String display();

   public abstract JsonElement save();

   public abstract void load(JsonElement var1);
}
