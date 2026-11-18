package dev.violet.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.ItemSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.DonutUtils;
import dev.violet.util.InvUtil;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_9304;
import net.minecraft.class_9334;

public class AuctionSniper extends Module {
   private static final String API_URL = "https://api.donutsmp.net/v1/auction/list/1";
   private final Setting<class_1792> item = this.add(new ItemSetting("Item", "Item to snipe (item id, e.g. elytra).", class_1802.field_8162));
   private final Setting<String> enchantments = this.add(
      new StringSetting("Enchantments", "Enchantments the item must have, comma separated (e.g. mending, unbreaking).", "")
   );
   private final Setting<String> blacklist = this.add(
      new StringSetting("Blacklist", "Enchantments the item must not have, comma separated (e.g. binding_curse).", "")
   );
   private final Setting<String> maxPrice = this.add(new StringSetting("Max Price", "Maximum price per item, e.g. 500, 1.5k, 2m.", "1k"));
   private final Setting<AuctionSniper.Mode> mode = this.add(
      new EnumSetting<>("Mode", "Manual keeps refreshing the /ah GUI. Api polls the DonutSMP API and only opens /ah on a hit.", AuctionSniper.Mode.Manual)
   );
   private final Setting<Boolean> quickBuy = this.add(new BoolSetting("Quick Buy", "Shift-click to skip the confirm screen (Donut+ rank). Risky.", false));
   private final Setting<Integer> refreshDelay = this.add(new IntSetting("Refresh Delay", "Ticks between /ah refreshes.", 2, 0, 100))
      .visible(() -> this.mode.get() == AuctionSniper.Mode.Manual);
   private final Setting<Integer> buyDelay = this.add(new IntSetting("Buy Delay", "Ticks to wait before clicking a found item.", 2, 0, 100));
   private final Setting<String> apiKey;
   private final Setting<Integer> apiRefreshRate;
   private final Setting<Boolean> apiNotifications;
   private final Setting<Boolean> autoResell;
   private final Setting<String> resellPrice;
   private final Setting<String> webhook;
   private final HttpClient http;
   private int wait;
   private boolean readyToBuy;
   private volatile boolean apiQuerying;
   private long lastApiQuery;
   private String apiSeller;
   private int apiStep;
   private AuctionSniper.Purchase purchase;
   private boolean resellQueued;
   private int resellTimeout;

   public AuctionSniper() {
      super("Auction Sniper", "Snipes cheap items from the auction house.", Category.DONUT);
      this.group("API");
      this.apiKey = this.add(new StringSetting("Api Key", "Get it with /api in game.", "", true)).visible(() -> this.mode.get() == AuctionSniper.Mode.Api);
      this.apiRefreshRate = this.add(new IntSetting("Api Refresh Rate", "Milliseconds between API requests.", 250, 10, 5000))
         .visible(() -> this.mode.get() == AuctionSniper.Mode.Api);
      this.apiNotifications = this.add(new BoolSetting("Api Notifications", "Show API hits and errors as notifications.", true))
         .visible(() -> this.mode.get() == AuctionSniper.Mode.Api);
      this.group("Resell");
      this.autoResell = this.add(new BoolSetting("Auto Resell", "Puts sniped items back on the auction house.", false));
      this.resellPrice = this.add(new StringSetting("Resell Price", "Resell price per item.", "1k")).visible(this.autoResell::get);
      this.group("Webhook");
      this.webhook = this.add(new StringSetting("Webhook Url", "Discord webhook that gets a message for every sniped item (empty = off).", "", true));
      this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
   }

   @Override
   public void onEnable() {
      this.wait = 0;
      this.readyToBuy = false;
      this.apiQuerying = false;
      this.lastApiQuery = 0L;
      this.apiSeller = null;
      this.apiStep = -1;
      this.purchase = null;
      this.resellQueued = false;
      if (this.item.get() == class_1802.field_8162) {
         this.error("Select an item first.", new Object[0]);
         this.setEnabled(false);
      } else {
         if (DonutUtils.parsePrice(this.maxPrice.get()) < 0.0) {
            this.error("Invalid max price.", new Object[0]);
            this.setEnabled(false);
         }
      }
   }

   @Override
   public void onTick() {
      this.trackPurchase();
      if (!this.resellQueued || !this.handleResell()) {
         if (this.wait > 0) {
            this.wait--;
         } else {
            if (this.mode.get() == AuctionSniper.Mode.Api) {
               this.tickApi();
            } else {
               this.tickManual();
            }
         }
      }
   }

   private void tickManual() {
      if (mc.field_1724.field_7512 instanceof class_1707 container) {
         if (container.method_17388() == 6) {
            this.handleAuctionList(container);
         } else if (container.method_17388() == 3) {
            this.handleConfirm(container);
         }
      } else {
         StringBuilder query = new StringBuilder(this.item.get().method_63680().getString());

         for (class_2960 id : enchantmentIds(this.enchantments.get())) {
            query.append(' ').append(id.method_12832().replace('_', ' '));
         }

         DonutUtils.command("ah " + query);
         this.wait = 20;
      }
   }

   private void tickApi() {
      if (this.apiSeller != null) {
         if (mc.field_1724.field_7512 instanceof class_1707 container) {
            this.apiStep = -1;
            if (container.method_17388() == 6) {
               this.handleAuctionList(container);
            } else if (container.method_17388() == 3) {
               this.handleConfirm(container);
            }
         } else {
            if (this.apiStep == -1) {
               DonutUtils.command("ah " + this.apiSeller);
               this.apiStep = 0;
            } else if (++this.apiStep > 40) {
               this.apiSeller = null;
            }
         }
      } else if (mc.field_1724.field_7512 instanceof class_1707 && mc.field_1755 != null && mc.field_1755.method_25440().getString().contains("Page")) {
         mc.field_1724.method_7346();
         this.wait = 20;
      } else if (!this.apiQuerying) {
         long now = System.currentTimeMillis();
         if (now - this.lastApiQuery >= (long)this.apiRefreshRate.get().intValue()) {
            this.lastApiQuery = now;
            if (this.apiKey.get().isBlank()) {
               this.error("API key is not set. Get one with /api.", new Object[0]);
               this.setEnabled(false);
            } else {
               this.apiQuerying = true;
               String key = this.apiKey.get().trim();
               String targetId = class_7923.field_41178.method_10221(this.item.get()).method_12832();
               double limit = DonutUtils.parsePrice(this.maxPrice.get());
               this.http
                  .sendAsync(
                     HttpRequest.newBuilder(URI.create("https://api.donutsmp.net/v1/auction/list/1"))
                        .timeout(Duration.ofSeconds(10L))
                        .header("Authorization", "Bearer " + key)
                        .header("Content-Type", "application/json")
                        .POST(BodyPublishers.ofString("{\"sort\": \"recently_listed\"}"))
                        .build(),
                     BodyHandlers.ofString()
                  )
                  .whenComplete((response, throwable) -> mc.execute(() -> {
                        this.apiQuerying = false;
                        if (this.isActive()) {
                           if (throwable != null) {
                              if (this.apiNotifications.get()) {
                                 this.warning("API error: %s", new Object[]{throwable.getMessage()});
                              }
                           } else if (response.statusCode() != 200) {
                              if (this.apiNotifications.get()) {
                                 this.warning("API error: %d", new Object[]{response.statusCode()});
                              }
                           } else {
                              this.handleApiResponse((String)response.body(), targetId, limit);
                           }
                        }
                     }));
            }
         }
      }
   }

   private void handleApiResponse(String body, String targetId, double limit) {
      try {
         JsonArray results = JsonParser.parseString(body).getAsJsonObject().getAsJsonArray("result");
         if (results == null) {
            return;
         }

         for (JsonElement element : results) {
            JsonObject auction = element.getAsJsonObject();
            JsonObject auctionItem = auction.getAsJsonObject("item");
            String id = auctionItem.get("id").getAsString();
            if (!id.contains("shulker_box")) {
               String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
               if (path.equals(targetId)) {
                  int count = auctionItem.has("count") ? Math.max(1, auctionItem.get("count").getAsInt()) : 1;
                  double price = auction.get("price").getAsDouble();
                  if (!(price / (double)count > limit)) {
                     String seller = auction.getAsJsonObject("seller").get("name").getAsString();
                     if (this.apiNotifications.get()) {
                        this.info("Found %s x%d for %s from %s", new Object[]{path, count, DonutUtils.formatPrice(price), seller});
                     }

                     this.apiSeller = seller;
                     this.apiStep = -1;
                     return;
                  }
               }
            }
         }
      } catch (Exception var16) {
         if (this.apiNotifications.get()) {
            this.warning("Could not read API response: %s", new Object[]{var16.getMessage()});
         }
      }
   }

   private void handleAuctionList(class_1707 container) {
      class_1799 sortButton = container.method_7611(47).method_7677();
      if (sortButton.method_7960()) {
         this.wait = 2;
      } else if (!DonutUtils.isSortActive(sortButton, "Recently Listed")) {
         DonutUtils.click(container.field_7763, 47, 1, class_1713.field_7794);
         this.wait = 5;
      } else {
         for (int slot = 0; slot < 45; slot++) {
            class_1799 stack = container.method_7611(slot).method_7677();
            if (stack.method_31574(this.item.get()) && !DonutUtils.isShulkerBox(stack) && this.matches(stack)) {
               if (!this.readyToBuy) {
                  this.readyToBuy = true;
                  this.wait = this.buyDelay.get();
                  return;
               }

               this.readyToBuy = false;
               if (this.quickBuy.get()) {
                  this.startPurchase(stack, DonutUtils.extractPrice(stack));
                  DonutUtils.click(container.field_7763, slot, 1, class_1713.field_7794);
               } else {
                  DonutUtils.click(container.field_7763, slot, 0, class_1713.field_7790);
               }

               this.wait = 5;
               return;
            }
         }

         this.readyToBuy = false;
         if (this.apiSeller != null) {
            this.apiSeller = null;
            mc.field_1724.method_7346();
         } else {
            DonutUtils.click(container.field_7763, 49, 1, class_1713.field_7794);
            this.wait = this.refreshDelay.get();
         }
      }
   }

   private void handleConfirm(class_1707 container) {
      class_1799 stack = container.method_7611(13).method_7677();
      if (!DonutUtils.isShulkerBox(stack) && stack.method_31574(this.item.get()) && this.matches(stack)) {
         this.startPurchase(stack, DonutUtils.extractPrice(stack));
         DonutUtils.click(container.field_7763, 15, 1, class_1713.field_7794);
         this.wait = 20;
      } else {
         mc.field_1724.method_7346();
         this.wait = 10;
      }

      this.apiSeller = null;
   }

   private boolean matches(class_1799 stack) {
      for (class_2960 id : enchantmentIds(this.enchantments.get())) {
         if (!hasEnchantment(stack, id)) {
            return false;
         }
      }

      for (class_2960 idx : enchantmentIds(this.blacklist.get())) {
         if (hasEnchantment(stack, idx)) {
            return false;
         }
      }

      double price = DonutUtils.extractPrice(stack);
      double limit = DonutUtils.parsePrice(this.maxPrice.get());
      return !(price < 0.0) && !(limit < 0.0) ? price / (double)stack.method_7947() <= limit : false;
   }

   private static List<class_2960> enchantmentIds(String text) {
      List<class_2960> ids = new ArrayList<>();

      for (String part : text.split(",")) {
         String name = part.trim().toLowerCase().replace(' ', '_');
         if (!name.isEmpty()) {
            class_2960 id = class_2960.method_12829(name.contains(":") ? name : "minecraft:" + name);
            if (id != null) {
               ids.add(id);
            }
         }
      }

      return ids;
   }

   private static boolean hasEnchantment(class_1799 stack, class_2960 id) {
      class_9304 enchants = (class_9304)stack.method_58695(class_9334.field_49633, class_9304.field_49385);
      class_9304 stored = (class_9304)stack.method_58695(class_9334.field_49643, class_9304.field_49385);
      return enchants.method_57534().stream().anyMatch(e -> e.method_40226(id)) || stored.method_57534().stream().anyMatch(e -> e.method_40226(id));
   }

   private void startPurchase(class_1799 stack, double price) {
      this.purchase = new AuctionSniper.Purchase(
         stack.method_7909(), stack.method_7964().getString(), stack.method_7947(), price, InvUtil.count(stack.method_7909()), 100
      );
   }

   private void trackPurchase() {
      if (this.purchase != null) {
         if (InvUtil.count(this.purchase.item) > this.purchase.countBefore) {
            this.sendWebhook(this.purchase);
            if (this.autoResell.get()) {
               this.resellQueued = true;
               this.resellTimeout = 100;
            }

            this.purchase = null;
         } else if (--this.purchase.timeout <= 0) {
            this.purchase = null;
         }
      }
   }

   private boolean handleResell() {
      if (--this.resellTimeout > 0 && !(mc.field_1724.field_7512 instanceof class_1707)) {
         double perItem = DonutUtils.parsePrice(this.resellPrice.get());
         if (perItem < 0.0) {
            this.error("Invalid resell price.", new Object[0]);
            this.resellQueued = false;
            return false;
         } else {
            int hotbar = InvUtil.findHotbar(this.item.get());
            if (hotbar == -1) {
               int main = InvUtil.findMain(this.item.get());
               if (main == -1) {
                  this.resellQueued = false;
                  return false;
               } else {
                  InvUtil.moveToHotbar(main, mc.field_1724.method_31548().method_67532());
                  return true;
               }
            } else {
               InvUtil.select(hotbar);
               int count = mc.field_1724.method_31548().method_5438(hotbar).method_7947();
               DonutUtils.command("ah sell " + (long)(perItem * (double)count));
               this.resellQueued = false;
               this.wait = 20;
               return true;
            }
         }
      } else {
         if (this.resellTimeout <= 0) {
            this.resellQueued = false;
         }

         return false;
      }
   }

   private void sendWebhook(AuctionSniper.Purchase p) {
      String url = this.webhook.get().trim();
      if (!url.isEmpty()) {
         JsonObject embed = new JsonObject();
         embed.addProperty("title", "Item Sniped");
         embed.addProperty("color", 16766720);
         JsonArray fields = new JsonArray();
         fields.add(field("Item", p.name + (p.count > 1 ? " x" + p.count : "")));
         fields.add(field("Price", p.price > 0.0 ? DonutUtils.formatPrice(p.price) : "Unknown"));
         embed.add("fields", fields);
         JsonArray embeds = new JsonArray();
         embeds.add(embed);
         JsonObject payload = new JsonObject();
         payload.addProperty("username", "Auction Sniper");
         payload.add("embeds", embeds);

         try {
            this.http
               .sendAsync(
                  HttpRequest.newBuilder(URI.create(url))
                     .timeout(Duration.ofSeconds(10L))
                     .header("Content-Type", "application/json")
                     .POST(BodyPublishers.ofString(payload.toString()))
                     .build(),
                  BodyHandlers.discarding()
               );
         } catch (IllegalArgumentException var8) {
            this.warning("Invalid webhook URL.", new Object[0]);
         }
      }
   }

   private static JsonObject field(String name, String value) {
      JsonObject field = new JsonObject();
      field.addProperty("name", name);
      field.addProperty("value", value);
      field.addProperty("inline", true);
      return field;
   }

   public static enum Mode {
      Manual,
      Api;
   }

   private static final class Purchase {
      final class_1792 item;
      final String name;
      final int count;
      final double price;
      final int countBefore;
      int timeout;

      Purchase(class_1792 item, String name, int count, double price, int countBefore, int timeout) {
         this.item = item;
         this.name = name;
         this.count = count;
         this.price = price;
         this.countBefore = countBefore;
         this.timeout = timeout;
      }
   }
}
