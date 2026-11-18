package dev.violet.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.event.impl.TickEvent;
import dev.violet.module.BusModule;
import dev.violet.module.ModuleCategory;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.StringSetting;
import dev.violet.util.discord.DiscordIpc;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2535;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_642;
import net.minecraft.class_8732;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscordPresence extends BusModule {
   private static final Logger LOGGER = LoggerFactory.getLogger("Violet");
   private static final String DEFAULT_APPLICATION_ID = "1559646375757615134";
   private static final String OLD_APPLICATION_ID = "1563661770114072688";
   private static final String DEFAULT_LOGO = "https://cdn.discordapp.com/app-icons/1559646375757615134/0bcb337998c0af0a201594fa884e71b3.png?size=512";
   private static volatile String logo = "https://cdn.discordapp.com/app-icons/1559646375757615134/0bcb337998c0af0a201594fa884e71b3.png?size=512";
   private static volatile boolean logoChecked;
   private static final String LOGO_TEXT = "SoberClient";
   private static final String CLIENT_LINE = "SoberClient "
      + FabricLoader.getInstance().getModContainer("soberclient").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("");
   private static final String BUTTON_LABEL = "Discord";
   private static final String BUTTON_URL = "https://discord.gg/soberclient";
   private static final String IN_MENU = "In Main Menu";
   private static final String SINGLEPLAYER = "Singleplayer";
   private static final String MULTIPLAYER = "Multiplayer";
   private static final String LAN_WORLD = "LAN World";
   private static final String REALMS = "Realms";
   private static final long GAME_STARTED = ProcessHandle.current().info().startInstant().orElseGet(Instant::now).getEpochSecond();
   private static final long RETRY_MIN_MS = 5000L;
   private static final long RETRY_MAX_MS = 30000L;
   private static final long IDLE_POLL_MS = 1000L;
   private static final long MIN_UPDATE_GAP_MS = 4000L;
   private static final long KEEP_ALIVE_MS = 60000L;
   private final BooleanSetting showServerIp = this.addSetting("show-server-ip", new BooleanSetting("Show Server IP", true));
   private final StringSetting applicationId = this.addSetting("application-id", new StringSetting("Application ID", "1559646375757615134"));
   private volatile DiscordPresence.Card card;
   private DiscordPresence.Link link;
   private class_2535 session;
   private long joinedAt;
   private boolean loggedIdle;

   private String applicationId() {
      String id = this.applicationId.get().trim();
      return !id.isEmpty() && !id.equals("1563661770114072688") ? id : "1559646375757615134";
   }

   public DiscordPresence() {
      super("discord-presence", "Discord Presence", "Shows SoberClient and the server you are on in your Discord status.", ModuleCategory.CLIENT);
   }

   @Override
   public void onEnable() {
      if (this.applicationId().isBlank()) {
         if (!this.loggedIdle) {
            this.loggedIdle = true;
            LOGGER.info("[Sober] Discord Presence: no Discord application id set; idle");
         }
      } else {
         this.session = null;
         this.card = new DiscordPresence.Card("In Main Menu", GAME_STARTED);
         this.link = new DiscordPresence.Link(this.link);
         this.refresh();
         this.on(TickEvent.TYPE, event -> this.refresh());
         this.link.start();
      }
   }

   @Override
   public void onDisable() {
      if (this.link != null) {
         this.link.stop();
      }

      this.session = null;
   }

   private void refresh() {
      class_310 mc = class_310.method_1551();
      class_634 listener = mc.method_1562();
      String details;
      long startedAt;
      if (listener != null) {
         if (listener.method_48296() != this.session) {
            this.session = listener.method_48296();
            this.joinedAt = System.currentTimeMillis() / 1000L;
         }

         details = this.place(mc, listener.method_45734());
         startedAt = this.joinedAt;
      } else {
         if (this.session != null && this.session.method_10758() && this.session.method_10744() instanceof class_8732) {
            return;
         }

         this.session = null;
         details = "In Main Menu";
         startedAt = GAME_STARTED;
      }

      DiscordPresence.Card shown = this.card;
      if (startedAt != shown.startedAt() || !details.equals(shown.details())) {
         this.card = new DiscordPresence.Card(details, startedAt);
         this.link.wake();
      }
   }

   private String place(class_310 mc, class_642 server) {
      if (mc.method_1542()) {
         return "Singleplayer";
      } else if (server == null) {
         return "Multiplayer";
      } else if (server.method_52811()) {
         return "Realms";
      } else if (server.method_2994()) {
         return "LAN World";
      } else {
         return this.showServerIp.get() ? server.field_3761 : "Multiplayer";
      }
   }

   private static void refreshLogo(String applicationId) {
      if (!logoChecked) {
         logoChecked = true;

         try {
            try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build()) {
               HttpRequest request = HttpRequest.newBuilder(URI.create("https://discord.com/api/v10/applications/" + applicationId + "/rpc"))
                  .timeout(Duration.ofSeconds(8L))
                  .GET()
                  .build();
               HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
               if (response.statusCode() == 200) {
                  JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
                  if (body.has("icon") && body.get("icon").isJsonPrimitive()) {
                     logo = "https://cdn.discordapp.com/app-icons/" + applicationId + "/" + body.get("icon").getAsString() + ".png?size=512";
                  }

                  return;
               }
            }

            return;
         } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
         } catch (RuntimeException | IOException var8) {
            LOGGER.debug("[Sober] Discord Presence: could not look up the app icon", var8);
         }
      }
   }

   private static JsonObject activity(DiscordPresence.Card card) {
      JsonObject activity = new JsonObject();
      activity.addProperty("details", card.details());
      activity.addProperty("state", CLIENT_LINE.strip());
      JsonObject timestamps = new JsonObject();
      timestamps.addProperty("start", card.startedAt());
      activity.add("timestamps", timestamps);
      JsonObject assets = new JsonObject();
      assets.addProperty("large_image", logo);
      assets.addProperty("large_text", "SoberClient");
      activity.add("assets", assets);
      if (!"https://discord.gg/soberclient".isEmpty()) {
         JsonObject button = new JsonObject();
         button.addProperty("label", "Discord");
         button.addProperty("url", "https://discord.gg/soberclient");
         JsonArray buttons = new JsonArray();
         buttons.add(button);
         activity.add("buttons", buttons);
      }

      return activity;
   }

   private static record Card(String details, long startedAt) {
   }

   private final class Link implements Runnable {
      private final Thread thread = new Thread(this, "Violet Discord Presence");
      private volatile boolean running = true;
      private Thread previous;
      private DiscordIpc ipc;
      private DiscordPresence.Card sent;
      private long sentAt;
      private long retryMs = 5000L;
      private String lastProblem;

      Link(DiscordPresence.Link previous) {
         this.previous = previous != null ? previous.thread : null;
         this.thread.setDaemon(true);
      }

      void start() {
         this.thread.start();
      }

      void wake() {
         LockSupport.unpark(this.thread);
      }

      void stop() {
         this.running = false;
         LockSupport.unpark(this.thread);
      }

      @Override
      public void run() {
         this.awaitPrevious();

         while (this.running) {
            if (this.ipc == null && !this.connect()) {
               this.pause(this.retryMs);
               this.retryMs = Math.min(this.retryMs * 2L, 30000L);
            } else {
               try {
                  this.ipc.poll();
                  DiscordPresence.Card next = DiscordPresence.this.card;
                  long since = System.currentTimeMillis() - this.sentAt;
                  if (this.sent == null || since >= (next.equals(this.sent) ? 60000L : 4000L)) {
                     this.send(next);
                  }
               } catch (IOException var41) {
                  this.report("lost Discord", var41);
                  this.hangUp();
                  continue;
               }

               this.pause(1000L);
            }
         }

         if (this.ipc != null) {
            this.clear();
         }
      }

      private void awaitPrevious() {
         if (this.previous != null) {
            try {
               this.previous.join();
            } catch (InterruptedException var2) {
               this.running = false;
            }

            this.previous = null;
         }
      }

      private boolean connect() {
         try {
            this.ipc = DiscordIpc.connect(DiscordPresence.this.applicationId());
         } catch (IOException var2) {
            this.report("could not connect", var2);
            return false;
         }

         if (this.ipc == null) {
            return false;
         } else {
            DiscordPresence.LOGGER.info("[Sober] Discord Presence: connected to Discord");
            DiscordPresence.refreshLogo(DiscordPresence.this.applicationId());
            this.retryMs = 5000L;
            this.lastProblem = null;
            this.sent = null;
            return true;
         }
      }

      private void send(DiscordPresence.Card next) throws IOException {
         try {
            this.ipc.setActivity(DiscordPresence.activity(next));
         } catch (DiscordIpc.RefusedException var3) {
            this.report("Discord refused the card", var3);
         }

         this.sent = next;
         this.sentAt = System.currentTimeMillis();
      }

      private void clear() {
         try {
            this.ipc.setActivity(null);
         } catch (IOException | DiscordIpc.RefusedException var2) {
         }

         this.hangUp();
      }

      private void hangUp() {
         this.ipc.close();
         this.ipc = null;
         this.sent = null;
      }

      private void pause(long millis) {
         LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(millis));
      }

      private void report(String what, Exception problem) {
         String line = what + " (" + problem + ")";
         if (!line.equals(this.lastProblem)) {
            this.lastProblem = line;
            DiscordPresence.LOGGER.warn("[Sober] Discord Presence: {}", line);
         }
      }
   }
}
