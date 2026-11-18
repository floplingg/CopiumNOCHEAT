package dev.violet.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.ConfigSlots;
import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.module.Status;
import dev.violet.modules.ClickGui;
import dev.violet.modules.Configs;
import dev.violet.modules.Hud;
import dev.violet.modules.SkinChange;
import dev.violet.render.Color;
import dev.violet.setting.BlockListSetting;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.ItemSetting;
import dev.violet.setting.KeySetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class DropdownScreen extends class_437 implements ModuleMenu {
   private static final int CANVAS_W = 1386;
   private static final int CANVAS_H = 780;
   private static final int PANEL_W = 166;
   private static final int GAP = 12;
   private static final int HEADER_H = 34;
   private static final int BAR_H = 34;
   private static final int ROW_H = 20;
   private static final int ROW_STEP = 21;
   private static final int PAD = 6;
   private static final int PICKER_H = 150;
   private static final int BAR_W = 436;
   private static final int PILL_H = 22;
   private static final int TOP_SPACE = 76;
   private static final int POPUP_H = 254;
   private static final int GROUP_H = 13;
   private static final String BOLD = "montserrat-bold.ttf";
   private static final String MEDIUM = "montserrat-bold.ttf";
   private static final int LINK = -12877066;
   private static final ExactImage LOGO = new ExactImage("textures/brand/orbit_glow.png");
   private static final ExactImage WORDMARK = new ExactImage("textures/brand/wordmark.png");
   private static final String[] BOTTOM = new String[]{"CONFIGS", "HUD", "SETTINGS"};
   private static final int PANEL_BASE = -401270497;
   private static final int CARD_BASE = 201326591;
   private static final int CARD_HOVER_BASE = 385875967;
   private static final int SUB_BASE = 150994943;
   private static final int SUB_HOVER_BASE = 318767103;
   private static final int BORDER_BASE = 956301311;
   private static final int TEXT_BASE = -1184016;
   private static final int MUTED_BASE = -6052178;
   private static final int VALUE_BASE = 352321535;
   private static int PANEL = -401270497;
   private static int CARD = 201326591;
   private static int CARD_HOVER = 385875967;
   private static int SUB = 150994943;
   private static int SUB_HOVER = 318767103;
   private static int BORDER = 956301311;
   private static int TEXT = -1184016;
   private static int MUTED = -6052178;
   private static int ACCENT = -2762274;
   private static int VALUE = 352321535;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("soberclient-dropdown.json");
   private static final EnumMap<Category, DropdownScreen.Panel> PANELS = new EnumMap<>(Category.class);
   private static final List<DropdownScreen.Panel> ORDER = new ArrayList<>();
   private static final Set<Module> EXPANDED = new HashSet<>();
   private static final Set<ColorSetting> OPEN_COLORS = new HashSet<>();
   private final Map<Object, Float> animations = new HashMap<>();
   private final Map<DropdownScreen.Key, DropdownScreen.Timed> timed = new HashMap<>();
   private Supplier<class_8685> skin;
   private long frame = System.nanoTime();
   private final Map<Object, Long> pressedAt = new HashMap<>();
   private final List<DropdownScreen.Hit> hits = new ArrayList<>();
   private String search = "";
   private final TypedText searchText = new TypedText();
   private boolean searching;
   private DropdownScreen.Panel draggingPanel;
   private float grabX;
   private float grabY;
   private double pressX;
   private double pressY;
   private boolean panelMoved;
   private DropdownScreen.Hit dragging;
   private Setting<?> editing;
   private String editBuffer = "";
   private long invalidUntil;
   private Setting<?> invalidSetting;
   private Module binding;
   private KeySetting keyBinding;
   private long swallowUntil;
   private long lastFrame = System.nanoTime();
   private float delta;
   private float openProgress;
   private final long openedAt = System.nanoTime();
   private float alpha;
   private boolean closing;
   private float renderScale = 1.0F;
   private float scaleShown = -1.0F;
   private Object hoveredKey;
   private long hoverStart;
   private String tooltip;
   private boolean configOpen;
   private float popupOpen;
   private int popupX;
   private int popupY;
   private boolean renaming;
   private String renameBuffer = "";
   private static final int GLASS_VEIL = -2146169816;

   private static float layoutLeft(int count) {
      return (float)(1386 - count * 178 + 12) / 2.0F;
   }

   private static float layoutWidth(int count) {
      return (float)(count * 178 - 12);
   }

   public DropdownScreen() {
      super(class_2561.method_43470("SoberClient"));
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25432() {
      SoberClient.CONFIG.save();
      save();
   }

   public void method_25419() {
      if (!this.closing) {
         if (this.editing != null) {
            this.commitEdit();
         }

         if (this.renaming) {
            this.commitRename();
         }

         this.binding = null;
         this.closing = true;
         UiSounds.close();
      }
   }

   private static ClickGui options() {
      return SoberClient.MODULES.get(ClickGui.class);
   }

   private static Configs configs() {
      return SoberClient.MODULES.get(Configs.class);
   }

   private static int withAlpha(int color, int a) {
      return (Math.max(0, Math.min(255, a)) & 0xFF) << 24 | color & 16777215;
   }

   private static int lerp(int a, int b, float t) {
      int aa = a >>> 24;
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)((float)aa + (float)(ba - aa) * t) << 24
         | (int)((float)ar + (float)(br - ar) * t) << 16
         | (int)((float)ag + (float)(bg - ag) * t) << 8
         | (int)((float)ab + (float)(bb - ab) * t);
   }

   private int col(int color) {
      return withAlpha(color, Math.round((float)(color >>> 24) * this.alpha));
   }

   private static void palette() {
      PANEL = Theme.fill(-401270497);
      CARD = Theme.tint(201326591);
      CARD_HOVER = Theme.tint(385875967);
      SUB = Theme.tint(150994943);
      SUB_HOVER = Theme.tint(318767103);
      BORDER = Theme.tint(956301311);
      TEXT = Theme.textTint(-1184016);
      MUTED = Theme.textTint(Theme.liquidGlass() ? -3749167 : -6052178);
      ACCENT = Theme.accent();
      VALUE = Theme.tint(352321535);
      if (Theme.liquidGlass()) {
         CARD = 486539263;
         CARD_HOVER = 822083583;
         SUB = 318767103;
         SUB_HOVER = 620756991;
         BORDER = 1090519039;
      }
   }

   private int panel(int color) {
      return withAlpha(color, Math.round((float)((color >>> 24) * options().glassOpacity.get()) / 100.0F));
   }

   private float approach(float current, float target, float speed) {
      float value = current + (target - current) * (1.0F - (float)Math.exp((double)(-this.delta * speed * Theme.animationSpeed())));
      return Math.abs(value - target) < 0.002F ? target : value;
   }

   private static int millis(String tag) {
      return switch (tag) {
         case "open", "bool", "switch" -> 200;
         case "expand", "picker" -> 250;
         default -> 150;
      };
   }

   private static boolean eased(String tag) {
      return switch (tag) {
         case "open", "expand", "picker", "match" -> true;
         default -> false;
      };
   }

   private float anim(String tag, Object target, boolean on) {
      DropdownScreen.Key key = new DropdownScreen.Key(tag, target);
      float goal = on ? 1.0F : 0.0F;
      DropdownScreen.Timed timed = this.timed.get(key);
      if (timed == null) {
         timed = new DropdownScreen.Timed(goal);
         this.timed.put(key, timed);
      } else if (timed.target != goal) {
         timed.from = timed.at(this.frame);
         timed.target = goal;
         timed.start = this.frame;
         timed.duration = (long)Math.round((float)((long)millis(tag) * 1000000L) / Math.max(0.05F, Theme.animationSpeed()));
         timed.eased = eased(tag);
      }

      return timed.at(this.frame);
   }

   private float value(String tag, Object target) {
      DropdownScreen.Timed timed = this.timed.get(new DropdownScreen.Key(tag, target));
      return timed == null ? 0.0F : timed.at(this.frame);
   }

   private float press(Object key) {
      Long time = this.pressedAt.get(key);
      return time == null ? 0.0F : Math.max(0.0F, 1.0F - (float)(System.nanoTime() - time) / 2.1E8F);
   }

   private float fitScale() {
      return Math.min((float)this.field_22789 / 1386.0F, (float)this.field_22790 / 780.0F);
   }

   private float baseScale() {
      if (this.scaleShown < 0.0F) {
         this.scaleShown = (float)options().guiScale.get().intValue();
      }

      return Math.max(0.2F, this.fitScale() * this.scaleShown / 100.0F);
   }

   private boolean previewingScale() {
      return this.dragging != null && this.dragging.target() == options().guiScale;
   }

   private void drawScalePreview(class_332 context) {
      float shown = this.anim("scalepreview", options().guiScale, this.previewingScale());
      if (!(shown <= 0.01F)) {
         int percent = options().guiScale.get();
         float target = Math.max(0.2F, this.fitScale() * (float)percent / 100.0F);
         float w = 1386.0F * target;
         float h = 780.0F * target;
         float x = ((float)this.field_22789 - w) / 2.0F;
         float y = ((float)this.field_22790 - h) / 2.0F;
         float previous = this.alpha;
         this.alpha = previous * shown;
         this.rect(context, x, y, w, h, 8, withAlpha(ACCENT, 20));
         int dash = 7;
         int gap = 5;
         int left = Math.round(x);
         int top = Math.round(y);
         int right = Math.round(x + w);
         int bottom = Math.round(y + h);
         int line = this.col(withAlpha(ACCENT, 230));

         for (int px = left; px < right; px += dash + gap) {
            context.method_25294(px, top, Math.min(right, px + dash), top + 1, line);
            context.method_25294(px, bottom - 1, Math.min(right, px + dash), bottom, line);
         }

         for (int py = top; py < bottom; py += dash + gap) {
            context.method_25294(left, py, left + 1, Math.min(bottom, py + dash), line);
            context.method_25294(right - 1, py, right, Math.min(bottom, py + dash), line);
         }

         double factor = (double)this.field_22787.method_22683().method_4495();
         String label = percent + "%";
         String size = Math.round((double)w * factor) + " × " + Math.round((double)h * factor);
         float big = 30.0F;
         float small = 9.0F;
         float labelW = CrispText.width(label, "ubuntu-bold.ttf", big, 0.0F);
         float sizeW = CrispText.width(size, "ubuntu-medium.ttf", small, 0.0F);
         float boxW = Math.max(labelW, sizeW) + 36.0F;
         float boxH = 58.0F;
         float cx = (float)this.field_22789 / 2.0F;
         float cy = (float)this.field_22790 / 2.0F;
         this.rect(context, cx - boxW / 2.0F, cy - boxH / 2.0F, boxW, boxH, 12, withAlpha(1316380, 216));
         CrispText.draw(context, label, "ubuntu-bold.ttf", cx - labelW / 2.0F, cy + 6.0F, big, 0.0F, this.col(-1));
         CrispText.draw(context, size, "ubuntu-medium.ttf", cx - sizeW / 2.0F, cy + 20.0F, small, 0.0F, this.col(withAlpha(16777215, 168)));
         CrispText.draw(context, "New size", "ubuntu-medium.ttf", (float)(left + 8), (float)(top + 14), small, 0.0F, this.col(withAlpha(ACCENT, 240)));
         this.alpha = previous;
      }
   }

   private float viewLeft() {
      return 693.0F - (float)this.field_22789 / (2.0F * this.baseScale());
   }

   private float viewRight() {
      return 693.0F + (float)this.field_22789 / (2.0F * this.baseScale());
   }

   private float viewTop() {
      return 390.0F - (float)this.field_22790 / (2.0F * this.baseScale());
   }

   private float viewBottom() {
      return 390.0F + (float)this.field_22790 / (2.0F * this.baseScale());
   }

   private double uiX(double x) {
      return (x - (double)this.field_22789 / 2.0) / (double)this.renderScale + 693.0;
   }

   private double uiY(double y) {
      return (y - (double)this.field_22790 / 2.0) / (double)this.renderScale + 390.0;
   }

   private void rect(class_332 context, float x, float y, float w, float h, int r, int color) {
      if (color >>> 24 != 0) {
         Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.col(color));
      }
   }

   private void glass(class_332 context, int x, int y, int w, int h, int r, int color) {
      if (Theme.liquidGlass()) {
         Smooth.liquid(context, x, y, w, h, r, this.alpha, true);
         Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(-2146169816, this.alpha));
      } else {
         Smooth.rect(context, x, y, w, h, r, this.col(color));
      }
   }

   private void text(class_332 context, String value, int x, int y, int color) {
      if (Theme.menuCrisp()) {
         ScreenStyle.crisp(context, value, (float)x, (float)y, 1.0F, this.col(color), false);
      } else if (Theme.smoothFont(value)) {
         SdfText.draw(context, value, x, y, this.col(color));
      } else {
         context.method_51439(this.field_22793, Theme.text(value), x, y, this.col(color), false);
      }
   }

   private int textWidth(String value) {
      if (Theme.menuCrisp()) {
         return (int)Math.ceil((double)ScreenStyle.crispWidth(value, 1.0F, false));
      } else {
         return Theme.smoothFont(value) ? SdfText.width(value) : this.field_22793.method_27525(Theme.text(value));
      }
   }

   private String fit(String value, int maxWidth) {
      if (maxWidth < 12) {
         return "";
      } else if (this.textWidth(value) <= maxWidth) {
         return value;
      } else {
         while (!value.isEmpty() && this.textWidth(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
         }

         return value + "...";
      }
   }

   private String tail(String value, int maxWidth) {
      while (!value.isEmpty() && this.textWidth(value) > maxWidth) {
         value = value.substring(1);
      }

      return value;
   }

   private void icon(class_332 context, Category item, int x, int y, int size) {
      String name = switch (item) {
         case BASE_FINDING -> "boxes";
         case DONUT -> "crown";
         case COMBAT -> "flame";
         case VISUALS -> "sparkles";
         case CLIENT -> "box";
         case CONFIG -> "config";
         case MOVEMENT -> "lock";
      };
      context.method_25293(
         MenuKit.ICON_PIPELINE,
         class_2960.method_60655("violet", "textures/gui/" + name + ".png"),
         x,
         y,
         0.0F,
         0.0F,
         size,
         size,
         128,
         128,
         128,
         128,
         this.col(-1)
      );
   }

   private void brand(class_332 context, int x, int y, int size) {
      Smooth.brand(context, x, y, size, this.renderScale, this.col(-1));
   }

   private void hit(DropdownScreen.Kind kind, float x, float y, float w, float h, Object target, int index) {
      this.hits.add(new DropdownScreen.Hit(kind, x, y, w, h, target, index));
   }

   private void clippedHit(DropdownScreen.Kind kind, float x, float y, float w, float h, Object target, int index, float top, float bottom) {
      float y1 = Math.max(y, top);
      float y2 = Math.min(y + h, bottom);
      if (y2 > y1) {
         this.hits.add(new DropdownScreen.Hit(kind, x, y1, w, y2 - y1, target, index));
      }
   }

   private DropdownScreen.Hit hitAt(double mx, double my) {
      for (int i = this.hits.size() - 1; i >= 0; i--) {
         if (this.hits.get(i).contains(mx, my)) {
            return this.hits.get(i);
         }
      }

      return null;
   }

   private static boolean inside(double mx, double my, float x, float y, float w, float h) {
      return mx >= (double)x && my >= (double)y && mx < (double)(x + w) && my < (double)(y + h);
   }

   private float sinceOpen() {
      return (float)(System.nanoTime() - this.openedAt) / 1.0E9F;
   }

   private static float quint(float p) {
      p = Math.max(0.0F, Math.min(1.0F, p));
      float inverse = 1.0F - p;
      return 1.0F - inverse * inverse * inverse * inverse * inverse;
   }

   private void chevron(class_332 context, float cx, float cy, float size, float rotation, int color) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(cx, cy);
      matrices.rotate(rotation);
      Smooth.triangle(context, 0.0F, 0.0F, size, this.col(color));
      matrices.popMatrix();
   }

   private int pillWidth(String label) {
      return Math.round(CrispText.width(label, "montserrat-bold.ttf", 8.0F, 0.5F)) + 26;
   }

   private float topY() {
      return this.viewTop() + 16.0F;
   }

   private float bottomY() {
      return this.viewBottom() - 22.0F - 12.0F;
   }

   private String[] bottomLabels() {
      return new String[]{BOTTOM[0], BOTTOM[1], "LAYOUT", BOTTOM[2]};
   }

   private int bottomWidth() {
      int w = 0;
      String[] labels = this.bottomLabels();

      for (String label : labels) {
         w += this.pillWidth(label);
      }

      return w + (labels.length - 1) * 6;
   }

   private float barLeft() {
      return layoutLeft(PANELS.size());
   }

   private float barWidth() {
      return layoutWidth(PANELS.size());
   }

   private boolean inBars(double mx, double my) {
      return inside(mx, my, this.barLeft(), this.topY(), this.barWidth(), 34.0F)
         ? true
         : inside(mx, my, 693.0F - (float)this.bottomWidth() / 2.0F, this.bottomY(), (float)this.bottomWidth(), 22.0F);
   }

   private void spaced(class_332 context, String value, float x, float baseline, float em, int color) {
      CrispText.draw(context, value, "montserrat-bold.ttf", x, baseline, em, 0.5F, this.col(color));
   }

   private void zeroPanel(class_332 context, float x, float y, float w, float h, int r, int fill, int border) {
      if (Theme.liquidGlass()) {
         Smooth.liquid(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.alpha, true);
         Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, MenuKit.alpha(-2146169816, this.alpha));
      } else {
         this.rect(context, x, y, w, h, r, fill);
         Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.col(border));
      }
   }

   private static int groupCount(Module module) {
      int count = 0;
      String group = null;

      for (Setting<?> setting : visibleSettings(module)) {
         if (!setting.getGroup().equals(group)) {
            group = setting.getGroup();
            count++;
         }
      }

      return count;
   }

   private List<Module> all(Category category) {
      List<Module> list = new ArrayList<>();

      for (Module module : SoberClient.MODULES.all()) {
         if (module.category == category) {
            list.add(module);
         }
      }

      return list;
   }

   private boolean matches(Module module) {
      return this.search.isEmpty() || module.name.toLowerCase(Locale.ROOT).contains(this.search.toLowerCase(Locale.ROOT));
   }

   private List<Module> modules(Category category) {
      List<Module> list = new ArrayList<>();

      for (Module module : this.all(category)) {
         if (this.matches(module)) {
            list.add(module);
         }
      }

      return list;
   }

   private static List<Setting<?>> visibleSettings(Module module) {
      List<Setting<?>> list = new ArrayList<>();

      for (Setting<?> setting : module.getSettings()) {
         if (setting.isVisible()) {
            list.add(setting);
         }
      }

      return list;
   }

   private float settingHeight(Setting<?> setting) {
      if (setting instanceof IntSetting) {
         return 28.0F;
      } else {
         return setting instanceof ColorSetting color ? 20.0F + 150.0F * this.value("picker", color) : 20.0F;
      }
   }

   private float settingsHeight(Module module) {
      float h = 4.0F;
      if (module instanceof Status status) {
         int rows = status.statusRows().size();
         if (rows > 0) {
            h += (float)(16 + rows * 30 + 3);
         }
      }

      int presets = MenuKit.presetCount(module);
      if (presets > 0) {
         h += (float)(15 + presets * 20 + 3);
      }

      for (Setting<?> setting : visibleSettings(module)) {
         h += this.settingHeight(setting);
      }

      h += (float)(13 * groupCount(module));
      if (module.bindable()) {
         h += 20.0F;
      }

      return h + 2.0F;
   }

   private float moduleHeight(Module module) {
      return 21.0F + (this.settingsHeight(module) + 4.0F) * this.value("expand", module);
   }

   private float rowHeight(Module module) {
      return this.moduleHeight(module) * this.value("match", module);
   }

   private void clampPanel(DropdownScreen.Panel panel) {
      panel.x = Math.clamp(panel.x, this.viewLeft() + 2.0F, Math.max(this.viewLeft() + 2.0F, this.viewRight() - 166.0F - 2.0F));
      panel.y = Math.clamp(panel.y, this.viewTop() + 76.0F, Math.max(this.viewTop() + 76.0F, this.viewBottom() - 34.0F - 22.0F - 30.0F));
   }

   private void front(DropdownScreen.Panel panel) {
      ORDER.remove(panel);
      ORDER.add(panel);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      if (this.field_22787.field_1687 == null) {
         super.method_25420(context, mouseX, mouseY, deltaTicks);
      } else {
         if (options().blur.get()) {
            context.method_71278();
         }

         int dim = Math.round((float)(255 * options().backgroundDim.get()) / 100.0F * this.alpha * (Theme.liquidGlass() ? 0.45F : 1.0F));
         context.method_25294(0, 0, this.field_22789, this.field_22790, withAlpha(526861, dim));
         if (Theme.liquidGlass()) {
            float haze = (float)options().glassTint.get().intValue() / 100.0F * this.alpha;
            int tint = Theme.glassTint();
            context.method_25296(
               0, 0, this.field_22789, this.field_22790, withAlpha(tint, Math.round(80.0F * haze)), withAlpha(656912, Math.round(112.0F * haze))
            );
         }
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      palette();
      long now = System.nanoTime();
      this.delta = Math.min(0.06F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      this.frame = now;
      this.openProgress = this.approach(this.openProgress, this.closing ? 0.0F : 1.0F, this.closing ? 16.0F : 12.0F);
      if (this.closing && this.openProgress < 0.025F) {
         this.field_22787.method_1507(null);
      } else {
         this.alpha = 1.0F - (float)Math.pow((double)(1.0F - this.openProgress), 3.0);
         if (this.scaleShown < 0.0F) {
            this.scaleShown = (float)options().guiScale.get().intValue();
         }

         if (!this.previewingScale()) {
            this.scaleShown = this.approach(this.scaleShown, (float)options().guiScale.get().intValue(), 11.0F);
         }

         float scale = this.baseScale();
         this.renderScale = scale;
         this.hits.clear();
         this.tooltip = null;
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate((float)this.field_22789 / 2.0F, (float)this.field_22790 / 2.0F);
         matrices.scale(scale, scale);
         matrices.translate(-693.0F, -390.0F);
         int mx = (int)Math.round(this.uiX((double)mouseX));
         int my = (int)Math.round(this.uiY((double)mouseY));
         DropdownScreen.Panel top = this.topPanel((double)mx, (double)my);
         boolean first = true;

         for (DropdownScreen.Panel panel : new ArrayList<>(ORDER)) {
            this.clampPanel(panel);
            if (!first) {
               context.method_71048();
            }

            first = false;
            boolean mouse = panel == top || this.draggingPanel == panel;
            float shown = quint((this.sinceOpen() - (float)panel.category.ordinal() * 0.04F) / 0.5F);
            float previous = this.alpha;
            this.alpha = previous * shown;
            matrices.pushMatrix();
            matrices.translate(0.0F, 10.0F * (1.0F - shown));
            this.drawPanel(context, panel, mouse ? mx : -2147483648, mouse ? my : -2147483648);
            matrices.popMatrix();
            this.alpha = previous;
         }

         context.method_71048();
         this.drawTopBar(context, mx, my);
         this.drawBottomBar(context, mx, my);
         this.drawConfigPopup(context, mx, my);
         matrices.popMatrix();
         context.method_71048();
         this.drawScalePreview(context);
         context.method_71048();
         Notifications.render(context, true);
         context.method_71048();
         this.drawTooltip(context, mouseX, mouseY);
      }
   }

   private DropdownScreen.Panel topPanel(double mx, double my) {
      if (this.inBars(mx, my)) {
         return null;
      } else if (this.popupOpen > 0.5F && inside(mx, my, (float)this.popupX, (float)this.popupY, 436.0F, 254.0F)) {
         return null;
      } else {
         for (int i = ORDER.size() - 1; i >= 0; i--) {
            DropdownScreen.Panel panel = ORDER.get(i);
            if (inside(mx, my, panel.x, panel.y, 166.0F, Math.max(34.0F, panel.height))) {
               return panel;
            }
         }

         return null;
      }
   }

   private void drawPanel(class_332 context, DropdownScreen.Panel panel, int mouseX, int mouseY) {
      List<Module> modules = this.all(panel.category);
      int matching = 0;

      for (Module module : modules) {
         boolean match = this.matches(module);
         if (match) {
            matching++;
         }

         this.anim("match", module, match);
         boolean expanded = EXPANDED.contains(module);
         this.anim("expand", module, expanded);
         if (expanded || !(this.value("expand", module) < 0.01F)) {
            for (Setting<?> setting : module.getSettings()) {
               if (setting instanceof ColorSetting color) {
                  this.anim("picker", color, OPEN_COLORS.contains(color));
               }
            }
         }
      }

      int x = Math.round(panel.x);
      int y = Math.round(panel.y);
      float open = this.anim("open", panel.category, !panel.collapsed);
      float rows = 0.0F;

      for (Module module : modules) {
         rows += this.rowHeight(module);
      }

      float content = 6.0F + rows;
      if (matching == 0) {
         content = Math.max(28.0F, content);
      }

      float maxView = Math.max(40.0F, this.bottomY() - 12.0F - (float)(y + 34));
      panel.content = content;
      panel.view = Math.min(content, maxView);
      panel.scrollTarget = Math.clamp(panel.scrollTarget, 0.0F, Math.max(0.0F, content - panel.view));
      panel.scroll = this.approach(panel.scroll, panel.scrollTarget, 16.0F);
      float body = panel.view * open;
      int h = 34 + Math.round(body);
      panel.height = (float)h;
      this.hit(DropdownScreen.Kind.PANEL, (float)x, (float)y, 166.0F, (float)h, panel, 0);
      this.zeroPanel(context, (float)x, (float)y, 166.0F, (float)h, 6, PANEL, 452984831);
      boolean overHeader = inside((double)mouseX, (double)mouseY, (float)x, (float)y, 166.0F, 34.0F);
      float headerHover = this.anim("header", panel.category, overHeader || this.draggingPanel == panel);
      if (headerHover > 0.01F) {
         this.rect(context, (float)(x + 4), (float)(y + 4), 158.0F, 26.0F, 4, withAlpha(16777215, Math.round(12.0F * headerHover)));
      }

      this.icon(context, panel.category, x + 12, y + 11, 12);
      String title = this.fit(panel.category.title.toUpperCase(Locale.ROOT), 106);
      this.spaced(context, title, (float)(x + 32), (float)y + 20.5F, 9.2F, -1);
      float fold = Math.max(headerHover, 1.0F - open);
      if (fold > 0.01F) {
         this.chevron(
            context,
            (float)(x + 166 - 15),
            (float)y + 17.0F,
            5.0F,
            (float)(1.5707963267948966 + 1.5707963267948966 * (double)open),
            withAlpha(lerp(MUTED, TEXT, headerHover), Math.round(255.0F * fold))
         );
      }

      this.hit(DropdownScreen.Kind.HEADER, (float)x, (float)y, 166.0F, 34.0F, panel, 0);
      if (overHeader) {
         this.hover(panel.category, "Drag to move. Click to fold the panel in or out.");
      }

      if (!(body < 0.5F)) {
         int top = y + 34 - 2;
         int bottom = top + Math.round(body);
         context.method_44379(x, top, x + 166, bottom);
         float sy = (float)top - panel.scroll;
         float since = this.sinceOpen() - (float)panel.category.ordinal() * 0.04F - 0.08F;
         int row = 0;

         for (Module module : modules) {
            float mh = this.rowHeight(module);
            if (!(mh < 0.5F)) {
               int clipTop = Math.max(top, Math.round(sy));
               int clipBottom = Math.min(bottom, Math.round(sy + mh));
               if (clipBottom > clipTop) {
                  float shown = quint((since - (float)row * 0.012F) / 0.35F);
                  float rowAlpha = this.alpha;
                  this.alpha = rowAlpha * shown;
                  boolean clip = this.value("match", module) < 1.0F;
                  if (clip) {
                     context.method_44379(x, clipTop, x + 166, clipBottom);
                  }

                  this.drawModule(
                     context, module, x, sy, panel.content > panel.view + 1.0F ? 6 : 0, mouseX, mouseY, clip ? clipTop : top, clip ? clipBottom : bottom
                  );
                  if (clip) {
                     context.method_44380();
                  }

                  this.alpha = rowAlpha;
               }

               sy += mh;
               row++;
            }
         }

         if (matching == 0) {
            float gone = 1.0F - Math.min(1.0F, rows / 21.0F);
            if (gone > 0.01F) {
               this.text(context, this.search.isEmpty() ? "Empty" : "No matches", x + 12, top + 9, withAlpha(MUTED, Math.round(255.0F * gone)));
            }
         }

         context.method_44380();
         if (panel.content > panel.view + 1.0F) {
            float barH = Math.max(16.0F, body * panel.view / panel.content);
            float barY = (float)top + (body - barH) * (panel.scroll / Math.max(1.0F, panel.content - panel.view));
            this.rect(context, (float)(x + 166 - 7), barY + 2.0F, 2.5F, barH - 6.0F, 1, 1023410175);
         }
      }
   }

   private String fitCrisp(String value, String font, float em, float max) {
      if (CrispText.width(value, font, em, 0.0F) <= max) {
         return value;
      } else {
         while (value.length() > 1 && CrispText.width(value + "...", font, em, 0.0F) > max) {
            value = value.substring(0, value.length() - 1);
         }

         return value + "...";
      }
   }

   private void drawModule(class_332 context, Module module, int px, float sy, int scrollbar, int mouseX, int mouseY, int top, int bottom) {
      int x = px + 6;
      int w = 154 - scrollbar;
      int y = Math.round(sy);
      boolean over = mouseX >= x && mouseX < x + w && (float)mouseY >= Math.max(sy, (float)top) && (float)mouseY < Math.min(sy + 20.0F, (float)bottom);
      boolean overMore = over && mouseX >= x + w - 22;
      float hover = this.anim("module", module, over);
      boolean link = module instanceof Configs;
      float enabled = this.anim("enabled", module, link || module.isActive());
      float expanded = this.value("expand", module);
      float lit = Math.max(hover * 0.6F, expanded * 0.6F);
      float fill = Math.max(enabled, lit * 0.6F);
      if (fill > 0.01F && !link) {
         this.rect(context, (float)x, (float)y, (float)w, 20.0F, 3, withAlpha(16777215, Math.round(10.0F * lit)));
         if (enabled > 0.01F) {
            this.rect(context, (float)x, (float)y, (float)w, 20.0F, 3, withAlpha(ACCENT, Math.round(82.0F * enabled)));
            Smooth.outline(context, x, y, w, 20, 3, this.col(withAlpha(ACCENT, Math.round(140.0F * enabled))));
         }
      }

      float click = this.press(module);
      if (click > 0.0F) {
         this.rect(context, (float)x, (float)y, (float)w, 20.0F, 3, withAlpha(16777215, Math.round(255.0F * click * 0.12F)));
      }

      String font = enabled > 0.5F ? "montserrat-bold.ttf" : "montserrat-bold.ttf";
      int color = lerp(lerp(Theme.liquidGlass() ? -3091238 : -3946546, -1512722, hover), -1, enabled);
      String name = this.fitCrisp(Theme.label(module.name), font, 8.8F, (float)(w - (link ? 52 : 30)));
      CrispText.draw(context, name, font, (float)(x + 7), (float)y + 13.4F, 8.8F, 0.1F, this.col(color));
      float more = Math.max(overMore ? 1.0F : 0.0F, expanded);
      float dotX = (float)(x + w - 12);
      float dotY = (float)y + 10.0F;
      if (link) {
         float lw = CrispText.width("OPEN", "montserrat-bold.ttf", 8.0F, 0.4F);
         CrispText.draw(
            context,
            "OPEN",
            "montserrat-bold.ttf",
            (float)(x + w - 8) - lw,
            (float)y + 13.2F,
            8.0F,
            0.4F,
            this.col(lerp(-12877066, -8539905, Math.max(hover, this.configOpen ? 1.0F : 0.0F)))
         );
      } else if (more > 0.01F) {
         this.chevron(
            context,
            dotX,
            dotY,
            4.0F,
            (float)(1.5707963267948966 + 1.5707963267948966 * (double)expanded),
            withAlpha(lerp(MUTED, TEXT, expanded), Math.round(255.0F * more))
         );
      } else {
         float r = 1.8F + 0.4F * enabled;
         Smooth.softDot(context, dotX, dotY, r, this.col(lerp(1023410175, -1, enabled)));
      }

      this.clippedHit(DropdownScreen.Kind.MODULE, (float)x, sy, (float)w, 20.0F, module, 0, (float)top, (float)bottom);
      if (!link) {
         this.clippedHit(DropdownScreen.Kind.MORE, (float)(x + w - 22), sy, 22.0F, 20.0F, module, 0, (float)top, (float)bottom);
      }

      if (over) {
         this.hover(module, module.description);
      }

      if (expanded > 0.01F) {
         this.drawModuleSettings(context, module, px, sy + 21.0F, expanded, mouseX, mouseY, top, bottom);
      }
   }

   private void drawModuleSettings(class_332 context, Module module, int px, float sy, float expanded, int mouseX, int mouseY, int top, int bottom) {
      float full = this.settingsHeight(module);
      float area = full * expanded;
      int clipTop = Math.max(top, Math.round(sy));
      int clipBottom = Math.min(bottom, Math.round(sy + area));
      if (clipBottom > clipTop) {
         context.method_44379(px, clipTop, px + 166, clipBottom);
         this.rect(context, (float)(px + 6), sy, 154.0F, full, 4, 939524096);
         int sx = px + 6 + 4;
         int sw = 146;
         float y = sy + 4.0F;
         if (module.bindable()) {
            boolean over = mouseX >= sx
               && mouseX < sx + sw
               && (float)mouseY >= Math.max(y, (float)clipTop)
               && (float)mouseY < Math.min(y + 18.0F, (float)clipBottom);
            boolean active = this.binding == module;
            float h = this.anim("bind", module, over || active);
            this.rect(context, (float)sx, y, (float)sw, 18.0F, 4, lerp(CARD, CARD_HOVER, h));
            this.text(context, Theme.label("Keybind"), sx + 7, Math.round(y + 5.0F), MUTED);
            String value = this.fit(active ? "Press a key" : MenuKit.keyName(module.getKey()), sw - 70);
            int vw = this.textWidth(value) + 10;
            this.rect(context, (float)(sx + sw - 3 - vw), y + 3.0F, (float)vw, 12.0F, 3, active ? withAlpha(ACCENT, 85) : VALUE);
            this.text(context, value, sx + sw - 3 - vw + 5, Math.round(y + 5.0F), active ? TEXT : MUTED);
            this.clippedHit(DropdownScreen.Kind.BIND, (float)sx, y, (float)sw, 18.0F, module, 0, (float)clipTop, (float)clipBottom);
            if (over) {
               this.hover("bind" + module.name, "Click, then press a key. Escape or Delete removes the bind.");
            }

            y += 20.0F;
         }

         if (module instanceof Status status) {
            List<Status.Row> rows = status.statusRows();
            if (!rows.isEmpty()) {
               this.text(context, this.fit(status.statusTitle(), sw - 6), sx + 3, Math.round(y + 4.0F), MUTED);
               y += 16.0F;

               for (int i = 0; i < rows.size(); i++) {
                  Status.Row row = rows.get(i);
                  boolean over = mouseX >= sx
                     && mouseX < sx + sw
                     && (float)mouseY >= Math.max(y, (float)clipTop)
                     && (float)mouseY < Math.min(y + 28.0F, (float)clipBottom);
                  float h = this.anim("status", module.name + i, over);
                  this.rect(context, (float)sx, y, (float)sw, 28.0F, 4, lerp(CARD, CARD_HOVER, h));
                  float click = this.press("status" + i);
                  if (click > 0.0F) {
                     this.rect(context, (float)sx, y, (float)sw, 28.0F, 6, withAlpha(ACCENT, Math.round(255.0F * click * 0.22F)));
                  }

                  int bw = this.textWidth(row.badge()) + 10;
                  this.rect(context, (float)(sx + sw - 5 - bw), y + 7.0F, (float)bw, 14.0F, 7, row.badgeColor());
                  this.text(context, row.badge(), sx + sw - 5 - bw + 5, Math.round(y + 10.0F), -1);
                  this.text(context, this.fit(row.title(), sw - bw - 18), sx + 7, Math.round(y + 4.0F), TEXT);
                  this.text(context, this.fit(row.detail(), sw - bw - 18), sx + 7, Math.round(y + 16.0F), lerp(MUTED, ACCENT, h));
                  this.clippedHit(DropdownScreen.Kind.STATUS, (float)sx, y, (float)sw, 28.0F, row.link(), i, (float)clipTop, (float)clipBottom);
                  if (over) {
                     this.hover("status" + module.name + i, status.statusDescription());
                  }

                  y += 30.0F;
               }

               y += 3.0F;
            }
         }

         int presets = MenuKit.presetCount(module);
         if (presets > 0) {
            this.text(context, "PRESETS", sx + 3, Math.round(y + 4.0F), MUTED);
            y += 15.0F;

            for (int i = 0; i < presets; i++) {
               boolean active = MenuKit.presetActive(module, i);
               boolean overx = mouseX >= sx
                  && mouseX < sx + sw
                  && (float)mouseY >= Math.max(y, (float)clipTop)
                  && (float)mouseY < Math.min(y + 18.0F, (float)clipBottom);
               float hx = this.anim("preset", module.name + i, overx);
               this.rect(context, (float)sx, y, (float)sw, 18.0F, 4, active ? ACCENT : lerp(CARD, CARD_HOVER, hx));
               this.text(context, this.fit(MenuKit.presetName(module, i), sw - 16), sx + 7, Math.round(y + 5.0F), active ? -16182498 : TEXT);
               this.clippedHit(DropdownScreen.Kind.PRESET, (float)sx, y, (float)sw, 18.0F, module, i, (float)clipTop, (float)clipBottom);
               if (overx && module instanceof Presets p) {
                  this.hover("preset" + module.name + i, p.presetDescription());
               }

               y += 20.0F;
            }

            y += 3.0F;
         }

         String group = null;

         for (Setting<?> setting : visibleSettings(module)) {
            if (!setting.getGroup().equals(group)) {
               group = setting.getGroup();
               this.text(context, this.fit(group.toUpperCase(Locale.ROOT), sw - 8), sx + 3, Math.round(y + 3.0F), withAlpha(MUTED, 192));
               y += 13.0F;
            }

            float hx = this.settingHeight(setting);
            if (y + hx >= (float)clipTop && y <= (float)clipBottom) {
               this.drawSetting(context, setting, sx, y, sw, mouseX, mouseY, clipTop, clipBottom);
            }

            y += hx;
         }

         context.method_44380();
      }
   }

   private void drawSetting(class_332 context, Setting<?> setting, int sx, float sy, int sw, int mouseX, int mouseY, int top, int bottom) {
      float picker = setting instanceof ColorSetting color ? this.value("picker", color) : 0.0F;
      int base = setting instanceof IntSetting ? 26 : 18;
      int rh = base + Math.round(150.0F * picker);
      int y = Math.round(sy);
      boolean over = mouseX >= sx && mouseX < sx + sw && (float)mouseY >= Math.max(sy, (float)top) && (float)mouseY < Math.min(sy + (float)base, (float)bottom);
      float h = this.anim("setting", setting, over);
      this.rect(context, (float)sx, (float)y, (float)sw, (float)rh, 4, lerp(CARD, CARD_HOVER, h));
      float click = this.press(setting);
      if (click > 0.0F) {
         this.rect(context, (float)sx, (float)y, (float)sw, (float)base, 4, withAlpha(ACCENT, Math.round(255.0F * click * 0.2F)));
      }

      if (over) {
         this.hover(setting, setting.description);
      }

      if (setting instanceof BoolSetting bool) {
         this.text(context, this.fit(setting.name, sw - 40), sx + 7, y + 5, TEXT);
         float p = this.anim("bool", setting, bool.get());
         int tx = sx + sw - 26;
         float s = 1.0F - 0.06F * h;
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate((float)(tx + 10), (float)(y + 9));
         matrices.scale(s, s);
         matrices.translate((float)(-(tx + 10)), (float)(-(y + 9)));
         this.rect(context, (float)tx, (float)(y + 4), 20.0F, 10.0F, 5, BORDER);
         if (p > 0.0F) {
            context.method_44379(tx, y + 4, tx + Math.round(20.0F * p), y + 14);
            this.rect(context, (float)tx, (float)(y + 4), 20.0F, 10.0F, 5, ACCENT);
            context.method_44380();
         }

         this.rect(context, (float)(tx + 2) + 10.0F * p, (float)(y + 6), 6.0F, 6.0F, 3, -1);
         matrices.popMatrix();
      } else if (setting instanceof IntSetting integer) {
         String value = integer.display();
         int vw = this.textWidth(value) + 10;
         this.text(context, this.fit(setting.name, sw - 18 - vw), sx + 7, y + 5, TEXT);
         this.rect(context, (float)(sx + sw - 3 - vw), (float)(y + 3), (float)vw, 12.0F, 3, VALUE);
         this.text(context, value, sx + sw - 3 - vw + 5, y + 5, TEXT);
         int trackX = sx + 7;
         int trackW = sw - 14;
         this.rect(context, (float)trackX, (float)(y + 19), (float)trackW, 2.0F, 1, BORDER);
         float p = (float)(integer.get() - integer.min) / (float)Math.max(1, integer.max - integer.min);
         float shown = this.approach(this.animations.getOrDefault(new DropdownScreen.Key("slider", setting), p), p, 20.0F);
         this.animations.put(new DropdownScreen.Key("slider", setting), shown);
         if (shown > 0.0F) {
            this.rect(context, (float)trackX, (float)(y + 19), Math.max(2.0F, (float)trackW * shown), 2.0F, 1, ACCENT);
         }

         int knob = trackX + Math.round((float)(trackW - 8) * shown);
         this.rect(context, (float)(knob - 1), (float)(y + 15), 10.0F, 10.0F, 5, 855638016);
         this.rect(context, (float)knob, (float)(y + 16), 8.0F, 8.0F, 4, -1);
      } else {
         if (setting instanceof ColorSetting colorx) {
            boolean edit = this.editing == setting;
            boolean invalid = this.invalidSetting == setting && System.currentTimeMillis() < this.invalidUntil;
            String hex = edit ? this.editBuffer + "|" : colorx.display();
            int hw = Math.max(this.textWidth("#DDDDDD|"), this.textWidth(hex)) + 10;
            int hx = sx + sw - 21 - hw;
            this.text(context, this.fit(setting.name, sw - 30 - hw), sx + 7, y + 5, TEXT);
            this.rect(context, (float)hx, (float)(y + 3), (float)hw, 12.0F, 3, invalid ? -11525341 : VALUE);
            this.text(context, hex, hx + 5, y + 5, edit ? TEXT : MUTED);
            this.rect(context, (float)(sx + sw - 17), (float)(y + 2), 14.0F, 14.0F, 5, 1090519039);
            this.rect(context, (float)(sx + sw - 16), (float)(y + 3), 12.0F, 12.0F, 4, colorx.get().packed() | 0xFF000000);
            if (picker > 0.01F) {
               this.drawPicker(context, colorx, sx, y, sw, picker, top, bottom);
            }

            this.clippedHit(DropdownScreen.Kind.SETTING, (float)sx, sy, (float)sw, (float)base, setting, 0, (float)top, (float)bottom);
            this.clippedHit(DropdownScreen.Kind.HEX, (float)hx, sy + 2.0F, (float)hw, 14.0F, setting, 0, (float)top, (float)bottom);
            return;
         }

         boolean edit = this.editing == setting;
         boolean invalid = edit && System.currentTimeMillis() < this.invalidUntil;
         String value;
         if (setting instanceof BlockListSetting blocks) {
            int count = blocks.get().size();
            value = count == 0 ? "Pick" : count + " blocks";
         } else if (edit) {
            value = this.tail(this.editBuffer, sw / 2 - 16) + "|";
         } else if (this.keyBinding == setting) {
            value = "Press a key";
         } else {
            value = setting instanceof EnumSetting<?> e ? e.display() : setting.display();
         }

         boolean dropdown = setting instanceof EnumSetting;
         int extra = dropdown ? 10 : 0;
         int bw = edit ? sw / 2 : Math.max(30, Math.min(sw / 2 + extra, this.textWidth(value) + 12 + extra));
         int bx = sx + sw - 3 - bw;
         this.text(context, this.fit(setting.name, sw - bw - 14), sx + 7, y + 5, TEXT);
         this.rect(context, (float)bx, (float)(y + 3), (float)bw, 12.0F, 3, invalid ? -11525341 : VALUE);
         this.text(context, this.fit(value, bw - 10 - extra), bx + 6, y + 5, TEXT);
         if (dropdown) {
            this.chevron(context, (float)(bx + bw - 7), (float)(y + 9), 4.0F, 3.1415927F, MUTED);
         }
      }

      this.clippedHit(DropdownScreen.Kind.SETTING, (float)sx, sy, (float)sw, (float)base, setting, 0, (float)top, (float)bottom);
   }

   private void drawPicker(class_332 context, ColorSetting color, int sx, int y, int sw, float open, int top, int bottom) {
      float[] hsv = color.hsv();
      int wheel = 96;
      int wx = sx + (sw - wheel) / 2;
      int wy = y + 22;
      ColorWheel.draw(context, wx, wy, wheel, hsv[2], this.alpha);
      double angle = (double)hsv[0] * 3.141592653589793 * 2.0;
      double radius = (double)hsv[1] * ((double)wheel / 2.0 - 1.0);
      int px = wx + wheel / 2 + (int)Math.round(Math.cos(angle) * radius);
      int py = wy + wheel / 2 + (int)Math.round(Math.sin(angle) * radius);
      Smooth.circle(context, px, py, 4, this.col(-1));
      Smooth.circle(context, px, py, 3, this.col(0xFF000000 | Color.fromHsv(hsv[0], hsv[1], 1.0F, 255).packed()));
      if (open > 0.95F) {
         this.clippedHit(DropdownScreen.Kind.WHEEL, (float)wx, (float)wy, (float)wheel, (float)wheel, color, 0, (float)top, (float)bottom);
      }

      int side = sx + 8;
      int sideW = sw - 16;

      for (int channel = 0; channel < 2; channel++) {
         int barY = wy + wheel + 18 + channel * 24;
         int start = channel == 0 ? -16777216 : color.get().packed() & 16777215;
         int end = 0xFF000000 | (channel == 0 ? Color.fromHsv(hsv[0], hsv[1], 1.0F, 255).packed() : color.get().packed());
         this.text(context, this.fit(channel == 0 ? "Brightness" : "Alpha", sideW), side, barY - 10, MUTED);
         Smooth.horizontalGradient(context, side + 3, barY, side + sideW - 3, barY + 5, this.col(start), this.col(end));
         Smooth.rect(context, side, barY, 5, 5, 2, this.col(start));
         Smooth.rect(context, side + sideW - 5, barY, 5, 5, 2, this.col(end));
         float value = channel == 0 ? hsv[2] : (float)color.get().a() / 255.0F;
         Smooth.circle(context, side + Math.round((float)sideW * value), barY + 2, 5, this.col(1426063360));
         Smooth.circle(context, side + Math.round((float)sideW * value), barY + 2, 4, this.col(TEXT));
         if (open > 0.95F) {
            this.clippedHit(
               channel == 0 ? DropdownScreen.Kind.BRIGHTNESS : DropdownScreen.Kind.ALPHA,
               (float)(side - 4),
               (float)(barY - 4),
               (float)(sideW + 8),
               13.0F,
               color,
               channel,
               (float)top,
               (float)bottom
            );
         }
      }
   }

   private void pill(class_332 context, String label, int x, int y, int w, boolean active, int mouseX, int mouseY) {
      boolean over = inside((double)mouseX, (double)mouseY, (float)x, (float)y, (float)w, 22.0F);
      float h = this.anim("pill", label, over);
      this.zeroPanel(context, (float)x, (float)y, (float)w, 22.0F, 4, this.panel(PANEL), lerp(452984831, 1090519039, Math.max(h, active ? 1.0F : 0.0F)));
      if (h > 0.01F) {
         this.rect(context, (float)x, (float)y, (float)w, 22.0F, 4, withAlpha(16777215, Math.round(14.0F * h)));
      }

      if (active) {
         Smooth.outline(context, x, y, w, 22, 4, this.col(-1929379841));
      }

      float click = this.press("pill" + label);
      if (click > 0.0F) {
         this.rect(context, (float)x, (float)y, (float)w, 22.0F, 4, withAlpha(16777215, Math.round(255.0F * click * 0.14F)));
      }

      this.spaced(context, label, (float)(x + 13), (float)y + 11.0F + 3.0F, 8.0F, lerp(-922746881, -1, Math.max(h, active ? 1.0F : 0.0F)));
   }

   private void drawTopBar(class_332 context, int mouseX, int mouseY) {
      float y = this.topY();
      float left = this.barLeft();
      float total = this.barWidth();
      float lh = 38.0F;
      float lw = lh / LOGO.aspect();
      LOGO.draw(context, left - 4.0F, y + (34.0F - lh) / 2.0F, lw, this.col(-1));
      float wh = 21.0F;
      float ww = wh / WORDMARK.aspect();
      WORDMARK.draw(context, left + lw - 6.0F, y + (34.0F - wh) / 2.0F, ww, this.col(-1));
      String name = this.field_22787.method_1548() == null ? "Player" : this.field_22787.method_1548().method_1676();
      float chipW = Math.max(200.0F, 52.0F + CrispText.width(name, "montserrat-bold.ttf", 9.4F, 0.0F) + 20.0F);
      float chipX = left + total - chipW;
      this.zeroPanel(context, chipX, y, chipW, 34.0F, 5, this.panel(PANEL), 452984831);

      try {
         if (this.skin == null) {
            this.skin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
         }

         class_8685 textures = this.skin.get();
         SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
         if (textures != null && skins != null && skins.isActive()) {
            textures = skins.selfSkin(textures);
         }

         if (textures != null && this.alpha > 0.6F) {
            Avatars.draw(context, textures, Math.round(chipX + 8.0F), Math.round(y + 6.0F), 22);
         }
      } catch (RuntimeException var25) {
      }

      CrispText.draw(context, name, "montserrat-bold.ttf", chipX + 38.0F, y + 15.0F, 9.4F, 0.0F, this.col(-1));
      this.spaced(context, this.field_22787.field_1687 != null ? "IN GAME" : "IN MENU", chipX + 38.0F, y + 26.0F, 7.0F, -1593835521);
      float sx = Math.max(left + 166.0F + 12.0F + 120.0F, left + lw + ww + 16.0F);
      float sw = chipX - 28.0F - sx;
      boolean overSearch = inside((double)mouseX, (double)mouseY, sx, y, sw, 34.0F);
      float focus = this.anim("searchfocus", "search", this.searching);
      float lit = this.anim("searchhover", "search", overSearch);
      this.zeroPanel(context, sx, y, sw, 34.0F, 5, this.panel(PANEL), lerp(lerp(452984831, 822083583, lit), -1929379841, focus));
      int iconColor = this.col(lerp(-1426063361, -1, focus));
      int ix = Math.round(sx + 14.0F);
      int iy = Math.round(y + 17.0F);
      context.method_25294(ix, iy - 4, ix + 8, iy - 3, iconColor);
      context.method_25294(ix, iy, ix + 5, iy + 1, iconColor);
      context.method_25294(ix, iy + 4, ix + 5, iy + 5, iconColor);
      Smooth.ring(context, ix + 9, iy + 1, 3, iconColor);
      context.method_25294(ix + 11, iy + 4, ix + 13, iy + 5, iconColor);
      int found = 0;
      if (!this.search.isEmpty()) {
         for (DropdownScreen.Panel panel : ORDER) {
            found += this.modules(panel.category).size();
         }
      }

      String results = this.search.isEmpty() ? "" : found + (found == 1 ? " RESULT" : " RESULTS");
      float resultsW = results.isEmpty() ? 0.0F : CrispText.width(results, "montserrat-bold.ttf", 7.4F, 0.5F) + 14.0F;
      if (!results.isEmpty()) {
         this.spaced(context, results, sx + sw - 12.0F - resultsW + 14.0F, y + 17.0F + 2.8F, 7.4F, -1258291201);
      }

      this.searchText.sync(this.search, value -> CrispText.width(value, "montserrat-bold.ttf", 8.6F, 0.3F));
      this.searchText
         .draw(
            context,
            sx + 36.0F,
            y + 17.0F - 3.6F,
            sw - 52.0F - resultsW,
            8.0F,
            this.searching,
            "SEARCH MODULES",
            -1,
            -1090519041,
            -1,
            this.alpha,
            value -> CrispText.width(value, "montserrat-bold.ttf", 8.6F, 0.3F),
            (ctx, value, tx, ty, color) -> CrispText.draw(ctx, value, "montserrat-bold.ttf", tx, ty + 6.9F, 8.6F, 0.3F, this.col(color))
         );
      this.hit(DropdownScreen.Kind.SEARCH, sx, y, sw, 34.0F, null, 0);
   }

   private void drawBottomBar(class_332 context, int mouseX, int mouseY) {
      int y = Math.round(this.bottomY());
      int x = Math.round(693.0F - (float)this.bottomWidth() / 2.0F);
      String[] labels = this.bottomLabels();

      for (int i = 0; i < labels.length; i++) {
         String label = labels[i];
         int w = this.pillWidth(label);
         boolean active = i == 0 && this.configOpen;
         this.pill(context, label, x, y, w, active, mouseX, mouseY);
         boolean over = inside((double)mouseX, (double)mouseY, (float)x, (float)y, (float)w, 22.0F);
         switch (i) {
            case 2:
               this.hit(DropdownScreen.Kind.LAYOUT, (float)x, (float)y, (float)w, 22.0F, null, 0);
               if (over) {
                  this.hover("layout", "Opens the chat so you can drag, resize and reset your HUD cards.");
               }
               break;
            default:
               int index = i == 3 ? 2 : i;
               this.hit(DropdownScreen.Kind.BOTTOM, (float)x, (float)y, (float)w, 22.0F, BOTTOM[index], index);
               if (over) {
                  this.hover("bottom" + index, switch (index) {
                     case 0 -> "Opens your configs: save, activate, rename, delete, export and import.";
                     case 1 -> "Opens the HUD settings in the Client panel.";
                     default -> "Opens the menu settings: style, colors, fonts and sounds.";
                  });
               }
         }

         x += w + 6;
      }
   }

   private void reveal(Module module) {
      if (module != null) {
         DropdownScreen.Panel panel = PANELS.get(module.category);
         if (panel != null) {
            this.search = "";
            this.searching = false;
            panel.collapsed = false;
            this.front(panel);
            EXPANDED.add(module);
            float offset = 0.0F;

            for (Module other : this.modules(panel.category)) {
               if (other == module) {
                  break;
               }

               offset += this.moduleHeight(other);
            }

            panel.scrollTarget = offset;
            UiSounds.click();
            save();
         }
      }
   }

   private void drawConfigPopup(class_332 context, int mouseX, int mouseY) {
      this.popupOpen = this.approach(this.popupOpen, this.configOpen ? 1.0F : 0.0F, 14.0F);
      if (!(this.popupOpen < 0.01F)) {
         int x = Math.round(475.0F);
         int w = 436;
         int y = Math.round(this.bottomY() - 8.0F - 254.0F + (1.0F - this.popupOpen) * 12.0F);
         this.popupX = x;
         this.popupY = y;
         float previous = this.alpha;
         this.alpha = previous * this.popupOpen;
         boolean live = this.popupOpen > 0.5F;
         if (live) {
            this.hit(DropdownScreen.Kind.PANEL, (float)x, (float)y, (float)w, 254.0F, null, 0);
         }

         this.glass(context, x, y, w, 254, 10, this.panel(PANEL));
         this.rect(context, (float)(x + 9), (float)(y + 9), 22.0F, 22.0F, 7, CARD);
         this.icon(context, Category.CONFIG, x + 13, y + 13, 14);
         this.text(context, "CONFIGS", x + 38, y + 16, TEXT);
         this.configButton(context, "+ ADD CONFIG", -1, x + w - 196, y + 8, 106, 24, 6, mouseX, mouseY, live);
         this.configButton(context, "IMPORT", -1, x + w - 84, y + 8, 76, 24, 5, mouseX, mouseY, live);
         Configs configs = configs();
         int activeSlot = ConfigSlots.active();
         int rx = x + 9;
         int rw = w - 18;
         int ry = y + 42;
         int shown = 0;

         for (int i = 0; i < 5; i++) {
            if (!ConfigSlots.empty(i)) {
               shown++;
               boolean active = activeSlot == i;
               int buttons = rx + rw - 8 - 200 - 12;
               int nameMax = buttons - 12 - (rx + 13);
               boolean over = live && inside((double)mouseX, (double)mouseY, (float)rx, (float)ry, (float)rw, 34.0F);
               float h = this.anim("cslot", i, over);
               this.rect(context, (float)rx, (float)ry, (float)rw, 34.0F, 4, lerp(CARD, CARD_HOVER, h));
               if (active) {
                  this.rect(context, (float)rx, (float)ry, (float)rw, 34.0F, 4, withAlpha(ACCENT, 34));
                  this.rect(context, (float)(rx + 4), (float)(ry + 9), 3.0F, 16.0F, 1, ACCENT);
               }

               if (this.renaming && configs.slot() == i) {
                  int fw = Math.max(60, nameMax);
                  this.rect(context, (float)(rx + 9), (float)(ry + 4), (float)fw, 14.0F, 5, VALUE);
                  this.text(context, this.tail(this.renameBuffer, fw - 14) + "|", rx + 13, ry + 7, TEXT);
               } else {
                  int badge = this.textWidth("ACTIVE") + 12;
                  String name = this.fit(ConfigSlots.name(i), nameMax - (active ? badge + 6 : 0));
                  this.text(context, name, rx + 13, ry + 7, TEXT);
                  if (active) {
                     int bx = rx + 13 + this.textWidth(name) + 6;
                     this.rect(context, (float)bx, (float)(ry + 5), (float)badge, 12.0F, 6, ACCENT);
                     this.text(context, "ACTIVE", bx + 6, ry + 7, -16182498);
                  }
               }

               this.text(context, this.fit(ConfigSlots.enabledCount(i) + " modules  -  " + configs.status(i), nameMax), rx + 13, ry + 21, MUTED);
               if (live) {
                  this.hit(DropdownScreen.Kind.CONFIG_SLOT, (float)rx, (float)ry, (float)(buttons - rx - 4), 34.0F, null, i);
               }

               if (over && mouseX < buttons - 4) {
                  this.hover("cslot" + i, "Double click the name to rename this config.");
               }

               this.configButton(context, "LOAD", i, buttons, ry + 6, 50, 22, 1, mouseX, mouseY, live);
               this.configButton(context, "SAVE", i, buttons + 54, ry + 6, 50, 22, 0, mouseX, mouseY, live);
               this.configButton(context, "SHARE", i, buttons + 108, ry + 6, 50, 22, 2, mouseX, mouseY, live);
               this.configButton(context, "DELETE", i, buttons + 162, ry + 6, 50, 22, 4, mouseX, mouseY, live);
               ry += 38;
            }
         }

         if (shown == 0) {
            String title = "NO CONFIGS YET";
            this.text(context, title, x + (w - this.textWidth(title)) / 2, y + 110, TEXT);
            String hint = this.fit("Save your setup with + Add Config, or copy a code like SC-123456 and press Import.", w - 24);
            this.text(context, hint, x + (w - this.textWidth(hint)) / 2, y + 126, MUTED);
         }

         int fieldW = 196;
         this.drawSetting(context, configs.shareName, x + w - 9 - fieldW, (float)(y + 254 - 22), fieldW, mouseX, mouseY, y, y + 254);
         String footer = configs.busy() ? "Connecting..." : "Double click a name to rename it.";
         this.text(context, this.fit(footer, w - 32 - fieldW), x + 12, y + 254 - 16, MUTED);
         this.alpha = previous;
      }
   }

   private void configButton(class_332 context, String label, int slot, int bx, int by, int bw, int bh, int action, int mouseX, int mouseY, boolean live) {
      String key = action + ":" + slot;
      boolean over = live && inside((double)mouseX, (double)mouseY, (float)bx, (float)by, (float)bw, (float)bh);
      float h = this.anim("caction", key, over);
      boolean armed = action == 4 && configs().deleteArmed() && configs().slot() == slot;
      boolean primary = action == 6 || action == 1 && ConfigSlots.active() != slot;
      int fill = armed ? -7721158 : (primary ? ACCENT : lerp(CARD, CARD_HOVER, h));
      float s = 1.0F - 0.06F * h;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate((float)bx + (float)bw / 2.0F, (float)by + (float)bh / 2.0F);
      matrices.scale(s, s);
      matrices.translate(-((float)bx + (float)bw / 2.0F), -((float)by + (float)bh / 2.0F));
      this.rect(context, (float)bx, (float)by, (float)bw, (float)bh, 4, fill);
      if (action == 4 && !armed) {
         Smooth.outline(context, bx, by, bw, bh, 6, this.col(-1864346005));
      }

      if ((armed || primary) && h > 0.01F) {
         this.rect(context, (float)bx, (float)by, (float)bw, (float)bh, 6, withAlpha(16777215, Math.round(36.0F * h)));
      }

      float click = this.press("caction" + key);
      if (click > 0.0F) {
         this.rect(context, (float)bx, (float)by, (float)bw, (float)bh, 6, withAlpha(primary ? 16777215 : ACCENT, Math.round(255.0F * click * 0.22F)));
      }

      String shown = armed ? "SURE?" : label;
      this.text(context, shown, bx + (bw - this.textWidth(shown)) / 2, by + (bh - 8) / 2, primary ? -16182498 : TEXT);
      matrices.popMatrix();
      if (live) {
         this.hit(DropdownScreen.Kind.CONFIG_ACTION, (float)bx, (float)by, (float)bw, (float)bh, slot, action);
      }

      if (over) {
         this.hover("caction" + key, Configs.TIPS[action]);
      }
   }

   private void startRename(int slot) {
      configs().select(slot);
      this.renaming = true;
      this.renameBuffer = ConfigSlots.name(slot);
      this.searching = false;
   }

   private void commitRename() {
      this.renaming = false;
      configs().renameSlot(this.renameBuffer);
   }

   private void configAction(int action, Object target) {
      if (target instanceof Integer slot && slot >= 0) {
         configs().select(slot);
      }

      configs().run(action);
      SoberClient.CONFIG.save();
   }

   private void hover(Object key, String description) {
      if (key != this.hoveredKey && (this.hoveredKey == null || !this.hoveredKey.equals(key))) {
         this.hoveredKey = key;
         this.hoverStart = System.currentTimeMillis();
         if (key instanceof Module || key instanceof Category) {
            UiSounds.hover();
         }
      }

      this.tooltip = description;
   }

   private void drawTooltip(class_332 context, int mouseX, int mouseY) {
      if (this.tooltip == null) {
         this.hoveredKey = null;
      } else if (!this.tooltip.isEmpty() && this.dragging == null && this.draggingPanel == null && System.currentTimeMillis() - this.hoverStart >= 600L) {
         float visible = Math.min(1.0F, (float)(System.currentTimeMillis() - this.hoverStart - 600L) / 150.0F);
         float scale = this.renderScale;
         int localX = Math.round((float)mouseX / scale);
         int localY = Math.round((float)mouseY / scale);
         int localW = Math.round((float)this.field_22789 / scale);
         int localH = Math.round((float)this.field_22790 / scale);
         List<String> lines = this.wrap(this.tooltip, 190);
         int w = 0;

         for (String line : lines) {
            w = Math.max(w, this.textWidth(line));
         }

         int h = lines.size() * 11 + 9;
         int tx = Math.max(4, Math.min(localX + 12, localW - w - 18));
         int ty = Math.max(4, Math.min(localY + 14, localH - h - 6));
         float previous = this.alpha;
         this.alpha = previous * visible;
         context.method_51448().pushMatrix();
         context.method_51448().scale(scale, scale);
         this.rect(context, (float)tx, (float)ty, (float)(w + 14), (float)h, 6, -266263510);
         int ly = ty + 7;

         for (String line : lines) {
            this.text(context, line, tx + 7, ly, -2301722);
            ly += 11;
         }

         context.method_51448().popMatrix();
         this.alpha = previous;
      }
   }

   private List<String> wrap(String value, int maxWidth) {
      List<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : value.split(" ")) {
         String next = line.isEmpty() ? word : line + " " + word;
         if (this.textWidth(next) > maxWidth && !line.isEmpty()) {
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

   private void toggleCollapse(DropdownScreen.Panel panel) {
      panel.collapsed = !panel.collapsed;
      UiSounds.click();
      save();
   }

   private void toggleExpand(Module module) {
      if (!EXPANDED.remove(module)) {
         EXPANDED.add(module);
      }

      if (this.editing != null) {
         this.commitEdit();
      }

      this.binding = null;
      UiSounds.click();
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (!this.closing && !(this.openProgress < 0.8F)) {
         double mx = this.uiX(click.comp_4798());
         double my = this.uiY(click.comp_4799());
         int button = click.method_74245();
         DropdownScreen.Hit hit = this.hitAt(mx, my);
         if (this.editing != null && (hit == null || hit.target() != this.editing)) {
            this.commitEdit();
         }

         if (this.binding != null && (hit == null || hit.kind() != DropdownScreen.Kind.BIND)) {
            this.binding = null;
         }

         if (this.keyBinding != null && (hit == null || hit.target() != this.keyBinding)) {
            this.keyBinding = null;
         }

         if (hit == null || hit.kind() != DropdownScreen.Kind.SEARCH) {
            this.searching = false;
         }

         if (this.renaming && (hit == null || hit.kind() != DropdownScreen.Kind.CONFIG_SLOT)) {
            this.commitRename();
         }

         if (hit == null) {
            this.configOpen = false;
            return super.method_25402(click, doubled);
         } else {
            switch (hit.kind()) {
               case HEADER:
                  DropdownScreen.Panel panel = (DropdownScreen.Panel)hit.target();
                  this.front(panel);
                  if (button == 0) {
                     this.draggingPanel = panel;
                     this.grabX = (float)(mx - (double)panel.x);
                     this.grabY = (float)(my - (double)panel.y);
                     this.pressX = mx;
                     this.pressY = my;
                     this.panelMoved = false;
                  } else if (button == 1) {
                     this.toggleCollapse(panel);
                  }
                  break;
               case MODULE:
                  Module module = (Module)hit.target();
                  this.pressedAt.put(module, System.nanoTime());
                  if (module instanceof Configs && button == 0) {
                     this.configOpen = !this.configOpen;
                     UiSounds.click();
                  } else if (button == 1 || module instanceof ClickGui) {
                     this.toggleExpand(module);
                  } else if (button == 2) {
                     EXPANDED.add(module);
                     this.binding = module;
                     UiSounds.click();
                  } else if (button == 0) {
                     module.toggle();
                     UiSounds.moduleToggle(module.isActive());
                     SoberClient.CONFIG.save();
                  }
                  break;
               case SETTING:
                  this.clickSetting(hit, mx, button);
                  break;
               case HEX:
                  ColorSetting color = (ColorSetting)hit.target();
                  if (button == 1) {
                     color.reset();
                     this.editing = null;
                     SoberClient.CONFIG.save();
                  } else if (this.editing != color) {
                     this.editing = color;
                     this.editBuffer = color.display();
                  }

                  UiSounds.click();
                  break;
               case PRESET:
                  MenuKit.selectPreset((Module)hit.target(), hit.index());
                  UiSounds.click();
                  SoberClient.CONFIG.save();
                  break;
               case BIND:
                  this.binding = this.binding == hit.target() ? null : (Module)hit.target();
                  UiSounds.click();
                  break;
               case WHEEL:
               case BRIGHTNESS:
               case ALPHA:
                  this.dragging = hit;
                  this.updateDrag(hit, mx, my);
                  break;
               case SEARCH:
                  this.searching = true;
                  break;
               case LAYOUT:
                  UiSounds.click();
                  Hud hud = SoberClient.MODULES.get(Hud.class);
                  boolean editable = hud != null && hud.isActive() && hud.chatEditing.get();
                  this.field_22787.method_1507(new class_408("", false));
                  Notifications.push(
                     "HUD",
                     editable
                        ? "Drag your HUD cards to move them, scroll to resize, right click to reset the size."
                        : "Turn on the HUD and Chat Editing in the HUD settings to move your HUD cards.",
                     Notifications.Type.INFO
                  );
                  break;
               case BOTTOM:
                  this.pressedAt.put("pill" + hit.target(), System.nanoTime());
                  if (hit.index() == 0) {
                     this.configOpen = !this.configOpen;
                     UiSounds.click();
                  } else {
                     this.configOpen = false;
                     this.reveal((Module)(hit.index() == 1 ? SoberClient.MODULES.get(Hud.class) : options()));
                  }
                  break;
               case MORE:
                  this.pressedAt.put(hit.target(), System.nanoTime());
                  this.toggleExpand((Module)hit.target());
                  break;
               case CONFIGS:
                  this.configOpen = !this.configOpen;
                  UiSounds.click();
                  break;
               case CONFIG_SLOT:
                  if (doubled && !this.renaming) {
                     this.startRename(hit.index());
                     UiSounds.click();
                  }
                  break;
               case CONFIG_ACTION:
                  this.pressedAt.put("caction" + hit.index() + ":" + hit.target(), System.nanoTime());
                  UiSounds.click();
                  this.configAction(hit.index(), hit.target());
                  break;
               case STATUS:
                  this.pressedAt.put("status" + hit.index(), System.nanoTime());
                  UiSounds.click();
                  MenuKit.openLink((String)hit.target());
                  break;
               case PANEL:
                  if (hit.target() instanceof DropdownScreen.Panel panel) {
                     this.front(panel);
                  }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   private void clickSetting(DropdownScreen.Hit hit, double mx, int button) {
      Setting<?> setting = (Setting<?>)hit.target();
      this.pressedAt.put(setting, System.nanoTime());
      if (setting instanceof BoolSetting bool) {
         bool.toggle();
         UiSounds.toggle(bool.get());
      } else if (setting instanceof IntSetting) {
         this.dragging = hit;
         this.updateDrag(hit, mx, 0.0);
      } else if (setting instanceof EnumSetting<?> enumeration) {
         enumeration.cycle(button != 1);
         UiSounds.click();
      } else if (setting instanceof ColorSetting color) {
         if (!OPEN_COLORS.remove(color)) {
            OPEN_COLORS.add(color);
         }

         UiSounds.click();
      } else {
         if (setting instanceof BlockListSetting blocks) {
            UiSounds.click();
            this.field_22787.method_1507(new BlockPickerScreen(this, blocks));
            return;
         }

         if (setting instanceof ItemSetting item) {
            if (button == 1) {
               item.reset();
               this.editing = null;
            } else {
               this.editing = item;
               this.editBuffer = item.id();
            }
         } else if (setting instanceof KeySetting key) {
            if (button == 1) {
               key.bind(-1);
               this.keyBinding = null;
            } else {
               this.keyBinding = this.keyBinding == key ? null : key;
            }

            UiSounds.click();
         } else if (setting instanceof StringSetting string) {
            if (button == 1) {
               string.set("");
               this.editing = null;
            } else {
               this.editing = string;
               this.editBuffer = string.get();
            }
         }
      }

      SoberClient.CONFIG.save();
   }

   private void updateDrag(DropdownScreen.Hit hit, double mx, double my) {
      if (hit.target() instanceof IntSetting integer) {
         double trackX = (double)(hit.x() + 7.0F);
         double trackW = (double)(hit.w() - 14.0F);
         double progress = Math.max(0.0, Math.min(1.0, (mx - trackX - 4.0) / (trackW - 8.0)));
         integer.set(Integer.valueOf((int)Math.round((double)integer.min + progress * (double)(integer.max - integer.min))));
      } else if (hit.target() instanceof ColorSetting color) {
         if (hit.kind() == DropdownScreen.Kind.WHEEL) {
            float[] picked = ColorWheel.pick(mx, my, Math.round(hit.x()), Math.round(hit.y()), Math.round(hit.w()));
            float value = color.hsv()[2];
            color.setHsv(picked[0], picked[1], value < 0.05F ? 1.0F : value);
         } else {
            double progress = Math.max(0.0, Math.min(1.0, (mx - (double)(hit.x() + 4.0F)) / (double)(hit.w() - 8.0F)));
            if (hit.kind() == DropdownScreen.Kind.BRIGHTNESS) {
               float[] hsv = color.hsv();
               color.setHsv(hsv[0], hsv[1], (float)progress);
            } else {
               color.setAlpha((int)Math.round(progress * 255.0));
            }
         }
      }
   }

   public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
      double mx = this.uiX(click.comp_4798());
      double my = this.uiY(click.comp_4799());
      if (this.draggingPanel != null) {
         if (!this.panelMoved && Math.hypot(mx - this.pressX, my - this.pressY) < 3.0) {
            return true;
         } else {
            this.panelMoved = true;
            float nx = (float)(mx - (double)this.grabX);
            float ny = (float)(my - (double)this.grabY);
            if (!this.field_22787.method_74187()) {
               for (DropdownScreen.Panel other : ORDER) {
                  if (other != this.draggingPanel) {
                     if (Math.abs(ny - other.y) < 6.0F) {
                        ny = other.y;
                     }

                     if (Math.abs(nx - other.x) < 6.0F) {
                        nx = other.x;
                     }

                     if (Math.abs(nx - (other.x + 166.0F + 12.0F)) < 6.0F) {
                        nx = other.x + 166.0F + 12.0F;
                     }

                     if (Math.abs(nx + 166.0F + 12.0F - other.x) < 6.0F) {
                        nx = other.x - 166.0F - 12.0F;
                     }
                  }
               }
            }

            this.draggingPanel.x = nx;
            this.draggingPanel.y = ny;
            this.clampPanel(this.draggingPanel);
            return true;
         }
      } else if (this.dragging != null) {
         this.updateDrag(this.dragging, mx, my);
         return true;
      } else {
         return super.method_25403(click, offsetX, offsetY);
      }
   }

   public boolean method_25406(class_11909 click) {
      if (this.draggingPanel != null) {
         if (!this.panelMoved) {
            this.toggleCollapse(this.draggingPanel);
         }

         this.draggingPanel = null;
         save();
         return true;
      } else if (this.dragging != null) {
         this.dragging = null;
         SoberClient.CONFIG.save();
         return true;
      } else {
         return super.method_25406(click);
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.closing) {
         return true;
      } else {
         double mx = this.uiX(mouseX);
         double my = this.uiY(mouseY);
         int step = vertical > 0.0 ? 1 : -1;
         DropdownScreen.Hit hit = this.hitAt(mx, my);
         boolean ctrl = class_3675.method_15987(this.field_22787.method_22683(), 341);
         boolean shift = class_3675.method_15987(this.field_22787.method_22683(), 340);
         if (ctrl && hit != null && hit.target() instanceof IntSetting integer) {
            integer.set(Integer.valueOf(integer.get() + step * (shift ? 10 : 1)));
            return true;
         } else if (this.popupOpen > 0.5F && inside(mx, my, (float)this.popupX, (float)this.popupY, 436.0F, 254.0F)) {
            return true;
         } else {
            DropdownScreen.Panel panel = this.topPanel(mx, my);
            if (panel != null && !panel.collapsed && panel.content > panel.view) {
               panel.scrollTarget = Math.clamp(panel.scrollTarget - (float)(step * 21), 0.0F, Math.max(0.0F, panel.content - panel.view));
               UiSounds.key(0.3F);
            }

            return true;
         }
      }
   }

   private void commitEdit() {
      if (this.editing instanceof ItemSetting item) {
         if (!item.setFromId(this.editBuffer)) {
            this.invalidUntil = System.currentTimeMillis() + 600L;
            this.editing = null;
            return;
         }
      } else if (this.editing instanceof StringSetting string) {
         string.set(this.editBuffer.trim());
      } else if (this.editing instanceof ColorSetting color && !color.setHex(this.editBuffer)) {
         this.invalidSetting = color;
         this.invalidUntil = System.currentTimeMillis() + 600L;
      }

      this.editing = null;
      SoberClient.CONFIG.save();
   }

   public boolean method_25404(class_11908 input) {
      if (this.closing) {
         return true;
      } else {
         int key = input.comp_4795();
         if (this.binding != null) {
            this.binding.setKey(key != 256 && key != 259 && key != 261 ? key : -1);
            UiSounds.keybind(this.binding.getKey() >= 0);
            this.binding = null;
            this.swallowUntil = System.currentTimeMillis() + 150L;
            SoberClient.CONFIG.save();
            return true;
         } else if (this.keyBinding != null) {
            if (key != 256) {
               this.keyBinding.bind(key != 259 && key != 261 ? key : -1);
            }

            UiSounds.keybind(this.keyBinding.get() >= 0);
            this.keyBinding = null;
            this.swallowUntil = System.currentTimeMillis() + 150L;
            SoberClient.CONFIG.save();
            return true;
         } else if (this.renaming) {
            switch (key) {
               case 86:
                  if (input.method_75377()) {
                     String pasted = this.renameBuffer + this.field_22787.field_1774.method_1460().replace("\n", "").replace("\r", "");
                     this.renameBuffer = pasted.length() > 32 ? pasted.substring(0, 32) : pasted;
                  }
                  break;
               case 256:
                  this.renaming = false;
                  break;
               case 257:
               case 335:
                  this.commitRename();
                  break;
               case 259:
                  if (!this.renameBuffer.isEmpty()) {
                     this.renameBuffer = this.renameBuffer.substring(0, this.renameBuffer.length() - 1);
                  }
            }

            return true;
         } else if (this.editing != null) {
            switch (key) {
               case 86:
                  if (input.method_75377()) {
                     this.editBuffer = this.editBuffer + this.field_22787.field_1774.method_1460().replace("\n", "").replace("\r", "");
                  }
                  break;
               case 256:
                  this.commitEdit();
                  break;
               case 257:
               case 335:
                  this.commitEdit();
                  break;
               case 259:
                  if (!this.editBuffer.isEmpty()) {
                     this.editBuffer = this.editBuffer.substring(0, this.editBuffer.length() - 1);
                  }
            }

            return true;
         } else if (key == 259 && !this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
            this.searching = true;
            UiSounds.key(0.7F);
            return true;
         } else if (key != 256) {
            if (key == options().getKey() && this.search.isEmpty()) {
               this.method_25419();
               return true;
            } else {
               return super.method_25404(input);
            }
         } else {
            if (this.searching || !this.search.isEmpty()) {
               this.search = "";
               this.searching = false;
            } else if (this.configOpen) {
               this.configOpen = false;
               UiSounds.close();
            } else {
               this.method_25419();
            }

            return true;
         }
      }
   }

   public boolean method_25400(class_11905 input) {
      if (!this.closing && input.method_74227()) {
         if (System.currentTimeMillis() < this.swallowUntil) {
            this.swallowUntil = 0L;
            return true;
         } else if (this.renaming) {
            if (this.renameBuffer.length() < 32) {
               this.renameBuffer = this.renameBuffer + input.method_74226();
            }

            return true;
         } else if (this.editing != null) {
            String chars = input.method_74226();
            if (this.editing instanceof ColorSetting) {
               if (this.editBuffer.length() < 9 && chars.matches("[#0-9a-fA-FxX]")) {
                  this.editBuffer = this.editBuffer + chars.toUpperCase(Locale.ROOT);
               }
            } else if (this.editBuffer.length() < 100) {
               this.editBuffer = this.editBuffer + chars;
            }

            return true;
         } else {
            String typed = input.method_74226();
            if (this.search.isEmpty() && typed.isBlank()) {
               return true;
            } else {
               if (this.search.length() < 40) {
                  this.search = this.search + typed;
                  this.searching = true;
                  UiSounds.key(1.0F);

                  for (DropdownScreen.Panel panel : ORDER) {
                     panel.scrollTarget = 0.0F;
                  }
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }

   private static void load() {
      if (Files.exists(FILE)) {
         try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("version") || root.get("version").getAsInt() != 8) {
               return;
            }

            for (DropdownScreen.Panel panel : PANELS.values()) {
               if (root.has(panel.category.name())) {
                  JsonObject o = root.getAsJsonObject(panel.category.name());
                  panel.x = o.get("x").getAsFloat();
                  panel.y = o.get("y").getAsFloat();
                  panel.collapsed = o.has("collapsed") && o.get("collapsed").getAsBoolean();
               }
            }
         } catch (Exception var4) {
         }
      }
   }

   private static void save() {
      try {
         JsonObject root = new JsonObject();
         root.addProperty("version", 8);

         for (DropdownScreen.Panel panel : PANELS.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("x", Math.round(panel.x));
            o.addProperty("y", Math.round(panel.y));
            o.addProperty("collapsed", panel.collapsed);
            root.add(panel.category.name(), o);
         }

         Files.createDirectories(FILE.getParent());
         Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (Exception var4) {
      }
   }

   static {
      List<Category> categories = new ArrayList<>();

      for (Category category : Category.values()) {
         if (category != Category.CONFIG) {
            categories.add(category);
         }
      }

      Map<Category, Integer> sizes = new EnumMap<>(Category.class);

      for (Module module : SoberClient.MODULES.all()) {
         sizes.merge(module.category, 1, Integer::sum);
      }

      categories.sort(Comparator.<Category>comparingInt(categoryx -> -sizes.getOrDefault(categoryx, 0)).thenComparing(categoryx -> categoryx.title));
      categories.add(Category.CONFIG);
      float start = layoutLeft(categories.size());
      int index = 0;

      for (Category categoryx : categories) {
         DropdownScreen.Panel panel = new DropdownScreen.Panel(categoryx, start + (float)(index++ * 178), 76.0F);
         PANELS.put(categoryx, panel);
         ORDER.add(panel);
      }

      load();
   }

   private static record Hit(DropdownScreen.Kind kind, float x, float y, float w, float h, Object target, int index) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
      }
   }

   private static record Key(String tag, Object target) {
   }

   private static enum Kind {
      HEADER,
      MODULE,
      SETTING,
      HEX,
      PRESET,
      BIND,
      WHEEL,
      BRIGHTNESS,
      ALPHA,
      SEARCH,
      LAYOUT,
      BOTTOM,
      MORE,
      CONFIGS,
      CONFIG_SLOT,
      CONFIG_ACTION,
      STATUS,
      PANEL;
   }

   private static final class Panel {
      final Category category;
      float x;
      float y;
      boolean collapsed;
      float scroll;
      float scrollTarget;
      float content;
      float view;
      float height;

      Panel(Category category, float x, float y) {
         this.category = category;
         this.x = x;
         this.y = y;
      }
   }

   private static final class Timed {
      float from;
      float target;
      long start;
      long duration;
      boolean eased;

      Timed(float value) {
         this.from = this.target = value;
      }

      float at(long now) {
         long elapsed = now - this.start;
         if (this.duration > 0L && elapsed < this.duration) {
            float p = (float)elapsed / (float)this.duration;
            float k = this.eased ? (p < 0.5F ? 4.0F * p * p * p : 1.0F - (float)Math.pow((double)(-2.0F * p + 2.0F), 3.0) / 2.0F) : p;
            return this.from + (this.target - this.from) * k;
         } else {
            return this.target;
         }
      }
   }
}
