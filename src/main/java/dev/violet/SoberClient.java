package dev.violet;

import com.mojang.logging.LogUtils;
import dev.violet.esp.ChunkScanner;
import dev.violet.event.impl.PacketAppliedEvent;
import dev.violet.gui.BrandWindow;
import dev.violet.gui.HudLayout;
import dev.violet.gui.HudMetrics;
import dev.violet.gui.HudRenderer;
import dev.violet.gui.MenuAudio;
import dev.violet.gui.PauseMenuButton;
import dev.violet.gui.SkyLyrics;
import dev.violet.gui.VanishAlert;
import dev.violet.module.Module;
import dev.violet.module.ModuleManager;
import dev.violet.modules.ActivityInspector;
import dev.violet.modules.AimAssist;
import dev.violet.modules.AmethystBypass;
import dev.violet.modules.AnchorMacro;
import dev.violet.modules.AnimatedTyping;
import dev.violet.modules.AntiCheat;
import dev.violet.modules.AntiTrap;
import dev.violet.modules.AntiVanish;
import dev.violet.modules.Aspect;
import dev.violet.modules.AuctionSniper;
import dev.violet.modules.AutoClicker;
import dev.violet.modules.AutoCrystal;
import dev.violet.modules.AutoDoubleHand;
import dev.violet.modules.AutoEat;
import dev.violet.modules.AutoFirework;
import dev.violet.modules.AutoHitCrystal;
import dev.violet.modules.AutoInvTotem;
import dev.violet.modules.AutoJump;
import dev.violet.modules.AutoJumpReset;
import dev.violet.modules.AutoLog;
import dev.violet.modules.AutoReconnect;
import dev.violet.modules.AutoRespawn;
import dev.violet.modules.AutoSell;
import dev.violet.modules.AutoShop;
import dev.violet.modules.AutoTool;
import dev.violet.modules.AutoTotem;
import dev.violet.modules.AutoWalk;
import dev.violet.modules.BedrockVoidESP;
import dev.violet.modules.BlockESP;
import dev.violet.modules.BlockEntityDebug;
import dev.violet.modules.BlockNotifier;
import dev.violet.modules.BlockOverlay;
import dev.violet.modules.BoatNoclip;
import dev.violet.modules.Boatfly;
import dev.violet.modules.Capes;
import dev.violet.modules.ChunkScannerModule;
import dev.violet.modules.ClearView;
import dev.violet.modules.ClickGui;
import dev.violet.modules.ClickTp;
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
import dev.violet.modules.ElytraFly;
import dev.violet.modules.ElytraSwap;
import dev.violet.modules.EntityControl;
import dev.violet.modules.EntitySpeed;
import dev.violet.modules.FakePay;
import dev.violet.modules.FakeStats;
import dev.violet.modules.FastUse;
import dev.violet.modules.Fly;
import dev.violet.modules.FlySpeed;
import dev.violet.modules.FpsBooster;
import dev.violet.modules.Freecam;
import dev.violet.modules.Freelook;
import dev.violet.modules.Fullbright;
import dev.violet.modules.Glass;
import dev.violet.modules.HomeMeta;
import dev.violet.modules.HomeReset;
import dev.violet.modules.Hotkeys;
import dev.violet.modules.Hud;
import dev.violet.modules.InventoryHud;
import dev.violet.modules.Jesus;
import dev.violet.modules.JumpCircles;
import dev.violet.modules.KeyHomesetter;
import dev.violet.modules.KeyPearl;
import dev.violet.modules.LightEsp;
import dev.violet.modules.LightFinder;
import dev.violet.modules.MainMenu;
import dev.violet.modules.MediaHider;
import dev.violet.modules.MotionBlur;
import dev.violet.modules.MotionScanner;
import dev.violet.modules.NameTags;
import dev.violet.modules.NetheriteFinder;
import dev.violet.modules.NoJumpDelay;
import dev.violet.modules.PanicPay;
import dev.violet.modules.PlayerESP;
import dev.violet.modules.RegionMap;
import dev.violet.modules.RelogMethod;
import dev.violet.modules.RenderDistanceToggle;
import dev.violet.modules.RtpMethod;
import dev.violet.modules.SeedChunkFinder;
import dev.violet.modules.ShieldBreaker;
import dev.violet.modules.SkeletonESP;
import dev.violet.modules.SkinChange;
import dev.violet.modules.Sky;
import dev.violet.modules.Sneak;
import dev.violet.modules.SpawnerBeam;
import dev.violet.modules.Speed;
import dev.violet.modules.SpeedMine;
import dev.violet.modules.Spider;
import dev.violet.modules.SpotifyPlayer;
import dev.violet.modules.Sprint;
import dev.violet.modules.StaffLive;
import dev.violet.modules.StepTiles;
import dev.violet.modules.StorageESP;
import dev.violet.modules.SusChunk;
import dev.violet.modules.SuspiciousESP;
import dev.violet.modules.SwingAnimations;
import dev.violet.modules.Tracers;
import dev.violet.modules.TridentBoost;
import dev.violet.modules.TunnelHoleESP;
import dev.violet.modules.WeatherNotifier;
import dev.violet.modules.Zoom;
import dev.violet.render.AccessoryFeature;
import dev.violet.render.Render3D;
import dev.violet.render.XrayFeature;
import dev.violet.sky.AuraRenderer;
import dev.violet.sky.GlintManager;
import dev.violet.util.CompletionChannel;
import dev.violet.util.netherite.NetheriteChunkTracker;
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
   private static final Object NETHERITE_TRACKER_OWNER = new Object();
   private static Render3D renderer;

   private static void importLegacyConfig() {
      Path dir = FabricLoader.getInstance().getConfigDir();
      if (!Files.exists(dir.resolve("soberclient.json"))) {
         String old = Files.exists(dir.resolve("violetclient.json")) ? "violetclient" : "yepstein";
         if (!old.equals("yepstein") || Files.exists(dir.resolve("yepstein-client.json"))) {
            String main = old.equals("violetclient") ? "violetclient.json" : "yepstein-client.json";
            String areas = old.equals("violetclient") ? "violetclient-areas.json" : "yepstein-client-areas.json";
            String[][] files = new String[][]{
               {main, "soberclient.json"},
               {old + "-configs.json", "soberclient-configs.json"},
               {old + "-dropdown.json", "soberclient-dropdown.json"},
               {old + "-hud-layout.json", "soberclient-hud-layout.json"},
               {areas, "soberclient-areas.json"}
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
                     for (Path path : paths::iterator) {
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
      MODULES.add(new SusChunk());
      MODULES.add(new AmethystBypass());
      MODULES.add(new ChunkScannerModule());
      MODULES.add(new SuspiciousESP());
      MODULES.add(new TunnelHoleESP());
      MODULES.add(new BedrockVoidESP());
      MODULES.add(new LightFinder());
      MODULES.add(new SeedChunkFinder());
      MODULES.add(new SpawnerBeam());
      MODULES.add(new PlayerESP());
      MODULES.add(new SkeletonESP());
      MODULES.add(new BlockESP());
      MODULES.add(new StorageESP());
      MODULES.add(new BlockNotifier());
      MODULES.add(new Tracers());
      MODULES.add(new LightEsp());
      MODULES.add(new HomeMeta());
      MODULES.add(new RenderDistanceToggle());
      MODULES.add(new AntiTrap());
      MODULES.add(new AutoTotem());
      MODULES.add(new AutoInvTotem());
      MODULES.add(new AnchorMacro());
      MODULES.add(new AutoCrystal());
      MODULES.add(new AutoHitCrystal());
      MODULES.add(new AutoDoubleHand());
      MODULES.add(new AimAssist());
      MODULES.add(new AutoClicker());
      MODULES.add(new ShieldBreaker());
      MODULES.add(new AutoJumpReset());
      MODULES.add(new AutoEat());
      MODULES.add(new AutoFirework());
      MODULES.add(new AutoRespawn());
      MODULES.add(new FastUse());
      MODULES.add(new AuctionSniper());
      MODULES.add(new AutoSell());
      MODULES.add(new AutoShop());
      MODULES.add(new AutoTool());
      MODULES.add(new RtpMethod());
      MODULES.add(new RelogMethod());
      MODULES.add(new AutoReconnect());
      MODULES.add(new AutoLog());
      MODULES.add(new FakeStats());
      MODULES.add(new FakePay());
      MODULES.add(new StaffLive());
      MODULES.add(new NetheriteFinder());
      MODULES.add(new PanicPay());
      MODULES.add(new HomeReset());
      MODULES.add(new KeyHomesetter());
      MODULES.add(new KeyPearl());
      MODULES.add(new CustomCrosshair());
      MODULES.add(new MotionBlur());
      MODULES.add(new Sky());
      MODULES.add(new ClearView());
      MODULES.add(new Fullbright());
      MODULES.add(new Capes());
      MODULES.add(new Glass());
      MODULES.add(new Freelook());
      MODULES.add(new Freecam());
      MODULES.add(new Zoom());
      MODULES.add(new WeatherNotifier());
      MODULES.add(new SwingAnimations());
      MODULES.add(new FlySpeed());
      MODULES.add(new Boatfly());
      MODULES.add(new Fly());
      MODULES.add(new Speed());
      MODULES.add(new Sprint());
      MODULES.add(new Jesus());
      MODULES.add(new Spider());
      MODULES.add(new ElytraFly());
      MODULES.add(new ElytraSwap());
      MODULES.add(new BoatNoclip());
      MODULES.add(new EntityControl());
      MODULES.add(new EntitySpeed());
      MODULES.add(new AutoWalk());
      MODULES.add(new AutoJump());
      MODULES.add(new ClickTp());
      MODULES.add(new Sneak());
      MODULES.add(new NoJumpDelay());
      MODULES.add(new TridentBoost());
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
      MODULES.add(new BlockEntityDebug());
      MODULES.add(new MotionScanner());
      MODULES.add(new SpeedMine());
      MODULES.add(new AntiCheat());
      MODULES.add(new AntiVanish());
      MODULES.add(new ActivityInspector());
      MODULES.add(new Configs());
      LivingEntityFeatureRendererRegistrationCallback.EVENT.register((LivingEntityFeatureRendererRegistrationCallback)(type, renderer, helper, context) -> {
         if (renderer instanceof class_1007<?> player) {
            helper.register(new AccessoryFeature(player));
         }

         if (renderer instanceof class_1007<?> player) {
            helper.register(new XrayFeature(player));
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
      HudElementRegistry.addLast(class_2960.method_60655("violet", "vanish_alert"), (context, tickCounter) -> VanishAlert.render(context));
      CompletionChannel.install();
      if (Boolean.getBoolean("violet.guitest")) {
         GuiTest.register();
      }

      ChunkScanner.init();
      PacketAppliedEvent.TYPE.listen(NETHERITE_TRACKER_OWNER, event -> NetheriteChunkTracker.onPacketApplied(event.packet));
      LOG.info("SoberClient loaded with {} modules", MODULES.all().size());
   }
}
