package dev.violet.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Status;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_10608;
import net.minecraft.class_2568.class_10613;

public class StaffLive extends Module implements Status {
   private static final Pattern LIVE_STATUS = Pattern.compile("\"liveRoom(?:UserInfo)?\"\\s*:\\s*\\{.{0,4000}?\"status\"\\s*:\\s*(\\d)", 32);
   private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36";
   private final Setting<Boolean> pasta = this.add(new BoolSetting("Pastagamer08", "Notifies when Pastagamer08 goes live on TikTok or YouTube.", true));
   private final Setting<Boolean> wizox = this.add(new BoolSetting("W1zox_", "Notifies when W1zox_ goes live on YouTube.", true));
   private final Setting<Boolean> munkerlich = this.add(new BoolSetting("Munkerlich", "Notifies when Munkerlich goes live on YouTube.", true));
   private final Setting<Integer> interval = this.add(new IntSetting("Interval", "Minutes between two checks.", 2, 1, 15));
   private final Setting<Boolean> chatLink = this.add(new BoolSetting("Chat Link", "Also writes a clickable link to the stream into your chat.", true));
   public final Setting<Boolean> hud = this.add(
      new BoolSetting("HUD", "Shows who is live as a card in your HUD. Move and resize it in the HUD editor while the chat is open.", true)
   );
   private final List<StaffLive.Stream> streams = List.of(
      new StaffLive.Stream("Pastagamer08", StaffLive.Platform.TikTok, "pastagamer08", "https://www.tiktok.com/@pastagamer08/live", this.pasta::get),
      new StaffLive.Stream("Pastagamer08", StaffLive.Platform.YouTube, "pastagamer08", "https://www.youtube.com/@pastagamer08/streams", this.pasta::get),
      new StaffLive.Stream("W1zox_", StaffLive.Platform.YouTube, "wizoxlol", "https://www.youtube.com/@wizoxlol/streams", this.wizox::get),
      new StaffLive.Stream("Munkerlich", StaffLive.Platform.YouTube, "Munkerlich", "https://www.youtube.com/@Munkerlich/streams", this.munkerlich::get)
   );
   private final Map<StaffLive.Stream, Boolean> live = new ConcurrentHashMap<>();
   private final Set<StaffLive.Stream> failed = ConcurrentHashMap.newKeySet();
   private HttpClient http;
   private ScheduledExecutorService executor;
   private long nextCheck;

   public StaffLive() {
      super("Staff Live", "Notifies you when Pastagamer08, W1zox_ or Munkerlich goes live on TikTok or YouTube.", Category.DONUT);
   }

   @Override
   public void onEnable() {
      this.live.clear();
      this.failed.clear();
      this.nextCheck = 0L;
      this.http = HttpClient.newBuilder()
         .connectTimeout(Duration.ofSeconds(10L))
         .followRedirects(Redirect.NORMAL)
         .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
         .proxy(ProxySelector.getDefault())
         .build();
      this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
         Thread thread = new Thread(runnable, "Violet Staff Live");
         thread.setDaemon(true);
         return thread;
      });
      this.executor.scheduleWithFixedDelay(this::poll, 0L, 15L, TimeUnit.SECONDS);
   }

   @Override
   public void onDisable() {
      if (this.executor != null) {
         this.executor.shutdownNow();
      }

      this.executor = null;
      this.live.clear();
      this.failed.clear();
   }

   private void poll() {
      long now = System.currentTimeMillis();
      if (now >= this.nextCheck) {
         this.nextCheck = now + (long)this.interval.get().intValue() * 60000L;

         for (StaffLive.Stream stream : this.streams) {
            if (!stream.enabled().get()) {
               this.live.remove(stream);
               this.failed.remove(stream);
            } else {
               Boolean result = this.check(stream);
               if (result == null) {
                  this.failed.add(stream);
               } else {
                  this.failed.remove(stream);
                  Boolean before = this.live.put(stream, result);
                  if (result && !Boolean.TRUE.equals(before)) {
                     this.announce(stream);
                  }
               }
            }
         }
      }
   }

   @Override
   public String statusTitle() {
      long count = this.live.values().stream().filter(Boolean::booleanValue).count();
      return count > 0L ? "STATUS  /  " + count + " LIVE" : "STATUS";
   }

   public List<StaffLive.Entry> entries() {
      List<StaffLive.Entry> entries = new ArrayList<>();

      for (StaffLive.Stream stream : this.streams) {
         Boolean state = this.live.get(stream);
         boolean enabled = this.isActive() && stream.enabled().get();
         String badge;
         int color;
         if (!enabled) {
            badge = "OFF";
            color = -11907241;
         } else if (Boolean.TRUE.equals(state)) {
            badge = "LIVE";
            color = -1750963;
         } else if (this.failed.contains(stream) && state == null) {
            badge = "NO DATA";
            color = -9741778;
         } else if (state == null) {
            badge = "CHECKING";
            color = -12959929;
         } else {
            badge = "OFFLINE";
            color = -12959929;
         }

         entries.add(new StaffLive.Entry(stream.name(), stream.platform().title, badge, color, enabled && Boolean.TRUE.equals(state), enabled, stream.link()));
      }

      return entries;
   }

   @Override
   public List<Status.Row> statusRows() {
      List<Status.Row> rows = new ArrayList<>();

      for (StaffLive.Entry entry : this.entries()) {
         String detail = entry.link().replace("https://", "").replace("www.", "");
         rows.add(new Status.Row(entry.name() + "  /  " + entry.platform(), detail, entry.badge(), entry.color(), entry.link()));
      }

      return rows;
   }

   @Override
   public String statusDescription() {
      return "Who is live and where. Click a row to open the stream in your browser.";
   }

   private Boolean check(StaffLive.Stream stream) {
      try {
         return stream.platform() == StaffLive.Platform.YouTube ? this.youtube(stream.handle()) : this.tiktok(stream.handle());
      } catch (Exception var3) {
         return null;
      }
   }

   private boolean youtube(String handle) throws Exception {
      HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.youtube.com/@" + handle + "/live"))
         .timeout(Duration.ofSeconds(20L))
         .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36")
         .header("Accept-Language", "en-US,en;q=0.9")
         .header("Cookie", "SOCS=CAI")
         .GET()
         .build();
      HttpResponse<String> response = this.http.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
         throw new IllegalStateException("status " + response.statusCode());
      } else {
         String body = response.body();
         if (body.contains("consent.youtube.com/m")) {
            throw new IllegalStateException("consent page");
         } else {
            return body.contains("\"isLiveNow\":true");
         }
      }
   }

   private boolean tiktok(String handle) throws Exception {
      try {
         return this.tiktokApi(handle);
      } catch (Exception var3) {
         return this.tiktokPage(handle);
      }
   }

   private boolean tiktokPage(String handle) throws Exception {
      HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.tiktok.com/@" + handle + "/live"))
         .timeout(Duration.ofSeconds(20L))
         .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36")
         .header("Accept-Language", "en-US,en;q=0.9")
         .GET()
         .build();
      HttpResponse<String> response = this.http.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
         throw new IllegalStateException("status " + response.statusCode());
      } else {
         Matcher room = LIVE_STATUS.matcher(response.body());
         if (room.find()) {
            return room.group(1).equals("2");
         } else {
            throw new IllegalStateException("no status");
         }
      }
   }

   private boolean tiktokApi(String handle) throws Exception {
      HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.tiktok.com/api-live/user/room/?aid=1988&sourceType=54&uniqueId=" + handle))
         .timeout(Duration.ofSeconds(20L))
         .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36")
         .header("Accept", "application/json")
         .GET()
         .build();
      HttpResponse<String> response = this.http.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
         throw new IllegalStateException("status " + response.statusCode());
      } else {
         JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("data");
         if (data == null) {
            throw new IllegalStateException("no data");
         } else {
            JsonObject room = data.has("liveRoom") && data.get("liveRoom").isJsonObject() ? data.getAsJsonObject("liveRoom") : null;
            JsonElement status = room != null ? room.get("status") : null;
            if (status == null && data.has("user") && data.get("user").isJsonObject()) {
               status = data.getAsJsonObject("user").get("status");
            }

            if (status != null && status.isJsonPrimitive()) {
               return status.getAsInt() == 2;
            } else {
               throw new IllegalStateException("no status");
            }
         }
      }
   }

   private void announce(StaffLive.Stream stream) {
      String platform = stream.platform().title;
      Notifications.push("Staff Live", stream.name() + " is live on " + platform + "!", Notifications.Type.WARNING);
      if (this.chatLink.get()) {
         mc.execute(
            () -> {
               if (mc.field_1724 != null) {
                  class_5250 link = class_2561.method_43470("[Open " + platform + "]")
                     .method_27694(
                        style -> style.method_10977(class_124.field_1075)
                              .method_30938(true)
                              .method_10958(new class_10608(URI.create(stream.link())))
                              .method_10949(new class_10613(class_2561.method_43470(stream.link())))
                     );
                  class_5250 message = class_2561.method_43470("[Staff Live] ")
                     .method_27692(class_124.field_1076)
                     .method_10852(class_2561.method_43470(stream.name() + " is live on " + platform + " ").method_27692(class_124.field_1068))
                     .method_10852(link);
                  mc.field_1705.method_1743().method_1812(message);
               }
            }
         );
      }
   }

   @Override
   public String getInfoString() {
      long count = this.live.values().stream().filter(Boolean::booleanValue).count();
      return count > 0L ? count + " live" : null;
   }

   public static record Entry(String name, String platform, String badge, int color, boolean live, boolean enabled, String link) {
   }

   private static enum Platform {
      YouTube("YouTube"),
      TikTok("TikTok");

      final String title;

      private Platform(String title) {
         this.title = title;
      }
   }

   private static record Stream(String name, StaffLive.Platform platform, String handle, String link, Supplier<Boolean> enabled) {
   }
}
