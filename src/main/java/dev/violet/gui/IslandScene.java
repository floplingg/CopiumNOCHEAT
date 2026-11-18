package dev.violet.gui;

import java.util.Random;
import net.minecraft.class_332;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

final class IslandScene {
   private static final float GROUP = 1447.0F;
   private static final float CANVAS_W = 1494.0F;
   private static final float SEA_X = 807.8F;
   private static final float SEA_Y = 1351.0F;
   private static final float BASE_X = 807.8F;
   private static final float BASE_Y = 1222.3F;
   private static final float CROWN_X = 661.4F;
   private static final float CROWN_Y = 326.2F;
   private static final float WATER_RX = 578.0F;
   private static final float WATER_RY = 79.0F;
   private static final float[] ISLAND_BOX = new float[]{137.0F, 1142.0F, 1331.0F, 331.0F};
   private static final float[] TRUNK_BOX = new float[]{619.0F, 290.0F, 288.0F, 973.0F};
   private static final float[] BACK_BOX = new float[]{140.0F, 26.0F, 1070.0F, 472.0F};
   private static final float[] FRONT_BOX = new float[]{26.0F, 64.0F, 1313.0F, 708.0F};
   private static final float TITLE_LEFT = -318.8F;
   private static final float TITLE_WIDTH = 2594.6F;
   private static final float[][] LETTERS = new float[][]{
      {-85.0F, -366.0F, 347.0F, 400.0F},
      {270.0F, -360.0F, 348.0F, 398.0F},
      {626.0F, -367.0F, 356.0F, 407.0F},
      {990.0F, -368.0F, 253.0F, 406.0F},
      {1251.0F, -367.0F, 360.0F, 405.0F},
      {1619.0F, -368.0F, 423.0F, 404.0F}
   };
   private static final float[][] CLOUDS = new float[][]{
      {0.08F, 0.4F, 0.17F, 0.0F, 0.0075F, 1.0F},
      {0.62F, 0.33F, 0.15F, 1.0F, 0.0095F, 1.0F},
      {0.3F, 0.52F, 0.075F, 3.0F, 0.0042F, 0.92F},
      {0.86F, 0.55F, 0.09F, 2.0F, 0.005F, 0.92F},
      {0.47F, 0.47F, 0.06F, 0.0F, 0.0035F, 0.85F},
      {0.18F, 0.08F, 0.11F, 2.0F, 0.006F, 0.9F},
      {0.74F, 0.05F, 0.12F, 3.0F, 0.0068F, 0.9F}
   };
   private static final float[][] SPARKLES = new float[22][4];
   private static final SharpTexture ISLAND = new SharpTexture("textures/menu/island3d.png");
   private static final SharpTexture TRUNK = new SharpTexture("textures/menu/trunk3d.png");
   private static final SharpTexture CROWN_BACK = new SharpTexture("textures/menu/crown_back.png");
   private static final SharpTexture CROWN_FRONT = new SharpTexture("textures/menu/crown_front.png");
   private static final SharpTexture SEA = new SharpTexture("textures/menu/sea.png");
   private static final SharpTexture FOAM = new SharpTexture("textures/menu/foam.png");
   private static final SharpTexture SHALLOW = new SharpTexture("textures/menu/shallow.png");
   private static final SharpTexture BIRD = new SharpTexture("textures/menu/bird.png");
   private static final SharpTexture[] CLOUD = new SharpTexture[4];
   private static final SharpTexture[] TITLE = new SharpTexture[LETTERS.length];
   private static float parallaxX;
   private static float parallaxY;

   private IslandScene() {
   }

   private static int alpha(int rgb, float alpha) {
      return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | rgb & 16777215;
   }

   private static float clamp(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static float easeOut(float t) {
      float f = 1.0F - t;
      return 1.0F - f * f * f;
   }

   private static float easeBack(float t) {
      float c = 1.5F;
      float d = c + 1.0F;
      float f = t - 1.0F;
      return 1.0F + d * f * f * f + c * f * f;
   }

   static float horizon(int height) {
      return (float)height * 0.635F;
   }

   static float scale(int width, int height) {
      return Math.min((float)height * (height < 300 ? 0.47F : 0.5F) / 1447.0F, (float)width * 0.62F / 1494.0F);
   }

   static float seaLevel(int height) {
      return (float)height * 0.758F;
   }

   static float islandBottom(int width, int height) {
      return seaLevel(height) + (ISLAND_BOX[1] + ISLAND_BOX[3] - 1351.0F) * scale(width, height);
   }

   static void background(class_332 context, int width, int height, float t, int mouseX, int mouseY, float delta) {
      double now = (double)System.nanoTime() / 1.0E9;
      float targetX = (float)mouseX / (float)Math.max(1, width) - 0.5F;
      float targetY = (float)mouseY / (float)Math.max(1, height) - 0.5F;
      float follow = 1.0F - (float)Math.exp((double)(-3.5F * delta));
      parallaxX = parallaxX + (targetX - parallaxX) * follow;
      parallaxY = parallaxY + (targetY - parallaxY) * follow;
      float horizon = horizon(height);
      sky(context, width, height, horizon);
      Matrix3x2fStack matrices = context.method_51448();
      float zoom = 1.0F + 0.07F * (1.0F - easeOut(clamp(t / 2.2F)));
      matrices.pushMatrix();
      matrices.translate((float)width / 2.0F, horizon);
      matrices.scale(zoom, zoom);
      matrices.translate((float)(-width) / 2.0F, -horizon);
      Smooth.glow(
         context, width / 2, Math.round((float)height * 0.16F), Math.round(Math.min((float)width, (float)height * 1.6F) * 0.42F), alpha(16777215, 0.16F)
      );
      clouds(context, width, height, now, 0.6F);
      birds(context, width, height, now);
      sea(context, width, height, horizon, now);
      island(context, width, height, horizon, t, now);
      matrices.popMatrix();
   }

   private static void sky(class_332 context, int width, int height, float horizon) {
      int h = Math.round(horizon);
      int a = Math.round(horizon * 0.45F);
      int b = Math.round(horizon * 0.8F);
      context.method_25296(0, 0, width, a, -13668400, -12151832);
      context.method_25296(0, a, width, b, -12151832, -10637586);
      context.method_25296(0, b, width, h, -10637586, -7678984);
      int sea1 = h + Math.round((float)(height - h) * 0.12F);
      int sea2 = h + Math.round((float)(height - h) * 0.55F);
      context.method_25296(0, h, width, sea1, -12941610, -12611622);
      context.method_25296(0, sea1, width, sea2, -12611622, -12082470);
      context.method_25296(0, sea2, width, height, -12082470, -12675640);
      context.method_25296(0, Math.round(horizon - (float)height * 0.07F), width, h, 16777215, 788529151);
      context.method_25296(0, h, width, h + Math.max(2, Math.round((float)height * 0.014F)), 1090519039, 16777215);
   }

   private static void clouds(class_332 context, int width, int height, double now, float depth) {
      for (float[] cloud : CLOUDS) {
         float w = (float)width * cloud[2];
         float h = w * CLOUD[(int)cloud[3]].aspect();
         float span = (float)width + w * 2.0F;
         float x = (float)(((double)(cloud[0] * span) + now * (double)cloud[4] * (double)width) % (double)span) - w;
         float near = cloud[2] / 0.17F;
         x -= parallaxX * (float)width * 0.012F * near * depth / 0.6F;
         float y = (float)height * cloud[1]
            - h / 2.0F
            + (float)Math.sin(now * 0.35 + (double)(cloud[0] * 9.0F)) * (float)height * 0.004F
            - parallaxY * (float)height * 0.006F * near;
         CLOUD[(int)cloud[3]].draw(context, x, y, w, h, alpha(16777215, cloud[5]));
      }
   }

   private static void birds(class_332 context, int width, int height, double now) {
      for (int i = 0; i < 2; i++) {
         double period = (double)(22 + i * 7);
         double local = (now + (double)i * 9.5) % period;
         double travel = 15.0;
         if (!(local > travel)) {
            float p = (float)(local / travel);
            float size = (float)width * (0.016F - (float)i * 0.004F);
            float x = -size + p * ((float)width + size * 2.0F);
            float y = (float)height * (0.27F + (float)i * 0.06F) + (float)Math.sin(now * 0.8 + (double)i) * (float)height * 0.015F;
            float flap = (float)Math.sin(now * 7.5 + (double)(i * 2));
            float sy = 0.25F + 0.75F * Math.abs(flap);
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(1.0F, flap < 0.0F ? -sy * 0.6F : sy);
            BIRD.draw(context, -size / 2.0F, -size / 4.0F, size, size / 2.0F, size, alpha(1979728, 0.75F));
            matrices.popMatrix();
         }
      }
   }

   private static void sea(class_332 context, int width, int height, float horizon, double now) {
      float seaH = (float)height - horizon;
      float tile = (float)width * 0.75F;

      for (int layer = 0; layer < 2; layer++) {
         float speed = layer == 0 ? 0.012F : -0.007F;
         float offset = (float)(now * (double)speed * (double)width % (double)tile);
         if (offset > 0.0F) {
            offset -= tile;
         }

         float shift = -parallaxX * (float)width * 0.01F;
         float a = layer == 0 ? 0.55F : 0.32F;
         float pulse = 0.85F + 0.15F * (float)Math.sin(now * 0.9 + (double)(layer * 2));
         Matrix3x2fStack matrices = context.method_51448();
         matrices.pushMatrix();
         if (layer == 1) {
            matrices.translate((float)width, 0.0F);
            matrices.scale(-1.0F, 1.0F);
         }

         for (float x = offset + shift % tile - tile; x < (float)width + tile; x += tile) {
            SEA.draw(context, x, horizon + (layer == 1 ? seaH * 0.015F : 0.0F), tile, seaH, tile, alpha(16777215, a * pulse));
         }

         matrices.popMatrix();
      }

      for (float[] sparkle : SPARKLES) {
         float depth = (float)Math.pow((double)sparkle[1], 1.4);
         float x = sparkle[0] * (float)width - parallaxX * (float)width * 0.01F * depth;
         float y = horizon + 3.0F + depth * (seaH - 6.0F);
         float twinkle = (float)Math.pow(Math.max(0.0, Math.sin(now * (double)sparkle[2] + (double)sparkle[3])), 14.0);
         if (!(twinkle < 0.02F)) {
            float r = 0.8F + 2.6F * depth;
            Smooth.softDot(context, x, y, r * 2.2F, alpha(16777215, twinkle * 0.45F));
            Smooth.softDot(context, x, y, r, alpha(16777215, twinkle));
         }
      }
   }

   private static void island(class_332 context, int width, int height, float horizon, float t, double now) {
      float k = scale(width, height);
      float cx = (float)width / 2.0F - parallaxX * (float)width * 0.018F;
      float seaY = seaLevel(height);
      int white = alpha(16777215, 1.0F);
      float shallowW = 1560.6F * k;
      float shallowH = 363.4F * k;
      SHALLOW.draw(context, cx - shallowW / 2.0F, seaY - shallowH / 2.0F, shallowW, shallowH, alpha(16777215, 0.9F));
      float ringW = 1344.186F * k;
      float ringH = 183.72093F * k;

      for (int i = 0; i < 2; i++) {
         float cycle = (float)((now / 4.2 + (double)i * 0.5) % 1.0);
         float s = 1.0F + cycle * 0.32F;
         FOAM.draw(
            context,
            cx - ringW * s / 2.0F,
            seaY - ringH * (1.0F + cycle * 0.9F) / 2.0F,
            ringW * s,
            ringH * (1.0F + cycle * 0.9F),
            ringW,
            alpha(16777215, 0.42F * (1.0F - cycle) * Math.min(1.0F, cycle * 6.0F))
         );
      }

      float breathe = 1.0F + 0.01F * (float)Math.sin(now * 1.7);
      FOAM.draw(context, cx - ringW * breathe / 2.0F, seaY - ringH * breathe / 2.0F, ringW * breathe, ringH * breathe, ringW, alpha(16777215, 0.8F));
      layer(context, ISLAND, ISLAND_BOX, cx, seaY, k, white);
      float sway = 0.016F * (float)Math.sin(now * 0.85) + 0.006F * (float)Math.sin(now * 2.3 + 1.1);
      if (t > 0.5F && t < 4.0F) {
         float local = t - 0.5F;
         sway += 0.05F * (float)Math.exp((double)(-local * 1.9F)) * (float)Math.sin((double)(local * 7.5F));
      }

      float baseX = cx + 0.0F * k;
      float baseY = seaY + -128.69995F * k;
      float crownX = cx + -146.39996F * k;
      float crownY = seaY + -1024.8F * k;
      float crownShift = sway * (baseY - crownY);
      float angle = sway * 0.85F + 0.012F * (float)Math.sin(now * 2.7 + 0.4);
      Matrix3x2fStack matrices = context.method_51448();
      crown(context, CROWN_BACK, BACK_BOX, cx, seaY, k, crownX, crownY, crownShift, angle, white);
      matrices.pushMatrix();
      matrices.translate(baseX, baseY);
      matrices.mul(new Matrix3x2f(1.0F, 0.0F, -sway, 1.0F, 0.0F, 0.0F));
      matrices.translate(-baseX, -baseY);
      layer(context, TRUNK, TRUNK_BOX, cx, seaY, k, white);
      matrices.popMatrix();
      crown(context, CROWN_FRONT, FRONT_BOX, cx, seaY, k, crownX, crownY, crownShift, angle, white);
   }

   private static void layer(class_332 context, SharpTexture texture, float[] box, float cx, float seaY, float k, int color) {
      texture.draw(context, cx + (box[0] - 807.8F) * k, seaY + (box[1] - 1351.0F) * k, box[2] * k, box[3] * k, color);
   }

   private static void crown(
      class_332 context, SharpTexture texture, float[] box, float cx, float seaY, float k, float pivotX, float pivotY, float shift, float angle, int color
   ) {
      Matrix3x2fStack matrices = context.method_51448();
      matrices.pushMatrix();
      matrices.translate(pivotX + shift, pivotY);
      matrices.rotate(angle);
      matrices.translate(-pivotX, -pivotY);
      layer(context, texture, box, cx, seaY, k, color);
      matrices.popMatrix();
   }

   static float titleBottom(int width, int height) {
      float titleW = Math.min((float)width * 0.54F, (float)height * 1.0F);
      return (float)height * 0.215F + 170.0F * titleW / 2594.6F;
   }

   static void title(class_332 context, int width, int height, float t) {
      double now = (double)System.nanoTime() / 1.0E9;
      float titleW = Math.min((float)width * 0.54F, (float)height * 1.0F);
      float s = titleW / 2594.6F;
      float baseline = (float)height * 0.215F - parallaxY * (float)height * 0.008F;
      float left = (float)width / 2.0F - titleW / 2.0F - -318.8F * s - parallaxX * (float)width * 0.008F;

      for (int i = 0; i < LETTERS.length; i++) {
         float[] m = LETTERS[i];
         float p = clamp((t - 0.75F - (float)i * 0.085F) / 0.6F);
         if (!(p <= 0.0F)) {
            float size = 0.3F + 0.7F * easeBack(p);
            float fade = clamp(p * 2.5F);
            float bob = (float)Math.sin(now * 1.15 + (double)i * 0.55) * (float)height * 0.0035F;
            float rise = (1.0F - easeOut(p)) * (float)height * 0.02F;
            float w = m[2] * s;
            float h = m[3] * s;
            float cx = left + m[0] * s + w / 2.0F;
            float cy = baseline + m[1] * s + bob + rise + h / 2.0F;
            TITLE[i].draw(context, cx - w * size / 2.0F, cy - h * size / 2.0F, w * size, h * size, w, alpha(16777215, fade));
         }
      }
   }

   static {
      for (int i = 0; i < CLOUD.length; i++) {
         CLOUD[i] = new SharpTexture("textures/menu/cloud_" + i + ".png");
      }

      for (int i = 0; i < TITLE.length; i++) {
         TITLE[i] = new SharpTexture("textures/menu/title_" + i + ".png");
      }

      Random random = new Random(1337L);

      for (float[] sparkle : SPARKLES) {
         sparkle[0] = random.nextFloat();
         sparkle[1] = random.nextFloat();
         sparkle[2] = 0.6F + random.nextFloat() * 1.4F;
         sparkle[3] = random.nextFloat() * 6.28F;
      }
   }
}
