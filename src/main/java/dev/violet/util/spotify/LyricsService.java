package dev.violet.util.spotify;

/**
 * Lyrics are turned off in Copium Client: it never connects to a lyrics
 * service. This keeps the Spotify player's API so it still shows the song,
 * artist, album art and progress.
 */
public final class LyricsService implements AutoCloseable {
   public LyricsService() {
   }

   public Lyrics lookup(LyricsQuery query) {
      return Lyrics.UNAVAILABLE;
   }

   public void setOnline(boolean online) {
   }

   @Override
   public void close() {
   }
}
