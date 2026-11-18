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
import dev.violet.sky.GlintManager;
import dev.violet.sky.GlintType;
import net.fabricmc.loader.api.FabricLoader;

public class CustomGlint extends Module implements Presets {
   public final Setting<CustomGlint.Glint> glint;
   public final Setting<Integer> glintSpeed;
   public final Setting<Integer> glintStrength;
   public final Setting<Boolean> itemAura;
   public final Setting<Boolean> matchSky;
   public final Setting<Boolean> itemRainbow;
   public final Setting<Color> itemColor;
   public final Setting<Integer> itemWidth;
   public final Setting<Integer> itemGlow;
   private boolean warned;

   public CustomGlint() {
      super("Custom Glint", "Custom enchantment glints and the item aura.", Category.VISUALS);
      this.group("Glint");
      this.glint = this.add(new EnumSetting<>("Glint", "Look of the enchantment glint.", CustomGlint.Glint.Palm))
         .visible(() -> false)
         .onChanged(v -> this.applyGlint());
      this.glintSpeed = this.add(new IntSetting("Glint Speed", "How fast the glint moves over items, in percent.", 50, 0, 100))
         .onChanged(v -> this.applyOptions());
      this.glintStrength = this.add(
            new IntSetting(
               "Glint Strength",
               "How much the SoberClient glints cover the item, in percent. At 100 percent the item looks like it is made of the glint. The vanilla glint uses it as brightness.",
               75,
               0,
               100
            )
         )
         .onChanged(v -> this.applyOptions());
      this.group("Item Aura");
      this.itemAura = this.add(new BoolSetting("Item Aura", "Glowing outline around the item in your hand.", true));
      this.matchSky = this.add(new BoolSetting("Match Sky", "The outline takes the color of your sky, like galaxy purple or aurora green.", true))
         .visible(this.itemAura::get);
      this.itemRainbow = this.add(new BoolSetting("Item Rainbow", "Rainbow colored item aura.", false))
         .visible(() -> this.itemAura.get() && !this.matchSky.get());
      this.itemColor = this.add(new ColorSetting("Item Color", "Color of the item aura.", new Color(157, 107, 255, 255)))
         .visible(() -> this.itemAura.get() && !this.matchSky.get() && !this.itemRainbow.get());
      this.itemWidth = this.add(new IntSetting("Item Width", "Outline thickness.", 3, 1, 8)).visible(this.itemAura::get);
      this.itemGlow = this.add(new IntSetting("Item Glow", "Soft glow size.", 5, 0, 10)).visible(this.itemAura::get);
   }

   public int itemAuraColor(float seconds) {
      if (this.matchSky.get()) {
         int sky = SoberClient.MODULES.get(Sky.class).accentColor();
         if (sky >= 0) {
            return sky;
         }
      } else if (this.itemRainbow.get()) {
         return AuraRenderer.hsvToRgb(seconds * 0.12F % 1.0F, 0.75F, 1.0F);
      }

      return this.itemColor.get().packed() & 16777215;
   }

   public void select(CustomGlint.Glint value) {
      if (this.glint.get() == value) {
         this.applyGlint();
      } else {
         this.glint.set(value);
      }
   }

   public void sync() {
      this.applyOptions();
      this.applyGlint();
   }

   public void checkConflicts() {
      if (!this.warned && this.glint.get() != CustomGlint.Glint.Vanilla && FabricLoader.getInstance().isModLoaded("customglint")) {
         this.warned = true;
         this.warning("The Custom Glint mod replaces enchant glints. Disable it in your mods folder to see the SoberClient glint.", new Object[0]);
      }
   }

   private void applyGlint() {
      try {
         GlintManager.apply(this.glint.get().type);
      } catch (RuntimeException var2) {
         SoberClient.LOG.warn("Could not apply glint", var2);
      }
   }

   private void applyOptions() {
      if (mc.field_1690 != null) {
         mc.field_1690.method_48580().method_41748((double)this.glintSpeed.get().intValue() / 100.0);
         mc.field_1690.method_48581().method_41748(Math.min(1.0, (double)this.glintStrength.get().intValue() / 100.0));
      }
   }

   @Override
   public int presetCount() {
      return CustomGlint.Glint.values().length;
   }

   @Override
   public String presetName(int index) {
      return CustomGlint.Glint.values()[index].title;
   }

   @Override
   public int presetColor(int index) {
      Color color = CustomGlint.Glint.values()[index].color;
      return color != null ? 0xFF000000 | color.packed() : Color.rainbow((double)index * 0.1, 0.25, 0.7F, 1.0F, 255).packed();
   }

   @Override
   public boolean presetActive(int index) {
      return this.glint.get().ordinal() == index;
   }

   @Override
   public void selectPreset(int index) {
      this.select(CustomGlint.Glint.values()[index]);
   }

   @Override
   public String presetDescription() {
      return "Applies this enchantment glint.";
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   @Override
   public boolean bindable() {
      return false;
   }

   public static enum Glint {
      Vanilla("Vanilla", GlintType.VANILLA, new Color(138, 79, 216, 255)),
      Palm("Palm", GlintType.PALM, new Color(95, 242, 228, 255)),
      Galaxy("Galaxy", GlintType.GALAXY, new Color(155, 80, 232, 255)),
      Rainbow("Rainbow", GlintType.RAINBOW, null),
      Ice("Ice", GlintType.ICE, new Color(111, 201, 255, 255)),
      Summer("Summer", GlintType.SUMMER, new Color(255, 138, 61, 255)),
      Lagoon("Lagoon", GlintType.LAGOON, new Color(24, 200, 200, 255)),
      Inferno("Inferno", GlintType.INFERNO, new Color(232, 70, 12, 255)),
      Velvet("Velvet", GlintType.VELVET, new Color(140, 30, 140, 255)),
      Aurum("Aurum", GlintType.AURUM, new Color(214, 154, 30, 255)),
      Fire("Fire", GlintType.FIRE, new Color(255, 106, 26, 255)),
      Lightning("Lightning", GlintType.LIGHTNING, new Color(127, 184, 255, 255)),
      Sakura("Sakura", GlintType.SAKURA, new Color(255, 143, 192, 255)),
      Gold("Gold", GlintType.GOLD, new Color(255, 200, 68, 255)),
      Void("Void", GlintType.VOID, new Color(138, 63, 224, 255)),
      Matrix("Matrix", GlintType.MATRIX, new Color(34, 224, 90, 255)),
      Synthwave("Synthwave", GlintType.SYNTHWAVE, new Color(255, 63, 184, 255)),
      Chrome("Chrome", GlintType.CHROME, new Color(200, 212, 232, 255)),
      Toxic("Toxic", GlintType.TOXIC, new Color(124, 255, 42, 255)),
      Hearts("Hearts", GlintType.HEARTS, new Color(255, 79, 127, 255));

      public final String title;
      public final GlintType type;
      public final Color color;

      private Glint(String title, GlintType type, Color color) {
         this.title = title;
         this.type = type;
         this.color = color;
      }
   }
}
