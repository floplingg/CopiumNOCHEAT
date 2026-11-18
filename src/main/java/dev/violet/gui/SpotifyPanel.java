package dev.violet.gui;

import dev.violet.modules.SpotifyPlayer;
import dev.violet.util.spotify.AlbumArt;
import dev.violet.util.spotify.DisplayText;
import dev.violet.util.spotify.Lyrics;
import dev.violet.util.spotify.NowPlaying;
import dev.violet.util.spotify.PlaybackClock;
import dev.violet.util.spotify.SpotifyWindow;
import dev.violet.util.spotify.SyncedLyrics;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import net.minecraft.class_1041;
import net.minecraft.class_10799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

public final class SpotifyPanel {
   public static final SpotifyPanel INSTANCE = new SpotifyPanel();
   private static final float UNIT = 0.5F;
   private static final float LOGO_ROOM = 22.0F;
   private final Map<String, Int2ObjectOpenHashMap<StableWidth>> slots = new HashMap<>();
   private float mouseX = -1.0F;
   private float mouseY = -1.0F;
   private boolean chatInput;
   private SpotifyPanel.SpotifyPlan plan;
   private long lastFrame;
   private static final SpotifyWindow.MediaKey[] SPOTIFY_BUTTONS = new SpotifyWindow.MediaKey[]{
      SpotifyWindow.MediaKey.PREVIOUS, SpotifyWindow.MediaKey.PLAY_PAUSE, SpotifyWindow.MediaKey.NEXT
   };
   private final float[] spotifyButtons = new float[12];
   private boolean spotifyButtonsShown;
   private boolean spotifyButtonsDrawn;
   private static final float SPOTIFY_PAD_Y = 8.0F;
   private static final float SPOTIFY_LINE_GAP = 2.0F;
   private static final float SPOTIFY_GLYPH = 10.0F;
   private static final float SPOTIFY_GLYPH_GAP = 10.0F;
   private static final float SPOTIFY_PAUSED = 0.62F;
   private static final float MARQUEE_SPEED = 30.0F;
   private static final float MARQUEE_HOLD = 2.4F;
   private static final int LYRIC_MAX_ROWS = 4;
   private static final float LYRIC_GROW_SPEED = 14.0F;
   private static final float LYRIC_SHRINK_SPEED = 9.0F;
   private static final float LYRIC_FAR_ALPHA = 0.6F;
   private static final float LYRIC_PAUSED_ALPHA = 0.7F;
   private static final float LYRIC_PAUSE_SPEED = 8.0F;
   private static final float LYRIC_BREATH_PERIOD = 1.6F;
   private static final long LYRIC_LEAD_MS = 200L;
   private static final long LYRIC_SWEEP_MS = 180L;
   private static final float LYRIC_SLOW_PACE = 2.0F;
   private static final float LYRIC_CHAR_MS = 90.0F;
   private static final float LYRIC_LIT_ALL = 1.0F / 0.0F;
   private static final long LYRIC_QUIET_MS = 700L;
   private static final float LYRIC_MIN_COVERAGE = 0.75F;
   private static final String[] NO_ROWS = new String[0];
   private static final NowPlaying SAMPLE_TRACK = new NowPlaying(
      NowPlaying.Status.PLAYING, "Golden Hour Drive", "Sample Artist", "", NowPlaying.Source.SESSION, 214000L, 84000L, 0L, 1.0, null
   );
   private static final Lyrics SAMPLE_LYRICS = new Lyrics(
      Lyrics.Status.SYNCED,
      SyncedLyrics.parse(
         "[01:21.00]Every light in the city knows my name\n[01:27.40]We keep on driving till the morning comes\n[01:33.80]Nothing but the radio and you\n"
      ),
      List.of(),
      0L
   );
   private float marqueeTime;
   private String marqueeTitle = "";
   private String marqueeArtist = "";
   private final PlaybackClock spotifyClock = new PlaybackClock();
   private NowPlaying spotifyFolded;
   private String spotifyTitle = "";
   private String spotifyArtist = "";
   private AlbumArt spotifyArt;
   private CardTexture spotifyArtTexture;
   private Lyrics lyricPrepared;
   private String[] lyricTexts = NO_ROWS;
   private String[][] lyricRows = new String[0][];
   private int lyricRowsPx;
   private int lyricRowsWidth;
   private SpotifyPanel.LyricWords[] lyricWords = new SpotifyPanel.LyricWords[0];
   private float lyricCharMs = 90.0F;
   private boolean lyricReadable = true;
   private float lyricViewH = -1.0F;
   private int lyricViewLines;
   private float lyricViewScale;
   private SyncedLyrics lyricSource;
   private int lyricStart;
   private final List<SpotifyPanel.LyricShift> lyricShifts = new ArrayList<>(8);
   private float lyricClock;
   private List<SpotifyPanel.LyricItem> lyricFadeItems;
   private float lyricFade = 1.0F;
   private List<SpotifyPanel.LyricItem> lyricLastItems = List.of();
   private List<SpotifyPanel.LyricItem> lyricLeaveItems;
   private float lyricLeave;
   private float[] lyricShown = new float[0];
   private float lyricBreath;
   private float lyricPlaying = 1.0F;
   private int lyricCurrentFirst = -1;
   private int lyricCurrentLast = -1;
   private float[] lyricBright = new float[0];
   private int lyricBrightLo;
   private int lyricBrightHi = -1;
   private float lyricBlockH;
   private long lyricLoadingSince;

   private SpotifyPanel() {
   }

   public int width(boolean editMode) {
      SpotifyPanel.SpotifyPlan current = this.ensurePlan(editMode);
      return (int)Math.ceil((double)(current.width() * 0.5F));
   }

   public int height(boolean editMode) {
      SpotifyPanel.SpotifyPlan current = this.ensurePlan(editMode);
      return (int)Math.ceil((double)(current.height() * 0.5F));
   }

   public boolean visible() {
      return SpotifyPlayer.shouldShowHud();
   }

   private SpotifyPanel.SpotifyPlan ensurePlan(boolean editMode) {
      long now = System.nanoTime();
      if (this.plan == null || now - this.lastFrame > 5000000L) {
         float dt = this.lastFrame == 0L ? 0.0F : Math.min(0.1F, (float)(now - this.lastFrame) / 1.0E9F);
         this.lastFrame = now;
         this.plan = this.planSpotify(editMode, dt);
      }

      return this.plan;
   }

   public void draw(class_332 context, double localX, double localY, boolean editMode, boolean chatOpen) {
      SpotifyPanel.SpotifyPlan current = this.ensurePlan(editMode);
      this.chatInput = chatOpen;
      this.mouseX = (float)(localX / 0.5);
      this.mouseY = (float)(localY / 0.5);
      this.spotifyButtonsShown = this.spotifyButtonsDrawn;
      this.spotifyButtonsDrawn = false;
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.scale(0.5F, 0.5F);

      try {
         this.drawSpotify(OverlayCanvas.of(context), current, 1.0F);
      } finally {
         matrices.popMatrix();
      }

      this.plan = null;
   }

   public boolean press(double localX, double localY) {
      if (!this.spotifyButtonsShown) {
         return false;
      } else {
         int button = this.spotifyButtonAt((float)(localX / 0.5), (float)(localY / 0.5));
         if (button < 0) {
            return false;
         } else {
            SpotifyPlayer.hudButton(SPOTIFY_BUTTONS[button]);
            return true;
         }
      }
   }

   private SpotifyPanel.SpotifyPlan planSpotify(boolean editMode, float dt) {
      float s = 1.0F;
      SpotifyPanel.Type t = SpotifyPanel.Type.of(s);
      if (!editMode && !SpotifyPlayer.shouldShowHud()) {
         this.removeSlot("spotify", t.textPx());
         this.releaseSpotifyArt();
         this.lyricBlockH = 0.0F;
         return new SpotifyPanel.SpotifyPlan(
            t,
            SpotifyPanel.SpotifyLook.HIDDEN,
            SpotifyPanel.SpotifyGlyph.NONE,
            "",
            "",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            false,
            null,
            SpotifyPanel.SpotifyColors.DEFAULT,
            1.0F,
            (float)Math.round(28.0F * s)
         );
      } else {
         SpotifyPlayer module = SpotifyPlayer.get();
         SpotifyPlayer active = SpotifyPlayer.active();
         NowPlaying now = active != null ? active.nowPlaying() : NowPlaying.WAITING;
         boolean track = now.active() && now.hasTrack();
         if (track && now.source() == NowPlaying.Source.SESSION) {
            return this.planSpotifyCard(module, now, false, true, t, s, editMode, dt);
         } else if (editMode && !track) {
            return this.planSpotifyCard(module, SAMPLE_TRACK, true, true, t, s, true, dt);
         } else {
            this.releaseSpotifyArt();
            this.lyricBlockH = 0.0F;
            return this.planSpotifyCompact(module, now, t, s, editMode, dt);
         }
      }
   }

   private void drawSpotify(OverlayCanvas dl, SpotifyPanel.SpotifyPlan plan, float a) {
      if (!(a <= 0.001F) && plan.look() != SpotifyPanel.SpotifyLook.HIDDEN) {
         float s = 1.0F;
         float x = 0.0F;
         float y = 0.0F;
         float w = plan.width();
         float h = plan.height();
         CardChrome.card(dl, x, y, w, h, 8.0F * s, a, true, s);
         if (plan.look() == SpotifyPanel.SpotifyLook.CARD) {
            this.drawSpotifyCard(dl, plan, x, y, w, h, s, a, false);
            float size = (float)plan.type().labelPx();
            this.drawLogo(dl, x + w - 10.0F * s - size, y + plan.card().textTop(), size, a);
         } else {
            this.drawSpotifyCompact(dl, plan, x, y, w, h, s, a, false);
            float size = 14.0F * s;
            this.drawLogo(dl, x + w - 12.0F * s - size, y + (h - size) * 0.5F, size, a);
         }
      }
   }

   private void drawLogo(OverlayCanvas dl, float x, float y, float size, float a) {
      if (!(a <= 0.003F)) {
         class_332 context = dl.context();
         Matrix3x2fStack matrices = context.method_51448();
         Vector2f origin = matrices.transformPosition(x, y, new Vector2f());
         class_1041 window = class_310.method_1551().method_22683();
         float ratio = (float)window.method_4506() / (float)Math.max(1, window.method_4502());
         int side = Math.max(4, Math.round(size * Smooth.pixelsPerUnit(context)));
         int texture = SpotifyLogo.sizeFor((float)side);
         int left = Math.round(origin.x * ratio);
         int top = Math.round(origin.y * ratio);
         int color = Math.round(Math.max(0.0F, Math.min(1.0F, a)) * 255.0F) << 24 | 16777215;
         matrices.pushMatrix();
         matrices.identity();
         matrices.scale(1.0F / ratio, 1.0F / ratio);
         context.method_25293(
            class_10799.field_56883, SpotifyLogo.texture(texture), left, top, 0.0F, 0.0F, side, side, texture, texture, texture, texture, color
         );
         matrices.popMatrix();
      }
   }

   private float slot(String key, int px, float target, float dt) {
      Int2ObjectOpenHashMap<StableWidth> bySize = this.slots.computeIfAbsent(key, k -> new Int2ObjectOpenHashMap());
      StableWidth width = (StableWidth)bySize.get(px);
      if (width == null) {
         width = new StableWidth();
         bySize.put(px, width);
      }

      return width.update(target, dt);
   }

   private void removeSlot(String key, int px) {
      Int2ObjectOpenHashMap<StableWidth> bySize = this.slots.get(key);
      if (bySize != null) {
         bySize.remove(px);
      }
   }

   private SpotifyPanel.SpotifyPlan planSpotifyCompact(SpotifyPlayer module, NowPlaying now, SpotifyPanel.Type t, float s, boolean editMode, float dt) {
      boolean withArtist = module == null || module.showArtist();
      String artist = "";
      SpotifyPanel.SpotifyGlyph glyph = SpotifyPanel.SpotifyGlyph.NONE;
      boolean idle = false;
      String title;
      switch (now.status()) {
         case PLAYING:
         case PAUSED:
            glyph = now.playing() ? SpotifyPanel.SpotifyGlyph.PLAYING : SpotifyPanel.SpotifyGlyph.PAUSED;
            this.foldTrack(now, t.text());
            title = now.hasTrack() ? this.spotifyTitle : "Not playing";
            if (withArtist) {
               artist = this.spotifyArtist;
            }
            break;
         case NOT_RUNNING:
            title = "Spotify is not open";
            idle = true;
            break;
         case UNSUPPORTED:
            title = "Spotify · Windows only";
            idle = true;
            break;
         default:
            title = "Nothing playing";
            idle = true;
      }

      CardFont titleFont = idle ? t.label() : t.text();
      int titlePx = idle ? t.labelPx() : t.textPx();
      float titleW = CardChrome.width(titleFont, titlePx, title);
      float artistW = artist.isEmpty() ? 0.0F : CardChrome.width(t.label(), t.labelPx(), artist);
      float glyphW = glyph == SpotifyPanel.SpotifyGlyph.NONE ? 0.0F : 20.0F * s;
      float fixed = 12.0F * s * 2.0F + glyphW + 22.0F * s;
      float maxText = Math.max(40.0F * s, (module != null ? module.cardWidth() : 320.0F) * s - fixed);
      float textW = Math.min(Math.max(titleW, artistW), maxText);
      float w = this.slot("spotify", t.textPx(), (float)Math.ceil((double)(fixed + textW)), dt);
      float height = artist.isEmpty() ? 28.0F * s : 8.0F * s * 2.0F + (float)t.textPx() + 2.0F * s + (float)t.labelPx();
      float avail = Math.max(1.0F, w - fixed);
      boolean scroll = module == null || module.scrollTitles();
      float loopGap = 40.0F * s;
      float[] offsets = this.marquee(title, artist, titleW, artistW, avail, scroll, now.playing() || editMode, loopGap, s, dt);
      return new SpotifyPanel.SpotifyPlan(
         t,
         idle ? SpotifyPanel.SpotifyLook.IDLE : SpotifyPanel.SpotifyLook.COMPACT,
         glyph,
         title,
         artist,
         titleW,
         artistW,
         offsets[0],
         offsets[1],
         loopGap,
         scroll,
         null,
         SpotifyPanel.SpotifyColors.of(module),
         (float)Math.round(w),
         (float)Math.round(height)
      );
   }

   private void drawSpotifyCompact(OverlayCanvas dl, SpotifyPanel.SpotifyPlan plan, float x, float y, float w, float h, float s, float a, boolean guard) {
      SpotifyPanel.Type t = plan.type();
      float cx = x + 12.0F * s;
      if (plan.glyph() != SpotifyPanel.SpotifyGlyph.NONE) {
         this.drawPlayState(dl, cx, y + h * 0.5F, 10.0F * s, plan.glyph() == SpotifyPanel.SpotifyGlyph.PLAYING, a);
         cx += 20.0F * s;
      }

      float avail = Math.max(1.0F, x + w - 12.0F * s - 22.0F * s - cx);
      if (plan.look() == SpotifyPanel.SpotifyLook.IDLE) {
         float top = y + (float)Math.floor((double)((h - (float)t.labelPx()) * 0.5F));
         marqueeLine(
            dl,
            t.label(),
            t.labelPx(),
            cx,
            top,
            avail,
            plan.titleW(),
            0.0F,
            plan.loopGap(),
            false,
            plan.title(),
            CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a),
            guard
         );
      } else {
         float dim = plan.glyph() == SpotifyPanel.SpotifyGlyph.PAUSED ? 0.62F : 1.0F;
         boolean twoLines = !plan.artist().isEmpty();
         float contentH = twoLines ? (float)t.textPx() + 2.0F * s + (float)t.labelPx() : (float)t.textPx();
         float titleTop = y + (float)Math.floor((double)((h - contentH) * 0.5F));
         marqueeLine(
            dl,
            t.text(),
            t.textPx(),
            cx,
            titleTop,
            avail,
            plan.titleW(),
            plan.titleOffset(),
            plan.loopGap(),
            plan.scroll(),
            plan.title(),
            CardChrome.scaleAlpha(plan.colors().title(), a * dim),
            guard
         );
         if (twoLines) {
            float artistTop = titleTop + (float)t.textPx() + 2.0F * s;
            marqueeLine(
               dl,
               t.label(),
               t.labelPx(),
               cx,
               artistTop,
               avail,
               plan.artistW(),
               plan.artistOffset(),
               plan.loopGap(),
               plan.scroll(),
               plan.artist(),
               CardChrome.scaleAlpha(plan.colors().artist(), a),
               guard
            );
         }
      }
   }

   private void drawPlayState(OverlayCanvas dl, float left, float centerY, float size, boolean playing, float a) {
      float top = (float)Math.round(centerY - size * 0.5F);
      float bottom = top + (float)Math.round(size);
      if (playing) {
         dl.addTriangleFilled(left, top, left + size * 0.9F, (top + bottom) * 0.5F, left, bottom, CardChrome.scaleAlpha(CardChrome.accent(), a));
      } else {
         float bar = Math.max(2.0F, (float)Math.round(size * 0.3F));
         float gap = Math.max(2.0F, (float)Math.round(size * 0.25F));
         int color = CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a);
         float l = (float)Math.round(left + (size - bar * 2.0F - gap) * 0.5F);
         dl.addRectFilled(l, top, l + bar, bottom, color, 1.0F);
         dl.addRectFilled(l + bar + gap, top, l + bar * 2.0F + gap, bottom, color, 1.0F);
      }
   }

   private SpotifyPanel.SpotifyPlan planSpotifyCard(
      SpotifyPlayer module, NowPlaying track, boolean sample, boolean lyricsSeen, SpotifyPanel.Type t, float s, boolean editMode, float dt
   ) {
      boolean withArtist = module == null || module.showArtist();
      boolean withArt = module == null || module.showAlbumArt();
      boolean withProgress = module == null || module.showProgress();
      boolean withLyrics = module == null || module.showLyrics();
      boolean scroll = module == null || module.scrollTitles();
      long nowMs = System.currentTimeMillis();
      this.foldTrack(track, t.text());
      String title = this.spotifyTitle;
      String artist = withArtist ? this.spotifyArtist : "";
      boolean timeline = track.hasTimeline();
      long position = track.positionMs();
      if (!sample) {
         this.spotifyClock.update(track, nowMs);
         position = this.spotifyClock.positionAt(nowMs);
      }

      float capTop = this.capTop(t.label(), t.labelPx());
      float transportTop = (float)t.labelPx() + 3.0F * s + (float)t.textPx() + (artist.isEmpty() ? 0.0F : 1.0F * s + (float)t.labelPx()) + 7.0F * s;
      boolean progressBar = withProgress && timeline;
      float columnBottom = progressBar
         ? transportTop + (float)t.labelPx() + 6.0F * s + progressHeight(s)
         : transportTop + CardChrome.ascent(t.label(), t.labelPx());
      float header = (float)Math.round(columnBottom - capTop);
      float pad = 10.0F * s;
      float w = (float)Math.round(Math.max(200.0F, module != null ? module.cardWidth() : 320.0F) * s);
      boolean art = withArt && w - pad * 2.0F - header - 10.0F * s >= 128.0F * s;
      float column = w - pad * 2.0F - (art ? header + 10.0F * s : 0.0F);
      String elapsed = "";
      String remaining = "";
      float progress = 0.0F;
      if (timeline) {
         long elapsedSeconds = position / 1000L;
         long totalSeconds = (track.durationMs() + 500L) / 1000L;
         elapsed = formatSeconds(elapsedSeconds);
         remaining = "−" + formatSeconds(Math.max(0L, totalSeconds - elapsedSeconds));
         progress = clamp01((float)position / (float)track.durationMs());
      }

      float titleW = CardChrome.width(t.text(), t.textPx(), title);
      float artistW = artist.isEmpty() ? 0.0F : CardChrome.width(t.label(), t.labelPx(), artist);
      float loopGap = 40.0F * s;
      float[] offsets = this.marquee(title, artist, titleW, artistW, column, scroll, track.playing() || editMode, loopGap, s, dt);
      boolean lyricsWanted = sample ? withLyrics && timeline : lyricsSeen && withLyrics && module != null && module.wantsLyrics(track);
      Lyrics lyrics = !lyricsWanted ? null : (sample ? SAMPLE_LYRICS : module.lyricsFor(track));
      boolean online = module == null || module.lyricsOnline();
      int lines = module == null ? 2 : module.lyricLines();
      SpotifyPlayer.LyricMotion motion = module == null ? SpotifyPlayer.LyricMotion.WAVE : module.lyricMotion();
      SpotifyPlayer.LyricHighlight highlight = module == null ? SpotifyPlayer.LyricHighlight.OFF : module.lyricHighlight();
      SpotifyPanel.LyricView view = this.planLyrics(lyrics, lyricsWanted, online, position, lines, motion, highlight, t, w - pad * 2.0F, s, dt, nowMs);
      float playing = track.playing() ? 1.0F : 0.0F;
      this.lyricPlaying = this.lyricPlaying + (playing - this.lyricPlaying) * (1.0F - (float)Math.exp((double)(-8.0F * dt)));
      float lyricAlpha = 0.7F + 0.3F * this.lyricPlaying;
      SpotifyPanel.SpotifyCard card = new SpotifyPanel.SpotifyCard(
         track, sample, art, header, pad - capTop, column, timeline, elapsed, remaining, progress, progressBar, view, lyricAlpha
      );
      float height = pad * 2.0F + header + view.height();
      return new SpotifyPanel.SpotifyPlan(
         t,
         SpotifyPanel.SpotifyLook.CARD,
         SpotifyPanel.SpotifyGlyph.NONE,
         title,
         artist,
         titleW,
         artistW,
         offsets[0],
         offsets[1],
         loopGap,
         scroll,
         card,
         SpotifyPanel.SpotifyColors.of(module),
         w,
         (float)Math.round(height)
      );
   }

   private void drawSpotifyCard(OverlayCanvas dl, SpotifyPanel.SpotifyPlan plan, float x, float y, float w, float h, float s, float a, boolean guard) {
      SpotifyPanel.SpotifyCard card = plan.card();
      SpotifyPanel.Type t = plan.type();
      float pad = 10.0F * s;
      boolean playing = card.track().playing();
      float left = x + pad;
      if (card.art()) {
         this.drawAlbumArt(dl, card, left, y + pad, card.header(), s, a);
         left += card.header() + 10.0F * s;
      } else {
         this.releaseSpotifyArt();
      }

      float column = card.column();
      float top = y + card.textTop();
      CardChrome.text(
         dl, t.label(), t.labelPx(), left, (float)Math.round(top), CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a), playing ? "NOW PLAYING" : "PAUSED", guard
      );
      top += (float)t.labelPx() + 3.0F * s;
      marqueeLine(
         dl,
         t.text(),
         t.textPx(),
         left,
         (float)Math.round(top),
         column,
         plan.titleW(),
         plan.titleOffset(),
         plan.loopGap(),
         plan.scroll(),
         plan.title(),
         CardChrome.scaleAlpha(plan.colors().title(), a),
         guard
      );
      top += (float)t.textPx();
      if (!plan.artist().isEmpty()) {
         top += 1.0F * s;
         marqueeLine(
            dl,
            t.label(),
            t.labelPx(),
            left,
            (float)Math.round(top),
            column,
            plan.artistW(),
            plan.artistOffset(),
            plan.loopGap(),
            plan.scroll(),
            plan.artist(),
            CardChrome.scaleAlpha(plan.colors().artist(), a),
            guard
         );
         top += (float)t.labelPx();
      }

      top = (float)Math.round(top + 7.0F * s);
      this.drawTransport(dl, t, card, left, top, column, s, a, guard);
      if (card.progressBar()) {
         float barTop = (float)Math.round(top + (float)t.labelPx() + 6.0F * s);
         this.drawProgress(dl, left, barTop, column, progressHeight(s), card.progress(), playing, a);
      }

      SpotifyPanel.LyricView lyrics = card.lyrics();
      if (lyrics.height() > 0.5F) {
         float blockTop = (float)Math.round(y + pad + card.header() + 12.0F * s);
         this.drawLyrics(dl, t, lyrics, plan.colors(), x + pad, blockTop, w - pad * 2.0F, y + h - Math.max(1.0F, s), s, a * card.lyricAlpha(), guard);
      }
   }

   private void drawAlbumArt(OverlayCanvas dl, SpotifyPanel.SpotifyCard card, float x, float y, float size, float s, float a) {
      float r = Math.min(4.0F, 4.0F) * s;
      if (card.sample()) {
         this.releaseSpotifyArt();
      }

      CardTexture texture = card.sample() ? null : this.spotifyArtTexture(card.track().art());
      if (texture != null) {
         dl.addImage(texture.id(), x, y, x + size, y + size, CardChrome.scaleAlpha(-1, a));
      } else {
         dl.addRectFilled(x, y, x + size, y + size, CardChrome.scaleAlpha(318767103, a), r);
         float bar = Math.max(2.0F, (float)Math.round(size * 0.07F));
         float gap = Math.max(2.0F, (float)Math.round(bar * 0.8F));
         float base = (float)Math.round(y + size * 0.62F);
         float l = (float)Math.round(x + (size - bar * 3.0F - gap * 2.0F) * 0.5F);
         int color = CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a * 0.45F);
         float[] heights = new float[]{0.2F, 0.34F, 0.26F};

         for (int i = 0; i < heights.length; i++) {
            float bx = l + (float)i * (bar + gap);
            dl.addRectFilled(bx, base - (float)Math.round(size * heights[i]), bx + bar, base, color, 1.0F);
         }
      }

      dl.addRect(x, y, x + size, y + size, CardChrome.scaleAlpha(402653183, a), r, 0, 1.0F);
   }

   private void drawTransport(
      OverlayCanvas dl, SpotifyPanel.Type t, SpotifyPanel.SpotifyCard card, float left, float top, float column, float s, float a, boolean guard
   ) {
      float elapsedW = 0.0F;
      float remainingW = 0.0F;
      if (card.timeline()) {
         elapsedW = CardChrome.tabularWidth(t.label(), t.labelPx(), card.elapsed());
         remainingW = CardChrome.tabularWidth(t.label(), t.labelPx(), card.remaining());
         int color = CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a);
         CardChrome.tabular(dl, t.label(), t.labelPx(), left, top, color, card.elapsed(), guard);
         CardChrome.tabular(dl, t.label(), t.labelPx(), (float)Math.round(left + column - remainingW), top, color, card.remaining(), guard);
      }

      float skip = Math.max(4.0F, (float)Math.round(8.0F * s));
      float play = Math.max(5.0F, (float)Math.round(10.0F * s));
      float spacing = (float)Math.round(14.0F * s);
      float cluster = skip * 2.0F + play + spacing * 2.0F;
      float centre = left + column * 0.5F;
      float margin = 8.0F * s;
      if (!(centre - cluster * 0.5F < left + elapsedW + margin) && !(centre + cluster * 0.5F > left + column - remainingW - margin)) {
         float centerY = top + (this.capTop(t.label(), t.labelPx()) + CardChrome.ascent(t.label(), t.labelPx())) * 0.5F;
         float playLeft = (float)Math.round(centre - play * 0.5F);
         float[] lefts = new float[]{playLeft - spacing - skip, playLeft, playLeft + play + spacing};
         float[] sizes = new float[]{skip, play, skip};
         float reach = (float)Math.round(3.0F * s);
         float mouseX = this.mouseX;
         float mouseY = this.mouseY;

         for (int i = 0; i < 3; i++) {
            float l = lefts[i] - reach;
            float r = lefts[i] + sizes[i] + reach;
            float tp = (float)Math.round(centerY - sizes[i] * 0.5F) - reach;
            float b = (float)Math.round(centerY + sizes[i] * 0.5F) + reach;
            this.spotifyButtons[i * 4] = l;
            this.spotifyButtons[i * 4 + 1] = tp;
            this.spotifyButtons[i * 4 + 2] = r;
            this.spotifyButtons[i * 4 + 3] = b;
            boolean hovered = this.chatInput && mouseX >= l && mouseX <= r && mouseY >= tp && mouseY <= b;
            if (hovered) {
               dl.addRectFilled(l, tp, r, b, CardChrome.scaleAlpha(654311423, a), (r - l) * 0.5F);
            }

            int color = CardChrome.scaleAlpha(!hovered && i != 1 ? CardChrome.TEXT_LABEL : CardChrome.TEXT_NAME, a);
            if (i == 1) {
               drawPlayPauseGlyph(dl, lefts[i], centerY, play, card.track().playing(), color);
            } else {
               drawSkipGlyph(dl, lefts[i], centerY, skip, i == 2, color);
            }
         }

         this.spotifyButtonsDrawn = true;
      }
   }

   private int spotifyButtonAt(float x, float y) {
      for (int i = 0; i < SPOTIFY_BUTTONS.length; i++) {
         if (x >= this.spotifyButtons[i * 4]
            && x <= this.spotifyButtons[i * 4 + 2]
            && y >= this.spotifyButtons[i * 4 + 1]
            && y <= this.spotifyButtons[i * 4 + 3]) {
            return i;
         }
      }

      return -1;
   }

   private static void drawSkipGlyph(OverlayCanvas dl, float left, float centerY, float size, boolean next, int color) {
      float top = (float)Math.round(centerY - size * 0.5F);
      float bottom = top + size;
      float mid = (top + bottom) * 0.5F;
      float bar = Math.max(1.0F, (float)Math.round(size * 0.2F));
      if (next) {
         dl.addTriangleFilled(left, top, left + size - bar, mid, left, bottom, color);
         dl.addRectFilled(left + size - bar, top, left + size, bottom, color);
      } else {
         dl.addRectFilled(left, top, left + bar, bottom, color);
         dl.addTriangleFilled(left + size, top, left + size, bottom, left + bar, mid, color);
      }
   }

   private static void drawPlayPauseGlyph(OverlayCanvas dl, float left, float centerY, float size, boolean playing, int color) {
      float top = (float)Math.round(centerY - size * 0.5F);
      float bottom = top + size;
      if (playing) {
         float bar = Math.max(2.0F, (float)Math.round(size * 0.3F));
         float gap = Math.max(2.0F, (float)Math.round(size * 0.24F));
         float l = (float)Math.round(left + (size - bar * 2.0F - gap) * 0.5F);
         dl.addRectFilled(l, top, l + bar, bottom, color, 1.0F);
         dl.addRectFilled(l + bar + gap, top, l + bar * 2.0F + gap, bottom, color, 1.0F);
      } else {
         float inset = (float)Math.round(size * 0.08F);
         dl.addTriangleFilled(left + inset, top, left + inset + size * 0.86F, (top + bottom) * 0.5F, left + inset, bottom, color);
      }
   }

   private void drawProgress(OverlayCanvas dl, float left, float top, float width, float height, float progress, boolean playing, float a) {
      float r = height * 0.5F;
      dl.addRectFilled(left, top, left + width, top + height, CardChrome.scaleAlpha(452984831, a), r);
      float fill = (float)Math.round(width * clamp01(progress));
      if (!(fill < 1.0F)) {
         dl.addRectFilled(left, top, left + fill, top + height, CardChrome.scaleAlpha(playing ? CardChrome.accent() : CardChrome.TEXT_LABEL, a), r);
      }
   }

   private static float progressHeight(float s) {
      return Math.max(1.0F, (float)Math.round(2.0F * s));
   }

   private SpotifyPanel.LyricView planLyrics(
      Lyrics lyrics,
      boolean wanted,
      boolean online,
      long position,
      int lines,
      SpotifyPlayer.LyricMotion motion,
      SpotifyPlayer.LyricHighlight highlight,
      SpotifyPanel.Type t,
      float innerW,
      float s,
      float dt,
      long nowMs
   ) {
      List<SpotifyPanel.LyricItem> items = List.of();
      String status = null;
      float content = -1.0F;
      boolean live = false;
      if (!wanted) {
         this.lyricLoadingSince = 0L;
         content = 0.0F;
      } else {
         if (lyrics == null || lyrics.status() != Lyrics.Status.LOADING) {
            this.lyricLoadingSince = 0L;
         } else if (this.lyricLoadingSince == 0L) {
            this.lyricLoadingSince = nowMs;
         }

         if (lyrics != null) {
            switch (lyrics.status()) {
               case LOADING:
                  status = nowMs - this.lyricLoadingSince < 700L ? null : "Loading lyrics";
                  break;
               case SYNCED:
                  this.prepareLyrics(lyrics, t.text());
                  if (this.lyricReadable) {
                     items = new ArrayList<>(16);
                     float viewH = this.lyricViewHeight(lyrics.synced(), lines, t, innerW, s);
                     this.syncedItems(items, lyrics.synced(), position, viewH, motion, highlight, t, innerW, s, dt);
                     content = viewH - ((float)t.textPx() - CardChrome.ascent(t.text(), t.textPx()));
                     live = true;
                  } else {
                     status = "Lyrics in a script the HUD font lacks";
                  }
                  break;
               case PLAIN:
                  status = "No synced lyrics";
                  break;
               case INSTRUMENTAL:
                  status = "Instrumental";
                  break;
               case NOT_FOUND:
                  status = online ? "No lyrics found" : "No saved lyrics";
                  break;
               case UNAVAILABLE:
                  status = "Lyrics unavailable";
            }
         }
      }

      if (status != null) {
         content = (float)Math.round((float)(t.textPx() - t.labelPx()) * 0.5F) + CardChrome.ascent(t.label(), t.labelPx());
      }

      if (live) {
         this.lyricLastItems = items;
         this.lyricLeaveItems = null;
      } else {
         items = this.leavingItems(status == null, t, s, dt);
      }

      float target = content < 0.0F ? this.lyricBlockH : (content > 0.0F ? 12.0F * s + (float)Math.round(content) : 0.0F);
      return new SpotifyPanel.LyricView(items, status, this.easeLyricBlock(target, dt));
   }

   private List<SpotifyPanel.LyricItem> leavingItems(boolean quiet, SpotifyPanel.Type t, float s, float dt) {
      this.lyricSource = null;
      if (!this.lyricLastItems.isEmpty()) {
         this.lyricLeaveItems = this.lyricLastItems;
         this.lyricLeave = 0.0F;
         this.lyricLastItems = List.of();
      }

      if (this.lyricLeaveItems == null) {
         return List.of();
      } else {
         this.lyricLeave = Math.min(1.0F, this.lyricLeave + dt / 0.32F);
         if (quiet && !(this.lyricLeave >= 1.0F)) {
            List<SpotifyPanel.LyricItem> items = new ArrayList<>(this.lyricLeaveItems.size());
            addFaded(items, this.lyricLeaveItems, smooth(this.lyricLeave), 8.0F * s);
            return items;
         } else {
            this.lyricLeaveItems = null;
            return List.of();
         }
      }
   }

   private static void addFaded(List<SpotifyPanel.LyricItem> items, List<SpotifyPanel.LyricItem> frozen, float fade, float rise) {
      for (SpotifyPanel.LyricItem item : frozen) {
         float alpha = item.alpha() * (1.0F - fade);
         if (!(alpha <= 0.004F)) {
            items.add(new SpotifyPanel.LyricItem(item.text(), item.top() - rise * fade, item.highlight(), alpha, item.progress(), item.fill()));
         }
      }
   }

   private void syncedItems(
      List<SpotifyPanel.LyricItem> items,
      SyncedLyrics synced,
      long position,
      float viewH,
      SpotifyPlayer.LyricMotion motion,
      SpotifyPlayer.LyricHighlight highlight,
      SpotifyPanel.Type t,
      float innerW,
      float s,
      float dt
   ) {
      float lineGap = 6.0F * s;
      int currentLast = synced.indexAt(position + 200L);
      int currentFirst = synced.firstWithTimeOf(currentLast);
      int start = Math.max(0, currentFirst);
      if (synced != this.lyricSource) {
         this.lyricSource = synced;
         this.lyricStart = start;
         this.lyricShifts.clear();
         this.lyricClock = 0.0F;
         this.lyricFadeItems = null;
         this.lyricFade = 1.0F;
         this.lyricShown = new float[synced.size() * 2];
         this.lyricBright = new float[synced.size()];
         this.lyricBrightLo = 0;
         this.lyricBrightHi = -1;

         for (int i = Math.max(0, currentFirst); i <= currentLast; i++) {
            this.lyricBright[i] = 1.0F;
         }

         if (currentLast >= 0) {
            this.lyricBrightLo = Math.max(0, currentFirst);
            this.lyricBrightHi = currentLast;
         }
      }

      this.lyricClock += dt;
      this.lyricBreath = (this.lyricBreath + dt) % 1.6F;
      this.lyricCurrentFirst = currentFirst;
      this.lyricCurrentLast = currentLast;
      float stagger = motion == SpotifyPlayer.LyricMotion.WAVE ? 0.045F : 0.0F;
      if (start != this.lyricStart) {
         float distance = start > this.lyricStart ? this.restDistance(this.lyricStart, start, t, innerW, s) : -1.0F;
         if (motion != SpotifyPlayer.LyricMotion.FADE && distance > 0.0F && distance <= viewH + lineGap + 0.5F) {
            if (this.lyricShifts.size() == 8) {
               this.lyricShifts.remove(0);
            }

            this.lyricShifts.add(new SpotifyPanel.LyricShift(distance, this.lyricClock, start, stagger));
         } else {
            this.lyricFadeItems = this.lyricLastItems;
            this.lyricFade = 0.0F;
            this.lyricShifts.clear();
            this.showView(synced, start, viewH, t, innerW, s);
         }

         this.lyricStart = start;
      }

      this.updateBrightness(currentFirst, currentLast, dt);
      this.lyricFade = Math.min(1.0F, this.lyricFade + dt / 0.32F);
      if (this.lyricFade >= 1.0F) {
         this.lyricFadeItems = null;
      }

      float settled = 1.37F;
      this.lyricShifts.removeIf(shift -> this.lyricClock - shift.at() > settled);
      if (this.lyricShifts.isEmpty()) {
         this.lyricClock = 0.0F;
      }

      float rise = 8.0F * s;
      if (this.lyricFadeItems != null) {
         float fade = smooth(this.lyricFade);
         addFaded(items, this.lyricFadeItems, fade, rise);
         this.addLyricView(items, synced, this.lyricStart, fade, rise * (1.0F - fade), viewH, position, highlight, t, innerW, s, dt);
      } else {
         this.addLyricView(items, synced, this.lyricStart, 1.0F, 0.0F, viewH, position, highlight, t, innerW, s, dt);
      }
   }

   private void addLyricView(
      List<SpotifyPanel.LyricItem> items,
      SyncedLyrics synced,
      int from,
      float alpha,
      float rise,
      float viewH,
      long position,
      SpotifyPlayer.LyricHighlight mode,
      SpotifyPanel.Type t,
      float innerW,
      float s,
      float dt
   ) {
      CardFont font = t.text();
      int px = t.textPx();
      float rowGap = 6.0F * s;
      float lineGap = 6.0F * s;
      float slot = (float)px + lineGap;
      boolean karaoke = mode == SpotifyPlayer.LyricHighlight.KARAOKE;
      float restTop = 0.0F;

      for (int i = from; i < synced.size() && restTop <= viewH + slot * 2.0F; i++) {
         String[] rows = this.rowsFor(i, font, px, innerW);
         int count = Math.max(1, rows.length);
         float lineTop = restTop + this.lyricOffset(i) + rise;
         float depth = 1.0F - 0.39999998F * clamp01(lineTop / slot - 1.0F);
         float highlight = this.lyricGlow(i, mode);
         float progress = rows.length == 0 ? this.breakProgress(synced, i, position) : 1.0F;

         for (int r = 0; r < count; r++) {
            float rowRest = restTop + (float)r * ((float)px + rowGap);
            float shown = this.updateShown(i, r, rowRest + (float)px <= viewH + 0.5F, dt);
            float top = lineTop + (float)r * ((float)px + rowGap);
            float rowAlpha = alpha * depth * smooth(shown) * topFade(top, s) * bottomFade(top + (float)px - viewH, px);
            if (!(rowAlpha <= 0.004F)) {
               float fill = karaoke && rows.length > 0 ? this.rowFill(synced, i, rows, r, font, px, position) : 0.0F;
               items.add(new SpotifyPanel.LyricItem(rows.length == 0 ? null : rows[r], top, highlight, rowAlpha, progress, fill));
            }
         }

         restTop += lineHeight(rows, px, rowGap) + lineGap;
      }

      float lineRest = 0.0F;

      for (int i = from - 1; i >= 0; i--) {
         String[] rows = this.rowsFor(i, font, px, innerW);
         int count = Math.max(1, rows.length);
         lineRest -= lineHeight(rows, px, rowGap) + lineGap;
         float lineTop = lineRest + this.lyricOffset(i) + rise;
         if (topFade(lineTop + (float)(count - 1) * ((float)px + rowGap), s) <= 0.0F) {
            break;
         }

         float highlight = this.lyricGlow(i, mode);
         float progress = rows.length == 0 ? this.breakProgress(synced, i, position) : 1.0F;

         for (int rx = 0; rx < count; rx++) {
            float top = lineTop + (float)rx * ((float)px + rowGap);
            float rowAlpha = alpha * smooth(this.lyricShown[i * 2 + rx]) * topFade(top, s);
            if (!(rowAlpha <= 0.004F)) {
               float fill = karaoke && rows.length > 0 ? this.rowFill(synced, i, rows, rx, font, px, position) : 0.0F;
               items.add(new SpotifyPanel.LyricItem(rows.length == 0 ? null : rows[rx], top, highlight, rowAlpha, progress, fill));
            }
         }
      }
   }

   private float lyricOffset(int index) {
      float offset = 0.0F;

      for (int k = 0; k < this.lyricShifts.size(); k++) {
         SpotifyPanel.LyricShift shift = this.lyricShifts.get(k);
         float delay = (float)Math.max(0, Math.min(6, index - shift.start())) * shift.stagger();
         float time = this.lyricClock - shift.at() - delay;
         offset += time <= 0.0F ? shift.distance() : shift.distance() * springLeft(time);
      }

      return offset;
   }

   private static float springLeft(float time) {
      float wt = 13.0F * time;
      return (1.0F + wt) * (float)Math.exp((double)(-wt));
   }

   private static float topFade(float top, float s) {
      return top >= 0.0F ? 1.0F : smooth(1.0F + top / (12.0F * s * 0.75F));
   }

   private static float bottomFade(float below, int px) {
      return below <= 0.5F ? 1.0F : smooth(1.0F - below / ((float)px * 0.6F));
   }

   private float updateShown(int index, int row, boolean fits, float dt) {
      int k = index * 2 + row;
      float step = dt / 0.3F;
      float shown = fits ? Math.min(1.0F, this.lyricShown[k] + step) : Math.max(0.0F, this.lyricShown[k] - step);
      this.lyricShown[k] = shown;
      return shown;
   }

   private void showView(SyncedLyrics synced, int from, float viewH, SpotifyPanel.Type t, float innerW, float s) {
      Arrays.fill(this.lyricShown, 0.0F);
      int px = t.textPx();
      float rowGap = 6.0F * s;
      float restTop = 0.0F;

      for (int i = from; i < synced.size() && restTop < viewH; i++) {
         String[] rows = this.rowsFor(i, t.text(), px, innerW);
         int r = 0;

         for (int count = Math.max(1, rows.length); r < count; r++) {
            if (restTop + (float)r * ((float)px + rowGap) + (float)px <= viewH + 0.5F) {
               this.lyricShown[i * 2 + r] = 1.0F;
            }
         }

         restTop += lineHeight(rows, px, rowGap) + 6.0F * s;
      }
   }

   private float restDistance(int from, int to, SpotifyPanel.Type t, float innerW, float s) {
      float rowGap = 6.0F * s;
      float distance = 0.0F;

      for (int i = from; i < to; i++) {
         distance += lineHeight(this.rowsFor(i, t.text(), t.textPx(), innerW), t.textPx(), rowGap) + 6.0F * s;
      }

      return distance;
   }

   private void updateBrightness(int currentFirst, int currentLast, float dt) {
      int lo = this.lyricBrightHi >= 0 ? this.lyricBrightLo : 2147483647;
      int hi = this.lyricBrightHi;
      if (currentLast >= 0) {
         lo = Math.min(lo, Math.max(0, currentFirst));
         hi = Math.max(hi, currentLast);
      }

      float step = dt / 0.35F;
      int lit = 2147483647;
      int last = -1;

      for (int i = Math.max(0, lo); i <= hi && i < this.lyricBright.length; i++) {
         boolean sung = i >= currentFirst && i <= currentLast;
         float bright = sung ? Math.min(1.0F, this.lyricBright[i] + step) : Math.max(0.0F, this.lyricBright[i] - step);
         this.lyricBright[i] = bright;
         if (bright > 0.0F) {
            lit = Math.min(lit, i);
            last = i;
         }
      }

      this.lyricBrightLo = last < 0 ? 0 : lit;
      this.lyricBrightHi = last;
   }

   private float lyricGlow(int index, SpotifyPlayer.LyricHighlight mode) {
      if (mode != SpotifyPlayer.LyricHighlight.OFF && index >= 0 && index < this.lyricCurrentFirst) {
         return 1.0F;
      } else {
         return index >= 0 && index < this.lyricBright.length ? smooth(this.lyricBright[index]) : 0.0F;
      }
   }

   private float breakProgress(SyncedLyrics synced, int index, long position) {
      if (index >= this.lyricCurrentFirst && index <= this.lyricCurrentLast) {
         int next = synced.lastWithTimeOf(index) + 1;
         if (next >= synced.size()) {
            return 1.0F;
         } else {
            long begin = synced.line(index).timeMs();
            long end = synced.line(next).timeMs() - 200L;
            return end <= begin ? 1.0F : clamp01((float)(position - begin) / (float)(end - begin));
         }
      } else {
         return 1.0F;
      }
   }

   private float rowFill(SyncedLyrics synced, int index, String[] rows, int row, CardFont font, int px, long position) {
      if (index < this.lyricCurrentFirst) {
         return 1.0F / 0.0F;
      } else if (index > this.lyricCurrentLast) {
         return 0.0F;
      } else {
         String text = rows[row];
         int rowStart = 0;
         if (row > 0) {
            String line = this.lyricTexts[index];
            rowStart = rows[0].length();

            while (rowStart < line.length() && line.charAt(rowStart) == ' ') {
               rowStart++;
            }
         }

         float lit = this.litChars(synced, index, position) - (float)rowStart;
         if (lit <= 0.0F) {
            return 0.0F;
         } else if (lit >= (float)text.length()) {
            return 1.0F / 0.0F;
         } else {
            int whole = (int)lit;
            if (whole > 0 && Character.isLowSurrogate(text.charAt(whole))) {
               whole--;
            }

            int next = whole + Character.charCount(text.codePointAt(whole));
            float x0 = CardChrome.width(font, px, text.substring(0, whole));
            float x1 = CardChrome.width(font, px, text.substring(0, next));
            return x0 + (x1 - x0) * clamp01((lit - (float)whole) / (float)(next - whole));
         }
      }
   }

   private float litChars(SyncedLyrics synced, int index, long position) {
      SpotifyPanel.LyricWords words = this.lyricWords[index];
      if (words == null) {
         this.timeWords(synced, synced.firstWithTimeOf(index));
         words = this.lyricWords[index];
      }

      float lit = 0.0F;
      long[] start = words.start();

      for (int k = 0; k < start.length && position >= start[k]; k++) {
         float swept = smooth((float)(position - start[k]) / (float)words.sweep()[k]);
         lit = (float)words.from()[k] + (float)(words.to()[k] - words.from()[k]) * swept;
      }

      return lit;
   }

   private void timeWords(SyncedLyrics synced, int first) {
      int last = synced.lastWithTimeOf(first);
      int next = last + 1;
      long begin = synced.line(first).timeMs();
      long handover = next < synced.size() ? synced.line(next).timeMs() - 200L : 9223372036854775807L;
      int letters = 0;

      for (int i = first; i <= last; i++) {
         letters += letterCount(this.lyricTexts[i]);
      }

      long window = next < synced.size()
         ? Math.min(handover - begin, (long)Math.round((float)letters * this.lyricCharMs * 2.0F))
         : (long)Math.round((float)letters * this.lyricCharMs);
      window = Math.max(0L, window);
      int done = 0;

      for (int i = first; i <= last; i++) {
         String text = this.lyricTexts[i];
         int count = text.isEmpty() ? 0 : text.length() - letterCount(text) + 1;
         int[] from = new int[count];
         int[] to = new int[count];
         long[] start = new long[count];
         long[] sweep = new long[count];
         List<Long> timed = synced.line(i).wordTimes();
         boolean own = timed.size() == count;
         int k = 0;

         for (int c = 0; k < count; k++) {
            from[k] = c;
            c = text.indexOf(32, c);
            if (c < 0) {
               c = text.length();
            }

            to[k] = c++;
            long at = own ? timed.get(k) : begin + window * (long)done / (long)letters;
            done += to[k] - from[k];
            long until = own ? (k + 1 < count ? timed.get(k + 1) : Math.min(handover, at + 180L)) : begin + window * (long)done / (long)letters;
            start[k] = at;
            sweep[k] = Math.max(1L, Math.min(180L, until - at));
         }

         this.lyricWords[i] = new SpotifyPanel.LyricWords(from, to, start, sweep);
      }
   }

   private float lyricPace(SyncedLyrics synced) {
      float[] paces = new float[synced.size()];
      int n = 0;
      int first = 0;

      while (first < synced.size()) {
         int last = synced.lastWithTimeOf(first);
         int letters = 0;

         for (int i = first; i <= last; i++) {
            letters += letterCount(this.lyricTexts[i]);
         }

         if (letters > 0 && last + 1 < synced.size()) {
            paces[n++] = (float)(synced.line(last + 1).timeMs() - synced.line(first).timeMs()) / (float)letters;
         }

         first = last + 1;
      }

      if (n == 0) {
         return 90.0F;
      } else {
         Arrays.sort(paces, 0, n);
         return paces[n / 2];
      }
   }

   private static int letterCount(String text) {
      int letters = text.length();

      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == ' ') {
            letters--;
         }
      }

      return letters;
   }

   private float lyricViewHeight(SyncedLyrics synced, int lines, SpotifyPanel.Type t, float innerW, float s) {
      int px = t.textPx();
      this.prepareRows(px, innerW);
      if (this.lyricViewH >= 0.0F && this.lyricViewLines == lines && this.lyricViewScale == s) {
         return this.lyricViewH;
      } else {
         int slots = lines;
         int start = 0;

         while (start < synced.size()) {
            int rows = 0;
            int i = start;

            for (int last = synced.lastWithTimeOf(start); i <= last; i++) {
               rows += Math.max(1, this.rowsFor(i, t.text(), px, innerW).length);
            }

            slots = Math.max(slots, Math.min(4, rows));
            start = synced.lastWithTimeOf(start) + 1;
         }

         this.lyricViewH = (float)(slots * px) + (float)(slots - 1) * 6.0F * s;
         this.lyricViewLines = lines;
         this.lyricViewScale = s;
         return this.lyricViewH;
      }
   }

   private static float lineHeight(String[] rows, int px, float rowGap) {
      return rows.length <= 1 ? (float)px : (float)(rows.length * px) + (float)(rows.length - 1) * rowGap;
   }

   private float easeLyricBlock(float target, float dt) {
      float diff = target - this.lyricBlockH;
      if (Math.abs(diff) < 0.5F) {
         this.lyricBlockH = target;
      } else {
         float speed = diff > 0.0F ? 14.0F : 9.0F;
         this.lyricBlockH = this.lyricBlockH + diff * (1.0F - (float)Math.exp((double)(-speed * dt)));
      }

      return this.lyricBlockH;
   }

   private void prepareLyrics(Lyrics lyrics, CardFont font) {
      if (lyrics != this.lyricPrepared) {
         this.lyricPrepared = lyrics;
         this.lyricViewH = -1.0F;
         SyncedLyrics synced = lyrics.synced();
         this.lyricTexts = new String[synced.size()];
         this.lyricRows = new String[synced.size()][];
         this.lyricWords = new SpotifyPanel.LyricWords[synced.size()];
         int letters = 0;
         int missing = 0;
         IntPredicate drawable = cpx -> this.hudHasGlyph(font, cpx);

         for (int i = 0; i < synced.size(); i++) {
            String text = DisplayText.fold(synced.line(i).text(), drawable);
            this.lyricTexts[i] = text;
            int k = 0;

            while (k < text.length()) {
               int cp = text.codePointAt(k);
               k += Character.charCount(cp);
               if (Character.isLetter(cp)) {
                  letters++;
                  if (cp >= 128 && !this.hudHasGlyph(font, cp)) {
                     missing++;
                  }
               }
            }
         }

         this.lyricReadable = (float)missing <= (float)letters * 0.25F;
         this.lyricCharMs = this.lyricPace(synced);
      }
   }

   private boolean hudHasGlyph(CardFont font, int cp) {
      return cp <= 65535;
   }

   private void prepareRows(int px, float maxW) {
      int widthKey = (int)maxW;
      if (px != this.lyricRowsPx || widthKey != this.lyricRowsWidth) {
         Arrays.fill(this.lyricRows, null);
         this.lyricRowsPx = px;
         this.lyricRowsWidth = widthKey;
         this.lyricViewH = -1.0F;
      }
   }

   private String[] rowsFor(int index, CardFont font, int px, float maxW) {
      this.prepareRows(px, maxW);
      String[] rows = this.lyricRows[index];
      if (rows == null) {
         rows = wrapLyric(font, px, this.lyricTexts[index], (float)this.lyricRowsWidth);
         this.lyricRows[index] = rows;
      }

      return rows;
   }

   private static String[] wrapLyric(CardFont font, int px, String text, float maxW) {
      if (text.isEmpty()) {
         return NO_ROWS;
      } else if (CardChrome.width(font, px, text) <= maxW) {
         return new String[]{text};
      } else {
         int best = -1;
         float bestW = 3.4028235E38F;

         for (int i = text.indexOf(32); i > 0; i = text.indexOf(32, i + 1)) {
            float firstW = CardChrome.width(font, px, text.substring(0, i).stripTrailing());
            if (firstW > maxW) {
               break;
            }

            float secondW = CardChrome.width(font, px, text.substring(i + 1).stripLeading());
            if (secondW <= maxW && Math.max(firstW, secondW) < bestW) {
               bestW = Math.max(firstW, secondW);
               best = i;
            }
         }

         if (best > 0) {
            return new String[]{text.substring(0, best).stripTrailing(), text.substring(best + 1).stripLeading()};
         } else {
            int cut = fitPrefix(font, px, text, maxW);
            String first = text.substring(0, cut).stripTrailing();
            String rest = text.substring(cut).stripLeading();
            return rest.isEmpty()
               ? new String[]{first}
               : new String[]{first, CardChrome.width(font, px, rest) <= maxW ? rest : ellipsize(font, px, rest, maxW)};
         }
      }
   }

   private static int fitPrefix(CardFont font, int px, String text, float maxW) {
      int lastBreak = -1;

      for (int i = text.indexOf(32); i > 0 && !(CardChrome.width(font, px, text.substring(0, i)) > maxW); i = text.indexOf(32, i + 1)) {
         lastBreak = i;
      }

      if (lastBreak > 0) {
         return lastBreak + 1;
      } else {
         int lo = 1;
         int hi = text.length();

         while (lo < hi) {
            int mid = lo + hi + 1 >>> 1;
            if (CardChrome.width(font, px, text.substring(0, mid)) <= maxW) {
               lo = mid;
            } else {
               hi = mid - 1;
            }
         }

         if (lo > 1 && lo < text.length() && Character.isHighSurrogate(text.charAt(lo - 1))) {
            lo--;
         }

         return lo;
      }
   }

   private void drawLyrics(
      OverlayCanvas dl,
      SpotifyPanel.Type t,
      SpotifyPanel.LyricView view,
      SpotifyPanel.SpotifyColors colors,
      float left,
      float top,
      float width,
      float bottom,
      float s,
      float a,
      boolean guard
   ) {
      float clipTop = top - 12.0F * s + Math.max(1.0F, 2.0F * s);
      if (!(bottom <= clipTop) && !(a <= 0.001F)) {
         float clipLeft = (float)Math.floor((double)(left - 2.0F));
         float clipRight = (float)Math.ceil((double)(left + width + 2.0F));
         dl.pushClipRect(clipLeft, clipTop, clipRight, bottom, true);
         if (view.status() != null) {
            float statusTop = (float)Math.round(top + (float)(t.textPx() - t.labelPx()) * 0.5F);
            CardChrome.text(dl, t.label(), t.labelPx(), left, statusTop, CardChrome.scaleAlpha(CardChrome.TEXT_LABEL, a * 0.85F), view.status(), guard);
         }

         for (SpotifyPanel.LyricItem item : view.items()) {
            float alpha = a * item.alpha();
            if (!(alpha <= 0.004F)) {
               float rowTop = top + item.top();
               if (item.text() == null) {
                  float centerY = rowTop + (this.capTop(t.text(), t.textPx()) + CardChrome.ascent(t.text(), t.textPx())) * 0.5F;
                  this.drawLyricBreak(dl, colors, left, centerY, s, item.highlight(), item.progress(), alpha);
               } else {
                  int color = CardChrome.scaleAlpha(CardChrome.lerpColor(colors.upcoming(), colors.lyric(), item.highlight()), alpha);
                  int lit = CardChrome.scaleAlpha(colors.lit(), alpha);
                  float edge = Math.min(clipRight, (float)Math.floor((double)left) + (float)Math.round(item.fill()));
                  if (item.fill() <= 0.0F) {
                     CardChrome.text(dl, t.text(), t.textPx(), left, (float)Math.round(rowTop), color, item.text(), guard);
                  } else if (edge >= clipRight) {
                     CardChrome.text(dl, t.text(), t.textPx(), left, (float)Math.round(rowTop), lit, item.text(), guard);
                  } else {
                     dl.pushClipRect(clipLeft, clipTop, edge, bottom, true);
                     CardChrome.text(dl, t.text(), t.textPx(), left, (float)Math.round(rowTop), lit, item.text(), guard);
                     dl.popClipRect();
                     dl.pushClipRect(edge, clipTop, clipRight, bottom, true);
                     CardChrome.text(dl, t.text(), t.textPx(), left, (float)Math.round(rowTop), color, item.text(), guard);
                     dl.popClipRect();
                  }
               }
            }
         }

         dl.popClipRect();
      }
   }

   private void drawLyricBreak(
      OverlayCanvas dl, SpotifyPanel.SpotifyColors colors, float left, float centerY, float s, float highlight, float progress, float alpha
   ) {
      float r = Math.max(1.0F, 1.6F * s);
      float step = Math.max(4.0F, (float)Math.round(6.0F * s));
      float breath = 0.5F - 0.5F * (float)Math.cos((double)(this.lyricBreath / 1.6F) * 2.0 * 3.141592653589793);

      for (int i = 0; i < 3; i++) {
         float lit = smooth(progress * 3.0F - (float)i) * highlight;
         int color = CardChrome.scaleAlpha(CardChrome.lerpColor(colors.upcoming(), colors.lit(), lit), alpha);
         float radius = r * (1.0F + 0.3F * lit + 0.12F * breath * highlight);
         dl.addCircleFilled(left + r + (float)i * step, centerY, radius, color, 12);
      }
   }

   private void foldTrack(NowPlaying now, CardFont font) {
      if (now != this.spotifyFolded) {
         this.spotifyFolded = now;
         IntPredicate drawable = cp -> this.hudHasGlyph(font, cp);
         String title = DisplayText.fold(now.title(), drawable);
         this.spotifyTitle = title.isEmpty() ? now.title() : title;
         this.spotifyArtist = DisplayText.fold(now.artist(), drawable);
      }
   }

   private float[] marquee(
      String title, String artist, float titleW, float artistW, float avail, boolean scroll, boolean advancing, float loopGap, float s, float dt
   ) {
      if (!title.equals(this.marqueeTitle) || !artist.equals(this.marqueeArtist)) {
         this.marqueeTitle = title;
         this.marqueeArtist = artist;
         this.marqueeTime = 0.0F;
      }

      float titleOffset = 0.0F;
      float artistOffset = 0.0F;
      float longest = Math.max(titleW > avail ? titleW : 0.0F, artistW > avail ? artistW : 0.0F);
      if (scroll && longest > 0.0F) {
         float speed = 30.0F * s;
         float period = 2.4F + (longest + loopGap) / speed;
         this.marqueeTime %= period;
         if (advancing || this.marqueeTime > 2.4F) {
            this.marqueeTime = (this.marqueeTime + dt) % period;
         }

         float travelled = Math.max(0.0F, this.marqueeTime - 2.4F) * speed;
         if (titleW > avail) {
            titleOffset = Math.min(travelled, titleW + loopGap);
         }

         if (artistW > avail) {
            artistOffset = Math.min(travelled, artistW + loopGap);
         }
      } else {
         this.marqueeTime = 0.0F;
      }

      return new float[]{titleOffset, artistOffset};
   }

   private static void marqueeLine(
      OverlayCanvas dl,
      CardFont font,
      int px,
      float x,
      float top,
      float avail,
      float textW,
      float offset,
      float loopGap,
      boolean scroll,
      String text,
      int color,
      boolean guard
   ) {
      if (textW <= avail + 0.5F) {
         CardChrome.text(dl, font, px, x, top, color, text, guard);
      } else if (!scroll) {
         CardChrome.text(dl, font, px, x, top, color, ellipsize(font, px, text, avail), guard);
      } else {
         dl.pushClipRect(x, top - 2.0F, x + avail, top + (float)px + 3.0F, true);
         CardChrome.text(dl, font, px, x - offset, top, color, text, guard);
         if (offset > 0.0F) {
            CardChrome.text(dl, font, px, x - offset + textW + loopGap, top, color, text, guard);
         }

         dl.popClipRect();
      }
   }

   private static String ellipsize(CardFont font, int px, String text, float maxW) {
      float room = maxW - CardChrome.width(font, px, "...");
      int lo = 0;
      int hi = text.length();

      while (lo < hi) {
         int mid = lo + hi + 1 >>> 1;
         if (CardChrome.width(font, px, text.substring(0, mid)) <= room) {
            lo = mid;
         } else {
            hi = mid - 1;
         }
      }

      return text.substring(0, lo).stripTrailing() + "...";
   }

   private static String formatSeconds(long totalSeconds) {
      long seconds = Math.max(0L, totalSeconds);
      long hours = seconds / 3600L;
      long minutes = seconds / 60L % 60L;
      StringBuilder out = new StringBuilder(8);
      if (hours > 0L) {
         out.append(hours).append(':').append(minutes < 10L ? "0" : "");
      }

      out.append(minutes).append(':');
      if (seconds % 60L < 10L) {
         out.append('0');
      }

      return out.append(seconds % 60L).toString();
   }

   private float capTop(CardFont font, int px) {
      return Math.max(0.0F, (float)Math.round((float)px * (font == null ? 0.2F : font.capTop())));
   }

   private CardTexture spotifyArtTexture(AlbumArt art) {
      if (art == this.spotifyArt) {
         return this.spotifyArtTexture;
      } else {
         this.releaseSpotifyArt();
         if (art == null) {
            return null;
         } else {
            this.spotifyArt = art;
            ByteBuffer pixels = MemoryUtil.memAlloc(art.byteSize());

            try {
               art.copyTo(pixels);
               pixels.flip();
               this.spotifyArtTexture = CardTexture.fromPixels(pixels, art.size());
            } catch (RuntimeException var7) {
               this.spotifyArtTexture = null;
            } finally {
               MemoryUtil.memFree(pixels);
            }

            return this.spotifyArtTexture;
         }
      }
   }

   private void releaseSpotifyArt() {
      if (this.spotifyArtTexture != null) {
         this.spotifyArtTexture.release();
      }

      this.spotifyArtTexture = null;
      this.spotifyArt = null;
   }

   private static float smooth(float value) {
      float t = clamp01(value);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static record LyricItem(String text, float top, float highlight, float alpha, float progress, float fill) {
   }

   private static record LyricShift(float distance, float at, int start, float stagger) {
   }

   private static record LyricView(List<SpotifyPanel.LyricItem> items, String status, float height) {
   }

   private static record LyricWords(int[] from, int[] to, long[] start, long[] sweep) {
   }

   private static record SpotifyCard(
      NowPlaying track,
      boolean sample,
      boolean art,
      float header,
      float textTop,
      float column,
      boolean timeline,
      String elapsed,
      String remaining,
      float progress,
      boolean progressBar,
      SpotifyPanel.LyricView lyrics,
      float lyricAlpha
   ) {
   }

   private static record SpotifyColors(int title, int artist, int lyric, int upcoming, int lit) {
      static final SpotifyPanel.SpotifyColors DEFAULT = new SpotifyPanel.SpotifyColors(
         CardChrome.TEXT_NAME, CardChrome.TEXT_LABEL, CardChrome.TEXT_VALUE, CardChrome.TEXT_LABEL, CardChrome.TEXT_VALUE
      );

      static SpotifyPanel.SpotifyColors of(SpotifyPlayer module) {
         if (module == null) {
            return DEFAULT;
         } else {
            SpotifyPlayer.LyricHighlight mode = module.lyricHighlight();
            int lyric = GuiPalette.abgr(module.lyricsColor());
            int highlight = GuiPalette.abgr(module.highlightColor());
            return new SpotifyPanel.SpotifyColors(
               GuiPalette.abgr(module.titleColor()),
               GuiPalette.abgr(module.artistColor()),
               mode == SpotifyPlayer.LyricHighlight.LINE ? highlight : lyric,
               GuiPalette.abgr(module.upcomingLyricsColor()),
               mode == SpotifyPlayer.LyricHighlight.OFF ? lyric : highlight
            );
         }
      }
   }

   private static enum SpotifyGlyph {
      NONE,
      PLAYING,
      PAUSED;
   }

   private static enum SpotifyLook {
      HIDDEN,
      IDLE,
      COMPACT,
      CARD;
   }

   private static record SpotifyPlan(
      SpotifyPanel.Type type,
      SpotifyPanel.SpotifyLook look,
      SpotifyPanel.SpotifyGlyph glyph,
      String title,
      String artist,
      float titleW,
      float artistW,
      float titleOffset,
      float artistOffset,
      float loopGap,
      boolean scroll,
      SpotifyPanel.SpotifyCard card,
      SpotifyPanel.SpotifyColors colors,
      float width,
      float height
   ) {
   }

   private static record Type(CardFont value, int valuePx, CardFont text, int textPx, CardFont label, int labelPx) {
      static SpotifyPanel.Type of(float scale) {
         return new SpotifyPanel.Type(
            CardFont.TEXT, CardChrome.px(18.0F, scale), CardFont.TEXT, CardChrome.px(17.0F, scale), CardFont.LABEL, CardChrome.px(14.0F, scale)
         );
      }
   }
}
