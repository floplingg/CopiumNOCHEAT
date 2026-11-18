package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.CapeTexture;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_10055;
import net.minecraft.class_11890;
import net.minecraft.class_2960;
import net.minecraft.class_8685;
import net.minecraft.class_12079.class_10726;
import net.minecraft.class_12079.class_12081;

public class Capes extends Module {
   private static final class_12081 ELYTRA = new class_10726(class_2960.method_60656("entity/equipment/wings/elytra"));
   public final Setting<Capes.Style> style = this.add(
      new EnumSetting<>(
         "Style",
         "Violet is deep violet with the paw logo. Violet Shine has a light that sweeps over it. Neon Paw draws the paw as flickering neon lines. Violet Paw shows the violet paw with a soft pulse. Badge shows the round Copium Client badge turning slowly. VC Logo shows the VC monogram. Lightning flashes violet bolts behind the paw. Night Sky has twinkling stars and a glowing paw. Galaxy has a slowly drifting nebula. Holo shimmers in rainbow colors.",
         Capes.Style.VioletShine
      )
   );
   public final Setting<Boolean> effects = this.add(new BoolSetting("Effects", "Plays the moving effect of the style. Off keeps the cape still.", true));
   public final Setting<Boolean> alwaysShow = this.add(
      new BoolSetting("Always Show", "Shows the cape even when capes are turned off in your skin settings.", true)
   );

   public Capes() {
      super("Capes", "A Copium Client cape with the logo in different styles and effects. Only you can see it.", Category.VISUALS);
   }

   public static void apply(class_11890 entity, class_10055 state) {
      if (entity == mc.field_1724) {
         Capes module = SoberClient.MODULES.get(Capes.class);
         if (module != null && module.isActive()) {
            class_12081 cape = CapeTexture.asset(module.style.get(), module.effects.get());
            class_8685 skin = state.field_53520;
            state.field_53520 = new class_8685(skin.comp_1626(), cape, skin.comp_1628() != null ? skin.comp_1628() : ELYTRA, skin.comp_1629(), skin.comp_1630());
            if (module.alwaysShow.get()) {
               state.field_53532 = true;
            }
         }
      }
   }

   public static enum Style {
      Violet("Violet"),
      VioletShine("Violet Shine"),
      NeonPaw("Neon Paw"),
      VioletPaw("Violet Paw"),
      Badge("Badge"),
      VcLogo("VC Logo"),
      Lightning("Lightning"),
      NightSky("Night Sky"),
      Galaxy("Galaxy"),
      Holo("Holo");

      private final String label;

      private Style(String label) {
         this.label = label;
      }

      @Override
      public String toString() {
         return this.label;
      }
   }
}
