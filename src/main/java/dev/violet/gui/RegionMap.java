package dev.violet.gui;

import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import net.minecraft.class_11719.class_11721;
import org.joml.Matrix3x2fStack;

public final class RegionMap {
   private static final int REGION_BLOCKS = 50000;
   private static final int ORIGIN = -225000;
   private static final int SIZE = 9;
   private static final float UNIT = 0.5F;
   private static final float PAD = 10.0F;
   private static final float INFO_GAP = 8.0F;
   private static final float INFO_LINE_H = 22.0F;
   private static final float LEGEND_GAP = 4.0F;
   private static final float LEGEND_ROW_H = 16.0F;
   private static final float LEGEND_SWATCH = 9.0F;
   private static final float LABEL_GAP = 8.0F;
   private static final float TEXT_PX = 17.0F;
   private static final float LABEL_PX = 14.0F;
   private static final float FONT_PX = 9.0F;
   private static final float CARD_ROUNDING = 8.0F;
   private static final String MINUS = "−";
   private static final String DOT = "  ·  ";
   private static final String COORDINATE_LABEL = "Coordinate";
   private static final String REGION_LABEL = "Region";
   private static final int TEXT_VALUE = 16053493;
   private static final int TEXT_NAME = 16053493;
   private static final int TEXT_LABEL = 11447990;
   private static final int BORDER = -13750733;
   private static final int GUARD = -1946157056;
   private static final int ARROW_OUTLINE = -1728053248;
   private static final int[] SHADOW = new int[]{771751936, 385875968, 201326592};
   private static final class_2583 TEXT_FONT = class_2583.field_24360.method_27704(new class_11721(class_2960.method_60655("violet", "geist_text")));
   private static final class_2583 LABEL_FONT = class_2583.field_24360.method_27704(new class_11721(class_2960.method_60655("violet", "geist_label")));
   private static final int[][] LAYOUT = new int[][]{
      {82, 5},
      {100, 3},
      {101, 3},
      {102, 3},
      {103, 2},
      {104, 2},
      {105, 2},
      {106, 2},
      {91, 2},
      {83, 5},
      {44, 3},
      {75, 3},
      {42, 3},
      {41, 2},
      {40, 2},
      {39, 2},
      {38, 2},
      {92, 2},
      {84, 5},
      {45, 3},
      {14, 3},
      {13, 3},
      {12, 2},
      {11, 2},
      {10, 2},
      {37, 2},
      {93, 2},
      {85, 5},
      {46, 5},
      {74, 5},
      {3, 3},
      {2, 2},
      {1, 2},
      {25, 2},
      {36, 2},
      {94, 2},
      {86, 4},
      {47, 4},
      {72, 4},
      {71, 4},
      {5, 2},
      {4, 2},
      {24, 2},
      {35, 2},
      {95, 2},
      {87, 4},
      {51, 1},
      {17, 1},
      {9, 0},
      {8, 0},
      {7, 0},
      {23, 0},
      {34, 0},
      {96, 2},
      {88, 4},
      {54, 1},
      {18, 1},
      {61, 0},
      {62, 0},
      {21, 0},
      {22, 0},
      {33, 0},
      {97, 0},
      {89, 0},
      {26, 1},
      {27, 0},
      {28, 0},
      {29, 0},
      {30, 0},
      {59, 0},
      {32, 0},
      {98, 0},
      {90, 0},
      {107, 1},
      {108, 1},
      {109, 1},
      {110, 1},
      {111, 1},
      {112, 1},
      {113, 1},
      {99, 0}
   };
   private static final RegionMap.Server[] TYPES = new RegionMap.Server[]{
      RegionMap.Server.EUC, RegionMap.Server.EUW, RegionMap.Server.NAE, RegionMap.Server.NAW, RegionMap.Server.ASIA, RegionMap.Server.OCE
   };

   private RegionMap() {
   }

   private static RegionMap.Server server(int row, int col) {
      return TYPES[LAYOUT[row * 9 + col][1]];
   }

   private static int regionId(int row, int col) {
      return LAYOUT[row * 9 + col][0];
   }

   private static int cell(double coordinate) {
      long index = Math.floorDiv((long)Math.floor(coordinate) - -225000L, 50000);
      return (int)Math.max(0L, Math.min(8L, index));
   }

   public static RegionMap.Region at(double x, double z) {
      int col = cell(x);
      int row = cell(z);
      double minX = -225000.0 + (double)col * 50000.0;
      double minZ = -225000.0 + (double)row * 50000.0;
      double cx = Math.max(minX, Math.min(minX + 50000.0, x));
      double cz = Math.max(minZ, Math.min(minZ + 50000.0, z));
      double edge = Math.min(Math.min(cx - minX, minX + 50000.0 - cx), Math.min(cz - minZ, minZ + 50000.0 - cz));
      return new RegionMap.Region(
         row, col, String.valueOf(regionId(row, col)), server(row, col), edge, (float)((cx - minX) / 50000.0), (float)((cz - minZ) / 50000.0)
      );
   }

   public static RegionMap.Region current(class_310 mc) {
      return mc.field_1724 != null && mc.field_1687 != null && mc.field_1687.method_27983() == class_1937.field_25179
         ? at(mc.field_1724.method_23317(), mc.field_1724.method_23321())
         : null;
   }

   private static int[] worldToGrid(double x, double z) {
      return new int[]{(int)((x + 225000.0) / 50000.0), (int)((z + 225000.0) / 50000.0)};
   }

   private static double[] worldToCellPosition(double x, double z) {
      return new double[]{positiveModulo(x + 225000.0, 50000.0) / 50000.0, positiveModulo(z + 225000.0, 50000.0) / 50000.0};
   }

   private static double positiveModulo(double value, double divisor) {
      double mod = value % divisor;
      return mod < 0.0 ? mod + divisor : mod;
   }

   private static boolean inside(int[] grid) {
      return grid[0] >= 0 && grid[0] < 9 && grid[1] >= 0 && grid[1] < 9;
   }

   public static int width(dev.violet.modules.RegionMap map) {
      float grid = gridSize(map);
      float content = Math.max(grid, infoWidth(map));
      return (int)Math.ceil((double)((content + 20.0F) * 0.5F));
   }

   public static int height(dev.violet.modules.RegionMap map) {
      return (int)Math.ceil((double)((gridSize(map) + 20.0F + 8.0F + infoHeight(map)) * 0.5F));
   }

   private static float gridSize(dev.violet.modules.RegionMap map) {
      return 9.0F * (map.cellSize() + map.cellGap()) - map.cellGap();
   }

   private static float infoWidth(dev.violet.modules.RegionMap map) {
      float labelCol = labelColumn(map) + 8.0F;
      float values = map.showCoordinates() ? width("−000000, −000000", 17.0F, TEXT_FONT) : 0.0F;
      float legend = 0.0F;

      for (RegionMap.Server type : TYPES) {
         values = Math.max(values, width("000  ·  " + type.name, 17.0F, TEXT_FONT));
         legend = Math.max(legend, 15.0F + width(type.name, 14.0F, LABEL_FONT));
      }

      values = Math.max(values, width("000  ·  Unknown", 17.0F, TEXT_FONT));
      return (float)Math.ceil((double)Math.max(labelCol + values, map.showLegend() ? legend : 0.0F));
   }

   private static float infoHeight(dev.violet.modules.RegionMap map) {
      float height = 22.0F * (float)infoLines(map);
      if (map.showLegend()) {
         height += 4.0F + (float)TYPES.length * 16.0F;
      }

      return height;
   }

   private static int infoLines(dev.violet.modules.RegionMap map) {
      return map.showCoordinates() ? 2 : 1;
   }

   private static float labelColumn(dev.violet.modules.RegionMap map) {
      float region = width("Region", 14.0F, LABEL_FONT);
      return map.showCoordinates() ? Math.max(width("Coordinate", 14.0F, LABEL_FONT), region) : region;
   }

   private static float width(String value, float px, class_2583 font) {
      return (float)class_310.method_1551().field_1772.method_27525(class_2561.method_43470(value).method_10862(font)) * px / 9.0F;
   }

   private static void text(class_332 context, String value, float x, float y, float px, class_2583 font, int color, boolean guard) {
      if (!value.isEmpty() && color >>> 24 != 0) {
         float scale = px / 9.0F;
         class_2561 line = class_2561.method_43470(value).method_10862(font);
         Matrix3x2fStack matrices = context.method_51448();
         if (guard) {
            matrices.pushMatrix();
            matrices.translate(x + 1.0F, y + 1.0F);
            matrices.scale(scale, scale);
            context.method_51439(class_310.method_1551().field_1772, line, 0, 0, scaleAlpha(-1946157056, (float)(color >>> 24) / 255.0F), false);
            matrices.popMatrix();
         }

         matrices.pushMatrix();
         matrices.translate(x, y);
         matrices.scale(scale, scale);
         context.method_51439(class_310.method_1551().field_1772, line, 0, 0, color, false);
         matrices.popMatrix();
      }
   }

   private static int alpha(int rgb, float alpha) {
      return Math.round(255.0F * Math.max(0.0F, Math.min(1.0F, alpha))) << 24 | rgb & 16777215;
   }

   private static int scaleAlpha(int argb, float alpha) {
      return Math.round((float)(argb >>> 24) * Math.max(0.0F, Math.min(1.0F, alpha))) << 24 | argb & 16777215;
   }

   private static void card(class_332 context, float w, float h, float alpha, boolean shadow) {
      int r = Math.round(8.0F);
      if (Theme.liquidGlass()) {
         Smooth.liquid(context, 0, 0, Math.round(w), Math.round(h), r + 2, alpha, false);
      } else {
         if (shadow) {
            Smooth.rect(context, -1, 0, Math.round(w) + 2, Math.round(h) + 2, r + 1, scaleAlpha(SHADOW[0], alpha));
            Smooth.rect(context, -2, 1, Math.round(w) + 4, Math.round(h) + 3, r + 2, scaleAlpha(SHADOW[1], alpha));
            Smooth.rect(context, -3, 2, Math.round(w) + 6, Math.round(h) + 4, r + 3, scaleAlpha(SHADOW[2], alpha));
         }

         Smooth.rect(context, 0, 0, Math.round(w), Math.round(h), r, scaleAlpha(HudPalette.card(), alpha));
         Smooth.outline(context, 0, 0, Math.round(w), Math.round(h), r, scaleAlpha(-13750733, alpha));
      }
   }

   public static void draw(class_332 context, class_310 mc, dev.violet.modules.RegionMap map) {
      class_746 player = mc.field_1724;
      float alpha = map.opacity();
      int w = width(map);
      int h = height(map);
      float layoutWidth = (float)w / 0.5F;
      float layoutHeight = (float)h / 0.5F;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.scale(0.5F, 0.5F);
      card(context, layoutWidth, layoutHeight, alpha, map.shadow());
      if (player == null) {
         matrices.popMatrix();
      } else {
         float cellSize = map.cellSize();
         float cellGap = map.cellGap();
         float gridSize = gridSize(map);
         float gridX = (float)Math.round((layoutWidth - gridSize) * 0.5F);
         float gridY = 10.0F;
         int cellR = Math.round(Math.min(Math.max(2.0F, -2.0F), cellSize * 0.3F));

         for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
               float cx = gridX + (float)col * (cellSize + cellGap);
               float cy = gridY + (float)row * (cellSize + cellGap);
               Smooth.rect(context, Math.round(cx), Math.round(cy), Math.round(cellSize), Math.round(cellSize), cellR, alpha(server(row, col).color, alpha));
               if (map.showRegionIds()) {
                  drawCenteredCellText(context, regionId(row, col), cx, cy, cellSize, map.textScale(), alpha);
               }
            }
         }

         renderPlayerPin(context, map, player, gridX, gridY, cellSize, cellGap, alpha);
         renderInfoPanel(context, map, player, 10.0F, gridY + gridSize + 8.0F, alpha);
         matrices.popMatrix();
      }
   }

   private static void drawCenteredCellText(class_332 context, int regionId, float cx, float cy, float size, float textScale, float alpha) {
      float fontPx = Math.max(8.0F, (float)Math.round(14.0F * textScale * 0.85F));
      String label = String.valueOf(regionId);
      float tx = cx + (size - width(label, fontPx, LABEL_FONT)) * 0.5F;
      float ty = cy + (size - fontPx * 7.0F / 9.0F) * 0.5F;
      text(context, label, tx, ty, fontPx, LABEL_FONT, alpha(16053493, alpha), true);
   }

   private static void renderPlayerPin(
      class_332 context, dev.violet.modules.RegionMap map, class_746 player, float gridX, float gridY, float cellSize, float cellGap, float alpha
   ) {
      double px = player.method_23317();
      double pz = player.method_23321();
      int[] grid = worldToGrid(px, pz);
      if (inside(grid)) {
         double[] cellPos = worldToCellPosition(px, pz);
         float centerX = gridX + (float)grid[0] * (cellSize + cellGap) + (float)(cellPos[0] * (double)cellSize);
         float centerY = gridY + (float)grid[1] * (cellSize + cellGap) + (float)(cellPos[1] * (double)cellSize);
         double angle = Math.toRadians((double)(-player.method_36454()) - 90.0);
         float arrowSize = 9.0F * map.playerScale();
         float dx = (float)Math.cos(angle);
         float dy = (float)(-Math.sin(angle));
         int color = map.playerIndicatorColor().argb();
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(centerX, centerY);
         matrices.rotate((float)Math.atan2((double)dx, (double)(-dy)));
         float reach = arrowSize * 2.8F;
         float spread = reach * 0.62F;
         int beam = scaleAlpha(color & 16777215 | 1711276032, alpha);
         int clear = color & 16777215;
         Smooth.polygon(context, new float[]{0.0F, 0.0F, -spread, -reach, 0.0F, -reach * 1.08F, spread, -reach}, new int[]{beam, clear, clear, clear});
         float l = arrowSize * 1.05F;
         float[] dart = new float[]{0.0F, -l, l * 0.74F, l * 0.78F, 0.0F, l * 0.34F, -l * 0.74F, l * 0.78F};
         float[] xs = new float[]{dart[0], dart[2], dart[4], dart[6], dart[0]};
         float[] ys = new float[]{dart[1], dart[3], dart[5], dart[7], dart[1]};
         int edge = scaleAlpha(-1728053248, alpha);
         Smooth.polyline(context, xs, ys, new float[]{2.4F, 2.4F, 2.4F, 2.4F, 2.4F}, new int[]{edge, edge, edge, edge, edge}, 5);
         Smooth.polygon(context, dart, scaleAlpha(color, alpha));
         int rim = scaleAlpha(-855638017, alpha);
         Smooth.polyline(context, xs, ys, new float[]{0.8F, 0.8F, 0.8F, 0.8F, 0.8F}, new int[]{rim, rim, rim, rim, rim}, 5);
         matrices.popMatrix();
      }
   }

   private static void renderInfoPanel(class_332 context, dev.violet.modules.RegionMap map, class_746 player, float x, float y, float alpha) {
      float labelOffset = (float)Math.round(2.4F);
      float valueX = x + labelColumn(map) + 8.0F;
      float lineH = 22.0F;
      float textInset = (float)Math.floor((double)((lineH - 17.0F) * 0.5F));
      float lineTop = y + textInset;
      if (map.showCoordinates()) {
         text(context, "Coordinate", x, lineTop + labelOffset, 14.0F, LABEL_FONT, alpha(11447990, alpha), false);
         text(
            context,
            number((long)Math.floor(player.method_23317())) + ", " + number((long)Math.floor(player.method_23321())),
            valueX,
            lineTop,
            17.0F,
            TEXT_FONT,
            alpha(16053493, alpha),
            false
         );
         lineTop += lineH;
      }

      int[] grid = worldToGrid(player.method_23317(), player.method_23321());
      int regionId = inside(grid) ? regionId(grid[1], grid[0]) : -1;
      String regionName = inside(grid) ? server(grid[1], grid[0]).name : "Unknown";
      text(context, "Region", x, lineTop + labelOffset, 14.0F, LABEL_FONT, alpha(11447990, alpha), false);
      text(context, regionId + "  ·  " + regionName, valueX, lineTop, 17.0F, TEXT_FONT, alpha(16053493, alpha), false);
      if (map.showLegend()) {
         float legendY = y + lineH * (float)infoLines(map) + 4.0F;

         for (int i = 0; i < TYPES.length; i++) {
            float rowY = legendY + (float)i * 16.0F;
            float swatchY = rowY + 3.5F;
            Smooth.rect(context, Math.round(x), Math.round(swatchY), Math.round(9.0F), Math.round(9.0F), 2, alpha(TYPES[i].color, alpha));
            text(context, TYPES[i].name, x + 9.0F + 6.0F, rowY + (float)Math.floor(2.5555553436279297), 14.0F, LABEL_FONT, alpha(16053493, alpha), false);
         }
      }
   }

   private static String number(long value) {
      return value < 0L ? "−" + -value : Long.toString(value);
   }

   public static record Region(int row, int col, String number, RegionMap.Server server, double edge, float fx, float fz) {
   }

   public static enum Server {
      EUC("EU-C", "EU Central", 10473059),
      EUW("EU-W", "EU West", 42595),
      NAE("NA-E", "NA East", 5221866),
      NAW("NA-W", "NA West", 3108538),
      ASIA("ASIA", "Asia", 16106050),
      OCE("OCE", "Oceania", 16549891);

      public final String title;
      public final String name;
      public final int color;

      private Server(String title, String name, int color) {
         this.title = title;
         this.name = name;
         this.color = color;
      }
   }
}
