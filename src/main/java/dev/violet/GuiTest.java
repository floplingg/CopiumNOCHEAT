package dev.violet;

import dev.violet.gui.MenuKit;
import dev.violet.gui.PanelScreen;
import dev.violet.gui.VanishAlert;
import dev.violet.module.Module;
import dev.violet.modules.AntiCheat;
import dev.violet.modules.AutoRespawn;
import dev.violet.modules.ClickGui;
import dev.violet.modules.CsPickaxe;
import dev.violet.modules.Freecam;
import dev.violet.modules.Freelook;
import dev.violet.modules.Hud;
import dev.violet.modules.InventoryHud;
import dev.violet.modules.JumpCircles;
import dev.violet.modules.MediaHider;
import dev.violet.modules.RegionMap;
import dev.violet.modules.Sky;
import dev.violet.modules.SusChunk;
import dev.violet.modules.Zoom;
import dev.violet.setting.Setting;
import dev.violet.setting.lite.BooleanSetting;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.lang.reflect.Field;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_1267;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1928;
import net.minecraft.class_1934;
import net.minecraft.class_1940;
import net.minecraft.class_310;
import net.minecraft.class_318;
import net.minecraft.class_4267;
import net.minecraft.class_490;
import net.minecraft.class_500;
import net.minecraft.class_5285;
import net.minecraft.class_5317;
import net.minecraft.class_5498;
import net.minecraft.class_641;
import net.minecraft.class_642;
import net.minecraft.class_7701;
import net.minecraft.class_7712;
import net.minecraft.class_4267.class_504;
import net.minecraft.class_642.class_8678;

final class GuiTest {
   private static int menuTicks;
   private static int worldTicks;
   private static boolean creating;

   private GuiTest() {
   }

   static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(GuiTest::tick);
   }

   private static void tick(class_310 client) {
      if (client.field_1687 != null && client.field_1724 != null) {
         worldTicks++;
         if (worldTicks == 20) {
            setUp(client);
         }

         if (worldTicks > 20 && worldTicks < 90 && client.field_1755 != null) {
            client.method_1507(null);
         }

         if (worldTicks == 30) {
            double dx = 40.0 - client.field_1724.method_23317();
            double dz = -30.0 - client.field_1724.method_23321();
            client.field_1724.method_36456((float)Math.toDegrees(Math.atan2(-dx, dz)) + 25.0F);
            client.field_1724.method_36457(-12.0F);
         }

         if (worldTicks == 90) {
            shot(client, "hud_world.png");
         }

         if (worldTicks == 95) {
            client.method_1507(MenuKit.menu());
         }

         if (worldTicks == 128) {
            shot(client, "click_gui.png");
         }

         if (worldTicks == 129 && client.field_1755 instanceof PanelScreen panel) {
            panel.openSettings(SoberClient.MODULES.get(Freecam.class));
         }

         if (worldTicks == 140) {
            shot(client, "panel_drawer.png");
         }

         if (worldTicks == 141 && client.field_1755 instanceof PanelScreen panel) {
            panel.openSettingsTab();
         }

         if (worldTicks == 152) {
            shot(client, "panel_settings.png");
         }

         if (worldTicks == 153) {
            ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
            gui.panelScale.set(125);
            gui.panelWidth.set(92);
            gui.panelHeight.set(88);
            if (client.field_1755 instanceof PanelScreen panel) {
               panel.openSettings(SoberClient.MODULES.get(Freecam.class));
            }
         }

         if (worldTicks == 158) {
            shot(client, "panel_big.png");
         }

         if (worldTicks == 160) {
            ClickGui gui = SoberClient.MODULES.get(ClickGui.class);
            gui.panelScale.reset();
            gui.panelWidth.reset();
            gui.panelHeight.reset();
            client.method_1507(null);
            VanishAlert.watching(UUID.randomUUID(), "TestStaff", 0);
         }

         if (worldTicks == 175) {
            shot(client, "vanish_alert.png");
         }

         if (worldTicks == 200) {
            enable(Freelook.class);
         }

         if (worldTicks == 204) {
            shot(client, "freelook_enter.png");
         }

         if (worldTicks == 216) {
            shot(client, "freelook_on.png");
         }

         if (worldTicks == 240) {
            SoberClient.MODULES.get(Freelook.class).setEnabled(false);
         }

         if (worldTicks == 243) {
            shot(client, "freelook_exit.png");
         }

         if (worldTicks >= 250 && worldTicks <= 262) {
            susChunks(client);
         }

         if (worldTicks == 262) {
            shot(client, "sus_chunks.png");
         }

         if (worldTicks == 264) {
            enable(Freecam.class);
         }

         if (worldTicks == 270) {
            shot(client, "freecam_hint.png");
         }

         if (worldTicks == 271) {
            SoberClient.MODULES.get(Freecam.class).setEnabled(false);
         }

         if (worldTicks == 272) {
            SoberClient.MODULES.get(SusChunk.class).setEnabled(false);
            int slot = client.field_1724.method_31548().method_67532();
            client.field_1761.method_2909(new class_1799(class_1802.field_8377), 36 + slot);
            client.field_1724.method_31548().method_5447(slot, new class_1799(class_1802.field_8377));
            client.field_1724.method_36457(10.0F);
            enable(CsPickaxe.class);
         }

         if (worldTicks == 296) {
            shot(client, "cs_pickaxe.png");
         }

         if (worldTicks == 298) {
            SoberClient.MODULES.get(CsPickaxe.class).inspectNow();
         }

         if (worldTicks == 306) {
            shot(client, "cs_inspect_1.png");
         }

         if (worldTicks == 316) {
            shot(client, "cs_inspect_2.png");
         }

         if (worldTicks == 330) {
            shot(client, "cs_inspect_3.png");
         }

         if (worldTicks == 338) {
            client.field_1724.method_6104(class_1268.field_5808);
         }

         if (worldTicks == 340) {
            shot(client, "cs_hit.png");
         }

         if (worldTicks == 345) {
            client.field_1690.method_31043(class_5498.field_26666);
         }

         if (worldTicks == 352) {
            shot(client, "cs_third.png");
         }

         if (worldTicks == 353) {
            client.field_1690.method_31043(class_5498.field_26664);
         }

         if (worldTicks == 356 || worldTicks == 380) {
            client.field_1724.method_31548().method_61496(1);
         }

         if (worldTicks == 360 || worldTicks == 384) {
            client.field_1724.method_31548().method_61496(0);
         }

         if (worldTicks == 361) {
            shot(client, "deploy_a1.png");
         }

         if (worldTicks == 363) {
            shot(client, "deploy_a2.png");
         }

         if (worldTicks == 366) {
            shot(client, "deploy_a3.png");
         }

         if (worldTicks == 370) {
            shot(client, "deploy_a4.png");
         }

         if (worldTicks == 386) {
            shot(client, "deploy_b1.png");
         }

         if (worldTicks == 389) {
            shot(client, "deploy_b2.png");
         }

         if (worldTicks == 392) {
            client.field_1724.method_36457(72.0F);
         }

         if (worldTicks == 394) {
            client.field_1724.method_6043();
         }

         if (worldTicks == 400) {
            shot(client, "jump_circle.png");
         }

         if (worldTicks == 402) {
            client.field_1724.method_36457(-5.0F);
            zoom(true);
         }

         if (worldTicks == 418) {
            shot(client, "zoom.png");
         }

         if (worldTicks == 419) {
            zoom(false);
         }

         if (worldTicks == 424) {
            enable(AutoRespawn.class);
            client.field_1724.field_3944.method_45730("gamemode survival");
         }

         if (worldTicks == 430) {
            client.field_1724.field_3944.method_45730("kill");
         }

         if (worldTicks == 434) {
            SoberClient.LOG
               .info(
                  "GUI test: dead={} screen={}",
                  client.field_1724.method_29504(),
                  client.field_1755 == null ? "none" : client.field_1755.getClass().getSimpleName()
               );
         }

         if (worldTicks == 460) {
            SoberClient.LOG
               .info(
                  "GUI test: after Auto Respawn alive={} health={} screen={}",
                  new Object[]{
                     client.field_1724.method_5805(),
                     client.field_1724.method_6032(),
                     client.field_1755 == null ? "none" : client.field_1755.getClass().getSimpleName()
                  }
               );
            shot(client, "auto_respawn.png");
         }

         if (worldTicks == 462) {
            enable(MediaHider.class);
            client.method_1507(new class_490(client.field_1724));
         }

         if (worldTicks == 470) {
            shot(client, "media_hider.png");
         }

         if (worldTicks == 474) {
            client.method_1592();
         }
      } else {
         if (!creating && client.field_1755 != null && client.method_18506() == null) {
            menuTicks++;
            if (menuTicks == 80) {
               shot(client, "start_menu.png");
            }

            if (menuTicks == 90) {
               openMultiplayer(client);
            }

            if (menuTicks == 150 && client.field_1755 instanceof class_500 mp) {
               select(mp);
            }

            if (menuTicks == 175) {
               shot(client, "multiplayer.png");
            }

            if (menuTicks == 190) {
               creating = true;
               createWorld(client);
            }
         }
      }
   }

   private static void zoom(boolean on) {
      Zoom zoom = SoberClient.MODULES.get(Zoom.class);

      try {
         Field mode = Zoom.class.getDeclaredField("mode");
         mode.setAccessible(true);
         ((Setting)mode.get(zoom)).set(on ? Zoom.Mode.Toggle : Zoom.Mode.Hold);
         Field toggled = Zoom.class.getDeclaredField("toggled");
         toggled.setAccessible(true);
         toggled.setBoolean(zoom, on);
      } catch (ReflectiveOperationException var4) {
         SoberClient.LOG.error("GUI test: could not zoom", var4);
      }
   }

   private static void susChunks(class_310 client) {
      SusChunk sus = SoberClient.MODULES.get(SusChunk.class);
      if (sus != null) {
         if (!sus.isActive()) {
            sus.setEnabled(true);
         }

         client.field_1724.method_36457(35.0F);

         try {
            Field follow = SusChunk.class.getDeclaredField("followHeight");
            follow.setAccessible(true);
            ((BooleanSetting)follow.get(sus)).set(true);
            LongOpenHashSet chunks = new LongOpenHashSet();
            class_1923 center = client.field_1724.method_31476();

            for (int x = -3; x <= 3; x++) {
               for (int z = -3; z <= 3; z++) {
                  if ((x + z & 1) == 0 || Math.abs(x) + Math.abs(z) < 3) {
                     chunks.add(class_1923.method_8331(center.field_9181 + x, center.field_9180 + z));
                  }
               }
            }

            Field primary = SusChunk.class.getDeclaredField("primary");
            primary.setAccessible(true);
            primary.set(sus, chunks);
         } catch (ReflectiveOperationException var7) {
            SoberClient.LOG.error("GUI test: could not fill SusChunks", var7);
         }
      }
   }

   private static void openMultiplayer(class_310 client) {
      client.field_1690.field_21840 = true;
      class_641 list = new class_641(client);
      list.method_2981();
      if (list.method_2984() == 0) {
         String[][] servers = new String[][]{
            {"DonutSMP", "127.0.0.1:25601"}, {"Hypixel", "127.0.0.1:25602"}, {"Mein SMP", "127.0.0.1:25603"}, {"Test Server", "127.0.0.1:25604"}
         };

         for (String[] s : servers) {
            list.method_2988(new class_642(s[0], s[1], class_8678.field_45611), false);
         }

         list.method_2987();
      }

      client.method_1507(new class_500(client.field_1755));
   }

   private static void select(class_500 screen) {
      try {
         Field field = class_500.class.getDeclaredField("serverListWidget");
         field.setAccessible(true);
         class_4267 widget = (class_4267)field.get(screen);
         if (!widget.method_25396().isEmpty()) {
            widget.method_20122((class_504)widget.method_25396().get(0));
         }
      } catch (ReflectiveOperationException var3) {
         SoberClient.LOG.error("GUI test: could not select a server", var3);
      }
   }

   private static void createWorld(class_310 client) {
      String name = "VioletTest" + System.currentTimeMillis() % 100000L;
      class_1940 info = new class_1940(
         name, class_1934.field_9220, false, class_1267.field_5801, true, new class_1928(class_7701.field_40183), class_7712.field_40260
      );
      client.method_41735().method_41895(name, info, class_5285.method_64223(), class_5317::method_64225, client.field_1755);
   }

   private static void setUp(class_310 client) {
      Hud hud = SoberClient.MODULES.get(Hud.class);
      hud.setEnabled(true);
      hud.keystrokes.set(true);
      hud.keysBackground.set(true);
      enable(InventoryHud.class);
      enable(JumpCircles.class);
      enable(RegionMap.class);
      AntiCheat ac = SoberClient.MODULES.get(AntiCheat.class);

      try {
         Field feed = AntiCheat.class.getDeclaredField("feedUrl");
         feed.setAccessible(true);
         ((Setting)feed.get(ac)).set(System.getProperty("violet.guitest.feed", "http://127.0.0.1:8765/api.json"));
      } catch (ReflectiveOperationException var7) {
         SoberClient.LOG.error("GUI test: could not set the feed", var7);
      }

      ac.setEnabled(true);
      String skyMode = System.getProperty("violet.guitest.sky", "Sober");

      try {
         Sky sky = SoberClient.MODULES.get(Sky.class);
         Field mode = Sky.class.getDeclaredField("skyMode");
         mode.setAccessible(true);
         ((Setting)mode.get(sky)).set(Sky.SkyMode.valueOf(skyMode));
         sky.setEnabled(true);
      } catch (IllegalArgumentException | ReflectiveOperationException var6) {
         SoberClient.LOG.error("GUI test: could not set the sky", var6);
      }
   }

   private static void enable(Class<? extends Module> type) {
      Module module = SoberClient.MODULES.get(type);
      if (module != null) {
         module.setEnabled(true);
      }
   }

   private static void shot(class_310 client, String name) {
      class_318.method_22690(client.field_1697, name, client.method_1522(), 1, message -> SoberClient.LOG.info("GUI test: {}", message.getString()));
   }
}
