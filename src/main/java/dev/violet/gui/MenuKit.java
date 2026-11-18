package dev.violet.gui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.modules.ClickGui;
import dev.violet.modules.Configs;
import dev.violet.render.Color;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_10789;
import net.minecraft.class_156;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;

public final class MenuKit {
   public static final class_2960 BRAND = class_2960.method_60655("violet", "textures/gui/brand.png");
   static final RenderPipeline ICON_PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60655("violet", "icon_sdf"))
      .withVertexShader(class_2960.method_60656("core/position_tex_color"))
      .withFragmentShader(class_2960.method_60655("violet", "icon_sdf"))
      .withUniform("DynamicTransforms", class_10789.field_60031)
      .withUniform("Projection", class_10789.field_60031)
      .withSampler("Sampler0")
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withVertexFormat(class_290.field_1575, class_5596.field_27382)
      .build();
   static final int DARK = -15069650;
   static final int TEXT = -855051;
   static final int MUTED = -4012338;
   static final int FAINT = -6906969;
   private static final Object[][] THEMES = new Object[][]{
      {"Lavender", new Color(176, 140, 240, 255)},
      {"Blossom", new Color(255, 111, 181, 255)},
      {"Amethyst", new Color(167, 121, 255, 255)},
      {"Ocean", new Color(79, 163, 255, 255)},
      {"Crimson", new Color(232, 58, 74, 255)},
      {"Jade", new Color(63, 214, 155, 255)},
      {"Sunset", new Color(255, 150, 60, 255)},
      {"Rainbow", ClickGui.AccentMode.Rainbow},
      {"Galaxy", ClickGui.AccentMode.Galaxy}
   };
   private static final Deque<Module> RECENT = new ArrayDeque<>();
   private static final Map<String, Float> WIDTHS = lru(6000);
   private static final Map<String, String> FITS = lru(2000);
   private static final Map<String, List<String>> WRAPS = lru(600);

   private static <V> Map<String, V> lru(int limit) {
      return new LinkedHashMap<String, V>(256, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Entry<String, V> eldest) {
            return this.size() > limit;
         }
      };
   }

   private MenuKit() {
   }

   static ClickGui options() {
      return SoberClient.MODULES.get(ClickGui.class);
   }

   static int accent() {
      return 0xFF000000 | Theme.accent();
   }

   static int alpha(int color, float opacity) {
      int a = Math.round((float)(color >>> 24) * Math.max(0.0F, Math.min(1.0F, opacity)));
      return a << 24 | color & 16777215;
   }

   static int withAlpha(int color, int a) {
      return (Math.max(0, Math.min(255, a)) & 0xFF) << 24 | color & 16777215;
   }

   static int lerp(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int aa = a >>> 24;
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return Math.round((float)aa + (float)(ba - aa) * t) << 24
         | Math.round((float)ar + (float)(br - ar) * t) << 16
         | Math.round((float)ag + (float)(bg - ag) * t) << 8
         | Math.round((float)ab + (float)(bb - ab) * t);
   }

   static float quint(float p) {
      p = Math.max(0.0F, Math.min(1.0F, p));
      float inverse = 1.0F - p;
      return 1.0F - inverse * inverse * inverse * inverse * inverse;
   }

   static float approach(float current, float target, float speed, float delta) {
      float value = current + (target - current) * (1.0F - (float)Math.exp((double)(-delta * speed * Theme.animationSpeed())));
      return Math.abs(value - target) < 0.002F ? target : value;
   }

   static void text(class_332 context, String value, float x, float y, float size, int color, boolean heavy) {
      ScreenStyle.crisp(context, value, x, y, size, color, heavy);
   }

   static float width(String value, float size, boolean heavy) {
      if (value != null && !value.isEmpty()) {
         String key = Theme.crispFile(heavy) + "|" + size + "|" + value;
         Float cached = WIDTHS.get(key);
         if (cached != null) {
            return cached;
         } else {
            float measured = ScreenStyle.crispWidth(value, size, heavy);
            WIDTHS.put(key, measured);
            return measured;
         }
      } else {
         return 0.0F;
      }
   }

   static void centered(class_332 context, String value, float cx, float y, float size, int color, boolean heavy) {
      text(context, value, cx - width(value, size, heavy) / 2.0F, y, size, color, heavy);
   }

   static String fit(String value, float max, float size, boolean heavy) {
      if (value == null) {
         return "";
      } else if (max < 8.0F) {
         return "";
      } else if (width(value, size, heavy) <= max) {
         return value;
      } else {
         String key = Theme.crispFile(heavy) + "|" + size + "|" + Math.round(max * 2.0F) + "|" + value;
         String cached = FITS.get(key);
         if (cached != null) {
            return cached;
         } else {
            int low = 0;
            int high = value.length();

            while (low < high) {
               int middle = (low + high + 1) / 2;
               if (width(value.substring(0, middle).trim() + "...", size, heavy) <= max) {
                  low = middle;
               } else {
                  high = middle - 1;
               }
            }

            String result = value.substring(0, low).trim() + "...";
            FITS.put(key, result);
            return result;
         }
      }
   }

   static String tail(String value, float max, float size, boolean heavy) {
      if (width(value, size, heavy) <= max) {
         return value;
      } else {
         int low = 0;
         int high = value.length();

         while (low < high) {
            int middle = (low + high) / 2;
            if (width(value.substring(middle), size, heavy) <= max) {
               high = middle;
            } else {
               low = middle + 1;
            }
         }

         return value.substring(low);
      }
   }

   static List<String> wrap(String value, float max, float size, boolean heavy) {
      if (value != null && !value.isEmpty()) {
         String key = Theme.crispFile(heavy) + "|" + size + "|" + Math.round(max * 2.0F) + "|" + value;
         List<String> cached = WRAPS.get(key);
         if (cached != null) {
            return cached;
         } else {
            List<String> lines = wrapUncached(value, max, size, heavy);
            WRAPS.put(key, lines);
            return lines;
         }
      } else {
         return new ArrayList<>();
      }
   }

   private static List<String> wrapUncached(String value, float max, float size, boolean heavy) {
      List<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : value.split(" ")) {
         String next = line.isEmpty() ? word : line + " " + word;
         if (width(next, size, heavy) > max && !line.isEmpty()) {
            lines.add(line.toString());
            line = new StringBuilder(word);
         } else {
            line = new StringBuilder(next);
         }
      }

      if (!line.isEmpty()) {
         lines.add(line.toString());
      }

      return lines;
   }

   static String label(String value) {
      return value.toUpperCase(Locale.ROOT);
   }

   static void panel(class_332 context, int x, int y, int w, int h, int r, float opacity) {
      if (w > 0 && h > 0 && !(opacity <= 0.01F)) {
         if (Theme.liquidGlass()) {
            Smooth.liquid(context, x, y, w, h, r, opacity, true);
         } else {
            int base = Theme.fill(-484893920);
            base = withAlpha(base, Math.round((float)((base >>> 24) * options().glassOpacity.get()) / 100.0F));
            Smooth.rect(context, x, y + 2, w, h + 2, r, alpha(805306368, opacity));
            Smooth.rect(context, x, y, w, h, r, alpha(base, opacity));
            Smooth.sheen(context, x, y, w, h, r, alpha(318767103, opacity));
            Smooth.outline(context, x, y, w, h, r, alpha(620756991, opacity));
         }
      }
   }

   static void shadow(class_332 context, int x, int y, int w, int h, int r, float opacity) {
      ScreenStyle.shadow(context, x, y, w, h, r, opacity);
   }

   static void well(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
      Smooth.rect(context, x, y, w, h, r, alpha(lerp(318767103, 553648127, hover), opacity));
      Smooth.outline(context, x, y, w, h, r, alpha(lerp(486539263, 889192447, hover), opacity));
   }

   static void toggle(class_332 context, float x, float y, float w, float h, float on, float opacity) {
      int ix = Math.round(x);
      int iy = Math.round(y);
      int iw = Math.round(w);
      int ih = Math.round(h);
      int accent = accent();
      if (on > 0.01F) {
         Smooth.glow(context, ix + iw / 2, iy + ih / 2, Math.round((float)iw * 0.75F), alpha(withAlpha(accent, 85), on * opacity));
      }

      Smooth.rect(context, ix, iy, iw, ih, ih / 2, alpha(lerp(1023410175, accent, on), opacity));
      if (on < 0.99F) {
         Smooth.outline(context, ix, iy, iw, ih, ih / 2, alpha(587202559, (1.0F - on) * opacity));
      }

      int knob = ih - 4;
      int kx = Math.round((float)(ix + 2) + (float)(iw - knob - 4) * on);
      Smooth.rect(context, kx, iy + 2, knob, knob, knob / 2, alpha(-855051, opacity));
   }

   static void chip(class_332 context, String value, float x, float y, float size, float opacity) {
      float w = width(value, size, true) + 10.0F;
      float h = 8.4F * size + 6.0F;
      Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 4, alpha(1510606354, opacity));
      Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 4, alpha(822083583, opacity));
      text(context, value, x + 5.0F, y + 3.0F, size, alpha(-420285451, opacity), true);
   }

   static float chipWidth(String value, float size) {
      return width(value, size, true) + 10.0F;
   }

   static void hint(class_332 context, float x, float y, String key, String action, float opacity) {
      chip(context, key, x, y, 0.62F, opacity);
      text(context, action, x + chipWidth(key, 0.62F) + 4.0F, y + 3.0F, 0.66F, alpha(-4012338, opacity), false);
   }

   static float hintWidth(String key, String action) {
      return chipWidth(key, 0.62F) + 4.0F + width(action, 0.66F, false);
   }

   static void brand(class_332 context, float x, float y, float size, float opacity) {
      Smooth.glow(context, Math.round(x + size / 2.0F), Math.round(y + size / 2.0F), Math.round(size * 0.85F), alpha(777384181, opacity));
      Smooth.brandCrisp(context, x, y, size, alpha(-1, opacity));
   }

   static void tooltip(class_332 context, String tip, int mouseX, int mouseY, int screenW, int screenH, float opacity) {
      tooltip(context, tip, mouseX, mouseY, screenW, screenH, opacity, false);
   }

   static void tooltip(class_332 context, String tip, int mouseX, int mouseY, int screenW, int screenH, float opacity, boolean light) {
      if (tip != null && !tip.isEmpty() && !(opacity <= 0.01F)) {
         List<String> lines = wrap(tip, 190.0F, 0.72F, false);
         float w = 0.0F;

         for (String line : lines) {
            w = Math.max(w, width(line, 0.72F, false));
         }

         int bw = Math.round(w + 14.0F);
         int bh = lines.size() * 10 + 9;
         int tx = Math.max(4, Math.min(mouseX + 12, screenW - bw - 4));
         int ty = Math.max(4, Math.min(mouseY + 14, screenH - bh - 4));
         Smooth.rect(context, tx, ty + 2, bw, bh, 6, alpha(light ? 402653184 : 1342177280, opacity));
         Smooth.rect(context, tx, ty, bw, bh, 6, alpha(light ? -50331649 : -182772959, opacity));
         Smooth.outline(context, tx, ty, bw, bh, 6, alpha(light ? 469762048 : 654311423, opacity));
         float ly = (float)(ty + 5);

         for (String line : lines) {
            text(context, line, (float)(tx + 7), ly, 0.72F, alpha(light ? -14935010 : -1644308, opacity), false);
            ly += 10.0F;
         }
      }
   }

   static void background(class_332 context, class_310 client, int width, int height, float alpha, boolean blur) {
      ClickGui options = options();
      if (blur && options.blur.get()) {
         context.method_71278();
      }

      boolean liquid = Theme.liquidGlass();
      int dim = Math.round((float)(255 * options.backgroundDim.get()) / 100.0F * alpha * (liquid ? 0.45F : 1.0F));
      context.method_25294(0, 0, width, height, withAlpha(526861, dim));
      if (liquid) {
         float haze = (float)options.glassTint.get().intValue() / 100.0F * alpha;
         context.method_25296(0, 0, width, height, withAlpha(Theme.glassTint(), Math.round(80.0F * haze)), withAlpha(656912, Math.round(112.0F * haze)));
      }
   }

   static Icons.Icon icon(Category category) {
      return switch (category) {
         case BASE_FINDING -> Icons.Icon.SEARCH;
         case DONUT -> Icons.Icon.DONUT;
         case COMBAT -> Icons.Icon.SWORD;
         case VISUALS -> Icons.Icon.EYE;
         case MOVEMENT -> Icons.Icon.SPEED;
         case CLIENT -> Icons.Icon.GEAR;
         case CONFIG -> Icons.Icon.COLUMNS;
      };
   }

   static Icons.Icon icon(Module module) {
      String name = module.name.toLowerCase(Locale.ROOT);
      if (name.equals("sky")) {
         return Icons.Icon.MOON;
      } else if (name.contains("glint")) {
         return Icons.Icon.SPARKLE;
      } else if (name.contains("accessories") || name.contains("crown")) {
         return Icons.Icon.CROWN;
      } else if (name.contains("crosshair")) {
         return Icons.Icon.CROSSHAIR;
      } else if (name.contains("freecam")) {
         return Icons.Icon.CAMERA;
      } else if (name.contains("jump circle")) {
         return Icons.Icon.RINGS;
      } else if (name.contains("name tag")) {
         return Icons.Icon.TAG;
      } else if (name.contains("skin")) {
         return Icons.Icon.PERSON;
      } else if (name.contains("swing")) {
         return Icons.Icon.SWING;
      } else if (name.contains("weather")) {
         return Icons.Icon.CLOUD;
      } else if (name.contains("key") || name.contains("typing")) {
         return Icons.Icon.KEY;
      } else if (name.contains("blur") || name.contains("speed") || name.contains("fly") || name.contains("sprint")) {
         return Icons.Icon.SPEED;
      } else if (name.contains("look") || name.contains("esp") || name.contains("inspector") || name.contains("hider")) {
         return Icons.Icon.EYE;
      } else if (name.contains("overlay") || name.contains("model") || name.contains("chunk")) {
         return Icons.Icon.CUBE;
      } else if (name.contains("tool") || name.contains("mine")) {
         return Icons.Icon.PICKAXE;
      } else if (name.contains("sell") || name.contains("shop") || name.contains("auction") || name.contains("inventory")) {
         return Icons.Icon.CHEST;
      } else if (name.contains("beam") || name.contains("live") || name.contains("notifier")) {
         return Icons.Icon.PULSE;
      } else if (name.contains("hud") || name.contains("menu") || name.contains("spotify") || name.contains("map")) {
         return Icons.Icon.WINDOW;
      } else if (name.contains("click gui") || name.contains("ribbon")) {
         return Icons.Icon.GRID;
      } else if (name.contains("config")) {
         return Icons.Icon.COLUMNS;
      } else if (name.contains("totem") || name.contains("crystal") || name.contains("anchor")) {
         return Icons.Icon.SPARKLE;
      } else {
         return !name.contains("rtp") && !name.contains("relog") ? icon(module.category) : Icons.Icon.CHEVRONS;
      }
   }

   static void iconBox(class_332 context, Module module, float x, float y, float size, float on, float opacity) {
      int ix = Math.round(x);
      int iy = Math.round(y);
      int is = Math.round(size);
      int r = Math.max(3, Math.round(size * 0.3F));
      int accent = accent();
      if (on > 0.01F) {
         Smooth.glow(context, ix + is / 2, iy + is / 2, Math.round(size * 0.95F), alpha(withAlpha(accent, 80), on * opacity));
      }

      Smooth.rect(context, ix, iy, is, is, r, alpha(lerp(318767103, withAlpha(accent, 68), on), opacity));
      Smooth.outline(context, ix, iy, is, is, r, alpha(lerp(587202559, withAlpha(accent, 144), on), opacity));
      int glyph = Math.round(size * 0.52F);
      Icons.draw(context, icon(module), ix + (is - glyph) / 2, iy + (is - glyph) / 2, glyph, alpha(lerp(-420285451, lerp(accent, -1, 0.55F), on), opacity));
   }

   static void touch(Module module) {
      if (module != null) {
         RECENT.remove(module);
         RECENT.addFirst(module);

         while (RECENT.size() > 4) {
            RECENT.removeLast();
         }
      }
   }

   static List<Module> recent() {
      return new ArrayList<>(RECENT);
   }

   static int count(Category category) {
      int count = 0;

      for (Module module : SoberClient.MODULES.all()) {
         if (module.category == category) {
            count++;
         }
      }

      return count;
   }

   static boolean opensMenu(Module module) {
      return module instanceof ClickGui || module instanceof Configs;
   }

   public static class_437 menu() {
      ClickGui options = options();
      return (class_437)(options != null && options.style.get() == ClickGui.Style.Panel ? new PanelScreen() : new DropdownScreen());
   }

   static int presetCount(Module module) {
      if (module instanceof Presets presets) {
         return presets.presetCount();
      } else {
         return module instanceof ClickGui ? THEMES.length : 0;
      }
   }

   static int themeCount() {
      return THEMES.length;
   }

   static int themeColor(int index) {
      return THEMES[index][1] instanceof Color color ? color.packed() & 16777215 : -1;
   }

   static String themeName(int index) {
      return (String)THEMES[index][0];
   }

   static String presetName(Module module, int index) {
      return module instanceof Presets presets ? presets.presetName(index) : (String)THEMES[index][0];
   }

   static boolean presetActive(Module module, int index) {
      if (module instanceof Presets presets) {
         return presets.presetActive(index);
      } else {
         ClickGui gui = options();
         Object value = THEMES[index][1];
         return value instanceof ClickGui.AccentMode mode
            ? gui.accentMode.get() == mode
            : gui.accentMode.get() == ClickGui.AccentMode.Static && gui.accent.get().equals(value);
      }
   }

   static String presetDescription(Module module) {
      return module instanceof Presets presets ? presets.presetDescription() : "Applies this accent color to the menus and the HUD.";
   }

   static void selectPreset(Module module, int index) {
      if (module instanceof Presets presets) {
         presets.selectPreset(index);
      } else {
         ClickGui gui = options();
         Object value = THEMES[index][1];
         if (value instanceof ClickGui.AccentMode mode) {
            gui.accentMode.set(mode);
         } else {
            gui.accentMode.set(ClickGui.AccentMode.Static);
            gui.accent.set((Color)value);
         }
      }
   }

   public static String keyName(int key) {
      return key < 0 ? "None" : class_307.field_1668.method_1447(key).method_27445().getString();
   }

   static void openLink(String link) {
      if (link != null && !link.isEmpty()) {
         try {
            class_156.method_668().method_673(URI.create(link));
         } catch (RuntimeException var2) {
            SoberClient.LOG.warn("Could not open {}", link, var2);
         }
      }
   }
}
