package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.MenuAudio;
import dev.violet.gui.Theme;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;

public class MainMenu extends Module {
   public final Setting<MainMenu.Style> style = this.add(
      new EnumSetting<>(
         "Menu Style",
         "Sober is the black start screen with the dripping chrome Sober logo and dark buttons in the middle, with matching dark menus. Vanilla+ is the normal Minecraft title screen, only cleaner, with floating bottles, the SoberClient logo and a matching pause menu. Home is the clean start screen with quick join, your 3D skin and name and skin fields. Island is the animated beach with the cloud title. Classic is the old dark menu.",
         MainMenu.Style.Violet
      )
   );
   public final Setting<MainMenu.ServerLayout> serverLayout = this.add(
         new EnumSetting<>(
            "Server List",
            "Cards shows your servers as cards in three columns with the big Multiplayer title. List shows them as one slim column.",
            MainMenu.ServerLayout.Cards
         )
      )
      .visible(() -> this.style.get() == MainMenu.Style.Violet);
   private final Setting<Integer> menuVersion = this.add(new IntSetting("Menu Version", "", 0, 0, 100)).visible(() -> false);
   public final Setting<Boolean> intro = this.add(new BoolSetting("Intro", "Plays the SoberClient intro when the game starts.", true));
   public final Setting<Boolean> audio = this.add(
         new BoolSetting("Menu Sound", "Plays a short SoberClient chime once when the start menu opens after starting the game.", true)
      )
      .onChanged(v -> {
         if (!v) {
            MenuAudio.stop();
         }
      });
   public final Setting<Integer> audioVolume = this.add(new IntSetting("Menu Audio Volume", "Volume of the start menu sound in percent.", 70, 0, 100))
      .visible(this.audio::get);
   public final Setting<Boolean> matchSky = this.add(new BoolSetting("Match Sky", "Menu colors follow your sky.", true));
   public final Setting<Boolean> stars = this.add(new BoolSetting("Stars", "Floating stars in the menu background.", true));
   public final Setting<Boolean> customScreens = this.add(
      new BoolSetting("Custom Screens", "Restyles singleplayer, multiplayer, options and other menus.", true)
   );
   public final Setting<Boolean> customWidgets = this.add(
      new BoolSetting("Custom Buttons", "Rounded buttons, sliders, text fields and lists everywhere.", true)
   );

   public MainMenu() {
      super("Main Menu", "Custom SoberClient title screen with an intro animation.", Category.CLIENT);
      this.setEnabledSilently(true);
   }

   public boolean migrate() {
      if (this.menuVersion.get() >= 5) {
         return false;
      } else {
         this.style.set(MainMenu.Style.Violet);
         this.menuVersion.set(5);
         return true;
      }
   }

   public int color() {
      if (this.matchSky.get()) {
         int sky = SoberClient.MODULES.get(Sky.class).accentColor();
         if (sky >= 0 && sky != 16777215) {
            return Theme.neon(0xFF000000 | sky) & 16777215;
         }
      }

      return Theme.accent() & 16777215;
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   @Override
   public boolean bindable() {
      return false;
   }

   public static enum ServerLayout {
      Cards,
      List;
   }

   public static enum Style {
      Violet("Sober"),
      VanillaPlus("Vanilla+"),
      Home("Home"),
      Island("Island"),
      Classic("Classic");

      private final String title;

      private Style(String title) {
         this.title = title;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
