package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.sky.AuraRenderer;

public class BlockOverlay extends Module implements Presets {
   public final Setting<BlockOverlay.Style> style;
   public final Setting<Boolean> overlay;
   public final Setting<Boolean> matchSky;
   public final Setting<Boolean> rainbow;
   public final Setting<Color> color;
   public final Setting<Integer> pulse;
   public final Setting<Integer> width;
   public final Setting<Integer> fill;

   public BlockOverlay() {
      super("Block Overlay", "Custom outline and overlay on the block you look at. Turn it off for the normal Minecraft outline.", Category.VISUALS);
      this.group("Overlay");
      this.style = this.add(new EnumSetting<>("Style", "Look of the block overlay.", BlockOverlay.Style.Galaxy)).visible(() -> false);
      this.overlay = this.add(new BoolSetting("Block Overlay", "Custom overlay on the block you look at.", true));
      this.matchSky = this.add(new BoolSetting("Match Sky", "The outline takes the color of your sky.", true)).visible(this.overlay::get);
      this.rainbow = this.add(new BoolSetting("Rainbow", "Rainbow colored outline.", false)).visible(() -> this.overlay.get() && !this.matchSky.get());
      this.color = this.add(new ColorSetting("Color", "Color of the outline.", new Color(124, 247, 197, 255)))
         .visible(() -> this.overlay.get() && !this.matchSky.get() && !this.rainbow.get());
      this.pulse = this.add(new IntSetting("Pulse", "Pulse speed in percent.", 100, 0, 300)).visible(this.overlay::get);
      this.width = this.add(new IntSetting("Width", "Edge thickness.", 3, 1, 10)).visible(this.overlay::get);
      this.fill = this.add(new IntSetting("Fill", "Fill opacity of the classic style in percent.", 18, 0, 60))
         .visible(() -> this.overlay.get() && this.style.get() == BlockOverlay.Style.Classic);
      this.setEnabledSilently(true);
   }

   public int color(float seconds) {
      if (this.matchSky.get()) {
         int sky = SoberClient.MODULES.get(Sky.class).accentColor();
         return sky >= 0 ? sky : this.color.get().packed() & 16777215;
      } else {
         return this.rainbow.get() ? AuraRenderer.hsvToRgb(seconds * 0.12F % 1.0F, 0.8F, 1.0F) : this.color.get().packed() & 16777215;
      }
   }

   @Override
   public int presetCount() {
      return BlockOverlay.Style.values().length;
   }

   @Override
   public String presetName(int index) {
      return BlockOverlay.Style.values()[index].title;
   }

   @Override
   public int presetColor(int index) {
      return 0xFF000000 | BlockOverlay.Style.values()[index].color.packed();
   }

   @Override
   public boolean presetActive(int index) {
      return this.isActive() && this.overlay.get() && this.style.get().ordinal() == index;
   }

   @Override
   public void selectPreset(int index) {
      this.style.set(BlockOverlay.Style.values()[index]);
      this.overlay.set(true);
      this.setEnabled(true);
   }

   @Override
   public String presetDescription() {
      return "Applies this block overlay style.";
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   @Override
   public boolean bindable() {
      return false;
   }

   public static enum Style {
      Classic("Classic", new Color(124, 247, 197, 255)),
      Galaxy("Galaxy", new Color(155, 80, 232, 255)),
      Palm("Palm", new Color(95, 242, 228, 255)),
      Target("Target", new Color(176, 140, 240, 255)),
      Hologram("Hologram", new Color(79, 230, 255, 255)),
      Opal("Opal", new Color(242, 184, 255, 255)),
      Molten("Molten", new Color(255, 122, 31, 255));

      public final String title;
      public final Color color;

      private Style(String title, Color color) {
         this.title = title;
         this.color = color;
      }
   }
}
