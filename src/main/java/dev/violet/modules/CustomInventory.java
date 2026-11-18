package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.Theme;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;

public class CustomInventory extends Module implements Presets {
   public final Setting<CustomInventory.Style> style;
   public final Setting<Boolean> matchSky;
   public final Setting<Color> color;
   public final Setting<Integer> strength;
   public final Setting<CustomInventory.Animation> animation;
   public final Setting<Integer> animationSpeed;
   public final Setting<Boolean> background;
   public final Setting<Boolean> glow;
   public final Setting<Boolean> slots;
   public final Setting<Boolean> logo;

   public CustomInventory() {
      super("Custom Inventory", "Recolors inventories to match your sky and client.", Category.CLIENT);
      this.group("Inventory");
      this.style = this.add(new EnumSetting<>("Style", "Look of every inventory and container.", CustomInventory.Style.Glass)).visible(() -> false);
      this.matchSky = this.add(new BoolSetting("Match Sky", "Inventory colors follow your sky.", true)).visible(this::styled);
      this.color = this.add(new ColorSetting("Color", "Color of the inventory theme.", new Color(176, 140, 240, 255)))
         .visible(() -> this.styled() && !this.matchSky.get());
      this.strength = this.add(new IntSetting("Strength", "How strongly the inventory is recolored, in percent.", 90, 0, 100)).visible(this::styled);
      this.animation = this.add(
            new EnumSetting<>("Animation", "Animated effect inside the inventory. Glint uses your Custom Glint.", CustomInventory.Animation.Stars)
         )
         .visible(() -> this.styled() && this.style.get() != CustomInventory.Style.Tinted);
      this.animationSpeed = this.add(new IntSetting("Animation Speed", "Speed of the inventory animation in percent.", 100, 10, 300))
         .visible(() -> this.styled() && this.style.get() != CustomInventory.Style.Tinted && this.animation.get() != CustomInventory.Animation.None);
      this.background = this.add(new BoolSetting("Background", "Colored backdrop behind the inventory.", true)).visible(this::styled);
      this.glow = this.add(new BoolSetting("Glow", "Soft glow and outline around the inventory.", true)).visible(this::styled);
      this.slots = this.add(new BoolSetting("Rounded Slots", "Draws clean rounded slots.", true))
         .visible(() -> this.styled() && this.style.get() != CustomInventory.Style.Tinted);
      this.logo = this.add(new BoolSetting("Logo", "Shows the Copium Client logo on inventories.", true)).visible(this::styled);
   }

   public boolean styled() {
      return this.style.get() != CustomInventory.Style.Vanilla;
   }

   public int color() {
      if (this.matchSky.get()) {
         int sky = SoberClient.MODULES.get(Sky.class).accentColor();
         return sky >= 0 ? sky : Theme.accent() & 16777215;
      } else {
         return this.color.get().packed() & 16777215;
      }
   }

   @Override
   public int presetCount() {
      return CustomInventory.Style.values().length;
   }

   @Override
   public String presetName(int index) {
      return CustomInventory.Style.values()[index].title;
   }

   @Override
   public int presetColor(int index) {
      return 0xFF000000 | CustomInventory.Style.values()[index].color.packed();
   }

   @Override
   public boolean presetActive(int index) {
      return this.style.get().ordinal() == index;
   }

   @Override
   public void selectPreset(int index) {
      this.style.set(CustomInventory.Style.values()[index]);
   }

   @Override
   public String presetDescription() {
      return "Applies this inventory style.";
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   @Override
   public boolean bindable() {
      return false;
   }

   public static enum Animation {
      None,
      Stars,
      Aurora,
      Glint,
      Shimmer;
   }

   public static enum Style {
      Vanilla("Vanilla", new Color(198, 198, 198, 255)),
      Tinted("Tinted", new Color(176, 140, 240, 255)),
      Glass("Glass", new Color(120, 200, 255, 255)),
      Midnight("Midnight", new Color(52, 44, 78, 255));

      public final String title;
      public final Color color;

      private Style(String title, Color color) {
         this.title = title;
         this.color = color;
      }
   }
}
