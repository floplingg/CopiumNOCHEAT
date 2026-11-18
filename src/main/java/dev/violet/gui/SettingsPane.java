package dev.violet.gui;

import dev.violet.ConfigSlots;
import dev.violet.SoberClient;
import dev.violet.module.Module;
import dev.violet.module.Status;
import dev.violet.modules.Configs;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

final class SettingsPane {
   private static final float NAME = 0.8F;
   private static final float VALUE = 0.74F;
   private static final float SMALL = 0.62F;
   private static final int PICKER_H = 76;
   private static final Set<ColorSetting> OPEN_COLORS = new HashSet<>();
   private final class_437 host;
   private final class_310 client = class_310.method_1551();
   private final List<SettingsPane.Hit> hits = new ArrayList<>();
   private final Map<SettingsPane.Key, Float> animations = new HashMap<>();
   private final Map<Object, Long> pressedAt = new HashMap<>();
   private Module module;
   private boolean preview = true;
   private float scroll;
   private float scrollTarget;
   private float content;
   private float view;
   private int boxX;
   private int boxY;
   private int boxW;
   private int boxH;
   private float opacity = 1.0F;
   private float delta;
   private SettingsPane.Hit dragging;
   private Setting<?> editing;
   private String editBuffer = "";
   private Setting<?> invalidSetting;
   private float hexX;
   private float hexW;
   private long invalidUntil;
   private boolean binding;
   private KeySetting keyBinding;
   private long swallowUntil;
   private boolean renaming;
   private String renameBuffer = "";
   private boolean typingCode;
   private String codeText = "";
   private Setting<?> focused;
   private long focusedAt;
   private float focusOffset = -1.0F;
   private String tooltip;
   private Object hoverKey;
   static final SettingsPane.Palette DARK = new SettingsPane.Palette(
      true, -855051, -4012338, -6906969, 385875967, 687865855, 620756991, 1023410175, 654311423, 251658239, -855051, 1023410175, 0, true, 0
   );
   private SettingsPane.Palette pal = DARK;
   private static final String CONFIG_NOTE = "Configs only hold visual settings, so imported configs are always safe to try.";

   private int accent() {
      return this.pal.accent() != 0 ? this.pal.accent() : MenuKit.accent();
   }

   void palette(SettingsPane.Palette palette) {
      this.pal = palette;
   }

   private void well(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
      Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hover), opacity));
      Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(this.pal.line(), this.pal.lineHover(), hover), opacity));
   }

   private void toggle(class_332 context, float x, float y, float w, float h, float on) {
      int ix = Math.round(x);
      int iy = Math.round(y);
      int iw = Math.round(w);
      int ih = Math.round(h);
      int onColor = this.pal.toggleOn() != 0 ? this.pal.toggleOn() : this.accent();
      if (this.pal.glow() && on > 0.01F) {
         Smooth.glow(context, ix + iw / 2, iy + ih / 2, Math.round((float)iw * 0.75F), this.c(MenuKit.withAlpha(onColor, Math.round(85.0F * on))));
      }

      Smooth.rect(context, ix, iy, iw, ih, ih / 2, this.c(MenuKit.lerp(this.pal.toggleOff(), onColor, on)));
      if (this.pal.dark() && on < 0.99F) {
         Smooth.outline(context, ix, iy, iw, ih, ih / 2, this.c(MenuKit.alpha(587202559, 1.0F - on)));
      }

      int knob = ih - 4;
      int kx = Math.round((float)(ix + 2) + (float)(iw - knob - 4) * on);
      int luma = ((onColor >> 16 & 0xFF) * 3 + (onColor >> 8 & 0xFF) * 6 + (onColor & 0xFF)) / 10;
      Smooth.rect(context, kx, iy + 2, knob, knob, knob / 2, this.c(MenuKit.lerp(-1, luma > 205 ? -15395042 : -1, on)));
   }

   SettingsPane(class_437 host) {
      this.host = host;
   }

   Module module() {
      return this.module;
   }

   void show(Module next) {
      if (next != this.module) {
         this.commit();
         this.module = next;
         this.scroll = 0.0F;
         this.scrollTarget = 0.0F;
         this.focused = null;
         this.focusOffset = -1.0F;
         this.binding = false;
         this.keyBinding = null;
      }
   }

   void preview(boolean show) {
      this.preview = show;
   }

   void bindNow() {
      if (this.module != null && this.module.bindable()) {
         this.binding = true;
      }
   }

   boolean binding() {
      return this.binding;
   }

   void focus(Setting<?> setting) {
      this.focused = setting;
      this.focusedAt = System.currentTimeMillis();
      this.focusOffset = -1.0F;
   }

   String tooltip() {
      return this.tooltip;
   }

   Object hoverKey() {
      return this.hoverKey;
   }

   boolean typing() {
      return this.editing != null || this.renaming || this.typingCode || this.binding || this.keyBinding != null;
   }

   boolean contains(double mx, double my) {
      return mx >= (double)this.boxX && my >= (double)this.boxY && mx < (double)(this.boxX + this.boxW) && my < (double)(this.boxY + this.boxH);
   }

   float contentHeight() {
      return this.content;
   }

   private float anim(String tag, Object target, boolean on, float speed) {
      SettingsPane.Key key = new SettingsPane.Key(tag, target);
      float value = MenuKit.approach(this.animations.getOrDefault(key, on ? 1.0F : 0.0F), on ? 1.0F : 0.0F, speed, this.delta);
      this.animations.put(key, value);
      return value;
   }

   private float press(Object key) {
      Long time = this.pressedAt.get(key);
      return time == null ? 0.0F : Math.max(0.0F, 1.0F - (float)(System.nanoTime() - time) / 2.1E8F);
   }

   private int c(int color) {
      return MenuKit.alpha(color, this.opacity);
   }

   private void text(class_332 context, String value, float x, float y, float size, int color, boolean heavy) {
      MenuKit.text(context, value, x, y, size, this.c(color), heavy);
   }

   private void rect(class_332 context, float x, float y, float w, float h, int r, int color) {
      Smooth.rect(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.c(color));
   }

   private void outline(class_332 context, float x, float y, float w, float h, int r, int color) {
      Smooth.outline(context, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, this.c(color));
   }

   private void hit(SettingsPane.Kind kind, float x, float y, float w, float h, Object target, int index) {
      float top = (float)this.boxY;
      float bottom = (float)(this.boxY + this.boxH);
      float y1 = Math.max(y, top);
      float y2 = Math.min(y + h, bottom);
      if (y2 > y1) {
         this.hits.add(new SettingsPane.Hit(kind, x, y1, w, y2 - y1, target, index));
      }
   }

   private SettingsPane.Hit hitAt(double mx, double my) {
      for (int i = this.hits.size() - 1; i >= 0; i--) {
         if (this.hits.get(i).contains(mx, my)) {
            return this.hits.get(i);
         }
      }

      return null;
   }

   private void hover(Object key, String description) {
      this.hoverKey = key;
      this.tooltip = description;
   }

   private List<Setting<?>> visibleSettings() {
      List<Setting<?>> list = new ArrayList<>();
      if (this.module == null) {
         return list;
      } else {
         for (Setting<?> setting : this.module.getSettings()) {
            if (setting.isVisible()) {
               list.add(setting);
            }
         }

         return list;
      }
   }

   private float rowHeight(Setting<?> setting) {
      float height = setting instanceof IntSetting ? 30.0F : 22.0F;
      if (setting instanceof ColorSetting color) {
         height += 76.0F * this.animations.getOrDefault(new SettingsPane.Key("picker", color), 0.0F);
      }

      return height;
   }

   private boolean grouped(List<Setting<?>> settings) {
      String first = null;

      for (Setting<?> setting : settings) {
         if (first == null) {
            first = setting.getGroup();
         } else if (!first.equals(setting.getGroup())) {
            return true;
         }
      }

      return false;
   }

   void render(class_332 context, int x, int y, int w, int h, int mouseX, int mouseY, float opacity, float delta) {
      this.opacity = opacity;
      this.delta = delta;
      this.boxX = x;
      this.boxY = y;
      this.boxW = w;
      this.boxH = h;
      this.hits.clear();
      this.tooltip = null;
      this.hoverKey = null;
      if (this.module != null && !(opacity <= 0.01F)) {
         this.view = (float)h;
         boolean inside = this.contains((double)mouseX, (double)mouseY);
         int mx = inside ? mouseX : -10000;
         int my = inside ? mouseY : -10000;
         List<Setting<?>> settings = this.visibleSettings();
         boolean groups = this.grouped(settings);
         boolean configs = this.module instanceof Configs;
         List<Status.Row> status = this.module instanceof Status s ? s.statusRows() : List.of();
         int presets = configs ? 0 : MenuKit.presetCount(this.module);
         boolean showPreview = this.preview && ModulePreview.supports(this.module);
         float previewH = showPreview ? (float)Math.round((float)w * 0.5F) : 0.0F;
         float total = 0.0F;
         if (showPreview) {
            total += previewH + 10.0F;
         }

         if (configs) {
            total += this.configHeight(w);
         }

         if (!status.isEmpty()) {
            total += 16.0F + (float)status.size() * 32.0F + 6.0F;
         }

         if (presets > 0) {
            total += 16.0F + this.presetRowsHeight(w, presets) + 8.0F;
         }

         String group = null;
         float focusY = -1.0F;

         for (Setting<?> setting : settings) {
            if (groups && !setting.getGroup().equals(group)) {
               group = setting.getGroup();
               total += 18.0F;
            }

            if (setting == this.focused) {
               focusY = total;
            }

            total += this.rowHeight(setting);
         }

         if (this.module.bindable()) {
            total += 30.0F;
         }

         this.content = total;
         if (this.focused != null && focusY >= 0.0F && this.focusOffset < 0.0F) {
            this.focusOffset = focusY;
            this.scrollTarget = Math.max(0.0F, focusY - (float)h * 0.3F);
         }

         this.scrollTarget = Math.max(0.0F, Math.min(this.scrollTarget, Math.max(0.0F, this.content - this.view)));
         this.scroll = MenuKit.approach(this.scroll, this.scrollTarget, 16.0F, delta);
         context.method_44379(x - 6, y, x + w + 6, y + h);
         float sy = (float)y - this.scroll;
         if (showPreview) {
            if (sy + previewH >= (float)y && sy <= (float)(y + h)) {
               ModulePreview.draw(context, this.module, (float)x, sy, (float)w, previewH, opacity);
               String sub = ModulePreview.subtitle(this.module);
               String chip = sub.isEmpty() ? "LIVE" : "LIVE  ·  " + sub;
               MenuKit.chip(context, MenuKit.fit(chip, (float)(w - 24), 0.62F, true), (float)(x + 6), sy + previewH - 17.0F, 0.62F, opacity);
            }

            sy += previewH + 10.0F;
         }

         if (configs) {
            sy = this.drawConfigs(context, x, sy, w, mx, my);
         }

         if (!status.isEmpty() && this.module instanceof Status sx) {
            this.text(context, MenuKit.label(sx.statusTitle()), (float)(x + 2), sy + 3.0F, 0.62F, this.pal.faint(), true);
            sy += 16.0F;

            for (int i = 0; i < status.size(); i++) {
               Status.Row row = status.get(i);
               float ry = sy + (float)i * 32.0F;
               if (!(ry + 28.0F < (float)y) && !(ry > (float)(y + h))) {
                  boolean over = mx >= x && mx < x + w && (float)my >= ry && (float)my < ry + 28.0F;
                  float hv = this.anim("status", this.module.name + i, over, 12.0F);
                  this.well(context, x, Math.round(ry), w, 28, 7, hv, opacity);
                  float click = this.press("status" + i);
                  if (click > 0.0F) {
                     this.rect(context, (float)x, ry, (float)w, 28.0F, 7, MenuKit.withAlpha(this.accent(), Math.round(60.0F * click)));
                  }

                  float bw = MenuKit.width(row.badge(), 0.62F, true) + 10.0F;
                  this.rect(context, (float)(x + w - 6) - bw, ry + 8.0F, bw, 12.0F, 6, row.badgeColor());
                  this.text(context, row.badge(), (float)(x + w - 1) - bw, ry + 10.0F, 0.62F, -1, true);
                  this.text(context, MenuKit.fit(row.title(), (float)w - bw - 22.0F, 0.74F, true), (float)(x + 7), ry + 4.0F, 0.74F, this.pal.text(), true);
                  this.text(
                     context,
                     MenuKit.fit(row.detail(), (float)w - bw - 22.0F, 0.62F, false),
                     (float)(x + 7),
                     ry + 16.0F,
                     0.62F,
                     MenuKit.lerp(this.pal.muted(), this.accent(), hv),
                     false
                  );
                  this.hit(SettingsPane.Kind.STATUS, (float)x, ry, (float)w, 28.0F, row.link(), i);
                  if (over) {
                     this.hover("status" + i, sx.statusDescription());
                  }
               }
            }

            sy += (float)status.size() * 32.0F + 6.0F;
         }

         if (presets > 0) {
            this.text(context, "PRESETS", (float)(x + 2), sy + 3.0F, 0.62F, this.pal.faint(), true);
            sy += 16.0F;
            sy = this.drawPresets(context, x, sy, w, presets, mx, my) + 8.0F;
         }

         group = null;

         for (Setting<?> setting : settings) {
            if (groups && !setting.getGroup().equals(group)) {
               group = setting.getGroup();
               if (sy + 18.0F >= (float)y && sy <= (float)(y + h)) {
                  this.text(context, MenuKit.label(group), (float)(x + 2), sy + 6.0F, 0.62F, this.pal.faint(), true);
               }

               sy += 18.0F;
            }

            float height = this.rowHeight(setting);
            if (sy + height >= (float)y && sy <= (float)(y + h)) {
               this.drawSetting(context, setting, x, sy, w, mx, my);
            }

            sy += height;
         }

         if (this.module.bindable()) {
            if (sy + 30.0F >= (float)y && sy <= (float)(y + h)) {
               this.drawBind(context, x, sy + 6.0F, w, mx, my);
            }

            sy += 30.0F;
         }

         if (settings.isEmpty() && presets == 0 && !configs && status.isEmpty() && !showPreview && !this.module.bindable()) {
            this.text(context, "No settings", (float)(x + 2), (float)(y + 6), 0.74F, this.pal.muted(), false);
         }

         context.method_44380();
         if (this.content > this.view + 1.0F) {
            float track = (float)h - 4.0F;
            float thumb = Math.max(18.0F, track * this.view / this.content);
            float ty = (float)y + 2.0F + (track - thumb) * (this.scroll / Math.max(1.0F, this.content - this.view));
            this.rect(context, (float)(x + w + 3), ty, 2.0F, thumb, 1, this.pal.lineHover());
         }
      }
   }

   private float presetRowsHeight(int w, int count) {
      float cx = 0.0F;
      float rows = 1.0F;

      for (int i = 0; i < count; i++) {
         float pw = Math.min((float)w, MenuKit.width(MenuKit.presetName(this.module, i), 0.74F, true) + 16.0F);
         if (cx > 0.0F && cx + pw > (float)w) {
            rows++;
            cx = 0.0F;
         }

         cx += pw + 4.0F;
      }

      return rows * 20.0F;
   }

   private float drawPresets(class_332 context, int x, float sy, int w, int count, int mx, int my) {
      float cx = 0.0F;
      float cy = 0.0F;
      int accent = this.accent();

      for (int i = 0; i < count; i++) {
         String name = MenuKit.presetName(this.module, i);
         float pw = Math.min((float)w, MenuKit.width(name, 0.74F, true) + 16.0F);
         if (cx > 0.0F && cx + pw > (float)w) {
            cx = 0.0F;
            cy += 20.0F;
         }

         float px = (float)x + cx;
         float py = sy + cy;
         boolean active = MenuKit.presetActive(this.module, i);
         boolean over = (float)mx >= px && (float)mx < px + pw && (float)my >= py && (float)my < py + 16.0F;
         float hv = this.anim("preset", this.module.name + i, over, 14.0F);
         float on = this.anim("presetOn", this.module.name + i, active, 14.0F);
         if (this.pal.glow() && on > 0.01F) {
            Smooth.glow(
               context, Math.round(px + pw / 2.0F), Math.round(py + 8.0F), Math.round(pw * 0.6F), this.c(MenuKit.withAlpha(accent, Math.round(64.0F * on)))
            );
         }

         this.rect(context, px, py, pw, 16.0F, 8, MenuKit.lerp(MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv), accent, on));
         if (on < 0.99F) {
            this.outline(context, px, py, pw, 16.0F, 8, MenuKit.alpha(this.pal.line(), 1.0F - on));
         }

         this.text(context, MenuKit.fit(name, pw - 12.0F, 0.74F, true), px + 8.0F, py + 4.5F, 0.74F, MenuKit.lerp(this.pal.text(), -15069650, on), true);
         this.hit(SettingsPane.Kind.PRESET, px, py, pw, 16.0F, this.module, i);
         if (over) {
            this.hover("preset" + i, MenuKit.presetDescription(this.module));
         }

         cx += pw + 4.0F;
      }

      return sy + cy + 20.0F;
   }

   private void drawSetting(class_332 context, Setting<?> setting, int x, float sy, int w, int mx, int my) {
      float picker = setting instanceof ColorSetting color ? this.anim("picker", color, OPEN_COLORS.contains(color), 13.0F) : 0.0F;
      float base = setting instanceof IntSetting ? 30.0F : 22.0F;
      float rowH = base + 76.0F * picker;
      boolean over = mx >= x - 4 && mx < x + w + 4 && (float)my >= sy && (float)my < sy + base;
      float hv = this.anim("setting", setting, over, 12.0F);
      if (hv > 0.01F || picker > 0.01F) {
         this.rect(context, (float)(x - 4), sy + 1.0F, (float)(w + 8), rowH - 2.0F, 7, MenuKit.alpha(this.pal.hover(), Math.max(hv, picker)));
      }

      float click = this.press(setting);
      if (click > 0.0F) {
         this.rect(context, (float)(x - 4), sy + 1.0F, (float)(w + 8), base - 2.0F, 7, MenuKit.withAlpha(this.accent(), Math.round(50.0F * click)));
      }

      if (setting == this.focused) {
         float age = (float)(System.currentTimeMillis() - this.focusedAt) / 1000.0F;
         float pulse = Math.max(0.0F, 1.0F - age / 1.6F);
         if (pulse > 0.0F) {
            this.rect(context, (float)(x - 4), sy + 1.0F, (float)(w + 8), base - 2.0F, 7, MenuKit.withAlpha(this.accent(), Math.round(70.0F * pulse)));
            this.outline(context, (float)(x - 4), sy + 1.0F, (float)(w + 8), base - 2.0F, 7, MenuKit.withAlpha(this.accent(), Math.round(200.0F * pulse)));
         }
      }

      if (over) {
         this.hover(setting, setting.description);
      }

      int accent = this.accent();
      float right = (float)(x + w);
      if (setting instanceof IntSetting integer) {
         String value = integer.display();
         float vw = MenuKit.width(value, 0.74F, true);
         this.text(context, MenuKit.fit(setting.name, (float)w - vw - 10.0F, 0.8F, true), (float)x, sy + 4.0F, 0.8F, this.pal.text(), true);
         this.text(context, value, right - vw, sy + 4.5F, 0.74F, this.pal.dark() ? MenuKit.lerp(accent, -1, 0.45F) : accent, true);
         float p = (float)(integer.get() - integer.min) / (float)Math.max(1, integer.max - integer.min);
         SettingsPane.Key key = new SettingsPane.Key("slider", setting);
         float shown = MenuKit.approach(this.animations.getOrDefault(key, p), p, 20.0F, this.delta);
         this.animations.put(key, shown);
         float ty = sy + 19.0F;
         this.rect(context, (float)x, ty, (float)w, 3.0F, 1, this.pal.track());
         if (shown > 0.0F) {
            this.rect(context, (float)x, ty, Math.max(3.0F, (float)w * shown), 3.0F, 1, accent);
         }

         float kx = (float)x + (float)w * shown;
         if (this.pal.glow()) {
            Smooth.glow(context, Math.round(kx), Math.round(ty + 1.5F), 9, this.c(MenuKit.withAlpha(accent, 96)));
         }

         this.rect(context, kx - 4.0F, ty - 2.5F, 8.0F, 8.0F, 4, this.pal.knob());
         if (!this.pal.dark()) {
            this.outline(context, kx - 4.0F, ty - 2.5F, 8.0F, 8.0F, 4, this.pal.lineHover());
         }

         this.hit(SettingsPane.Kind.SETTING, (float)(x - 4), sy, (float)(w + 8), base, setting, 0);
      } else {
         float nameW = (float)w * 0.56F;
         this.text(context, MenuKit.fit(setting.name, nameW, 0.8F, true), (float)x, sy + 7.0F, 0.8F, this.pal.text(), true);
         if (setting instanceof BoolSetting bool) {
            float px = this.anim("bool", setting, bool.get(), 15.0F);
            this.toggle(context, right - 22.0F, sy + 5.5F, 22.0F, 11.0F, px);
         } else if (setting instanceof ColorSetting colorx) {
            boolean edit = this.editing == setting;
            String hex = edit ? this.editBuffer + "|" : colorx.display();
            this.hexW = Math.max(MenuKit.width("#DDDDDD|", 0.74F, false), MenuKit.width(hex, 0.74F, false)) + 10.0F;
            this.hexX = right - 18.0F - this.hexW;
            boolean overHex = (float)mx >= this.hexX && (float)mx < this.hexX + this.hexW && (float)my >= sy && (float)my < sy + base;
            boolean invalid = this.invalidSetting == setting && System.currentTimeMillis() < this.invalidUntil;
            float hp = this.anim("hex", setting, overHex || edit, 14.0F);
            this.rect(context, this.hexX, sy + 4.0F, this.hexW, 14.0F, 5, invalid ? -9819600 : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hp));
            this.outline(
               context,
               this.hexX,
               sy + 4.0F,
               this.hexW,
               14.0F,
               5,
               edit ? MenuKit.withAlpha(accent, 192) : MenuKit.lerp(this.pal.line(), this.pal.lineHover(), hp)
            );
            this.text(context, hex, this.hexX + 5.0F, sy + 7.5F, 0.74F, edit ? this.pal.text() : this.pal.muted(), false);
            if (overHex) {
               this.hover("hex" + setting.name, "Click and type a color code like #FF0000.");
            }

            this.rect(context, right - 12.0F, sy + 5.0F, 12.0F, 12.0F, 4, colorx.get().packed() | 0xFF000000);
            this.outline(context, right - 12.0F, sy + 5.0F, 12.0F, 12.0F, 4, this.pal.lineHover());
            if (picker > 0.01F) {
               this.drawPicker(context, colorx, x, sy, w, picker);
            }
         } else {
            boolean field = setting instanceof StringSetting || setting instanceof ItemSetting;
            boolean editx = this.editing == setting;
            String valuex;
            if (editx) {
               valuex = MenuKit.tail(this.editBuffer, (float)w * 0.42F - 14.0F, 0.74F, false) + "|";
            } else if (this.keyBinding == setting) {
               valuex = "Press a key";
            } else if (setting instanceof BlockListSetting blocks) {
               valuex = blocks.get().isEmpty() ? "Pick blocks" : blocks.get().size() + " blocks";
            } else {
               valuex = setting.display();
            }

            float pw = field
               ? Math.max(70.0F, (float)w * 0.42F)
               : Math.min((float)w * 0.44F, MenuKit.width(valuex, 0.74F, true) + (setting instanceof EnumSetting ? 22.0F : 14.0F));
            float px = right - pw;
            boolean invalidx = editx && System.currentTimeMillis() < this.invalidUntil;
            float hpx = this.anim("pill", setting, over || editx, 14.0F);
            this.rect(context, px, sy + 3.0F, pw, 16.0F, 6, invalidx ? -9819600 : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hpx));
            this.outline(
               context, px, sy + 3.0F, pw, 16.0F, 6, editx ? MenuKit.withAlpha(accent, 192) : MenuKit.lerp(this.pal.line(), this.pal.lineHover(), hpx)
            );
            String shownx = MenuKit.fit(valuex, pw - (setting instanceof EnumSetting ? 20.0F : 10.0F), 0.74F, !field);
            this.text(context, shownx, px + 6.0F, sy + 7.5F, 0.74F, this.pal.text(), !field);
            if (setting instanceof EnumSetting) {
               Icons.draw(context, Icons.Icon.CHEVRON, Math.round(right - 12.0F), Math.round(sy + 7.0F), 8, this.c(this.pal.muted()));
            }
         }

         this.hit(SettingsPane.Kind.SETTING, (float)(x - 4), sy, (float)(w + 8), base, setting, 0);
         if (setting instanceof ColorSetting) {
            this.hit(SettingsPane.Kind.HEX, this.hexX, sy + 3.0F, this.hexW, 16.0F, setting, 0);
         }
      }
   }

   private void drawPicker(class_332 context, ColorSetting color, int x, float sy, int w, float open) {
      float previous = this.opacity;
      this.opacity = previous * open;
      float[] hsv = color.hsv();
      int wheel = 60;
      int wx = x + 4;
      int wy = Math.round(sy + 26.0F);
      ColorWheel.draw(context, wx, wy, wheel, hsv[2], this.opacity);
      double angle = (double)hsv[0] * 3.141592653589793 * 2.0;
      double radius = (double)hsv[1] * ((double)wheel / 2.0 - 1.0);
      int px = wx + wheel / 2 + (int)Math.round(Math.cos(angle) * radius);
      int py = wy + wheel / 2 + (int)Math.round(Math.sin(angle) * radius);
      Smooth.circle(context, px, py, 4, this.c(-1));
      Smooth.circle(context, px, py, 3, this.c(0xFF000000 | Color.fromHsv(hsv[0], hsv[1], 1.0F, 255).packed()));
      if (open > 0.95F) {
         this.hit(SettingsPane.Kind.WHEEL, (float)wx, (float)wy, (float)wheel, (float)wheel, color, 0);
      }

      int side = wx + wheel + 12;
      int sideW = x + w - 4 - side;

      for (int channel = 0; channel < 2; channel++) {
         int barY = wy + 12 + channel * 26;
         int start = channel == 0 ? -16777216 : color.get().packed() & 16777215;
         int end = 0xFF000000 | (channel == 0 ? Color.fromHsv(hsv[0], hsv[1], 1.0F, 255).packed() : color.get().packed());
         this.text(context, channel == 0 ? "Brightness" : "Alpha", (float)side, (float)(barY - 10), 0.62F, this.pal.muted(), false);
         Smooth.horizontalGradient(context, side + 3, barY, side + sideW - 3, barY + 6, this.c(start), this.c(end));
         Smooth.rect(context, side, barY, 6, 6, 3, this.c(start));
         Smooth.rect(context, side + sideW - 6, barY, 6, 6, 3, this.c(end));
         float value = channel == 0 ? hsv[2] : (float)color.get().a() / 255.0F;
         Smooth.circle(context, side + Math.round((float)sideW * value), barY + 3, 5, this.c(this.pal.text()));
         if (open > 0.95F) {
            this.hit(
               channel == 0 ? SettingsPane.Kind.BRIGHTNESS : SettingsPane.Kind.ALPHA,
               (float)(side - 4),
               (float)(barY - 4),
               (float)(sideW + 8),
               14.0F,
               color,
               channel
            );
         }
      }

      this.opacity = previous;
   }

   private void drawBind(class_332 context, int x, float sy, int w, int mx, int my) {
      boolean over = mx >= x - 4 && mx < x + w + 4 && (float)my >= sy && (float)my < sy + 20.0F;
      float hv = this.anim("bindRow", this.module, over || this.binding, 12.0F);
      if (hv > 0.01F) {
         this.rect(context, (float)(x - 4), sy, (float)(w + 8), 20.0F, 7, MenuKit.alpha(this.pal.hover(), hv));
      }

      this.text(context, "Keybind", (float)x, sy + 6.0F, 0.8F, this.pal.text(), true);
      String value = this.binding ? "Press a key..." : MenuKit.keyName(this.module.getKey());
      float pw = Math.min((float)w * 0.5F, MenuKit.width(value, 0.74F, true) + 14.0F);
      int accent = this.accent();
      this.rect(
         context,
         (float)(x + w) - pw,
         sy + 2.0F,
         pw,
         16.0F,
         6,
         this.binding ? MenuKit.withAlpha(accent, 80) : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv)
      );
      this.outline(context, (float)(x + w) - pw, sy + 2.0F, pw, 16.0F, 6, this.binding ? MenuKit.withAlpha(accent, 208) : this.pal.line());
      this.text(context, MenuKit.fit(value, pw - 10.0F, 0.74F, true), (float)(x + w) - pw + 7.0F, sy + 6.5F, 0.74F, this.pal.text(), true);
      this.hit(SettingsPane.Kind.BIND, (float)(x - 4), sy, (float)(w + 8), 20.0F, this.module, 0);
      if (over) {
         this.hover("bind", "Click, then press a key. Escape or Delete removes the bind.");
      }
   }

   private float configHeight(int w) {
      return 242.0F
         + (float)MenuKit.wrap(
                  "Configs only hold visual settings, so imported configs are always safe to try.",
                  (float)w,
                  0.62F,
                  false
               )
               .size()
            * 9.0F
         + 8.0F;
   }

   private float drawConfigs(class_332 context, int x, float sy, int w, int mx, int my) {
      Configs configs = (Configs)this.module;
      int current = configs.slot();
      int accent = this.accent();

      for (int i = 0; i < 5; i++) {
         float ry = sy + (float)i * 30.0F;
         boolean over = mx >= x && mx < x + w && (float)my >= ry && (float)my < ry + 26.0F;
         float hv = this.anim("cslot", i, over, 12.0F);
         float s = this.anim("cselect", i, i == current, 14.0F);
         this.well(context, x, Math.round(ry), w, 26, 7, hv, this.opacity);
         if (s > 0.01F) {
            this.rect(context, (float)x, ry, (float)w, 26.0F, 7, MenuKit.withAlpha(accent, Math.round(38.0F * s)));
            this.rect(context, (float)(x + 3), ry + 7.0F, 2.0F, 12.0F, 1, MenuKit.withAlpha(accent, Math.round(255.0F * s)));
         }

         float click = this.press("cslot" + i);
         if (click > 0.0F) {
            this.rect(context, (float)x, ry, (float)w, 26.0F, 7, MenuKit.withAlpha(accent, Math.round(60.0F * click)));
         }

         if (this.renaming && i == current) {
            float fw = (float)Math.min(160, w - 70);
            this.rect(context, (float)(x + 9), ry + 3.0F, fw, 12.0F, 4, this.pal.softHover());
            this.text(context, MenuKit.tail(this.renameBuffer, fw - 10.0F, 0.74F, true) + "|", (float)(x + 13), ry + 5.0F, 0.74F, this.pal.text(), true);
         } else {
            this.text(
               context,
               MenuKit.fit(ConfigSlots.name(i), (float)(w - 70), 0.74F, true),
               (float)(x + 10),
               ry + 4.0F,
               0.74F,
               ConfigSlots.empty(i) ? this.pal.muted() : this.pal.text(),
               true
            );
         }

         this.text(context, MenuKit.fit(configs.status(i), (float)(w - 70), 0.62F, false), (float)(x + 10), ry + 15.0F, 0.62F, this.pal.muted(), false);
         if (ConfigSlots.active() == i) {
            float bw = MenuKit.width("ACTIVE", 0.62F, true) + 10.0F;
            this.rect(context, (float)(x + w - 8) - bw, ry + 7.0F, bw, 12.0F, 6, accent);
            this.text(context, "ACTIVE", (float)(x + w - 3) - bw, ry + 9.5F, 0.62F, -15069650, true);
         }

         this.hit(SettingsPane.Kind.CONFIG_SLOT, (float)x, ry, (float)w, 26.0F, null, i);
         if (over) {
            this.hover("cslot" + i, "Click to select this config. Double click to rename it.");
         }
      }

      float cy = sy + 150.0F + 4.0F;
      float importW = 56.0F;
      float fw = (float)w - importW - 4.0F;
      this.well(context, x, Math.round(cy), Math.round(fw), 22, 7, this.typingCode ? 1.0F : 0.0F, this.opacity);
      String typed = this.codeText.length() > 60 ? this.codeText.length() + " characters pasted" : MenuKit.tail(this.codeText, fw - 16.0F, 0.74F, false);
      this.text(
         context,
         this.codeText.isEmpty() ? MenuKit.fit("Paste a config or leave empty for clipboard", fw - 14.0F, 0.74F, false) : typed,
         (float)(x + 7),
         cy + 7.0F,
         0.74F,
         this.codeText.isEmpty() ? this.pal.muted() : this.pal.text(),
         false
      );
      if (this.typingCode) {
         float blink = (float)(0.5 + 0.5 * Math.cos((double)System.currentTimeMillis() / 530.0 * 3.141592653589793));
         float cx = (float)(x + 8) + (this.codeText.isEmpty() ? 0.0F : MenuKit.width(typed, 0.74F, false));
         this.rect(context, cx, cy + 5.0F, 1.0F, 11.0F, 0, MenuKit.withAlpha(this.pal.text(), Math.round(255.0F * blink)));
      }

      this.hit(SettingsPane.Kind.CONFIG_CODE, (float)x, cy, fw, 22.0F, null, 0);
      this.configButton(context, "Import", (float)x + fw + 4.0F, cy, importW, 22.0F, 5, mx, my);
      float by = cy + 28.0F;
      float bw = ((float)w - 16.0F) / 5.0F;
      String[] labels = new String[]{"Save", "Activate", "Export", this.renaming ? "Done" : "Rename", "Delete"};

      for (int i = 0; i < labels.length; i++) {
         this.configButton(context, labels[i], (float)x + (float)i * (bw + 4.0F), by, bw, 22.0F, i, mx, my);
      }

      float ty = by + 30.0F;
      this.drawSetting(context, configs.shareName, x, ty, w, mx, my);
      ty += 26.0F;

      for (String line : MenuKit.wrap(
         "Configs only hold visual settings, so imported configs are always safe to try.", (float)w, 0.62F, false
      )) {
         this.text(context, line, (float)x, ty, 0.62F, this.pal.faint(), false);
         ty += 9.0F;
      }

      return ty + 8.0F;
   }

   private void configButton(class_332 context, String label, float bx, float by, float bw, float bh, int action, int mx, int my) {
      boolean over = (float)mx >= bx && (float)mx < bx + bw && (float)my >= by && (float)my < by + bh;
      float hv = this.anim("caction", action, over, 12.0F);
      Configs configs = (Configs)this.module;
      boolean danger = action == 4 && configs.deleteArmed();
      boolean primary = action == 1;
      int accent = this.accent();
      int fill = danger ? -7721158 : (primary ? accent : MenuKit.lerp(this.pal.soft(), this.pal.softHover(), hv));
      this.rect(context, bx, by, bw, bh, 7, fill);
      if ((danger || primary) && hv > 0.01F) {
         this.rect(context, bx, by, bw, bh, 7, MenuKit.withAlpha(16777215, Math.round(36.0F * hv)));
      }

      if (!danger && !primary) {
         this.outline(context, bx, by, bw, bh, 7, this.pal.line());
      }

      float click = this.press("caction" + action);
      if (click > 0.0F) {
         this.rect(context, bx, by, bw, bh, 7, MenuKit.withAlpha(16777215, Math.round(50.0F * click)));
      }

      String shown = MenuKit.fit(danger ? "Sure?" : label, bw - 6.0F, 0.62F, true);
      MenuKit.centered(context, shown, bx + bw / 2.0F, by + (bh - 6.0F) / 2.0F, 0.62F, this.c(primary ? -15069650 : this.pal.text()), true);
      this.hit(SettingsPane.Kind.CONFIG_ACTION, bx, by, bw, bh, null, action);
      if (over) {
         this.hover("caction" + action, Configs.TIPS[action]);
      }
   }

   private void startRename() {
      this.renaming = true;
      this.renameBuffer = ConfigSlots.name(((Configs)this.module).slot());
      this.typingCode = false;
   }

   private void commitRename() {
      this.renaming = false;
      if (this.module instanceof Configs configs) {
         configs.renameSlot(this.renameBuffer);
      }
   }

   private void configAction(int action) {
      if (this.module instanceof Configs configs) {
         if (action == 3) {
            if (this.renaming) {
               this.commitRename();
            } else {
               this.startRename();
            }
         } else {
            if (action == 5) {
               configs.importText(this.codeText);
               this.codeText = "";
               this.typingCode = false;
            } else {
               configs.run(action);
            }

            SoberClient.CONFIG.save();
         }
      }
   }

   void commit() {
      if (this.editing != null) {
         this.commitEdit();
      }

      if (this.renaming) {
         this.commitRename();
      }

      this.binding = false;
      this.keyBinding = null;
      this.typingCode = false;
      this.dragging = null;
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

   boolean click(double mx, double my, int button, boolean doubled) {
      SettingsPane.Hit hit = this.contains(mx, my) ? this.hitAt(mx, my) : null;
      if (this.editing != null && (hit == null || hit.target() != this.editing)) {
         this.commitEdit();
      }

      if (this.binding && (hit == null || hit.kind() != SettingsPane.Kind.BIND)) {
         this.binding = false;
      }

      if (this.keyBinding != null && (hit == null || hit.target() != this.keyBinding)) {
         this.keyBinding = null;
      }

      if (this.renaming && (hit == null || hit.kind() != SettingsPane.Kind.CONFIG_ACTION || hit.index() != 3)) {
         this.commitRename();
      }

      if (hit == null || hit.kind() != SettingsPane.Kind.CONFIG_CODE) {
         this.typingCode = false;
      }

      if (!this.contains(mx, my)) {
         return false;
      } else if (hit == null) {
         return true;
      } else {
         switch (hit.kind()) {
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
            case STATUS:
               this.pressedAt.put("status" + hit.index(), System.nanoTime());
               UiSounds.click();
               MenuKit.openLink((String)hit.target());
               break;
            case BIND:
               this.binding = !this.binding;
               UiSounds.click();
               break;
            case WHEEL:
            case BRIGHTNESS:
            case ALPHA:
               this.dragging = hit;
               this.updateDrag(hit, mx, my);
               break;
            case CONFIG_SLOT:
               ((Configs)this.module).select(hit.index());
               this.pressedAt.put("cslot" + hit.index(), System.nanoTime());
               UiSounds.click();
               if (doubled) {
                  this.startRename();
               }

               SoberClient.CONFIG.save();
               break;
            case CONFIG_ACTION:
               this.pressedAt.put("caction" + hit.index(), System.nanoTime());
               UiSounds.click();
               this.configAction(hit.index());
               break;
            case CONFIG_CODE:
               this.typingCode = true;
         }

         return true;
      }
   }

   private void clickSetting(SettingsPane.Hit hit, double mx, int button) {
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
            this.client.method_1507(new BlockPickerScreen(this.host, blocks));
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

   private void updateDrag(SettingsPane.Hit hit, double mx, double my) {
      if (hit.target() instanceof IntSetting integer) {
         float trackX = hit.x() + 4.0F;
         float trackW = hit.w() - 8.0F;
         double progress = Math.max(0.0, Math.min(1.0, (mx - (double)trackX) / (double)trackW));
         integer.set(Integer.valueOf((int)Math.round((double)integer.min + progress * (double)(integer.max - integer.min))));
      } else if (hit.target() instanceof ColorSetting color) {
         if (hit.kind() == SettingsPane.Kind.WHEEL) {
            float[] picked = ColorWheel.pick(mx, my, Math.round(hit.x()), Math.round(hit.y()), Math.round(hit.w()));
            float value = color.hsv()[2];
            color.setHsv(picked[0], picked[1], value < 0.05F ? 1.0F : value);
         } else {
            double progress = Math.max(0.0, Math.min(1.0, (mx - (double)(hit.x() + 4.0F)) / (double)(hit.w() - 8.0F)));
            if (hit.kind() == SettingsPane.Kind.BRIGHTNESS) {
               float[] hsv = color.hsv();
               color.setHsv(hsv[0], hsv[1], (float)progress);
            } else {
               color.setAlpha((int)Math.round(progress * 255.0));
            }
         }
      }
   }

   boolean drag(double mx, double my) {
      if (this.dragging == null) {
         return false;
      } else {
         this.updateDrag(this.dragging, mx, my);
         return true;
      }
   }

   boolean release() {
      if (this.dragging == null) {
         return false;
      } else {
         this.dragging = null;
         SoberClient.CONFIG.save();
         return true;
      }
   }

   boolean scroll(double mx, double my, double vertical, boolean ctrl, boolean shift) {
      if (this.contains(mx, my) && this.module != null) {
         int step = vertical > 0.0 ? 1 : -1;
         SettingsPane.Hit hit = this.hitAt(mx, my);
         if (ctrl && hit != null && hit.target() instanceof IntSetting integer) {
            integer.set(Integer.valueOf(integer.get() + step * (shift ? 10 : 1)));
            return true;
         } else {
            this.scrollTarget = Math.max(0.0F, Math.min(Math.max(0.0F, this.content - this.view), this.scrollTarget - (float)step * 34.0F));
            return true;
         }
      } else {
         return false;
      }
   }

   boolean key(class_11908 input) {
      int key = input.comp_4795();
      if (this.binding && this.module != null) {
         this.module.setKey(key != 256 && key != 259 && key != 261 ? key : -1);
         UiSounds.keybind(this.module.getKey() >= 0);
         this.binding = false;
         this.swallowUntil = System.currentTimeMillis() + 150L;
         SoberClient.CONFIG.save();
         return true;
      } else if (this.keyBinding == null) {
         if (this.renaming) {
            switch (key) {
               case 86:
                  if (input.method_75377()) {
                     String pasted = this.renameBuffer + this.client.field_1774.method_1460().replace("\n", "").replace("\r", "");
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
         } else if (this.typingCode) {
            switch (key) {
               case 86:
                  if (input.method_75377()) {
                     String pasted = this.codeText + this.client.field_1774.method_1460().replace("\r", "").replace('\n', ' ').replace('\t', ' ');
                     this.codeText = pasted.length() > 200000 ? pasted.substring(0, 200000) : pasted;
                  }
                  break;
               case 256:
                  this.typingCode = false;
                  break;
               case 257:
               case 335:
                  this.configAction(5);
                  break;
               case 259:
                  if (!this.codeText.isEmpty()) {
                     this.codeText = this.codeText.substring(0, this.codeText.length() - 1);
                  }
            }

            return true;
         } else if (this.editing != null) {
            switch (key) {
               case 86:
                  if (input.method_75377()) {
                     this.editBuffer = this.editBuffer + this.client.field_1774.method_1460().replace("\n", "").replace("\r", "");
                  }
                  break;
               case 256:
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
         } else {
            return false;
         }
      } else {
         if (key != 256) {
            this.keyBinding.bind(key != 259 && key != 261 ? key : -1);
         }

         UiSounds.keybind(this.keyBinding.get() >= 0);
         this.keyBinding = null;
         this.swallowUntil = System.currentTimeMillis() + 150L;
         SoberClient.CONFIG.save();
         return true;
      }
   }

   boolean typed(class_11905 input) {
      if (System.currentTimeMillis() < this.swallowUntil) {
         this.swallowUntil = 0L;
         return true;
      } else if (this.renaming) {
         if (this.renameBuffer.length() < 32) {
            this.renameBuffer = this.renameBuffer + input.method_74226();
         }

         return true;
      } else if (this.typingCode) {
         if (this.codeText.length() < 200000) {
            this.codeText = this.codeText + input.method_74226();
         }

         return true;
      } else if (this.editing != null) {
         String typed = input.method_74226();
         if (this.editing instanceof ColorSetting) {
            if (this.editBuffer.length() < 9 && typed.matches("[#0-9a-fA-FxX]")) {
               this.editBuffer = this.editBuffer + typed.toUpperCase(Locale.ROOT);
            }
         } else if (this.editBuffer.length() < 100) {
            this.editBuffer = this.editBuffer + typed;
         }

         return true;
      } else {
         return this.binding || this.keyBinding != null;
      }
   }

   private static record Hit(SettingsPane.Kind kind, float x, float y, float w, float h, Object target, int index) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
      }
   }

   private static record Key(String tag, Object target) {
   }

   private static enum Kind {
      SETTING,
      HEX,
      PRESET,
      STATUS,
      BIND,
      WHEEL,
      BRIGHTNESS,
      ALPHA,
      CONFIG_SLOT,
      CONFIG_ACTION,
      CONFIG_CODE;
   }

   static record Palette(
      boolean dark,
      int text,
      int muted,
      int faint,
      int soft,
      int softHover,
      int line,
      int lineHover,
      int track,
      int hover,
      int knob,
      int toggleOff,
      int toggleOn,
      boolean glow,
      int accent
   ) {
   }
}
