package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.Setting;

public class ColorSync extends Module {
   private final Setting<Boolean> accent = this.add(new BoolSetting("Menu Accent Too", "Also changes the accent color of the menu and the HUD.", false));
   public final Setting<Color> color = this.add(
         new ColorSetting(
            "All Colors",
            "Move this one color wheel and every module color follows: glows, accents, glints and more. Each color keeps its own transparency.",
            new Color(176, 140, 240, 255)
         )
      )
      .onChanged(this::apply);

   public ColorSync() {
      super("Color Sync", "One color wheel for every module color at once.", Category.CLIENT);
   }

   private void apply(Color picked) {
      if (picked != null) {
         for (Module module : SoberClient.MODULES.all()) {
            if (module != this && !(module instanceof ClickGui)) {
               for (Setting<?> setting : module.getSettings()) {
                  if (setting instanceof ColorSetting) {
                     ColorSetting target = (ColorSetting)setting;
                     if (target.get() != null) {
                        target.set(new Color(picked.r(), picked.g(), picked.b(), target.get().a()));
                     }
                  }
               }
            }
         }

         if (this.accent.get()) {
            ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
            if (gui != null) {
               gui.accentMode.set(ClickGui.AccentMode.Static);
               gui.accent.set(new Color(picked.r(), picked.g(), picked.b(), 255));
            }
         }
      }
   }

   @Override
   public boolean bindable() {
      return false;
   }

   @Override
   public boolean showInHud() {
      return false;
   }
}
