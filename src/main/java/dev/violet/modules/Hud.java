package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class Hud extends Module {
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
   public final Setting<Boolean> keystrokes;
   public final Setting<Boolean> keysBackground;
   public final Setting<Boolean> togisKeyboard;
   public final Setting<Integer> notificationTime;
   public final Setting<Boolean> notificationSound;
   public final Setting<Hud.SoundType> soundType;
   public final Setting<Integer> notificationVolume;

   public Hud() {
      super("HUD", "Watermark, stats, module list, keystrokes and notifications.", Category.CLIENT);
      this.group("Stats");
      this.coordinates = this.add(new BoolSetting("XYZ", "Shows your block coordinates.", true));
      this.fps = this.add(new BoolSetting("FPS", "Shows the current frame rate.", true));
      this.bps = this.add(new BoolSetting("BPS", "Shows horizontal blocks per second.", true));
      this.tps = this.add(new BoolSetting("TPS", "Shows the server tick rate when available.", true));
      this.keystrokes = this.add(new BoolSetting("Keystrokes", "Shows animated movement and mouse keys.", false));
      this.keysBackground = this.add(new BoolSetting("Keys Background", "Draws the dark card with the KEYS title behind the keystrokes.", false))
         .visible(this.keystrokes::get);
      this.togisKeyboard = this.add(
         new BoolSetting("Togis Keyboard", "Shows the Togis keyboard picture on your HUD. Open the chat to move it, scroll on it to resize it.", false)
      );
      this.group("Notifications");
      this.notificationTime = this.add(new IntSetting("Display Time", "How long notifications stay, in tenths of a second.", 45, 15, 150));
      this.notificationSound = this.add(new BoolSetting("Sound", "Plays a sound for every notification.", true));
      this.soundType = this.add(new EnumSetting<>("Sound Type", "Which sound notifications make.", Hud.SoundType.Pop)).visible(this.notificationSound::get);
      this.notificationVolume = this.add(new IntSetting("Volume", "Notification sound volume in percent.", 60, 5, 100)).visible(this.notificationSound::get);
      this.setEnabledSilently(true);
   }

   public boolean migrate() {
      return false;
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
