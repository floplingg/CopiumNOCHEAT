package dev.violet.render.knife;

import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class KnifeSkin {
   public static final String[] NAMES = new String[]{"Butterfly", "Karambit", "Katana"};
   public static final int MAX_STREAKS = 2;
   public static final float[] REST = new float[16];
   public static final float SPARKLE_SIZE = 0.07F;
   private static final class_2960 WHITE = class_2960.method_60655("violet", "textures/models/white.png");
   private static final Vector3f SCABBARD_FALL = new Vector3f(-0.3F, -0.9F, 0.25F);
   private static final float SCABBARD_TURN = 0.6981317F;
   private static final float[][] BUTTERFLY_INSPECT = new float[][]{
      {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F},
      {0.08F, -0.07F, 0.09F, 0.05F, -8.0F, 8.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F},
      {0.14F, -0.09F, 0.12F, 0.06F, -12.0F, 8.0F, 14.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 12.0F},
      {0.19F, -0.09F, 0.12F, 0.06F, -10.0F, 10.0F, 2.0F, -18.0F, 0.0F, 90.0F, 180.0F, 0.0F, 12.0F},
      {0.24F, -0.09F, 0.13F, 0.06F, -12.0F, 8.0F, 10.0F, 0.0F, 0.0F, 180.0F, 360.0F, 0.0F, 12.0F},
      {0.29F, -0.09F, 0.12F, 0.06F, -10.0F, 10.0F, 2.0F, 18.0F, 0.0F, 270.0F, 540.0F, 0.0F, 12.0F},
      {0.34F, -0.09F, 0.12F, 0.06F, -12.0F, 8.0F, 14.0F, 0.0F, 0.0F, 360.0F, 720.0F, 0.0F, 12.0F},
      {0.39F, -0.09F, 0.12F, 0.06F, -10.0F, 10.0F, 2.0F, -18.0F, 0.0F, 450.0F, 900.0F, 0.0F, 12.0F},
      {0.44F, -0.09F, 0.13F, 0.06F, -12.0F, 8.0F, 10.0F, 0.0F, 0.0F, 540.0F, 1080.0F, 0.0F, 12.0F},
      {0.49F, -0.09F, 0.12F, 0.06F, -10.0F, 10.0F, 2.0F, 18.0F, 0.0F, 630.0F, 1260.0F, 0.0F, 12.0F},
      {0.54F, -0.09F, 0.12F, 0.06F, -12.0F, 8.0F, 14.0F, 0.0F, 0.0F, 720.0F, 1440.0F, 0.0F, 12.0F},
      {0.62F, -0.1F, 0.12F, 0.07F, -35.0F, 6.0F, 20.0F, 0.0F, 0.0F, 720.0F, 1440.0F, 0.0F, 0.0F},
      {0.76F, -0.1F, 0.13F, 0.07F, 25.0F, 6.0F, 16.0F, 0.0F, 0.0F, 720.0F, 1440.0F, 1.0F, 0.0F},
      {0.88F, -0.05F, 0.06F, 0.03F, 8.0F, 3.0F, 6.0F, 0.0F, 0.0F, 720.0F, 1440.0F, 1.0F, 0.0F},
      {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 720.0F, 1440.0F, 1.0F, 0.0F}
   };
   private static final float[][] BUTTERFLY_DEPLOY = new float[][]{
      {0.0F, 0.02F, -0.14F, 0.02F, 0.0F, -30.0F, -20.0F, 0.0F, 0.0F, 180.0F, 0.0F, 0.0F, 10.0F},
      {0.3F, 0.01F, -0.04F, 0.01F, 0.0F, -10.0F, -10.0F, 0.0F, 0.0F, 220.0F, 100.0F, 0.0F, 10.0F},
      {0.62F, 0.0F, 0.01F, 0.0F, 0.0F, 4.0F, 6.0F, 0.0F, 0.0F, 350.0F, 340.0F, 0.0F, 3.0F},
      {0.8F, 0.0F, 0.0F, 0.0F, 0.0F, -2.0F, -2.0F, 0.0F, 0.0F, 365.0F, 368.0F, 0.0F, 0.0F},
      {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 360.0F, 360.0F, 0.0F, 0.0F}
   };
   private static final float[][] KARAMBIT_INSPECT = new float[][]{
      {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F},
      {0.1F, -0.12F, 0.14F, 0.06F, -10.0F, 14.0F, 10.0F, 0.0F, 0.0F, -25.0F, 0.0F, 0.0F},
      {0.14F, -0.14F, 0.16F, 0.06F, -10.0F, 14.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F},
      {0.205F, -0.14F, 0.17F, 0.06F, -10.0F, 14.0F, 10.0F, 0.0F, 0.0F, 180.0F, 0.0F, 0.0F},
      {0.27F, -0.14F, 0.16F, 0.06F, -10.0F, 14.0F, 10.0F, 0.0F, 0.0F, 360.0F, 0.0F, 0.0F},
      {0.335F, -0.14F, 0.17F, 0.06F, -10.0F, 14.0F, 10.0F, 0.0F, 0.0F, 540.0F, 0.0F, 0.0F},
      {0.4F, -0.14F, 0.16F, 0.06F, -10.0F, 14.0F, 10.0F, 0.0F, 0.0F, 720.0F, 0.0F, 0.0F},
      {0.48F, -0.13F, 0.16F, 0.06F, -10.0F, 12.0F, 10.0F, 0.0F, 0.0F, 750.0F, 0.0F, 0.0F},
      {0.56F, -0.1F, 0.12F, 0.06F, 20.0F, 10.0F, -70.0F, 0.0F, 0.0F, 720.0F, 0.0F, 0.0F},
      {0.72F, -0.1F, 0.13F, 0.06F, -10.0F, 10.0F, -60.0F, 0.0F, 0.0F, 720.0F, 0.0F, 1.0F},
      {0.86F, -0.06F, 0.08F, 0.05F, -6.0F, 4.0F, 8.0F, 0.0F, 0.0F, 360.0F, 0.0F, 1.0F},
      {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 360.0F, 0.0F, 1.0F}
   };
   private static final float[][] KARAMBIT_DEPLOY = new float[][]{
      {0.0F, 0.02F, -0.14F, 0.02F, 0.0F, -30.0F, -20.0F, 0.0F, 0.0F, -540.0F, 0.0F, 0.0F},
      {0.55F, 0.0F, 0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, -60.0F, 0.0F, 0.0F},
      {0.78F, 0.0F, 0.0F, 0.0F, 0.0F, -2.0F, -2.0F, 0.0F, 0.0F, 12.0F, 0.0F, 0.0F},
      {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F}
   };
   private static final float[][] KATANA_INSPECT = new float[][]{
      {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 4.0F},
      {0.1F, -0.06F, 0.12F, 0.06F, 0.0F, 0.0F, 60.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 4.0F},
      {0.15F, -0.07F, 0.14F, 0.08F, -2.0F, 0.0F, 60.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 4.0F},
      {0.2F, -0.07F, 0.14F, 0.08F, -2.0F, 0.0F, 60.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 12.0F},
      {0.36F, -0.07F, 0.14F, 0.08F, 0.0F, 0.0F, 58.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 44.0F},
      {0.4F, -0.07F, 0.14F, 0.08F, 0.0F, 0.0F, 58.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 53.0F},
      {0.44F, -0.06F, 0.13F, 0.06F, 2.0F, 1.0F, 54.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 58.0F},
      {0.48F, -0.05F, 0.11F, 0.03F, 4.0F, 2.0F, 42.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F},
      {0.52F, -0.04F, 0.1F, 0.02F, 0.0F, 4.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F},
      {0.6F, -0.04F, 0.12F, 0.02F, 0.0F, 4.0F, -160.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F},
      {0.68F, -0.04F, 0.1F, 0.02F, 0.0F, 4.0F, -340.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F},
      {0.78F, 0.04F, -0.03F, 0.0F, 0.0F, -6.0F, -400.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F},
      {0.88F, 0.0F, 0.02F, 0.0F, 0.0F, 2.0F, -365.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F},
      {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -360.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 62.0F}
   };
   private static final float[][] KATANA_DEPLOY = new float[][]{
      {0.0F, 0.06F, 0.0F, 0.02F, -10.0F, 0.0F, 45.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F},
      {0.05F, 0.05F, 0.01F, 0.01F, -12.0F, 0.0F, 47.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F},
      {0.18F, 0.15F, 0.0F, 0.05F, -10.0F, 0.0F, 45.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.3F, 0.0F, 16.0F, 1.0F, 0.0F},
      {0.3F, 0.3F, -0.02F, 0.1F, -8.0F, 0.0F, 43.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.9F, 0.0F, 50.0F, 1.0F, 0.0F},
      {0.33F, 0.3F, 0.0F, 0.1F, -4.0F, 4.0F, 36.0F, 180.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 52.0F, 1.0F, 0.0F},
      {0.41F, 0.08F, 0.12F, 0.04F, 0.0F, 10.0F, -40.0F, 180.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 52.0F, 0.25F, 0.0F},
      {0.47F, -0.02F, 0.1F, 0.02F, 5.0F, 8.0F, -100.0F, 180.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 52.0F, 0.0F, 0.0F},
      {0.62F, -0.02F, 0.08F, 0.02F, 0.0F, 6.0F, -460.0F, 180.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 52.0F, 0.0F, 0.0F},
      {0.71F, 0.04F, -0.04F, 0.0F, 0.0F, -6.0F, -520.0F, 180.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 52.0F, 0.0F, 0.0F},
      {0.82F, 0.0F, 0.02F, 0.0F, 0.0F, 2.0F, -370.0F, 300.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 52.0F, 0.0F, 0.0F},
      {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -360.0F, 360.0F, 0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 52.0F, 0.0F, 0.0F}
   };
   private static KnifeSkin butterfly;
   private static KnifeSkin karambit;
   private static KnifeSkin katana;
   public final String name;
   public final InspectClip inspect;
   public final InspectClip deploy;
   private final KnifeSkin.Part[] parts;
   private final KnifeSkin.Streak[] streaks;
   private final float voxel;
   private final float gripX;
   private final float gripY;
   private final Vector3f fist;
   private final Quaternionf rest;
   private KnifeMesh scabbard;
   private float saddleX;
   private float saddleY;
   private float[] letGo;
   private KnifeSkin.Centre centre;
   private float bladeFrom;
   private float bladeTo;

   private KnifeSkin(
      String name,
      KnifeSkin.Part[] parts,
      KnifeSkin.Streak[] streaks,
      float voxel,
      float gripX,
      float gripY,
      Vector3f fist,
      Vector3f blade,
      Vector3f spine,
      InspectClip inspect,
      InspectClip deploy
   ) {
      this.name = name;
      this.parts = parts;
      this.streaks = streaks;
      this.voxel = voxel;
      this.gripX = gripX;
      this.gripY = gripY;
      this.fist = fist;
      this.inspect = inspect;
      this.deploy = deploy;
      Vector3f x = new Vector3f(blade).normalize();
      Vector3f y = new Vector3f(spine).fma(-spine.dot(x), x).normalize();
      this.rest = new Quaternionf().setFromNormalized(new Matrix3f(x, y, new Vector3f(x).cross(y)));
   }

   public static KnifeSkin of(String name) {
      return switch (name) {
         case "Karambit" -> karambit != null ? karambit : (karambit = karambit());
         case "Katana" -> katana != null ? katana : (katana = katana());
         default -> butterfly != null ? butterfly : (butterfly = butterfly());
      };
   }

   private static KnifeSkin butterfly() {
      float pivotX = -1.5F;
      float pivotY = 0.5F;
      KnifeSkin.Part[] parts = new KnifeSkin.Part[]{
         new KnifeSkin.Part(KnifeModels.butterflyHandle(1), -1, pivotX, pivotY),
         new KnifeSkin.Part(KnifeModels.butterflyBlade(), 8, pivotX, pivotY),
         new KnifeSkin.Part(KnifeModels.butterflyHandle(-1), 9, pivotX, pivotY)
      };
      KnifeSkin.Streak[] streaks = new KnifeSkin.Streak[]{
         new KnifeSkin.Streak(1, 2.5F, 0.5F, 21.5F, -0.5F, 1.0F), new KnifeSkin.Streak(2, -4.0F, 0.5F, -27.5F, 0.5F, 0.8F)
      };
      return new KnifeSkin(
         "Butterfly",
         parts,
         streaks,
         0.021F,
         -10.0F,
         0.5F,
         new Vector3f(-0.06F, 0.06F, 0.02F),
         new Vector3f(-0.15F, 1.0F, -0.1F),
         new Vector3f(1.0F, 0.15F, 0.3F),
         new InspectClip(BUTTERFLY_INSPECT, 3.8F, 0.14F, 0.54F),
         new InspectClip(BUTTERFLY_DEPLOY, 0.9F)
      );
   }

   private static KnifeSkin karambit() {
      float ringX = -16.0F;
      float ringY = -1.0F;
      KnifeSkin.Part[] parts = new KnifeSkin.Part[]{new KnifeSkin.Part(KnifeModels.karambit(), 8, ringX, ringY)};
      float[] tip = KnifeModels.KARAMBIT_TIP;
      int k = 2;
      KnifeSkin.Streak[] streaks = new KnifeSkin.Streak[]{new KnifeSkin.Streak(0, 6.0F * (float)k, 0.5F * (float)k, tip[0], tip[1], 1.0F)};
      return new KnifeSkin(
         "Karambit",
         parts,
         streaks,
         0.022F / (float)k,
         ringX + 2.0F,
         ringY,
         new Vector3f(-0.06F, 0.1F, 0.02F),
         new Vector3f(-0.5F, -0.8F, -0.3F),
         new Vector3f(0.8F, -0.5F, 0.35F),
         new InspectClip(KARAMBIT_INSPECT, 3.2F, 0.14F, 0.4F),
         new InspectClip(KARAMBIT_DEPLOY, 0.8F)
      );
   }

   private static KnifeSkin katana() {
      KnifeSkin.Part[] parts = new KnifeSkin.Part[]{new KnifeSkin.Part(KnifeModels.katana(), -1, 0.0F, 0.0F)};
      KnifeSkin.Streak[] streaks = new KnifeSkin.Streak[]{
         new KnifeSkin.Streak(0, 6.0F, (float)KnifeModels.katanaBow(6) + 0.5F, 48.5F, (float)KnifeModels.katanaBow(48) + 1.5F, 1.0F)
      };
      KnifeSkin skin = new KnifeSkin(
         "Katana",
         parts,
         streaks,
         0.028F,
         -5.0F,
         0.5F,
         new Vector3f(-0.06F, 0.02F, 0.02F),
         new Vector3f(-0.6F, 0.8F, -0.2F),
         new Vector3f(0.6F, 0.45F, 0.1F),
         new InspectClip(KATANA_INSPECT, 4.4F, 0.52F, 0.68F),
         new InspectClip(KATANA_DEPLOY, 2.0F)
      );
      skin.scabbard = KnifeModels.katanaScabbard();
      skin.saddleX = 20.0F;
      skin.saddleY = 0.0F;
      skin.centre = x -> x < 3.0F ? 0.5F : KnifeModels.katanaCentre(x);
      skin.bladeFrom = 3.0F;
      skin.bladeTo = 49.0F;
      skin.letGo = skin.findLetGo();
      return skin;
   }

   private float[] findLetGo() {
      float[] at = new float[16];
      float[] held = new float[16];

      for (int i = 0; i <= 2000; i++) {
         this.deploy.sample((float)i / 2000.0F, at);
         if (at[13] < 1.0F) {
            break;
         }

         System.arraycopy(at, 0, held, 0, 16);
      }

      return held;
   }

   public int streakCount() {
      return this.streaks.length;
   }

   public float streakStrength(int streak) {
      return this.streaks[streak].strength;
   }

   public static KnifeSkin.Sink sink(class_11659 queue, int light, KnifeMaterial material, boolean foil) {
      final class_1921 solid = class_12249.method_75994(WHITE);
      final int[] palette = material.palette;
      return new KnifeSkin.Sink() {
         @Override
         public void part(class_4587 part, KnifeMesh mesh, KnifeMesh.Shine shine) {
            queue.method_73483(part, solid, (entry, out) -> mesh.emit(entry, out, palette, light, shine));
            if (foil) {
               queue.method_73529(1)
                  .method_73483(part, class_12249.method_75997(), (entry, out) -> mesh.emit(entry, out, palette, light, KnifeMesh.Shine.NONE));
            }
         }

         @Override
         public void glow(class_4587 part, KnifeMesh mesh, KnifeMesh.Shine shine) {
            queue.method_73529(1)
               .method_73483(part, class_12249.method_76014(KnifeSkin.WHITE), (entry, out) -> mesh.emitGlow(entry, out, material.glow, shine));
         }
      };
   }

   public void draw(class_4587 pose, float[] at, float weight, float size, KnifeSkin.Sink sink, float[] trail) {
      Matrix4f knife = this.knifeMatrix(at, weight, size, new Matrix4f());
      Matrix4f sheath = this.scabbard != null && weight >= 0.999F && at[13] > 0.01F ? this.scabbardMatrix(at, size) : null;
      KnifeMesh.Shine wipe = this.wipe(at, weight);
      KnifeMesh.Shine shine = wipe;
      if (wipe == null) {
         float sweep = shinePosition(at[10]);
         shine = new KnifeMesh.Shine(sweep, sweep > 0.0F ? class_3532.method_15374((double)(3.1415927F * sweep)) * weight : 0.0F, 0.0F);
      }

      pose.method_22903();
      pose.method_34425(knife);

      for (int i = 0; i < this.parts.length; i++) {
         KnifeSkin.Part part = this.parts[i];
         pose.method_22903();
         if (part.channel >= 0) {
            float angle = class_3532.method_15393(at[part.channel]) * weight;
            if (angle != 0.0F) {
               pose.method_49278(class_7833.field_40718.rotationDegrees(angle), part.pivotX, part.pivotY, 0.5F);
            }
         }

         sink.part(pose, part.mesh, shine);
         if (wipe != null) {
            sink.glow(pose, part.mesh, wipe);
         }

         if (trail != null) {
            this.streaks(i, pose.method_23760().method_23761(), trail);
         }

         pose.method_22909();
      }

      pose.method_22909();
      if (sheath != null) {
         pose.method_22903();
         pose.method_34425(sheath);
         sink.part(pose, this.scabbard, KnifeMesh.Shine.NONE);
         pose.method_22909();
      }
   }

   public void drawHeld(class_4587 pose, float size, KnifeSkin.Sink sink) {
      float scale = this.voxel * 1.15F * size;
      pose.method_22903();
      pose.method_22905(scale, scale, scale);
      pose.method_46416(-this.gripX, -this.gripY, -0.5F);

      for (KnifeSkin.Part part : this.parts) {
         sink.part(pose, part.mesh, KnifeMesh.Shine.NONE);
      }

      pose.method_22909();
   }

   private void streaks(int part, Matrix4f m, float[] trail) {
      Vector3f p = new Vector3f();

      for (int s = 0; s < this.streaks.length; s++) {
         KnifeSkin.Streak streak = this.streaks[s];
         if (streak.part == part) {
            m.transformPosition(streak.baseX, streak.baseY, 0.5F, p);
            trail[s * 6] = p.x;
            trail[s * 6 + 1] = p.y;
            trail[s * 6 + 2] = p.z;
            m.transformPosition(streak.tipX, streak.tipY, 0.5F, p);
            trail[s * 6 + 3] = p.x;
            trail[s * 6 + 4] = p.y;
            trail[s * 6 + 5] = p.z;
         }
      }
   }

   private Matrix4f knifeMatrix(float[] at, float weight, float size, Matrix4f dest) {
      float turn = 0.017453292F * weight;
      float scale = this.voxel * size;
      dest.translation(this.fist.x + at[0] * weight, this.fist.y + at[1] * weight, this.fist.z + at[2] * weight)
         .rotate(
            new Quaternionf()
               .rotateY(class_3532.method_15393(at[3]) * turn)
               .rotateX(class_3532.method_15393(at[4]) * turn)
               .rotateZ(class_3532.method_15393(at[5]) * turn)
         )
         .rotate(this.rest);
      float flip = class_3532.method_15393(at[6]) * turn;
      if (flip != 0.0F) {
         dest.rotateX(flip);
      }

      return dest.scale(scale).translate(at[11] * weight - this.gripX, -this.gripY, -0.5F);
   }

   private Matrix4f scabbardMatrix(float[] at, float size) {
      boolean held = at[13] >= 1.0F;
      float[] state = held ? at : this.letGo;
      float radius = 312.5F;
      Matrix4f m = this.knifeMatrix(state, 1.0F, size, new Matrix4f())
         .rotateAround(new Quaternionf().rotationZ(state[12] / radius), 3.0F, radius, 0.5F)
         .translate(0.05F, 0.0F, 0.0F);
      if (!held) {
         float fall = 1.0F - Math.max(at[13], 0.0F);
         fall *= fall;
         Vector3f hold = m.transformPosition(this.saddleX, this.saddleY, 0.5F, new Vector3f());
         m.translateLocal(-hold.x, -hold.y, -hold.z)
            .rotateLocalZ(0.6981317F * fall)
            .translateLocal(hold.x + SCABBARD_FALL.x * fall, hold.y + SCABBARD_FALL.y * fall, hold.z + SCABBARD_FALL.z * fall);
      }

      return m;
   }

   private KnifeMesh.Shine wipe(float[] at, float weight) {
      float touch = this.centre != null ? class_3532.method_15363((at[14] * weight - 0.7F) / 0.3F, 0.0F, 1.0F) : 0.0F;
      if (!(touch <= 0.01F) && !(at[15] < this.bladeFrom)) {
         float along = (at[15] - this.bladeFrom) / (this.bladeTo - this.bladeFrom);
         return new KnifeMesh.Shine(along, touch * touch * (3.0F - 2.0F * touch), 16.0F);
      } else {
         return null;
      }
   }

   public float sparkle(float[] at, float weight) {
      float flash = sparkle(at[10]);
      if (this.centre != null && at[15] >= this.bladeFrom) {
         float along = (at[15] - this.bladeFrom) / (this.bladeTo - this.bladeFrom);
         float leaving = class_3532.method_15363((along - 0.96F) / 0.06F, 0.0F, 1.0F) * class_3532.method_15363((1.3F - along) / 0.15F, 0.0F, 1.0F);
         flash = Math.max(flash, leaving * class_3532.method_15363(at[14] * 1.6F, 0.0F, 1.0F));
      }

      return flash * weight;
   }

   static float shinePosition(float channel) {
      if (channel <= 0.0F) {
         return 0.0F;
      } else {
         float f = channel - 2.0F * (float)class_3532.method_15375(channel / 2.0F);
         return f < 1.0F ? f : 0.0F;
      }
   }

   static float sparkle(float channel) {
      float f = shinePosition(channel);
      return class_3532.method_15363((f - 0.72F) / 0.16F, 0.0F, 1.0F) * class_3532.method_15363((1.0F - f) / 0.07F, 0.0F, 1.0F);
   }

   public void glow(class_11659 queue, Matrix4f base, Matrix3f baseNormal, SlashTrail[] trails, long now, long lifetime, int rgb, float flash, float[] tip) {
      class_4587 hand = new class_4587();
      hand.method_23760().method_23761().set(base);
      hand.method_23760().method_23762().set(baseNormal);
      float tipX = tip[3];
      float tipY = tip[4];
      float tipZ = tip[5];
      float turn = (float)(now % 6283185307L) / 1.0E9F * 1.5F;
      int count = this.streakCount();
      queue.method_73529(1).method_73483(hand, class_12249.method_76014(WHITE), (entry, out) -> {
         for (int s = 0; s < count; s++) {
            trails[s].emit(entry, out, now, lifetime, rgb, this.streakStrength(s));
         }

         if (flash > 0.0F) {
            SlashTrail.sparkle(entry, out, tipX, tipY, tipZ, 0.07F * flash, turn, rgb, flash);
         }
      });
   }

   private interface Centre {
      float y(float var1);
   }

   private static record Part(KnifeMesh mesh, int channel, float pivotX, float pivotY) {
   }

   public interface Sink {
      void part(class_4587 var1, KnifeMesh var2, KnifeMesh.Shine var3);

      void glow(class_4587 var1, KnifeMesh var2, KnifeMesh.Shine var3);
   }

   private static record Streak(int part, float baseX, float baseY, float tipX, float tipY, float strength) {
   }
}
