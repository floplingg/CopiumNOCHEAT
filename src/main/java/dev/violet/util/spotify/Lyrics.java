package dev.violet.util.spotify;

import java.util.ArrayList;
import java.util.List;

public record Lyrics(Lyrics.Status status, SyncedLyrics synced, List<String> plainLines, long sourceId) {
   public static final Lyrics LOADING = new Lyrics(Lyrics.Status.LOADING, SyncedLyrics.EMPTY, List.of(), 0L);
   public static final Lyrics NOT_FOUND = new Lyrics(Lyrics.Status.NOT_FOUND, SyncedLyrics.EMPTY, List.of(), 0L);
   public static final Lyrics UNAVAILABLE = new Lyrics(Lyrics.Status.UNAVAILABLE, SyncedLyrics.EMPTY, List.of(), 0L);

   public Lyrics(Lyrics.Status status, SyncedLyrics synced, List<String> plainLines, long sourceId) {
      if (status == null) {
         status = Lyrics.Status.NOT_FOUND;
      }

      if (synced == null) {
         synced = SyncedLyrics.EMPTY;
      }

      plainLines = plainLines == null ? List.of() : List.copyOf(plainLines);
      this.status = status;
      this.synced = synced;
      this.plainLines = plainLines;
      this.sourceId = sourceId;
   }

   static List<String> plainLines(String text) {
      if (text != null && !text.isBlank()) {
         List<String> lines = new ArrayList<>();
         boolean gap = false;

         for (String raw : text.split("\r\n|\r|\n")) {
            String line = raw.strip();
            if (line.isEmpty()) {
               gap = !lines.isEmpty();
            } else {
               if (gap) {
                  lines.add("");
               }

               gap = false;
               lines.add(line);
            }
         }

         return List.copyOf(lines);
      } else {
         return List.of();
      }
   }

   public static enum Status {
      LOADING,
      SYNCED,
      PLAIN,
      INSTRUMENTAL,
      NOT_FOUND,
      UNAVAILABLE;
   }
}
