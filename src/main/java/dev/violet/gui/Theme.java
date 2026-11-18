package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.ClickGui;
import dev.violet.render.Color;
import java.util.Locale;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5223;
import net.minecraft.class_342.class_11734;

public final class Theme {
   private static final Color[] GALAXY = new Color[]{
      new Color(120, 60, 255, 255), new Color(60, 140, 255, 255), new Color(220, 80, 220, 255), new Color(90, 40, 200, 255)
   };
   private static final int DEFAULT_PANEL = 1777187;
   private static final int DEFAULT_OPACITY = 88;
   private static ClickGui gui;

   private Theme() {
   }

   private static ClickGui gui() {
      if (gui == null) {
         gui = SoberClient.MODULES.get(ClickGui.class);
      }

      return gui;
   }

   public static int accent() {
      return accent(0.0);
   }

   public static int accent(double offset) {
      ClickGui gui = gui();
      double speed = (double)gui.accentSpeed.get().intValue() * 0.04;

      return neon(switch ((ClickGui.AccentMode)gui.accentMode.get()) {
         case Static -> 0xFF000000 | gui.accent.get().packed();
         case Rainbow -> Color.rainbow(offset, speed, 0.7F, 1.0F, 255).packed();
         case Galaxy -> galaxy((double)System.currentTimeMillis() / 1000.0 * speed + offset).packed();
      });
   }

   public static int neon(int color) {
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      if (!(max <= 0.001F) && !(max - min <= 0.001F)) {
         float saturation = (max - min) / max;
         if (saturation < 0.12F) {
            return color;
         } else {
            float value = Math.max(max, 0.97F);
            float vivid = Math.min(saturation, Math.max(0.3F, Math.min(0.62F, saturation * 0.62F + 0.08F)));
            int nr = Math.round(value * (1.0F - vivid * (1.0F - (r - min) / (max - min))) * 255.0F);
            int ng = Math.round(value * (1.0F - vivid * (1.0F - (g - min) / (max - min))) * 255.0F);
            int nb = Math.round(value * (1.0F - vivid * (1.0F - (b - min) / (max - min))) * 255.0F);
            return color & 0xFF000000 | nr << 16 | ng << 8 | nb;
         }
      } else {
         return color;
      }
   }

   public static Color galaxy(double t) {
      double scaled = (t % 1.0 + 1.0) % 1.0 * (double)GALAXY.length;
      int index = (int)Math.floor(scaled);
      double f = scaled - (double)index;
      f = f * f * (3.0 - 2.0 * f);
      return GALAXY[index % GALAXY.length].lerp(GALAXY[(index + 1) % GALAXY.length], f);
   }

   public static int panel() {
      ClickGui gui = gui();
      int alpha = Math.round((float)gui.panelOpacity.get().intValue() / 100.0F * 255.0F);
      return alpha << 24 | gui.panelColor.get().packed() & 16777215;
   }

   public static boolean liquidGlass() {
      ClickGui gui = gui();
      return gui != null && gui.format.get() == ClickGui.Format.LiquidGlass;
   }

   public static boolean shimmer() {
      ClickGui gui = gui();
      return gui != null && gui.format.get() == ClickGui.Format.LiquidGlass && gui.shimmer.get();
   }

   public static int glassTint() {
      ClickGui gui = gui();
      float amount = gui == null ? 0.0F : (float)gui.glassTint.get().intValue() / 100.0F * 0.6F;
      return mix(724241, accent(), amount);
   }

   public static int glassRim() {
      ClickGui gui = gui();
      float amount = gui == null ? 0.0F : Math.min(1.0F, (float)gui.glassTint.get().intValue() / 100.0F + 0.15F) * 0.5F;
      return mix(16777215, accent(), amount);
   }

   private static int mix(int from, int to, float amount) {
      int r = Math.round((float)(from >> 16 & 0xFF) + (float)((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * amount);
      int g = Math.round((float)(from >> 8 & 0xFF) + (float)((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * amount);
      int b = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * amount);
      return r << 16 | g << 8 | b;
   }

   public static int panelSolid(int alpha) {
      return (alpha & 0xFF) << 24 | gui().panelColor.get().packed() & 16777215;
   }

   public static int text() {
      return 0xFF000000 | gui().textColor.get().packed();
   }

   public static int fill(int base) {
      int start = base >>> 24;
      int opacity = gui().panelOpacity.get();
      int alpha = opacity <= 88 ? Math.round((float)(start * opacity) / 88.0F) : Math.round((float)start + (float)((255 - start) * (opacity - 88)) / 12.0F);
      return Math.max(0, Math.min(255, alpha)) << 24 | tint(base) & 16777215;
   }

   public static int opacity(int base) {
      ClickGui gui = gui();
      if (gui == null) {
         return base;
      } else {
         int start = base >>> 24;
         int opacity = gui.panelOpacity.get();
         int alpha = opacity <= 88 ? Math.round((float)(start * opacity) / 88.0F) : Math.round((float)start + (float)((255 - start) * (opacity - 88)) / 12.0F);
         return Math.max(0, Math.min(255, alpha)) << 24 | base & 16777215;
      }
   }

   public static int tint(int base) {
      int panel = gui().panelColor.get().packed();
      int r = shift(base >> 16, panel >> 16, 27);
      int g = shift(base >> 8, panel >> 8, 6942);
      int b = shift(base, panel, 1777187);
      return base & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static int shift(int base, int panel, int reference) {
      return Math.max(0, Math.min(255, (base & 0xFF) + (panel & 0xFF) - (reference & 0xFF)));
   }

   public static int textTint(int base) {
      int text = gui().textColor.get().packed();
      int r = (base >> 16 & 0xFF) * (text >> 16 & 0xFF) / 255;
      int g = (base >> 8 & 0xFF) * (text >> 8 & 0xFF) / 255;
      int b = (base & 0xFF) * (text & 0xFF) / 255;
      return base & 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static float animationSpeed() {
      return (float)gui().animationSpeed.get().intValue() / 100.0F;
   }

   private static class_2583 styleFor(Fonts.Family family, Fonts.Weight weight) {
      return switch (family) {
         case Minecraft -> weight.ordinal() >= Fonts.Weight.Bold.ordinal()
         ? class_2583.field_24360.method_27704(Fonts.VANILLA).method_10982(true)
         : class_2583.field_24360.method_27704(Fonts.VANILLA);
         case Unifont -> class_2583.field_24360.method_27704(Fonts.UNIFONT);
         default -> class_2583.field_24360.method_27704(family.font(weight));
      };
   }

   public static class_2583 style() {
      return styleFor(gui().font.get(), gui().fontWeight.get());
   }

   public static class_11719 gameFont(class_11719 source) {
      if (source != class_11719.field_61972 && !class_11719.field_61972.equals(source)) {
         return source;
      } else {
         ClickGui gui = gui();
         if (gui == null) {
            return source;
         } else {
            Fonts.Family family = gui.gameFont.get();
            if (family == Fonts.Family.Minecraft) {
               return source;
            } else {
               return family == Fonts.Family.Unifont ? Fonts.UNIFONT : family.font(gui.gameWeight.get());
            }
         }
      }
   }

   private static Fonts.Family cleanFamily() {
      ClickGui gui = gui();
      Fonts.Family family = gui == null ? Fonts.Family.Ubuntu : gui.font.get();
      return family.custom() ? family : Fonts.Family.Ubuntu;
   }

   public static class_2583 crispStyle() {
      return class_2583.field_24360.method_27704(cleanFamily().large(Fonts.Weight.Medium));
   }

   public static class_11734 fieldFormatter() {
      return (value, first) -> visitor -> class_5223.method_27479(value, crispStyle(), visitor);
   }

   public static String crispFile(boolean heavy) {
      ClickGui gui = gui();
      Fonts.Weight weight = gui == null ? Fonts.Weight.Medium : gui.fontWeight.get();
      return cleanFamily().file(heavy ? weight.heavier() : weight);
   }

   public static float crispScale() {
      return cleanFamily().scale();
   }

   public static boolean menuCrisp() {
      ClickGui gui = gui();
      return gui != null && gui.font.get().custom();
   }

   public static class_2561 text(String value) {
      return class_2561.method_43470(value).method_10862(style());
   }

   public static boolean smoothFont(String value) {
      ClickGui gui = gui();
      return (gui == null || gui.font.get() == Fonts.Family.Xuong) && SdfText.supports(value);
   }

   public static class_2561 bold(String value) {
      Fonts.Family family = gui().font.get();
      return class_2561.method_43470(value)
         .method_10862(styleFor(family, family == Fonts.Family.Minecraft ? Fonts.Weight.Bold : gui().fontWeight.get().heavier()));
   }

   public static class_2561 large(String value) {
      Fonts.Family family = gui().font.get();
      return (class_2561)(!family.custom()
         ? bold(value)
         : class_2561.method_43470(value).method_10862(class_2583.field_24360.method_27704(family.large(gui().fontWeight.get().heavier()))));
   }

   public static String label(String value) {
      return gui().uppercase.get() ? value.toUpperCase(Locale.ROOT) : value;
   }
}
