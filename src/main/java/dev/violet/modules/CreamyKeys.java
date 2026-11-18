package dev.violet.modules;

import dev.violet.gui.KeySynth;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_304;
import net.minecraft.class_3675;

public class CreamyKeys extends Module {
   private final Setting<CreamyKeys.Sound> sound = this.add(
      new EnumSetting<>(
         "Key Sound", "Aula F75, FB67, 60he v2 and Frosted 65 play the real keyboard sounds cut from your recordings, unchanged.", CreamyKeys.Sound.AulaF75
      )
   );
   private final Setting<Boolean> inGame = this.add(new BoolSetting("In Game", "Also clicks while playing, not only in chat and menus.", true));
   private final Setting<Boolean> movement = this.add(
         new BoolSetting(
            "Movement Keys",
            "Walking, jumping, sneaking and sprinting keys also click while playing. Turn off for clicks only while typing and on other keys.",
            true
         )
      )
      .visible(this.inGame::get);
   private final Setting<Boolean> release = this.add(
      new BoolSetting("Release Sound", "Plays a second, softer click when a key is let go, like a real keyboard. Doubles the amount of clicks.", false)
   );
   private final Setting<Integer> cooldown = this.add(
      new IntSetting("Cooldown", "Minimum time between two clicks in milliseconds, so many keys at once do not rattle.", 70, 0, 300)
   );
   private final Setting<Boolean> stereo = this.add(
      new BoolSetting("Stereo", "Keys on the left of the keyboard sound from the left, keys on the right from the right.", true)
   );
   private final Setting<Integer> volume = this.add(new IntSetting("Volume", "Loudness of the key clicks in percent.", 100, 10, 250));
   private final Setting<Boolean> mouse = this.add(new BoolSetting("Mouse", "Mouse buttons play the mouse click from your clip when pressed.", true));
   private final Set<Integer> down = new HashSet<>();
   private final Set<Integer> buttons = new HashSet<>();
   private long lastSound;
   private long lastMouse;

   public CreamyKeys() {
      super("Creamy Keys", "Every key press sounds like a real creamy mechanical keyboard.", Category.CLIENT);
   }

   @Override
   public void onDisable() {
      this.down.clear();
      this.buttons.clear();
   }

   public void onMouse(int button, int action) {
      if (action == 0) {
         this.buttons.remove(button);
      } else if (action == 1 && this.buttons.add(button)) {
         if (this.mouse.get() && (mc.field_1755 != null || this.inGame.get())) {
            long now = System.currentTimeMillis();
            if (now - this.lastMouse >= 20L) {
               this.lastMouse = now;
               KeySynth.playMouse((float)this.volume.get().intValue() / 100.0F);
            }
         }
      }
   }

   private boolean movementKey(int key) {
      for (class_304 binding : new class_304[]{
         mc.field_1690.field_1894,
         mc.field_1690.field_1881,
         mc.field_1690.field_1913,
         mc.field_1690.field_1849,
         mc.field_1690.field_1903,
         mc.field_1690.field_1832,
         mc.field_1690.field_1867
      }) {
         if (class_3675.method_15981(binding.method_1428()).method_1444() == key) {
            return true;
         }
      }

      return false;
   }

   public void onKey(int key, int action) {
      if (key != -1 && action != 2) {
         if (action == 0) {
            if (this.down.remove(key) && this.release.get()) {
               this.play(key, true);
            }
         } else if (this.down.add(key)) {
            boolean playing = mc.field_1755 == null;
            if (!playing || this.inGame.get() && (this.movement.get() || !this.movementKey(key))) {
               this.play(key, false);
            }
         }
      }
   }

   private void play(int key, boolean up) {
      long now = System.currentTimeMillis();
      if (now - this.lastSound >= (long)this.cooldown.get().intValue()) {
         this.lastSound = now;
         float level = (float)this.volume.get().intValue() / 100.0F;
         KeySynth.play(this.sound.get().profile, key, up, level, this.stereo.get());
      }
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   public static enum Sound {
      AulaF75("Aula F75", KeySynth.Profile.Recorded),
      FB67("FB67", KeySynth.Profile.Fb67),
      He60v2("60he v2", KeySynth.Profile.He60v2),
      Frost65("Frosted 65", KeySynth.Profile.Frost65);

      private final String title;
      private final KeySynth.Profile profile;

      private Sound(String title, KeySynth.Profile profile) {
         this.title = title;
         this.profile = profile;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
