package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.modules.ClickGui;
import dev.violet.modules.Configs;
import dev.violet.modules.Hud;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;

public final class PanelScreen extends class_437 {
   private static final ExactImage WORDMARK = new ExactImage("textures/brand/lockup.png");
   private static final Category[] SIDEBAR = new Category[]{
      Category.BASE_FINDING, Category.VISUALS, Category.MOVEMENT, Category.CLIENT, Category.DONUT, Category.COMBAT
   };
   private static final int TOP_H = 30;
   private static final int SIDE_W = 122;
   private static final int CARD_H = 26;
   private static final int GAP = 5;
   private static final int SEARCH_W = 170;
   private static final int FOOTER_H = 16;
   private static final int MIN_W = 380;
   private static final int MIN_H = 250;
   private static final int GRIP = 12;
   private static final String HINT = "Left click toggles  ·  Right click opens settings  ·  Middle click binds a key";
   private static final float NAME_EM = 7.6F;
   private static final float SMALL_EM = 6.4F;
   private static final float TAB_EM = 7.2F;
   private static final SettingsPane.Palette PALETTE = new SettingsPane.Palette(
      true, -723723, -7434602, -10724252, 284488949, 485815541, -14013906, -12237491, 653587701, 217380085, -723723, 821359861, -723723, false, -723723
   );
   private static PanelScreen.Tab tab = PanelScreen.Tab.MODULES;
   private static Category category = Category.BASE_FINDING;
   private static boolean acUnlocked;
   private final List<PanelScreen.Hit> hits = new ArrayList<>();
   private final Map<PanelScreen.Key, Float> animations = new HashMap<>();
   private final SettingsPane pane = new SettingsPane(this);
   private Module selected;
   private Module binding;
   private String search = "";
   private float scroll;
   private float scrollTarget;
   private float content;
   private float view;
   private long lastFrame = System.nanoTime();
   private long swallowUntil;
   private float delta;
   private float open;
   private float alpha;
   private boolean closing;
   private int wx;
   private int wy;
   private int ww;
   private int wh;
   private int gridX;
   private int gridY;
   private int gridW;
   private int gridH;
   private float z = 1.0F;
   private int vw;
   private int vh;
   private boolean resizing;
   private double resizeX;
   private double resizeY;
   private int resizeW;
   private int resizeH;
   private String tooltip;

   public PanelScreen() {
      super(class_2561.method_43470("SoberClient"));
      this.pane.palette(PALETTE);
   }

   private static ClickGui options() {
      return SoberClient.MODULES.get(ClickGui.class);
   }

   public void openSettings(Module module) {
      tab = PanelScreen.Tab.MODULES;
      this.selected = module;
      this.pane.show(module);
   }

   public void openSettingsTab() {
      this.switchTab(PanelScreen.Tab.SETTINGS);
   }

   public boolean method_25421() {
      return false;
   }

   protected void method_25426() {
      this.layout();
      if (tab != PanelScreen.Tab.MODULES) {
         this.pane.show(tabModule(tab));
      } else if (this.selected != null) {
         this.pane.show(this.selected);
      }
   }

   private void layout() {
      ClickGui options = options();
      float most = Math.max(0.5F, Math.min((float)this.field_22789 / 388.0F, (float)this.field_22790 / 258.0F));
      this.z = Math.max(0.5F, Math.min((float)options.panelScale.get().intValue() / 100.0F, most));
      this.vw = Math.round((float)this.field_22789 / this.z);
      this.vh = Math.round((float)this.field_22790 / this.z);
      this.ww = Math.clamp((long)Math.round((float)(this.vw * options.panelWidth.get()) / 100.0F), Math.min(380, this.vw - 8), this.vw - 8);
      this.wh = Math.clamp((long)Math.round((float)(this.vh * options.panelHeight.get()) / 100.0F), Math.min(250, this.vh - 8), this.vh - 8);
      this.wx = (this.vw - this.ww) / 2;
      this.wy = (this.vh - this.wh) / 2;
   }

   public void method_25419() {
      if (!this.closing) {
         this.pane.commit();
         this.binding = null;
         this.closing = true;
         UiSounds.close();
      }
   }

   public void method_25432() {
      SoberClient.CONFIG.save();
   }

   private static Module tabModule(PanelScreen.Tab tab) {
      return (Module)(switch (tab) {
         case MODULES -> null;
         case HUD -> (Hud)SoberClient.MODULES.get(Hud.class);
         case CONFIGS -> (Configs)SoberClient.MODULES.get(Configs.class);
         case SETTINGS -> (ClickGui)SoberClient.MODULES.get(ClickGui.class);
      });
   }

   private void switchTab(PanelScreen.Tab next) {
      tab = next;
      this.selected = null;
      this.binding = null;
      this.scroll = this.scrollTarget = 0.0F;
      this.pane.show(tabModule(next));
   }

   private float anim(String tag, Object target, boolean on, float speed) {
      PanelScreen.Key key = new PanelScreen.Key(tag, target);
      float value = MenuKit.approach(this.animations.getOrDefault(key, on ? 1.0F : 0.0F), on ? 1.0F : 0.0F, speed, this.delta);
      this.animations.put(key, value);
      return value;
   }

   private int c(int color) {
      return MenuKit.alpha(color, this.alpha);
   }

   private void text(class_332 context, String value, String font, float x, float baseline, float em, int color) {
      CrispText.draw(context, value, font, x, baseline, em, 0.0F, this.c(color));
   }

   private static float width(String value, String font, float em) {
      return CrispText.width(value, font, em, 0.0F);
   }

   private static String fit(String value, String font, float max, float em) {
      if (value != null && !(max < 8.0F)) {
         if (width(value, font, em) <= max) {
            return value;
         } else {
            String cut = value;

            while (cut.length() > 1 && width(cut + "...", font, em) > max) {
               cut = cut.substring(0, cut.length() - 1);
            }

            return cut.stripTrailing() + "...";
         }
      } else {
         return "";
      }
   }

   private void hit(String kind, float x, float y, float w, float h, Object target) {
      this.hits.add(new PanelScreen.Hit(kind, x, y, w, h, target));
   }

   private void toggle(class_332 context, float x, float y, float on) {
      int ix = Math.round(x);
      int iy = Math.round(y);
      Smooth.rect(context, ix, iy, 26, 14, 7, this.c(MenuKit.lerp(787805429, -723723, on)));
      Smooth.rect(context, ix + 2 + Math.round(12.0F * on), iy + 2, 10, 10, 5, this.c(MenuKit.lerp(-4934468, -15856112, on)));
   }

   private void lock(class_332 context, float cx, float cy, float size, int color) {
      int bw = Math.round(size);
      int bh = Math.round(size * 0.68F);
      int bx = Math.round(cx - (float)bw / 2.0F);
      int by = Math.round(cy - (float)bh / 2.0F + size * 0.2F);
      Smooth.rect(context, bx, by, bw, bh, 1, this.c(color));
      int n = 11;
      float r = size * 0.3F;
      float top = (float)by - 0.5F;
      float[] xs = new float[n + 2];
      float[] ys = new float[n + 2];
      float[] ws = new float[n + 2];
      int[] cs = new int[n + 2];
      xs[0] = cx - r;
      ys[0] = (float)by + 1.0F;

      for (int i = 0; i < n; i++) {
         double a = 3.141592653589793 + 3.141592653589793 * (double)i / (double)(n - 1);
         xs[i + 1] = cx + (float)Math.cos(a) * r;
         ys[i + 1] = top + (float)Math.sin(a) * r;
      }

      xs[n + 1] = cx + r;
      ys[n + 1] = (float)by + 1.0F;

      for (int i = 0; i < n + 2; i++) {
         ws[i] = Math.max(1.0F, size * 0.14F);
         cs[i] = this.c(color);
      }

      Smooth.polyline(context, xs, ys, ws, cs, n + 2);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      if (this.field_22787.field_1687 == null) {
         VioletScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0F);
      } else {
         MenuKit.background(context, this.field_22787, this.field_22789, this.field_22790, this.alpha, true);
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      long now = System.nanoTime();
      this.delta = Math.min(0.06F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      this.open = MenuKit.approach(this.open, this.closing ? 0.0F : 1.0F, this.closing ? 16.0F : 12.0F, this.delta);
      if (this.closing && this.open < 0.03F) {
         this.field_22787.method_1507(null);
      } else {
         this.alpha = 1.0F - (float)Math.pow((double)(1.0F - this.open), 3.0);
         super.method_25394(context, mouseX, mouseY, deltaTicks);
         this.hits.clear();
         this.tooltip = null;
         this.layout();
         context.method_51448().pushMatrix();
         context.method_51448().scale(this.z, this.z);
         int mx = Math.round((float)mouseX / this.z);
         int my = Math.round((float)mouseY / this.z);
         Smooth.rect(context, this.wx, this.wy, this.ww, this.wh, 4, this.c(Theme.opacity(-183628272)));
         Smooth.outline(context, this.wx, this.wy, this.ww, this.wh, 4, this.c(-14013906));
         float markH = (float)this.wh * 0.46F;
         Mono.watermark(
            context,
            (float)(this.wx + this.ww) - 40.0F - Mono.watermarkWidth(markH),
            (float)(this.wy + this.wh) - markH - 16.0F,
            markH,
            Math.round(13.0F * this.alpha)
         );
         this.drawTop(context, mx, my);
         context.method_25294(this.wx + 1, this.wy + 30, this.wx + this.ww - 1, this.wy + 30 + 1, this.c(-14013906));
         if (tab == PanelScreen.Tab.MODULES) {
            this.drawModules(context, mx, my);
         } else {
            this.drawTabSettings(context, mx, my);
         }

         this.drawGrip(context, mx, my);
         String tip = this.pane.tooltip();
         if (tip == null || tip.isEmpty()) {
            tip = this.tooltip;
         }

         if (tip != null && !tip.isEmpty()) {
            MenuKit.tooltip(context, tip, mx, my, this.vw, this.vh, this.alpha);
         }

         context.method_51448().popMatrix();
      }
   }

   private boolean overGrip(double mx, double my) {
      return mx >= (double)(this.wx + this.ww - 12)
         && mx < (double)(this.wx + this.ww + 2)
         && my >= (double)(this.wy + this.wh - 12)
         && my < (double)(this.wy + this.wh + 2);
   }

   private void drawGrip(class_332 context, int mx, int my) {
      float hv = this.anim("grip", this, this.resizing || this.overGrip((double)mx, (double)my), 14.0F);
      int color = this.c(MenuKit.lerp(-10724252, -723723, hv));
      float right = (float)(this.wx + this.ww) - 3.0F;
      float bottom = (float)(this.wy + this.wh) - 3.0F;

      for (int i = 0; i < 3; i++) {
         float d = 3.0F + (float)i * 3.0F;
         Smooth.polyline(context, new float[]{right - d, right}, new float[]{bottom, bottom - d}, new float[]{1.0F, 1.0F}, new int[]{color, color}, 2);
      }

      if (this.overGrip((double)mx, (double)my) && !this.resizing) {
         this.tooltip = "Drag to resize";
      }
   }

   private void drawTop(class_332 context, int mouseX, int mouseY) {
      float lw = 22.0F / WORDMARK.aspect();
      WORDMARK.draw(context, (float)(this.wx + 10), (float)(this.wy + 4), lw, this.c(-1));
      float x = (float)(this.wx + 12) + lw + 14.0F;

      for (PanelScreen.Tab t : PanelScreen.Tab.values()) {
         float w = width(t.label, "geist-semibold.ttf", 7.2F) + 18.0F;
         float h = 18.0F;
         float y = (float)(this.wy + 6);
         boolean over = (float)mouseX >= x && (float)mouseX < x + w && (float)mouseY >= y && (float)mouseY < y + h;
         float hv = this.anim("tab", t, over, 14.0F);
         float on = this.anim("tabOn", t, t == tab, 14.0F);
         Mono.box(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), Math.max(hv, on), this.alpha);
         if (on > 0.01F) {
            context.method_25294(Math.round(x + 6.0F), this.wy + 30 - 2, Math.round(x + w - 6.0F), this.wy + 30, this.c(MenuKit.alpha(-723723, on)));
         }

         this.text(context, t.label, "geist-semibold.ttf", x + 9.0F, y + 12.2F, 7.2F, MenuKit.lerp(-7434602, -723723, Math.max(on, hv * 0.6F)));
         this.hit("tab", x, y, w, h, t);
         x += w + 5.0F;
      }

      float cx = (float)(this.wx + this.ww - 22);
      boolean overClose = (float)mouseX >= cx - 4.0F && (float)mouseX < cx + 14.0F && mouseY >= this.wy + 6 && mouseY < this.wy + 24;
      Icons.draw(context, Icons.Icon.CLOSE, Math.round(cx), this.wy + 10, 9, this.c(overClose ? -723723 : -10724252));
      this.hit("close", cx - 4.0F, (float)(this.wy + 6), 18.0F, 18.0F, null);
      String style = "Dropdown";
      float sw = width(style, "geist-semibold.ttf", 7.2F) + 30.0F;
      float sx = cx - 10.0F - sw;
      float sy = (float)(this.wy + 6);
      boolean overStyle = (float)mouseX >= sx && (float)mouseX < sx + sw && (float)mouseY >= sy && (float)mouseY < sy + 18.0F;
      float hv = this.anim("style", style, overStyle, 14.0F);
      Mono.box(context, Math.round(sx), Math.round(sy), Math.round(sw), 18, hv, this.alpha);
      Icons.draw(context, Icons.Icon.COLUMNS, Math.round(sx + 8.0F), Math.round(sy + 5.0F), 8, this.c(MenuKit.lerp(-7434602, -723723, hv)));
      this.text(context, style, "geist-semibold.ttf", sx + 21.0F, sy + 12.2F, 7.2F, MenuKit.lerp(-4934468, -723723, hv));
      this.hit("style", sx, sy, sw, 18.0F, null);
      if (overStyle) {
         this.tooltip = "Switch to the Dropdown menu. Menu Style in Settings switches back.";
      }
   }

   private void drawModules(class_332 context, int mouseX, int mouseY) {
      int bodyY = this.wy + 30 + 1;
      float sy = (float)(bodyY + 10);

      for (Category cat : SIDEBAR) {
         float x = (float)(this.wx + 10);
         float w = 102.0F;
         float h = 22.0F;
         boolean over = (float)mouseX >= x && (float)mouseX < x + w && (float)mouseY >= sy && (float)mouseY < sy + h;
         boolean active = cat == category && this.search.isEmpty();
         float hv = this.anim("cat", cat, over, 14.0F);
         float on = this.anim("catOn", cat, active, 14.0F);
         Mono.box(context, Math.round(x), Math.round(sy), Math.round(w), Math.round(h), Math.max(hv, on), this.alpha);
         if (on > 0.01F) {
            Smooth.rect(context, Math.round(x + 1.0F), Math.round(sy + 5.0F), 2, Math.round(h - 10.0F), 1, this.c(MenuKit.alpha(-723723, on)));
         }

         int color = MenuKit.lerp(-4934468, -723723, Math.max(on, hv * 0.6F));
         Icons.draw(context, MenuKit.icon(cat), Math.round(x + 9.0F), Math.round(sy + 6.0F), 10, this.c(color));
         String title = cat == Category.MOVEMENT ? "AC Off" : cat.title;
         this.text(context, title, "geist-semibold.ttf", x + 25.0F, sy + 14.4F, 7.2F, color);
         if (cat == Category.MOVEMENT && !acUnlocked) {
            this.lock(context, x + w - 11.0F, sy + h / 2.0F - 1.5F, 6.5F, -7434602);
         } else {
            String n = Integer.toString(count(cat));
            this.text(context, n, "geist-medium.ttf", x + w - 8.0F - width(n, "geist-medium.ttf", 6.4F), sy + 14.2F, 6.4F, -10724252);
         }

         this.hit("cat", x, sy, w, h, cat);
         sy += h + 5.0F;
      }

      context.method_25294(this.wx + 122, bodyY, this.wx + 122 + 1, this.wy + this.wh - 1, this.c(-14013906));
      int cx = this.wx + 122 + 11;
      int cw = this.ww - 122 - 22;
      int gw = this.selected == null ? cw : Math.round((float)cw * 0.5F - 5.0F);
      int searchW = Math.min(170, gw);
      Mono.box(context, cx, bodyY + 10, searchW, 18, 0.0F, this.alpha);
      Icons.draw(context, Icons.Icon.SEARCH, cx + 7, bodyY + 15, 9, this.c(-10724252));
      boolean typing = !this.search.isEmpty();
      String shown = typing
         ? fit(this.search, "geist-medium.ttf", (float)(searchW - 30), 6.8F) + (System.currentTimeMillis() / 500L % 2L == 0L ? "_" : "")
         : fit("Search modules", "geist-medium.ttf", (float)(searchW - 26), 6.8F);
      this.text(context, shown, "geist-medium.ttf", (float)(cx + 21), (float)bodyY + 22.2F, 6.8F, typing ? -723723 : -10724252);
      List<Module> modules = this.modules();
      String count = modules.size() + (modules.size() == 1 ? " module" : " modules");
      float countW = width(count, "geist-medium.ttf", 6.4F);
      if ((float)(gw - searchW) >= countW + 10.0F) {
         this.text(context, count, "geist-medium.ttf", (float)(cx + gw) - countW, (float)bodyY + 21.5F, 6.4F, -10724252);
      }

      boolean locked = category == Category.MOVEMENT && !acUnlocked && this.search.isEmpty();
      this.gridX = cx;
      this.gridY = bodyY + 36;
      this.gridW = gw;
      this.gridH = this.wy + this.wh - 10 - 16 - this.gridY;
      String hint = fit("Left click toggles  ·  Right click opens settings  ·  Middle click binds a key", "geist-medium.ttf", (float)gw, 6.4F);
      this.text(context, hint, "geist-medium.ttf", (float)cx, (float)(this.wy + this.wh - 10), 6.4F, -10724252);
      if (locked) {
         this.drawLock(context, cx, cw, mouseX, mouseY);
      } else {
         this.drawGrid(context, modules, mouseX, mouseY);
         if (this.selected != null) {
            this.drawDrawer(context, cx + gw + 10, bodyY + 10, cw - gw - 10, this.wy + this.wh - 10 - (bodyY + 10), mouseX, mouseY);
         }
      }
   }

   private List<Module> modules() {
      List<Module> list = new ArrayList<>();
      String query = this.search.toLowerCase(Locale.ROOT).trim();

      for (Module module : SoberClient.MODULES.all()) {
         if (!query.isEmpty()) {
            if (module.name.toLowerCase(Locale.ROOT).contains(query)) {
               list.add(module);
            }
         } else if (module.category == category) {
            list.add(module);
         }
      }

      return list;
   }

   private static int count(Category cat) {
      int n = 0;

      for (Module module : SoberClient.MODULES.all()) {
         if (module.category == cat) {
            n++;
         }
      }

      return n;
   }

   private void drawGrid(class_332 context, List<Module> modules, int mouseX, int mouseY) {
      int cols = this.selected == null ? 2 : 1;
      float colW = (float)(this.gridW - 5 * (cols - 1)) / (float)cols;
      int rows = (modules.size() + cols - 1) / cols;
      this.content = (float)(rows * 31 - 5);
      this.view = (float)this.gridH;
      this.scrollTarget = Math.max(0.0F, Math.min(this.scrollTarget, Math.max(0.0F, this.content - this.view)));
      this.scroll = MenuKit.approach(this.scroll, this.scrollTarget, 16.0F, this.delta);
      if (modules.isEmpty()) {
         this.text(context, "No modules found", "geist-medium.ttf", (float)(this.gridX + 2), (float)(this.gridY + 12), 7.0F, -7434602);
      } else {
         boolean insideGrid = mouseX >= this.gridX && mouseX < this.gridX + this.gridW && mouseY >= this.gridY && mouseY < this.gridY + this.gridH;
         context.method_44379(this.gridX - 2, this.gridY - 2, this.gridX + this.gridW + 2, this.gridY + this.gridH);

         for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            float x = (float)this.gridX + (float)(i % cols) * (colW + 5.0F);
            float y = (float)(this.gridY + i / cols * 31) - this.scroll;
            if (!(y + 26.0F < (float)this.gridY) && !(y > (float)(this.gridY + this.gridH))) {
               boolean over = insideGrid && (float)mouseX >= x && (float)mouseX < x + colW && (float)mouseY >= y && (float)mouseY < y + 26.0F;
               this.drawCard(context, module, x, y, colW, over);
               float top = Math.max(y, (float)this.gridY);
               float bottom = Math.min(y + 26.0F, (float)(this.gridY + this.gridH));
               if (bottom > top) {
                  this.hit("card", x, top, colW, bottom - top, module);
               }
            }
         }

         context.method_44380();
         if (this.content > this.view + 1.0F) {
            float track = (float)this.gridH - 4.0F;
            float thumb = Math.max(18.0F, track * this.view / this.content);
            float ty = (float)this.gridY + 2.0F + (track - thumb) * (this.scroll / Math.max(1.0F, this.content - this.view));
            Smooth.rect(context, this.gridX + this.gridW + 3, Math.round(ty), 2, Math.round(thumb), 1, this.c(-12237491));
         }
      }
   }

   private void drawCard(class_332 context, Module module, float x, float y, float w, boolean over) {
      float hv = this.anim("card", module, over, 14.0F);
      float on = this.anim("on", module, module.isActive(), 14.0F);
      int ix = Math.round(x);
      int iy = Math.round(y);
      int iw = Math.round(w);
      Mono.box(context, ix, iy, iw, 26, hv, this.alpha);
      if (module == this.selected) {
         Smooth.outline(context, ix, iy, iw, 26, 3, this.c(-789252875));
      }

      if (on > 0.01F) {
         Smooth.rect(context, ix + 1, iy + 6, 2, 14, 1, this.c(MenuKit.alpha(-723723, on)));
      }

      float toggleX = x + w - 36.0F;
      this.toggle(context, toggleX, y + 6.0F, on);
      float right = toggleX - 8.0F;
      boolean bindingThis = module == this.binding;
      if (bindingThis || module.getKey() >= 0) {
         String key = bindingThis ? "..." : MenuKit.keyName(module.getKey()).toUpperCase(Locale.ROOT);
         float kw = width(key, "geist-semibold.ttf", 6.4F) + 10.0F;
         Smooth.rect(context, Math.round(right - kw), iy + 7, Math.round(kw), 12, 2, this.c(bindingThis ? 821359861 : 318043381));
         Smooth.outline(context, Math.round(right - kw), iy + 7, Math.round(kw), 12, 2, this.c(-14013906));
         this.text(context, key, "geist-semibold.ttf", right - kw + 5.0F, (float)iy + 15.6F, 6.4F, bindingThis ? -723723 : -7434602);
         right -= kw + 8.0F;
      }

      String name = Theme.label(module.name);
      float nameMax = Math.max(30.0F, right - x - 12.0F);
      String shownName = fit(name, "geist-semibold.ttf", nameMax, 7.6F);
      this.text(context, shownName, "geist-semibold.ttf", x + 10.0F, y + 16.6F, 7.6F, MenuKit.lerp(-2829096, -723723, Math.max(on, hv)));
      float descX = x + 10.0F + width(shownName, "geist-semibold.ttf", 7.6F) + 9.0F;
      String description = fit(module.description, "geist-medium.ttf", right - descX, 6.4F);
      if (!description.isEmpty()) {
         this.text(context, description, "geist-medium.ttf", descX, y + 16.2F, 6.4F, -10724252);
      }
   }

   private void drawDrawer(class_332 context, int x, int y, int w, int h, int mouseX, int mouseY) {
      Mono.box(context, x, y, w, h, 0.0F, this.alpha);
      this.text(
         context,
         fit(Theme.label(this.selected.name), "geist-semibold.ttf", (float)(w - 40), 8.6F),
         "geist-semibold.ttf",
         (float)(x + 10),
         (float)y + 17.0F,
         8.6F,
         -723723
      );
      this.text(
         context,
         fit(this.selected.description, "geist-medium.ttf", (float)(w - 20), 6.4F),
         "geist-medium.ttf",
         (float)(x + 10),
         (float)y + 29.0F,
         6.4F,
         -10724252
      );
      float cx = (float)(x + w - 18);
      boolean over = (float)mouseX >= cx - 4.0F && (float)mouseX < cx + 14.0F && mouseY >= y + 6 && mouseY < y + 22;
      Icons.draw(context, Icons.Icon.CLOSE, Math.round(cx), y + 9, 8, this.c(over ? -723723 : -10724252));
      this.hit("drawerClose", cx - 4.0F, (float)(y + 6), 18.0F, 16.0F, null);
      context.method_25294(x + 1, y + 37, x + w - 1, y + 38, this.c(-14013906));
      this.pane.render(context, x + 10, y + 44, w - 22, h - 50, mouseX, mouseY, this.alpha, this.delta);
   }

   private void drawLock(class_332 context, int cx, int cw, int mouseX, int mouseY) {
      float mid = (float)cx + (float)cw / 2.0F;
      float top = (float)this.gridY + Math.max(16.0F, (float)this.gridH * 0.22F);
      this.lock(context, mid, top, 13.0F, -723723);
      String title = "AC Off";
      this.text(context, title, "geist-semibold.ttf", mid - width(title, "geist-semibold.ttf", 10.0F) / 2.0F, top + 26.0F, 10.0F, -723723);
      String[] lines = new String[]{"Anticheats can easily spot these modules.", "Only use them on servers where the anticheat is off."};

      for (int i = 0; i < lines.length; i++) {
         this.text(context, lines[i], "geist-medium.ttf", mid - width(lines[i], "geist-medium.ttf", 6.8F) / 2.0F, top + 42.0F + (float)(i * 11), 6.8F, -7434602);
      }

      float bw = 96.0F;
      float bh = 20.0F;
      float by = top + 70.0F;
      float gap = 8.0F;
      float ax = mid - bw - gap / 2.0F;
      float bx = mid + gap / 2.0F;
      boolean overCancel = (float)mouseX >= ax && (float)mouseX < ax + bw && (float)mouseY >= by && (float)mouseY < by + bh;
      boolean overOpen = (float)mouseX >= bx && (float)mouseX < bx + bw && (float)mouseY >= by && (float)mouseY < by + bh;
      Mono.box(context, Math.round(ax), Math.round(by), Math.round(bw), Math.round(bh), this.anim("lockCancel", this, overCancel, 14.0F), this.alpha);
      Mono.primary(context, Math.round(bx), Math.round(by), Math.round(bw), Math.round(bh), this.anim("lockOpen", this, overOpen, 14.0F), this.alpha);
      String cancel = "Cancel";
      String openLabel = "Show modules";
      this.text(context, cancel, "geist-semibold.ttf", ax + (bw - width(cancel, "geist-semibold.ttf", 7.2F)) / 2.0F, by + 13.2F, 7.2F, -723723);
      this.text(context, openLabel, "geist-semibold.ttf", bx + (bw - width(openLabel, "geist-semibold.ttf", 7.2F)) / 2.0F, by + 13.2F, 7.2F, -15856112);
      this.hit("lockCancel", ax, by, bw, bh, null);
      this.hit("lockOpen", bx, by, bw, bh, null);
   }

   private void drawTabSettings(class_332 context, int mouseX, int mouseY) {
      Module module = tabModule(tab);
      if (module != null) {
         int bodyY = this.wy + 30 + 1;
         int pw = Math.min(this.ww - 60, 440);
         int px = this.wx + (this.ww - pw) / 2;

         String title = switch (tab) {
            case HUD -> "HUD";
            case CONFIGS -> "Configs";
            default -> "Settings";
         };

         String sub = switch (tab) {
            case HUD -> "What your HUD shows. Open the chat to move and resize the cards.";
            case CONFIGS -> "Save, load and share your settings.";
            default -> "Look, fonts, sounds and keys of the menu. Panel Width, Height and Scale change the size of this window.";
         };
         this.text(context, title, "geist-semibold.ttf", (float)px, (float)bodyY + 22.0F, 10.0F, -723723);
         this.text(context, fit(sub, "geist-medium.ttf", (float)pw, 6.8F), "geist-medium.ttf", (float)px, (float)bodyY + 35.0F, 6.8F, -7434602);
         context.method_25294(px, bodyY + 43, px + pw, bodyY + 44, this.c(-14013906));
         this.pane.render(context, px, bodyY + 52, pw, this.wy + this.wh - 10 - (bodyY + 52), mouseX, mouseY, this.alpha, this.delta);
      }
   }

   private PanelScreen.Hit hitAt(double mx, double my) {
      for (int i = this.hits.size() - 1; i >= 0; i--) {
         if (this.hits.get(i).contains(mx, my)) {
            return this.hits.get(i);
         }
      }

      return null;
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (this.closing) {
         return true;
      } else {
         double mx = click.comp_4798() / (double)this.z;
         double my = click.comp_4799() / (double)this.z;
         int button = click.method_74245();
         if (button == 0 && this.overGrip(mx, my)) {
            this.resizing = true;
            this.resizeX = mx;
            this.resizeY = my;
            this.resizeW = this.ww;
            this.resizeH = this.wh;
            return true;
         } else if (this.binding != null) {
            this.binding = null;
            return true;
         } else {
            boolean paneShown = tab != PanelScreen.Tab.MODULES || this.selected != null;
            if (paneShown && this.pane.click(mx, my, button, doubled)) {
               return true;
            } else {
               PanelScreen.Hit hit = this.hitAt(mx, my);
               if (hit == null) {
                  return true;
               } else {
                  String var10 = hit.kind();
                  switch (var10) {
                     case "tab":
                        if (hit.target() != tab) {
                           this.switchTab((PanelScreen.Tab)hit.target());
                        }

                        UiSounds.click();
                        break;
                     case "close":
                        this.method_25419();
                        break;
                     case "style":
                        options().style.set(ClickGui.Style.Dropdown);
                        SoberClient.CONFIG.save();
                        UiSounds.click();
                        this.pane.commit();
                        this.field_22787.method_1507(new DropdownScreen());
                        break;
                     case "cat":
                        category = (Category)hit.target();
                        this.search = "";
                        this.selected = null;
                        this.pane.show(null);
                        this.scroll = this.scrollTarget = 0.0F;
                        UiSounds.click();
                        break;
                     case "card":
                        this.clickCard((Module)hit.target(), button);
                        break;
                     case "drawerClose":
                        this.selected = null;
                        this.pane.show(null);
                        UiSounds.click();
                        break;
                     case "lockCancel":
                        category = Category.BASE_FINDING;
                        UiSounds.click();
                        break;
                     case "lockOpen":
                        acUnlocked = true;
                        UiSounds.click();
                  }

                  return true;
               }
            }
         }
      }
   }

   private void clickCard(Module module, int button) {
      if (button == 2) {
         this.binding = module.bindable() ? module : null;
         UiSounds.click();
      } else if (button != 1 && !MenuKit.opensMenu(module)) {
         module.toggle();
         MenuKit.touch(module);
         UiSounds.moduleToggle(module.isActive());
         SoberClient.CONFIG.save();
      } else {
         this.selected = this.selected == module ? null : module;
         this.pane.show(this.selected);
         UiSounds.click();
      }
   }

   public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
      if (this.resizing) {
         double mx = click.comp_4798() / (double)this.z;
         double my = click.comp_4799() / (double)this.z;
         int w = (int)Math.round((double)this.resizeW + (mx - this.resizeX) * 2.0);
         int h = (int)Math.round((double)this.resizeH + (my - this.resizeY) * 2.0);
         options().panelWidth.set(Math.round((float)Math.clamp((long)w, 380, this.vw - 8) * 100.0F / (float)this.vw));
         options().panelHeight.set(Math.round((float)Math.clamp((long)h, 250, this.vh - 8) * 100.0F / (float)this.vh));
         return true;
      } else {
         return this.pane.drag(click.comp_4798() / (double)this.z, click.comp_4799() / (double)this.z) || super.method_25403(click, offsetX, offsetY);
      }
   }

   public boolean method_25406(class_11909 click) {
      if (this.resizing) {
         this.resizing = false;
         SoberClient.CONFIG.save();
         return true;
      } else {
         return this.pane.release() || super.method_25406(click);
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      boolean ctrl = class_3675.method_15987(this.field_22787.method_22683(), 341) || class_3675.method_15987(this.field_22787.method_22683(), 345);
      boolean shift = class_3675.method_15987(this.field_22787.method_22683(), 340);
      mouseX /= (double)this.z;
      mouseY /= (double)this.z;
      if ((tab != PanelScreen.Tab.MODULES || this.selected != null) && this.pane.scroll(mouseX, mouseY, vertical, ctrl, shift)) {
         return true;
      } else if (ctrl && vertical != 0.0) {
         options().panelScale.set(options().panelScale.get() + (vertical > 0.0 ? 10 : -10));
         SoberClient.CONFIG.save();
         return true;
      } else {
         if (tab == PanelScreen.Tab.MODULES
            && mouseX >= (double)this.gridX
            && mouseX < (double)(this.gridX + this.gridW + 6)
            && mouseY >= (double)this.gridY
            && mouseY < (double)(this.gridY + this.gridH)) {
            this.scrollTarget = Math.max(0.0F, Math.min(Math.max(0.0F, this.content - this.view), this.scrollTarget - (float)vertical * 31.0F));
         }

         return true;
      }
   }

   public boolean method_25404(class_11908 input) {
      if (this.closing) {
         return true;
      } else {
         int key = input.comp_4795();
         if (this.binding == null) {
            if (this.pane.key(input)) {
               return true;
            } else if (key == 259 && !this.search.isEmpty()) {
               this.search = this.search.substring(0, this.search.length() - 1);
               UiSounds.key(0.7F);
               return true;
            } else if (key == 256) {
               if (!this.search.isEmpty()) {
                  this.search = "";
               } else if (this.selected != null) {
                  this.selected = null;
                  this.pane.show(null);
               } else {
                  this.method_25419();
               }

               return true;
            } else if (key == options().getKey() && this.search.isEmpty() && !this.pane.typing()) {
               this.method_25419();
               return true;
            } else {
               return super.method_25404(input);
            }
         } else {
            this.binding.setKey(key != 256 && key != 259 && key != 261 ? key : -1);
            UiSounds.keybind(this.binding.getKey() >= 0);
            this.binding = null;
            this.swallowUntil = System.currentTimeMillis() + 150L;
            SoberClient.CONFIG.save();
            return true;
         }
      }
   }

   public boolean method_25400(class_11905 input) {
      if (!this.closing && input.method_74227()) {
         if (System.currentTimeMillis() < this.swallowUntil) {
            this.swallowUntil = 0L;
            return true;
         } else if (this.pane.typed(input)) {
            return true;
         } else if (tab != PanelScreen.Tab.MODULES) {
            return true;
         } else {
            String typed = input.method_74226();
            if (this.search.isEmpty() && typed.isBlank()) {
               return true;
            } else {
               if (this.search.length() < 40) {
                  this.search = this.search + typed;
                  this.selected = null;
                  this.pane.show(null);
                  this.scroll = this.scrollTarget = 0.0F;
                  UiSounds.key(1.0F);
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }

   private static record Hit(String kind, float x, float y, float w, float h, Object target) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
      }
   }

   private static record Key(String tag, Object target) {
   }

   private static enum Tab {
      MODULES("Modules"),
      HUD("HUD"),
      CONFIGS("Configs"),
      SETTINGS("Settings");

      final String label;

      private Tab(String label) {
         this.label = label;
      }
   }
}
