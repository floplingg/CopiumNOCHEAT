package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.MainMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4189;
import net.minecraft.class_426;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_8219;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class VanillaPlusScreen extends class_437 {
   private static final String TAG_FONT = "geist-medium.ttf";
   private static final String COPYRIGHT = "Copyright Mojang AB. Do not distribute!";
   private static final float TAG_EM = 6.2F;
   private static final float TAG_TRACK = 0.22F;
   private static final String[] KINDS = new String[]{"orbit"};
   private static boolean shownOnce;
   private static List<VanillaPlusScreen.Prop> props;
   private final List<VanillaPlusScreen.Button> buttons = new ArrayList<>();
   private final Map<VanillaPlusScreen.Button, Float> hover = new HashMap<>();
   private final long openedAt = System.nanoTime();
   private long lastFrame = System.nanoTime();
   private float delta;
   private float fade = 1.0F;
   private boolean firstShow;
   private int buttonTop;
   private Supplier<class_8685> skin;

   public VanillaPlusScreen() {
      super(class_2561.method_43470("Copium Client"));
   }

   public boolean method_25422() {
      return false;
   }

   protected void method_25426() {
      this.buttons.clear();
      this.hover.clear();
      boolean mods = FabricLoader.getInstance().isModLoaded("modmenu");
      int bw = 200;
      int bh = 20;
      int step = 24;
      int rows = mods ? 4 : 3;
      int top = Math.max(this.field_22790 / 4 + 48, Math.min(this.field_22790 / 2 - 8, this.field_22790 - (rows * step + 12 + bh) - 18));
      this.buttonTop = top;
      int x = this.field_22789 / 2 - bw / 2;
      this.buttons
         .add(
            new VanillaPlusScreen.Button("Singleplayer", VanillaPlusScreen.Icon.NONE, x, top, bw, bh, () -> this.field_22787.method_1507(new class_526(this)))
         );
      int y = top + step;
      this.buttons
         .add(
            new VanillaPlusScreen.Button(
               "Multiplayer",
               VanillaPlusScreen.Icon.NONE,
               x,
               y,
               bw,
               bh,
               () -> this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500(this) : new class_4749(this)))
            )
         );
      y += step;
      this.buttons
         .add(
            new VanillaPlusScreen.Button(
               "Minecraft Realms", VanillaPlusScreen.Icon.NONE, x, y, bw, bh, () -> this.field_22787.method_1507(new class_4325(this))
            )
         );
      y += step;
      if (mods) {
         this.buttons.add(new VanillaPlusScreen.Button("Mods", VanillaPlusScreen.Icon.NONE, x, y, bw, bh, this::openMods));
         y += step;
      }

      y += 12;
      this.buttons
         .add(
            new VanillaPlusScreen.Button(
               "Options...", VanillaPlusScreen.Icon.NONE, x, y, 98, bh, () -> this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690))
            )
         );
      this.buttons.add(new VanillaPlusScreen.Button("Quit Game", VanillaPlusScreen.Icon.NONE, x + 102, y, 98, bh, () -> this.field_22787.method_1592()));
      this.buttons
         .add(
            new VanillaPlusScreen.Button(
               "",
               VanillaPlusScreen.Icon.GLOBE,
               x - 24,
               y,
               20,
               bh,
               () -> this.field_22787.method_1507(new class_426(this, this.field_22787.field_1690, this.field_22787.method_1526()))
            )
         );
      this.buttons
         .add(
            new VanillaPlusScreen.Button(
               "", VanillaPlusScreen.Icon.PERSON, x + bw + 4, y, 20, bh, () -> this.field_22787.method_1507(new class_4189(this, this.field_22787.field_1690))
            )
         );

      try {
         this.skin = this.field_22787.method_1582().method_73544(this.field_22787.method_53462(), false);
      } catch (RuntimeException var10) {
         this.skin = null;
      }

      if (props == null) {
         props = scatter();
      }

      if (!shownOnce) {
         shownOnce = true;
         MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
         this.firstShow = menu == null || menu.intro.get();
         MenuAudio.menuShown();
      }
   }

   private static List<VanillaPlusScreen.Prop> scatter() {
      List<VanillaPlusScreen.Prop> list = new ArrayList<>();
      Random random = new Random(21L);
      int tries = 0;

      while (list.size() < 32 && tries++ < 4000) {
         int depth = list.size() < 13 ? 0 : (list.size() < 24 ? 1 : 2);
         float x = random.nextFloat();
         float y = random.nextFloat();
         if ((depth <= 0 || !(Math.abs(x - 0.5F) < (depth == 2 ? 0.25F : 0.19F)) || !(y > 0.04F) || !(y < 0.93F))
            && (depth <= 0 || !(x < 0.3F) || !(y > 0.44F))) {
            String kind = KINDS[random.nextInt(KINDS.length)];
            float size = (depth == 0 ? 0.045F : (depth == 1 ? 0.07F : 0.1F)) * (0.8F + random.nextFloat() * 0.5F);
            list.add(
               new VanillaPlusScreen.Prop(
                  kind, x, y, size, random.nextFloat() * 360.0F - 180.0F, (random.nextFloat() - 0.5F) * 14.0F, random.nextFloat() * 6.283F, depth
               )
            );
         }
      }

      return list;
   }

   private void openMods() {
      try {
         Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
         this.field_22787.method_1507((class_437)type.getConstructor(class_437.class).newInstance(this));
      } catch (RuntimeException | ReflectiveOperationException var2) {
         SoberClient.LOG.error("Could not open the mod list", var2);
      }
   }

   private int c(int color) {
      return MenuKit.alpha(color, this.fade);
   }

   private static String version(String id) {
      return FabricLoader.getInstance().getModContainer(id).map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("");
   }

   private static void image(class_332 context, BrandTextures.Sprite sprite, float x, float y, float w, float h, int color) {
      if (sprite != null) {
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         matrices.translate(x, y);
         matrices.scale(w / (float)sprite.width(), h / (float)sprite.height());
         context.method_25293(
            class_10799.field_56883,
            sprite.id(),
            0,
            0,
            0.0F,
            0.0F,
            sprite.width(),
            sprite.height(),
            sprite.width(),
            sprite.height(),
            sprite.width(),
            sprite.height(),
            color
         );
         matrices.popMatrix();
      }
   }

   private static float pixels(class_332 context) {
      return Smooth.pixelsPerUnit(context);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      VioletScreen.paintSpace(context, this.field_22789, this.field_22790, 1.0F);
      this.drawProps(context, mouseX, mouseY);
      context.method_25296(0, Math.round((float)this.field_22790 * 0.62F), this.field_22789, this.field_22790, 722710, 2013988630);
   }

   private void drawProps(class_332 context, int mouseX, int mouseY) {
      if (props != null) {
         float time = (float)(System.nanoTime() - this.openedAt) / 1.0E9F + 40.0F;
         float ppu = pixels(context);
         float scale = Math.min((float)this.field_22789, (float)this.field_22790 * 1.78F);
         float mx = ((float)mouseX - (float)this.field_22789 / 2.0F) / Math.max(1.0F, (float)this.field_22789);
         float my = ((float)mouseY - (float)this.field_22790 / 2.0F) / Math.max(1.0F, (float)this.field_22790);
         int[] alpha = new int[]{44, 70, 102};
         float[] blur = new float[]{3.2F, 1.6F, 1.0F};
         float[] parallax = new float[]{4.0F, 9.0F, 16.0F};

         for (VanillaPlusScreen.Prop prop : props) {
            float size = prop.size() * scale;
            float x = prop.x() * (float)this.field_22789 + (float)Math.cos((double)(time * 0.21F + prop.phase())) * size * 0.18F - mx * parallax[prop.depth()];
            float y = prop.y() * (float)this.field_22790 + (float)Math.sin((double)(time * 0.33F + prop.phase())) * size * 0.22F - my * parallax[prop.depth()];
            float angle = prop.angle() + prop.spin() * time * 0.6F + (float)Math.sin((double)(time * 0.4F + prop.phase())) * 9.0F;
            BrandTextures.Sprite sprite = BrandTextures.fit(prop.kind(), size * ppu / blur[prop.depth()]);
            if (sprite != null) {
               float h = size * (float)sprite.height() / (float)sprite.width();
               Matrix3x2fStack matrices = context.method_51448();
               if (prop.depth() == 2) {
                  matrices.pushMatrix();
                  matrices.translate(x + size * 0.04F, y + size * 0.07F);
                  matrices.rotate((float)Math.toRadians((double)angle));
                  image(context, BrandTextures.fit(prop.kind(), size * ppu / 3.0F), -size / 2.0F, -h / 2.0F, size, h, this.c(939524096));
                  matrices.popMatrix();
               }

               matrices.pushMatrix();
               matrices.translate(x, y);
               matrices.rotate((float)Math.toRadians((double)angle));
               image(context, sprite, -size / 2.0F, -h / 2.0F, size, h, this.c(alpha[prop.depth()] << 24 | 12890367));
               matrices.popMatrix();
            }
         }
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      long now = System.nanoTime();
      this.delta = Math.min(0.06F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      float since = (float)(now - this.openedAt) / 1.0E9F;
      this.fade = this.firstShow ? MenuKit.quint(Math.min(1.0F, since / 1.1F)) : MenuKit.quint(Math.min(1.0F, since / 0.25F));
      super.method_25394(context, mouseX, mouseY, deltaTicks);
      this.drawTitle(context);

      for (VanillaPlusScreen.Button button : this.buttons) {
         this.drawButton(context, button, mouseX, mouseY);
      }

      this.drawAccount(context);
      this.drawFooter(context, mouseX, mouseY);
   }

   private void drawTitle(class_332 context) {
      float space = (float)this.buttonTop - 10.0F;
      float logoH = space * 0.52F;
      float wordH = space * 0.2F;
      float ppu = pixels(context);
      BrandTextures.Sprite logoFull = BrandTextures.get("orbit_glow");
      if (logoFull != null) {
         float logoW = logoH * (float)logoFull.width() / (float)logoFull.height();
         image(context, BrandTextures.fit("orbit_glow", logoW * ppu), (float)this.field_22789 / 2.0F - logoW / 2.0F, 8.0F, logoW, logoH, this.c(-1));
      }

      BrandTextures.Sprite wordFull = BrandTextures.get("wordmark");
      float wordW = 0.0F;
      float wordTop = 8.0F + logoH + 2.0F;
      if (wordFull != null) {
         wordW = wordH * (float)wordFull.width() / (float)wordFull.height();
         if (wordW > (float)this.field_22789 - 20.0F) {
            wordW = (float)this.field_22789 - 20.0F;
            wordH = wordW * (float)wordFull.height() / (float)wordFull.width();
         }

         image(context, BrandTextures.fit("wordmark", wordW * ppu), (float)this.field_22789 / 2.0F - wordW / 2.0F, wordTop, wordW, wordH, this.c(-1));
      }

      String tag = "COPIUM CLIENT  ·  " + class_155.method_16673().comp_4025();
      float sw = CrispText.width(tag, "geist-medium.ttf", 6.2F, 0.22F);
      float ty = Math.min(wordTop + wordH + 6.0F, (float)this.buttonTop - 4.0F);
      CrispText.draw(context, tag, "geist-medium.ttf", (float)this.field_22789 / 2.0F - sw / 2.0F, ty, 6.2F, 0.22F, this.c(-926370561));
   }

   private void drawButton(class_332 context, VanillaPlusScreen.Button button, int mouseX, int mouseY) {
      boolean over = button.contains((double)mouseX, (double)mouseY);
      float hv = MenuKit.approach(this.hover.getOrDefault(button, 0.0F), over ? 1.0F : 0.0F, 18.0F, this.delta);
      this.hover.put(button, hv);
      int x = button.x();
      int y = button.y();
      int w = button.w();
      int h = button.h();
      Smooth.rect(context, x, y, w, h, 5, this.c(MenuKit.lerp(-433448366, -7643914, hv)));
      Smooth.rect(context, x, y + h / 2, w, h - h / 2, 5, this.c(MenuKit.lerp(1007355670, 1079714230, hv)));
      Smooth.rect(context, x + 2, y + 1, w - 4, 2, 1, this.c(MenuKit.lerp(822083583, 1895825407, hv)));
      Smooth.outline(context, x, y, w, h, 5, this.c(MenuKit.lerp(-2136503302, -3885571, hv)));
      int tint = this.c(-1);
      if (button.icon() == VanillaPlusScreen.Icon.NONE) {
         MenuKit.centered(context, button.label(), (float)x + (float)w / 2.0F, (float)y + (float)h / 2.0F - 3.4F, 0.8F, tint, false);
      } else if (button.icon() == VanillaPlusScreen.Icon.GLOBE) {
         globe(context, (float)x + 10.0F, (float)y + 10.0F, tint);
      } else {
         Icons.draw(context, Icons.Icon.PERSON, x + 4, y + 4, 12, tint);
      }
   }

   private static void globe(class_332 context, float cx, float cy, int color) {
      int n = 25;
      float[] xs = new float[n];
      float[] ys = new float[n];
      float[] ws = new float[n];
      int[] cs = new int[n];

      for (int ring = 0; ring < 2; ring++) {
         float rx = ring == 0 ? 5.0F : 2.2F;

         for (int i = 0; i < n; i++) {
            double a = 6.283185307179586 * (double)i / (double)(n - 1);
            xs[i] = cx + (float)Math.cos(a) * rx;
            ys[i] = cy + (float)Math.sin(a) * 5.0F;
            ws[i] = 0.9F;
            cs[i] = color;
         }

         Smooth.polyline(context, (float[])xs.clone(), (float[])ys.clone(), (float[])ws.clone(), (int[])cs.clone(), n);
      }

      Smooth.polyline(context, new float[]{cx - 5.0F, cx + 5.0F}, new float[]{cy, cy}, new float[]{0.9F, 0.9F}, new int[]{color, color}, 2);
   }

   private void drawAccount(class_332 context) {
      if (this.field_22787.method_1548() != null) {
         String name = this.field_22787.method_1548().method_1676();
         float nw = MenuKit.width(name, 0.7F, false);
         int chipW = Math.round(21.0F + nw + 8.0F);
         int chipH = 16;
         int chipX = this.field_22789 - 6 - chipW;
         int chipY = 6;
         Smooth.rect(context, chipX, chipY, chipW, chipH, 8, this.c(-1273491664));
         Smooth.outline(context, chipX, chipY, chipW, chipH, 8, this.c(1023410175));

         try {
            class_8685 textures = this.skin == null ? null : this.skin.get();

            if (textures != null && this.fade > 0.6F) {
               Avatars.draw(context, textures, chipX + 4, chipY + 3, 10);
            }
         } catch (RuntimeException var10) {
         }

         Smooth.circle(context, chipX + 13, chipY + 12, 1, this.c(-8396640));
         MenuKit.text(context, name, (float)(chipX + 18), (float)chipY + 4.6F, 0.7F, this.c(-1), false);
      }
   }

   private boolean overCopyright(double mx, double my) {
      float w = MenuKit.width("Copyright Mojang AB. Do not distribute!", 0.6F, false);
      return mx >= (double)((float)(this.field_22789 - 5) - w)
         && mx < (double)(this.field_22789 - 3)
         && my >= (double)(this.field_22790 - 11)
         && my < (double)this.field_22790;
   }

   private void drawFooter(class_332 context, int mouseX, int mouseY) {
      String left = "Minecraft " + class_155.method_16673().comp_4025() + "  ·  Fabric  ·  Copium Client " + version("copium");
      MenuKit.text(context, left, 5.0F, (float)this.field_22790 - 9.0F, 0.6F, this.c(-922746881), false);
      boolean over = this.overCopyright((double)mouseX, (double)mouseY);
      float w = MenuKit.width("Copyright Mojang AB. Do not distribute!", 0.6F, false);
      MenuKit.text(
         context,
         "Copyright Mojang AB. Do not distribute!",
         (float)(this.field_22789 - 5) - w,
         (float)this.field_22790 - 9.0F,
         0.6F,
         this.c(over ? -1 : -922746881),
         false
      );
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (super.method_25402(click, doubled)) {
         return true;
      } else if (this.fade < 0.5F) {
         return true;
      } else {
         for (VanillaPlusScreen.Button button : this.buttons) {
            if (button.contains(click.comp_4798(), click.comp_4799())) {
               UiSounds.click();
               button.action().run();
               return true;
            }
         }

         if (this.overCopyright(click.comp_4798(), click.comp_4799())) {
            UiSounds.click();
            this.field_22787.method_1507(new class_8219(this));
            return true;
         } else {
            return false;
         }
      }
   }

   private static record Button(String label, VanillaPlusScreen.Icon icon, int x, int y, int w, int h, Runnable action) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
      }
   }

   private static enum Icon {
      NONE,
      GLOBE,
      PERSON;
   }

   private static record Prop(String kind, float x, float y, float size, float angle, float spin, float phase, int depth) {
   }
}
