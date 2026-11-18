package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.skin.SkinManager;
import dev.violet.skin.SkinMesh;
import dev.violet.skin.SkinRig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12122;
import net.minecraft.class_12249;
import net.minecraft.class_1306;
import net.minecraft.class_156;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix4f;

public class CustomSkin extends Module implements Presets {
   private static final float PI = 3.1415927F;
   private static final float PLAYER_SCALE = 0.9375F;
   private static final float SKIN_HEIGHT = 2.0F;
   private static final float SKIN_WIDTH = 1.0F;
   private static final int[] PART_COLORS = new int[]{0, -1522361, -1552305, -11563032, -11943297, -5870616};
   public final Setting<String> skin;
   public final Setting<Integer> size;
   public final Setting<Boolean> fitSkin;
   public final Setting<Integer> turn;
   public final Setting<Integer> height;
   public final Setting<Integer> detail;
   public final Setting<Boolean> showElytra;
   public final Setting<Boolean> animate;
   public final Setting<Boolean> animateArms;
   public final Setting<Boolean> animateLegs;
   public final Setting<Boolean> animateHead;
   public final Setting<Boolean> relaxArms;
   public final Setting<Integer> swing;
   public final Setting<Boolean> showParts;
   public final Setting<Boolean> openFolder;
   public final Setting<Boolean> reload;
   private static final int RING = 6;
   private SkinRig rig;
   private boolean rigRelaxed;
   private Object drawnState;
   private SkinRig poseRig;
   private float[][][] posePositions;
   private float[][][] poseNormals;
   private int[][] tinted;
   private final Map<int[], int[]> quadCache;
   private SkinMesh quadMesh;
   private int ring;
   private float elytraDy;
   private float elytraDz;

   public CustomSkin() {
      super("Custom Skin", "Shows your own 3D model instead of your player, with moving arms and legs. Only you can see it.", Category.VISUALS);
      this.group("Skin");
      this.skin = this.add(new StringSetting("Skin Model", "", "builtin/violet_cat.glb", true)).visible(() -> false);
      this.size = this.add(new IntSetting("Size", "Size in percent. 100 is exactly the size of a normal Minecraft skin.", 100, 30, 300));
      this.fitSkin = this.add(
         new BoolSetting(
            "Fit Skin Size", "Keeps the model inside the space of a normal Minecraft skin: as tall as a player and never wider than one with arms.", true
         )
      );
      this.turn = this.add(new IntSetting("Turn", "Turns the model in degrees if it looks the wrong way.", 0, 0, 359));
      this.height = this.add(new IntSetting("Height Offset", "Moves the model up or down in hundredths of a block.", 0, -100, 100));
      this.detail = this.add(new IntSetting("Detail", "Maximum triangles in thousands. Lower values load faster and run smoother.", 25, 5, 120));
      this.showElytra = this.add(new BoolSetting("Show Elytra", "Shows your elytra wings on the back of the model.", true));
      this.group("Animation");
      this.animate = this.add(
         new BoolSetting(
            "Animate",
            "Moves arms, legs and head like a normal player: walking, hitting, sneaking, looking around and riding. The model gets split into parts automatically.",
            true
         )
      );
      this.animateArms = this.add(new BoolSetting("Arms", "Swings the arms.", true)).visible(this.animate::get);
      this.animateLegs = this.add(new BoolSetting("Legs", "Moves the legs.", true)).visible(this.animate::get);
      this.animateHead = this.add(new BoolSetting("Head", "Turns the head where you look.", true)).visible(this.animate::get);
      this.relaxArms = this.add(new BoolSetting("Relax Arms", "Lowers arms that stick out to the side (T-pose) so they hang down and can swing.", true))
         .visible(this.animate::get);
      this.swing = this.add(new IntSetting("Swing Strength", "How far arms and legs swing while walking, in percent.", 100, 0, 200)).visible(this.animate::get);
      this.showParts = this.add(
            new BoolSetting(
               "Show Parts",
               "Colors the detected parts: head yellow, right arm red, left arm blue, right leg green, left leg purple. Helps to check the split.",
               false
            )
         )
         .visible(this.animate::get);
      this.group("Files");
      this.openFolder = this.add(new BoolSetting("Open Folder", "Opens config/violet/skins. Put .glb, .gltf, .obj or .stl files there.", false))
         .onChanged(v -> {
            if (v) {
               class_156.method_668().method_672(SkinManager.folder().toFile());
               this.openFolder.set(false);
            }
         });
      this.reload = this.add(new BoolSetting("Reload Skins", "Loads the skin files again.", false)).onChanged(v -> {
         if (v) {
            SkinManager.reload();
            this.rig = null;
            Notifications.push("Custom Skin", SkinManager.files().size() + " skins found.", Notifications.Type.SUCCESS);
            this.reload.set(false);
         }
      });
      this.quadCache = new IdentityHashMap<>();
   }

   public static boolean render(class_10055 state, class_4587 matrices, class_11659 queue, int overlay) {
      CustomSkin module = SoberClient.MODULES.get(CustomSkin.class);
      if (module == null) {
         return false;
      } else {
         module.drawnState = null;
         if (module.isActive() && !module.skin.get().isEmpty()) {
            if (mc.field_1724 != null && state.field_53528 == mc.field_1724.method_5628() && !state.field_53333 && !state.field_53542) {
               SkinMesh mesh = SkinManager.get(module.skin.get(), module.detail.get() * 1000);
               if (mesh == null) {
                  return false;
               } else {
                  module.draw(mesh, state, matrices, queue, overlay);
                  module.drawnState = state;
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static boolean hidesFeatures(Object state) {
      CustomSkin module = SoberClient.MODULES.get(CustomSkin.class);
      return module != null && state != null && module.drawnState == state;
   }

   public static boolean showsElytra() {
      CustomSkin module = SoberClient.MODULES.get(CustomSkin.class);
      return module != null && module.showElytra.get();
   }

   public static void elytraOffset(class_4587 matrices) {
      CustomSkin module = SoberClient.MODULES.get(CustomSkin.class);
      if (module != null) {
         matrices.method_46416(0.0F, module.elytraDy, module.elytraDz);
      }
   }

   private SkinRig rig(SkinMesh mesh) {
      boolean relax = this.relaxArms.get();
      if (this.rig == null || this.rig.mesh != mesh || this.rig.turn != this.turn.get() || this.rigRelaxed != relax) {
         this.rig = SkinRig.build(mesh, this.turn.get(), relax);
         this.rigRelaxed = relax;
      }

      return this.rig;
   }

   private void draw(SkinMesh mesh, class_10055 state, class_4587 matrices, class_11659 queue, int overlay) {
      SkinRig rig = this.rig(mesh);
      Matrix4f[] bones = this.animate.get() ? this.bones(rig, state) : null;
      boolean tint = this.animate.get() && this.showParts.get();
      int light = state.field_61820;
      float s = 2.0F;
      if (this.fitSkin.get() && rig.halfWidth * 2.0F * s > 1.0F) {
         s = 1.0F / Math.max(0.01F, rig.halfWidth * 2.0F);
      }

      s *= (float)this.size.get().intValue() / 100.0F;
      float offsetY = (float)this.height.get().intValue() / 100.0F / 0.9375F;
      this.elytraDy = 1.501F - (offsetY + s * rig.wingY);
      this.elytraDz = -(s * rig.backZ + 0.125F);
      matrices.method_22903();
      matrices.method_46416(0.0F, 1.501F, 0.0F);
      matrices.method_22905(-1.0F, -1.0F, 1.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F));
      matrices.method_46416(0.0F, (float)this.height.get().intValue() / 100.0F / 0.9375F, 0.0F);
      matrices.method_22905(s, s, s);
      float[][] affine = null;
      int slot = 0;
      if (bones != null) {
         affine = new float[bones.length][];

         for (int b = 0; b < bones.length; b++) {
            affine[b] = affine(bones[b]);
         }

         if (this.poseRig != rig) {
            this.poseRig = rig;
            this.posePositions = new float[6][mesh.parts.size()][];
            this.poseNormals = new float[6][mesh.parts.size()][];
            this.tinted = null;
         }

         slot = this.ring;
         this.ring = (this.ring + 1) % 6;
      }

      if (tint && this.tinted == null) {
         this.tinted = new int[mesh.parts.size()][];

         for (int i = 0; i < this.tinted.length; i++) {
            this.tinted[i] = tint(rig, i, mesh.parts.get(i).colors);
         }
      }

      for (int i = 0; i < mesh.parts.size(); i++) {
         SkinMesh.Part part = mesh.parts.get(i);
         if (part.texture() != null) {
            float[] uv = part.uvs;
            if (affine != null) {
               if (this.posePositions[slot][i] == null) {
                  this.posePositions[slot][i] = new float[rig.positions[i].length];
                  this.poseNormals[slot][i] = new float[rig.normals[i].length];
               }

               pose(rig, i, affine, this.posePositions[slot][i], this.poseNormals[slot][i]);
            }

            float[] p = affine != null ? this.posePositions[slot][i] : rig.positions[i];
            float[] n = affine != null ? this.poseNormals[slot][i] : rig.normals[i];
            int[] c = tint ? this.tinted[i] : part.colors;
            int[] q = this.quads(mesh, part.triangles);
            queue.method_73483(
               matrices,
               class_12249.method_75994(part.texture()),
               (entry, consumer) -> {
                  for (int v : q) {
                     consumer.method_56824(entry, p[v * 3], p[v * 3 + 1], p[v * 3 + 2])
                        .method_39415(c[v])
                        .method_22913(uv[v * 2], uv[v * 2 + 1])
                        .method_22922(overlay)
                        .method_60803(light)
                        .method_60831(entry, n[v * 3], n[v * 3 + 1], n[v * 3 + 2]);
                  }
               }
            );
         }
      }

      matrices.method_22909();
   }

   private int[] quads(SkinMesh mesh, int[] triangles) {
      if (this.quadMesh != mesh) {
         this.quadCache.clear();
         this.quadMesh = mesh;
      }

      return this.quadCache.computeIfAbsent(triangles, CustomSkin::pair);
   }

   private static long edge(int a, int b) {
      return (long)Math.min(a, b) << 32 | (long)Math.max(a, b);
   }

   private static int[] pair(int[] t) {
      int count = t.length / 3;
      int[] partner = new int[count];
      Arrays.fill(partner, -1);
      Map<Long, Integer> open = new HashMap<>();

      for (int i = 0; i < count; i++) {
         for (int e = 0; e < 3 && partner[i] < 0; e++) {
            Integer other = open.get(edge(t[i * 3 + e], t[i * 3 + (e + 1) % 3]));
            if (other != null && partner[other] < 0) {
               partner[i] = other;
               partner[other] = i;
            }
         }

         if (partner[i] < 0) {
            for (int ex = 0; ex < 3; ex++) {
               open.put(edge(t[i * 3 + ex], t[i * 3 + (ex + 1) % 3]), i);
            }
         }
      }

      int quads = 0;

      for (int i = 0; i < count; i++) {
         if (partner[i] < 0 || partner[i] > i) {
            quads++;
         }
      }

      int[] out = new int[quads * 4];
      int o = 0;

      for (int ix = 0; ix < count; ix++) {
         int j = partner[ix];
         if (j < 0 || j >= ix) {
            if (j < 0) {
               out[o++] = t[ix * 3];
               out[o++] = t[ix * 3 + 1];
               out[o++] = t[ix * 3 + 2];
               out[o++] = t[ix * 3 + 2];
            } else {
               for (int r = 0; r < 3; r++) {
                  int x = t[ix * 3 + r];
                  int y = t[ix * 3 + (r + 1) % 3];
                  int z = t[ix * 3 + (r + 2) % 3];
                  int d = -1;
                  int shared = 0;

                  for (int k = 0; k < 3; k++) {
                     int v = t[j * 3 + k];
                     if (v != x && v != y) {
                        d = v;
                     } else {
                        shared++;
                     }
                  }

                  if (shared == 2 && d >= 0) {
                     out[o++] = x;
                     out[o++] = d;
                     out[o++] = y;
                     out[o++] = z;
                     break;
                  }
               }
            }
         }
      }

      return o == out.length ? out : Arrays.copyOf(out, o);
   }

   private static int[] tint(SkinRig rig, int part, int[] colors) {
      int[] out = new int[colors.length];
      byte[] bones = rig.bones[part];
      float[] weights = rig.weights[part];

      for (int v = 0; v < out.length; v++) {
         int bone = bones[v];
         out[v] = bone == 0 ? colors[v] : mix(colors[v], PART_COLORS[bone], 0.75F * weights[v]);
      }

      return out;
   }

   private static int mix(int a, int b, float t) {
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return a & 0xFF000000
         | (int)((float)ar + (float)(br - ar) * t) << 16
         | (int)((float)ag + (float)(bg - ag) * t) << 8
         | (int)((float)ab + (float)(bb - ab) * t);
   }

   private static Matrix4f around(float[] pivot, Matrix4f rotation) {
      return new Matrix4f().translate(pivot[0], pivot[1], pivot[2]).mul(rotation).translate(-pivot[0], -pivot[1], -pivot[2]);
   }

   private static Matrix4f limb(float pitch, float yaw, float roll) {
      return new Matrix4f().rotateZ(-roll).rotateY(-yaw).rotateX(pitch);
   }

   private Matrix4f[] bones(SkinRig rig, class_10055 state) {
      float strength = (float)this.swing.get().intValue() / 100.0F;
      float g = state.field_53450;
      float h = state.field_53451 / Math.max(0.01F, state.field_53405);
      float headPitch = state.field_53448 * 3.1415927F / 180.0F;
      float headYaw = state.field_53447 * 3.1415927F / 180.0F;
      float glide = state.field_53411 ? class_3532.method_15363(state.method_64259(), 0.0F, 1.0F) : 0.0F;
      if (state.field_53411) {
         headPitch = rig.headStrength >= 1.0F ? -0.7853982F : class_3532.method_16439(glide, headPitch, -0.15F);
         headYaw *= 1.0F - 0.6F * glide;
      } else if (state.field_53403 > 0.0F) {
         headPitch = class_3532.method_61345(state.field_53403, headPitch, -0.7853982F);
      }

      float rArmP = class_3532.method_15362((double)(g * 0.6662F + 3.1415927F)) * h * strength;
      float lArmP = class_3532.method_15362((double)(g * 0.6662F)) * h * strength;
      float rArmY = 0.0F;
      float lArmY = 0.0F;
      float rArmR = 0.0F;
      float lArmR = 0.0F;
      float rLegP = class_3532.method_15362((double)(g * 0.6662F)) * 1.4F * h * strength;
      float lLegP = class_3532.method_15362((double)(g * 0.6662F + 3.1415927F)) * 1.4F * h * strength;
      float rLegY = 0.0F;
      float lLegY = 0.0F;
      float rLegR = 0.0F;
      float lLegR = 0.0F;
      if (state.field_53413) {
         rArmP -= 0.62831855F;
         lArmP -= 0.62831855F;
         rLegP = -1.4137167F;
         rLegY = 0.31415927F;
         rLegR = 0.07853982F;
         lLegP = -1.4137167F;
         lLegY = -0.31415927F;
         lLegR = -0.07853982F;
      }

      String right = state.field_55304.name();
      String left = state.field_55306.name();
      boolean twoHanded = false;
      if (right.equals("BOW_AND_ARROW") || left.equals("BOW_AND_ARROW") || right.startsWith("CROSSBOW") || left.startsWith("CROSSBOW")) {
         boolean mirror = left.equals("BOW_AND_ARROW") || left.startsWith("CROSSBOW");
         rArmY = mirror ? -0.1F + headYaw - 0.4F : -0.1F + headYaw;
         lArmY = mirror ? 0.1F + headYaw : 0.1F + headYaw + 0.4F;
         rArmP = -1.5707964F + headPitch;
         lArmP = -1.5707964F + headPitch;
         twoHanded = true;
      }

      if (!twoHanded) {
         float[] r = armPose(right, rArmP, rArmY, headPitch, headYaw, state.field_53410, true);
         rArmP = r[0];
         rArmY = r[1];
         float[] l = armPose(left, lArmP, lArmY, headPitch, headYaw, state.field_53410, false);
         lArmP = l[0];
         lArmY = l[1];
      }

      if (glide > 0.0F) {
         float flutter = class_3532.method_15374((double)(state.field_53328 * 0.35F)) * 0.05F;
         float kick = class_3532.method_15374((double)(state.field_53328 * 0.22F)) * 0.08F;
         rArmP = class_3532.method_16439(glide, rArmP, 0.18F + flutter);
         lArmP = class_3532.method_16439(glide, lArmP, 0.18F - flutter);
         rArmY = class_3532.method_16439(glide, rArmY, 0.0F);
         lArmY = class_3532.method_16439(glide, lArmY, 0.0F);
         rArmR = class_3532.method_16439(glide, rArmR, 0.62F + flutter);
         lArmR = class_3532.method_16439(glide, lArmR, -0.62F - flutter);
         rLegP = class_3532.method_16439(glide, rLegP, 0.1F + kick);
         lLegP = class_3532.method_16439(glide, lLegP, 0.1F - kick);
         rLegY = class_3532.method_16439(glide, rLegY, 0.0F);
         lLegY = class_3532.method_16439(glide, lLegY, 0.0F);
         rLegR = class_3532.method_16439(glide, rLegR, 0.09F);
         lLegR = class_3532.method_16439(glide, lLegR, -0.09F);
      }

      float bodyYaw = 0.0F;
      float f = state.field_63604;
      if (f > 0.0F) {
         bodyYaw = class_3532.method_15374((double)(class_3532.method_15355(f) * 3.1415927F * 2.0F)) * 0.2F;
         if (state.field_53408 == class_1306.field_6182) {
            bodyYaw = -bodyYaw;
         }

         rArmY += bodyYaw;
         lArmY += bodyYaw;
         lArmP += bodyYaw;
         if (state.field_63603 == class_12122.field_63399) {
            float eased = 1.0F - (1.0F - f) * (1.0F - f) * (1.0F - f) * (1.0F - f);
            float i = class_3532.method_15374((double)(eased * 3.1415927F));
            float j = class_3532.method_15374((double)(f * 3.1415927F)) * -(headPitch - 0.7F) * 0.75F;
            if (state.field_53408 == class_1306.field_6182) {
               lArmP -= i * 1.2F + j;
               lArmY += bodyYaw * 2.0F;
               lArmR += class_3532.method_15374((double)(f * 3.1415927F)) * -0.4F;
            } else {
               rArmP -= i * 1.2F + j;
               rArmY += bodyYaw * 2.0F;
               rArmR += class_3532.method_15374((double)(f * 3.1415927F)) * -0.4F;
            }
         }
      }

      float sway = 1.0F - glide;
      if (!right.equals("SPYGLASS")) {
         rArmR += (class_3532.method_15362((double)(state.field_53328 * 0.09F)) * 0.05F + 0.05F) * sway;
         rArmP += class_3532.method_15374((double)(state.field_53328 * 0.067F)) * 0.05F * sway;
      }

      if (!left.equals("SPYGLASS")) {
         lArmR -= (class_3532.method_15362((double)(state.field_53328 * 0.09F)) * 0.05F + 0.05F) * sway;
         lArmP -= class_3532.method_15374((double)(state.field_53328 * 0.067F)) * 0.05F * sway;
      }

      float leaning = state.field_53403;
      if (leaning > 0.0F) {
         lLegP = class_3532.method_16439(leaning, lLegP, 0.3F * class_3532.method_15362((double)(g * 0.33333334F + 3.1415927F)));
         rLegP = class_3532.method_16439(leaning, rLegP, 0.3F * class_3532.method_15362((double)(g * 0.33333334F)));
      }

      if (!this.animateArms.get()) {
         lArmR = 0.0F;
         rArmR = 0.0F;
         lArmY = 0.0F;
         rArmY = 0.0F;
         lArmP = 0.0F;
         rArmP = 0.0F;
      }

      if (!this.animateLegs.get()) {
         lLegR = 0.0F;
         rLegR = 0.0F;
         lLegY = 0.0F;
         rLegY = 0.0F;
         lLegP = 0.0F;
         rLegP = 0.0F;
      }

      float headScale = this.animateHead.get() ? rig.headStrength : 0.0F;
      float lean = state.field_53410 ? 0.45F : 0.0F;
      Matrix4f body = around(rig.hip, new Matrix4f().rotateY(-bodyYaw).rotateX(lean));
      return new Matrix4f[]{
         body,
         new Matrix4f(body).mul(around(rig.neck, new Matrix4f().rotateY(-headYaw * headScale).rotateX(headPitch * headScale))),
         new Matrix4f(body).mul(around(rig.armPivot[0], limb(rArmP, rArmY, rArmR).rotateZ(rig.armRest[0]))),
         new Matrix4f(body).mul(around(rig.armPivot[1], limb(lArmP, lArmY, lArmR).rotateZ(rig.armRest[1]))),
         around(rig.legPivot[0], limb(rLegP, rLegY, rLegR)),
         around(rig.legPivot[1], limb(lLegP, lLegY, lLegR))
      };
   }

   private static float[] armPose(String pose, float pitch, float yaw, float headPitch, float headYaw, boolean sneaking, boolean right) {
      return switch (pose) {
         case "ITEM" -> new float[]{pitch * 0.5F - 0.31415927F, 0.0F};
         case "BLOCK" -> new float[]{pitch * 0.5F - 0.9424779F, right ? -0.5235988F : 0.5235988F};
         case "THROW_TRIDENT", "SPEAR" -> new float[]{pitch * 0.5F - 3.1415927F, 0.0F};
         case "SPYGLASS" -> new float[]{
         class_3532.method_15363(headPitch - 1.9198622F - (sneaking ? 0.2617994F : 0.0F), -2.4F, 3.3F), headYaw + (right ? -0.2617994F : 0.2617994F)
      };
         case "TOOT_HORN" -> new float[]{class_3532.method_15363(headPitch, -1.2F, 1.2F) - 1.4835298F, headYaw + (right ? -0.5235988F : 0.5235988F)};
         case "BRUSH" -> new float[]{pitch * 0.5F - 0.62831855F, 0.0F};
         default -> new float[]{pitch, yaw};
      };
   }

   private static void pose(SkinRig rig, int part, float[][] m, float[] out, float[] outN) {
      float[] src = rig.positions[part];
      float[] srcN = rig.normals[part];
      byte[] boneOf = rig.bones[part];
      float[] weight = rig.weights[part];
      float[] upper = rig.upper[part];
      float[] body = m[0];
      int v = 0;

      for (int k = 0; v < src.length; k++) {
         float x = src[v];
         float y = src[v + 1];
         float z = src[v + 2];
         float nx = srcN[v];
         float ny = srcN[v + 1];
         float nz = srcN[v + 2];
         float px = x;
         float py = y;
         float pz = z;
         float qx = nx;
         float qy = ny;
         float qz = nz;
         float u = upper[k];
         if (u > 0.0F) {
            px = x + (tx(body, x, y, z) - x) * u;
            py = y + (ty(body, x, y, z) - y) * u;
            pz = z + (tz(body, x, y, z) - z) * u;
            qx = nx + (rx(body, nx, ny, nz) - nx) * u;
            qy = ny + (ry(body, nx, ny, nz) - ny) * u;
            qz = nz + (rz(body, nx, ny, nz) - nz) * u;
         }

         int bone = boneOf[k];
         float w = weight[k];
         if (bone != 0 && w > 0.0F) {
            float[] a = m[bone];
            px += (tx(a, x, y, z) - px) * w;
            py += (ty(a, x, y, z) - py) * w;
            pz += (tz(a, x, y, z) - pz) * w;
            qx += (rx(a, nx, ny, nz) - qx) * w;
            qy += (ry(a, nx, ny, nz) - qy) * w;
            qz += (rz(a, nx, ny, nz) - qz) * w;
         }

         float length = (float)Math.sqrt((double)(qx * qx + qy * qy + qz * qz));
         if (length > 1.0E-6F) {
            qx /= length;
            qy /= length;
            qz /= length;
         }

         out[v] = px;
         out[v + 1] = py;
         out[v + 2] = pz;
         outN[v] = qx;
         outN[v + 1] = qy;
         outN[v + 2] = qz;
         v += 3;
      }
   }

   private static float[] affine(Matrix4f m) {
      return new float[]{m.m00(), m.m01(), m.m02(), m.m10(), m.m11(), m.m12(), m.m20(), m.m21(), m.m22(), m.m30(), m.m31(), m.m32()};
   }

   private static float tx(float[] m, float x, float y, float z) {
      return m[0] * x + m[3] * y + m[6] * z + m[9];
   }

   private static float ty(float[] m, float x, float y, float z) {
      return m[1] * x + m[4] * y + m[7] * z + m[10];
   }

   private static float tz(float[] m, float x, float y, float z) {
      return m[2] * x + m[5] * y + m[8] * z + m[11];
   }

   private static float rx(float[] m, float x, float y, float z) {
      return m[0] * x + m[3] * y + m[6] * z;
   }

   private static float ry(float[] m, float x, float y, float z) {
      return m[1] * x + m[4] * y + m[7] * z;
   }

   private static float rz(float[] m, float x, float y, float z) {
      return m[2] * x + m[5] * y + m[8] * z;
   }

   @Override
   public int presetCount() {
      return 1 + SkinManager.files().size();
   }

   @Override
   public String presetName(int index) {
      if (index == 0) {
         return "None";
      } else {
         List<String> files = SkinManager.files();
         return index - 1 < files.size() ? SkinManager.title(files.get(index - 1)) : "";
      }
   }

   @Override
   public int presetColor(int index) {
      return index == 0 ? -9801856 : -2762274;
   }

   @Override
   public boolean presetActive(int index) {
      if (index == 0) {
         return this.skin.get().isEmpty();
      } else {
         List<String> files = SkinManager.files();
         return index - 1 < files.size() && files.get(index - 1).equals(this.skin.get());
      }
   }

   @Override
   public void selectPreset(int index) {
      if (index == 0) {
         this.skin.set("");
      } else {
         List<String> files = SkinManager.files();
         if (index - 1 < files.size()) {
            this.skin.set(files.get(index - 1));
            if (!this.isActive()) {
               this.setEnabled(true);
            }

            Notifications.push("Custom Skin", "Loading " + SkinManager.title(files.get(index - 1)) + "...", Notifications.Type.INFO);
         }
      }
   }

   @Override
   public String presetDescription() {
      return "Violet Cat, Violet Bot and Banana Buddy are built in. Your own 3D models go into config/violet/skins (.glb, .gltf, .obj or .stl). Use Open Folder below.";
   }

   @Override
   public boolean showInHud() {
      return false;
   }
}
