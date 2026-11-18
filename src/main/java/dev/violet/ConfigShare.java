package dev.violet;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigShare {
   public static final String PREFIX = "SC-";
   public static final int NAME_MIN = 3;
   public static final int NAME_MAX = 16;
   private static final String BASE = "https://textdb.online/";
   private static final String KEY = "soberclient-cfg-";
   private static final Pattern SHORT = Pattern.compile("(?i)^[SY]C-?([A-Za-z0-9_]{3,16})$");
   private static final SecureRandom RANDOM = new SecureRandom();
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(Redirect.NORMAL).build();

   private ConfigShare() {
   }

   public static String shortCode(String text) {
      if (text == null) {
         return null;
      } else {
         Matcher matcher = SHORT.matcher(text.replaceAll("\\s+", ""));
         return matcher.matches() ? matcher.group(1).toUpperCase(Locale.ROOT) : null;
      }
   }

   public static String cleanName(String text) {
      if (text == null) {
         return "";
      } else {
         String name = text.strip();
         if (name.regionMatches(true, 0, "SC-", 0, "SC-".length())) {
            name = name.substring("SC-".length());
         }

         name = name.replaceAll("[^A-Za-z0-9_]", "").toUpperCase(Locale.ROOT);
         return name.length() > 16 ? name.substring(0, 16) : name;
      }
   }

   public static CompletableFuture<String> upload(String json, String name, Set<String> mine) {
      return CompletableFuture.supplyAsync(() -> {
         String payload = ConfigSlots.pack(json);
         if (name != null && !name.isEmpty()) {
            if (name.length() < 3) {
               throw new IllegalArgumentException("A share name needs at least 3 letters or numbers.");
            } else if (!read(name).isEmpty() && !mine.contains(name)) {
               throw new IllegalArgumentException("SC-" + name + " is already taken. Pick another share name.");
            } else {
               write(name, payload);
               return "SC-" + name;
            }
         } else {
            for (int attempt = 0; attempt < 6; attempt++) {
               String digits = Integer.toString(100000 + RANDOM.nextInt(900000));
               if (read(digits).isEmpty()) {
                  write(digits, payload);
                  return "SC-" + digits;
               }
            }

            throw new IllegalStateException("No free code found.");
         }
      });
   }

   public static CompletableFuture<String> download(String code) {
      return CompletableFuture.supplyAsync(() -> {
         String payload = read(code);
         if (payload.isEmpty()) {
            throw new IllegalArgumentException("There is no config with the code SC-" + code + ".");
         } else {
            return ConfigSlots.unpack(payload);
         }
      });
   }

   private static void write(String code, String payload) {
      String body = "value=" + URLEncoder.encode(payload, StandardCharsets.UTF_8);
      HttpRequest request = HttpRequest.newBuilder(URI.create("https://textdb.online/update/?key=" + key(code)))
         .timeout(Duration.ofSeconds(20L))
         .header("Content-Type", "application/x-www-form-urlencoded")
         .POST(BodyPublishers.ofString(body))
         .build();
      String answer = send(request);
      if (!answer.replace(" ", "").contains("\"status\":1")) {
         throw new IllegalStateException("The upload was refused.");
      } else if (!read(code).equals(payload)) {
         throw new IllegalStateException("The upload could not be checked.");
      }
   }

   private static String read(String code) {
      HttpRequest request = HttpRequest.newBuilder(URI.create("https://textdb.online/" + key(code))).timeout(Duration.ofSeconds(15L)).GET().build();
      return send(request).strip();
   }

   private static String key(String code) {
      return "soberclient-cfg-" + code.toLowerCase(Locale.ROOT);
   }

   private static String send(HttpRequest request) {
      try {
         HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
         if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("The config server answered " + response.statusCode() + ".");
         } else {
            return response.body();
         }
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("The connection was interrupted.");
      } catch (IOException var3) {
         throw new IllegalStateException("The config server could not be reached.");
      }
   }
}
