package dev.violet.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AcOffFeed {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   private volatile AcOffFeed.Snapshot snapshot = AcOffFeed.Snapshot.EMPTY;
   private volatile boolean polling;
   private long lastPoll;

   public AcOffFeed.Snapshot snapshot() {
      return this.snapshot;
   }

   public void reset() {
      this.snapshot = AcOffFeed.Snapshot.EMPTY;
      this.lastPoll = 0L;
   }

   public void poll(String url, int intervalSeconds, Consumer<AcOffFeed.Snapshot> onResult) {
      long now = System.currentTimeMillis();
      if (!this.polling && now - this.lastPoll >= (long)intervalSeconds * 1000L) {
         this.lastPoll = now;

         HttpRequest request;
         try {
            request = HttpRequest.newBuilder(URI.create(url.trim())).timeout(Duration.ofSeconds(8L)).header("User-Agent", "SoberClient").GET().build();
         } catch (IllegalArgumentException var8) {
            this.finish(this.failed("Invalid feed URL", now), onResult);
            return;
         }

         this.polling = true;
         HTTP.sendAsync(request, BodyHandlers.ofString()).whenComplete((response, error) -> {
            AcOffFeed.Snapshot next;
            if (error != null) {
               next = this.failed("Feed not reachable", now);
            } else if (response.statusCode() != 200) {
               next = this.failed("Feed answered " + response.statusCode(), now);
            } else {
               try {
                  next = parse(response.body(), now);
               } catch (RuntimeException var8x) {
                  next = this.failed("Feed sent unreadable data", now);
               }
            }

            this.polling = false;
            this.finish(next, onResult);
         });
      }
   }

   private void finish(AcOffFeed.Snapshot next, Consumer<AcOffFeed.Snapshot> onResult) {
      this.snapshot = next;
      onResult.accept(next);
   }

   private AcOffFeed.Snapshot failed(String error, long now) {
      AcOffFeed.Snapshot last = this.snapshot;
      return new AcOffFeed.Snapshot(last.updatedAt(), last.spots(), error, now);
   }

   static AcOffFeed.Snapshot parse(String body, long now) {
      JsonObject root = JsonParser.parseString(body).getAsJsonObject();
      String updatedAt = root.has("updatedAt") ? root.get("updatedAt").getAsString() : "";
      List<AcOffFeed.Spot> spots = new ArrayList<>();
      if (root.has("alerts") && root.get("alerts").isJsonArray()) {
         for (JsonElement element : root.getAsJsonArray("alerts")) {
            if (element.isJsonObject()) {
               JsonObject o = element.getAsJsonObject();
               boolean test = o.has("test") && o.get("test").getAsBoolean();
               int goliath = o.has("goliath") ? o.get("goliath").getAsInt() : -1;
               int newGoliath = o.has("newGoliath") ? o.get("newGoliath").getAsInt() : goliath;
               int oldGoliath = o.has("oldGoliath") ? o.get("oldGoliath").getAsInt() : goliath;
               String region = o.has("region") ? o.get("region").getAsString() : "Unknown";
               String coords = o.has("coords") ? o.get("coords").getAsString() : "";
               double x = 0.0;
               double y = 0.0;
               double z = 0.0;
               if (o.has("position") && o.get("position").isJsonObject()) {
                  JsonObject p = o.getAsJsonObject("position");
                  x = p.has("x") ? p.get("x").getAsDouble() : 0.0;
                  y = p.has("y") ? p.get("y").getAsDouble() : 0.0;
                  z = p.has("z") ? p.get("z").getAsDouble() : 0.0;
               } else {
                  String[] parts = coords.trim().split("\\s+");
                  if (parts.length >= 3) {
                     try {
                        x = Double.parseDouble(parts[0]);
                        y = Double.parseDouble(parts[1]);
                        z = Double.parseDouble(parts[2]);
                     } catch (NumberFormatException var24) {
                     }
                  }
               }

               spots.add(new AcOffFeed.Spot(test, newGoliath, oldGoliath, region, coords, x, y, z));
            }
         }
      }

      return new AcOffFeed.Snapshot(updatedAt, List.copyOf(spots), null, now);
   }

   public static record Snapshot(String updatedAt, List<AcOffFeed.Spot> spots, String error, long fetchedAt) {
      public static final AcOffFeed.Snapshot EMPTY = new AcOffFeed.Snapshot("", List.of(), null, 0L);

      public boolean loaded() {
         return this.fetchedAt != 0L;
      }
   }

   public static record Spot(boolean test, int goliath, int oldGoliath, String region, String coords, double x, double y, double z) {
      public String key() {
         return this.region + "|" + this.coords + "|" + this.goliath + "|" + this.test;
      }
   }
}
