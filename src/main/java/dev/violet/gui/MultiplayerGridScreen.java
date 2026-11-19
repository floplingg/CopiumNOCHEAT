package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.mixin.ClickableWidgetAccessor;
import dev.violet.mixin.LanServerEntryAccessor;
import dev.violet.mixin.MultiplayerScreenAccessor;
import dev.violet.mixin.ServerEntryAccessor;
import dev.violet.modules.MainMenu;
import dev.violet.modules.NameTags;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.class_10799;
import net.minecraft.class_1131;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_642;
import net.minecraft.class_7919;
import net.minecraft.class_8573;
import net.minecraft.class_8685;
import net.minecraft.class_4267.class_4269;
import net.minecraft.class_4267.class_4270;
import net.minecraft.class_4267.class_504;
import net.minecraft.class_642.class_9083;
import org.joml.Matrix3x2fStack;

public class MultiplayerGridScreen extends class_500 {
   private static final float WIDTH = 860.0F;
   private static final float HEIGHT = 440.0F;
   private static final float GRID_X = 54.0F;
   private static final float GRID_Y = 124.0F;
   private static final float GRID_W = 752.0F;
   private static final float CARD_H = 74.0F;
   private static final float GAP = 10.0F;
   private static final float ROW = 84.0F;
   private static final float GRID_BOTTOM = 380.0F;
   private static final float DOCK_Y = 392.0F;
   private static final float DOCK_H = 38.0F;
   private static final float SEARCH_X = 552.0F;
   private static final float SEARCH_Y = 72.0F;
   private static final float SEARCH_W = 192.0F;
   private static final float FIELD_H = 26.0F;
   private static final float REFRESH_X = 752.0F;
   private static final int TEXT = -855051;
   private static final int MUTED = -4275510;
   private static final int FAINT = -7367779;
   private static final int CARD = 352321535;
   private static final int CARD_HOVER = 587202559;
   private static final int EDGE = 536870911;
   private static final class_2960 UNKNOWN = class_2960.method_60656("textures/misc/unknown_server.png");
   private static final String[] DOCK = new String[]{
      "selectServer.select", "selectServer.direct", "selectServer.add", "selectServer.edit", "selectServer.delete", "selectServer.refresh", "gui.back"
   };
   private static final float[] DOCK_W = new float[]{100.0F, 124.0F, 96.0F, 64.0F, 70.0F, 74.0F, 64.0F};
   private static final String ADD = "add";
   private static final String DIRECT = "direct";
   private static final String SEARCH = "search";
   private static final String REFRESH = "refresh";
   private static final ExactImage LOGO = new ExactImage("textures/brand/lockup.png");
   private static final float COL_W = 440.0F;
   private static final float COL_X = 210.0F;
   private static final float HEAD_Y = 40.0F;
   private static final float LIST_Y = 96.0F;
   private static final float LIST_BOTTOM = 350.0F;
   private static final float ROW_H = 38.0F;
   private static final float ROW_STEP = 44.0F;
   private static final float ACTION_H = 30.0F;
   private static final float LSEARCH_W = 170.0F;
   private static final float LSEARCH_X = 448.0F;
   private static final float LREFRESH_X = 624.0F;
   private static final int CREAM = -723723;
   private static final int CREAM_MUTED = -4934468;
   private static final int CREAM_FAINT = -7434602;
   private static final int INK = -15856112;
   private static final String[] ACTIONS = new String[]{"selectServer.select", "selectServer.add", "selectServer.direct", "gui.back"};
   private final Map<String, class_4185> buttons = new HashMap<>();
   private final Set<class_364> vanilla = Collections.newSetFromMap(new IdentityHashMap<>());
   private final Map<Object, Float> hover = new IdentityHashMap<>();
   private final Map<Object, Float> chosen = new IdentityHashMap<>();
   private final List<MultiplayerGridScreen.Card> cards = new ArrayList<>();
   private final float[] dividers = new float[2];
   private class_342 search;
   private Supplier<class_8685> skin;
   private float scale = 1.0F;
   private float originX;
   private float originY;
   private float dockX;
   private float dockW;
   private float scroll;
   private float scrollTarget;
   private float content;
   private float delta;
   private float fade = 1.0F;
   private float colShift;
   private float listView = 254.0F;
   private long openedAt;
   private long lastFrame;
   private boolean arranged;
   private List<class_2561> tooltip;
   private List<class_5481> orderedTooltip;
   private final List<class_339> foreign = new ArrayList<>();
   private int tooltipX;
   private int tooltipY;

   public MultiplayerGridScreen(class_437 parent) {
      super(parent);
   }

   public static class_437 replace(class_500 screen) {
      return new MultiplayerGridScreen(((MultiplayerScreenAccessor)screen).violet$parent());
   }

   protected void method_25426() {
      super.method_25426();
      if (this.openedAt == 0L) {
         this.openedAt = System.nanoTime();
      }

      this.vanilla.clear();
      this.buttons.clear();
      this.vanilla.add(this.field_3043);
      this.method_37066(this.field_3043);

      for (class_364 element : List.copyOf(this.method_25396())) {
         if (element instanceof class_339) {
            class_339 widget = (class_339)element;
            String key = key(widget.method_25369());
            if (key.equals("multiplayer.title")) {
               widget.field_22764 = false;
               this.vanilla.add(widget);
            } else {
               for (String dock : DOCK) {
                  if (dock.equals(key) && widget instanceof class_4185 button) {
                     this.buttons.put(key, button);
                     this.vanilla.add(widget);
                  }
               }
            }
         }
      }

      class_4185 join = this.buttons.get(DOCK[0]);
      if (join != null) {
         ScreenStyle.primary(join);
      }

      for (int i = 1; i < DOCK.length; i++) {
         class_4185 button = this.buttons.get(DOCK[i]);
         if (button != null) {
            ScreenStyle.flat(button);
         }
      }

      this.search = new class_342(this.field_22793, 0, 0, 100, 10, class_2561.method_43470("Search servers"));
      this.search.method_1858(false);
      this.search.method_71503(false);
      this.search.method_1868(-855051);
      this.search.method_1880(48);
      this.search
         .method_47404(
            class_2561.method_43470(violetStyle() ? "Search servers" : "Search servers...")
               .method_10862(Theme.crispStyle().method_36139(violetStyle() ? 9342614 : 7040888))
         );
      this.search.method_73210(Theme.fieldFormatter());
      this.search.method_1863(value -> this.scrollTarget = 0.0F);
      this.method_37063(this.search);
      this.vanilla.add(this.search);
      this.skin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
      this.arranged = false;
      this.layout();
   }

   protected void method_48640() {
      super.method_48640();
      if (this.search != null) {
         this.layout();
         this.arranged = false;
      }
   }

   private void layout() {
      this.scale = Math.max(0.45F, Math.min((float)this.field_22789 / 860.0F, (float)this.field_22790 / 440.0F));
      this.originX = (float)Math.round(((float)this.field_22789 - 860.0F * this.scale) / 2.0F);
      this.originY = (float)Math.round(((float)this.field_22790 - 440.0F * this.scale) / 2.0F);
      if (listMode()) {
         this.search.method_55444(Math.round(130.0F * this.scale), 10, this.px(476.0F), this.py(40.0F) + Math.round((26.0F * this.scale - 9.0F) / 2.0F));

         for (class_4185 button : this.buttons.values()) {
            button.field_22764 = false;
         }
      } else {
         this.search.method_55444(Math.round(158.0F * this.scale), 10, this.px(578.0F), this.py(72.0F) + Math.round((26.0F * this.scale - 9.0F) / 2.0F));
         if (violetStyle()) {
            for (class_4185 button : this.buttons.values()) {
               button.field_22764 = false;
            }
         } else {
            float total = 32.0F;

            for (float w : DOCK_W) {
               total += w;
            }

            total += (float)(DOCK.length - 1) * 6.0F;
            this.dockW = total;
            this.dockX = (860.0F - total) / 2.0F;
            float x = this.dockX + 8.0F;
            int divider = 0;

            for (int i = 0; i < DOCK.length; i++) {
               if (i == 3 || i == 5) {
                  this.dividers[divider++] = x + 1.0F;
                  x += 8.0F;
               }

               class_4185 button = this.buttons.get(DOCK[i]);
               if (button != null) {
                  button.method_55444(Math.round(DOCK_W[i] * this.scale), Math.round(26.0F * this.scale), this.px(x), this.py(398.0F));
               }

               x += DOCK_W[i] + 6.0F;
            }
         }
      }
   }

   private void arrange() {
      this.foreign.clear();

      for (class_364 element : this.method_25396()) {
         if (element instanceof class_339) {
            class_339 widget = (class_339)element;
            if (!this.vanilla.contains(widget) && widget.field_22764 && widget.method_46427() < this.py(52.0F)) {
               this.foreign.add(widget);
            }
         }
      }

      this.foreign.sort(Comparator.comparingInt(class_339::method_46426));

      for (class_339 widget : this.foreign) {
         widget.field_22764 = false;
      }
   }

   private List<float[]> foreignBoxes() {
      List<float[]> boxes = new ArrayList<>();
      float right = !vanillaPlus() && !listMode() ? this.clockLeft() - 10.0F : 836.0F;

      for (int i = this.foreign.size() - 1; i >= 0; i--) {
         String label = plain(this.foreign.get(i).method_25369());
         float w = Math.max(40.0F, this.textWidth(label, 1.0F) + 22.0F);
         right -= w;
         boxes.add(0, new float[]{right, 13.0F, w, 24.0F});
         right -= 6.0F;
      }

      return boxes;
   }

   private static String plain(class_2561 text) {
      StringBuilder out = new StringBuilder();
      if (text != null) {
         for (ScreenStyle.Run run : ScreenStyle.runs(text, -1)) {
            out.append(run.text());
         }
      }

      return out.toString();
   }

   private void foreignButtons(class_332 context, double mx, double my, int mouseX, int mouseY) {
      List<float[]> boxes = this.foreignBoxes();
      int accent = accent();

      for (int i = 0; i < this.foreign.size(); i++) {
         class_339 widget = this.foreign.get(i);
         float[] b = boxes.get(i);
         boolean over = inside(mx, my, b[0], b[1], b[2], b[3]);
         float lit = this.animate(this.hover, widget, over && widget.field_22763, 12.0F);
         if (!this.glass(context, b[0], b[1], b[2], b[3], 12)) {
            this.rect(context, b[0], b[1], b[2], b[3], 12, lerp(352321535, 587202559, lit));
            this.outline(context, b[0], b[1], b[2], b[3], 12, 536870911);
         }

         this.neon(context, b[0], b[1], b[2], b[3], 12, accent, lit);
         float cx = b[0] + (b[2] - this.textWidth(plain(widget.method_25369()), 1.0F)) / 2.0F;

         for (ScreenStyle.Run run : ScreenStyle.runs(widget.method_25369(), widget.field_22763 ? -855051 : -4275510)) {
            this.text(context, run.text(), cx, b[1] + 8.0F, 1.0F, run.color());
            cx += this.textWidth(run.text(), 1.0F);
         }

         if (over && widget instanceof ClickableWidgetAccessor access && access.violet$tooltip() != null) {
            class_7919 tip = access.violet$tooltip().method_56137();
            if (tip != null) {
               this.orderedTooltip = tip.method_47405(this.field_22787);
               this.tooltipX = mouseX;
               this.tooltipY = mouseY;
            }
         }
      }
   }

   private boolean clickForeign(class_11909 click, double mx, double my, boolean doubled) {
      List<float[]> boxes = this.foreignBoxes();

      for (int i = 0; i < this.foreign.size(); i++) {
         float[] b = boxes.get(i);
         if (inside(mx, my, b[0], b[1], b[2], b[3])) {
            class_339 widget = this.foreign.get(i);
            widget.field_22764 = true;

            try {
               widget.method_25402(
                  new class_11909(
                     (double)widget.method_46426() + (double)widget.method_25368() / 2.0,
                     (double)widget.method_46427() + (double)widget.method_25364() / 2.0,
                     click.comp_4800()
                  ),
                  doubled
               );
            } finally {
               if (this.field_22787.field_1755 == this) {
                  widget.field_22764 = false;
               }
            }

            return true;
         }
      }

      return false;
   }

   private static String key(class_2561 text) {
      return text != null && text.method_10851() instanceof class_2588 translatable ? translatable.method_11022() : "";
   }

   private int px(float x) {
      return Math.round(this.originX + x * this.scale);
   }

   private int py(float y) {
      return Math.round(this.originY + y * this.scale);
   }

   private static boolean violetStyle() {
      return ScreenStyle.violetStyle();
   }

   private static boolean listMode() {
      MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
      return violetStyle() && menu != null && menu.serverLayout.get() == MainMenu.ServerLayout.List;
   }

   private float listTop() {
      return listMode() ? 96.0F + this.colShift : 124.0F;
   }

   private float listBottom() {
      return listMode() ? 96.0F + this.colShift + this.listView : 380.0F;
   }

   private float headY() {
      return 40.0F + this.colShift;
   }

   private float actionY() {
      return this.listBottom() + 14.0F;
   }

   private float[] chipBox() {
      float w = Math.max(130.0F, 34.0F + CrispText.width(this.shownName(), "montserrat-bold.ttf", 9.0F, 0.0F) + 30.0F);
      return new float[]{806.0F - w, 9.0F, w, 32.0F};
   }

   private void violetPanel(class_332 context, float x, float y, float w, float h, int r, float hover, float selected) {
      ScreenStyle.violetPanel(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, hover, selected, this.fade);
   }

   private void spaced(class_332 context, String value, float x, float baseline, float em, int color) {
      CrispText.draw(context, value, "montserrat-bold.ttf", x, baseline, em, 0.5F, this.f(color));
   }

   private static int accent() {
      if (violetStyle()) {
         return -1513234;
      } else {
         MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
         return menu != null && menu.style.get() == MainMenu.Style.VanillaPlus ? -1184014 : 0xFF000000 | (menu != null ? menu.color() : Theme.accent());
      }
   }

   private static boolean vanillaPlus() {
      MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
      return menu != null && menu.style.get() == MainMenu.Style.VanillaPlus;
   }

   private void base(class_332 context, float x, float y, float w, float h, int r) {
      if (vanillaPlus()) {
         this.rect(context, x, y, w, h, r, 1879048192);
      }
   }

   private static int alpha(int color, float alpha) {
      return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | color & 16777215;
   }

   private int f(int color) {
      return alpha(color, (float)(color >>> 24) / 255.0F * this.fade);
   }

   private static int lerp(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
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

   private static float quint(float p) {
      float inverse = 1.0F - Math.max(0.0F, Math.min(1.0F, p));
      return 1.0F - inverse * inverse * inverse * inverse * inverse;
   }

   private float animate(Map<Object, Float> map, Object key, boolean on, float speed) {
      float value = map.getOrDefault(key, 0.0F);
      value += ((on ? 1.0F : 0.0F) - value) * (1.0F - (float)Math.exp((double)(-speed * this.delta)));
      if (Math.abs((on ? 1.0F : 0.0F) - value) < 0.002F) {
         value = on ? 1.0F : 0.0F;
      }

      map.put(key, value);
      return value;
   }

   private static boolean inside(double mx, double my, float x, float y, float w, float h) {
      return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
   }

   private class_2561 styled(String value, float size) {
      return size >= 1.6F ? Theme.large(value) : (size >= 1.1F ? Theme.bold(value) : Theme.text(value));
   }

   private void text(class_332 context, String value, float x, float y, float size, int color) {
      if (!ScreenStyle.formatted(value)) {
         String file = Theme.crispFile(size >= 1.1F);
         if (!CrispText.canDisplay(value, file)) {
            this.text(context, this.styled(value, size), x, y, size, color);
         } else {
            int shown = this.f(ScreenStyle.readable(color));
            float em = 9.4F * size;
            float baseline = y + 7.6F * size;
            if (Theme.liquidGlass()) {
               CrispText.draw(context, value, file, x + 0.5F, baseline + 0.7F, em, 0.0F, alpha(0, (float)(shown >>> 24) / 255.0F * 0.3F));
            }

            CrispText.draw(context, value, file, x, baseline, em, 0.0F, shown);
         }
      } else {
         float cx = x;

         for (ScreenStyle.Run run : ScreenStyle.runs(class_5348.method_29430(value), color)) {
            this.text(context, run.text(), cx, y, size, run.color());
            cx += this.textWidth(run.text(), size);
         }
      }
   }

   private List<List<MultiplayerGridScreen.Run>> runs(class_2561 value, int fallback) {
      List<List<MultiplayerGridScreen.Run>> lines = new ArrayList<>();
      lines.add(new ArrayList<>());
      if (value == null) {
         return lines;
      } else {
         value.method_27658((style, string) -> {
            int color = style.method_10973() != null ? 0xFF000000 | style.method_10973().method_27716() : fallback;
            String[] parts = string.split("\n", -1);

            for (int i = 0; i < parts.length; i++) {
               if (i > 0) {
                  lines.add(new ArrayList<>());
               }

               if (!parts[i].isEmpty()) {
                  lines.get(lines.size() - 1).add(new MultiplayerGridScreen.Run(parts[i], color));
               }
            }

            return Optional.empty();
         }, class_2583.field_24360);
         return lines;
      }
   }

   private float runsWidth(List<MultiplayerGridScreen.Run> line, float size) {
      float width = 0.0F;

      for (MultiplayerGridScreen.Run run : line) {
         width += this.textWidth(run.text(), size);
      }

      return width;
   }

   private void drawRuns(class_332 context, List<MultiplayerGridScreen.Run> line, float x, float y, float size, float maxWidth) {
      float cx = x;
      float limit = x + maxWidth;

      for (MultiplayerGridScreen.Run run : line) {
         String value = run.text();
         float width = this.textWidth(value, size);
         if (cx + width > limit) {
            String cut = value;

            while (!cut.isEmpty() && cx + this.textWidth(cut + "...", size) > limit) {
               cut = cut.substring(0, cut.length() - 1);
            }

            this.text(context, cut.stripTrailing() + "...", cx, y, size, run.color());
            return;
         }

         this.text(context, value, cx, y, size, run.color());
         cx += width;
      }
   }

   private List<List<MultiplayerGridScreen.Run>> wrap(List<List<MultiplayerGridScreen.Run>> lines, float size, float maxWidth) {
      if (lines.size() == 1 && !(this.runsWidth(lines.get(0), size) <= maxWidth)) {
         List<MultiplayerGridScreen.Run> first = new ArrayList<>();
         List<MultiplayerGridScreen.Run> second = new ArrayList<>();
         float used = 0.0F;
         boolean full = false;

         for (MultiplayerGridScreen.Run run : lines.get(0)) {
            for (String word : run.text().split("(?<= )")) {
               float width = this.textWidth(word, size);
               if (!full && used + width <= maxWidth) {
                  first.add(new MultiplayerGridScreen.Run(word, run.color()));
                  used += width;
               } else {
                  full = true;
                  second.add(new MultiplayerGridScreen.Run(second.isEmpty() ? word.stripLeading() : word, run.color()));
               }
            }
         }

         return List.of(first, second);
      } else {
         return lines;
      }
   }

   private void text(class_332 context, class_2561 value, float x, float y, float size, int color) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(x, y);
      matrices.scale(size, size);
      context.method_51439(this.field_22793, value, 0, 0, this.f(ScreenStyle.readable(color)), Theme.liquidGlass());
      matrices.popMatrix();
   }

   private boolean glass(class_332 context, float x, float y, float w, float h, int r) {
      return vanillaPlus() ? false : ScreenStyle.glass(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.fade);
   }

   private void neon(class_332 context, float x, float y, float w, float h, int r, int color, float strength) {
      if (!vanillaPlus()) {
         ScreenStyle.neon(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, color, strength * this.fade);
      }
   }

   private void topLight(class_332 context, float x, float y, float w, int r, float strength) {
      if (!vanillaPlus()) {
         ScreenStyle.topLight(context, Math.round(x), Math.round(y), Math.round(w), r, strength * this.fade);
      }
   }

   private float textWidth(String value, float size) {
      if (!ScreenStyle.formatted(value)) {
         String file = Theme.crispFile(size >= 1.1F);
         return !CrispText.canDisplay(value, file)
            ? (float)this.field_22793.method_27525(this.styled(value, size)) * size
            : CrispText.width(value, file, 9.4F * size, 0.0F);
      } else {
         float width = 0.0F;

         for (ScreenStyle.Run run : ScreenStyle.runs(class_5348.method_29430(value), -1)) {
            width += this.textWidth(run.text(), size);
         }

         return width;
      }
   }

   private float textWidth(class_2561 value, float size) {
      return (float)this.field_22793.method_27525(value) * size;
   }

   private String fit(String value, float max, float size) {
      if (value == null) {
         return "";
      } else if (this.textWidth(value, size) <= max) {
         return value;
      } else {
         String cut = value;

         while (cut.length() > 1 && this.textWidth(cut + "...", size) > max) {
            cut = cut.substring(0, cut.length() - 1);
         }

         return cut + "...";
      }
   }

   private void rect(class_332 context, float x, float y, float w, float h, int r, int color) {
      Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), vanillaPlus() && r > 4 ? 3 : r, this.f(color));
   }

   private void outline(class_332 context, float x, float y, float w, float h, int r, int color) {
      Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), vanillaPlus() && r > 4 ? 3 : r, this.f(color));
   }

   private String shownName() {
      String custom = NameTags.customName().trim();
      if (!custom.isEmpty()) {
         return custom;
      } else {
         return this.field_22787.method_1548() == null ? "Player" : this.field_22787.method_1548().method_1676();
      }
   }

   private class_8685 worn(class_8685 original) {
      return original;
   }

   private static String clock() {
      LocalTime local = LocalTime.now();
      return String.format("%02d:%02d", local.getHour(), local.getMinute());
   }

   private float clockLeft() {
      if (violetStyle()) {
         return this.chipBox()[0] - 12.0F - CrispText.width(clock(), "montserrat-bold.ttf", 9.0F, 0.0F);
      } else {
         int pill = Math.round(30.0F + this.textWidth(this.shownName(), 1.0F));
         return (float)(830 - pill - 12) - this.textWidth(clock(), 1.0F);
      }
   }

   private void group(class_332 context, float time, float delay, Runnable draw) {
      float in = quint((time - delay) / 0.6F);
      if (!(in <= 0.002F)) {
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(0.0F, 10.0F * (1.0F - in));
         this.fade = in;

         try {
            draw.run();
         } finally {
            this.fade = 1.0F;
            matrices.popMatrix();
         }
      }
   }

   private void poll(class_332 context, float deltaTicks) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(-100000.0F, -100000.0F);

      try {
         for (class_504 entry : this.field_3043.method_25396()) {
            if (entry instanceof class_4270 server) {
               server.method_25343(context, -1, -1, false, deltaTicks);
            }
         }
      } catch (RuntimeException var10) {
         SoberClient.LOG.debug("Server entry update failed", var10);
      } finally {
         matrices.popMatrix();
      }
   }

   private void buildCards() {
      this.cards.clear();
      String query = this.search == null ? "" : this.search.method_1882().trim().toLowerCase(Locale.ROOT);
      List<Object> items = new ArrayList<>();

      for (class_504 entry : this.field_3043.method_25396()) {
         if (entry instanceof class_4270) {
            class_4270 server = (class_4270)entry;
            class_642 info = server.method_20133();
            if (query.isEmpty() || info.field_3752.toLowerCase(Locale.ROOT).contains(query) || info.field_3761.toLowerCase(Locale.ROOT).contains(query)) {
               items.add(server);
            }
         } else if (entry instanceof class_4269) {
            class_4269 lan = (class_4269)entry;
            if (lan instanceof LanServerEntryAccessor) {
               LanServerEntryAccessor access = (LanServerEntryAccessor)lan;
               class_1131 info = access.violet$server();
               if (query.isEmpty() || info.method_4813().toLowerCase(Locale.ROOT).contains(query) || info.method_4812().contains(query)) {
                  items.add(lan);
               }
            }
         }
      }

      if (listMode()) {
         this.content = items.isEmpty() ? 0.0F : (float)items.size() * 44.0F - 6.0F;
         this.listView = Math.min(254.0F, Math.max(this.content, 82.0F));
         this.colShift = (float)Math.round((254.0F - this.listView) / 2.0F);

         for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            this.cards.add(new MultiplayerGridScreen.Card(item, item instanceof class_4269 ? 1 : 0, 210.0F, this.listTop() + (float)i * 44.0F, 440.0F, 38.0F));
         }

         this.scrollTarget = this.clampScroll(this.scrollTarget);
         this.search.method_46419(this.py(this.headY()) + Math.round((26.0F * this.scale - 9.0F) / 2.0F));
      } else {
         if (query.isEmpty()) {
            items.add("add");
            items.add("direct");
         }

         float cw = 244.0F;

         for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            int kind = item == "add" ? 2 : (item == "direct" ? 3 : (item instanceof class_4269 ? 1 : 0));
            this.cards.add(new MultiplayerGridScreen.Card(item, kind, 54.0F + (float)(i % 3) * (cw + 10.0F), 124.0F + (float)(i / 3) * 84.0F, cw, 74.0F));
         }

         int rows = (items.size() + 2) / 3;
         this.content = rows <= 0 ? 0.0F : (float)rows * 84.0F - 10.0F;
         this.scrollTarget = this.clampScroll(this.scrollTarget);
      }
   }

   private float clampScroll(float value) {
      return Math.max(0.0F, Math.min(value, Math.max(0.0F, this.content - (this.listBottom() - this.listTop()))));
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      try {
         this.paint(context, mouseX, mouseY, deltaTicks);
      } catch (RuntimeException var9) {
         SoberClient.LOG.error("Multiplayer screen render failed, using the vanilla screen", var9);
         class_437 parent = (class_437)(this instanceof MultiplayerScreenAccessor access ? access.violet$parent() : new class_442());
         this.field_22787.method_1507(new class_500(parent) {
         });
      }
   }

   private void paint(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      long nano = System.nanoTime();
      this.delta = this.lastFrame == 0L ? 0.0F : Math.min(0.05F, (float)(nano - this.lastFrame) / 1.0E9F);
      this.lastFrame = nano;
      if (!this.arranged) {
         this.arranged = true;
         this.arrange();
      }

      this.poll(context, deltaTicks);
      this.buildCards();
      this.scroll = this.scroll + (this.scrollTarget - this.scroll) * (1.0F - (float)Math.exp((double)(-16.0F * this.delta)));
      float time = (float)(nano - this.openedAt) / 1.0E9F;
      double mx = (double)(((float)mouseX - this.originX) / this.scale);
      double my = (double)(((float)mouseY - this.originY) / this.scale);
      this.tooltip = null;
      this.orderedTooltip = null;
      Matrix3x2fStack matrices = context.method_51448();
      boolean list = listMode();
      float top = this.listTop();
      float bottom = this.listBottom();
      float left = list ? 210.0F : 54.0F;
      float wide = list ? 440.0F : 752.0F;
      matrices.pushMatrix();
      matrices.translate(this.originX, this.originY);
      matrices.scale(this.scale, this.scale);
      if (list) {
         this.group(context, time, 0.0F, () -> this.listHeader(context, mx, my));
         this.group(context, time, 0.03F, () -> this.foreignButtons(context, mx, my, mouseX, mouseY));
         this.group(context, time, 0.16F, () -> this.actions(context, mx, my, mouseX, mouseY));
         if (this.cards.isEmpty()) {
            this.group(context, time, 0.1F, () -> this.emptyList(context));
         }
      } else {
         this.group(context, time, 0.0F, () -> this.topBar(context));
         this.group(context, time, 0.03F, () -> this.foreignButtons(context, mx, my, mouseX, mouseY));
         this.group(context, time, 0.06F, () -> this.header(context, mx, my));
         if (violetStyle()) {
            this.group(context, time, 0.18F, () -> this.actions(context, mx, my, mouseX, mouseY));
         } else {
            this.group(context, time, 0.18F, () -> this.dock(context));
         }
      }

      matrices.popMatrix();
      context.method_44379(this.px(left - 14.0F), this.py(top - 10.0F), this.px(left + wide + 14.0F), this.py(bottom + 8.0F));
      matrices.pushMatrix();
      matrices.translate(this.originX, this.originY);
      matrices.scale(this.scale, this.scale);
      boolean grid = my >= (double)(top - 10.0F) && my <= (double)(bottom + 8.0F);

      for (int i = 0; i < this.cards.size(); i++) {
         MultiplayerGridScreen.Card card = this.cards.get(i);
         float y = card.y() - this.scroll;
         if (!(y + card.h() < top - 12.0F) && !(y > bottom + 12.0F)) {
            float delay = 0.1F + (float)Math.min(i, 12) * 0.035F;
            this.group(context, time, delay, () -> this.drawCard(context, card, y, grid, mx, my, mouseX, mouseY));
         }
      }

      matrices.popMatrix();
      context.method_44380();
      if (this.content > bottom - top) {
         float view = bottom - top;
         float thumb = Math.max(24.0F, view * view / this.content);
         float travel = view - thumb;
         float at = top + (this.content - view <= 0.0F ? 0.0F : travel * this.scroll / (this.content - view));
         matrices.pushMatrix();
         matrices.translate(this.originX, this.originY);
         matrices.scale(this.scale, this.scale);
         this.rect(context, left + wide + 8.0F, at, 3.0F, thumb, 1, alpha(16777215, 0.22F));
         matrices.popMatrix();
      }

      super.method_25394(context, mouseX, mouseY, deltaTicks);
      if (this.tooltip != null && !this.tooltip.isEmpty()) {
         context.method_51434(this.field_22793, this.tooltip, this.tooltipX, this.tooltipY);
      } else if (this.orderedTooltip != null && !this.orderedTooltip.isEmpty()) {
         context.method_51447(this.field_22793, this.orderedTooltip, this.tooltipX, this.tooltipY);
      }
   }

   private void violetTopBar(class_332 context) {
      float lh = 26.0F;
      float lw = lh / LOGO.aspect();
      LOGO.draw(context, 54.0F, 12.0F, lw, this.f(-1));
      this.spaced(context, "CLIENT", 54.0F + lw + 8.0F, 29.0F, 6.6F, -1258291201);
      float[] chip = this.chipBox();
      this.violetPanel(context, chip[0], chip[1], chip[2], chip[3], 8, 0.0F, 0.0F);
      if (this.fade > 0.9F && this.skin != null) {
         Avatars.draw(context, this.worn(this.skin.get()), Math.round(chip[0] + 7.0F), Math.round(chip[1] + 6.0F), 20);
      }

      CrispText.draw(context, this.shownName(), "montserrat-bold.ttf", chip[0] + 34.0F, chip[1] + 15.0F, 9.0F, 0.0F, this.f(-1));
      this.spaced(context, "IN MENU", chip[0] + 34.0F, chip[1] + 25.0F, 6.6F, -1593835521);
      String clock = clock();
      CrispText.draw(context, clock, "montserrat-bold.ttf", this.clockLeft(), 29.0F, 9.0F, 0.0F, this.f(-922746881));
   }

   private void topBar(class_332 context) {
      if (violetStyle()) {
         this.violetTopBar(context);
      } else if (!vanillaPlus()) {
         if (!this.glass(context, 20.0F, 8.0F, 820.0F, 34.0F, 17)) {
            this.base(context, 20.0F, 8.0F, 820.0F, 34.0F, 17);
            this.rect(context, 20.0F, 8.0F, 820.0F, 34.0F, 17, 318767103);
            this.outline(context, 20.0F, 8.0F, 820.0F, 34.0F, 17, 536870911);
         }

         this.topLight(context, 20.0F, 8.0F, 820.0F, 17, 0.35F);
         Smooth.glow(context, 42, 25, 16, this.f(alpha(4059872, 0.3F)));
         Logo3D.draw(context, 42.0F, 25.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, this.fade, 0.7F);
         this.text(context, "Copium Client", 58.0F, 20.0F, 1.15F, -855051);
         String name = this.shownName();
         int pill = Math.round(30.0F + this.textWidth(name, 1.0F));
         int pillX = 830 - pill;
         if (!this.glass(context, (float)pillX, 12.0F, (float)pill, 26.0F, 13)) {
            this.base(context, (float)pillX, 12.0F, (float)pill, 26.0F, 13);
            this.rect(context, (float)pillX, 12.0F, (float)pill, 26.0F, 13, 352321535);
            this.outline(context, (float)pillX, 12.0F, (float)pill, 26.0F, 13, 536870911);
         }

         if (this.fade > 0.9F && this.skin != null) {
            Avatars.draw(context, this.worn(this.skin.get()), pillX + 6, 17, 16);
         }

         this.text(context, name, (float)(pillX + 26), 21.0F, 1.0F, -855051);
         String clock = clock();
         this.text(context, clock, (float)(pillX - 12) - this.textWidth(clock, 1.0F), 21.0F, 1.0F, -855051);
      }
   }

   private void violetHeader(class_332 context, double mx, double my) {
      CrispText.draw(context, "Multiplayer", "montserrat-bold.ttf", 54.0F, 98.0F, 30.0F, -0.01F, this.f(-1));
      int servers = 0;
      int lan = 0;

      for (class_504 entry : this.field_3043.method_25396()) {
         if (entry instanceof class_4270) {
            servers++;
         } else if (entry instanceof class_4269) {
            lan++;
         }
      }

      String sub = servers + (servers == 1 ? " SERVER" : " SERVERS") + "  ·  " + (lan > 0 ? lan + " LAN " + (lan == 1 ? "GAME" : "GAMES") : "LAN SCAN ACTIVE");
      this.spaced(context, sub, 56.0F, 114.0F, 7.4F, -1761607681);
      boolean overSearch = inside(mx, my, 552.0F, 72.0F, 192.0F, 26.0F);
      float focus = this.animate(this.hover, "search", this.search.method_25370(), 14.0F);
      float lit = this.animate(this.chosen, "search", overSearch, 12.0F);
      this.violetPanel(context, 552.0F, 72.0F, 192.0F, 26.0F, 8, Math.max(lit, focus), focus * 0.6F);
      Icons.draw(context, Icons.Icon.SEARCH, Math.round(561.0F), Math.round(79.0F), 12, this.f(lerp(-1761607681, -1, focus)));
      boolean overRefresh = inside(mx, my, 752.0F, 72.0F, 26.0F, 26.0F);
      float spin = this.animate(this.hover, "refresh", overRefresh, 12.0F);
      this.violetPanel(context, 752.0F, 72.0F, 26.0F, 26.0F, 8, spin, 0.0F);
      int cx = Math.round(765.0F);
      int cy = Math.round(85.0F);
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate((float)cx, (float)cy);
      matrices.rotate(spin * 3.1415927F);
      matrices.translate((float)(-cx), (float)(-cy));
      int color = this.f(lerp(-570425345, -1, spin));
      Smooth.ring(context, cx, cy, 6, color);
      Smooth.rect(context, cx + 2, cy - 7, 5, 5, 1, this.f(lerp(-15329253, -14934494, spin)));
      Smooth.triangle(context, (float)cx + 5.5F, (float)cy - 4.5F, 4.5F, color);
      matrices.popMatrix();
   }

   private void header(class_332 context, double mx, double my) {
      if (violetStyle()) {
         this.violetHeader(context, mx, my);
      } else {
         int accent = accent();
         if (!Theme.liquidGlass() && !vanillaPlus()) {
            Smooth.glow(context, Math.round(134.0F), 88, 100, this.f(alpha(accent, 0.2F)));
         }

         for (int k = 0; k < 8 && !Theme.liquidGlass() && !vanillaPlus(); k++) {
            double angle = 6.283185307179586 * (double)k / 8.0;
            CrispText.draw(
               context,
               "Multiplayer",
               "montserrat-bold.ttf",
               54.0F + (float)Math.cos(angle) * 1.4F,
               98.0F + (float)Math.sin(angle) * 1.4F,
               30.0F,
               -0.01F,
               this.f(alpha(accent, 0.16F))
            );
         }

         CrispText.draw(
            context,
            "Multiplayer",
            vanillaPlus() ? "geist-semibold.ttf" : "montserrat-bold.ttf",
            54.0F,
            98.0F,
            vanillaPlus() ? 26.0F : 30.0F,
            -0.01F,
            this.f(-855051)
         );
         int servers = 0;
         int lan = 0;

         for (class_504 entry : this.field_3043.method_25396()) {
            if (entry instanceof class_4270) {
               servers++;
            } else if (entry instanceof class_4269) {
               lan++;
            }
         }

         String sub = servers
            + (servers == 1 ? " SERVER" : " SERVERS")
            + "  ·  "
            + (lan > 0 ? lan + " LAN " + (lan == 1 ? "GAME" : "GAMES") : "LAN SCAN ACTIVE");
         this.text(context, sub, 56.0F, 106.0F, 0.85F, -4275510);
         boolean overSearch = inside(mx, my, 552.0F, 72.0F, 192.0F, 26.0F);
         float focus = this.animate(this.hover, "search", this.search.method_25370(), 14.0F);
         float lit = this.animate(this.chosen, "search", overSearch, 12.0F);
         if (!this.glass(context, 552.0F, 72.0F, 192.0F, 26.0F, 13)) {
            this.base(context, 552.0F, 72.0F, 192.0F, 26.0F, 13);
            this.rect(context, 552.0F, 72.0F, 192.0F, 26.0F, 13, lerp(268435455, 452984831, Math.max(focus, lit * 0.6F)));
            this.outline(context, 552.0F, 72.0F, 192.0F, 26.0F, 13, lerp(536870911, 872415231, lit * (1.0F - focus)));
         }

         this.neon(context, 552.0F, 72.0F, 192.0F, 26.0F, 13, accent, focus);
         Icons.draw(context, Icons.Icon.SEARCH, Math.round(561.0F), Math.round(79.0F), 12, this.f(lerp(-4275510, -855051, focus)));
         boolean overRefresh = inside(mx, my, 752.0F, 72.0F, 26.0F, 26.0F);
         float spin = this.animate(this.hover, "refresh", overRefresh, 12.0F);
         if (!this.glass(context, 752.0F, 72.0F, 26.0F, 26.0F, 13)) {
            this.base(context, 752.0F, 72.0F, 26.0F, 26.0F, 13);
            this.rect(context, 752.0F, 72.0F, 26.0F, 26.0F, 13, lerp(268435455, 486539263, spin));
            this.outline(context, 752.0F, 72.0F, 26.0F, 26.0F, 13, lerp(536870911, 16777215, spin));
         }

         this.neon(context, 752.0F, 72.0F, 26.0F, 26.0F, 13, accent, spin);
         int cx = Math.round(765.0F);
         int cy = Math.round(85.0F);
         int color = this.f(lerp(-855051, accent, spin));
         Smooth.ring(context, cx, cy, 6, color);
         Smooth.rect(context, cx + 2, cy - 7, 5, 5, 1, this.f(lerp(620756991, 822083583, spin)));
         Smooth.triangle(context, (float)cx + 5.5F, (float)cy - 4.5F, 4.5F, color);
      }
   }

   private void dock(class_332 context) {
      if (!vanillaPlus() && !violetStyle()) {
         if (!this.glass(context, this.dockX, 392.0F, this.dockW, 38.0F, 19)) {
            this.base(context, this.dockX, 392.0F, this.dockW, 38.0F, 19);
            this.rect(context, this.dockX, 392.0F, this.dockW, 38.0F, 19, 318767103);
            this.outline(context, this.dockX, 392.0F, this.dockW, 38.0F, 19, 536870911);
         }

         this.topLight(context, this.dockX, 392.0F, this.dockW, 19, 0.35F);

         for (float x : this.dividers) {
            this.rect(context, x, 402.0F, 1.0F, 18.0F, 0, 536870911);
         }
      }
   }

   private void drawCard(class_332 context, MultiplayerGridScreen.Card card, float y, boolean grid, double mx, double my, int mouseX, int mouseY) {
      float x = card.x();
      float w = card.w();
      float h = card.h();
      boolean over = grid && inside(mx, my, x, y, w, h);
      float lit = this.animate(this.hover, card.entry(), over, 12.0F);
      int accent = accent();
      if (listMode()) {
         this.creamRow(context, card, x, y, w, h, lit, mx, my, mouseX, mouseY);
      } else if (violetStyle()) {
         this.violetCard(context, card, x, y, w, h, lit, mx, my, mouseX, mouseY);
      } else if (card.kind() >= 2) {
         this.base(context, x, y, w, h, 14);
         this.rect(context, x, y, w, h, 14, lerp(134217727, 318767103, lit));
         this.outline(context, x, y, w, h, 14, lerp(452984831, alpha(accent, 0.6F), lit));
         String label = card.kind() == 2 ? "+  Add Server" : "Direct Connect";
         float tw = this.textWidth(label, 1.15F);
         this.text(context, label, x + (w - tw) / 2.0F, y + h / 2.0F - 5.0F, 1.15F, lerp(-4275510, -855051, lit));
      } else {
         boolean selected = card.entry() == this.field_3043.method_25334();
         float sel = this.animate(this.chosen, card.entry(), selected, 14.0F);
         float glow = Math.max(sel, lit * 0.55F);
         if (glow > 0.01F && !vanillaPlus()) {
            Smooth.glow(context, Math.round(x + w / 2.0F), Math.round(y + h / 2.0F), Math.round(w * 0.66F), this.f(alpha(accent, 0.2F * glow)));
         }

         if (!this.glass(context, x, y, w, h, 14)) {
            this.base(context, x, y, w, h, 14);
            this.rect(context, x, y, w, h, 14, lerp(352321535, 587202559, Math.max(lit, sel * 0.6F)));
            this.outline(
               context, x, y, w, h, 14, vanillaPlus() ? lerp(lerp(822083583, -1325400065, lit), -1, sel) : lerp(lerp(536870911, 872415231, lit), 16777215, sel)
            );
         }

         this.topLight(context, x, y, w, 14, 0.42F * (1.0F - Math.max(sel, lit)));
         this.neon(context, x, y, w, h, 14, lerp(-1, accent, 0.7F), lit * 0.45F * (1.0F - sel));
         this.neon(context, x, y, w, h, 14, accent, sel);
         if (card.kind() == 0) {
            this.drawServer(context, (class_4270)card.entry(), x, y, w, h, sel, mx, my, mouseX, mouseY);
         } else {
            this.drawLan(context, (class_4269)card.entry(), x, y, w, h, sel);
         }

         if (sel > 0.01F) {
            int cx = Math.round(x + w - 21.0F);
            int cy = Math.round(y + h - 21.0F);
            boolean overPlay = inside(mx, my, x + w - 33.0F, y + h - 33.0F, 24.0F, 24.0F);
            if (!vanillaPlus()) {
               Smooth.glow(context, cx, cy, 22, this.f(alpha(accent, 0.55F * sel)));
            }

            Smooth.circle(context, cx, cy, overPlay ? 12 : 11, this.f(alpha(accent, sel)));
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float)cx + 1.0F, (float)cy);
            matrices.rotate(1.5707964F);
            Smooth.triangle(context, 0.0F, 0.0F, 9.0F, this.f(alpha(1707524, sel)));
            matrices.popMatrix();
         }
      }
   }

   private void violetCard(
      class_332 context, MultiplayerGridScreen.Card card, float x, float y, float w, float h, float lit, double mx, double my, int mouseX, int mouseY
   ) {
      if (card.kind() >= 2) {
         Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 5, this.f(lerp(-1777265898, -937944546, lit)));
         this.dashed(context, x, y, w, h, this.f(lerp(620756991, 1526726655, lit)));
         String label = card.kind() == 2 ? "+  ADD SERVER" : "DIRECT CONNECT";
         float tw = CrispText.width(label, "montserrat-bold.ttf", 8.4F, 0.5F);
         this.spaced(context, label, x + (w - tw) / 2.0F, y + h / 2.0F + 3.0F, 8.4F, lerp(-1761607681, -1, lit));
      } else {
         boolean selected = card.entry() == this.field_3043.method_25334();
         float sel = this.animate(this.chosen, card.entry(), selected, 14.0F);
         if (sel > 0.01F || lit > 0.01F) {
            Smooth.glow(
               context, Math.round(x + w / 2.0F), Math.round(y + h / 2.0F), Math.round(w * 0.6F), this.f(alpha(16777215, 0.05F * Math.max(sel, lit * 0.6F)))
            );
         }

         this.violetPanel(context, x, y, w, h, 9, lit, sel);
         if (card.kind() == 0) {
            this.drawServer(context, (class_4270)card.entry(), x, y, w, h, sel, mx, my, mouseX, mouseY);
         } else {
            this.drawLan(context, (class_4269)card.entry(), x, y, w, h, sel);
         }

         if (sel > 0.01F) {
            int cx = Math.round(x + w - 21.0F);
            int cy = Math.round(y + h - 21.0F);
            boolean overPlay = inside(mx, my, x + w - 33.0F, y + h - 33.0F, 24.0F, 24.0F);
            Smooth.circle(context, cx, cy, overPlay ? 12 : 11, this.f(alpha(16053495, sel)));
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float)cx + 1.0F, (float)cy);
            matrices.rotate(1.5707964F);
            Smooth.triangle(context, 0.0F, 0.0F, 9.0F, this.f(alpha(1315864, sel)));
            matrices.popMatrix();
            if (card.kind() == 0) {
               this.smallButton(context, this.cardEditBox(x, y, w, h), Icons.Icon.GEAR, sel, mx, my, "edit");
               this.smallButton(context, this.cardDeleteBox(x, y, w, h), Icons.Icon.CLOSE, sel, mx, my, "delete");
            }
         }
      }
   }

   private float[] cardEditBox(float x, float y, float w, float h) {
      return new float[]{x + w - 84.0F, y + h - 31.0F, 20.0F, 20.0F};
   }

   private float[] cardDeleteBox(float x, float y, float w, float h) {
      return new float[]{x + w - 60.0F, y + h - 31.0F, 20.0F, 20.0F};
   }

   private void dashed(class_332 context, float x, float y, float w, float h, int color) {
      float r = 5.0F;
      float dash = 5.0F;
      float gap = 3.5F;

      for (float sx = x + r; sx < x + w - r; sx += dash + gap) {
         float len = Math.min(dash, x + w - r - sx);
         context.method_25294(Math.round(sx), Math.round(y), Math.round(sx + len), Math.round(y) + 1, color);
         context.method_25294(Math.round(sx), Math.round(y + h) - 1, Math.round(sx + len), Math.round(y + h), color);
      }

      for (float sy = y + r; sy < y + h - r; sy += dash + gap) {
         float len = Math.min(dash, y + h - r - sy);
         context.method_25294(Math.round(x), Math.round(sy), Math.round(x) + 1, Math.round(sy + len), color);
         context.method_25294(Math.round(x + w) - 1, Math.round(sy), Math.round(x + w), Math.round(sy + len), color);
      }

      int[][] corners = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}};

      for (int[] corner : corners) {
         float cx = corner[0] == 0 ? x + r : x + w - r;
         float cy = corner[1] == 0 ? y + r : y + h - r;
         float start = corner[0] == 0 ? (corner[1] == 0 ? 3.1415927F : 1.5707964F) : (corner[1] == 0 ? 4.712389F : 0.0F);
         float[] xs = new float[7];
         float[] ys = new float[7];
         float[] ws = new float[7];
         int[] cs = new int[7];

         for (int i = 0; i < 7; i++) {
            double a = (double)start + 1.5707963267948966 * (double)i / 6.0;
            xs[i] = cx + (float)Math.cos(a) * (r - 0.5F);
            ys[i] = cy + (float)Math.sin(a) * (r - 0.5F);
            ws[i] = 1.0F;
            cs[i] = color;
         }

         Smooth.polyline(context, xs, ys, ws, cs, 7);
      }
   }

   private void drawServer(class_332 context, class_4270 entry, float x, float y, float w, float h, float sel, double mx, double my, int mouseX, int mouseY) {
      class_642 server = entry.method_20133();
      class_2960 icon = UNKNOWN;
      if (server.method_49306() != null && entry instanceof ServerEntryAccessor access && access.violet$favicon() != null) {
         class_8573 world = access.violet$icon();
         if (world != null && !world.method_73412()) {
            icon = world.method_52201();
         }
      }

      context.method_25293(class_10799.field_56883, icon, Math.round(x + 10.0F), Math.round(y + 10.0F), 0.0F, 0.0F, 32, 32, 64, 64, 64, 64, this.f(-1));
      this.outline(context, x + 10.0F, y + 10.0F, 32.0F, 32.0F, 7, 620756991);
      class_9083 status = server.method_55825();
      boolean waiting = status == class_9083.field_47880 || status == class_9083.field_47881;
      class_2561 players;
      if (status == class_9083.field_47883) {
         players = server.field_3760.method_27661().method_27692(class_124.field_1061);
      } else if (waiting) {
         players = class_2561.method_43470(".".repeat(1 + (int)(System.currentTimeMillis() / 350L % 3L))).method_27692(class_124.field_1080);
      } else {
         players = (class_2561)(server.field_3753 == null ? class_2561.method_43473() : server.field_3753);
      }

      List<MultiplayerGridScreen.Run> playerRuns = this.runs(players, -855051).get(0);
      float pw = this.runsWidth(playerRuns, 0.85F);
      float barsX = x + w - 28.0F;
      this.drawRuns(context, playerRuns, barsX - 6.0F - pw, y + 13.0F, 0.85F, pw + 2.0F);
      this.pingBars(context, barsX, y + 21.0F, server, status);
      if (inside(mx, my, barsX - 6.0F - pw, y + 10.0F, pw, 12.0F) && server.field_3762 != null && !server.field_3762.isEmpty()) {
         this.tooltip = server.field_3762;
         this.tooltipX = mouseX;
         this.tooltipY = mouseY;
      } else if (inside(mx, my, barsX - 1.0F, y + 9.0F, 22.0F, 14.0F)) {
         this.tooltip = List.of(
            status == class_9083.field_47884
               ? class_2561.method_43470(server.field_3758 + " ms")
               : (waiting ? class_2561.method_43471("multiplayer.status.pinging") : class_2561.method_43471("multiplayer.status.no_connection"))
         );
         this.tooltipX = mouseX;
         this.tooltipY = mouseY;
      }

      float nameRoom = barsX - 12.0F - pw - (x + 50.0F);
      this.text(context, this.fit(server.field_3752, Math.max(40.0F, nameRoom), 1.15F), x + 50.0F, y + 13.0F, 1.15F, -855051);
      this.text(context, this.fit(server.field_3761, w - 62.0F, 0.85F), x + 50.0F, y + 28.0F, 0.85F, -4275510);
      if (!waiting && server.field_3757 != null) {
         float room = w - 20.0F - (sel > 0.01F ? 30.0F : 0.0F);
         List<List<MultiplayerGridScreen.Run>> lines = this.wrap(this.runs(server.field_3757, -4275510), 0.85F, room);

         for (int i = 0; i < Math.min(2, lines.size()); i++) {
            this.drawRuns(context, lines.get(i), x + 10.0F, y + 50.0F + (float)i * 11.0F, 0.85F, room);
         }
      } else {
         this.text(context, class_2561.method_43471("multiplayer.status.pinging").getString(), x + 10.0F, y + 50.0F, 0.85F, -7367779);
      }
   }

   private void pingBars(class_332 context, float x, float bottom, class_642 server, class_9083 status) {
      int bars;
      int color;
      if (status == class_9083.field_47884) {
         long ping = server.field_3758;
         bars = ping < 0L ? 5 : (ping < 150L ? 5 : (ping < 300L ? 4 : (ping < 600L ? 3 : (ping < 1000L ? 2 : 1))));
         if (listMode()) {
            color = bars >= 3 ? -723723 : -7434602;
         } else {
            color = bars >= 4 ? -9637766 : (bars == 3 ? -14006 : -38037);
         }
      } else if (status != class_9083.field_47880 && status != class_9083.field_47881) {
         bars = 1;
         color = listMode() ? -1750963 : -38037;
      } else {
         bars = 1 + (int)(System.currentTimeMillis() / 160L % 5L);
         color = listMode() ? -10724252 : -6446678;
      }

      for (int i = 0; i < 5; i++) {
         int bh = 3 + i * 2;
         this.rect(context, x + (float)i * 4.0F, bottom - (float)bh, 2.0F, (float)bh, 1, i < bars ? color : (listMode() ? -14013906 : 654311423));
      }
   }

   private void drawLan(class_332 context, class_4269 entry, float x, float y, float w, float h, float sel) {
      class_1131 info = ((LanServerEntryAccessor)entry).violet$server();
      context.method_25293(class_10799.field_56883, UNKNOWN, Math.round(x + 10.0F), Math.round(y + 10.0F), 0.0F, 0.0F, 32, 32, 64, 64, 64, 64, this.f(-1));
      this.outline(context, x + 10.0F, y + 10.0F, 32.0F, 32.0F, 7, 620756991);
      float chip = this.textWidth("LAN", 0.85F) + 12.0F;
      this.rect(context, x + w - 10.0F - chip, y + 10.0F, chip, 14.0F, 7, alpha(accent(), 0.25F));
      this.text(context, "LAN", x + w - 4.0F - chip, y + 13.5F, 0.85F, -855051);
      this.text(context, this.fit(info.method_4813(), w - 70.0F - chip, 1.15F), x + 50.0F, y + 13.0F, 1.15F, -855051);
      this.text(context, this.fit(info.method_4812(), w - 62.0F, 0.85F), x + 50.0F, y + 28.0F, 0.85F, -4275510);
      this.text(context, "Local network game", x + 10.0F, y + 50.0F, 0.85F, -4275510);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      if (listMode() && this.field_22787 != null && this.field_22787.field_1687 == null) {
         VioletScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0F);
         float markH = (float)this.field_22790 * 0.8F;
         Mono.watermark(context, (float)this.field_22789 * 0.88F - Mono.watermarkWidth(markH) / 2.0F, (float)this.field_22790 * 0.1F, markH, 18);
      } else {
         super.method_25420(context, mouseX, mouseY, deltaTicks);
      }
   }

   private void creamPanel(class_332 context, float x, float y, float w, float h, int r, float lit, float selected) {
      this.rect(context, x, y, w, h, 3, lerp(-15198181, -14737629, Math.max(lit, selected)));
      this.outline(context, x, y, w, h, 3, lerp(lerp(-14013906, -12237491, lit), -723723, selected * 0.85F));
   }

   private void creamText(class_332 context, String value, float x, float baseline, float em, int color) {
      CrispText.draw(context, value, "geist-semibold.ttf", x, baseline, em, 0.0F, this.f(color));
   }

   private static String creamFit(String value, float max, float em) {
      if (value == null) {
         return "";
      } else if (CrispText.width(value, "geist-semibold.ttf", em, 0.0F) <= max) {
         return value;
      } else {
         String cut = value;

         while (cut.length() > 1 && CrispText.width(cut + "...", "geist-semibold.ttf", em, 0.0F) > max) {
            cut = cut.substring(0, cut.length() - 1);
         }

         return cut.stripTrailing() + "...";
      }
   }

   private void listHeader(class_332 context, double mx, double my) {
      float hy = this.headY();
      float lh = 30.0F;
      float lw = lh / LOGO.aspect();
      LOGO.draw(context, 208.0F, hy - 4.0F, lw, this.f(-1));
      int servers = 0;
      int lan = 0;

      for (class_504 entry : this.field_3043.method_25396()) {
         if (entry instanceof class_4270) {
            servers++;
         } else if (entry instanceof class_4269) {
            lan++;
         }
      }

      String sub = servers + (servers == 1 ? " server" : " servers") + "  ·  " + (lan > 0 ? lan + " in your network" : "Looking for LAN games");
      this.creamText(context, sub, 211.0F, hy + 40.0F, 7.0F, -7434602);
      boolean overSearch = inside(mx, my, 448.0F, hy, 170.0F, 26.0F);
      float focus = this.animate(this.hover, "search", this.search.method_25370(), 14.0F);
      float lit = this.animate(this.chosen, "search", overSearch, 12.0F);
      this.creamPanel(context, 448.0F, hy, 170.0F, 26.0F, 13, Math.max(lit, focus), focus * 0.5F);
      Icons.draw(context, Icons.Icon.SEARCH, Math.round(459.0F), Math.round(hy + 7.0F), 12, this.f(lerp(-4934468, -723723, focus)));
      boolean overRefresh = inside(mx, my, 624.0F, hy, 26.0F, 26.0F);
      float spin = this.animate(this.hover, "refresh", overRefresh, 12.0F);
      this.creamPanel(context, 624.0F, hy, 26.0F, 26.0F, 13, spin, 0.0F);
      refreshIcon(context, 637.0F, hy + 13.0F, spin * 3.1415927F, this.f(lerp(-4934468, -723723, spin)));
      if (overRefresh) {
         this.tooltip = List.of(class_2561.method_43471("selectServer.refresh"));
         this.tooltipX = (int)Math.round((double)this.originX + mx * (double)this.scale);
         this.tooltipY = (int)Math.round((double)this.originY + my * (double)this.scale);
      }
   }

   private static void refreshIcon(class_332 context, float cx, float cy, float turn, int color) {
      int n = 22;
      float[] xs = new float[n];
      float[] ys = new float[n];
      float[] ws = new float[n];
      int[] cs = new int[n];
      float start = -0.6F + turn;
      float sweep = 4.8694687F;
      float r = 5.5F;

      for (int i = 0; i < n; i++) {
         double a = (double)(start + sweep * (float)i / (float)(n - 1));
         xs[i] = cx + (float)Math.cos(a) * r;
         ys[i] = cy + (float)Math.sin(a) * r;
         ws[i] = 1.4F;
         cs[i] = color;
      }

      Smooth.polyline(context, xs, ys, ws, cs, n);
      double end = (double)(start + sweep);
      float ex = cx + (float)Math.cos(end) * r;
      float ey = cy + (float)Math.sin(end) * r;
      float tx = (float)(-Math.sin(end));
      float ty = (float)Math.cos(end);
      float nx = (float)Math.cos(end);
      float ny = (float)Math.sin(end);
      Smooth.polyline(
         context,
         new float[]{ex - nx * 2.6F - tx * 0.6F, ex + tx * 2.2F, ex + nx * 2.6F - tx * 0.6F},
         new float[]{ey - ny * 2.6F - ty * 0.6F, ey + ty * 2.2F, ey + ny * 2.6F - ty * 0.6F},
         new float[]{1.4F, 1.4F, 1.4F},
         new int[]{color, color, color},
         3
      );
   }

   private void emptyList(class_332 context) {
      String title = "No servers yet";
      String hint = "Add one with the + button below.";
      float tw = CrispText.width(title, "geist-semibold.ttf", 10.0F, 0.0F);
      float hw = CrispText.width(hint, "geist-semibold.ttf", 7.4F, 0.0F);
      float cy = (this.listTop() + this.listBottom()) / 2.0F;
      this.creamText(context, title, 210.0F + (440.0F - tw) / 2.0F, cy - 2.0F, 10.0F, -723723);
      this.creamText(context, hint, 210.0F + (440.0F - hw) / 2.0F, cy + 13.0F, 7.4F, -7434602);
   }

   private float[][] actionBoxes() {
      float dw = CrispText.width(class_2561.method_43471(ACTIONS[2]).getString(), "geist-semibold.ttf", 8.2F, 0.0F) + 30.0F;
      float bw = CrispText.width(class_2561.method_43471(ACTIONS[3]).getString(), "geist-semibold.ttf", 8.2F, 0.0F) + 30.0F;
      float plus = 30.0F;
      float jw = 440.0F - plus - dw - bw - 18.0F;
      float ay = listMode() ? this.actionY() : 394.0F;
      return new float[][]{
         {210.0F, ay, jw, 30.0F},
         {210.0F + jw + 6.0F, ay, plus, 30.0F},
         {210.0F + jw + 12.0F + plus, ay, dw, 30.0F},
         {210.0F + jw + 18.0F + plus + dw, ay, bw, 30.0F}
      };
   }

   private void actions(class_332 context, double mx, double my, int mouseX, int mouseY) {
      float[][] boxes = this.actionBoxes();

      for (int i = 0; i < boxes.length; i++) {
         float[] b = boxes[i];
         class_4185 button = this.buttons.get(ACTIONS[i]);
         boolean active = button == null || button.field_22763;
         boolean over = inside(mx, my, b[0], b[1], b[2], b[3]);
         float lit = this.animate(this.hover, ACTIONS[i], over && active, 12.0F);
         int r = 3;
         if (i == 0) {
            this.rect(context, b[0], b[1], b[2], b[3], r, active ? lerp(-723723, -1, lit) : 871691509);
            String label = class_2561.method_43471(ACTIONS[0]).getString();
            float tw = CrispText.width(label, "geist-semibold.ttf", 8.6F, 0.0F);
            this.creamText(context, label, b[0] + (b[2] - tw) / 2.0F, b[1] + b[3] / 2.0F + 3.0F, 8.6F, active ? -15856112 : -7434602);
         } else {
            this.creamPanel(context, b[0], b[1], b[2], b[3], r, lit, 0.0F);
            if (i == 1) {
               Icons.draw(
                  context,
                  Icons.Icon.PLUS,
                  Math.round(b[0] + b[2] / 2.0F - 6.0F),
                  Math.round(b[1] + b[3] / 2.0F - 6.0F),
                  12,
                  this.f(lerp(-4934468, -723723, lit))
               );
               if (over) {
                  this.tooltip = List.of(class_2561.method_43471(ACTIONS[1]));
                  this.tooltipX = mouseX;
                  this.tooltipY = mouseY;
               }
            } else {
               String label = class_2561.method_43471(ACTIONS[i]).getString();
               float tw = CrispText.width(label, "geist-semibold.ttf", 8.2F, 0.0F);
               this.creamText(context, label, b[0] + (b[2] - tw) / 2.0F, b[1] + b[3] / 2.0F + 3.0F, 8.2F, lerp(-4934468, -723723, lit));
            }
         }
      }
   }

   private float[] editBox(float x, float y, float w, float h) {
      return new float[]{x + w - 84.0F, y + (h - 20.0F) / 2.0F, 20.0F, 20.0F};
   }

   private float[] deleteBox(float x, float y, float w, float h) {
      return new float[]{x + w - 60.0F, y + (h - 20.0F) / 2.0F, 20.0F, 20.0F};
   }

   private void smallButton(class_332 context, float[] b, Icons.Icon icon, float shown, double mx, double my, Object key) {
      float lit = this.animate(this.hover, key, inside(mx, my, b[0], b[1], b[2], b[3]), 14.0F);
      this.rect(context, b[0], b[1], b[2], b[3], 3, alpha(16053493, (0.08F + 0.16F * lit) * shown));
      Icons.draw(context, icon, Math.round(b[0] + 5.0F), Math.round(b[1] + 5.0F), 10, this.f(alpha(lerp(-4934468, -723723, lit), shown)));
   }

   private class_2960 favicon(class_4270 entry) {
      if (entry.method_20133().method_49306() != null && entry instanceof ServerEntryAccessor access && access.violet$favicon() != null) {
         class_8573 world = access.violet$icon();
         if (world != null && !world.method_73412()) {
            return world.method_52201();
         }
      }

      return UNKNOWN;
   }

   private void creamRow(
      class_332 context, MultiplayerGridScreen.Card card, float x, float y, float w, float h, float lit, double mx, double my, int mouseX, int mouseY
   ) {
      boolean selected = card.entry() == this.field_3043.method_25334();
      float sel = this.animate(this.chosen, card.entry(), selected, 14.0F);
      this.creamPanel(context, x, y, w, h, 12, lit, sel);
      if (card.kind() == 1) {
         this.creamLan(context, (class_4269)card.entry(), x, y, w, h);
      } else {
         class_4270 entry = (class_4270)card.entry();
         class_642 server = entry.method_20133();
         context.method_25293(
            class_10799.field_56883, this.favicon(entry), Math.round(x + 7.0F), Math.round(y + 6.0F), 0.0F, 0.0F, 26, 26, 64, 64, 64, 64, this.f(-1)
         );
         class_9083 status = server.method_55825();
         boolean waiting = status == class_9083.field_47880 || status == class_9083.field_47881;
         float barsX = x + w - 30.0F;
         this.pingBars(context, barsX, y + h / 2.0F + 5.0F, server, status);
         if (sel > 0.01F) {
            this.smallButton(context, this.editBox(x, y, w, h), Icons.Icon.GEAR, sel, mx, my, "edit");
            this.smallButton(context, this.deleteBox(x, y, w, h), Icons.Icon.CLOSE, sel, mx, my, "delete");
         }

         class_2561 players;
         if (status == class_9083.field_47883) {
            players = server.field_3760.method_27661().method_27692(class_124.field_1061);
         } else if (waiting) {
            players = class_2561.method_43470(".".repeat(1 + (int)(System.currentTimeMillis() / 350L % 3L))).method_27692(class_124.field_1080);
         } else {
            players = (class_2561)(server.field_3753 == null ? class_2561.method_43473() : server.field_3753);
         }

         List<MultiplayerGridScreen.Run> playerRuns = this.runs(players, -4934468).get(0);
         float pw = this.runsWidth(playerRuns, 0.8F);
         float playersRight = barsX - 10.0F - 56.0F * sel;
         this.drawRuns(context, playerRuns, playersRight - pw, y + h / 2.0F - 4.0F, 0.8F, pw + 2.0F);
         float textX = x + 42.0F;
         float room = playersRight - pw - 12.0F - textX;
         this.creamText(context, creamFit(server.field_3752, Math.max(40.0F, room), 8.6F), textX, y + 17.0F, 8.6F, -723723);
         if (!waiting && server.field_3757 != null) {
            List<List<MultiplayerGridScreen.Run>> lines = this.runs(server.field_3757, -4934468);
            if (!lines.isEmpty()) {
               this.drawRuns(context, lines.get(0), textX, y + 21.5F, 0.72F, Math.max(40.0F, room));
            }
         } else {
            this.creamText(context, class_2561.method_43471("multiplayer.status.pinging").getString(), textX, y + 29.0F, 6.8F, -7434602);
         }

         if (inside(mx, my, playersRight - pw, y + 8.0F, pw, 14.0F) && server.field_3762 != null && !server.field_3762.isEmpty()) {
            this.tooltip = server.field_3762;
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
         } else if (inside(mx, my, barsX - 1.0F, y + 8.0F, 22.0F, h - 16.0F)) {
            this.tooltip = List.of(
               status == class_9083.field_47884
                  ? class_2561.method_43470(server.field_3758 + " ms")
                  : (waiting ? class_2561.method_43471("multiplayer.status.pinging") : class_2561.method_43471("multiplayer.status.no_connection"))
            );
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
         } else if (sel > 0.5F && inside(mx, my, this.editBox(x, y, w, h)[0], this.editBox(x, y, w, h)[1], 20.0F, 20.0F)) {
            this.tooltip = List.of(class_2561.method_43471("selectServer.edit"));
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
         } else if (sel > 0.5F && inside(mx, my, this.deleteBox(x, y, w, h)[0], this.deleteBox(x, y, w, h)[1], 20.0F, 20.0F)) {
            this.tooltip = List.of(class_2561.method_43471("selectServer.delete"));
            this.tooltipX = mouseX;
            this.tooltipY = mouseY;
         }
      }
   }

   private void creamLan(class_332 context, class_4269 entry, float x, float y, float w, float h) {
      class_1131 info = ((LanServerEntryAccessor)entry).violet$server();
      context.method_25293(class_10799.field_56883, UNKNOWN, Math.round(x + 7.0F), Math.round(y + 6.0F), 0.0F, 0.0F, 26, 26, 64, 64, 64, 64, this.f(-1));
      float chip = CrispText.width("LAN", "geist-semibold.ttf", 6.8F, 0.0F) + 14.0F;
      this.rect(context, x + w - 12.0F - chip, y + (h - 14.0F) / 2.0F, chip, 14.0F, 3, 871691509);
      this.creamText(context, "LAN", x + w - 5.0F - chip, y + h / 2.0F + 2.4F, 6.8F, -723723);
      this.creamText(context, creamFit(info.method_4813(), w - 70.0F - chip, 8.6F), x + 42.0F, y + 17.0F, 8.6F, -723723);
      this.creamText(context, info.method_4812(), x + 42.0F, y + 29.0F, 6.8F, -7434602);
   }

   private boolean clickList(class_11909 click, double mx, double my) {
      if (inside(mx, my, 624.0F, this.headY(), 26.0F, 26.0F)) {
         UiSounds.click();
         this.press(DOCK[5]);
         return true;
      } else if (inside(mx, my, 448.0F, this.headY(), 170.0F, 26.0F)) {
         this.method_25395(this.search);
         this.search.method_25365(true);
         return true;
      } else {
         return this.clickActions(mx, my);
      }
   }

   private boolean clickActions(double mx, double my) {
      float[][] boxes = this.actionBoxes();

      for (int i = 0; i < boxes.length; i++) {
         if (inside(mx, my, boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3])) {
            class_4185 button = this.buttons.get(ACTIONS[i]);
            if (button != null && button.field_22763) {
               UiSounds.click();
               this.press(ACTIONS[i]);
            }

            return true;
         }
      }

      return false;
   }

   private boolean clickRow(MultiplayerGridScreen.Card card, float y, double mx, double my, boolean doubled) {
      class_504 entry = (class_504)card.entry();
      if (listMode() && card.kind() == 0 && entry == this.field_3043.method_25334()) {
         float[] edit = this.editBox(card.x(), y, card.w(), card.h());
         float[] delete = this.deleteBox(card.x(), y, card.w(), card.h());
         if (inside(mx, my, edit[0], edit[1], edit[2], edit[3])) {
            UiSounds.click();
            this.press(DOCK[3]);
            return true;
         }

         if (inside(mx, my, delete[0], delete[1], delete[2], delete[3])) {
            UiSounds.click();
            this.press(DOCK[4]);
            return true;
         }
      }

      this.select(entry);
      if (doubled) {
         entry.method_74043();
      }

      return true;
   }

   private void press(String key) {
      class_4185 button = this.buttons.get(key);
      if (button != null && button.field_22763) {
         button.method_25306(new class_11908(257, 0, 0));
      }
   }

   private void select(class_504 entry) {
      this.field_3043.method_20122(entry);
      this.method_20121();
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      double mx = (click.comp_4798() - (double)this.originX) / (double)this.scale;
      double my = (click.comp_4799() - (double)this.originY) / (double)this.scale;
      if (this.clickForeign(click, mx, my, doubled)) {
         return true;
      } else if (listMode() && click.method_74245() == 0) {
         if (this.search.method_25370() && inside(mx, my, 448.0F, this.headY(), 170.0F, 26.0F)) {
            if (!super.method_25402(click, doubled)) {
            }

            return true;
         } else if (this.clickList(click, mx, my)) {
            return true;
         } else {
            if (my >= (double)(this.listTop() - 6.0F) && my <= (double)(this.listBottom() + 6.0F)) {
               for (MultiplayerGridScreen.Card card : this.cards) {
                  float y = card.y() - this.scroll;
                  if (inside(mx, my, card.x(), y, card.w(), card.h())) {
                     this.method_25395(null);
                     return this.clickRow(card, y, mx, my, doubled);
                  }
               }
            }

            return super.method_25402(click, doubled);
         }
      } else {
         if (click.method_74245() == 0) {
            if (inside(mx, my, 752.0F, 72.0F, 26.0F, 26.0F)) {
               this.press(DOCK[5]);
               return true;
            }

            if (violetStyle() && this.clickActions(mx, my)) {
               return true;
            }

            if (inside(mx, my, 552.0F, 72.0F, 192.0F, 26.0F)) {
               this.method_25395(this.search);
               this.search.method_25365(true);
               if (!super.method_25402(click, doubled)) {
               }

               return true;
            }

            if (my >= 114.0 && my <= 388.0) {
               for (MultiplayerGridScreen.Card cardx : this.cards) {
                  float y = cardx.y() - this.scroll;
                  if (inside(mx, my, cardx.x(), y, cardx.w(), cardx.h())) {
                     this.method_25395(null);
                     if (cardx.kind() == 2) {
                        this.press(DOCK[2]);
                     } else if (cardx.kind() == 3) {
                        this.press(DOCK[1]);
                     } else {
                        class_504 entry = (class_504)cardx.entry();
                        if (violetStyle() && cardx.kind() == 0 && entry == this.field_3043.method_25334()) {
                           float[] edit = this.cardEditBox(cardx.x(), y, cardx.w(), cardx.h());
                           float[] delete = this.cardDeleteBox(cardx.x(), y, cardx.w(), cardx.h());
                           if (inside(mx, my, edit[0], edit[1], edit[2], edit[3])) {
                              UiSounds.click();
                              this.press(DOCK[3]);
                              return true;
                           }

                           if (inside(mx, my, delete[0], delete[1], delete[2], delete[3])) {
                              UiSounds.click();
                              this.press(DOCK[4]);
                              return true;
                           }
                        }

                        boolean play = entry == this.field_3043.method_25334()
                           && inside(mx, my, cardx.x() + cardx.w() - 33.0F, y + cardx.h() - 33.0F, 24.0F, 24.0F);
                        this.select(entry);
                        if (play || doubled) {
                           entry.method_74043();
                        }
                     }

                     return true;
                  }
               }
            }
         }

         return super.method_25402(click, doubled);
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      double my = (mouseY - (double)this.originY) / (double)this.scale;
      if (my >= (double)(this.listTop() - 10.0F) && my <= (double)(this.listBottom() + 8.0F)) {
         this.scrollTarget = this.clampScroll(this.scrollTarget - (float)verticalAmount * 32.0F);
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (key == 257 || key == 335) {
         class_504 entry = (class_504)this.field_3043.method_25334();
         if (entry == null && this.search.method_25370()) {
            for (MultiplayerGridScreen.Card card : this.cards) {
               if (card.kind() <= 1) {
                  entry = (class_504)card.entry();
                  break;
               }
            }
         }

         if (entry != null) {
            entry.method_74043();
            return true;
         }
      }

      if (!this.search.method_25370()) {
         byte var10000;
         if (listMode()) {
            switch (key) {
               case 264:
                  var10000 = 1;
                  break;
               case 265:
                  var10000 = -1;
                  break;
               default:
                  var10000 = 0;
            }
         } else {
            switch (key) {
               case 262:
                  var10000 = 1;
                  break;
               case 263:
                  var10000 = -1;
                  break;
               case 264:
                  var10000 = 3;
                  break;
               case 265:
                  var10000 = -3;
                  break;
               default:
                  var10000 = 0;
            }
         }

         int step = var10000;
         if (step != 0 && this.move(step)) {
            return true;
         }
      }

      return super.method_25404(input);
   }

   private boolean move(int step) {
      List<MultiplayerGridScreen.Card> servers = new ArrayList<>();

      for (MultiplayerGridScreen.Card card : this.cards) {
         if (card.kind() <= 1) {
            servers.add(card);
         }
      }

      if (servers.isEmpty()) {
         return false;
      } else {
         int index = -1;
         Object selected = this.field_3043.method_25334();

         for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).entry() == selected) {
               index = i;
            }
         }

         int next = index < 0 ? 0 : Math.max(0, Math.min(servers.size() - 1, index + step));
         MultiplayerGridScreen.Card cardx = servers.get(next);
         this.select((class_504)cardx.entry());
         float top = cardx.y() - this.listTop();
         float bottom = cardx.y() + cardx.h() - this.listTop();
         float view = this.listBottom() - this.listTop();
         if (top < this.scrollTarget) {
            this.scrollTarget = this.clampScroll(top);
         } else if (bottom > this.scrollTarget + view) {
            this.scrollTarget = this.clampScroll(bottom - view);
         }

         return true;
      }
   }

   private static record Card(Object entry, int kind, float x, float y, float w, float h) {
   }

   private static record Run(String text, int color) {
   }
}
