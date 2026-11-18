package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.AcStatusCard;
import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.AcOffFeed;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1109;
import net.minecraft.class_1937;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class AntiCheat extends Module {
   private final Setting<String> feedUrl = this.add(new StringSetting("Feed URL", "Address of the anticheat feed.", "https://anticheat.imjules.be/api.json"));
   private final Setting<Integer> feedInterval = this.add(new IntSetting("Check Every", "Seconds between two checks of the feed.", 10, 3, 120));
   private final Setting<AntiCheat.MapVersion> regionMap = this.add(
      new EnumSetting<>("Region Map", "Which DonutSMP region map the G# numbers follow.", AntiCheat.MapVersion.New)
   );
   private final Setting<Boolean> testAlerts = this.add(new BoolSetting("Test Alerts", "Also shows test entries of the feed.", false));
   private final Setting<Boolean> statusCard = this.add(
      new BoolSetting(
         "Status Card", "HUD card that shows if the anticheat is on or off and where it is off. Open the chat to drag it, scroll on it to resize it.", true
      )
   );
   private final Setting<Boolean> beams = this.add(new BoolSetting("Beams", "A beam into the sky at every place where the anticheat is off.", true));
   private final Setting<Boolean> alertSound = this.add(new BoolSetting("Alert Sound", "Plays a sound when the anticheat goes off somewhere.", true));
   private final Setting<Boolean> chat = this.add(new BoolSetting("Chat Message", "Also writes every alert into the chat.", false));
   private final AcOffFeed feed = new AcOffFeed();
   private final Set<String> knownSpots = new HashSet<>();
   private boolean feedLoaded;

   public AntiCheat() {
      super("Anti Cheat", "Alerts you when and where the DonutSMP anticheat goes off, from the anticheat feed.", Category.CLIENT);
   }

   @Override
   public void onDisable() {
      this.feed.reset();
      this.knownSpots.clear();
      this.feedLoaded = false;
   }

   @Override
   public void onTick() {
      if (mc.field_1724 != null) {
         this.feed.poll(this.feedUrl.get(), this.feedInterval.get(), this::onFeed);
      }
   }

   private List<AcOffFeed.Spot> visibleSpots() {
      List<AcOffFeed.Spot> spots = new ArrayList<>();

      for (AcOffFeed.Spot spot : this.feed.snapshot().spots()) {
         if (!spot.test() || this.testAlerts.get()) {
            spots.add(spot);
         }
      }

      return spots;
   }

   private void onFeed(AcOffFeed.Snapshot snapshot) {
      mc.execute(
         () -> {
            if (this.isActive() && snapshot.error() == null) {
               List<AcOffFeed.Spot> spots = this.visibleSpots();
               Set<String> now = new HashSet<>();
               List<AcOffFeed.Spot> fresh = new ArrayList<>();

               for (AcOffFeed.Spot spot : spots) {
                  now.add(spot.key());
                  if (!this.knownSpots.contains(spot.key())) {
                     fresh.add(spot);
                  }
               }

               boolean wasOff = !this.knownSpots.isEmpty();
               this.knownSpots.clear();
               this.knownSpots.addAll(now);
               boolean first = !this.feedLoaded;
               this.feedLoaded = true;
               if (!fresh.isEmpty()) {
                  if (first && fresh.size() > 1) {
                     this.announce("Anticheat OFF", "The anticheat is off at " + fresh.size() + " places right now. Check the status card.");
                  } else {
                     for (AcOffFeed.Spot spotx : fresh) {
                        this.announce(
                           spotx.test() ? "Anticheat OFF (test)" : "Anticheat OFF", this.place(spotx) + " · " + spotx.coords() + this.distance(spotx, " · ")
                        );
                     }
                  }

                  if (this.alertSound.get() && mc.method_1483() != null) {
                     mc.method_1483().method_4873(class_1109.method_4757((class_3414)class_3417.field_14622.comp_349(), 1.5F, 0.8F));
                  }
               } else if (wasOff && spots.isEmpty()) {
                  this.announce("Anticheat ON", "The anticheat is back on everywhere.");
               }
            }
         }
      );
   }

   private void announce(String title, String message) {
      Notifications.push(title, message, title.startsWith("Anticheat OFF") ? Notifications.Type.SUCCESS : Notifications.Type.INFO);
      if (this.chat.get()) {
         this.info(title + ": " + message, new Object[0]);
      }
   }

   private String place(AcOffFeed.Spot spot) {
      int number = this.regionMap.get() == AntiCheat.MapVersion.New ? spot.goliath() : spot.oldGoliath();
      return spot.region() + (number >= 0 ? " · G#" + number : "");
   }

   private String distance(AcOffFeed.Spot spot, String prefix) {
      if (mc.field_1724 != null && (spot.x() != 0.0 || spot.z() != 0.0)) {
         double dx = mc.field_1724.method_23317() - spot.x();
         double dz = mc.field_1724.method_23321() - spot.z();
         return prefix + Math.round(Math.sqrt(dx * dx + dz * dz)) + "m";
      } else {
         return "";
      }
   }

   @Override
   public void onRender(Render3D renderer) {
      if (this.beams.get() && mc.field_1687 != null && mc.field_1687.method_27983() == class_1937.field_25179) {
         for (AcOffFeed.Spot spot : this.visibleSpots()) {
            if (spot.x() != 0.0 || spot.z() != 0.0) {
               renderer.beam(spot.x(), spot.z(), spot.y() - 2.0, 320.0, 0.6, new Color(255, 92, 138, 200), 1.0);
            }
         }
      }
   }

   public static AcStatusCard.Model card() {
      AntiCheat module = SoberClient.MODULES.get(AntiCheat.class);
      if (module != null && module.isActive() && module.statusCard.get()) {
         AcOffFeed.Snapshot snapshot = module.feed.snapshot();
         List<AcOffFeed.Spot> spots = module.visibleSpots();
         List<AcStatusCard.Row> rows = new ArrayList<>();

         for (int i = 0; i < spots.size() && i < 4; i++) {
            rows.add(new AcStatusCard.Row(module.place(spots.get(i)), module.distance(spots.get(i), "")));
         }

         if (spots.size() > 4) {
            rows.add(new AcStatusCard.Row("+" + (spots.size() - 4) + " more", ""));
         }

         if (!snapshot.loaded()) {
            return new AcStatusCard.Model(AcStatusCard.State.UNKNOWN, "Checking...", rows);
         } else if (!spots.isEmpty()) {
            return new AcStatusCard.Model(AcStatusCard.State.OFF, "AC OFF · " + spots.size() + (spots.size() == 1 ? " place" : " places"), rows);
         } else {
            return snapshot.error() != null
               ? new AcStatusCard.Model(AcStatusCard.State.UNKNOWN, snapshot.error(), rows)
               : new AcStatusCard.Model(AcStatusCard.State.ON, "Anticheat on", rows);
         }
      } else {
         return null;
      }
   }

   @Override
   public String getInfoString() {
      int off = this.visibleSpots().size();
      return off == 0 ? null : off + " off";
   }

   public static enum MapVersion {
      New,
      Old;
   }
}
