package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.StaffList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class Hud extends Module {
   private static final String DONUT_STAFF = "DrDonutt, Munkerlich, archivePedro, W1zoX_, Itszdeath, GSMusie, Frwost, FluffyMaster07, showered, Pastagamer08, bautiedgar, bloodspulse, chaon, Evxn, Ryuul_, ShyAlyy, 0Gsummer, itsdefrealme, LzouZMp5, Dough4, Fallerfly, evify, Noah, zababi, Nathan, BobIsFound, CaptainMoose35, Frenk_Btw, Napooo_, u_vv";
   public final Setting<Boolean> moduleList = this.add(new BoolSetting("Module List", "Shows active modules in the top right corner.", true));
   public final Setting<Boolean> watermark = this.add(new BoolSetting("Watermark", "Shows the client name in the top left corner.", true));
   public final Setting<Boolean> infoLine = this.add(
      new BoolSetting(
         "Info Line",
         "Puts the logo, client name, FPS, ping, server and time together in one slim line. BPS and TPS join the line when they are on. Replaces the separate FPS, BPS and TPS cards.",
         true
      )
   );
   public final Setting<Boolean> linePing = this.add(new BoolSetting("Line Ping", "Shows your ping in the info line.", true)).visible(this.infoLine::get);
   public final Setting<Boolean> lineServer = this.add(new BoolSetting("Line Server", "Shows the server address in the info line.", true))
      .visible(this.infoLine::get);
   public final Setting<Boolean> lineTime = this.add(new BoolSetting("Line Time", "Shows the time in the info line.", true)).visible(this.infoLine::get);
   public final Setting<Boolean> chatEditing = this.add(
      new BoolSetting("Chat Editing", "Drag HUD cards while the chat is open, scroll on a card to resize it and right click to reset its size.", true)
   );
   public final Setting<Boolean> snapping = this.add(
      new BoolSetting("Snapping", "Cards snap to screen edges, the center and other cards while dragging.", true)
   );
   public final Setting<Boolean> coordinates;
   public final Setting<Boolean> fps;
   public final Setting<Boolean> bps;
   public final Setting<Boolean> tps;
   public final Setting<Boolean> radar;
   public final Setting<Integer> radarRange;
   public final Setting<Boolean> radarRotate;
   public final Setting<Boolean> radarPlayers;
   public final Setting<Boolean> radarChunks;
   public final Setting<Boolean> keystrokes;
   public final Setting<Boolean> keysBackground;
   public final Setting<Boolean> togisKeyboard;
   public final Setting<Boolean> staffList;
   public final Setting<String> staffNames;
   public final Setting<Integer> staffShown;
   public final Setting<Boolean> staffAlerts;
   private final Setting<Integer> staffVersion;
   public final Setting<Integer> notificationTime;
   public final Setting<Boolean> notificationSound;
   public final Setting<Hud.SoundType> soundType;
   public final Setting<Integer> notificationVolume;

   public Hud() {
      super("HUD", "Watermark, stats, radar, module list, keystrokes, staff list and notifications.", Category.CLIENT);
      this.group("Stats");
      this.coordinates = this.add(new BoolSetting("XYZ", "Shows your block coordinates.", true));
      this.fps = this.add(new BoolSetting("FPS", "Shows the current frame rate.", true));
      this.bps = this.add(new BoolSetting("BPS", "Shows horizontal blocks per second.", true));
      this.tps = this.add(new BoolSetting("TPS", "Shows the server tick rate when available.", true));
      this.radar = this.add(new BoolSetting("Player Radar", "Round radar with nearby players and sus chunks.", true));
      this.radarRange = this.add(new IntSetting("Radar Range", "How many blocks the radar covers from the middle to the edge.", 96, 16, 512))
         .visible(this.radar::get);
      this.radarRotate = this.add(new BoolSetting("Radar Rotate", "Turns the radar with your view, so up is always where you look.", true))
         .visible(this.radar::get);
      this.radarPlayers = this.add(new BoolSetting("Radar Players", "Shows other players with their head on the radar.", true)).visible(this.radar::get);
      this.radarChunks = this.add(new BoolSetting("Radar Chunks", "Shows sus chunks on the radar.", true)).visible(this.radar::get);
      this.keystrokes = this.add(new BoolSetting("Keystrokes", "Shows animated movement and mouse keys.", false));
      this.keysBackground = this.add(new BoolSetting("Keys Background", "Draws the dark card with the KEYS title behind the keystrokes.", false))
         .visible(this.keystrokes::get);
      this.togisKeyboard = this.add(
         new BoolSetting("Togis Keyboard", "Shows the Togis keyboard picture on your HUD. Open the chat to move it, scroll on it to resize it.", false)
      );
      this.staffList = this.add(new BoolSetting("Staff List", "Shows players with staff tags on the server.", true));
      this.staffNames = this.add(
            new StringSetting(
               "Staff Names",
               "Staff usernames, separated by commas. The current DonutSMP staff is already in there, you can add more.",
               "DrDonutt, Munkerlich, archivePedro, W1zoX_, Itszdeath, GSMusie, Frwost, FluffyMaster07, showered, Pastagamer08, bautiedgar, bloodspulse, chaon, Evxn, Ryuul_, ShyAlyy, 0Gsummer, itsdefrealme, LzouZMp5, Dough4, Fallerfly, evify, Noah, zababi, Nathan, BobIsFound, CaptainMoose35, Frenk_Btw, Napooo_, u_vv"
            )
         )
         .visible(this.staffList::get);
      this.staffShown = this.add(new IntSetting("Staff Shown", "How many staff members the staff list card shows at most.", 10, 1, 30))
         .visible(this.staffList::get);
      this.staffAlerts = this.add(new BoolSetting("Staff Alerts", "Shows a notification when a staff member joins or leaves the tab list.", true))
         .visible(this.staffList::get);
      this.staffVersion = this.add(new IntSetting("Staff Version", "", 0, 0, 100)).visible(() -> false);
      this.group("Notifications");
      this.notificationTime = this.add(new IntSetting("Display Time", "How long notifications stay, in tenths of a second.", 45, 15, 150));
      this.notificationSound = this.add(new BoolSetting("Sound", "Plays a sound for every notification.", true));
      this.soundType = this.add(new EnumSetting<>("Sound Type", "Which sound notifications make.", Hud.SoundType.Pop)).visible(this.notificationSound::get);
      this.notificationVolume = this.add(new IntSetting("Volume", "Notification sound volume in percent.", 60, 5, 100)).visible(this.notificationSound::get);
      this.setEnabledSilently(true);
   }

   public boolean migrate() {
      boolean changed = false;
      if (this.staffVersion.get() < 4) {
         this.staffNames
            .set(
               "DrDonutt, Munkerlich, archivePedro, W1zoX_, Itszdeath, GSMusie, Frwost, FluffyMaster07, showered, Pastagamer08, bautiedgar, bloodspulse, chaon, Evxn, Ryuul_, ShyAlyy, 0Gsummer, itsdefrealme, LzouZMp5, Dough4, Fallerfly, evify, Noah, zababi, Nathan, BobIsFound, CaptainMoose35, Frenk_Btw, Napooo_, u_vv"
            );
         this.staffVersion.set(4);
         changed = true;
      }

      if (this.staffVersion.get() < 5) {
         List<String> names = new ArrayList<>();
         Set<String> seen = new HashSet<>();

         for (String list : new String[]{
            this.staffNames.get(),
            "DrDonutt, Munkerlich, archivePedro, W1zoX_, Itszdeath, GSMusie, Frwost, FluffyMaster07, showered, Pastagamer08, bautiedgar, bloodspulse, chaon, Evxn, Ryuul_, ShyAlyy, 0Gsummer, itsdefrealme, LzouZMp5, Dough4, Fallerfly, evify, Noah, zababi, Nathan, BobIsFound, CaptainMoose35, Frenk_Btw, Napooo_, u_vv"
         }) {
            for (String part : list.split(",")) {
               String name = part.trim();
               if (!name.isEmpty() && seen.add(name.toLowerCase(Locale.ROOT))) {
                  names.add(name);
               }
            }
         }

         this.staffNames.set(String.join(", ", names));
         this.staffList.set(true);
         this.staffVersion.set(5);
         changed = true;
      }

      return changed;
   }

   @Override
   public void onTick() {
      StaffList.tick(mc, this);
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   public static enum SoundType {
      Pop(1.2F, 0.6F);

      public final float pitch;
      public final float volume;

      private SoundType(float pitch, float volume) {
         this.pitch = pitch;
         this.volume = volume;
      }

      public class_3414 event() {
         return class_3417.field_15065;
      }
   }
}
