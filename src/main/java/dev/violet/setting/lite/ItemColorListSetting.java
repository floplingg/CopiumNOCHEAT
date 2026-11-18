package dev.violet.setting.lite;

import dev.violet.setting.Setting;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_7923;

public final class ItemColorListSetting extends LiteSetting {
   private static final List<String> PRE_ENABLED = List.of(
      "diamond",
      "emerald",
      "netherite_ingot",
      "ancient_debris",
      "elytra",
      "totem_of_undying",
      "trident",
      "enchanted_golden_apple",
      "golden_apple",
      "nether_star",
      "heart_of_the_sea",
      "end_crystal",
      "shulker_box",
      "gold_ingot",
      "gold_block",
      "gold_nugget",
      "iron_ingot"
   );
   private dev.violet.setting.StringSetting setting;
   private String parsedFrom;
   private Set<String> enabled = Set.of();

   private ItemColorListSetting(String name) {
      super(name);
   }

   public static ItemColorListSetting createDefault() {
      return new ItemColorListSetting("Item Colors");
   }

   @Override
   protected Setting<?> create() {
      this.setting = new dev.violet.setting.StringSetting(this.name(), "Item ids that get highlighted, separated by commas.", String.join(", ", PRE_ENABLED));
      this.setting.onChanged(v -> this.notifyChanged());
      return this.setting;
   }

   private Set<String> enabled() {
      String raw = this.setting == null ? String.join(",", PRE_ENABLED) : this.setting.get();
      if (!raw.equals(this.parsedFrom)) {
         Set<String> next = new HashSet<>();

         for (String part : raw.split(",")) {
            String id = part.trim().toLowerCase(Locale.ROOT);
            if (id.startsWith("minecraft:")) {
               id = id.substring(10);
            }

            if (!id.isEmpty()) {
               next.add(id);
            }
         }

         this.enabled = next;
         this.parsedFrom = raw;
      }

      return this.enabled;
   }

   public boolean isEnabled(class_1792 item) {
      return this.enabled().contains(class_7923.field_41178.method_10221(item).method_12832());
   }

   public boolean hasAnyEnabled() {
      return !this.enabled().isEmpty();
   }

   public int colorArgb(class_1792 item) {
      return 0xFF000000 | colorFor(class_7923.field_41178.method_10221(item).method_12832());
   }

   private static int colorFor(String path) {
      return switch (path) {
         case "diamond", "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe", "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots" -> 65535;
         case "emerald" -> 65348;
         case "netherite_ingot", "ancient_debris", "netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_hoe", "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots" -> 8413328;
         case "gold_ingot", "gold_block", "gold_nugget", "golden_sword", "golden_pickaxe", "golden_axe", "golden_shovel", "golden_hoe", "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots", "golden_apple", "enchanted_golden_apple" -> 16766720;
         case "iron_ingot" -> 13421772;
         case "elytra" -> 5592575;
         case "totem_of_undying" -> 16755200;
         case "trident" -> 35071;
         case "nether_star" -> 16776960;
         case "heart_of_the_sea" -> 65501;
         case "end_crystal" -> 16711935;
         case "shulker_box" -> 13386956;
         default -> 16777215;
      };
   }
}
