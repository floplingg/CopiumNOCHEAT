package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.gui.HomeScreen;
import dev.violet.gui.MenuScreen;
import dev.violet.gui.MultiplayerGridScreen;
import dev.violet.gui.ScreenStyle;
import dev.violet.gui.VanillaPlusScreen;
import dev.violet.gui.VioletScreen;
import dev.violet.modules.MainMenu;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_310.class})
public class MinecraftClientMixin {
   @Inject(
      method = {"method_1507"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$customMenu(class_437 screen, CallbackInfo ci) {
      try {
         class_310 client = (class_310)(Object)this;
         boolean title = screen instanceof class_442 || screen == null && client.field_1687 == null;
         if (!title || !MenuScreen.enabled()) {
            return;
         }

         MainMenu settings = SoberClient.MODULES.get(MainMenu.class);
         MainMenu.Style style = settings == null ? MainMenu.Style.Violet : settings.style.get();
         class_437 menu = (class_437)(style == MainMenu.Style.Violet
            ? new VioletScreen()
            : (style == MainMenu.Style.VanillaPlus ? new VanillaPlusScreen() : (style == MainMenu.Style.Home ? new HomeScreen() : new MenuScreen())));
         ci.cancel();
         client.method_1507(menu);
      } catch (RuntimeException var8) {
         SoberClient.LOG.error("Custom menu failed", var8);
      }
   }

   @Inject(
      method = {"method_1507"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$multiplayer(class_437 screen, CallbackInfo ci) {
      if (screen != null && screen.getClass() == class_500.class && ScreenStyle.screens() && ScreenStyle.home()) {
         try {
            class_437 grid = MultiplayerGridScreen.replace((class_500)screen);
            ci.cancel();
            ((class_310)(Object)this).method_1507(grid);
         } catch (RuntimeException var4) {
            SoberClient.LOG.error("Custom multiplayer screen failed", var4);
         }
      }
   }
}
