package dev.violet;

import com.mojang.logging.LogUtils;
import dev.violet.event.impl.PacketAppliedEvent;
import dev.violet.gui.BrandWindow;
import dev.violet.gui.HudLayout;
import dev.violet.gui.HudMetrics;
import dev.violet.gui.HudRenderer;
import dev.violet.gui.MenuAudio;
import dev.violet.gui.PauseMenuButton;
import dev.violet.gui.SkyLyrics;
import dev.violet.module.Module;
import dev.violet.module.ModuleManager;
import dev.violet.modules.AnimatedTyping;
import dev.violet.modules.Aspect;
import dev.violet.modules.BlockOverlay;
import dev.violet.modules.Capes;
import dev.violet.modules.ClickGui;
import dev.violet.modules.ColorSync;
import dev.violet.modules.Configs;
import dev.violet.modules.CreamyKeys;
import dev.violet.modules.CsPickaxe;
import dev.violet.modules.CustomAccessories;
import dev.violet.modules.CustomCrosshair;
import dev.violet.modules.CustomGlint;
import dev.violet.modules.CustomInventory;
import dev.violet.modules.CustomModels;
import dev.violet.modules.CustomSkin;
import dev.violet.modules.DiscordPresence;
import dev.violet.modules.FpsBooster;
import dev.violet.modules.Glass;
import dev.violet.modules.Hotkeys;
import dev.violet.modules.Hud;
import dev.violet.modules.InventoryHud;
import dev.violet.modules.JumpCircles;
import dev.violet.modules.MainMenu;
import dev.violet.modules.MediaHider;
import dev.violet.modules.MotionBlur;
import dev.violet.modules.NameTags;
import dev.violet.modules.RegionMap;
import dev.violet.modules.SkinChange;
import dev.violet.modules.Sky;
import dev.violet.modules.SpotifyPlayer;
import dev.violet.modules.StepTiles;
import dev.violet.modules.SwingAnimations;
import dev.violet.modules.WeatherNotifier;
import dev.violet.modules.Zoom;
import dev.violet.render.AccessoryFeature;
import dev.violet.render.Render3D;
import dev.violet.sky.AuraRenderer;
import dev.violet.sky.GlintManager;
import dev.violet.util.CompletionChannel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Load;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.BeforeBlockOutline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.EndMain;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1007;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.slf4j.Logger;

public class SoberClient implements ClientModInitializer {
   public static final Logger LOG = LogUtils.getLogger();
   public static final ModuleManager MODULES = new ModuleManager();
   public static final Config CONFIG = new Config(MODULES);
   private static Render3D renderer;

   private static void importLegacyConfig() {
      Path dir = FabricLoader.getInstance().getConfigDir();
      if (!Files.exists(dir.resolve("copium.json"))) {
         String old = Files.exists(dir.resolve("violetclient.json")) ? "violetclient" : "yepstein";
         if (!old.equals("yepstein") || Files.exists(dir.resolve("yepstein-client.json"))) {
            String main = old.equals("violetclient") ? "violetclient.json" : "yepstein-client.json";
            String areas = old.equals("violetclient") ? "violetclient-areas.json" : "yepstein-client-areas.json";
            String[][] files = new String[][]{
               {main, "copium.json"},
               {old + "-configs.json", "copium-configs.json"},
               {old + "-dropdown.json", "copium-dropdown.json"},
               {old + "-hud-layout.json", "copium-hud-layout.json"},
               {areas, "copium-areas.json"}
            };

            try {
               for (String[] pair : files) {
                  Path from = dir.resolve(pair[0]);
                  Path to = dir.resolve(pair[1]);
                  if (Files.exists(from) && !Files.exists(to)) {
                     Files.copy(from, to);
                  }
               }

               Path oldDir = dir.resolve("yepstein");
               Path newDir = dir.resolve("violet");
               if (Files.isDirectory(oldDir) && !Files.exists(newDir)) {
                  try (Stream<Path> paths = Files.walk(oldDir)) {
                     for (Path path : (Iterable<Path>)paths::iterator) {
                        Path target = newDir.resolve(oldDir.relativize(path).toString());
                        if (Files.isDirectory(path)) {
                           Files.createDirectories(target);
                        } else {
                           Files.copy(path, target);
                        }
                     }
                  }
               }

               LOG.info("Imported the settings of the previous client install");
            } catch (IOException var13) {
               LOG.warn("Could not import the old settings", var13);
            }
         }
      }
   }

   public void onInitializeClient() {
      importLegacyConfig();
      Module.bindClient();
      MODULES.add(new CustomCrosshair());
      MODULES.add(new MotionBlur());
      MODULES.add(new Sky());
      MODULES.add(new Capes());
      MODULES.add(new Glass());
      MODULES.add(new Zoom());
      MODULES.add(new WeatherNotifier());
      MODULES.add(new SwingAnimations());
      MODULES.add(new ClickGui());
      MODULES.add(new ColorSync());
      MODULES.add(new Hud());
      MODULES.add(new InventoryHud());
      MODULES.add(new MainMenu());
      MODULES.add(new CreamyKeys());
      MODULES.add(new Aspect());
      MODULES.add(new AnimatedTyping());
      MODULES.add(new MediaHider());
      MODULES.add(new SpotifyPlayer());
      MODULES.add(new FpsBooster());
      MODULES.add(new RegionMap());
      MODULES.add(new Hotkeys());
      MODULES.add(new CustomGlint());
      MODULES.add(new BlockOverlay());
      MODULES.add(new CustomInventory());
      MODULES.add(new CustomModels());
      MODULES.add(new CsPickaxe());
      MODULES.add(new CustomAccessories());
      MODULES.add(new CustomSkin());
      MODULES.add(new SkinChange());
      MODULES.add(new NameTags());
      MODULES.add(new StepTiles());
      MODULES.add(new JumpCircles());
      MODULES.add(new Configs());
      LivingEntityFeatureRendererRegistrationCallback.EVENT.register((LivingEntityFeatureRendererRegistrationCallback)(type, renderer, helper, context) -> {
         if (renderer instanceof class_1007<?> player) {
            helper.register(new AccessoryFeature(player));
         }
      });
      ClickGuiKey.register();
      DiscordPresence presence = new DiscordPresence();
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted)client -> {
         renderer = new Render3D();
         CONFIG.load();
         HudLayout.load();
         boolean migrated = CONFIG.loadedCleanly(MODULES.get(ClickGui.class)) && MODULES.get(ClickGui.class).migrate();
         if (CONFIG.loadedCleanly(MODULES.get(Hud.class)) && MODULES.get(Hud.class).migrate()) {
            migrated = true;
         }

         if (CONFIG.loadedCleanly(MODULES.get(MainMenu.class)) && MODULES.get(MainMenu.class).migrate()) {
            migrated = true;
         }

         if (CONFIG.loadedCleanly(MODULES.get(CsPickaxe.class)) && MODULES.get(CsPickaxe.class).migrate()) {
            migrated = true;
         }

         if (migrated) {
            CONFIG.save();
         }

         MODULES.get(CustomGlint.class).sync();
         ClickGuiKey.sync(client);
         presence.setEnabled(true);
         BrandWindow.apply(client);
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
         presence.setEnabled(false);
         CONFIG.save();
         HudLayout.save();
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         ClickGuiKey.tick(client);
         MODULES.pollKeybinds(client);
         HudMetrics.tick(client);
         GlintManager.ensure();
         MenuAudio.tick(client);
         if (client.field_1724 != null && client.field_1687 != null) {
            MODULES.onTick();
         }
      });
      ClientChunkEvents.CHUNK_LOAD.register((Load)(world, chunk) -> MODULES.onChunkLoad(chunk));
      PauseMenuButton.register();
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> {
         HudMetrics.reset();
         MODULES.get(CustomGlint.class).checkConflicts();
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> MODULES.onGameLeft());
      WorldRenderEvents.END_MAIN.register((EndMain)context -> {
         class_310 mc = class_310.method_1551();
         if (renderer != null && mc.field_1687 != null && mc.field_1724 != null) {
            class_4184 camera = mc.field_1773.method_19418();

            try {
               renderer.begin(camera.method_71156(), camera.method_19329(), camera.method_19330());
               MODULES.onRender(renderer);
               renderer.end();
            } catch (RuntimeException var4) {
               LOG.error("Render failed", var4);
            }
         }
      });
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((BeforeBlockOutline)(context, outline) -> {
         try {
            return AuraRenderer.render(context, outline);
         } catch (RuntimeException var3) {
            return true;
         }
      });
      HudElementRegistry.addFirst(
         class_2960.method_60655("violet", "sky_lyrics"), (context, tickCounter) -> SkyLyrics.render(context, tickCounter.method_60637(true))
      );
      HudElementRegistry.addLast(class_2960.method_60655("violet", "module_list"), HudRenderer::render);
      CompletionChannel.install();
      LOG.info("Copium Client loaded with {} modules", MODULES.all().size());
   }
}
