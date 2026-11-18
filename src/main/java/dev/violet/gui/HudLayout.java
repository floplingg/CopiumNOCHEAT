package dev.violet.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import net.fabricmc.loader.api.FabricLoader;

public final class HudLayout {
   private static final int START = 0;
   private static final int CENTER = 1;
   private static final int END = 2;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("copium-hud-layout.json");
   private static final EnumMap<HudLayout.Element, HudLayout.Position> POSITIONS = new EnumMap<>(HudLayout.Element.class);

   private HudLayout() {
   }

   private static int place(int anchor, int offset, int screen, int size) {
      return switch (anchor) {
         case 1 -> Math.round((float)(screen - size) / 2.0F) + offset;
         case 2 -> screen - offset - size;
         default -> offset;
      };
   }

   private static int anchor(int pos, int size, int screen) {
      float center = (float)pos + (float)size / 2.0F;
      if (Math.abs(center - (float)screen / 2.0F) < 1.5F) {
         return 1;
      } else {
         return center > (float)screen / 2.0F ? 2 : 0;
      }
   }

   private static int offset(int anchor, int pos, int screen, int size) {
      return switch (anchor) {
         case 1 -> pos - Math.round((float)(screen - size) / 2.0F);
         case 2 -> screen - pos - size;
         default -> pos;
      };
   }

   public static HudLayout.Box box(HudLayout.Element element, int screenWidth, int screenHeight, int baseWidth, int baseHeight) {
      HudLayout.Position p = POSITIONS.get(element);
      int w = Math.max(1, Math.round((float)baseWidth * p.scale));
      int h = Math.max(1, Math.round((float)baseHeight * p.scale));
      int x = place(p.anchorX, p.x, screenWidth, w);
      int y = place(p.anchorY, p.y, screenHeight, h);
      return new HudLayout.Box(Math.max(0, Math.min(screenWidth - w, x)), Math.max(0, Math.min(screenHeight - h, y)), w, h, p.scale);
   }

   public static void move(HudLayout.Element element, int x, int y, int screenWidth, int screenHeight, int w, int h) {
      HudLayout.Position p = POSITIONS.get(element);
      x = Math.max(0, Math.min(screenWidth - w, x));
      y = Math.max(0, Math.min(screenHeight - h, y));
      p.anchorX = anchor(x, w, screenWidth);
      p.anchorY = anchor(y, h, screenHeight);
      p.x = offset(p.anchorX, x, screenWidth, w);
      p.y = offset(p.anchorY, y, screenHeight, h);
   }

   public static float scaleOf(HudLayout.Element element) {
      return POSITIONS.get(element).scale;
   }

   public static void scale(HudLayout.Element element, float amount) {
      HudLayout.Position p = POSITIONS.get(element);
      p.scale = Math.max(0.55F, Math.min(2.0F, (float)Math.round((p.scale + amount) * 20.0F) / 20.0F));
   }

   public static void resetScale(HudLayout.Element element) {
      POSITIONS.get(element).scale = 1.0F;
   }

   public static void load() {
      if (Files.exists(FILE)) {
         try {
            fromJson(JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject());
         } catch (Exception var1) {
         }
      }
   }

   public static void fromJson(JsonObject root) {
      for (HudLayout.Element e : HudLayout.Element.values()) {
         if (root.has(e.name())) {
            try {
               JsonObject o = root.getAsJsonObject(e.name());
               HudLayout.Position p = POSITIONS.get(e);
               p.x = o.get("x").getAsInt();
               p.y = o.get("y").getAsInt();
               p.scale = Math.max(0.55F, Math.min(2.0F, o.get("scale").getAsFloat()));
               p.anchorX = o.has("anchorX") ? o.get("anchorX").getAsInt() : (o.has("right") && o.get("right").getAsBoolean() ? 2 : 0);
               p.anchorY = o.has("anchorY") ? o.get("anchorY").getAsInt() : (o.has("bottom") && o.get("bottom").getAsBoolean() ? 2 : 0);
            } catch (Exception var7) {
            }
         }
      }
   }

   public static JsonObject toJson() {
      JsonObject root = new JsonObject();

      for (HudLayout.Element e : HudLayout.Element.values()) {
         HudLayout.Position p = POSITIONS.get(e);
         JsonObject o = new JsonObject();
         o.addProperty("x", p.x);
         o.addProperty("y", p.y);
         o.addProperty("scale", p.scale);
         o.addProperty("anchorX", p.anchorX);
         o.addProperty("anchorY", p.anchorY);
         root.add(e.name(), o);
      }

      return root;
   }

   public static void save() {
      try {
         JsonObject root = toJson();
         Files.createDirectories(FILE.getParent());
         Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (Exception var1) {
      }
   }

   static {
      POSITIONS.put(HudLayout.Element.WATERMARK, new HudLayout.Position(5, 5, 1.0F, false, false));
      POSITIONS.put(HudLayout.Element.FPS, new HudLayout.Position(83, 5, 1.0F, false, false));
      POSITIONS.put(HudLayout.Element.BPS, new HudLayout.Position(145, 5, 1.0F, false, false));
      POSITIONS.put(HudLayout.Element.TPS, new HudLayout.Position(5, 29, 1.0F, false, false));
      POSITIONS.put(HudLayout.Element.XYZ, new HudLayout.Position(78, 29, 1.0F, false, false));
      POSITIONS.put(HudLayout.Element.MODULE_LIST, new HudLayout.Position(5, 5, 1.0F, true, false));
      POSITIONS.put(HudLayout.Element.KEYSTROKES, new HudLayout.Position(5, 5, 1.0F, false, true));
      POSITIONS.put(HudLayout.Element.INVENTORY, new HudLayout.Position(5, 5, 1.0F, true, true));
      POSITIONS.put(HudLayout.Element.REGION, new HudLayout.Position(5, 152, 1.0F, false, false));
      POSITIONS.put(HudLayout.Element.SPOTIFY, new HudLayout.Position(5, 92, 1.0F, false, true));
      POSITIONS.put(HudLayout.Element.TOGIS, new HudLayout.Position(5, 100, 1.0F, true, true));
      POSITIONS.put(HudLayout.Element.HOTKEYS, new HudLayout.Position(5, 200, 1.0F, true, false));
   }

   public static record Box(int x, int y, int width, int height, float scale) {
      public boolean contains(double px, double py) {
         return px >= (double)this.x && py >= (double)this.y && px < (double)(this.x + this.width) && py < (double)(this.y + this.height);
      }
   }

   public static enum Element {
      WATERMARK,
      FPS,
      BPS,
      TPS,
      XYZ,
      MODULE_LIST,
      KEYSTROKES,
      INVENTORY,
      REGION,
      SPOTIFY,
      TOGIS,
      HOTKEYS;
   }

   private static final class Position {
      int x;
      int y;
      float scale;
      int anchorX;
      int anchorY;

      Position(int x, int y, float scale, boolean right, boolean bottom) {
         this.x = x;
         this.y = y;
         this.scale = scale;
         this.anchorX = right ? 2 : 0;
         this.anchorY = bottom ? 2 : 0;
      }
   }
}
