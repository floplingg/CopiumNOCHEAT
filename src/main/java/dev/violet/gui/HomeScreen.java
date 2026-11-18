package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.MainMenu;
import dev.violet.modules.NameTags;
import dev.violet.modules.SkinChange;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_412;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_5348;
import net.minecraft.class_639;
import net.minecraft.class_641;
import net.minecraft.class_642;
import net.minecraft.class_8685;
import net.minecraft.class_8765;
import net.minecraft.class_642.class_8678;
import org.joml.Matrix3x2fStack;

public class HomeScreen extends class_437 {
   private static final float WIDTH = 860.0F;
   private static final float HEIGHT = 440.0F;
   private static final float SLIDE = 0.6F;
   private static final float LEAVE = 0.22F;
   private static final float HOLD = 3.4F;
   private static final float WINDOW = 4.3F;
   private static final float INTRO = 5.2F;
   private static final int TEXT = -855051;
   private static final int MUTED = -4275510;
   private static final int FAINT = -7367779;
   private static final int CARD = 352321535;
   private static final int CARD_HOVER = 587202559;
   private static final int EDGE = 536870911;
   private static final float[][] STARS = stars();
   private static final class_2960 SAKURA = class_2960.method_60655("violet", "textures/gui/sakura_backdrop.png");
   private static final int SAKURA_W = 2400;
   private static final int SAKURA_H = 1350;
   private static boolean introPlayed;
   private static long firstShown;
   private final Map<String, Float> hover = new HashMap<>();
   private final List<HomeScreen.Tile> tiles = new ArrayList<>();
   private final List<class_642> servers = new ArrayList<>();
   private Supplier<class_8685> baseSkin;
   private class_8765 skinWidget;
   private class_342 nameField;
   private class_342 skinField;
   private String pendingSkin;
   private long skinChangedAt;
   private float scale = 1.0F;
   private float originX;
   private float originY;
   private long lastFrame = System.nanoTime();
   private long openedAt;
   private float delta;
   private float time;
   private float now;
   private float fade = 1.0F;
   private float parallaxX;
   private float parallaxY;
   private String pressedKey;
   private long pressedAt;
   private Runnable leaving;
   private long leaveStart;
   private boolean replay;
   private boolean shown;
   private float introShift;

   public HomeScreen() {
      super(class_2561.method_43470("SoberClient"));
   }

   protected void method_25426() {
      if (this.openedAt == 0L || this.replay) {
         this.openedAt = System.nanoTime();
         this.replay = false;
         this.hover.clear();
         MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
         this.introShift = !introPlayed && (menu == null || menu.intro.get()) ? 5.2F : 0.0F;
         introPlayed = true;
      }

      this.leaving = null;
      this.scale = Math.max(0.45F, Math.min((float)this.field_22789 / 860.0F, (float)this.field_22790 / 440.0F));
      this.originX = (float)Math.round(((float)this.field_22789 - 860.0F * this.scale) / 2.0F);
      this.originY = (float)Math.round(((float)this.field_22790 - 440.0F * this.scale) / 2.0F);
      this.loadServers();
      this.layoutTiles();
      this.baseSkin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
      this.skinWidget = new class_8765(
         Math.round(130.0F * this.scale), Math.round(170.0F * this.scale), this.field_22787.method_31974(), () -> this.worn(this.baseSkin.get())
      );
      this.skinWidget.method_48229(this.px(626.0F), this.py(96.0F));
      this.method_37063(this.skinWidget);
      int fieldWidth = Math.round(134.0F * this.scale);
      int inset = Math.round((22.0F * this.scale - 8.0F) / 2.0F);
      this.nameField = new class_342(this.field_22793, this.px(624.0F), this.py(307.0F) + inset, fieldWidth, 10, class_2561.method_43470("Name"));
      styleField(this.nameField);
      this.nameField.method_1880(32);
      this.nameField.method_1852(NameTags.customName());
      this.nameField.method_47404(class_2561.method_43470("Name on your screen").method_10862(Theme.crispStyle().method_36139(7040888)));
      this.nameField.method_1863(value -> NameTags.setCustomName(value.trim()));
      this.method_37063(this.nameField);
      this.skinField = new class_342(this.field_22793, this.px(624.0F), this.py(343.0F) + inset, fieldWidth, 10, class_2561.method_43470("Skin"));
      styleField(this.skinField);
      this.skinField.method_1880(16);
      this.skinField.method_1890(value -> value.matches("[A-Za-z0-9_]*"));
      SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
      this.skinField.method_1852(skins == null ? "" : skins.selfPlayer());
      this.skinField.method_47404(class_2561.method_43470("Wear a player's skin").method_10862(Theme.crispStyle().method_36139(7040888)));
      this.skinField.method_1863(value -> {
         this.pendingSkin = value.trim();
         this.skinChangedAt = System.currentTimeMillis();
      });
      this.method_37063(this.skinField);
      this.showWidgets(false);
      if (!this.shown) {
         this.shown = true;
         MenuAudio.menuShown();
      }
   }

   private static void styleField(class_342 field) {
      field.method_1858(false);
      field.method_71503(false);
      field.method_1868(-855051);
      field.method_73210(Theme.fieldFormatter());
   }

   private class_8685 worn(class_8685 original) {
      SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
      return skins != null && skins.isActive() ? skins.selfSkin(original) : original;
   }

   private void loadServers() {
      this.servers.clear();

      try {
         class_641 list = new class_641(this.field_22787);
         list.method_2981();

         for (int i = 0; i < list.method_2984() && this.servers.size() < 3; i++) {
            this.servers.add(list.method_2982(i));
         }
      } catch (RuntimeException var3) {
         SoberClient.LOG.warn("Could not read the server list for Quick Join", var3);
      }

      if (this.servers.isEmpty()) {
         this.servers.add(new class_642("DonutSMP", "donutsmp.net", class_8678.field_45611));
      }
   }

   private void layoutTiles() {
      this.tiles.clear();
      this.tiles.add(new HomeScreen.Tile("single", 74.0F, 184.0F, 198.0F, 70.0F, 0.1F, () -> this.field_22787.method_1507(new class_526(this))));
      this.tiles.add(new HomeScreen.Tile("multi", 284.0F, 184.0F, 198.0F, 70.0F, 0.14F, this::openMultiplayer));
      List<String> small = new ArrayList<>(List.of("Realms", "Options", "Quit"));
      if (FabricLoader.getInstance().isModLoaded("modmenu")) {
         small.add(2, "Mods");
      }

      float gap = 7.0F;
      float each = (408.0F - gap * (float)(small.size() - 1)) / (float)small.size();

      for (int i = 0; i < small.size(); i++) {
         String label = small.get(i);

         Runnable action = switch (label) {
            case "Realms" -> () -> this.field_22787.method_1507(new class_4325(this));
            case "Options" -> () -> this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690));
            case "Mods" -> this::openMods;
            default -> () -> this.field_22787.method_1592();
         };
         this.tiles
            .add(
               new HomeScreen.Tile(
                  label, (float)Math.round(74.0F + (float)i * (each + gap)), 266.0F, (float)Math.round(each), 32.0F, 0.18F + (float)i * 0.03F, action
               )
            );
      }

      float serverEach = (408.0F - gap * 2.0F) / 3.0F;

      for (int i = 0; i < this.servers.size(); i++) {
         class_642 info = this.servers.get(i);
         this.tiles
            .add(
               new HomeScreen.Tile(
                  "server" + i,
                  (float)Math.round(74.0F + (float)i * (serverEach + gap)),
                  332.0F,
                  (float)Math.round(serverEach),
                  38.0F,
                  0.28F + (float)i * 0.03F,
                  () -> this.join(info)
               )
            );
      }
   }

   private void openMultiplayer() {
      this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500(this) : new class_4749(this)));
   }

   private void openMods() {
      try {
         Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
         this.field_22787.method_1507((class_437)type.getConstructor(class_437.class).newInstance(this));
      } catch (RuntimeException | ReflectiveOperationException var2) {
         SoberClient.LOG.error("Could not open the mod list", var2);
      }
   }

   private void join(class_642 info) {
      if (class_639.method_36224(info.field_3761)) {
         class_412.method_36877(this, this.field_22787, class_639.method_2950(info.field_3761), info, false, null);
      }
   }

   public boolean method_25422() {
      return false;
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25393() {
      super.method_25393();
      if (this.pendingSkin != null && System.currentTimeMillis() - this.skinChangedAt > 700L) {
         this.applySkin();
      }
   }

   private void applySkin() {
      SkinChange skins = SoberClient.MODULES.get(SkinChange.class);
      if (skins != null && this.pendingSkin != null) {
         skins.setSelfPlayer(this.pendingSkin);
      }

      this.pendingSkin = null;
   }

   public void method_25432() {
      this.applySkin();
      SoberClient.CONFIG.save();
   }

   private int px(float x) {
      return Math.round(this.originX + x * this.scale);
   }

   private int py(float y) {
      return Math.round(this.originY + y * this.scale);
   }

   private static int accent() {
      MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
      return 0xFF000000 | (menu != null ? menu.color() : Theme.accent());
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

   private static float clamp(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static float quint(float p) {
      float inverse = 1.0F - clamp(p);
      return 1.0F - inverse * inverse * inverse * inverse * inverse;
   }

   private static float smooth(float p) {
      p = clamp(p);
      return p < 0.5F ? 4.0F * p * p * p : 1.0F - (float)Math.pow((double)(-2.0F * p + 2.0F), 3.0) / 2.0F;
   }

   private float shown(float delay) {
      return quint((this.time - delay) / 0.6F);
   }

   private float leave() {
      if (this.leaving == null) {
         return 0.0F;
      } else {
         float p = clamp((float)(System.nanoTime() - this.leaveStart) / 1.0E9F / 0.22F);
         return p * p * p;
      }
   }

   private float animate(String key, boolean on, float speed) {
      float value = this.hover.getOrDefault(key, 0.0F);
      value += ((on ? 1.0F : 0.0F) - value) * (1.0F - (float)Math.exp((double)(-speed * this.delta)));
      if (Math.abs((on ? 1.0F : 0.0F) - value) < 0.002F) {
         value = on ? 1.0F : 0.0F;
      }

      this.hover.put(key, value);
      return value;
   }

   private void group(class_332 context, float delay, float dx, float dy, Runnable draw) {
      float in = this.shown(delay);
      float out = this.leave();
      float visible = in * (1.0F - out);
      if (!(visible <= 0.002F)) {
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(dx * (1.0F - in), dy * (1.0F - in) - 8.0F * out);
         this.fade = visible;

         try {
            draw.run();
         } finally {
            this.fade = 1.0F;
            matrices.popMatrix();
         }
      }
   }

   private class_2561 styled(String value, float size) {
      return size >= 1.6F ? Theme.large(value) : (size >= 1.1F ? Theme.bold(value) : Theme.text(value));
   }

   private void text(class_332 context, String value, float x, float y, float size, int color) {
      if (!ScreenStyle.formatted(value)) {
         String file = Theme.crispFile(size >= 1.1F);
         if (!CrispText.canDisplay(value, file)) {
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(size, size);
            context.method_51439(this.field_22793, this.styled(value, size), 0, 0, this.f(ScreenStyle.readable(color)), Theme.liquidGlass());
            matrices.popMatrix();
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

   private boolean glass(class_332 context, int x, int y, int w, int h, int r) {
      return ScreenStyle.glass(context, x, y, w, h, r, this.fade);
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

   private void rect(class_332 context, int x, int y, int w, int h, int r, int color) {
      Smooth.rect(context, x, y, w, h, r, this.f(color));
   }

   private void outline(class_332 context, int x, int y, int w, int h, int r, int color) {
      Smooth.outline(context, x, y, w, h, r, this.f(color));
   }

   private void logo(class_332 context, float x, float y, float size) {
      Logo3D.draw(context, x + size / 2.0F, y + size / 2.0F, size, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, this.fade, 0.7F);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      if (firstShown == 0L) {
         firstShown = System.nanoTime();
      }

      float intro = smooth((float)(System.nanoTime() - firstShown) / 1.0E9F / 1.2F);
      paintBackdrop(context, this.field_22789, this.field_22790, intro, this.parallaxX, this.parallaxY);
      float black = 1.0F - smooth((this.introElapsed() - 4.3F) / 1.1999998F);
      if (black > 0.002F) {
         context.method_25294(0, 0, this.field_22789, this.field_22790, alpha(197638, black));
      }

      if (Backdrop.requestMenu(this.field_22787)) {
         try {
            context.method_71048();
            context.method_71278();
         } catch (IllegalStateException var8) {
         }
      }
   }

   public static void paintBackdrop(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY) {
      paintBackdrop(context, width, height, intro, parallaxX, parallaxY, 1.0F);
   }

   public static void paintBackdrop(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY, float cover) {
      if (!(cover <= 0.002F)) {
         intro *= cover;
         if (Theme.liquidGlass()) {
            paintBright(context, width, height, intro, parallaxX, parallaxY, cover);
         } else {
            context.method_25294(0, 0, width, height, alpha(395018, cover));
            int accent = accent();
            float clock = (float)System.nanoTime() / 1.0E9F;
            float breathe = 0.5F + 0.5F * (float)Math.sin((double)(clock * 0.5F));
            float glowX = (float)width * 0.2F + (float)Math.sin((double)(clock * 0.15F)) * (float)width * 0.04F - parallaxX * 16.0F;
            float glowY = (float)height * 0.33F + (float)Math.cos((double)(clock * 0.12F)) * (float)height * 0.05F - parallaxY * 10.0F;
            Smooth.glow(context, Math.round(glowX), Math.round(glowY), Math.round((float)width * 0.46F), alpha(accent, (0.2F + 0.06F * breathe) * intro));
            float glowX2 = (float)width * 0.8F + (float)Math.cos((double)(clock * 0.13F)) * (float)width * 0.04F - parallaxX * 22.0F;
            float glowY2 = (float)height * 0.68F + (float)Math.sin((double)(clock * 0.16F)) * (float)height * 0.04F - parallaxY * 14.0F;
            Smooth.glow(
               context, Math.round(glowX2), Math.round(glowY2), Math.round((float)width * 0.42F), alpha(accent, (0.15F + 0.05F * (1.0F - breathe)) * intro)
            );
            Smooth.glow(
               context,
               Math.round((float)width * 0.62F - parallaxX * 18.0F),
               Math.round((float)height * 0.2F - parallaxY * 10.0F),
               Math.round((float)width * 0.3F),
               alpha(4059872, (0.06F + 0.02F * breathe) * intro)
            );
            MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
            if (menu == null || menu.stars.get()) {
               for (float[] star : STARS) {
                  float depth = 0.3F + star[4];
                  float sy = (star[1] + clock * 0.004F * depth) % 1.0F * ((float)height + 20.0F) - 10.0F;
                  float sx = star[0] * (float)width - parallaxX * 20.0F * depth;
                  sy -= parallaxY * 12.0F * depth;
                  float twinkle = 0.45F + 0.55F * (0.5F + 0.5F * (float)Math.sin((double)(clock * star[3] * 0.7F + star[2])));
                  if (star[4] > 0.86F) {
                     Smooth.softDot(context, sx, sy, 3.6F, alpha(16777215, 0.16F * twinkle * intro));
                     Smooth.softDot(context, sx, sy, 1.3F, alpha(16777215, 0.7F * twinkle * intro));
                  } else {
                     Smooth.softDot(context, sx, sy, 1.1F, alpha(15133165, 0.5F * twinkle * intro));
                  }
               }

               paintParticles(context, width, height, intro, parallaxX, parallaxY);
            }
         }
      }
   }

   public static void paintParticles(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY) {
      if (!(intro <= 0.01F)) {
         int accent = accent();
         float clock = (float)System.nanoTime() / 1.0E9F;

         for (int i = 0; i < 22; i++) {
            float[] star = STARS[i * 5 % STARS.length];
            float depth = 0.5F + star[4];
            float rise = ((star[1] - clock * 0.01F * depth) % 1.0F + 1.0F) % 1.0F;
            float x = star[0] * (float)width + (float)Math.sin((double)(clock * 0.3F + star[2])) * 12.0F - parallaxX * 26.0F * depth;
            float y = rise * ((float)height + 40.0F) - 20.0F - parallaxY * 16.0F * depth;
            float edge = Math.min(1.0F, Math.min(rise, 1.0F - rise) * 6.0F);
            float pulse = 0.5F + 0.5F * (float)Math.sin((double)(clock * star[3] * 0.8F + star[2]));
            float radius = 1.4F + star[4] * 2.8F;
            float a = intro * edge;
            Smooth.softDot(context, x, y, radius * 5.5F, alpha(accent, (0.07F + 0.06F * pulse) * a));
            Smooth.softDot(context, x, y, radius * 2.0F, alpha(accent, (0.3F + 0.22F * pulse) * a));
            Smooth.softDot(context, x, y, radius * 0.8F, alpha(16777215, (0.4F + 0.3F * pulse) * a));
         }
      }
   }

   private static void paintBright(class_332 context, int width, int height, float intro, float parallaxX, float parallaxY, float cover) {
      float clock = (float)System.nanoTime() / 1.0E9F;
      int mid = Math.round((float)height * 0.55F);
      context.method_25296(0, 0, width, mid, alpha(2759758, cover), alpha(9060222, cover));
      context.method_25296(0, mid, width, height, alpha(9060222, cover), alpha(3809349, cover));
      float fit = Math.max((float)width / 2400.0F, (float)height / 1350.0F) * 1.05F;
      int w = (int)Math.ceil((double)(2400.0F * fit));
      int h = (int)Math.ceil((double)(1350.0F * fit));
      float x = (float)(width - w) / 2.0F - parallaxX * 12.0F;
      float y = (float)(height - h) / 2.0F - parallaxY * 8.0F;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(x, y);

      try {
         context.method_25293(class_10799.field_56883, SAKURA, 0, 0, 0.0F, 0.0F, w, h, 2400, 1350, 2400, 1350, alpha(16777215, Math.max(intro, 0.001F) * cover));
      } catch (RuntimeException var25) {
         SoberClient.LOG.debug("Sakura backdrop unavailable", var25);
      } finally {
         matrices.popMatrix();
      }

      context.method_25294(0, 0, width, height, alpha(1181720, 0.32F * cover));

      for (int i = 0; i < 18; i++) {
         float[] star = STARS[i * 7 % STARS.length];
         float speed = 0.012F + star[4] * 0.012F;
         float fall = ((star[1] + clock * speed) % 1.0F + 1.0F) % 1.0F;
         float px = star[0] * (float)width
            + (float)Math.sin((double)(clock * (0.4F + star[3] * 0.2F) + star[2])) * 18.0F
            + fall * (float)width * 0.08F
            - parallaxX * 22.0F;
         float py = fall * ((float)height + 30.0F) - 15.0F - parallaxY * 14.0F;
         float edge = Math.min(1.0F, Math.min(fall, 1.0F - fall) * 8.0F);
         float size = 1.4F + star[4] * 1.6F;
         Smooth.softDot(context, px, py, size * 2.6F, alpha(16758736, 0.18F * edge * intro));
         Smooth.softDot(context, px, py, size, alpha(16766694, 0.85F * edge * intro));
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      long nano = System.nanoTime();
      this.delta = Math.min(0.05F, (float)(nano - this.lastFrame) / 1.0E9F);
      this.lastFrame = nano;
      this.time = (float)(nano - this.openedAt) / 1.0E9F - this.introShift;
      this.now = (float)nano / 1.0E9F;
      float targetX = this.field_22789 > 0 ? (float)mouseX / (float)this.field_22789 - 0.5F : 0.0F;
      float targetY = this.field_22790 > 0 ? (float)mouseY / (float)this.field_22790 - 0.5F : 0.0F;
      float follow = 1.0F - (float)Math.exp((double)(-3.0F * this.delta));
      this.parallaxX = this.parallaxX + (targetX - this.parallaxX) * follow;
      this.parallaxY = this.parallaxY + (targetY - this.parallaxY) * follow;
      if (this.leaving != null && this.leave() >= 1.0F) {
         Runnable action = this.leaving;
         this.leaving = null;
         this.replay = true;
         action.run();
      } else {
         double mx = (double)(((float)mouseX - this.originX) / this.scale);
         double my = (double)(((float)mouseY - this.originY) / this.scale);
         this.surface(context, mouseX, mouseY);
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(this.originX, this.originY);
         matrices.scale(this.scale, this.scale);

         try {
            this.introWindow(context);
            this.heroLogo(context);
            this.group(context, 0.0F, 0.0F, -12.0F, () -> this.topBar(context));
            this.group(context, 0.05F, -22.0F, 0.0F, () -> this.title(context));

            for (HomeScreen.Tile tile : this.tiles) {
               this.group(context, tile.delay(), -22.0F, 0.0F, () -> this.drawTile(context, tile, this.leaving == null && tile.contains(mx, my)));
            }

            this.group(context, 0.26F, -22.0F, 0.0F, () -> this.text(context, "QUICK JOIN", 76.0F, 318.0F, 0.78F, -7367779));
            this.group(context, 0.12F, 0.0F, 0.0F, () -> this.skinCard(context));
            this.group(context, 0.34F, 0.0F, 6.0F, () -> this.footer(context));
         } finally {
            matrices.popMatrix();
         }

         this.showWidgets(this.shown(0.12F) * (1.0F - this.leave()) > 0.6F);
         super.method_25394(context, mouseX, mouseY, deltaTicks);
         this.introLabels(context, mouseX, mouseY);
         Notifications.render(context, true);
      }
   }

   private void surface(class_332 context, int mouseX, int mouseY) {
      float visible = this.shown(0.0F) * (1.0F - this.leave());
      if (!(visible <= 0.003F) && !Theme.liquidGlass()) {
         int accent = accent();
         float cell = 40.0F * this.scale;
         float reach = 170.0F * this.scale;
         float hover = this.animate("surface", mouseX > 0 && mouseY > 0 && mouseX < this.field_22789 - 1 && mouseY < this.field_22790 - 1, 4.0F);
         Smooth.glow(context, mouseX, mouseY, Math.round(reach * 0.9F), alpha(accent, 0.05F * visible * hover));

         for (float lineX = this.originX % cell; lineX < (float)this.field_22789; lineX += cell) {
            int x = Math.round(lineX);
            context.method_25294(x, 0, x + 1, this.field_22790, alpha(16777215, 0.018F * visible));
            float dx = Math.abs(lineX - (float)mouseX);
            if (!(dx >= reach) && !(hover <= 0.01F)) {
               float span = (float)Math.sqrt((double)(reach * reach - dx * dx));
               int peak = alpha(16777215, 0.075F * visible * hover * (1.0F - dx / reach) * (1.0F - dx / reach));
               context.method_25296(x, Math.round((float)mouseY - span), x + 1, mouseY, peak & 16777215, peak);
               context.method_25296(x, mouseY, x + 1, Math.round((float)mouseY + span), peak, peak & 16777215);
            }
         }

         for (float lineY = this.originY % cell; lineY < (float)this.field_22790; lineY += cell) {
            int y = Math.round(lineY);
            context.method_25294(0, y, this.field_22789, y + 1, alpha(16777215, 0.018F * visible));
            float dy = Math.abs(lineY - (float)mouseY);
            if (!(dy >= reach) && !(hover <= 0.01F)) {
               float span = (float)Math.sqrt((double)(reach * reach - dy * dy));
               int peak = alpha(16777215, 0.075F * visible * hover * (1.0F - dy / reach) * (1.0F - dy / reach));
               Smooth.horizontalGradient(context, Math.round((float)mouseX - span), y, mouseX, y + 1, peak & 16777215, peak);
               Smooth.horizontalGradient(context, mouseX, y, Math.round((float)mouseX + span), y + 1, peak, peak & 16777215);
            }
         }

         float drift = 0.5F + 0.5F * (float)Math.sin((double)(this.now * 0.35F));
         float markX = this.originX + 520.0F * this.scale - this.parallaxX * 14.0F;
         float markY = this.originY + 250.0F * this.scale - this.parallaxY * 10.0F + drift * 4.0F;
         float markSize = 330.0F * this.scale;
         float distance = (float)Math.hypot((double)((float)mouseX - markX), (double)((float)mouseY - markY));
         float reveal = this.animate("markReveal", distance < markSize * 0.55F, 4.5F) * clamp(1.2F - distance / (markSize * 0.6F));
         if (reveal > 0.01F) {
            Smooth.glow(context, Math.round(markX), Math.round(markY), Math.round(markSize * 0.55F), alpha(accent, 0.06F * reveal * visible));
         }

         if (!Theme.liquidGlass()) {
            Logo3D.draw(context, markX, markY, markSize, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (0.035F + 0.11F * reveal) * visible);
         }
      }
   }

   private void showWidgets(boolean visible) {
      if (this.skinWidget != null) {
         this.skinWidget.field_22764 = visible;
         this.nameField.field_22764 = visible;
         this.skinField.field_22764 = visible;
      }
   }

   private float introElapsed() {
      return this.introShift > 0.0F ? (float)(System.nanoTime() - this.openedAt) / 1.0E9F : 3.4028235E38F;
   }

   private float introCenterX() {
      return ((float)this.field_22789 / 2.0F - this.originX) / this.scale;
   }

   private float introCenterY() {
      return ((float)this.field_22790 * 0.43F - this.originY) / this.scale;
   }

   private float introBig() {
      return Math.min(230.0F, (float)this.field_22790 / this.scale * 0.48F);
   }

   private float[] introLogo(float elapsed) {
      float centerX = this.introCenterX();
      float centerY = this.introCenterY();
      float big = this.introBig();
      float enter = quint(elapsed / 0.9F);
      float move = smooth((elapsed - 3.4F) / 1.7999997F);
      float shrink = smooth((elapsed - 3.4F) / 1.7999997F);
      float size = big * (1.05F - 0.05F * enter);
      size += (30.0F - size) * shrink;
      float cx = centerX + (89.0F - centerX) * move;
      float cy = centerY + (123.0F - centerY) * move;
      return new float[]{cx, cy, size, move};
   }

   private void introWindow(class_332 context) {
      float elapsed = this.introElapsed();
      if (!(elapsed < 4.3F) && !(elapsed > 6.6F)) {
         float grow = smooth((elapsed - 4.3F) / 0.9999995F);
         float[] logo = this.introLogo(Math.min(elapsed, 5.2F));
         float sx = logo[0] - logo[2] * 0.72F;
         float sy = logo[1] - logo[2] * 0.62F;
         float sw = logo[2] * 1.44F;
         float sh = logo[2] * 1.24F;
         float ex = 40.0F;
         float ey = 70.0F;
         float ew = 780.0F;
         float eh = 322.0F;
         int x = Math.round(sx + (ex - sx) * grow);
         int y = Math.round(sy + (ey - sy) * grow);
         int w = Math.round(sw + (ew - sw) * grow);
         int h = Math.round(sh + (eh - sh) * grow);
         float alpha = clamp((elapsed - 4.3F) / 0.18F) * (1.0F - smooth((elapsed - 5.2F - 0.6F) / 0.7F)) * (1.0F - this.leave());
         if (!(alpha <= 0.003F)) {
            this.fade = alpha;
            this.rect(context, x, y, w, h, 16, -267710955);
            context.method_25296(x + 8, y + 1, x + w - 8, y + Math.min(h - 1, 60), this.f(218103807), this.f(16777215));
            this.outline(context, x, y, w, h, 16, 486539263);
            this.fade = 1.0F;
         }
      }
   }

   private void introLabels(class_332 context, int mouseX, int mouseY) {
      float elapsed = this.introElapsed();
      if (!(elapsed > 4.0F)) {
         float alpha = quint((elapsed - 0.2F) / 0.6F) * (1.0F - smooth((elapsed - 3.4F) / 0.45F));
         if (!(alpha <= 0.003F)) {
            int accent = accent();
            this.fade = alpha;
            MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
            boolean sound = menu == null || menu.audio.get();
            String version = FabricLoader.getInstance().getModContainer("soberclient").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
            this.text(context, "SOBERCLIENT", 14.0F, 12.0F, 0.72F, -7367779);
            this.text(context, "V" + version, (float)(this.field_22789 - 14) - this.textWidth("V" + version, 0.72F), 12.0F, 0.72F, -7367779);
            boolean overSound = mouseX >= 8 && mouseX < 90 && mouseY >= this.field_22790 - 24 && mouseY < this.field_22790 - 4;
            this.text(context, sound ? "SOUND ON" : "SOUND OFF", 14.0F, (float)(this.field_22790 - 17), 0.72F, overSound ? -855051 : -4275510);
            Smooth.softDot(
               context,
               14.0F + this.textWidth(sound ? "SOUND ON" : "SOUND OFF", 0.72F) + 7.0F,
               (float)this.field_22790 - 14.5F,
               3.2F,
               this.f(sound ? alpha(accent, 0.95F) : -2140442760)
            );
            String skip = "SKIP INTRO";
            float skipWidth = this.textWidth(skip, 0.72F);
            boolean overSkip = (float)mouseX >= (float)this.field_22789 - skipWidth - 34.0F && mouseY >= this.field_22790 - 24 && mouseY < this.field_22790 - 4;
            float skipX = (float)(this.field_22789 - 26) - skipWidth;
            this.text(context, skip, skipX, (float)(this.field_22790 - 17), 0.72F, overSkip ? -855051 : -4275510);
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float)this.field_22789 - 18.0F, (float)this.field_22790 - 14.5F);
            matrices.rotate(1.5707964F);
            Smooth.triangle(context, 0.0F, 0.0F, 6.0F, this.f(overSkip ? -855051 : -4275510));
            matrices.popMatrix();
            float progress = clamp(elapsed / 3.4F);
            int barX = this.field_22789 / 2 - 30;
            int barY = this.field_22790 - 14;
            context.method_25294(barX, barY, barX + 60, barY + 1, this.f(587202559));
            Smooth.horizontalGradient(
               context, barX, barY, barX + Math.max(1, Math.round(60.0F * progress)), barY + 1, this.f(alpha(accent, 0.2F)), this.f(alpha(accent, 0.95F))
            );
            this.fade = 1.0F;
         }
      }
   }

   private void introName(class_332 context, float elapsed) {
      float alpha = quint((elapsed - 0.35F) / 0.6F) * (1.0F - smooth((elapsed - 3.4F + 0.1F) / 0.4F));
      if (!(alpha <= 0.003F)) {
         float centerX = this.introCenterX();
         float top = this.introCenterY() + this.introBig() * 0.56F + (1.0F - alpha) * 3.0F;
         this.fade = alpha;
         this.spaced(context, "SOBERCLIENT", centerX, top, 1.25F, 5.5F, -2499358);
         this.spaced(context, "CLIENT", centerX, top + 16.0F, 0.7F, 3.2F, -7367779);
         this.rect(context, Math.round(centerX - 9.0F), Math.round(top + 29.0F), 18, 1, 0, 1090519039);
         this.fade = 1.0F;
      }
   }

   private void spaced(class_332 context, String value, float centerX, float y, float size, float spacing, int color) {
      float total = -spacing;

      for (int i = 0; i < value.length(); i++) {
         total += this.textWidth(String.valueOf(value.charAt(i)), size) + spacing;
      }

      float x = centerX - total / 2.0F;

      for (int i = 0; i < value.length(); i++) {
         String letter = String.valueOf(value.charAt(i));
         this.text(context, letter, x, y, size, color);
         x += this.textWidth(letter, size) + spacing;
      }
   }

   private void skipIntro() {
      if (this.introElapsed() < 4.3F) {
         this.openedAt = System.nanoTime() - 4300000256L;
      }
   }

   private void heroLogo(class_332 context) {
      int accent = accent();
      float elapsed = this.time + this.introShift;
      float out = this.leave();
      float targetCx = 89.0F;
      float targetCy = 123.0F;
      float targetSize = 30.0F;
      float cx = targetCx;
      float cy = targetCy;
      float size = targetSize;
      float yaw = 0.0F;
      float pitch = 0.0F;
      float depth = 0.0F;
      float shine = 0.0F;
      float shinePos = 0.0F;
      float glow = 0.3F;
      float visible;
      if (this.introShift > 0.0F && elapsed < 5.2F) {
         float[] logo = this.introLogo(elapsed);
         cx = logo[0];
         cy = logo[1];
         size = logo[2];
         float move = logo[3];
         float drift = clamp(elapsed / 3.4F);
         yaw = (-0.34F + 0.1F * drift) * (1.0F - move) + 0.22F * (float)Math.sin(3.141592653589793 * (double)move);
         pitch = 0.07F * (1.0F - move);
         depth = 1.0F - move * move;
         float first = clamp((elapsed - 0.6F) / 1.3F);
         float second = clamp((elapsed - 2.2F) / 1.1F);
         if (first > 0.0F && first < 1.0F) {
            shine = (float)Math.sin(3.141592653589793 * (double)first);
            shinePos = -0.25F + 1.5F * smooth(first);
         } else if (second > 0.0F && second < 1.0F) {
            shine = 0.6F * (float)Math.sin(3.141592653589793 * (double)second);
            shinePos = -0.25F + 1.5F * smooth(second);
         }

         visible = quint(elapsed / 0.6F);
         glow = 0.18F + 0.22F * (1.0F - move);
         this.introName(context, elapsed);
      } else if (this.introShift > 0.0F) {
         visible = 1.0F;
         glow = 0.3F + 0.35F * (float)Math.exp((double)(-(elapsed - 5.2F) * 5.0F));
      } else {
         float flip = quint((this.time - 0.05F) / 0.75F);
         visible = flip;
         yaw = -1.45F * (1.0F - flip);
         pitch = 0.18F * (1.0F - flip);
         depth = 1.0F - flip;
      }

      float shown = visible * (1.0F - out);
      if (!(shown <= 0.003F)) {
         this.fade = shown;
         float breathe = 0.5F + 0.5F * (float)Math.sin((double)(this.now * 1.2F));
         Smooth.glow(context, Math.round(cx), Math.round(cy), Math.round(size * 0.9F), this.f(alpha(accent, glow + 0.1F * breathe)));
         this.fade = 1.0F;
         Logo3D.draw(context, cx, cy, size, yaw, pitch, depth, shine, shinePos, shown, 1.0F);
      }
   }

   private void topBar(class_332 context) {
      if (!this.glass(context, 20, 8, 820, 34, 17)) {
         this.rect(context, 20, 8, 820, 34, 17, 318767103);
         this.outline(context, 20, 8, 820, 34, 17, 536870911);
      }

      ScreenStyle.topLight(context, 20, 8, 820, 17, 0.35F * this.fade);
      Smooth.glow(context, 42, 25, 16, this.f(alpha(4059872, 0.3F)));
      this.logo(context, 32.0F, 15.0F, 20.0F);
      this.text(context, "SoberClient", 58.0F, 20.0F, 1.15F, -855051);
      String name = this.shownName();
      int pill = Math.round(30.0F + this.textWidth(name, 1.0F));
      int pillX = 830 - pill;
      if (!this.glass(context, pillX, 12, pill, 26, 13)) {
         this.rect(context, pillX, 12, pill, 26, 13, 352321535);
         this.outline(context, pillX, 12, pill, 26, 13, 536870911);
      }

      if (this.fade > 0.9F) {
         Avatars.draw(context, this.worn(this.baseSkin.get()), pillX + 6, 17, 16);
      }

      this.text(context, name, (float)(pillX + 26), 21.0F, 1.0F, -855051);
      LocalTime local = LocalTime.now();
      String clock = String.format("%02d:%02d", local.getHour(), local.getMinute());
      this.text(context, clock, (float)(pillX - 12) - this.textWidth(clock, 1.0F), 21.0F, 1.0F, -855051);
   }

   private String shownName() {
      String custom = NameTags.customName().trim();
      if (!custom.isEmpty()) {
         return custom;
      } else {
         return this.field_22787.method_1548() == null ? "Player" : this.field_22787.method_1548().method_1676();
      }
   }

   private void title(class_332 context) {
      int accent = accent();
      if (!Theme.liquidGlass()) {
         Smooth.glow(context, 200, 124, 120, this.f(alpha(accent, 0.2F)));
      }

      for (int k = 0; k < 8 && !Theme.liquidGlass(); k++) {
         double angle = 6.283185307179586 * (double)k / 8.0;
         CrispText.draw(
            context,
            "SoberClient",
            "montserrat-bold.ttf",
            112.0F + (float)Math.cos(angle) * 1.6F,
            136.0F + (float)Math.sin(angle) * 1.6F,
            37.0F,
            -0.01F,
            this.f(alpha(accent, 0.16F))
         );
      }

      CrispText.draw(context, "SoberClient", "montserrat-bold.ttf", 112.0F, 136.0F, 37.0F, -0.01F, this.f(-855051));
      String minecraft = FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
      String violet = FabricLoader.getInstance().getModContainer("soberclient").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
      this.text(context, "Fabric " + minecraft + "  ·  SoberClient " + violet, 76.0F, 150.0F, 1.0F, -4275510);
   }

   private void drawTile(class_332 context, HomeScreen.Tile tile, boolean over) {
      float glow = this.animate(tile.key(), over, 11.0F);
      float press = tile.key().equals(this.pressedKey) ? clamp(1.0F - (float)(System.nanoTime() - this.pressedAt) / 1.0E9F / 0.35F) : 0.0F;
      int x = Math.round(tile.x());
      int y = Math.round(tile.y());
      int w = Math.round(tile.w());
      int h = Math.round(tile.h());
      int accent = accent();
      boolean big = tile.key().equals("single") || tile.key().equals("multi");
      boolean server = tile.key().startsWith("server");
      int radius = big ? 16 : (server ? 12 : h / 2);
      float lit = Math.max(glow, press);
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();

      try {
         if (lit > 0.01F) {
            Smooth.glow(context, x + w / 2, y + h / 2, Math.round((float)Math.max(w, h) * 0.7F), this.f(alpha(accent, 0.2F * lit)));
         }

         if (!this.glass(context, x, y, w, h, radius)) {
            this.rect(context, x, y, w, h, radius, lerp(352321535, 587202559, glow));
            this.outline(context, x, y, w, h, radius, lerp(536870911, 16777215, lit));
         }

         ScreenStyle.topLight(context, x, y, w, radius, 0.4F * (1.0F - lit) * this.fade);
         ScreenStyle.neon(context, x, y, w, h, radius, accent, lit * this.fade);
         if (big) {
            boolean single = tile.key().equals("single");
            if (glow > 0.01F) {
               ScreenStyle.neon(context, x + 14, y + 18, 34, 34, 11, accent, glow * this.fade);
            }

            this.rect(context, x + 14, y + 18, 34, 34, 11, lerp(385875967, alpha(accent, 0.92F), glow));
            this.outline(context, x + 14, y + 18, 34, 34, 11, lerp(620756991, 16777215, glow));
            Icons.draw(context, single ? Icons.Icon.CUBE : Icons.Icon.GRID, x + 23, y + 27, 16, this.f(lerp(-855051, -15069692, glow)));
            this.text(context, single ? "Singleplayer" : "Multiplayer", (float)(x + 58), (float)(y + 22), 1.35F, -855051);
            this.text(
               context,
               single ? "Jump back into your worlds" : "Servers and LAN games",
               (float)(x + 58),
               (float)(y + 40),
               0.95F,
               lerp(-4275510, -855051, glow * 0.4F)
            );
         } else if (server) {
            int index = Integer.parseInt(tile.key().substring(6));
            class_642 info = this.servers.get(index);
            float wave = (this.now * 0.5F + (float)index * 0.33F) % 1.0F;
            float ring = smooth(wave);
            Smooth.softDot(context, (float)x + 14.0F, (float)y + 19.0F, 4.0F + 6.0F * ring, this.f(alpha(accent, 0.4F * (1.0F - ring))));
            Smooth.glow(context, x + 14, y + 19, 10, this.f(alpha(accent, 0.55F)));
            Smooth.circle(context, x + 14, y + 19, 4, this.f(accent));
            this.text(context, this.fit(info.field_3752, (float)(w - 34), 1.1F), (float)(x + 26), (float)(y + 9), 1.1F, -855051);
            this.text(context, this.fit(info.field_3761, (float)(w - 34), 0.85F), (float)(x + 26), (float)(y + 23), 0.85F, -4275510);
         } else {
            String var25 = tile.key();

            Icons.Icon icon = switch (var25) {
               case "Realms" -> Icons.Icon.SPARKLE;
               case "Options" -> Icons.Icon.COLUMNS;
               case "Mods" -> Icons.Icon.WINDOW;
               default -> Icons.Icon.CLOSE;
            };
            int color = tile.key().equals("Quit") ? lerp(-855051, -38037, glow) : -855051;
            Icons.draw(context, icon, x + 14, y + 10, 12, this.f(color));
            this.text(context, tile.key(), (float)(x + 33), (float)(y + 12), 1.1F, color);
         }
      } finally {
         matrices.popMatrix();
      }
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

   private void skinCard(class_332 context) {
      int accent = accent();
      float breathe = 0.5F + 0.5F * (float)Math.sin((double)(this.now * 1.1F));
      Smooth.glow(context, 691, 232, 124, this.f(alpha(accent, 0.12F + 0.05F * breathe)));
      if (!this.glass(context, 604, 84, 174, 296, 22)) {
         this.rect(context, 604, 84, 174, 296, 22, 318767103);
      }

      ScreenStyle.neon(context, 604, 84, 174, 296, 22, accent, (0.4F + 0.2F * breathe) * this.fade);
      String name = this.shownName();
      this.text(context, name, 691.0F - this.textWidth(name, 1.15F) / 2.0F, 272.0F, 1.15F, -855051);
      this.text(context, "Drag to spin", 691.0F - this.textWidth("Drag to spin", 0.85F) / 2.0F, 286.0F, 0.85F, -7367779);
      this.text(context, "NAME", 616.0F, 297.0F, 0.72F, -7367779);
      this.text(context, "SKIN", 616.0F, 333.0F, 0.72F, -7367779);
      this.fieldBox(context, "nameBox", 614, 307, this.nameField != null && this.nameField.method_25370());
      this.fieldBox(context, "skinBox", 614, 343, this.skinField != null && this.skinField.method_25370());
   }

   private void fieldBox(class_332 context, String key, int x, int y, boolean focused) {
      float focus = this.animate(key, focused, 12.0F);
      int accent = accent();
      if (focus > 0.01F) {
         Smooth.glow(context, x + 77, y + 11, 70, this.f(alpha(accent, 0.16F * focus)));
      }

      if (!this.glass(context, x, y, 154, 22, 11)) {
         this.rect(context, x, y, 154, 22, 11, lerp(251658239, 419430399, focus));
         this.outline(context, x, y, 154, 22, 11, lerp(486539263, 16777215, focus));
      }

      ScreenStyle.neon(context, x, y, 154, 22, 11, accent, focus * this.fade);
   }

   private static boolean inBox(double mx, double my, float x, float y) {
      return mx >= (double)x && mx < (double)(x + 154.0F) && my >= (double)y && my < (double)(y + 22.0F);
   }

   private void footer(class_332 context) {
      String minecraft = FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
      this.text(context, "SoberClient  ·  Minecraft " + minecraft, 22.0F, 420.0F, 0.85F, -7367779);
      String copyright = "Copyright Mojang AB. Do not distribute!";
      this.text(context, copyright, 838.0F - this.textWidth(copyright, 0.85F), 420.0F, 0.85F, -7367779);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (this.leaving != null) {
         return true;
      } else if (this.introElapsed() < 4.3F) {
         if (click.method_74245() == 0
            && click.comp_4798() >= 8.0
            && click.comp_4798() < 90.0
            && click.comp_4799() >= (double)(this.field_22790 - 24)
            && click.comp_4799() < (double)(this.field_22790 - 4)) {
            MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
            if (menu != null) {
               menu.audio.set(!menu.audio.get());
            }

            UiSounds.click();
            return true;
         } else {
            this.skipIntro();
            return true;
         }
      } else if (super.method_25402(click, doubled)) {
         return true;
      } else if (click.method_74245() != 0) {
         return false;
      } else {
         double mx = (click.comp_4798() - (double)this.originX) / (double)this.scale;
         double my = (click.comp_4799() - (double)this.originY) / (double)this.scale;
         if (this.nameField != null && this.nameField.field_22764 && inBox(mx, my, 614.0F, 307.0F)) {
            this.method_25395(this.nameField);
            return true;
         } else if (this.skinField != null && this.skinField.field_22764 && inBox(mx, my, 614.0F, 343.0F)) {
            this.method_25395(this.skinField);
            return true;
         } else {
            for (HomeScreen.Tile tile : this.tiles) {
               if (this.shown(tile.delay()) > 0.7F && tile.contains(mx, my)) {
                  UiSounds.click();
                  this.pressedKey = tile.key();
                  this.pressedAt = System.nanoTime();
                  this.leaving = tile.action();
                  this.leaveStart = System.nanoTime() + 90000000L;
                  return true;
               }
            }

            this.method_25395(null);
            return false;
         }
      }
   }

   public boolean method_25404(class_11908 input) {
      if (!(this.introElapsed() < 4.3F) || input.comp_4795() != 32 && input.comp_4795() != 257 && input.comp_4795() != 256) {
         if (input.comp_4795() == 257 && this.skinField != null && this.skinField.method_25370()) {
            this.applySkin();
            return true;
         } else {
            return super.method_25404(input);
         }
      } else {
         this.skipIntro();
         return true;
      }
   }

   private static float[][] stars() {
      Random random = new Random(1337L);
      float[][] stars = new float[130][];

      for (int i = 0; i < stars.length; i++) {
         stars[i] = new float[]{random.nextFloat(), random.nextFloat(), random.nextFloat() * 6.28F, 0.6F + random.nextFloat() * 1.6F, random.nextFloat()};
      }

      return stars;
   }

   private static record Tile(String key, float x, float y, float w, float h, float delay, Runnable action) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
      }
   }
}
