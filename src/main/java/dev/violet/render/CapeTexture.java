package dev.violet.render;

import dev.violet.SoberClient;
import dev.violet.modules.Capes;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_12079.class_10726;
import net.minecraft.class_12079.class_12081;

public final class CapeTexture {
   private static final int S = 10;
   private static final int W = 640;
   private static final int H = 320;
   private static final int FACE_W = 100;
   private static final int FACE_H = 160;
   private static final class_2960 ID = class_2960.method_60655("violet", "cape/violet");
   private static final class_12081 ASSET = new class_10726(ID, ID);
   private static final long FRAME_MS = 80L;
   private static final String PAW = "textures/gui/logo_256.png";
   private static final String NEON = "textures/brand/paw_neon.png";
   private static final String VIOLET_PAW = "textures/effects/jump_violet_paw.png";
   private static final String BADGE = "textures/effects/jump_badge.png";
   private static final String VC = "textures/brand/vc.png";
   private static final Map<String, BufferedImage> IMAGES = new HashMap<>();
   private static class_1043 texture;
   private static Capes.Style built;
   private static boolean builtMoving;
   private static long paintedAt;

   private CapeTexture() {
   }

   public static class_12081 asset(Capes.Style style, boolean effects) {
      long now = System.currentTimeMillis();
      if (texture == null) {
         texture = new class_1043(() -> "violet cape", new class_1011(640, 320, true));
         class_310.method_1551().method_1531().method_4616(ID, texture);
         built = null;
      }

      boolean moving = effects && style != Capes.Style.Violet && style != Capes.Style.VcLogo;
      if (style != built || moving != builtMoving || moving && now - paintedAt >= 80L) {
         paint(texture.method_4525(), style, moving ? (double)(now % 3600000L) / 1000.0 : 1.2);
         texture.method_4524();
         built = style;
         builtMoving = moving;
         paintedAt = now;
      }

      return ASSET;
   }

   private static void paint(class_1011 image, Capes.Style style, double time) {
      if (image != null) {
         BufferedImage face = face(style, 100, 160, time);
         int edge = edge(style);
         image.method_4326(0, 0, 640, 320, edge);
         copy(image, face, 10, 10, 1.0F);
         copy(image, face, 120, 10, 0.45F);
         BufferedImage wing = new BufferedImage(100, 200, 2);
         Graphics2D g = wing.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.drawImage(face, 0, 0, wing.getWidth(), wing.getHeight(), null);
         g.dispose();
         copy(image, wing, 240, 20, 1.0F);
         copy(image, wing, 360, 20, 0.8F);
      }
   }

   private static void copy(class_1011 image, BufferedImage src, int ox, int oy, float light) {
      int w = src.getWidth();
      int h = src.getHeight();
      int[] pixels = src.getRGB(0, 0, w, h, null, 0, w);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = pixels[y * w + x];
            int r = Math.round((float)(c >> 16 & 0xFF) * light);
            int gr = Math.round((float)(c >> 8 & 0xFF) * light);
            int b = Math.round((float)(c & 0xFF) * light);
            image.method_61941(ox + x, oy + y, 0xFF000000 | r << 16 | gr << 8 | b);
         }
      }
   }

   private static int edge(Capes.Style style) {
      return switch (style) {
         case Violet, VioletPaw, Badge, VcLogo -> -15069133;
         case VioletShine -> -10799944;
         case NeonPaw, Lightning -> -16054506;
         case NightSky -> -15857114;
         case Galaxy -> -15070672;
         case Holo -> -3621648;
      };
   }

   private static BufferedImage image(String path) {
      BufferedImage cached = IMAGES.get(path);
      if (cached != null) {
         return cached;
      } else {
         BufferedImage loaded;
         try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655("violet", path))) {
            loaded = shrink(ImageIO.read(stream), 192);
         } catch (Exception var8) {
            SoberClient.LOG.warn("Could not load the cape image {}", path, var8);
            loaded = new BufferedImage(1, 1, 2);
         }

         IMAGES.put(path, loaded);
         return loaded;
      }
   }

   private static BufferedImage shrink(BufferedImage image, int max) {
      while (Math.max(image.getWidth(), image.getHeight()) > max * 2) {
         BufferedImage half = new BufferedImage(Math.max(1, image.getWidth() / 2), Math.max(1, image.getHeight() / 2), 2);
         Graphics2D g = half.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.drawImage(image, 0, 0, half.getWidth(), half.getHeight(), null);
         g.dispose();
         image = half;
      }

      return image;
   }

   private static BufferedImage face(Capes.Style style, int w, int h, double t) {
      BufferedImage img = new BufferedImage(w, h, 2);
      Graphics2D g = img.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      Random rnd = new Random((long)(31 + style.ordinal()));
      float u = (float)w / 200.0F;
      switch (style) {
         case Violet:
            violetBackground(g, w, h);
            stars(g, rnd, w, h, 40, u, 0.0, 0.6);
            glow(g, (double)w * 0.5, (double)h * 0.42, (double)w * 0.5, new java.awt.Color(150, 110, 255, 90));
            draw(g, "textures/gui/logo_256.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.72, 1.0F);
            border(g, w, h, new java.awt.Color(196, 181, 253, 150), u);
            break;
         case VioletPaw: {
            violetBackground(g, w, h);
            double pulse = 0.5 + 0.5 * Math.sin(t * 1.8);
            glow(
               g, (double)w * 0.5, (double)h * 0.42, (double)w * (0.45 + 0.12 * pulse), new java.awt.Color(160, 120, 255, (int)Math.round(60.0 + 70.0 * pulse))
            );
            draw(g, "textures/effects/jump_violet_paw.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.8, 1.0F);
            border(g, w, h, new java.awt.Color(196, 181, 253, 140), u);
            break;
         }
         case Badge:
            violetBackground(g, w, h);
            stars(g, rnd, w, h, 30, u, t, 0.5);
            AffineTransform saved = g.getTransform();
            g.rotate(t * 0.35, (double)w * 0.5, (double)h * 0.42);
            draw(g, "textures/effects/jump_badge.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.92, 1.0F);
            g.setTransform(saved);
            border(g, w, h, new java.awt.Color(196, 181, 253, 140), u);
            break;
         case VcLogo:
            violetBackground(g, w, h);
            glow(g, (double)w * 0.5, (double)h * 0.42, (double)w * 0.55, new java.awt.Color(150, 110, 255, 80));
            draw(g, "textures/brand/vc.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.82, 1.0F);
            g.setColor(new java.awt.Color(196, 181, 253, 120));
            g.setStroke(new BasicStroke(1.4F * u));
            g.drawLine(Math.round((float)w * 0.2F), Math.round((float)h * 0.7F), Math.round((float)w * 0.8F), Math.round((float)h * 0.7F));
            border(g, w, h, new java.awt.Color(196, 181, 253, 140), u);
            break;
         case VioletShine:
            g.setPaint(new GradientPaint(0.0F, 0.0F, new java.awt.Color(150, 104, 250), (float)w, (float)h, new java.awt.Color(76, 36, 170)));
            g.fillRect(0, 0, w, h);
            draw(g, "textures/gui/logo_256.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.72, 1.0F);
            double sweep = t / 2.6 % 1.0 * 1.7 - 0.35;
            Polygon band = new Polygon(
               new int[]{
                  (int)((double)w * (sweep - 0.25)), (int)((double)w * (sweep + 0.05)), (int)((double)w * (sweep + 0.35)), (int)((double)w * (sweep + 0.05))
               },
               new int[]{0, 0, h, h},
               4
            );
            g.setPaint(
               new GradientPaint(
                  (float)((double)w * (sweep - 0.15)),
                  0.0F,
                  new java.awt.Color(255, 255, 255, 0),
                  (float)((double)w * (sweep + 0.1)),
                  0.0F,
                  new java.awt.Color(255, 255, 255, 140),
                  true
               )
            );
            g.fill(band);
            border(g, w, h, new java.awt.Color(255, 255, 255, 110), u);
            break;
         case NeonPaw:
            g.setColor(new java.awt.Color(11, 7, 22));
            g.fillRect(0, 0, w, h);
            double flicker = 0.7 + 0.3 * Math.sin(t * 3.0) + (Math.sin(t * 23.0) > 0.96 ? -0.3 : 0.0);
            glow(
               g,
               (double)w * 0.5,
               (double)h * 0.42,
               (double)w * 0.6,
               new java.awt.Color(150, 110, 255, (int)Math.round(Math.max(0.0, Math.min(255.0, 70.0 * flicker))))
            );
            draw(g, "textures/brand/paw_neon.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.95, (float)Math.max(0.25, Math.min(1.0, flicker)));
            neonFrame(g, w, h, u, flicker);
            break;
         case Lightning:
            g.setColor(new java.awt.Color(11, 7, 22));
            g.fillRect(0, 0, w, h);
            long strike = (long)Math.floor(t / 1.7);
            double phase = t / 1.7 - (double)strike;
            double flash = phase < 0.18 ? 1.0 - phase / 0.18 : 0.0;
            if (flash > 0.0) {
               g.setColor(new java.awt.Color(150, 110, 255, (int)Math.round(70.0 * flash)));
               g.fillRect(0, 0, w, h);
               bolt(g, new Random(strike * 7919L), w, h, u, flash);
            }

            glow(g, (double)w * 0.5, (double)h * 0.42, (double)w * 0.5, new java.awt.Color(150, 110, 255, (int)Math.round(60.0 + 120.0 * flash)));
            draw(g, "textures/gui/logo_256.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.7, 1.0F);
            border(g, w, h, new java.awt.Color(196, 181, 253, (int)Math.round(100.0 + 120.0 * flash)), u);
            break;
         case NightSky: {
            g.setPaint(new GradientPaint(0.0F, 0.0F, new java.awt.Color(10, 8, 30), 0.0F, (float)h, new java.awt.Color(42, 26, 92)));
            g.fillRect(0, 0, w, h);
            stars(g, rnd, w, h, 70, u, t, 1.0);
            int pulse = (int)Math.round(95.0 + 55.0 * Math.sin(t * 1.6));
            glow(g, (double)w * 0.5, (double)h * 0.42, (double)w * 0.55, new java.awt.Color(150, 110, 255, Math.max(0, Math.min(255, pulse))));
            draw(g, "textures/gui/logo_256.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.7, 1.0F);
            border(g, w, h, new java.awt.Color(167, 139, 250, 120), u);
            break;
         }
         case Galaxy:
            g.setColor(new java.awt.Color(16, 6, 34));
            g.fillRect(0, 0, w, h);
            java.awt.Color[] nebula = new java.awt.Color[]{
               new java.awt.Color(140, 50, 220, 90), new java.awt.Color(230, 70, 170, 70), new java.awt.Color(70, 90, 230, 70)
            };

            for (int i = 0; i < 26; i++) {
               double bx = rnd.nextDouble() * (double)w + Math.sin(t * 0.25 + (double)i) * (double)w * 0.08;
               double by = rnd.nextDouble() * (double)h + Math.cos(t * 0.2 + (double)i * 1.3) * (double)h * 0.05;
               glow(g, bx, by, (double)w * (0.25 + rnd.nextDouble() * 0.35), nebula[i % 3]);
            }

            stars(g, rnd, w, h, 50, u, 0.0, 0.0);
            glow(g, (double)w * 0.5, (double)h * 0.42, (double)w * 0.5, new java.awt.Color(255, 255, 255, 70));
            draw(g, "textures/gui/logo_256.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.7, 1.0F);
            border(g, w, h, new java.awt.Color(210, 160, 255, 120), u);
            break;
         case Holo:
            for (int y = 0; y < h; y++) {
               for (int x = 0; x < w; x++) {
                  double v = ((double)x * 0.6 + (double)y) / ((double)w * 0.6 + (double)h) * 2.2 + t * 0.12;
                  img.setRGB(x, y, java.awt.Color.HSBtoRGB((float)(v - Math.floor(v)), 0.42F, 1.0F));
               }
            }

            g.setPaint(new GradientPaint(0.0F, 0.0F, new java.awt.Color(255, 255, 255, 90), (float)w, (float)h, new java.awt.Color(255, 255, 255, 0)));
            g.fillRect(0, 0, w, h);
            draw(g, "textures/gui/logo_256.png", (double)w * 0.5, (double)h * 0.42, (double)w * 0.7, 1.0F);
            border(g, w, h, new java.awt.Color(255, 255, 255, 160), u);
      }

      g.dispose();
      return img;
   }

   private static void violetBackground(Graphics2D g, int w, int h) {
      g.setPaint(
         new RadialGradientPaint(
            new Double((double)w * 0.5, (double)h * 0.4),
            (float)h * 0.75F,
            new float[]{0.0F, 1.0F},
            new java.awt.Color[]{new java.awt.Color(52, 28, 108), new java.awt.Color(14, 8, 30)}
         )
      );
      g.fillRect(0, 0, w, h);
   }

   private static void stars(Graphics2D g, Random rnd, int w, int h, int count, float u, double t, double twinkleAmount) {
      for (int i = 0; i < count; i++) {
         double sx = rnd.nextDouble() * (double)w;
         double sy = rnd.nextDouble() * (double)h;
         double phase = rnd.nextDouble() * 3.141592653589793 * 2.0;
         double speed = 1.2 + rnd.nextDouble() * 2.0;
         float size = (0.6F + rnd.nextFloat() * 2.0F) * u;
         double twinkle = 1.0 - twinkleAmount * (0.5 - 0.5 * Math.sin(t * speed + phase));
         g.setColor(new java.awt.Color(235, 225, 255, (int)Math.round((double)(90 + rnd.nextInt(165)) * twinkle)));
         g.fill(new java.awt.geom.Ellipse2D.Double(sx, sy, (double)size, (double)size));
      }
   }

   private static void bolt(Graphics2D g, Random rnd, int w, int h, float u, double flash) {
      Path2D path = new java.awt.geom.Path2D.Double();
      double x = (double)w * (0.2 + rnd.nextDouble() * 0.6);
      double y = 0.0;
      path.moveTo(x, y);

      while (y < (double)h) {
         y += (double)h * (0.06 + rnd.nextDouble() * 0.08);
         x += (double)w * (rnd.nextDouble() - 0.5) * 0.35;
         path.lineTo(x, y);
      }

      for (int pass = 0; pass < 3; pass++) {
         float width = (pass == 0 ? 9.0F : (pass == 1 ? 4.0F : 1.5F)) * u;
         int alpha = (int)Math.round((double)(pass == 0 ? 50 : (pass == 1 ? 120 : 255)) * flash);
         g.setColor(pass == 2 ? new java.awt.Color(245, 240, 255, alpha) : new java.awt.Color(167, 139, 250, alpha));
         g.setStroke(new BasicStroke(width, 1, 1));
         g.draw(path);
      }
   }

   private static void neonFrame(Graphics2D g, int w, int h, float u, double flicker) {
      g.setColor(new java.awt.Color(150, 110, 255, (int)Math.round(Math.max(0.0, Math.min(255.0, 60.0 * flicker)))));
      g.setStroke(new BasicStroke(6.0F * u));
      g.drawRect(Math.round(8.0F * u), Math.round(8.0F * u), w - 1 - Math.round(16.0F * u), h - 1 - Math.round(16.0F * u));
      g.setColor(new java.awt.Color(220, 205, 255, (int)Math.round(Math.max(0.0, Math.min(255.0, 220.0 * flicker)))));
      g.setStroke(new BasicStroke(1.6F * u));
      g.drawRect(Math.round(8.0F * u), Math.round(8.0F * u), w - 1 - Math.round(16.0F * u), h - 1 - Math.round(16.0F * u));
   }

   private static void border(Graphics2D g, int w, int h, java.awt.Color color, float u) {
      g.setColor(color);
      g.setStroke(new BasicStroke(2.0F * u));
      g.drawRect(Math.round(u), Math.round(u), w - 1 - Math.round(2.0F * u), h - 1 - Math.round(2.0F * u));
   }

   private static void glow(Graphics2D g, double cx, double cy, double r, java.awt.Color c) {
      g.setPaint(
         new RadialGradientPaint(
            new Double(cx, cy), (float)r, new float[]{0.0F, 1.0F}, new java.awt.Color[]{c, new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2.0, r * 2.0));
   }

   private static void draw(Graphics2D g, String path, double cx, double cy, double size, float opacity) {
      BufferedImage image = image(path);
      double scale = size / (double)Math.max(image.getWidth(), image.getHeight());
      double dw = (double)image.getWidth() * scale;
      double dh = (double)image.getHeight() * scale;
      g.setComposite(AlphaComposite.getInstance(3, Math.max(0.0F, Math.min(1.0F, opacity))));
      g.drawImage(image, (int)Math.round(cx - dw / 2.0), (int)Math.round(cy - dh / 2.0), (int)Math.round(dw), (int)Math.round(dh), null);
      g.setComposite(AlphaComposite.SrcOver);
   }
}
