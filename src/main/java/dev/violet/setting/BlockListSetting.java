package dev.violet.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockListSetting extends Setting<Set<class_2248>> {
   private final Map<class_2248, Integer> colors = new ConcurrentHashMap<>();

   public BlockListSetting(String name, String description, class_2248... defaults) {
      super(name, description, Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(defaults))));
   }

   public boolean contains(class_2248 block) {
      return this.value.contains(block);
   }

   public void toggle(class_2248 block) {
      Set<class_2248> next = new LinkedHashSet<>(this.value);
      if (!next.remove(block)) {
         next.add(block);
      }

      this.set(Collections.unmodifiableSet(next));
   }

   public void clear() {
      this.set(Collections.unmodifiableSet(new LinkedHashSet<>()));
   }

   public Integer color(class_2248 block) {
      return this.colors.get(block);
   }

   public void setColor(class_2248 block, Integer rgb) {
      if (rgb == null) {
         this.colors.remove(block);
      } else {
         this.colors.put(block, rgb & 16777215);
      }
   }

   @Override
   public String display() {
      return this.value.size() == 1 ? "1 block" : this.value.size() + " blocks";
   }

   @Override
   public JsonElement save() {
      JsonObject object = new JsonObject();
      JsonArray array = new JsonArray();

      for (class_2248 block : this.value) {
         array.add(class_7923.field_41175.method_10221(block).toString());
      }

      object.add("blocks", array);
      JsonObject colorObject = new JsonObject();

      for (Entry<class_2248, Integer> entry : this.colors.entrySet()) {
         colorObject.addProperty(class_7923.field_41175.method_10221(entry.getKey()).toString(), String.format("%06X", entry.getValue()));
      }

      object.add("colors", colorObject);
      return object;
   }

   @Override
   public void load(JsonElement element) {
      JsonArray array = null;
      this.colors.clear();
      if (element.isJsonArray()) {
         array = element.getAsJsonArray();
      } else if (element.isJsonObject()) {
         JsonObject object = element.getAsJsonObject();
         if (object.has("blocks") && object.get("blocks").isJsonArray()) {
            array = object.getAsJsonArray("blocks");
         }

         if (object.has("colors") && object.get("colors").isJsonObject()) {
            for (Entry<String, JsonElement> entry : object.getAsJsonObject("colors").entrySet()) {
               class_2960 id = class_2960.method_12829(entry.getKey());
               if (id != null && class_7923.field_41175.method_10250(id)) {
                  try {
                     this.colors.put((class_2248)class_7923.field_41175.method_63535(id), Integer.parseInt(entry.getValue().getAsString(), 16) & 16777215);
                  } catch (NumberFormatException var8) {
                  }
               }
            }
         }
      }

      if (array != null) {
         Set<class_2248> loaded = new LinkedHashSet<>();

         for (JsonElement entryx : array) {
            class_2960 id = class_2960.method_12829(entryx.getAsString());
            if (id != null && class_7923.field_41175.method_10250(id)) {
               loaded.add((class_2248)class_7923.field_41175.method_63535(id));
            }
         }

         this.value = Collections.unmodifiableSet(loaded);
      }
   }
}
