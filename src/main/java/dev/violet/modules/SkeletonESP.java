package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.BoneMesh;
import dev.violet.render.Color;
import dev.violet.render.Render3D;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10055;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_591;
import net.minecraft.class_630;
import org.joml.Matrix4f;

public class SkeletonESP extends Module {
   private static final double PIXEL = 0.05859375;
   private final Setting<SkeletonESP.Mode> mode = this.add(
      new EnumSetting<>(
         "Mode",
         "X-Ray shows a glowing 3D skeleton inside every player, on top of their normal skin. Bones draws simple bone lines through walls. Both shows both.",
         SkeletonESP.Mode.Skin
      )
   );
   private final Setting<Color> color = this.add(new ColorSetting("Color", "Color of the bones and the X-ray body.", new Color(170, 140, 255, 255)));
   private final Setting<Boolean> distanceColor = this.add(new BoolSetting("Distance Color", "Close players turn red, far ones stay in your color.", false));
   private final Setting<Integer> width = this.add(new IntSetting("Line Width", "Thickness of the bones in pixels.", 2, 1, 6));
   private final Setting<Boolean> body = this.add(new BoolSetting("X-Ray Body", "A see-through blue body around the bones, like an X-ray picture.", true));
   private final Setting<Boolean> glow = this.add(new BoolSetting("Glow", "Soft light around the bones and joints.", true));
   private final Setting<Boolean> joints = this.add(new BoolSetting("Joints", "Small dots on shoulders, hips, hands and feet.", true));
   private final Setting<Boolean> self = this.add(
      new BoolSetting("Self", "Also shows your own skeleton when you see yourself in third person (F5) or with Freecam.", true)
   );
   private final Setting<Boolean> throughWalls = this.add(new BoolSetting("Through Walls", "Bones are visible through blocks.", true));
   private static final Map<Integer, SkeletonESP.Capture> CAPTURES = new HashMap<>();
   private static BoneMesh[] meshes;

   public static void capture(class_591 model, class_10055 state, class_4587 matrices) {
      SkeletonESP module = SoberClient.MODULES.get(SkeletonESP.class);
      if (module != null && module.isActive() && module.mode.get() != SkeletonESP.Mode.Bones) {
         class_310 client = class_310.method_1551();
         if (client.field_1724 == null || state.field_53528 != client.field_1724.method_5628() || module.self.get()) {
            class_630[] parts = new class_630[]{model.field_3398, model.field_3391, model.field_3401, model.field_27433, model.field_3392, model.field_3397};
            Matrix4f[] out = new Matrix4f[parts.length];

            for (int i = 0; i < parts.length; i++) {
               if (parts[i].field_3665) {
                  matrices.method_22903();
                  parts[i].method_22703(matrices);
                  matrices.method_22905(0.0625F, 0.0625F, 0.0625F);
                  out[i] = new Matrix4f(matrices.method_23760().method_23761());
                  matrices.method_22909();
               }
            }

            CAPTURES.put(state.field_53528, new SkeletonESP.Capture(out));
         }
      }
   }

   @Override
   public void onDisable() {
      CAPTURES.clear();
   }

   private static int mixColor(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = Math.round((float)(a >> 16 & 0xFF) + (float)((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
      int g = Math.round((float)(a >> 8 & 0xFF) + (float)((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
      int bl = Math.round((float)(a & 0xFF) + (float)((b & 0xFF) - (a & 0xFF)) * t);
      return r << 16 | g << 8 | bl;
   }

   private void drawXray(Render3D renderer) {
      if (!CAPTURES.isEmpty()) {
         if (meshes == null) {
            meshes = BoneMesh.skeleton();
         }

         int base = this.color.get().packed() & 16777215;
         int core = mixColor(base, 16777215, 0.9F);
         int edge = mixColor(base, 16777215, 0.35F);
         int dark = 1709606;
         float fade = (float)(renderer.tint(-1) >>> 24) / 255.0F;
         renderer.setDepthTest(false);
         class_243 camera = renderer.cameraPos();

         for (SkeletonESP.Capture capture : CAPTURES.values()) {
            float[][] glows = new float[][]{{0.0F, -4.6F}, {1.0F, 3.5F}, {1.0F, 10.2F}};

            for (float[] spot : glows) {
               Matrix4f m = capture.parts()[(int)spot[0]];
               if (m != null) {
                  float x = m.m10() * spot[1] + m.m30();
                  float y = m.m11() * spot[1] + m.m31();
                  float z = m.m12() * spot[1] + m.m32();
                  Color center = Color.fromPacked(0xFF000000 | base);
                  renderer.billboardGlow(
                     camera.field_1352 + (double)x,
                     camera.field_1351 + (double)y,
                     camera.field_1350 + (double)z,
                     spot[0] == 0.0F ? 0.55 : 0.62,
                     center.withAlpha(Math.round(70.0F * fade)),
                     center.withAlpha(0),
                     20
                  );
               }
            }

            Matrix4f root = capture.parts()[1] != null ? capture.parts()[1] : capture.parts()[0];
            if (root == null || !(root.m30() * root.m30() + root.m31() * root.m31() + root.m32() * root.m32() > 9216.0F)) {
               drawCapture(renderer.relativeQuads(), capture, core, edge, dark, base, fade);
            }
         }
      }
   }

   private static void drawCapture(class_4588 buffer, SkeletonESP.Capture capture, int core, int edge, int dark, int base, float fade) {
      int total = 0;

      for (int i = 0; i < meshes.length; i++) {
         if (capture.parts()[i] != null) {
            total += meshes[i].quads;
         }
      }

      if (total != 0) {
         float[] pos = new float[total * 12];
         float[] nor = new float[total * 12];
         byte[] kind = new byte[total];
         float[] scale = new float[total];
         int q = 0;

         for (int ix = 0; ix < meshes.length; ix++) {
            Matrix4f m = capture.parts()[ix];
            if (m != null) {
               BoneMesh mesh = meshes[ix];
               float s = (float)Math.sqrt((double)(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02()));

               for (int k = 0; k < mesh.quads; q++) {
                  kind[q] = mesh.kinds[k];
                  scale[q] = s;

                  for (int v = 0; v < 4; v++) {
                     int o = k * 12 + v * 3;
                     int d = q * 12 + v * 3;
                     float x = mesh.positions[o];
                     float y = mesh.positions[o + 1];
                     float z = mesh.positions[o + 2];
                     pos[d] = m.m00() * x + m.m10() * y + m.m20() * z + m.m30();
                     pos[d + 1] = m.m01() * x + m.m11() * y + m.m21() * z + m.m31();
                     pos[d + 2] = m.m02() * x + m.m12() * y + m.m22() * z + m.m32();
                     float nx = mesh.normals[o];
                     float ny = mesh.normals[o + 1];
                     float nz = mesh.normals[o + 2];
                     float tx = m.m00() * nx + m.m10() * ny + m.m20() * nz;
                     float ty = m.m01() * nx + m.m11() * ny + m.m21() * nz;
                     float tz = m.m02() * nx + m.m12() * ny + m.m22() * nz;
                     float l = (float)Math.sqrt((double)(tx * tx + ty * ty + tz * tz));
                     if (l < 1.0E-6F) {
                        l = 1.0F;
                     }

                     nor[d] = tx / l;
                     nor[d + 1] = ty / l;
                     nor[d + 2] = tz / l;
                  }

                  k++;
               }
            }
         }

         int[] passes = new int[]{Math.round(18.0F * fade), Math.round(34.0F * fade)};
         float[] grow = new float[]{1.35F, 0.6F};

         for (int pass = 0; pass < 2; pass++) {
            int glowColor = passes[pass] << 24 | base;

            for (int ixx = 0; ixx < total; ixx++) {
               if (kind[ixx] == 0) {
                  float g = grow[pass] * scale[ixx];

                  for (int v = 0; v < 4; v++) {
                     int d = ixx * 12 + v * 3;
                     buffer.method_22912(pos[d] + nor[d] * g, pos[d + 1] + nor[d + 1] * g, pos[d + 2] + nor[d + 2] * g).method_39415(glowColor);
                  }
               }
            }
         }

         long[] order = new long[total];

         for (int ixxx = 0; ixxx < total; ixxx++) {
            int d = ixxx * 12;
            float cx = (pos[d] + pos[d + 3] + pos[d + 6] + pos[d + 9]) * 0.25F;
            float cy = (pos[d + 1] + pos[d + 4] + pos[d + 7] + pos[d + 10]) * 0.25F;
            float cz = (pos[d + 2] + pos[d + 5] + pos[d + 8] + pos[d + 11]) * 0.25F;
            long depth = Math.min(34359738367L, (long)((cx * cx + cy * cy + cz * cz) * 4096.0F));
            order[ixxx] = depth << 22 | (long)ixxx;
         }

         Arrays.sort(order);
         int alpha = Math.round(255.0F * fade) << 24;

         for (int o = total - 1; o >= 0; o--) {
            int ixxx = (int)(order[o] & 4194303L);

            for (int v = 0; v < 4; v++) {
               int d = ixxx * 12 + v * 3;
               float px = pos[d];
               float py = pos[d + 1];
               float pz = pos[d + 2];
               float l = (float)Math.sqrt((double)(px * px + py * py + pz * pz));
               float facing = l < 1.0E-5F ? 1.0F : Math.abs(nor[d] * px + nor[d + 1] * py + nor[d + 2] * pz) / l;
               int rgb = kind[ixxx] == 1
                  ? mixColor(dark, mixColor(dark, base, 0.3F), facing * 0.6F)
                  : mixColor(edge, core, (float)(Math.sqrt((double)facing) * Math.sqrt(Math.sqrt((double)facing))));
               buffer.method_22912(px, py, pz).method_39415(alpha | rgb);
            }
         }
      }
   }

   private boolean lines() {
      return this.mode.get() != SkeletonESP.Mode.Skin;
   }

   public SkeletonESP() {
      super("Player Xray", "Shows the glowing skeleton inside every player like an X-ray, through their skin and through walls.", Category.BASE_FINDING);
      this.distanceColor.visible(this::lines);
      this.width.visible(this::lines);
      this.body.visible(this::lines);
      this.glow.visible(this::lines);
      this.joints.visible(this::lines);
      this.throughWalls.visible(this::lines);
   }

   private static double[] pitchX(double x, double y, double z, double angle, double py, double pz) {
      double dy = y - py;
      double dz = z - pz;
      double c = Math.cos(angle);
      double s = Math.sin(angle);
      return new double[]{x, py + dy * c - dz * s, pz + dy * s + dz * c};
   }

   @Override
   public void onRender(Render3D renderer) {
      if (this.mode.get() != SkeletonESP.Mode.Bones) {
         this.drawXray(renderer);
      }

      CAPTURES.clear();
      if (mc.field_1687 != null && mc.field_1724 != null && this.lines()) {
         float delta = mc.method_61966().method_60637(true);
         renderer.setDepthTest(!this.throughWalls.get());
         boolean selfVisible = this.self.get() && (!mc.field_1690.method_31044().method_31034() || mc.method_1560() != mc.field_1724);

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player == mc.field_1724 ? selfVisible : !player.method_5767() || player.method_5851()) {
               Color c = this.color.get();
               if (this.distanceColor.get()) {
                  double t = Math.min(1.0, Math.sqrt(player.method_5858(mc.field_1724)) / 48.0);
                  c = new Color(255, 70, 90, c.a()).lerp(c, t);
               }

               this.drawSkeleton(renderer, player, delta, c);
            }
         }

         renderer.setLineWidth(1.0F);
         renderer.setDepthTest(false);
      }
   }

   private void drawSkeleton(Render3D renderer, class_1657 player, float delta, Color c) {
      float bodyYaw = class_3532.method_17821(delta, player.field_6220, player.field_6283);
      float headYaw = class_3532.method_17821(delta, player.field_6259, player.field_6241);
      float pitch = player.method_61414(delta);
      double yaw = Math.toRadians((double)bodyYaw);
      class_243 origin = player.method_30950(delta);
      SkeletonESP.Frame frame = new SkeletonESP.Frame(origin, -Math.cos(yaw), -Math.sin(yaw), -Math.sin(yaw), Math.cos(yaw));
      float progress = player.field_42108.method_48572(delta);
      float amplitude = Math.min(1.0F, player.field_42108.method_48570(delta));
      double rightLeg = (double)(class_3532.method_15362((double)(progress * 0.6662F)) * 1.4F * amplitude);
      double leftLeg = (double)(class_3532.method_15362((double)(progress * 0.6662F + 3.1415927F)) * 1.4F * amplitude);
      double rightArm = (double)(class_3532.method_15362((double)(progress * 0.6662F + 3.1415927F)) * amplitude);
      double leftArm = (double)(class_3532.method_15362((double)(progress * 0.6662F)) * amplitude);
      float swing = player.method_6055(delta);
      if (swing > 0.0F) {
         rightArm -= (double)class_3532.method_15374((double)(swing * 3.1415927F)) * 1.3;
      }

      boolean sneaking = player.method_18276();
      double lean = sneaking ? 0.5 : 0.0;
      double hipY = sneaking ? 9.6 : 12.0;
      double hipZ = sneaking ? -3.2 : 0.0;
      if (sneaking) {
         rightArm += 0.4;
         leftArm += 0.4;
      }

      boolean flat = player.method_6128() || player.method_20232();
      double tilt = flat ? Math.toRadians((double)(90.0F + pitch)) : 0.0;
      double[][] p = new double[14][];
      double ls = Math.sin(lean);
      double lc = Math.cos(lean);
      double neckY = hipY + 12.0 * lc;
      double neckZ = hipZ + 12.0 * ls;
      p[0] = new double[]{0.0, hipY, hipZ};
      p[1] = new double[]{0.0, neckY, neckZ};
      p[2] = new double[]{5.0, hipY + 10.0 * lc, hipZ + 10.0 * ls};
      p[3] = new double[]{-5.0, hipY + 10.0 * lc, hipZ + 10.0 * ls};
      p[4] = new double[]{6.0, p[2][1] - 10.0 * Math.cos(rightArm), p[2][2] - 10.0 * Math.sin(rightArm)};
      p[5] = new double[]{-6.0, p[3][1] - 10.0 * Math.cos(leftArm), p[3][2] - 10.0 * Math.sin(leftArm)};
      p[6] = new double[]{1.9, hipY, hipZ};
      p[7] = new double[]{-1.9, hipY, hipZ};
      p[8] = new double[]{1.9, hipY - 12.0 * Math.cos(rightLeg), hipZ - 12.0 * Math.sin(rightLeg)};
      p[9] = new double[]{-1.9, hipY - 12.0 * Math.cos(leftLeg), hipZ - 12.0 * Math.sin(leftLeg)};
      if (sneaking) {
         p[8][1] = Math.max(0.0, p[8][1] - 2.4);
         p[9][1] = Math.max(0.0, p[9][1] - 2.4);
      }

      double headTurn = Math.toRadians((double)class_3532.method_15393(headYaw - bodyYaw));
      double headPitch = Math.toRadians((double)pitch);
      double hx = Math.sin(headTurn) * Math.cos(headPitch);
      double hy = -Math.sin(headPitch);
      double hz = Math.cos(headTurn) * Math.cos(headPitch);
      double[] headCenter = new double[]{p[1][0], p[1][1] + 4.4, p[1][2]};
      p[10] = headCenter;
      p[11] = new double[]{headCenter[0] + hx * 5.5, headCenter[1] + hy * 5.5, headCenter[2] + hz * 5.5};
      if (tilt != 0.0) {
         for (int i = 0; i < 12; i++) {
            p[i] = pitchX(p[i][0], p[i][1], p[i][2], tilt, 6.0, 0.0);
         }
      }

      class_243[] w = new class_243[12];

      for (int i = 0; i < 12; i++) {
         w[i] = frame.at(p[i][0], p[i][1], p[i][2]);
      }

      if (flat) {
         class_243 shift = new class_243(0.0, -0.05156250000000001, 0.0);

         for (int i = 0; i < 12; i++) {
            w[i] = w[i].method_1019(shift);
         }
      }

      int[][] bones = new int[][]{{0, 1}, {2, 3}, {2, 4}, {3, 5}, {6, 7}, {6, 8}, {7, 9}, {1, 10}};
      if (this.body.get()) {
         class_243 camera = renderer.cameraPos();
         double perPixel = Render3D.pixelScale() * Math.max(0.5, camera.method_1022(w[0].method_1019(w[1]).method_1021(0.5)));
         int flesh = 48;
         renderer.setLineWidth((float)Math.min(90.0, Math.max(3.0, 0.46875 / perPixel)));
         line(renderer, w[0], w[1], c.withAlpha(flesh), c.withAlpha(flesh));
         renderer.setLineWidth((float)Math.min(60.0, Math.max(2.0, 0.234375 / perPixel)));

         for (int[] limb : new int[][]{{2, 4}, {3, 5}, {6, 8}, {7, 9}}) {
            line(renderer, w[limb[0]], w[limb[1]], c.withAlpha(flesh), c.withAlpha(flesh));
         }

         renderer.billboardGlow(w[10].field_1352, w[10].field_1351, w[10].field_1350, 0.3046875, c.withAlpha(flesh + 10), c.withAlpha(flesh - 10), 24);
      }

      Color core = c.withAlpha(255);
      Color soft = c.lerp(Color.WHITE, 0.55).withAlpha(255);
      if (this.glow.get()) {
         renderer.setLineWidth((float)this.width.get().intValue() + 7.0F);

         for (int[] bone : bones) {
            line(renderer, w[bone[0]], w[bone[1]], c.withAlpha(46), c.withAlpha(46));
         }
      }

      renderer.setLineWidth((float)this.width.get().intValue());

      for (int[] bone : bones) {
         line(renderer, w[bone[0]], w[bone[1]], soft, core);
      }

      double headRadius = 0.26953125;
      if (this.glow.get()) {
         renderer.billboardGlow(w[10].field_1352, w[10].field_1351, w[10].field_1350, headRadius * 2.1, c.withAlpha(70), c.withAlpha(0), 20);
      }

      renderer.billboardRing(w[10].field_1352, w[10].field_1351, w[10].field_1350, headRadius, soft, 20);
      renderer.setLineWidth(Math.max(1.0F, (float)this.width.get().intValue() - 0.5F));
      line(renderer, w[10], w[11], soft, c.withAlpha(0));
      if (this.joints.get()) {
         int[] dots = new int[]{2, 3, 4, 5, 6, 7, 8, 9};

         for (int i : dots) {
            if (this.glow.get()) {
               renderer.billboardGlow(w[i].field_1352, w[i].field_1351, w[i].field_1350, 0.1875, c.withAlpha(110), c.withAlpha(0), 12);
            }

            renderer.billboardGlow(w[i].field_1352, w[i].field_1351, w[i].field_1350, 0.064453125, Color.WHITE, soft.withAlpha(200), 10);
         }
      }
   }

   private static void line(Render3D renderer, class_243 a, class_243 b, Color from, Color to) {
      renderer.lineGradient(a.field_1352, a.field_1351, a.field_1350, b.field_1352, b.field_1351, b.field_1350, from, to);
   }

   private static record Capture(Matrix4f[] parts) {
   }

   private static record Frame(class_243 origin, double rx, double rz, double fx, double fz) {
      class_243 at(double x, double y, double z) {
         return new class_243(
            this.origin.field_1352 + (this.rx * x + this.fx * z) * 0.05859375,
            this.origin.field_1351 + y * 0.05859375,
            this.origin.field_1350 + (this.rz * x + this.fz * z) * 0.05859375
         );
      }
   }

   public static enum Mode {
      Skin("X-Ray"),
      Bones("Bones"),
      Both("Both");

      private final String title;

      private Mode(String title) {
         this.title = title;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
