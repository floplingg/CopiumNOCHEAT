package dev.violet.gui;

import dev.violet.SoberClient;
import dev.violet.modules.MainMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.joml.Matrix3x2fStack;

public final class VioletScreen extends class_437 {
   static final String BOLD = "montserrat-bold.ttf";
   private static final float REFERENCE_H = 323.0F;
   private static final float BUTTON_W = 100.0F;
   private static final float BUTTON_H = 14.0F;
   private static final float STEP = 17.0F;
   private static final float LOGO_W = 84.0F;
   private static final float LOGO_GAP = 58.0F;
   private static final float BLOCK_H = 140.5F;
   private static final ExactImage LOGO = new ExactImage("textures/brand/lockup.png");
   private static boolean shownOnce;
   private final List<VioletScreen.Button> buttons = new ArrayList<>();
   private final Map<VioletScreen.Button, Float> hover = new HashMap<>();
   private final long openedAt = System.nanoTime();
   private long lastFrame = System.nanoTime();
   private float delta;
   private float fade = 1.0F;
   private boolean firstShow;
   private float s;
   private float logoCenterY;

   public VioletScreen() {
      super(class_2561.method_43470("SoberClient"));
   }

   public boolean method_25422() {
      return false;
   }

   protected void method_25426() {
      this.buttons.clear();
      this.hover.clear();
      this.s = Math.clamp((float)this.field_22790 / 323.0F, 0.8F, 2.4F);
      boolean mods = FabricLoader.getInstance().isModLoaded("modmenu");
      int w = Math.round(100.0F * this.s);
      int h = Math.round(14.0F * this.s);
      int step = Math.round(17.0F * this.s);
      float blockTop = (float)this.field_22790 / 2.0F - 74.75F * this.s;
      this.logoCenterY = blockTop + 16.9F * this.s;
      int x = Math.round((float)(this.field_22789 - w) / 2.0F);
      int y = Math.round(blockTop + 58.0F * this.s);
      this.buttons.add(new VioletScreen.Button("Singleplayer", VioletScreen.Icon.PERSON, x, y, w, h, () -> this.field_22787.method_1507(new class_526(this))));
      y += step;
      this.buttons
         .add(
            new VioletScreen.Button(
               "Multiplayer",
               VioletScreen.Icon.GLOBE,
               x,
               y,
               w,
               h,
               () -> this.field_22787.method_1507((class_437)(this.field_22787.field_1690.field_21840 ? new class_500(this) : new class_4749(this)))
            )
         );
      y += step;
      if (mods) {
         this.buttons.add(new VioletScreen.Button("Mods", VioletScreen.Icon.CUBE, x, y, w, h, this::openMods));
         y += step;
      }

      this.buttons
         .add(
            new VioletScreen.Button(
               "Options", VioletScreen.Icon.GEAR, x, y, w, h, () -> this.field_22787.method_1507(new class_429(this, this.field_22787.field_1690))
            )
         );
      y += step;
      this.buttons.add(new VioletScreen.Button("Quit", VioletScreen.Icon.CLOSE, x, y, w, h, () -> this.field_22787.method_1592()));
      if (!shownOnce) {
         shownOnce = true;
         MainMenu menu = SoberClient.MODULES.get(MainMenu.class);
         this.firstShow = menu == null || menu.intro.get();
      }
   }

   private void openMods() {
      try {
         Class<?> type = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
         this.field_22787.method_1507((class_437)type.getConstructor(class_437.class).newInstance(this));
      } catch (RuntimeException | ReflectiveOperationException var2) {
         SoberClient.LOG.error("Could not open the mod list", var2);
      }
   }

   static void image(class_332 context, BrandTextures.Sprite sprite, float x, float y, float w, float h, int color) {
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

   public static void paintSpace(class_332 context, int width, int height, float opacity) {
      context.method_25294(0, 0, width, height, MenuKit.alpha(-15856112, opacity));
      CleanBackdrop.draw(context, opacity);
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      paintSpace(context, this.field_22789, this.field_22790, 1.0F);
   }

   static void glassBackdrop(class_332 context, class_310 client) {
      if (Backdrop.requestMenu(client)) {
         try {
            context.method_71048();
            context.method_71278();
         } catch (IllegalStateException var3) {
         }
      }
   }

   static void glassPill(class_332 context, int x, int y, int w, int h, int r, float hover, float opacity) {
      if (Theme.liquidGlass()) {
         Smooth.liquid(context, x, y, w, h, r, opacity, true);
         Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(806620200, 351135743, hover), opacity));
      } else {
         Smooth.rect(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(384690175, 653125631, hover), opacity));
         Smooth.outline(context, x, y, w, h, r, MenuKit.alpha(MenuKit.lerp(486539263, 889192447, hover), opacity));
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
      long now = System.nanoTime();
      this.delta = Math.min(0.06F, (float)(now - this.lastFrame) / 1.0E9F);
      this.lastFrame = now;
      float since = (float)(now - this.openedAt) / 1.0E9F;
      this.fade = this.firstShow ? MenuKit.quint(Math.min(1.0F, since / 1.1F)) : MenuKit.quint(Math.min(1.0F, since / 0.25F));
      super.method_25394(context, mouseX, mouseY, deltaTicks);
      float head = this.appear(since, 0.0F);
      float logoW = 84.0F * this.s;
      float logoH = logoW * LOGO.aspect();
      LOGO.draw(
         context, ((float)this.field_22789 - logoW) / 2.0F, this.logoCenterY - logoH / 2.0F + (1.0F - head) * 5.0F * this.s, logoW, MenuKit.alpha(-1, head)
      );

      for (int i = 0; i < this.buttons.size(); i++) {
         this.drawButton(context, this.buttons.get(i), this.appear(since, 0.1F + (float)i * 0.05F), mouseX, mouseY);
      }
   }

   private float appear(float since, float delay) {
      float start = this.firstShow ? 0.35F : 0.05F;
      return this.fade * MenuKit.quint(Math.max(0.0F, Math.min(1.0F, (since - start - delay) / 0.45F)));
   }

   private void drawButton(class_332 context, VioletScreen.Button button, float opacity, int mouseX, int mouseY) {
      if (!(opacity <= 0.002F)) {
         boolean over = button.contains((double)mouseX, (double)mouseY);
         float hv = MenuKit.approach(this.hover.getOrDefault(button, 0.0F), over ? 1.0F : 0.0F, 14.0F, this.delta);
         this.hover.put(button, hv);
         int x = button.x();
         int y = button.y() + Math.round((1.0F - opacity) * 6.0F * this.s);
         int w = button.w();
         int h = button.h();
         Smooth.rect(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-267119594, -199484385, hv), opacity));
         Smooth.outline(context, x, y, w, h, 3, MenuKit.alpha(MenuKit.lerp(-14408664, -12829629, hv), opacity));
         int tint = MenuKit.alpha(MenuKit.lerp(-2302752, -1, hv), opacity);
         float em = 4.7F * this.s;
         float iconSize = 4.4F * this.s;
         float gap = 3.2F * this.s;
         float textW = CrispText.width(button.label(), "montserrat-bold.ttf", em, 0.0F);
         float left = (float)x + ((float)w - iconSize - gap - textW) / 2.0F;
         icon(context, button.icon(), left + iconSize / 2.0F, (float)y + (float)h / 2.0F, Math.round(iconSize), tint);
         CrispText.draw(context, button.label(), "montserrat-bold.ttf", left + iconSize + gap, (float)y + (float)h / 2.0F + em * 0.36F, em, 0.0F, tint);
      }
   }

   private static void icon(class_332 context, VioletScreen.Icon icon, float cx, float cy, int size, int color) {
      switch (icon) {
         case PERSON:
            Icons.draw(context, Icons.Icon.PERSON, Math.round(cx - (float)size / 2.0F), Math.round(cy - (float)size / 2.0F), size, color);
            break;
         case GLOBE:
            globe(context, cx, cy, (float)size * 0.5F, color);
            break;
         case CUBE:
            Icons.draw(context, Icons.Icon.CUBE, Math.round(cx - (float)size / 2.0F), Math.round(cy - (float)size / 2.0F), size, color);
            break;
         case GEAR:
            Icons.draw(context, Icons.Icon.GEAR, Math.round(cx - (float)size / 2.0F), Math.round(cy - (float)size / 2.0F), size, color);
            break;
         case CLOSE:
            Icons.draw(context, Icons.Icon.CLOSE, Math.round(cx - (float)size / 2.0F), Math.round(cy - (float)size / 2.0F), size, color);
      }
   }

   private static void globe(class_332 context, float cx, float cy, float radius, int color) {
      int n = 33;
      float line = Math.max(0.4F, radius * 0.2F);

      for (int ring = 0; ring < 2; ring++) {
         float rx = ring == 0 ? radius : radius * 0.44F;
         float[] xs = new float[n];
         float[] ys = new float[n];
         float[] ws = new float[n];
         int[] cs = new int[n];

         for (int i = 0; i < n; i++) {
            double a = 6.283185307179586 * (double)i / (double)(n - 1);
            xs[i] = cx + (float)Math.cos(a) * rx;
            ys[i] = cy + (float)Math.sin(a) * radius;
            ws[i] = line;
            cs[i] = color;
         }

         Smooth.polyline(context, xs, ys, ws, cs, n);
      }

      Smooth.polyline(context, new float[]{cx - radius, cx + radius}, new float[]{cy, cy}, new float[]{line, line}, new int[]{color, color}, 2);
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (super.method_25402(click, doubled)) {
         return true;
      } else if (this.fade < 0.5F) {
         return true;
      } else {
         for (VioletScreen.Button button : this.buttons) {
            if (button.contains(click.comp_4798(), click.comp_4799())) {
               UiSounds.click();
               button.action().run();
               return true;
            }
         }

         return false;
      }
   }

   private static record Button(String label, VioletScreen.Icon icon, int x, int y, int w, int h, Runnable action) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
      }
   }

   private static enum Icon {
      PERSON,
      GLOBE,
      CUBE,
      GEAR,
      CLOSE;
   }
}
