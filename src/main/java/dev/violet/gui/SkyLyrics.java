package dev.violet.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import dev.violet.SoberClient;
import dev.violet.mixin.GameRendererAccessor;
import dev.violet.modules.SpotifyPlayer;
import dev.violet.util.spotify.Lyrics;
import dev.violet.util.spotify.NowPlaying;
import dev.violet.util.spotify.PlaybackClock;
import dev.violet.util.spotify.SyncedLyrics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

public final class SkyLyrics {
   private static final int EM = 80;
   private static final int PAD = 30;
   private static final int TEX_W = 1600;
   private static final int TEX_H = 400;
   private static final int MAX_ROW = 1480;
   private static final float ROW_H = 92.799995F;
   private static final double DISTANCE = 100.0;
   private static final double TILT = Math.toRadians(10.0);
   private static final long FADE_IN = 220L;
   private static final long FADE_OUT = 380L;
   private static final SkyLyrics.Slot[] SLOTS = new SkyLyrics.Slot[]{new SkyLyrics.Slot(0), new SkyLyrics.Slot(1)};
   private static Font font;
   private static SkyLyrics.Slot current;
   private static SkyLyrics.Slot previous;
   private static String shown = "";
   private static final PlaybackClock CLOCK = new PlaybackClock();
   private static float anchorYaw = 0.0F / 0.0F;
   private static long lastFrame;
   private static long statusSince;
   private static String statusSong = "";
   private static Lyrics.Status statusShown;

   private SkyLyrics() {
   }

   private static Font font() {
      if (font == null) {
         try (InputStream stream = class_310.method_1551().method_1478().open(class_2960.method_60655("violet", "font/ubuntu-bold.ttf"))) {
            font = Font.createFont(0, stream).deriveFont(80.0F);
         } catch (Exception var5) {
            SoberClient.LOG.warn("Could not load the sky lyrics font", var5);
            font = new Font("SansSerif", 1, 80);
         }
      }

      return font;
   }

   public static void render(class_332 context, float tickProgress) {
      class_310 mc = class_310.method_1551();
      SpotifyPlayer module = SpotifyPlayer.active();
      long now = System.currentTimeMillis();
      float dt = lastFrame == 0L ? 0.0F : Math.min(0.1F, (float)(now - lastFrame) / 1000.0F);
      lastFrame = now;
      if (module != null && module.skyLyrics() && mc.field_1687 != null && mc.field_1724 != null) {
         NowPlaying track = module.nowPlaying();
         String text = "";
         SyncedLyrics.Line line = null;
         long lineEnd = 0L;
         long position = 0L;
         String key = "";
         float size = 1.0F;
         if (track != null && track.active() && track.hasTimeline() && module.wantsLyrics(track)) {
            CLOCK.update(track, now);
            Lyrics lyrics = module.lyricsFor(track);
            Lyrics.Status status = lyrics == null ? Lyrics.Status.LOADING : lyrics.status();
            String song = track.title() + "|" + track.artist();
            if (!song.equals(statusSong) || status != statusShown) {
               statusSong = song;
               statusShown = status;
               statusSince = now;
            }

            if (status == Lyrics.Status.SYNCED) {
               SyncedLyrics synced = lyrics.synced();
               position = CLOCK.positionAt(now);
               int index = synced.indexAt(position);
               if (index >= 0) {
                  line = synced.line(index);
                  text = line.text();
                  lineEnd = index + 1 < synced.size() ? synced.line(index + 1).timeMs() : line.timeMs() + 5000L;
                  key = line.timeMs() + "|" + text;
               }
            } else {
               String message = message(status, now - statusSince);
               if (message != null) {
                  text = message;
                  line = new SyncedLyrics.Line(0L, message, List.of());
                  lineEnd = 1L;
                  position = 60000L;
                  size = 0.42F;
                  key = "message|" + message + "|" + song;
               }
            }
         }

         if (!key.equals(shown)) {
            shown = key;
            if (current != null) {
               current.goneAt = now;
               previous = current;
            }

            current = null;
            if (!text.isEmpty()) {
               SkyLyrics.Slot slot = previous == SLOTS[0] ? SLOTS[1] : SLOTS[0];
               build(slot, text, line, lineEnd, now - (position - line.timeMs()));
               slot.size = size;
               current = slot;
            }
         }

         if (previous != null && now - previous.goneAt > 380L) {
            previous = null;
         }

         if (current != null || previous != null) {
            class_4184 camera = mc.field_1773.method_19418();
            float yaw = camera.method_19330();
            float pitch = camera.method_19329();
            if (Float.isNaN(anchorYaw)) {
               anchorYaw = yaw;
            }

            anchorYaw = anchorYaw + class_3532.method_15393(yaw - anchorYaw) * (1.0F - (float)Math.exp((double)(-dt * 0.9F)));
            float fov = ((GameRendererAccessor)mc.field_1773).violet$getFov(camera, tickProgress, true);
            int sw = context.method_51421();
            int sh = context.method_51443();
            double focal = (double)sh / 2.0 / Math.tan(Math.toRadians((double)fov) / 2.0);
            double[] forward = direction(yaw, pitch);
            double[] right = normalize(cross(forward, new double[]{0.0, 1.0, 0.0}));
            double[] up = cross(right, forward);
            double elevation = (double)module.skyHeight();
            double[] center = scale(direction(anchorYaw, (float)(-elevation)), 100.0);
            double[] axisX = normalize(cross(direction(anchorYaw, 0.0F), new double[]{0.0, 1.0, 0.0}));
            double[] p0 = project(center, forward, right, up, focal, sw, sh);
            double[] px = project(add(center, axisX), forward, right, up, focal, sw, sh);
            if (p0 != null && px != null) {
               double unit = focal * Math.tan(Math.toRadians(6.0 * (double)module.skySize())) / 80.0;
               double angle = class_3532.method_15350(Math.atan2(px[1] - p0[1], px[0] - p0[0]), -TILT, TILT);
               double ax = Math.cos(angle) * unit;
               double ay = Math.sin(angle) * unit;
               double bx = Math.sin(angle) * unit;
               double by = -Math.cos(angle) * unit;
               if (previous != null) {
                  float fade = 1.0F - (float)(now - previous.goneAt) / 380.0F;
                  draw(context, previous, p0, ax, ay, bx, by, now, class_3532.method_15363(fade, 0.0F, 1.0F), (1.0F - fade) * 80.0F * 0.5F);
               }

               if (current != null) {
                  draw(context, current, p0, ax, ay, bx, by, now, 1.0F, 0.0F);
               }
            }
         }
      } else {
         shown = "";
         previous = null;
         current = null;
         anchorYaw = 0.0F / 0.0F;
      }
   }

   private static String message(Lyrics.Status status, long since) {
      return switch (status) {
         case LOADING -> since > 1500L ? "Loading lyrics…" : null;
         case PLAIN, NOT_FOUND -> since < 6000L ? "No synced lyrics for this song" : null;
         case INSTRUMENTAL -> since < 6000L ? "Instrumental" : null;
         case UNAVAILABLE -> since < 6000L ? "Lyrics are offline right now" : null;
         default -> null;
      };
   }

   private static void draw(class_332 context, SkyLyrics.Slot slot, double[] p0, double ax, double ay, double bx, double by, long now, float alpha, float lift) {
      ax *= (double)slot.size;
      ay *= (double)slot.size;
      bx *= (double)slot.size;
      by *= (double)slot.size;
      alpha *= slot.size < 1.0F ? 0.7F : 1.0F;
      double cx = (double)slot.w / 2.0;
      double cy = (double)slot.h / 2.0 + (double)lift;
      Matrix3x2f transform = new Matrix3x2f(
         (float)ax, (float)ay, (float)(-bx), (float)(-by), (float)(p0[0] - ax * cx + bx * cy), (float)(p0[1] - ay * cx + by * cy)
      );
      Matrix3x2fStack matrices = context.method_51448();

      for (SkyLyrics.Word word : slot.words) {
         float reveal = class_3532.method_15363((float)(now - word.at()) / 220.0F, 0.0F, 1.0F);
         float a = reveal * alpha;
         if (!(a <= 0.004F)) {
            float rise = (1.0F - reveal) * (1.0F - reveal) * 80.0F * 0.22F;
            int top = slot.bands[word.row()];
            int bottom = slot.bands[word.row() + 1];
            matrices.pushMatrix();
            matrices.mul(transform);
            matrices.translate(0.0F, rise);
            context.method_25293(
               class_10799.field_56883,
               slot.id,
               word.x0(),
               top,
               (float)word.x0(),
               (float)top,
               word.x1() - word.x0(),
               bottom - top,
               word.x1() - word.x0(),
               bottom - top,
               1600,
               400,
               Math.round(a * 255.0F) << 24 | 16777215
            );
            matrices.popMatrix();
         }
      }
   }

   private static void build(SkyLyrics.Slot slot, String text, SyncedLyrics.Line line, long lineEnd, long lineStartAt) {
      slot.ensure();
      slot.goneAt = -1L;
      slot.words.clear();
      Font f = font();
      FontRenderContext frc = new FontRenderContext(null, true, true);
      String[] words = text.split(" ");
      float space = (float)f.getStringBounds(" ", frc).getWidth();
      float[] widths = new float[words.length];

      for (int i = 0; i < words.length; i++) {
         widths[i] = (float)f.getStringBounds(words[i], frc).getWidth();
      }

      List<int[]> rows = new ArrayList<>();
      List<Float> rowWidths = new ArrayList<>();
      int start = 0;
      float width = 0.0F;

      for (int i = 0; i < words.length; i++) {
         float next = width == 0.0F ? widths[i] : width + space + widths[i];
         if (width > 0.0F && next > 1480.0F && rows.size() < 2) {
            rows.add(new int[]{start, i});
            rowWidths.add(width);
            start = i;
            width = widths[i];
         } else {
            width = next;
         }
      }

      rows.add(new int[]{start, words.length});
      rowWidths.add(width);
      float widest = 0.0F;

      for (float w : rowWidths) {
         widest = Math.max(widest, w);
      }

      float shrink = widest > 1480.0F ? 1480.0F / widest : 1.0F;
      Font drawFont = shrink < 1.0F ? f.deriveFont(80.0F * shrink) : f;
      slot.w = Math.min(1600, Math.round(Math.min(widest, 1480.0F)) + 60 + 8);
      slot.h = Math.min(400, Math.round((float)rows.size() * 92.799995F) + 60);
      BufferedImage glyphs = new BufferedImage(slot.w, slot.h, 2);
      Graphics2D g = glyphs.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setFont(drawFont);
      g.setColor(Color.WHITE);
      int total = Math.max(1, text.replace(" ", "").length());
      long span = Math.max(600L, Math.min(lineEnd - line.timeMs(), Math.max(900L, (long)total * 90L)) * 9L / 10L);
      List<Long> times = line.wordTimes();
      int before = 0;
      slot.bands = new int[rows.size() + 1];

      for (int r = 0; r < rows.size(); r++) {
         int[] row = rows.get(r);
         float rowW = rowWidths.get(r) * shrink;
         float x = ((float)slot.w - rowW) / 2.0F;
         float baseline = 30.0F + (float)r * 92.799995F + 68.8F;
         slot.bands[r] = r == 0 ? 0 : Math.round(30.0F + (float)r * 92.799995F);
         float[] lefts = new float[row[1] - row[0]];
         float[] rights = new float[row[1] - row[0]];

         for (int ix = row[0]; ix < row[1]; ix++) {
            g.drawString(words[ix], x, baseline);
            lefts[ix - row[0]] = x;
            rights[ix - row[0]] = x + widths[ix] * shrink;
            x += (widths[ix] + space) * shrink;
         }

         for (int ix = row[0]; ix < row[1]; ix++) {
            int j = ix - row[0];
            int x0 = j == 0 ? 0 : Math.round((rights[j - 1] + lefts[j]) / 2.0F);
            int x1 = j == lefts.length - 1 ? slot.w : Math.round((rights[j] + lefts[j + 1]) / 2.0F);
            long at = times.size() == words.length ? lineStartAt + (times.get(ix) - line.timeMs()) : lineStartAt + span * (long)before / (long)total;
            slot.words.add(new SkyLyrics.Word(r, x0, x1, at));
            before += words[ix].length();
         }
      }

      slot.bands[rows.size()] = slot.h;
      g.dispose();
      int w = slot.w;
      int h = slot.h;
      float[] alpha = new float[w * h];
      int[] pixels = glyphs.getRGB(0, 0, w, h, null, 0, w);

      for (int ix = 0; ix < pixels.length; ix++) {
         alpha[ix] = (float)(pixels[ix] >>> 24) / 255.0F;
      }

      float[] glow = blur(blur(alpha, w, h, 9), w, h, 9);
      class_1011 image = slot.image;
      image.method_4326(0, 0, 1600, 400, 0);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int ix = y * w + x;
            float ink = alpha[ix];
            float halo = Math.min(1.0F, glow[ix] * 0.5F);
            float out = ink + halo * (1.0F - ink);
            if (!(out <= 0.0F)) {
               float tint = ink / out;
               int r = Math.round(255.0F * tint + 228.0F * (1.0F - tint));
               int gg = Math.round(255.0F * tint + 214.0F * (1.0F - tint));
               image.method_61941(x, y, Math.round(out * 255.0F) << 24 | r << 16 | gg << 8 | 0xFF);
            }
         }
      }

      slot.texture.method_4524();
   }

   private static float[] blur(float[] src, int w, int h, int radius) {
      float[] tmp = new float[w * h];
      float[] out = new float[w * h];
      float norm = 1.0F / (float)(2 * radius + 1);

      for (int y = 0; y < h; y++) {
         float sum = 0.0F;

         for (int x = -radius; x <= radius; x++) {
            sum += src[y * w + class_3532.method_15340(x, 0, w - 1)];
         }

         for (int x = 0; x < w; x++) {
            tmp[y * w + x] = sum * norm;
            sum += src[y * w + Math.min(w - 1, x + radius + 1)] - src[y * w + Math.max(0, x - radius)];
         }
      }

      for (int x = 0; x < w; x++) {
         float sum = 0.0F;

         for (int y = -radius; y <= radius; y++) {
            sum += tmp[class_3532.method_15340(y, 0, h - 1) * w + x];
         }

         for (int y = 0; y < h; y++) {
            out[y * w + x] = sum * norm;
            sum += tmp[Math.min(h - 1, y + radius + 1) * w + x] - tmp[Math.max(0, y - radius) * w + x];
         }
      }

      return out;
   }

   private static double[] project(double[] p, double[] forward, double[] right, double[] up, double focal, int sw, int sh) {
      double z = dot(p, forward);
      return z < 1.0 ? null : new double[]{(double)sw / 2.0 + dot(p, right) / z * focal, (double)sh / 2.0 - dot(p, up) / z * focal};
   }

   private static double[] direction(float yaw, float pitch) {
      double y = Math.toRadians((double)yaw);
      double p = Math.toRadians((double)pitch);
      return new double[]{-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p)};
   }

   private static double dot(double[] a, double[] b) {
      return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
   }

   private static double[] cross(double[] a, double[] b) {
      return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
   }

   private static double[] normalize(double[] a) {
      double l = Math.sqrt(dot(a, a));
      return new double[]{a[0] / l, a[1] / l, a[2] / l};
   }

   private static double[] scale(double[] a, double s) {
      return new double[]{a[0] * s, a[1] * s, a[2] * s};
   }

   private static double[] add(double[] a, double[] b) {
      return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
   }

   private static final class Slot {
      final class_2960 id;
      class_1011 image;
      SkyLyrics.Texture texture;
      int w;
      int h;
      float size = 1.0F;
      final List<SkyLyrics.Word> words = new ArrayList<>();
      int[] bands = new int[0];
      long goneAt = -1L;

      Slot(int index) {
         this.id = class_2960.method_60655("violet", "sky_lyrics_" + index);
      }

      void ensure() {
         if (this.texture == null) {
            this.image = new class_1011(1600, 400, true);
            this.texture = new SkyLyrics.Texture(this.id, this.image);
            class_310.method_1551().method_1531().method_4616(this.id, this.texture);
         }
      }
   }

   private static final class Texture extends class_1043 {
      Texture(class_2960 id, class_1011 image) {
         super(id::toString, image);
      }

      public class_12137 method_75484() {
         return RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
      }
   }

   private static record Word(int row, int x0, int x1, long at) {
   }
}
