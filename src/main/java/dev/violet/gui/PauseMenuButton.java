package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.ClickGui;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_4185;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_7919;
import net.minecraft.class_4185.class_4241;

public final class PauseMenuButton {
   private PauseMenuButton() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT.register(PauseMenuButton::addButton);
   }

   private static void addButton(class_310 client, class_437 screen, int width, int height) {
      if (screen instanceof class_433 menu && menu.method_53558()) {
         try {
            List<class_339> buttons = Screens.getButtons(screen);

            for (class_339 widget : buttons) {
               if (widget.method_25369().method_10851() instanceof class_2588 text && text.method_11022().equals("menu.returnToGame")) {
                  class_4185 button = new PauseMenuButton.LogoButton(
                     widget.method_46426() + widget.method_25368() + 4, widget.method_46427(), 20, widget.method_25364(), pressed -> open(client)
                  );
                  button.method_47400(class_7919.method_47407(class_2561.method_43470("SoberClient Menu")));
                  buttons.add(button);
                  return;
               }
            }
         } catch (RuntimeException var10) {
            SoberClient.LOG.warn("Could not add the Violet button to the pause menu", var10);
         }

         return;
      }
   }

   private static void open(class_310 client) {
      ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
      if (gui != null) {
         client.method_1507(MenuKit.menu());
         UiSounds.open();
      }
   }

   private static final class LogoButton extends class_4185 {
      LogoButton(int x, int y, int width, int height, class_4241 action) {
         super(x, y, width, height, class_2561.method_43470("SoberClient"), action, field_40754);
      }

      protected void method_75752(class_332 context, int mouseX, int mouseY, float deltaTicks) {
         this.method_75794(context);
         int size = Math.min(this.method_25368(), this.method_25364()) - 4;
         Smooth.logo(context, this.method_46426() + (this.method_25368() - size) / 2, this.method_46427() + (this.method_25364() - size) / 2, size, -1);
      }
   }
}
