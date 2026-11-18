package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.sky.AuraRenderer;
import dev.violet.sky.SkySettings;
import dev.violet.sky.SkyType;

public class Sky extends Module {
   private boolean syncingAurora;
   private final Setting<Sky.SkyMode> skyMode;
   private final Setting<Integer> speed;
   private final Setting<Integer> brightness;
   private final Setting<Sky.AuroraColor> auroraPreset;
   private final Setting<Color> auroraColor;
   private final Setting<Boolean> hideClouds;
   private final Setting<Boolean> matchFog;

   public Sky() {
      super(
         "Sky",
         "Animated aurora, galaxy, anime, storm sea, blood moon, petal dusk, event horizon, frost halo, firefly night, candy clouds, nebula bloom, ashen squall, nightshade, phantom mist, synthwave, black hole, sakura, underwater, cyberpunk and Sober skies. Sober is a black night with the dripping chrome discord.gg/soberclient around the horizon.",
         Category.VISUALS
      );
      this.group("Sky");
      this.skyMode = this.add(new EnumSetting<>("Sky Mode", "Look of the sky.", Sky.SkyMode.Galaxy)).onChanged(v -> this.apply());
      this.speed = this.add(new IntSetting("Speed", "Animation speed of the sky in percent.", 100, 0, 300)).onChanged(v -> this.apply());
      this.brightness = this.add(new IntSetting("Brightness", "Brightness of the sky in percent.", 100, 50, 150)).onChanged(v -> this.apply());
      this.auroraPreset = this.add(new EnumSetting<>("Aurora Preset", "Ready made northern lights colors.", Sky.AuroraColor.Green))
         .visible(() -> this.skyMode.get() == Sky.SkyMode.Aurora)
         .onChanged(this::onAuroraPreset);
      this.auroraColor = this.add(new ColorSetting("Aurora Color", "Pick any color for the northern lights.", Sky.AuroraColor.Green.color))
         .visible(() -> this.skyMode.get() == Sky.SkyMode.Aurora)
         .onChanged(v -> this.onAuroraColor());
      this.hideClouds = this.add(new BoolSetting("Hide Clouds", "Hides the vanilla clouds.", true)).onChanged(v -> this.apply());
      this.matchFog = this.add(new BoolSetting("Match Fog", "Tints the fog so far terrain blends into the sky.", true)).onChanged(v -> this.apply());
   }

   @Override
   public void onEnable() {
      this.apply();
   }

   @Override
   public void onDisable() {
      SkySettings.sky = SkyType.VANILLA;
   }

   public SkyType previewType() {
      return this.skyMode.get().type;
   }

   public String previewName() {
      return this.skyMode.get().toString();
   }

   public float previewBrightness() {
      return (float)this.brightness.get().intValue() / 100.0F;
   }

   public float previewSpeed() {
      return (float)this.speed.get().intValue() / 100.0F;
   }

   public int previewPalette() {
      return this.auroraPreset.get().ordinal();
   }

   public int previewAuroraColor() {
      return this.auroraColor.get().packed() & 16777215;
   }

   public int accentColor() {
      if (!this.isActive()) {
         return -1;
      } else {
         return switch ((Sky.SkyMode)this.skyMode.get()) {
            case Aurora -> this.auroraPreset.get() == Sky.AuroraColor.Rainbow
            ? AuraRenderer.hsvToRgb((float)(System.currentTimeMillis() % 3600000L) / 1000.0F * 0.05F % 1.0F, 0.7F, 1.0F)
            : this.auroraColor.get().packed() & 16777215;
            case Galaxy -> 10316799;
            case Anime -> 16777215;
            case StormSea -> 8377599;
            case BloodMoon -> 16726826;
            case PetalDusk -> 16752324;
            case EventHorizon -> 16751164;
            case FrostHalo -> 12576511;
            case FireflyNight -> 13172570;
            case CandyClouds -> 16754904;
            case NebulaBloom -> 13134847;
            case AshenSquall -> 16747084;
            case Nightshade -> 10640639;
            case PhantomMist -> 13949666;
            case Synthwave -> 14696570;
            case BlackHole -> 16756816;
            case Sakura -> 16095928;
            case Underwater -> 6277336;
            case Cyberpunk -> 3008255;
            case Sober -> 15132396;
         };
      }
   }

   private void apply() {
      if (this.isActive()) {
         SkySettings.sky = this.skyMode.get().type;
         SkySettings.speed = this.speed.get();
         SkySettings.brightness = this.brightness.get();
         SkySettings.auroraPalette = this.auroraPreset.get().ordinal();
         SkySettings.auroraColor = this.auroraColor.get().packed() & 16777215;
         SkySettings.hideVanillaClouds = this.hideClouds.get();
         SkySettings.matchFog = this.matchFog.get();
      }
   }

   private void onAuroraPreset(Sky.AuroraColor preset) {
      if (preset.color != null && !this.syncingAurora) {
         this.syncingAurora = true;
         this.auroraColor.set(preset.color);
         this.syncingAurora = false;
      }

      this.apply();
   }

   private void onAuroraColor() {
      if (!this.syncingAurora && this.auroraPreset.get() != Sky.AuroraColor.Custom) {
         this.syncingAurora = true;
         this.auroraPreset.set(Sky.AuroraColor.Custom);
         this.syncingAurora = false;
      }

      this.apply();
   }

   public static enum AuroraColor {
      Green(new Color(26, 255, 107, 255)),
      Pink(new Color(255, 71, 158, 255)),
      Blue(new Color(38, 191, 255, 255)),
      Rainbow(null),
      Custom(null);

      final Color color;

      private AuroraColor(Color color) {
         this.color = color;
      }
   }

   public static enum SkyMode {
      Aurora("Aurora", SkyType.AURORA),
      Galaxy("Galaxy", SkyType.GALAXY),
      Anime("Anime", SkyType.ANIME),
      StormSea("Storm Sea", SkyType.STORM),
      BloodMoon("Blood Moon", SkyType.BLOODMOON),
      PetalDusk("Petal Dusk", SkyType.PETALDUSK),
      EventHorizon("Event Horizon", SkyType.EVENTHORIZON),
      FrostHalo("Frost Halo", SkyType.FROSTHALO),
      FireflyNight("Firefly Night", SkyType.FIREFLYNIGHT),
      CandyClouds("Candy Clouds", SkyType.CANDYCLOUDS),
      NebulaBloom("Nebula Bloom", SkyType.NEBULABLOOM),
      AshenSquall("Ashen Squall", SkyType.ASHENSQUALL),
      Nightshade("Nightshade", SkyType.NIGHTSHADE),
      PhantomMist("Phantom Mist", SkyType.PHANTOMMIST),
      Synthwave("Synthwave", SkyType.SYNTHWAVE),
      BlackHole("Black Hole", SkyType.BLACKHOLE),
      Sakura("Sakura", SkyType.SAKURA),
      Underwater("Underwater", SkyType.UNDERWATER),
      Cyberpunk("Cyberpunk", SkyType.CYBERPUNK),
      Sober("Sober", SkyType.SOBER);

      private final String title;
      final SkyType type;

      private SkyMode(String title, SkyType type) {
         this.title = title;
         this.type = type;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
