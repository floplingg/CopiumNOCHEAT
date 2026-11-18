package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.module.Module;
import dev.violet.modules.AntiCheat;
import dev.violet.modules.Freecam;
import dev.violet.modules.Freelook;
import dev.violet.modules.Hotkeys;
import dev.violet.modules.Hud;
import dev.violet.modules.InventoryHud;
import dev.violet.modules.MediaHider;
import dev.violet.modules.SpotifyPlayer;
import dev.violet.modules.StaffLive;
import dev.violet.modules.SusChunk;
import dev.violet.modules.Zoom;
import dev.violet.util.StaffList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_5321;
import net.minecraft.class_640;
import net.minecraft.class_642;
import net.minecraft.class_742;
import net.minecraft.class_9779;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public final class HudRenderer {
   private static final int TEXT = -723723;
   private static final int MUTED = -5329226;
   private static final Map<Module, Float> PROGRESS = new HashMap<>();
   private static final Map<String, Float> KEYS = new HashMap<>();
   private static final int EDGE = 5;
   private static final int SPACING = 4;
   private static final int SNAP = 4;
   private static final EnumMap<HudLayout.Element, HudLayout.Box> SHOWN = new EnumMap<>(HudLayout.Element.class);
   private static final class_2960 TOGIS = class_2960.method_60655("violet", "dynamic/togis_keyboard");
   private static final int TOGIS_W = 186;
   private static final int TOGIS_H = 141;
   private static boolean togisLoaded;
   private static boolean togisReady;
   private static final int[][] TOGIS_KEYS = togisKeys();
   private static final Map<Integer, Float> TOGIS_GLOW = new HashMap<>();
   private static long togisFrame = System.nanoTime();
   private static long lastKeysFrame = System.nanoTime();
   private static long lastFrame = System.nanoTime();
   private static HudLayout.Element dragging;
   private static int grabX;
   private static int grabY;
   private static int guideX = -1;
   private static int guideY = -1;
   private static float hudScale = 1.0F;
   private static float tickProgress = 1.0F;
   private static final class_2583 HUD_FONT = class_2583.field_24360.method_27704(Fonts.Family.Montserrat.font(Fonts.Weight.Bold));
   private static final String HUD_FILE = "geist-semibold.ttf";
   private static final float HUD_EM = 8.6F;
   private static final float HEADER_EM = 7.2F;
   private static final float HEADER_TRACK = 0.42F;
   private static final float HEADER_LOGO = 7.0F;
   private static final float BRAND_LOGO = 9.5F;
   private static final String BRAND_LEFT = "SOBER";
   private static final String BRAND_RIGHT = "CLIENT";
   private static final int BRAND_VIOLET = -5329226;
   private static final float BRAND_EM = 7.2F;
   private static final float BRAND_TRACK = 0.3F;
   private static class_1799 overworldIcon;
   private static class_1799 netherIcon;
   private static class_1799 endIcon;
   private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

   private HudRenderer() {
   }

   private static Hud hud() {
      return SoberClient.MODULES.get(Hud.class);
   }

   public static float uiScale(class_332 context) {
      return Math.max(0.3F, (float)context.method_51443() / 540.0F);
   }

   public static void render(class_332 context, class_9779 tickCounter) {
      class_310 mc = class_310.method_1551();
      float delta = frameDelta();
      tickProgress = tickCounter.method_60637(true);
      Backdrop.capture(mc);
      MediaHider.coverHotbar(context);
      if (!(mc.field_1755 instanceof ModuleMenu)) {
         Hud hud = hud();
         boolean cards = hud != null && hud.isActive() && !mc.field_1690.field_1842 && mc.field_1724 != null && !mc.method_53526().method_53536();
         boolean standalone = !cards && hud != null && !mc.field_1690.field_1842 && mc.field_1724 != null && !mc.method_53526().method_53536();
         boolean editing = (cards || standalone) && mc.field_1755 instanceof class_408 && hud.chatEditing.get();
         float scale = uiScale(context);
         hudScale = scale;
         int sw = Math.round((float)context.method_51421() / scale);
         int sh = Math.round((float)context.method_51443() / scale);
         if (!editing) {
            endDrag();
         }

         context.method_51448().pushMatrix();
         context.method_51448().scale(scale, scale);
         if (editing) {
            updateDrag(mc, hud, sw, sh);
         }

         if (cards) {
            renderCards(context, hud, sw, sh, editing, delta, false);
         } else if (standalone) {
            renderCards(context, hud, sw, sh, editing, delta, true);
         } else {
            SHOWN.clear();
         }

         boolean hintsShown = !mc.field_1690.field_1842 && mc.field_1724 != null;
         Freelook freelook = SoberClient.MODULES.get(Freelook.class);
         float hintAlpha = freelook != null && hintsShown ? freelook.hint() : 0.0F;
         if (hintAlpha > 0.01F) {
            cameraHint(context, sw, hintAlpha, "FREELOOK", freelook.getInfoString());
         }

         Freecam freecam = SoberClient.MODULES.get(Freecam.class);
         float camAlpha = freecam != null && hintsShown ? freecam.hint() : 0.0F;
         if (camAlpha > 0.01F) {
            cameraHint(context, sw, camAlpha, "FREECAM", freecam.getInfoString());
         }

         Zoom zoom = SoberClient.MODULES.get(Zoom.class);
         float zoomAlpha = zoom != null && hintsShown && !(hintAlpha > 0.01F) && !(camAlpha > 0.01F) ? zoom.hint() : 0.0F;
         if (zoomAlpha > 0.01F) {
            cameraHint(context, sw, zoomAlpha, "ZOOM", zoom.getInfoString());
         }

         if (editing) {
            context.method_71048();
            drawEditing(context, mc, sw, sh);
         }

         context.method_51448().popMatrix();
         if (!mc.field_1690.field_1842) {
            context.method_71048();
            Notifications.render(context);
         }
      }
   }

   private static boolean chatEditing(class_310 mc) {
      Hud hud = hud();
      return hud != null
         && hud.chatEditing.get()
         && !mc.field_1690.field_1842
         && mc.field_1724 != null
         && mc.field_1755 instanceof class_408
         && !mc.method_53526().method_53536();
   }

   private static HudLayout.Element shownAt(double x, double y) {
      HudLayout.Element[] values = HudLayout.Element.values();

      for (int i = values.length - 1; i >= 0; i--) {
         HudLayout.Box box = SHOWN.get(values[i]);
         if (box != null && box.contains(x, y)) {
            return values[i];
         }
      }

      return null;
   }

   private static boolean inChatField(class_310 mc, double guiY) {
      return guiY >= (double)(mc.method_22683().method_4502() - 14);
   }

   public static boolean chatPress(double guiX, double guiY, int button) {
      class_310 mc = class_310.method_1551();
      if (chatEditing(mc) && !inChatField(mc, guiY)) {
         double x = guiX / (double)hudScale;
         double y = guiY / (double)hudScale;
         HudLayout.Element element = shownAt(x, y);
         if (element == null) {
            return false;
         } else {
            if (element == HudLayout.Element.SPOTIFY && button == 0) {
               HudLayout.Box spotify = SHOWN.get(element);
               if (spotify != null
                  && SpotifyPanel.INSTANCE.press((x - (double)spotify.x()) / (double)spotify.scale(), (y - (double)spotify.y()) / (double)spotify.scale())) {
                  return true;
               }
            }

            if (button == 1) {
               HudLayout.resetScale(element);
               HudLayout.save();
               UiSounds.click();
               return true;
            } else if (button != 0) {
               return false;
            } else {
               HudLayout.Box box = SHOWN.get(element);
               dragging = element;
               grabX = (int)Math.round(x) - box.x();
               grabY = (int)Math.round(y) - box.y();
               UiSounds.click();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean chatScroll(double guiX, double guiY, double amount) {
      class_310 mc = class_310.method_1551();
      if (chatEditing(mc) && !inChatField(mc, guiY) && amount != 0.0) {
         HudLayout.Element element = shownAt(guiX / (double)hudScale, guiY / (double)hudScale);
         if (element == null) {
            return false;
         } else {
            HudLayout.scale(element, 0.05F * (float)Math.signum(amount));
            HudLayout.save();
            UiSounds.key(0.3F);
            return true;
         }
      } else {
         return false;
      }
   }

   private static void endDrag() {
      if (dragging != null) {
         dragging = null;
         guideX = -1;
         guideY = -1;
         HudLayout.save();
      }
   }

   private static void updateDrag(class_310 mc, Hud hud, int sw, int sh) {
      if (dragging != null) {
         if (GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 0) != 1) {
            endDrag();
         } else {
            HudLayout.Box box = SHOWN.get(dragging);
            if (box != null) {
               int x = (int)Math.round(mc.field_1729.method_68879(mc.method_22683()) / (double)hudScale) - grabX;
               int y = (int)Math.round(mc.field_1729.method_68883(mc.method_22683()) / (double)hudScale) - grabY;
               int[] snapped = snap(dragging, x, y, box.width(), box.height(), sw, sh, SHOWN, hud.snapping.get() && !mc.method_74187());
               HudLayout.move(dragging, snapped[0], snapped[1], sw, sh, box.width(), box.height());
            }
         }
      }
   }

   private static int[] snap(
      HudLayout.Element element, int x, int y, int w, int h, int sw, int sh, Map<HudLayout.Element, HudLayout.Box> others, boolean enabled
   ) {
      guideX = -1;
      guideY = -1;
      x = Math.max(0, Math.min(sw - w, x));
      y = Math.max(0, Math.min(sh - h, y));
      if (!enabled) {
         return new int[]{x, y};
      } else {
         List<int[]> xs = new ArrayList<>();
         List<int[]> ys = new ArrayList<>();
         xs.add(new int[]{0, 0});
         xs.add(new int[]{5, 5});
         xs.add(new int[]{sw - w, sw - 1});
         xs.add(new int[]{sw - 5 - w, sw - 5});
         xs.add(new int[]{Math.round((float)(sw - w) / 2.0F), sw / 2});
         ys.add(new int[]{0, 0});
         ys.add(new int[]{5, 5});
         ys.add(new int[]{sh - h, sh - 1});
         ys.add(new int[]{sh - 5 - h, sh - 5});
         ys.add(new int[]{Math.round((float)(sh - h) / 2.0F), sh / 2});

         for (Entry<HudLayout.Element, HudLayout.Box> entry : others.entrySet()) {
            if (entry.getKey() != element) {
               HudLayout.Box o = entry.getValue();
               xs.add(new int[]{o.x(), o.x()});
               xs.add(new int[]{o.x() + o.width() - w, o.x() + o.width()});
               xs.add(new int[]{o.x() + o.width() + 4, o.x() + o.width() + 4});
               xs.add(new int[]{o.x() - 4 - w, o.x() - 4});
               xs.add(new int[]{o.x() + (o.width() - w) / 2, o.x() + o.width() / 2});
               ys.add(new int[]{o.y(), o.y()});
               ys.add(new int[]{o.y() + o.height() - h, o.y() + o.height()});
               ys.add(new int[]{o.y() + o.height() + 4, o.y() + o.height() + 4});
               ys.add(new int[]{o.y() - 4 - h, o.y() - 4});
               ys.add(new int[]{o.y() + (o.height() - h) / 2, o.y() + o.height() / 2});
            }
         }

         int bestX = x;
         int bestY = y;
         int dx = 5;
         int dy = 5;

         for (int[] c : xs) {
            int d = Math.abs(c[0] - x);
            if (d < dx && c[0] >= 0 && c[0] <= sw - w) {
               dx = d;
               bestX = c[0];
               guideX = c[1];
            }
         }

         for (int[] cx : ys) {
            int d = Math.abs(cx[0] - y);
            if (d < dy && cx[0] >= 0 && cx[0] <= sh - h) {
               dy = d;
               bestY = cx[0];
               guideY = cx[1];
            }
         }

         return new int[]{bestX, bestY};
      }
   }

   private static void guides(class_332 context, int sw, int sh) {
      int thickness = Math.max(1, Math.round(1.0F / hudScale));
      if (guideX >= 0) {
         context.method_25294(guideX, 0, guideX + thickness, sh, -1930103563);
      }

      if (guideY >= 0) {
         context.method_25294(0, guideY, sw, guideY + thickness, -1930103563);
      }
   }

   private static void hint(class_332 context, String value, double mx, double my, int sw, int sh) {
      int w = textWidth(value) + 16;
      int x = (int)Math.round(mx) + 12;
      int y = (int)Math.round(my) + 14;
      if (x + w > sw - 2) {
         x = (int)Math.round(mx) - 12 - w;
      }

      if (y + 18 > sh - 16) {
         y = (int)Math.round(my) - 14 - 18;
      }

      x = Math.max(2, x);
      y = Math.max(2, y);
      Smooth.rect(context, x, y, w, 18, 3, -266856421);
      Smooth.outline(context, x, y, w, 18, 3, -14013906);
      text(context, class_310.method_1551().field_1772, value, x + 8, y + 5, -723723);
   }

   private static void drawEditing(class_332 context, class_310 mc, int sw, int sh) {
      double guiY = mc.field_1729.method_68883(mc.method_22683());
      double mx = mc.field_1729.method_68879(mc.method_22683()) / (double)hudScale;
      double my = guiY / (double)hudScale;
      HudLayout.Element active = dragging != null ? dragging : (inChatField(mc, guiY) ? null : shownAt(mx, my));
      if (dragging != null) {
         guides(context, sw, sh);
      }

      if (active != null) {
         HudLayout.Box box = SHOWN.get(active);
         if (box != null) {
            Smooth.outline(context, box.x() - 2, box.y() - 2, box.width() + 4, box.height() + 4, 4, dragging != null ? -723723 : -1711999755);
            String label = dragging != null ? "Shift for free move" : Math.round(box.scale() * 100.0F) + "%  /  Scroll to resize  /  Right click reset";
            hint(context, label, mx, my, sw, sh);
         }
      }
   }

   private static float frameDelta() {
      long now = System.nanoTime();
      float delta = Math.min(0.1F, (float)(now - lastFrame) / 1.0E9F);
      lastFrame = now;
      return delta;
   }

   private static void renderCards(class_332 context, Hud hud, int sw, int sh, boolean ghost, float delta, boolean standaloneOnly) {
      class_310 mc = class_310.method_1551();
      class_327 font = mc.field_1772;
      List<Module> modules = modules(delta);
      SHOWN.clear();

      for (HudLayout.Element element : HudLayout.Element.values()) {
         if ((
               !standaloneOnly
                  || element == HudLayout.Element.REGION
                  || element == HudLayout.Element.SPOTIFY
                  || element == HudLayout.Element.HOTKEYS
                  || element == HudLayout.Element.AC_OFF
            )
            && enabled(hud, element)
            && (element != HudLayout.Element.MODULE_LIST || !modules.isEmpty() || ghost)) {
            int baseW = baseWidth(element, mc, modules);
            int baseH = baseHeight(element, modules, mc, hud);
            HudLayout.Box box = HudLayout.box(element, sw, sh, baseW, baseH);
            SHOWN.put(element, box);
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)box.x(), (float)box.y());
            context.method_51448().scale(box.scale(), box.scale());
            drawElement(context, mc, hud, font, element, modules, ghost);
            context.method_51448().popMatrix();
         }
      }
   }

   private static boolean enabled(Hud hud, HudLayout.Element element) {
      return switch (element) {
         case WATERMARK -> hud.watermark.get() || hud.infoLine.get();
         case FPS -> hud.fps.get() && !hud.infoLine.get();
         case BPS -> hud.bps.get() && !hud.infoLine.get();
         case TPS -> hud.tps.get() && !hud.infoLine.get();
         case XYZ -> hud.coordinates.get();
         case RADAR -> hud.radar.get();
         case MODULE_LIST -> hud.moduleList.get();
         case STAFF_LIST -> hud.staffList.get();
         case KEYSTROKES -> hud.keystrokes.get();
         case INVENTORY -> {
            InventoryHud inventory = SoberClient.MODULES.get(InventoryHud.class);
            yield inventory != null && inventory.isActive();
         }
         case REGION -> {
            dev.violet.modules.RegionMap map = SoberClient.MODULES.get(dev.violet.modules.RegionMap.class);
            yield map != null && map.isActive();
         }
         case SPOTIFY -> {
            SpotifyPlayer player = SoberClient.MODULES.get(SpotifyPlayer.class);
            yield player != null && player.isActive();
         }
         case STAFF_LIVE -> {
            StaffLive live = SoberClient.MODULES.get(StaffLive.class);
            yield live != null && live.isActive() && live.hud.get();
         }
         case TOGIS -> hud.togisKeyboard.get();
         case HOTKEYS -> Hotkeys.shouldShowHud();
         case AC_OFF -> AntiCheat.card() != null;
      };
   }

   private static void togis(class_332 context) {
      if (!togisLoaded) {
         togisLoaded = true;

         try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655("violet", "textures/hud/togis_keyboard.png"))) {
            Smooth.register(TOGIS, class_1011.method_4309(stream));
            togisReady = true;
         } catch (Exception var20) {
            SoberClient.LOG.error("Could not load the Togis keyboard picture", var20);
         }
      }

      if (togisReady) {
         context.method_25293(class_10799.field_56883, TOGIS, 0, 0, 0.0F, 0.0F, 186, 141, 371, 282, 371, 282, -1);
         class_310 mc = class_310.method_1551();
         long now = System.nanoTime();
         float delta = Math.min(0.1F, (float)(now - togisFrame) / 1.0E9F);
         togisFrame = now;
         float k = 0.5013477F;
         int accent = Theme.accent();

         for (int[] key : TOGIS_KEYS) {
            boolean down = class_3675.method_15987(mc.method_22683(), key[0]);
            float shown = TOGIS_GLOW.getOrDefault(key[0], 0.0F);
            shown += ((down ? 1.0F : 0.0F) - shown) * (1.0F - (float)Math.exp((double)(-delta * (down ? 32.0F : 9.0F))));
            if (shown < 0.01F) {
               shown = 0.0F;
            }

            TOGIS_GLOW.put(key[0], shown);
            if (!(shown <= 0.0F)) {
               int x = Math.round((float)key[1] * k);
               int y = Math.round((float)key[2] * k);
               int w = Math.round((float)key[3] * k);
               int h = Math.round((float)key[4] * k);
               int grow = Math.round(1.5F * shown);
               Smooth.glow(context, x + w / 2, y + h / 2, Math.max(w, h), Math.round(112.0F * shown) << 24 | accent & 16777215);
               Smooth.rect(context, x - grow, y - grow, w + grow * 2, h + grow * 2, 4, Math.round(85.0F * shown) << 24 | accent & 16777215);
               Smooth.outline(context, x - grow, y - grow, w + grow * 2, h + grow * 2, 4, Math.round(224.0F * shown) << 24 | 16777215);
            }
         }
      }
   }

   private static int[][] togisKeys() {
      List<int[]> keys = new ArrayList<>();
      int[] top = new int[]{81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 91};
      int[] middle = new int[]{65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 39};
      int[] columns = new int[]{10, 45, 80, 117, 152, 190, 225, 260, 295, 328, 362};

      for (int i = 0; i < top.length; i++) {
         keys.add(new int[]{top[i], Math.max(0, columns[i] - 16), 44, 32, 46});
      }

      for (int i = 0; i < middle.length; i++) {
         keys.add(new int[]{middle[i], Math.max(0, columns[i] - 16), 106, 32, 46});
      }

      int[] bottom = new int[]{90, 88, 67, 86, 66, 78, 77, 44, 46, 47};
      int[] bottomColumns = new int[]{8, 47, 80, 112, 146, 178, 210, 243, 276, 308};

      for (int i = 0; i < bottom.length; i++) {
         keys.add(new int[]{bottom[i], Math.max(0, bottomColumns[i] - 15), 168, 30, 44});
      }

      int[] toolbar = new int[]{15, 58, 100, 145, 185, 228, 275, 315, 358};

      for (int i = 0; i < toolbar.length; i++) {
         keys.add(new int[]{49 + i, Math.max(0, toolbar[i] - 17), 2, 34, 28});
      }

      keys.add(new int[]{259, 330, 170, 40, 40});
      keys.add(new int[]{257, 314, 224, 56, 48});
      keys.add(new int[]{32, 92, 226, 206, 52});
      keys.add(new int[]{263, 148, 236, 24, 32});
      keys.add(new int[]{262, 222, 236, 24, 32});
      keys.add(new int[]{340, 0, 224, 40, 50});
      keys.add(new int[]{341, 42, 224, 44, 50});
      return keys.toArray(new int[0][]);
   }

   private static class_2561 hudText(String value) {
      return class_2561.method_43470(value).method_10862(HUD_FONT);
   }

   private static int textWidth(String value) {
      return CrispText.canDisplay(value, "geist-semibold.ttf")
         ? (int)Math.ceil((double)CrispText.width(value, "geist-semibold.ttf", 8.6F, 0.1F))
         : class_310.method_1551().field_1772.method_27525(hudText(value));
   }

   private static void text(class_332 context, class_327 font, String value, int x, int y, int color) {
      if (CrispText.canDisplay(value, "geist-semibold.ttf")) {
         CrispText.draw(context, value, "geist-semibold.ttf", (float)x, (float)y + 7.45F, 8.6F, 0.1F, color);
      } else {
         context.method_51439(font, hudText(value), x, y, color, false);
      }
   }

   static float labelWidth(String label) {
      return CrispText.width(label, "geist-semibold.ttf", 7.2F, 0.42F);
   }

   static void label(class_332 context, String label, float x, float y, int color) {
      CrispText.draw(context, label, "geist-semibold.ttf", x, y + 7.6F, 7.2F, 0.42F, color);
   }

   static float headerWidth(String label) {
      return logoWidth(7.0F) + 4.0F + labelWidth(label);
   }

   static float logoWidth(float height) {
      BrandTextures.Sprite orbit = BrandTextures.get("orbit");
      return orbit == null ? height * 2.0F : height * (float)orbit.width() / (float)orbit.height();
   }

   static float logo(class_332 context, float x, float y, float height, int color) {
      float width = logoWidth(height);
      BrandTextures.Sprite orbit = BrandTextures.fit("orbit", width * Smooth.pixelsPerUnit(context));
      if (orbit != null) {
         VioletScreen.image(context, orbit, x, y, width, height, color);
      }

      return width;
   }

   static void header(class_332 context, String label, float x, float y) {
      float w = logo(context, x, y + 1.7F, 7.0F, -1);
      label(context, label, x + w + 4.0F, y, -2565922);
   }

   static void glass(class_332 context, int x, int y, int w, int h, int r, float opacity) {
      Smooth.liquid(context, x, y, w, h, r, opacity, false);
      Smooth.rect(context, x, y, w, h, Math.max(0, Math.min(r, Math.min(w, h) / 2)), MenuKit.alpha(-2146430958, opacity));
   }

   static void panel(class_332 context, int x, int y, int w, int h, int edge) {
      if (Theme.liquidGlass()) {
         glass(context, x, y, w, h, Math.min(10, Math.min(w, h) / 2), 1.0F);
      } else {
         Smooth.rect(context, x, y, w, h, 3, HudPalette.card());
         Smooth.outline(context, x, y, w, h, 3, -13750733);
      }

      if (edge != 0) {
         Smooth.rect(context, x + 1, y + 5, 2, Math.max(2, h - 10), 1, MenuKit.lerp(edge, -1, 0.3F));
      }
   }

   private static int edgeColor() {
      return 0;
   }

   private static int pillWidth(String label, String value) {
      return 20 + Math.round(labelWidth(label)) + textWidth(value);
   }

   private static float brandPart(String part) {
      return CrispText.width(part, "geist-semibold.ttf", 7.2F, 0.3F);
   }

   private static int brandWidth() {
      return Math.round(brandPart("SOBER") + brandPart("CLIENT"));
   }

   private static int brandBlock() {
      return Math.round(logoWidth(9.5F) + 5.0F) + brandWidth();
   }

   private static void brand(class_332 ctx, int x) {
      float w = logo(ctx, (float)x, 5.25F, 9.5F, -1);
      float tx = (float)(x + Math.round(w + 5.0F));
      CrispText.draw(ctx, "SOBER", "geist-semibold.ttf", tx, 12.6F, 7.2F, 0.3F, -723723);
      CrispText.draw(ctx, "CLIENT", "geist-semibold.ttf", tx + brandPart("SOBER"), 12.6F, 7.2F, 0.3F, -5329226);
   }

   private static void cameraHint(class_332 ctx, int sw, float alpha, String title, String value) {
      float lw = labelWidth(title) - 3.024F;
      float vw = CrispText.width(value, "geist-semibold.ttf", 8.6F, 0.1F);
      int w = Math.round(17.0F + lw + 9.0F + vw + 11.0F);
      int h = 18;
      int x = (sw - w) / 2;
      int y = topClearance(x, x + w) + Math.round((1.0F - alpha) * -6.0F);
      Smooth.rect(ctx, x, y, w, h, 3, MenuKit.alpha(HudPalette.card(), alpha));
      Smooth.outline(ctx, x, y, w, h, 3, MenuKit.alpha(-13750733, alpha));
      Smooth.circle(ctx, x + 10, y + 9, 2, MenuKit.alpha(-723723, alpha));
      label(ctx, title, (float)x + 17.0F, (float)y + 4.6F, MenuKit.alpha(-2565922, alpha));
      CrispText.draw(ctx, value, "geist-semibold.ttf", (float)x + 17.0F + lw + 9.0F, (float)y + 12.6F, 8.6F, 0.1F, MenuKit.alpha(-723723, alpha));
   }

   static int topClearance(int x0, int x1) {
      int y = 6;

      for (HudLayout.Box box : SHOWN.values()) {
         if (box.y() <= 40 && box.x() < x1 && box.x() + box.width() > x0) {
            y = Math.max(y, box.y() + box.height() + 5);
         }
      }

      return y;
   }

   private static int watermarkWidth() {
      return 14 + brandBlock();
   }

   private static String value(HudLayout.Element element, class_310 mc) {
      return switch (element) {
         case FPS -> Integer.toString(mc.method_47599());
         case BPS -> String.format(Locale.ROOT, "%.1f", HudMetrics.bps());
         case TPS -> HudMetrics.hasTps() ? String.format(Locale.ROOT, "%.1f", HudMetrics.tps()) : "--";
         case XYZ -> mc.field_1724 == null
         ? "0  64  0"
         : (int)Math.floor(mc.field_1724.method_23317())
            + "  "
            + (int)Math.floor(mc.field_1724.method_23318())
            + "  "
            + (int)Math.floor(mc.field_1724.method_23321());
         default -> "";
      };
   }

   private static int baseWidth(HudLayout.Element element, class_310 mc, List<Module> modules) {
      return switch (element) {
         case WATERMARK -> infoLineOn() ? infoWidth(mc) : watermarkWidth();
         case FPS, BPS, TPS -> pillWidth(element.name(), value(element, mc));
         case XYZ -> coordinatesWidth(mc);
         case RADAR -> 88;
         case MODULE_LIST -> Math.max(76, modules.stream().mapToInt(m -> textWidth(Theme.label(m.name)) + 20).max().orElse(76));
         case STAFF_LIST -> 180;
         case KEYSTROKES -> keysBackground() ? 86 : 66;
         case INVENTORY -> 178;
         case REGION -> RegionMap.width(SoberClient.MODULES.get(dev.violet.modules.RegionMap.class));
         case SPOTIFY -> SpotifyPanel.INSTANCE.width(false);
         case STAFF_LIVE -> 186;
         case TOGIS -> 186;
         case HOTKEYS -> hotkeysWidth();
         case AC_OFF -> {
            AcStatusCard.Model card = AntiCheat.card();
            yield card == null ? 1 : AcStatusCard.width(card);
         }
      };
   }

   private static int baseHeight(HudLayout.Element element, List<Module> modules, class_310 mc, Hud hud) {
      return switch (element) {
         case XYZ -> 8 + 12 * coordinateRows(mc).size();
         case RADAR -> 88;
         case MODULE_LIST -> Math.max(17, modules.size() * 18 - 1);
         case STAFF_LIST -> 23 + 18 * Math.max(1, Math.min(hud.staffShown.get(), StaffList.online(mc, hud).size()));
         case KEYSTROKES -> keysBackground() ? 106 : 80;
         case INVENTORY -> 85;
         case REGION -> RegionMap.height(SoberClient.MODULES.get(dev.violet.modules.RegionMap.class));
         case SPOTIFY -> SpotifyPanel.INSTANCE.height(false);
         case STAFF_LIVE -> 23 + 18 * Math.max(1, liveEntries().size());
         case TOGIS -> 141;
         case HOTKEYS -> 23 + 18 * Math.max(1, hotkeyLines().size());
         case AC_OFF -> {
            AcStatusCard.Model card = AntiCheat.card();
            yield card == null ? 1 : AcStatusCard.height(card);
         }
         default -> 20;
      };
   }

   private static List<Module> modules(float delta) {
      List<Module> list = new ArrayList<>();

      for (Module module : SoberClient.MODULES.all()) {
         if (module.showInHud()) {
            float target = module.isActive() ? 1.0F : 0.0F;
            float current = PROGRESS.getOrDefault(module, 0.0F);
            current += (target - current) * (1.0F - (float)Math.exp((double)(-delta * 13.0F * Theme.animationSpeed())));
            if (Math.abs(current - target) < 0.002F) {
               current = target;
            }

            PROGRESS.put(module, current);
            if (current > 0.01F) {
               list.add(module);
            }
         }
      }

      list.sort(Comparator.<Module>comparingInt(m -> textWidth(Theme.label(m.name))).reversed().thenComparing(m -> m.name));
      return list;
   }

   private static void drawElement(class_332 ctx, class_310 mc, Hud hud, class_327 font, HudLayout.Element element, List<Module> modules, boolean preview) {
      switch (element) {
         case WATERMARK:
            if (infoLineOn()) {
               infoLine(ctx, mc, font);
            } else {
               watermark(ctx);
            }
            break;
         case FPS:
         case BPS:
         case TPS:
         default:
            String label = element.name();
            String value = value(element, mc);
            panel(ctx, 0, 0, pillWidth(label, value), 20, edgeColor());
            label(ctx, label, 8.0F, 5.8F, -2565922);
            text(ctx, font, value, 12 + Math.round(labelWidth(label)), 6, -723723);
            break;
         case XYZ:
            coordinates(ctx, mc, font);
            break;
         case RADAR:
            radar(ctx, mc, font, hud);
            break;
         case MODULE_LIST:
            moduleList(ctx, font, modules, preview);
            break;
         case STAFF_LIST:
            staffList(ctx, mc, hud, font);
            break;
         case KEYSTROKES:
            keystrokes(ctx, mc, font);
            break;
         case INVENTORY:
            inventory(ctx, mc, font);
            break;
         case REGION:
            RegionMap.draw(ctx, mc, SoberClient.MODULES.get(dev.violet.modules.RegionMap.class));
            break;
         case SPOTIFY:
            HudLayout.Box box = SHOWN.get(element);
            double mx = mc.field_1729.method_68879(mc.method_22683()) / (double)hudScale;
            double my = mc.field_1729.method_68883(mc.method_22683()) / (double)hudScale;
            double lx = box == null ? -1.0 : (mx - (double)box.x()) / (double)box.scale();
            double ly = box == null ? -1.0 : (my - (double)box.y()) / (double)box.scale();
            SpotifyPanel.INSTANCE.draw(ctx, lx, ly, preview, mc.field_1755 instanceof class_408);
            break;
         case STAFF_LIVE:
            staffLive(ctx, font);
            break;
         case TOGIS:
            togis(ctx);
            break;
         case HOTKEYS:
            hotkeys(ctx, font);
            break;
         case AC_OFF:
            AcStatusCard.Model card = AntiCheat.card();
            if (card != null) {
               AcStatusCard.draw(ctx, card);
            }
      }
   }

   private static List<HudRenderer.CoordinateRow> coordinateRows(class_310 mc) {
      if (overworldIcon == null) {
         overworldIcon = new class_1799(class_1802.field_8270);
         netherIcon = new class_1799(class_1802.field_8328);
         endIcon = new class_1799(class_1802.field_20399);
      }

      if (mc.field_1724 != null && mc.field_1687 != null) {
         double x = mc.field_1724.method_23317();
         double z = mc.field_1724.method_23321();
         int y = (int)Math.floor(mc.field_1724.method_23318());
         class_5321<class_1937> dimension = mc.field_1687.method_27983();
         if (dimension == class_1937.field_25181) {
            return List.of(new HudRenderer.CoordinateRow(endIcon, coordinates(x, y, z), true));
         } else {
            boolean nether = dimension == class_1937.field_25180;
            String overworld = nether ? coordinates(x * 8.0, y, z * 8.0) : coordinates(x, y, z);
            String netherText = nether ? coordinates(x, y, z) : coordinates(x / 8.0, y, z / 8.0);
            return List.of(new HudRenderer.CoordinateRow(overworldIcon, overworld, !nether), new HudRenderer.CoordinateRow(netherIcon, netherText, nether));
         }
      } else {
         return List.of(new HudRenderer.CoordinateRow(overworldIcon, "0  64  0", true), new HudRenderer.CoordinateRow(netherIcon, "0  64  0", false));
      }
   }

   private static String coordinates(double x, int y, double z) {
      return (int)Math.floor(x) + "  " + y + "  " + (int)Math.floor(z);
   }

   private static int coordinatesWidth(class_310 mc) {
      int widest = 0;

      for (HudRenderer.CoordinateRow row : coordinateRows(mc)) {
         widest = Math.max(widest, textWidth(row.text()));
      }

      return 29 + widest;
   }

   private static void coordinates(class_332 ctx, class_310 mc, class_327 font) {
      List<HudRenderer.CoordinateRow> rows = coordinateRows(mc);
      int width = coordinatesWidth(mc);
      int height = 8 + 12 * rows.size();
      panel(ctx, 0, 0, width, height, edgeColor());
      Matrix3x2fStack matrices = ctx.method_51448();

      for (int i = 0; i < rows.size(); i++) {
         HudRenderer.CoordinateRow row = rows.get(i);
         int y = 4 + i * 12;
         matrices.pushMatrix();
         matrices.translate(8.0F, (float)(y + 1));
         matrices.scale(0.625F, 0.625F);
         ctx.method_51427(row.icon(), 0, 0);
         matrices.popMatrix();
         text(ctx, font, row.text(), 21, y + 2, row.current() ? -723723 : -5329226);
      }
   }

   private static void card(class_332 ctx, int x, int y, int w, int h, int r) {
      if (r < 40) {
         r = 3;
      }

      if (Theme.liquidGlass()) {
         glass(ctx, x, y, w, h, Math.max(r, Math.min(10, Math.min(w, h) / 2)), 1.0F);
      } else {
         Smooth.rect(ctx, x, y, w, h, r, HudPalette.card());
         Smooth.outline(ctx, x, y, w, h, r, -13750733);
      }
   }

   private static void radar(class_332 ctx, class_310 mc, class_327 font, Hud hud) {
      int c = 44;
      int r = 42;
      if (Theme.liquidGlass()) {
         Smooth.rect(ctx, 0, 0, 88, 88, 44, -1944185034);
         Smooth.rect(ctx, 2, 2, 84, 84, 42, 317581311);
         Smooth.sheen(ctx, 0, 0, 88, 88, 44, 285212671);
         Smooth.ring(ctx, c, c, 43, 1290659839);
      } else {
         card(ctx, 0, 0, 88, 88, 44);
         Smooth.rect(ctx, 2, 2, 84, 84, 42, 351597813);
      }

      Smooth.ring(ctx, c, c, 43, 1895101685);
      Smooth.ring(ctx, c, c, 21, 586478837);
      Smooth.horizontalGradient(ctx, c - r + 6, c, c, c + 1, 16053493, 552924405);
      Smooth.horizontalGradient(ctx, c + 1, c, c + r - 6, c + 1, 552924405, 16053493);
      ctx.method_25296(c, c - r + 6, c + 1, c, 16053493, 552924405);
      ctx.method_25296(c, c + 1, c + 1, c + r - 6, 552924405, 16053493);
      boolean rotate = hud.radarRotate.get();
      float playerYaw = mc.field_1724 == null ? 180.0F : mc.field_1724.method_5705(tickProgress);
      double yaw = Math.toRadians(rotate ? (double)playerYaw : 180.0);
      double fx = -Math.sin(yaw);
      double fz = Math.cos(yaw);
      double rx = -Math.cos(yaw);
      double rz = -Math.sin(yaw);
      String[] letters = new String[]{"N", "E", "S", "W"};
      int[][] dirs = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
      Matrix3x2fStack letterMatrices = ctx.method_51448();

      for (int i = 0; i < 4; i++) {
         double sx = (double)dirs[i][0] * rx + (double)dirs[i][1] * rz;
         double sy = -((double)dirs[i][0] * fx + (double)dirs[i][1] * fz);
         class_2561 letter = Theme.bold(letters[i]);
         letterMatrices.pushMatrix();
         letterMatrices.translate((float)((double)c + sx * (double)(r - 9)), (float)((double)c + sy * (double)(r - 9)));
         letterMatrices.scale(0.9F, 0.9F);
         letterMatrices.translate((float)(-font.method_27525(letter)) / 2.0F, -4.0F);
         ctx.method_51439(font, letter, 0, 0, i == 0 ? -2565922 : -5329226, false);
         letterMatrices.popMatrix();
      }

      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 self = mc.field_1724.method_30950(tickProgress);
         double scale = (double)(r - 3) / (double)Math.max(1, hud.radarRange.get());
         if (hud.radarChunks.get()) {
            SusChunk sus = SoberClient.MODULES.get(SusChunk.class);
            if (sus != null) {
               radarArea(ctx, c, r, self, sus.radarChunks(), scale, fx, fz, rx, rz, sus.radarColor());
            }
         }

         if (hud.radarPlayers.get()) {
            for (class_742 other : mc.field_1687.method_18456()) {
               if (other != mc.field_1724) {
                  class_243 pos = other.method_30950(tickProgress);
                  double dx = pos.field_1352 - self.field_1352;
                  double dz = pos.field_1350 - self.field_1350;
                  double sx = (dx * rx + dz * rz) * scale;
                  double sy = -(dx * fx + dz * fz) * scale;
                  double distance = Math.hypot(sx, sy);
                  if (distance > (double)(r - 9)) {
                     if (!(distance > (double)((r - 9) * 3)) && !(distance < 0.01)) {
                        double ex = sx / distance * (double)(r - 4);
                        double ey = sy / distance * (double)(r - 4);
                        Smooth.circle(ctx, c + (int)Math.round(ex), c + (int)Math.round(ey), 2, -923470603);
                     }
                  } else {
                     int px = c + (int)Math.round(sx) - 5;
                     int py = c + (int)Math.round(sy) - 5;
                     Smooth.rect(ctx, px - 1, py - 1, 12, 12, 4, -723723);
                     Avatars.draw(ctx, other.method_52814(), px, py, 10);
                  }
               }
            }
         }

         Matrix3x2fStack matrices = ctx.method_51448();
         matrices.pushMatrix();
         matrices.translate((float)c, (float)c);
         if (!rotate) {
            matrices.rotate((float)Math.toRadians((double)(playerYaw - 180.0F)));
         }

         Smooth.triangle(ctx, 0.0F, 0.0F, 9.0F, -920844);
         matrices.popMatrix();
      }
   }

   private static void radarArea(
      class_332 ctx, int c, int r, class_243 self, LongSet chunks, double scale, double fx, double fz, double rx, double rz, int fill
   ) {
      if (!chunks.isEmpty()) {
         int edge = Math.min(255, (fill >>> 24) * 3 / 2) << 24
            | Math.min(255, (int)((float)(fill >> 16 & 0xFF) * 0.77F)) << 16
            | Math.min(255, (int)((float)(fill >> 8 & 0xFF) * 0.77F)) << 8
            | Math.min(255, (int)((float)(fill & 0xFF) * 0.77F));
         float angle = (float)Math.atan2(-fx, rx);
         int size = Math.max(1, (int)Math.ceil(16.0 * scale));
         Matrix3x2fStack matrices = ctx.method_51448();
         LongIterator keys = chunks.iterator();

         while (keys.hasNext()) {
            long key = keys.nextLong();
            int cx = class_1923.method_8325(key);
            int cz = class_1923.method_8332(key);
            double mx = (double)((cx << 4) + 8) - self.field_1352;
            double mz = (double)((cz << 4) + 8) - self.field_1350;
            double sx = (mx * rx + mz * rz) * scale;
            double sy = -(mx * fx + mz * fz) * scale;
            double distance = Math.hypot(sx, sy);
            if (distance > (double)(r - 5)) {
               if (!(distance > (double)((r - 5) * 3))) {
                  Smooth.circle(
                     ctx,
                     c + (int)Math.round(sx / distance * (double)(r - 4)),
                     c + (int)Math.round(sy / distance * (double)(r - 4)),
                     2,
                     fill & 16777215 | -1342177280
                  );
               }
            } else {
               double ox = (double)(cx << 4) - self.field_1352;
               double oz = (double)(cz << 4) - self.field_1350;
               double px = (ox * rx + oz * rz) * scale;
               double py = -(ox * fx + oz * fz) * scale;
               matrices.pushMatrix();
               matrices.translate((float)((double)c + px), (float)((double)c + py));
               matrices.rotate(angle);
               ctx.method_25294(0, 0, size, size, fill);
               if (!chunks.contains(class_1923.method_8331(cx, cz - 1))) {
                  ctx.method_25294(0, 0, size, 1, edge);
               }

               if (!chunks.contains(class_1923.method_8331(cx, cz + 1))) {
                  ctx.method_25294(0, size - 1, size, size, edge);
               }

               if (!chunks.contains(class_1923.method_8331(cx - 1, cz))) {
                  ctx.method_25294(0, 0, 1, size, edge);
               }

               if (!chunks.contains(class_1923.method_8331(cx + 1, cz))) {
                  ctx.method_25294(size - 1, 0, size, size, edge);
               }

               matrices.popMatrix();
            }
         }
      }
   }

   private static void moduleList(class_332 ctx, class_327 font, List<Module> modules, boolean preview) {
      if (modules.isEmpty()) {
         if (preview) {
            card(ctx, 0, 0, 96, 18, 6);
            text(ctx, font, "MODULE LIST", 8, 5, -5329226);
         }
      } else {
         int width = Math.max(76, modules.stream().mapToInt(m -> textWidth(Theme.label(m.name)) + 20).max().orElse(76));
         int row = 0;

         for (Module module : modules) {
            float p = PROGRESS.getOrDefault(module, 1.0F);
            int alpha = Math.round(255.0F * p);
            int index = row++;
            int y = index * 18;
            String name = Theme.label(module.name);
            int rowWidth = textWidth(name) + 20;
            int rowX = width - rowWidth + Math.round((1.0F - p) * 10.0F);
            int accent = 16053493;
            if (Theme.liquidGlass()) {
               glass(ctx, rowX, y, rowWidth, 16, 8, (float)alpha / 255.0F);
            } else {
               Smooth.rect(ctx, rowX, y, rowWidth, 16, 3, Math.round((float)((HudPalette.card() >>> 24) * alpha) / 255.0F) << 24 | HudPalette.card() & 16777215);
               Smooth.outline(ctx, rowX, y, rowWidth, 16, 3, Math.round((float)(255 * alpha) / 255.0F) << 24 | 3026483);
            }

            int barX = rowX + rowWidth - 6;
            Smooth.rect(ctx, barX, y + 4, 2, 8, 1, alpha << 24 | accent);
            text(ctx, font, name, rowX + 8, y + 5, alpha << 24 | 16053493);
         }
      }
   }

   private static List<HudRenderer.HotkeyLine> hotkeyLines() {
      List<HudRenderer.HotkeyLine> lines = new ArrayList<>();

      for (Module module : SoberClient.MODULES.all()) {
         int key = module.getKey();
         if (key >= 0) {
            lines.add(new HudRenderer.HotkeyLine(module.name, MenuKit.keyName(key).toUpperCase(Locale.ROOT), module.isActive()));
         }
      }

      return lines;
   }

   private static void watermark(class_332 ctx) {
      panel(ctx, 0, 0, watermarkWidth(), 20, edgeColor());
      brand(ctx, 6);
   }

   private static boolean infoLineOn() {
      Hud hud = SoberClient.MODULES.get(Hud.class);
      return hud != null && hud.infoLine.get();
   }

   private static List<HudRenderer.InfoPart> infoParts(class_310 mc) {
      Hud hud = SoberClient.MODULES.get(Hud.class);
      List<HudRenderer.InfoPart> parts = new ArrayList<>();
      if (hud.fps.get()) {
         parts.add(new HudRenderer.InfoPart(Integer.toString(mc.method_47599()), "FPS"));
      }

      if (hud.linePing.get() && mc.method_1562() != null && mc.field_1724 != null && !mc.method_1542()) {
         class_640 entry = mc.method_1562().method_2871(mc.field_1724.method_5667());
         if (entry != null) {
            parts.add(new HudRenderer.InfoPart(Integer.toString(entry.method_2959()), "ms"));
         }
      }

      if (hud.bps.get()) {
         parts.add(new HudRenderer.InfoPart(String.format(Locale.ROOT, "%.1f", HudMetrics.bps()), "BPS"));
      }

      if (hud.tps.get() && HudMetrics.hasTps()) {
         parts.add(new HudRenderer.InfoPart(String.format(Locale.ROOT, "%.1f", HudMetrics.tps()), "TPS"));
      }

      if (hud.lineServer.get()) {
         class_642 server = mc.method_1558();
         if (server != null) {
            String address = server.field_3761.toLowerCase(Locale.ROOT);
            int port = address.lastIndexOf(58);
            if (port > 0 && address.indexOf(58) == port) {
               address = address.substring(0, port);
            }

            parts.add(new HudRenderer.InfoPart(address, ""));
         } else if (mc.method_1542()) {
            parts.add(new HudRenderer.InfoPart("Singleplayer", ""));
         }
      }

      if (hud.lineTime.get()) {
         parts.add(new HudRenderer.InfoPart(LocalTime.now().format(CLOCK), ""));
      }

      return parts;
   }

   private static int infoWidth(class_310 mc) {
      Hud hud = SoberClient.MODULES.get(Hud.class);
      int w = hud.watermark.get() ? 6 + brandBlock() : -7;

      for (HudRenderer.InfoPart part : infoParts(mc)) {
         w += 15 + textWidth(part.value()) + (part.unit().isEmpty() ? 0 : 3 + unitWidth(part.unit()));
      }

      return w + 8;
   }

   private static int unitWidth(String unit) {
      return Math.round(labelWidth(unit.toUpperCase(Locale.ROOT)));
   }

   private static void infoLine(class_332 ctx, class_310 mc, class_327 font) {
      Hud hud = SoberClient.MODULES.get(Hud.class);
      int w = infoWidth(mc);
      panel(ctx, 0, 0, w, 20, edgeColor());
      int x = 6;
      boolean first = true;
      if (hud.watermark.get()) {
         brand(ctx, 6);
         x = 6 + brandBlock();
         first = false;
      }

      for (HudRenderer.InfoPart part : infoParts(mc)) {
         if (!first) {
            Smooth.rect(ctx, x + 7, 5, 1, 10, 0, 955577589);
            x += 15;
         } else {
            x += 2;
            first = false;
         }

         text(ctx, font, part.value(), x, 6, -723723);
         x += textWidth(part.value());
         if (!part.unit().isEmpty()) {
            label(ctx, part.unit().toUpperCase(Locale.ROOT), (float)(x + 3), 5.8F, -2565922);
            x += 3 + unitWidth(part.unit());
         }
      }
   }

   private static int hotkeysWidth() {
      List<HudRenderer.HotkeyLine> lines = hotkeyLines();
      int title = Math.round(headerWidth("HOTKEYS"));
      if (lines.isEmpty()) {
         return Math.max(title, textWidth("No keys bound")) + 16;
      } else {
         int name = 0;
         int key = 0;

         for (HudRenderer.HotkeyLine line : lines) {
            name = Math.max(name, textWidth(line.name()));
            key = Math.max(key, textWidth(line.key()));
         }

         return Math.max(title + 16, 16 + name + 16 + key + 10 + 8);
      }
   }

   private static void hotkeys(class_332 ctx, class_327 font) {
      List<HudRenderer.HotkeyLine> lines = hotkeyLines();
      int width = hotkeysWidth();
      panel(ctx, 0, 0, width, 23 + 18 * Math.max(1, lines.size()), edgeColor());
      header(ctx, "HOTKEYS", 8.0F, 6.3F);
      if (lines.isEmpty()) {
         text(ctx, font, "No keys bound", 8, 27, -5329226);
      } else {
         for (int i = 0; i < lines.size(); i++) {
            HudRenderer.HotkeyLine line = lines.get(i);
            int y = 23 + i * 18;
            Smooth.circle(ctx, 10, y + 8, 1, -5329226);
            text(ctx, font, line.name(), 16, y + 4, line.on() ? -723723 : -5329226);
            int capW = textWidth(line.key()) + 10;
            int capX = width - 8 - capW;
            Smooth.rect(ctx, capX, y + 1, capW, 14, 3, 351597813);
            Smooth.outline(ctx, capX, y + 1, capW, 14, 3, 653587701);
            text(ctx, font, line.key(), capX + 5, y + 4, -2565922);
         }
      }
   }

   private static void staffList(class_332 ctx, class_310 mc, Hud hud, class_327 font) {
      List<StaffList.Member> entries = StaffList.online(mc, hud);
      int count = Math.min(hud.staffShown.get(), entries.size());
      int accent = -723723;
      panel(ctx, 0, 0, 180, 23 + 18 * Math.max(1, count), edgeColor());
      header(ctx, "STAFF LIST", 8.0F, 6.3F);
      String online = count + " ONLINE";
      label(ctx, online, 172.0F - labelWidth(online), 6.3F, count > 0 ? accent : -5329226);
      if (count == 0) {
         text(ctx, font, "No staff online", 8, 27, -5329226);
      } else {
         for (int i = 0; i < count; i++) {
            StaffList.Member member = entries.get(i);
            class_640 entry = member.entry();
            int y = 23 + i * 18;
            Avatars.draw(ctx, entry.method_52810(), 8, y + 2, 12);
            String ping = Math.max(0, entry.method_2959()) + "ms";
            String name = member.name();
            int room = 172 - textWidth(ping) - 8 - 26;

            while (textWidth(name) > room && name.length() > 1) {
               name = name.substring(0, name.length() - 1);
            }

            text(ctx, font, name, 26, y + 4, -723723);
            text(ctx, font, ping, 172 - textWidth(ping), y + 4, accent);
         }
      }
   }

   private static List<StaffLive.Entry> liveEntries() {
      StaffLive live = SoberClient.MODULES.get(StaffLive.class);
      if (live == null) {
         return List.of();
      } else {
         List<StaffLive.Entry> result = new ArrayList<>();

         for (StaffLive.Entry entry : live.entries()) {
            if (entry.enabled()) {
               result.add(entry);
            }
         }

         return result;
      }
   }

   private static void staffLive(class_332 ctx, class_327 font) {
      List<StaffLive.Entry> entries = liveEntries();
      int width = 186;
      long count = entries.stream().filter(StaffLive.Entry::live).count();
      panel(ctx, 0, 0, width, 23 + 18 * Math.max(1, entries.size()), count > 0L ? -1750963 : edgeColor());
      header(ctx, "STAFF LIVE", 8.0F, 6.3F);
      String summary = count + " LIVE";
      if (count > 0L) {
         int sw = Math.round(labelWidth(summary)) + 10;
         Smooth.rect(ctx, width - 8 - sw, 5, sw, 12, 3, -1750963);
         label(ctx, summary, (float)(width - 8 - sw + 5), 5.2F, -1);
      } else {
         label(ctx, summary, (float)(width - 8) - labelWidth(summary), 6.3F, -5329226);
      }

      if (entries.isEmpty()) {
         text(ctx, font, "No streamers selected", 8, 27, -5329226);
      } else {
         float pulse = (float)(0.5 + 0.5 * Math.sin((double)System.currentTimeMillis() / 260.0));

         for (int i = 0; i < entries.size(); i++) {
            StaffLive.Entry entry = entries.get(i);
            int y = 23 + i * 18;
            if (entry.live()) {
               Smooth.circle(ctx, 11, y + 8, 5, Math.round(48.0F + 80.0F * pulse) << 24 | 15026253);
               Smooth.circle(ctx, 11, y + 8, 3, -1750963);
            } else {
               Smooth.circle(ctx, 11, y + 8, 3, -9539978);
            }

            int bw = textWidth(entry.badge()) + 10;
            int bx = width - 8 - bw;
            Smooth.rect(ctx, bx, y + 2, bw, 12, 3, entry.color());
            text(ctx, font, entry.badge(), bx + 5, y + 4, -1);
            int px = bx - 6 - textWidth(entry.platform());
            text(ctx, font, entry.platform(), px, y + 4, -5329226);
            String name = entry.name();

            while (textWidth(name) > px - 26 && name.length() > 1) {
               name = name.substring(0, name.length() - 1);
            }

            text(ctx, font, name, 20, y + 4, entry.live() ? -723723 : -5329226);
         }
      }
   }

   private static void key(class_332 ctx, class_327 font, String label, boolean pressed, int x, int y, int w, float delta) {
      float old = KEYS.getOrDefault(label, 0.0F);
      float value = old + ((pressed ? 1.0F : 0.0F) - old) * (1.0F - (float)Math.exp((double)(-delta * 17.0F)));
      KEYS.put(label, value);
      if (Theme.liquidGlass()) {
         glass(ctx, x, y, w, 18, 7, 1.0F);
         if (value > 0.01F) {
            Smooth.rect(ctx, x, y, w, 18, 7, Math.round(72.0F * value) << 24 | 16777215);
         }
      } else {
         Smooth.rect(ctx, x, y, w, 18, 3, HudPalette.card());
         if (value > 0.01F) {
            Smooth.rect(ctx, x, y, w, 18, 3, Math.round(242.0F * value) << 24 | 16053493);
         }

         Smooth.outline(ctx, x, y, w, 18, 3, value > 0.5F ? -723723 : -13750733);
      }

      int color = value > 0.5F ? -15856112 : -5329226;
      if (label.length() > 1) {
         float lw = labelWidth(label) - 3.024F;
         label(ctx, label, (float)x + ((float)w - lw) / 2.0F, (float)y + 3.8F, color);
      } else {
         float lw = CrispText.width(label, "geist-semibold.ttf", 8.6F, 0.1F) - 0.8600001F;
         CrispText.draw(ctx, label, "geist-semibold.ttf", (float)x + ((float)w - lw) / 2.0F, (float)y + 11.9F, 8.6F, 0.1F, color);
      }
   }

   private static void keystrokes(class_332 ctx, class_310 mc, class_327 font) {
      long now = System.nanoTime();
      float delta = Math.min(0.1F, (float)(now - lastKeysFrame) / 1.0E9F);
      lastKeysFrame = now;
      boolean background = keysBackground();
      int ox = background ? 0 : -10;
      int oy = background ? 0 : -21;
      if (background) {
         panel(ctx, 0, 0, 86, 106, edgeColor());
         header(ctx, "KEYS", 8.0F, 6.3F);
      }

      key(ctx, font, "W", mc.field_1690.field_1894.method_1434(), 34 + ox, 21 + oy, 18, delta);
      key(ctx, font, "A", mc.field_1690.field_1913.method_1434(), 10 + ox, 41 + oy, 18, delta);
      key(ctx, font, "S", mc.field_1690.field_1881.method_1434(), 34 + ox, 41 + oy, 18, delta);
      key(ctx, font, "D", mc.field_1690.field_1849.method_1434(), 58 + ox, 41 + oy, 18, delta);
      key(ctx, font, "LMB", mc.field_1690.field_1886.method_1434(), 10 + ox, 62 + oy, 30, delta);
      key(ctx, font, "RMB", mc.field_1690.field_1904.method_1434(), 46 + ox, 62 + oy, 30, delta);
      key(ctx, font, "SPACE", mc.field_1690.field_1903.method_1434(), 10 + ox, 83 + oy, 66, delta);
   }

   private static boolean keysBackground() {
      Hud hud = hud();
      return hud != null && hud.keysBackground.get();
   }

   private static void inventory(class_332 ctx, class_310 mc, class_327 font) {
      panel(ctx, 0, 0, 178, 85, edgeColor());
      header(ctx, "INVENTORY", 8.0F, 6.3F);
      InventoryHud module = SoberClient.MODULES.get(InventoryHud.class);

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = 24 + row * 18;
            class_1799 stack = mc.field_1724 == null ? class_1799.field_8037 : mc.field_1724.method_31548().method_5438(9 + row * 9 + col);
            if (module == null || module.emptySlots.get() || !stack.method_7960()) {
               Smooth.rect(ctx, x, y, 17, 17, 3, Theme.liquidGlass() ? 385875967 : 351597813);
            }

            if (!stack.method_7960()) {
               ctx.method_51427(stack, x, y);
               ctx.method_51431(font, stack, x, y);
            }
         }
      }
   }

   private static record CoordinateRow(class_1799 icon, String text, boolean current) {
   }

   private static record HotkeyLine(String name, String key, boolean on) {
   }

   private static record InfoPart(String value, String unit) {
   }
}
