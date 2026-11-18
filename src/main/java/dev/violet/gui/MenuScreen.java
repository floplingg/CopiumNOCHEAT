package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.MainMenu;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.joml.Matrix3x2fStack;

public class MenuScreen extends class_437 {
   private static final float SKIP = 2.7F;
   private static final String TITLE = "SOBERCLIENT";
   private static final String SUBTITLE = "CLIENT";
   private static final int PANEL = -484893920;
   private static final int CARD = -1087623114;
   private static final int CARD_HOVER = -700825785;
   private static final int DANGER_HOVER = -699781325;
   private static final int TEXT = -986380;
   private static final int MUTED = -3880752;
   private static final int ACCENT = -2762274;
   private static final int BRAND_GLOW = 6284270;
   private static boolean introPlayed;
   private static boolean vanilla;
   private final List<MenuScreen.Entry> entries = new ArrayList<>();
   private final float[] hover = new float[8];
   private final int[][] bounds = new int[8][4];
   private float linkHover;
   private int[] linkBounds = new int[4];
   private long start = -1L;
   private float offset;
   private boolean introSound;
   private boolean revealSound;
   private long lastFrame = System.nanoTime();
   private float delta;

   public MenuScreen() {
      super(class_2561.method_43470("SoberClient"));
   }

   public static boolean enabled() {
      if (vanilla) {
         return false;
      } else {
         MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
         return menu != null && menu.isActive();
      }
   }

   private MainMenu menu() {
      return SoberClient.MODULES.get(MainMenu.class);
   }

   private boolean island() {
      MainMenu menu = this.menu();
      return menu != null && menu.style.get() == MainMenu.Style.Island;
   }

   protected void method_25426() {
      this.entries.clear();
      this.entries.add(new MenuScreen.Entry("Singleplayer", () -> this.field_22787.method_1507(new class_526(this)), false));
      this.entries
         .add(
            new MenuScreen.Entry(
               "Multiplayer",
               () -> this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500(this) : new class_4749(this))),
               false
            )
         );
      if (FabricLoader.getInstance().isModLoaded("modmenu")) {
         this.entries.add(new MenuScreen.Entry("Mods", this::openMods, false));
      }

      this.entries.add(new MenuScreen.Entry("Options", () -> this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690)), false));
      this.entries.add(new MenuScreen.Entry("Quit Game", () -> this.field_22787.method_1592(), true));
   }

   private void openMods() {
      try {
         Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
         this.field_22787.method_1507((class_437)type.getConstructor(class_437.class).newInstance(this));
      } catch (RuntimeException | ReflectiveOperationException var2) {
         SoberClient.LOG.error("Could not open the mod list", var2);
         this.openVanilla();
      }
   }

   private void openVanilla() {
      vanilla = true;
      this.field_22787.method_1507(new class_442());
   }

   public boolean method_25422() {
      return false;
   }

   public boolean method_25421() {
      return false;
   }

   private float time() {
      if (this.field_22787.method_18506() != null) {
         this.start = -1L;
         return this.offset;
      } else {
         if (this.start < 0L) {
            this.start = System.nanoTime();
            MainMenu menu = this.menu();
            boolean intro = !introPlayed && menu != null && menu.intro.get();
            introPlayed = true;
            this.offset = intro ? 0.0F : 2.7F;
            this.introSound = this.revealSound = !intro;
            MenuAudio.menuShown();
         }

         return this.offset + (float)(System.nanoTime() - this.start) / 1.0E9F;
      }
   }

   private void skip() {
      float t = this.time();
      if (t < 2.7F) {
         this.offset = 2.7F;
         this.start = System.nanoTime();
         this.introSound = this.revealSound = true;
      }
   }

   private static float clamp(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static float phase(float t, float from, float length) {
      return clamp((t - from) / length);
   }

   private static float easeOut(float t) {
      float f = 1.0F - t;
      return 1.0F - f * f * f;
   }

   private static float easeInOut(float t) {
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), 3.0) / 2.0F;
   }

   private static float easeBack(float t) {
      float c = 1.2F;
      float d = c + 1.0F;
      float f = t - 1.0F;
      return 1.0F + d * f * f * f + c * f * f;
   }

   private static int alpha(int color, float alpha) {
      return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | color & 16777215;
   }

   private static int scaled(int color, float alpha) {
      return alpha(color, (float)(color >>> 24) / 255.0F * alpha);
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

   private static void text(class_332 context, String value, float x, float y, float scale, int color) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(x, y);
      matrices.scale(scale, scale);
      SdfText.draw(context, value, 0, 0, color);
      matrices.popMatrix();
   }

   private static float width(String value, float scale) {
      return (float)SdfText.width(value) * scale;
   }

   private static void brand(class_332 context, float cx, float cy, float size, float alpha) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(cx - size / 2.0F, cy - size / 2.0F);
      matrices.scale(size / 64.0F, size / 64.0F);
      context.method_25293(class_10799.field_56883, MenuKit.BRAND, 0, 0, 0.0F, 0.0F, 64, 64, 384, 384, 384, 384, alpha(16777215, alpha));
      matrices.popMatrix();
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      try {
         float t = this.time();
         if (this.island()) {
            IslandScene.background(context, this.field_22789, this.field_22790, t, mouseX, mouseY, this.delta);
         } else {
            ScreenStyle.backdrop(context, this.field_22789, this.field_22790);
            context.method_25294(0, 0, this.field_22789, this.field_22790, 1376258573);
         }

         float fade = easeInOut(phase(t, 0.15F, 1.3F));
         if (fade < 1.0F) {
            context.method_25294(0, 0, this.field_22789, this.field_22790, alpha(329224, 1.0F - fade));
         }
      } catch (RuntimeException var7) {
         this.fail(var7);
      }
   }

   private void fail(RuntimeException e) {
      SoberClient.LOG.error("Custom menu failed, using the vanilla menu", e);
      this.openVanilla();
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      try {
         this.drawMenu(context, mouseX, mouseY);
      } catch (RuntimeException var6) {
         this.fail(var6);
      }
   }

   private void drawMenu(class_332 context, int mouseX, int mouseY) {
      long now = System.nanoTime();
      this.delta = Math.min(0.05F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      float t = this.time();
      if (this.field_22787.method_18506() == null) {
         if (!this.introSound && t >= 0.2F) {
            this.introSound = true;
            UiSounds.intro();
         }

         if (!this.revealSound && t >= 0.85F) {
            this.revealSound = true;
            UiSounds.reveal();
         }

         if (this.island()) {
            this.drawIsland(context, mouseX, mouseY, t);
         } else {
            int cx = this.field_22789 / 2;
            boolean compact = this.field_22790 < 330;
            int panelW = 250;
            int buttonW = 204;
            int buttonH = compact ? 22 : 26;
            int gap = compact ? 5 : 7;
            float logoSize = compact ? 34.0F : 46.0F;
            float titleScale = compact ? 2.2F : 2.8F;
            float titleH = 8.0F * titleScale;
            int pad = compact ? 14 : 20;
            int buttonsH = this.entries.size() * (buttonH + gap) - gap;
            int panelH = Math.round((float)pad + logoSize + 10.0F + titleH + 9.0F + 8.0F + (float)(compact ? 14 : 20) + (float)buttonsH + (float)pad);
            int panelX = cx - panelW / 2;
            int panelY = Math.max(6, (this.field_22790 - panelH) / 2 - 8);
            float move = easeInOut(phase(t, 1.55F, 0.75F));
            float groupH = logoSize + 10.0F + titleH + 9.0F + 8.0F;
            float introTop = (float)this.field_22790 / 2.0F - groupH / 2.0F;
            float finalTop = (float)(panelY + pad);
            float top = introTop + (finalTop - introTop) * move;
            float panel = easeOut(phase(t, 1.75F, 0.6F));
            if (panel > 0.01F) {
               float s = 0.965F + 0.035F * panel;
               Matrix3x2fStack matrices = context.method_51448();
               matrices.pushMatrix();
               matrices.translate((float)cx, (float)panelY + (float)panelH / 2.0F);
               matrices.scale(s, s);
               matrices.translate((float)(-cx), -((float)panelY + (float)panelH / 2.0F));
               Smooth.rect(context, panelX - 1, panelY + 3, panelW + 2, panelH + 2, 16, alpha(0, 0.22F * panel));
               Smooth.rect(context, panelX, panelY, panelW, panelH, 15, scaled(-484893920, panel));
               Smooth.sheen(context, panelX, panelY, panelW, panelH, 15, alpha(16777215, 0.05F * panel));
               Smooth.outline(context, panelX, panelY, panelW, panelH, 15, alpha(16777215, 0.06F * panel));
               matrices.popMatrix();
            }

            float lcy = top + logoSize / 2.0F;
            float wave = phase(t, 0.3F, 1.2F);
            if (wave > 0.0F && wave < 1.0F) {
               int radius = Math.round(easeOut(wave) * (float)Math.max(this.field_22789, this.field_22790) * 0.5F);
               Smooth.wave(context, cx, Math.round(lcy), radius, alpha(-2762274, (1.0F - wave) * 0.5F));
            }

            float appear = phase(t, 0.25F, 0.7F);
            if (appear > 0.0F) {
               double seconds = (double)System.currentTimeMillis() / 1000.0;
               float pulse = (float)(0.5 + 0.5 * Math.sin(seconds * 1.3));
               float flash = 1.0F - Math.abs(phase(t, 0.55F, 0.6F) * 2.0F - 1.0F);
               Smooth.glow(
                  context,
                  cx,
                  Math.round(lcy),
                  Math.round(logoSize * (1.1F + 0.1F * pulse + flash * 0.6F)),
                  alpha(6284270, appear * (0.16F + 0.06F * pulse + flash * 0.25F))
               );
               float scale = Math.max(0.01F, easeBack(appear));
               brand(context, (float)cx, lcy, logoSize * scale, appear);
            }

            float titleY = top + logoSize + 10.0F;
            float titleW = width("SOBERCLIENT", titleScale);
            float charX = (float)cx - titleW / 2.0F;

            for (int i = 0; i < "SOBERCLIENT".length(); i++) {
               String letter = String.valueOf("SOBERCLIENT".charAt(i));
               float p = easeOut(phase(t, 0.85F + (float)i * 0.055F, 0.45F));
               if (p > 0.01F) {
                  float shimmer = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 700.0 - (double)i * 0.55);
                  int color = lerp(-2762274, -986380, 0.4F + 0.6F * shimmer);
                  text(context, letter, charX, titleY + (1.0F - p) * 9.0F, titleScale, alpha(color, p));
               }

               charX += width(letter, titleScale);
            }

            float sub = easeOut(phase(t, 1.35F, 0.5F));
            float subY = titleY + titleH + 9.0F;
            if (sub > 0.01F) {
               float spacing = 3.5F;
               float subW = 0.0F;

               for (char c : "CLIENT".toCharArray()) {
                  subW += width(String.valueOf(c), 1.0F) + spacing;
               }

               subW -= spacing;
               float sx = (float)cx - subW / 2.0F;
               int line = Math.round(30.0F * sub);
               int ly = Math.round(subY + 3.0F);
               context.method_25294(Math.round(sx - 10.0F - (float)line), ly, Math.round(sx - 10.0F), ly + 1, alpha(-2762274, 0.45F * sub));
               context.method_25294(Math.round(sx + subW + 10.0F), ly, Math.round(sx + subW + 10.0F + (float)line), ly + 1, alpha(-2762274, 0.45F * sub));

               for (char c : "CLIENT".toCharArray()) {
                  String letter = String.valueOf(c);
                  text(context, letter, sx, subY, 1.0F, alpha(-3880752, sub));
                  sx += width(letter, 1.0F) + spacing;
               }
            }

            int by = Math.round(finalTop + groupH + (float)(compact ? 14 : 20));
            int bx = cx - buttonW / 2;

            for (int i = 0; i < this.entries.size(); i++) {
               MenuScreen.Entry entry = this.entries.get(i);
               float p = easeOut(phase(t, 2.05F + (float)i * 0.08F, 0.45F));
               int y = by + Math.round((1.0F - p) * 14.0F);
               this.bounds[i][0] = bx;
               this.bounds[i][1] = by;
               this.bounds[i][2] = buttonW;
               this.bounds[i][3] = buttonH;
               boolean hovered = p >= 1.0F && mouseX >= bx && mouseX < bx + buttonW && mouseY >= by && mouseY < by + buttonH;
               float before = this.hover[i];
               this.hover[i] = this.hover[i] + ((hovered ? 1.0F : 0.0F) - this.hover[i]) * (1.0F - (float)Math.exp((double)(-16.0F * this.delta)));
               if (hovered && before < 0.05F) {
                  UiSounds.hover();
               }

               float h = this.hover[i];
               if (p > 0.01F) {
                  int fill = lerp(-1087623114, entry.danger() ? -699781325 : -700825785, h);
                  Smooth.rect(context, bx, y, buttonW, buttonH, 8, scaled(fill, p));
                  if (h > 0.01F) {
                     Smooth.outline(context, bx, y, buttonW, buttonH, 8, alpha(entry.danger() ? 15238026 : 16777215, 0.14F * h * p));
                  }

                  String label = entry.label();
                  float lw = width(label, 1.0F);
                  int color = entry.danger() ? lerp(-3880752, -1005394, h) : lerp(-3091496, -986380, h);
                  text(context, label, (float)cx - lw / 2.0F + h, (float)y + (float)(buttonH - 8) / 2.0F, 1.0F, alpha(color, p));
               }

               by += buttonH + gap;
            }

            this.footer(context, mouseX, mouseY, t);
            this.hint(context, t, cx);
            Notifications.render(context, true);
         }
      }
   }

   private void drawIsland(class_332 context, int mouseX, int mouseY, float t) {
      float shade = easeOut(phase(t, 0.4F, 1.2F));
      context.method_25296(0, this.field_22790 - Math.max(40, this.field_22790 / 6), this.field_22789, this.field_22790, 397862, alpha(397862, 0.42F * shade));
      IslandScene.title(context, this.field_22789, this.field_22790, t);
      int n = this.entries.size();
      int gap = 6;
      int buttonH = this.field_22790 < 300 ? 20 : 24;
      int columns = n;
      int buttonW = Math.min(104, (this.field_22789 - 40 - gap * (n - 1)) / n);
      if (buttonW < 66) {
         columns = (n + 1) / 2;
         buttonW = Math.min(104, (this.field_22789 - 40 - gap * (columns - 1)) / columns);
      }

      int rows = (n + columns - 1) / columns;
      int blockH = rows * buttonH + (rows - 1) * gap;
      int top = Math.round(Math.min((float)this.field_22790 * 0.835F, (float)(this.field_22790 - 36 - blockH)));
      int cx = this.field_22789 / 2;

      for (int i = 0; i < n; i++) {
         MenuScreen.Entry entry = this.entries.get(i);
         int row = i / columns;
         int col = i % columns;
         int inRow = Math.min(columns, n - row * columns);
         int rowW = inRow * buttonW + (inRow - 1) * gap;
         int bx = cx - rowW / 2 + col * (buttonW + gap);
         int by = top + row * (buttonH + gap);
         float p = easeOut(phase(t, 2.05F + (float)i * 0.08F, 0.45F));
         int y = by + Math.round((1.0F - p) * 14.0F);
         this.bounds[i][0] = bx;
         this.bounds[i][1] = by;
         this.bounds[i][2] = buttonW;
         this.bounds[i][3] = buttonH;
         boolean hovered = p >= 1.0F && mouseX >= bx && mouseX < bx + buttonW && mouseY >= by && mouseY < by + buttonH;
         float before = this.hover[i];
         this.hover[i] = this.hover[i] + ((hovered ? 1.0F : 0.0F) - this.hover[i]) * (1.0F - (float)Math.exp((double)(-16.0F * this.delta)));
         if (hovered && before < 0.05F) {
            UiSounds.hover();
         }

         float h = this.hover[i];
         if (p > 0.01F) {
            int fill = lerp(-1087623114, entry.danger() ? -699781325 : -700825785, h);
            Smooth.rect(context, bx - 1, y + 2, buttonW + 2, buttonH + 2, 9, alpha(0, 0.2F * p));
            Smooth.rect(context, bx, y, buttonW, buttonH, 8, scaled(fill, p));
            Smooth.sheen(context, bx, y, buttonW, buttonH, 8, alpha(16777215, 0.05F * p));
            if (h > 0.01F) {
               Smooth.outline(context, bx, y, buttonW, buttonH, 8, alpha(entry.danger() ? 15238026 : 16777215, 0.18F * h * p));
            }

            String label = entry.label();
            float lw = width(label, 1.0F);
            int color = entry.danger() ? lerp(-3880752, -1005394, h) : lerp(-3091496, -986380, h);
            text(context, label, (float)bx + (float)buttonW / 2.0F - lw / 2.0F, (float)y + (float)(buttonH - 8) / 2.0F, 1.0F, alpha(color, p));
         }
      }

      this.footer(context, mouseX, mouseY, t);
      this.hint(context, t, cx);
      Notifications.render(context, true);
   }

   private void footer(class_332 context, int mouseX, int mouseY, float t) {
      float footer = easeOut(phase(t, 2.5F, 0.5F));
      if (footer > 0.01F) {
         String version = FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
         brand(context, 15.0F, (float)(this.field_22790 - 25), 14.0F, footer);
         text(context, "SoberClient", 26.0F, (float)(this.field_22790 - 29), 1.0F, alpha(-986380, footer));
         text(
            context,
            "Minecraft " + version + "  /  " + FabricLoader.getInstance().getAllMods().size() + " mods",
            26.0F,
            (float)(this.field_22790 - 17),
            0.85F,
            alpha(-3880752, footer)
         );
         String link = "Vanilla Menu";
         int lw = Math.round(width(link, 0.9F));
         int lx = this.field_22789 - lw - 10;
         int ly = this.field_22790 - 17;
         this.linkBounds = new int[]{lx - 4, ly - 4, lw + 8, 15};
         boolean over = mouseX >= lx - 4 && mouseX < lx + lw + 4 && mouseY >= ly - 4 && mouseY < ly + 11;
         this.linkHover = this.linkHover + ((over ? 1.0F : 0.0F) - this.linkHover) * (1.0F - (float)Math.exp((double)(-16.0F * this.delta)));
         text(context, link, (float)lx, (float)ly, 0.9F, alpha(lerp(-3880752, -986380, this.linkHover), footer));
         int line = Math.round((float)lw * this.linkHover);
         if (line > 0) {
            context.method_25294(lx, ly + 9, lx + line, ly + 10, alpha(-2762274, footer * 0.8F));
         }
      }
   }

   private void hint(class_332 context, float t, int cx) {
      if (t < 2.4F && t > 0.6F) {
         String hint = "Click to skip";
         text(context, hint, (float)cx - width(hint, 0.85F) / 2.0F, (float)(this.field_22790 - 16), 0.85F, alpha(-3880752, 0.55F * phase(t, 0.6F, 0.5F)));
      }
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      float t = this.time();
      if (t < 2.7F) {
         this.skip();
         return true;
      } else if (t < 2.75F) {
         return true;
      } else {
         double mx = click.comp_4798();
         double my = click.comp_4799();

         for (int i = 0; i < this.entries.size(); i++) {
            int[] b = this.bounds[i];
            if (mx >= (double)b[0] && mx < (double)(b[0] + b[2]) && my >= (double)b[1] && my < (double)(b[1] + b[3])) {
               UiSounds.click();
               this.entries.get(i).action().run();
               return true;
            }
         }

         if (mx >= (double)this.linkBounds[0]
            && mx < (double)(this.linkBounds[0] + this.linkBounds[2])
            && my >= (double)this.linkBounds[1]
            && my < (double)(this.linkBounds[1] + this.linkBounds[3])) {
            UiSounds.click();
            this.openVanilla();
            return true;
         } else {
            return super.method_25402(click, doubled);
         }
      }
   }

   public boolean method_25404(class_11908 input) {
      if (this.time() < 2.7F) {
         this.skip();
         return true;
      } else {
         return super.method_25404(input);
      }
   }

   private static record Entry(String label, Runnable action, boolean danger) {
   }
}
