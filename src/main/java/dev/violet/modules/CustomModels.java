package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.Notifications;
import dev.violet.gui.Theme;
import dev.violet.model.ModelIcon;
import dev.violet.model.ModelLibrary;
import dev.violet.model.ModelManager;
import dev.violet.model.ModelMesh;
import dev.violet.model.SpriteModel;
import dev.violet.model.WaterIcon;
import dev.violet.model.WaterModels;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.module.Presets;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.ItemSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3489;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import org.joml.Quaternionf;

public class CustomModels extends Module implements Presets {
   private static final class_1921 LAYER = class_12249.method_75994(class_2960.method_60655("violet", "textures/models/white.png"));
   public final Setting<String> model;
   public final Setting<String> lastModel;
   public final Setting<CustomModels.Replace> replace;
   public final ItemSetting item;
   public final Setting<Integer> size;
   public final Setting<Boolean> flip;
   public final Setting<Boolean> firstPerson;
   public final Setting<Boolean> thirdPerson;
   public final Setting<Boolean> inventoryIcon;
   public final Setting<Color> waterColor;
   public final Setting<Boolean> waterGlow;
   public final Setting<Integer> offsetX;
   public final Setting<Integer> offsetY;
   public final Setting<Integer> offsetZ;
   public final Setting<CustomModels.ColorMode> colorMode;
   public final Setting<Boolean> shine;
   public final Setting<Color> headColor;
   public final Setting<Color> handleColor;
   public final Setting<Integer> headSize;
   public final Setting<Boolean> openFolder;
   public final Setting<Boolean> reload;

   public CustomModels() {
      super("Custom Models", "Swaps pickaxes or any item for your own 3D models or item packs like Fortnite Pack and Watery.", Category.VISUALS);
      this.group("Model");
      this.model = this.add(new StringSetting("Model", "", "", true)).visible(() -> false);
      this.lastModel = this.add(new StringSetting("Last Model", "", "", true)).visible(() -> false);
      this.replace = this.add(
         new EnumSetting<>("Replace", "Which items get the model: all pickaxes, one item of your choice, or both.", CustomModels.Replace.Pickaxes)
      );
      this.item = (ItemSetting)this.add(
            new ItemSetting("Item", "Item that gets the model. Click and type its name, for example netherite_sword.", class_1802.field_8377)
         )
         .visible(() -> this.replace.get() != CustomModels.Replace.Pickaxes);
      this.size = this.add(new IntSetting("Size", "Size of the model in percent.", 75, 30, 200));
      this.flip = this.add(new BoolSetting("Flip", "Mirrors the model if the head points the wrong way.", false));
      this.firstPerson = this.add(new BoolSetting("First Person", "Shows the model in your hand.", true));
      this.thirdPerson = this.add(new BoolSetting("Third Person", "Shows the model on players in third person.", true));
      this.inventoryIcon = this.add(new BoolSetting("Inventory Icon", "Shows the model as the item icon in your hotbar and inventories.", true));
      this.group("Watery");
      this.waterColor = this.add(
            new ColorSetting("Watery Color", "Color of the Watery Sword, Pickaxe, Shovel, Trident and Totem.", new Color(40, 110, 255, 255))
         )
         .visible(() -> ModelManager.isWatery(this.model.get()));
      this.waterGlow = this.add(new BoolSetting("Watery Glow", "A soft glowing rim around the Watery items.", true))
         .visible(() -> ModelManager.isWatery(this.model.get()));
      this.group("Position");
      this.offsetX = this.add(new IntSetting("Offset X", "Moves the model left or right in first person.", 0, -60, 60));
      this.offsetY = this.add(new IntSetting("Offset Y", "Moves the model up or down in first person.", 0, -60, 60));
      this.offsetZ = this.add(new IntSetting("Offset Z", "Moves the model closer or further away in first person.", 0, -60, 60));
      this.group("Colors");
      this.colorMode = this.add(
         new EnumSetting<>(
            "Color Mode",
            "FNCS look, two colors for head and handle, one color, your sky, rainbow, or the colors stored in the model.",
            CustomModels.ColorMode.FNCS
         )
      );
      this.shine = this.add(new BoolSetting("Shine", "A metallic shine runs over the model.", true));
      this.headColor = this.add(new ColorSetting("Head Color", "Color of the pickaxe head.", new Color(232, 64, 76, 255)))
         .visible(() -> this.colorMode.get() == CustomModels.ColorMode.TwoTone || this.colorMode.get() == CustomModels.ColorMode.Single);
      this.handleColor = this.add(new ColorSetting("Handle Color", "Color of the handle.", new Color(244, 244, 250, 255)))
         .visible(() -> this.colorMode.get() == CustomModels.ColorMode.TwoTone);
      this.headSize = this.add(new IntSetting("Head Size", "How much of the model counts as the head, in percent.", 35, 5, 95))
         .visible(() -> this.colorMode.get() == CustomModels.ColorMode.TwoTone || this.colorMode.get() == CustomModels.ColorMode.Sky);
      this.group("Files");
      this.openFolder = this.add(new BoolSetting("Open Folder", "Opens config/violet/models. Put .stl or .obj files there.", false)).onChanged(v -> {
         if (v) {
            class_156.method_668().method_672(ModelManager.folder().toFile());
            this.openFolder.set(false);
         }
      });
      this.reload = this.add(new BoolSetting("Reload Models", "Loads the model files again.", false)).onChanged(v -> {
         if (v) {
            ModelManager.reload();
            Notifications.push("Custom Models", ModelManager.files().size() + " models found.", Notifications.Type.SUCCESS);
            this.reload.set(false);
         }
      });
   }

   @Override
   public void onEnable() {
      if (this.model.get().isEmpty() && !this.lastModel.get().isEmpty()) {
         this.model.set(this.lastModel.get());
      }
   }

   @Override
   public void onDisable() {
      this.lastModel.set(this.model.get());
      this.model.set("");
   }

   private ModelMesh mesh() {
      return ModelManager.get(this.model.get());
   }

   private static String itemId(class_1799 stack) {
      return class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
   }

   private SpriteModel sprite(class_1799 stack) {
      String name = this.model.get();
      if (ModelManager.isPack(name)) {
         return ModelManager.packSprite(name, itemId(stack));
      } else {
         return ModelManager.isSprite(name) ? ModelManager.sprite(name) : null;
      }
   }

   public boolean matches(class_1799 stack) {
      if (stack.method_7960() || this.model.get().isEmpty()) {
         return false;
      } else if (ModelManager.isPack(this.model.get())) {
         return ModelManager.packHas(this.model.get(), itemId(stack));
      } else {
         return switch ((CustomModels.Replace)this.replace.get()) {
            case Pickaxes -> stack.method_31573(class_3489.field_42614);
            case Item -> stack.method_31574(this.item.get());
            case Both -> stack.method_31573(class_3489.field_42614) || stack.method_31574(this.item.get());
         };
      }
   }

   private boolean renderWater(class_1799 stack, boolean first, boolean left, boolean throwing, class_4587 matrices, class_11659 queue, int light) {
      WaterModels.Kind kind = WaterModels.kind(itemId(stack));
      if (kind != null && WaterModels.ready()) {
         boolean totem = kind == WaterModels.Kind.TOTEM;
         float s = (float)this.size.get().intValue() / 75.0F;
         matrices.method_22903();
         if (kind == WaterModels.Kind.TRIDENT) {
            if (first) {
               matrices.method_46416(
                  (float)this.offsetX.get().intValue() / 100.0F, (float)this.offsetY.get().intValue() / 100.0F, (float)this.offsetZ.get().intValue() / 100.0F
               );
               if (left) {
                  vanilla(matrices, true, 13.0F, 17.0F, 1.0F, 0.0F, 90.0F, -25.0F);
               } else {
                  vanilla(matrices, false, -3.0F, 17.0F, 1.0F, 0.0F, -90.0F, 25.0F);
               }
            } else if (throwing) {
               vanilla(matrices, left, 8.0F, -17.0F, left ? -7.0F : 9.0F, 0.0F, 90.0F, 180.0F);
            } else {
               vanilla(matrices, left, left ? 3.0F : 11.0F, 17.0F, left ? 12.0F : -2.0F, 0.0F, 60.0F, 0.0F);
            }

            float k = 1.41F * s;
            matrices.method_46416(0.0F, -1.6875F + k * 0.035F, 0.0F);
            matrices.method_22905(k, k, k);
            matrices.method_22907(new Quaternionf().rotationZ((float)Math.toRadians(45.0)));
            matrices.method_46416(-0.02F, -0.02F, -0.5F);
         } else if (first) {
            matrices.method_46416(
               (float)this.offsetX.get().intValue() / 100.0F, (float)this.offsetY.get().intValue() / 100.0F, (float)this.offsetZ.get().intValue() / 100.0F
            );
            if (totem) {
               pose(matrices, left, -2.6F, 4.0F, -2.1F, 0.0F, -62.0F, 0.0F, 0.41F * s);
            } else {
               pose(matrices, left, -3.2F, 4.1F, -2.1F, 0.0F, 125.0F, 43.0F, (kind == WaterModels.Kind.TRIDENT ? 0.4F : 0.44F) * s);
            }
         } else if (totem) {
            pose(matrices, left, 0.0F, 3.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.55F * s);
         } else {
            pose(matrices, left, 0.0F, 4.0F, 0.5F, 0.0F, -90.0F, 55.0F, 0.85F * s);
         }

         int color = 0xFF000000 | this.waterColor.get().packed() & 16777215;
         WaterModels.render(matrices, queue, kind, color, light, this.waterGlow.get());
         matrices.method_22909();
         return true;
      } else {
         return false;
      }
   }

   private static void vanilla(class_4587 matrices, boolean left, float tx, float ty, float tz, float rx, float ry, float rz) {
      if (left) {
         tx = -tx;
         ry = -ry;
         rz = -rz;
      }

      matrices.method_46416(tx / 16.0F, ty / 16.0F, tz / 16.0F);
      matrices.method_22907(
         new Quaternionf().rotationXYZ((float)Math.toRadians((double)rx), (float)Math.toRadians((double)ry), (float)Math.toRadians((double)rz))
      );
      matrices.method_46416(-0.5F, -0.5F, -0.5F);
   }

   private static void pose(class_4587 matrices, boolean left, float tx, float ty, float tz, float rx, float ry, float rz, float scale) {
      matrices.method_46416((left ? -tx : tx) / 16.0F, ty / 16.0F, tz / 16.0F);
      matrices.method_22907(
         new Quaternionf()
            .rotationXYZ(
               (float)Math.toRadians((double)rx),
               (float)Math.toRadians(left ? (double)(-ry) : (double)ry),
               (float)Math.toRadians(left ? (double)(-rz) : (double)rz)
            )
      );
      matrices.method_22905(scale, scale, scale);
      matrices.method_46416(-0.5F, -0.5F, -0.5F);
   }

   public boolean render(class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light) {
      return this.render(stack, context, matrices, queue, light, false);
   }

   public boolean render(class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light, boolean throwing) {
      boolean first = context == class_811.field_4322 || context == class_811.field_4321;
      boolean third = context == class_811.field_4320 || context == class_811.field_4323;
      if (!first && !third) {
         return false;
      } else {
         boolean leftHand = context == class_811.field_4321 || context == class_811.field_4323;
         if (first && this.firstPerson.get() || third && this.thirdPerson.get()) {
            if (!this.matches(stack)) {
               return false;
            } else if (ModelManager.isWatery(this.model.get())) {
               return this.renderWater(stack, first, leftHand, throwing, matrices, queue, light);
            } else {
               SpriteModel sprite = this.sprite(stack);
               ModelMesh mesh = sprite == null ? this.mesh() : null;
               if (sprite == null && mesh == null) {
                  return false;
               } else {
                  boolean left = context == class_811.field_4321 || context == class_811.field_4323;
                  matrices.method_22903();
                  if (first) {
                     matrices.method_46416(
                        (float)this.offsetX.get().intValue() / 100.0F,
                        (float)this.offsetY.get().intValue() / 100.0F,
                        (float)this.offsetZ.get().intValue() / 100.0F
                     );
                  }

                  if (first) {
                     display(matrices, left, 1.13F, 3.2F, 1.13F, -90.0F, 25.0F, 0.68F);
                  } else {
                     display(matrices, left, 0.0F, 4.0F, 0.5F, -90.0F, 55.0F, 0.85F);
                  }

                  float s = (float)this.size.get().intValue() / 100.0F;
                  matrices.method_46416(0.5F, 0.5F, 0.5F);
                  matrices.method_22905(this.flip.get() ? -s : s, s, s);
                  matrices.method_46416(-0.5F, -0.5F, -0.5F);
                  if (sprite != null) {
                     queue.method_73483(
                        matrices,
                        class_12249.method_75994(sprite.texture),
                        (entry, consumer) -> {
                           float[] p = sprite.positions;
                           float[] uv = sprite.uvs;
                           float[] n = sprite.normals;
                           int[] c = sprite.colors;

                           for (int q = 0; q < sprite.quads; q++) {
                              for (int k = 0; k < 4; k++) {
                                 int v = q * 4 + k;
                                 consumer.method_56824(entry, p[v * 3], p[v * 3 + 1], p[v * 3 + 2])
                                    .method_39415(c[q])
                                    .method_22913(uv[v * 2], uv[v * 2 + 1])
                                    .method_22922(class_4608.field_21444)
                                    .method_60803(light)
                                    .method_60831(entry, n[v * 3], n[v * 3 + 1], n[v * 3 + 2]);
                              }
                           }
                        }
                     );
                     matrices.method_22909();
                     return true;
                  } else {
                     int[] colors = this.colors(mesh, this.shine.get());
                     queue.method_73483(
                        matrices,
                        LAYER,
                        (entry, consumer) -> {
                           float[] p = mesh.positions;
                           float[] n = mesh.normals;

                           for (int t = 0; t < mesh.triangles; t++) {
                              float nx = n[t * 3];
                              float ny = n[t * 3 + 1];
                              float nz = n[t * 3 + 2];

                              for (int k = 0; k < 4; k++) {
                                 int v = t * 3 + Math.min(k, 2);
                                 consumer.method_56824(entry, p[v * 3], p[v * 3 + 1], p[v * 3 + 2])
                                    .method_39415(colors[v])
                                    .method_22913(0.5F, 0.5F)
                                    .method_22922(class_4608.field_21444)
                                    .method_60803(light)
                                    .method_60831(entry, nx, ny, nz);
                              }
                           }
                        }
                     );
                     matrices.method_22909();
                     return true;
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   private static void display(class_4587 matrices, boolean left, float tx, float ty, float tz, float ry, float rz, float scale) {
      matrices.method_46416((left ? -tx : tx) / 16.0F, ty / 16.0F, tz / 16.0F);
      matrices.method_22907(new Quaternionf().rotationXYZ(0.0F, (float)Math.toRadians((double)ry), (float)Math.toRadians((double)rz)));
      matrices.method_22905(scale, scale, scale);
      matrices.method_46416(-0.5F, -0.5F, -0.5F);
   }

   public class_2960 icon(class_1799 stack) {
      if (!this.inventoryIcon.get() || !this.matches(stack)) {
         return null;
      } else if (ModelManager.isWatery(this.model.get())) {
         WaterModels.Kind kind = WaterModels.kind(itemId(stack));
         return kind == null ? null : WaterIcon.get(kind, this.waterColor.get().packed() & 16777215);
      } else {
         SpriteModel sprite = this.sprite(stack);
         if (sprite != null) {
            return sprite.icon;
         } else if (!ModelManager.isPack(this.model.get()) && !ModelManager.isSprite(this.model.get())) {
            ModelMesh mesh = this.mesh();
            if (mesh == null) {
               return null;
            } else {
               boolean animated = this.colorMode.get() == CustomModels.ColorMode.Rainbow || this.colorMode.get() == CustomModels.ColorMode.Sky;
               int key = Objects.hash(
                  this.model.get(),
                  this.colorMode.get(),
                  this.headColor.get().packed(),
                  this.handleColor.get().packed(),
                  this.headSize.get(),
                  this.flip.get(),
                  animated ? System.currentTimeMillis() / 400L : 0L
               );
               return ModelIcon.get(mesh, () -> this.colors(mesh, false), this.flip.get(), key);
            }
         } else {
            return null;
         }
      }
   }

   private int[] colors(ModelMesh mesh, boolean withShine) {
      int[] colors = new int[mesh.triangles * 3];
      CustomModels.ColorMode mode = this.colorMode.get();
      boolean colored = mesh.hasColors();
      if (mode == CustomModels.ColorMode.Model && !colored) {
         mode = CustomModels.ColorMode.TwoTone;
      }

      if (mode == CustomModels.ColorMode.FNCS && colored) {
         mode = CustomModels.ColorMode.Model;
      }

      float split = 1.0F - (float)this.headSize.get().intValue() / 100.0F;
      double time = (double)System.currentTimeMillis() / 1000.0;
      int head = this.headColor.get().packed() & 16777215;
      int handle = this.handleColor.get().packed() & 16777215;
      if (mode == CustomModels.ColorMode.Sky) {
         int sky = SoberClient.MODULES.get(Sky.class).accentColor();
         head = sky >= 0 ? sky : Theme.accent() & 16777215;
         handle = mix(head, 2105384, 0.55F);
      }

      for (int v = 0; v < colors.length; v++) {
         float along = mesh.along[v];

         int rgb = switch (mode) {
            case Single -> head;
            default -> mix(handle, head, smooth((along - split) / 0.04F + 0.5F));
            case Rainbow -> Color.fromHsv((float)(((double)along * 0.6 + time * 0.25) % 1.0), 0.75F, 1.0F, 255).packed() & 16777215;
            case Model -> mesh.colors[v / 3] == -1 ? head : mesh.colors[v / 3];
            case FNCS -> fncs(mesh, v, along, split);
         };
         if (withShine) {
            float band = (float)(time * 0.45 % 1.6) - 0.3F;
            float glow = 1.0F - Math.min(1.0F, Math.abs(along - band) / 0.07F);
            if (glow > 0.0F) {
               rgb = mix(rgb, 16777215, glow * 0.55F);
            }
         }

         colors[v] = 0xFF000000 | rgb;
      }

      return colors;
   }

   private static int fncs(ModelMesh mesh, int v, float along, float split) {
      int t = v / 3;
      float nz = Math.abs(mesh.normals[t * 3 + 2]);
      float raised = Math.abs(mesh.positions[v * 3 + 2] - 0.5F) / mesh.depth;
      if (!(along > split)) {
         if (along < 0.045F) {
            return 14692414;
         } else if (along < 0.075F) {
            return 15909466;
         } else {
            float stripe = nz < 0.55F ? 0.35F : 0.0F;
            return mix(mix(12605486, 15768158, along / Math.max(0.01F, split)), 16769224, stripe);
         }
      } else {
         boolean frame = nz < 0.55F || raised > 0.72F;
         return frame ? mix(15328494, 16103624, 0.25F + 0.25F * (1.0F - nz)) : mix(13116988, 15222874, along);
      }
   }

   private static float smooth(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t * t * (3.0F - 2.0F * t);
   }

   private static int mix(int a, int b, float t) {
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
   }

   @Override
   public int presetCount() {
      return 1 + ModelManager.files().size();
   }

   @Override
   public String presetName(int index) {
      if (index == 0) {
         return "None";
      } else {
         List<String> files = ModelManager.files();
         return index - 1 < files.size() ? ModelManager.title(files.get(index - 1)) : "";
      }
   }

   @Override
   public int presetColor(int index) {
      if (index == 0) {
         return -9801856;
      } else {
         List<String> files = ModelManager.files();
         if (index - 1 < files.size() && ModelManager.isWatery(files.get(index - 1))) {
            return 0xFF000000 | this.waterColor.get().packed() & 16777215;
         } else {
            int swatch = index - 1 < files.size() ? ModelLibrary.swatch(files.get(index - 1)) : -1;
            return 0xFF000000 | (swatch >= 0 ? swatch : this.headColor.get().packed() & 16777215);
         }
      }
   }

   @Override
   public boolean presetActive(int index) {
      if (index == 0) {
         return this.model.get().isEmpty();
      } else {
         List<String> files = ModelManager.files();
         return index - 1 < files.size() && files.get(index - 1).equals(this.model.get());
      }
   }

   @Override
   public void selectPreset(int index) {
      if (index == 0) {
         this.model.set("");
      } else {
         List<String> files = ModelManager.files();
         if (index - 1 < files.size()) {
            String file = files.get(index - 1);
            this.model.set(file);
            if (!this.isActive()) {
               this.setEnabled(true);
            }

            if (!ModelManager.isPack(file) && !ModelManager.isSprite(file)) {
               if (ModelManager.get(file) == null) {
                  Notifications.push("Custom Models", "Could not load " + file + ": " + ModelManager.error(file), Notifications.Type.ERROR);
               }
            }
         }
      }
   }

   @Override
   public String presetDescription() {
      return "Uses this model. Folders are item packs: every item gets the picture with its name, for example diamond_pickaxe.png. Watery turns swords, pickaxes, shovels, tridents and totems into deep blue water glass. Your own .stl, .obj or .png files go into config/violet/models.";
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   @Override
   public boolean bindable() {
      return false;
   }

   public static enum ColorMode {
      TwoTone,
      Single,
      Sky,
      Rainbow,
      Model,
      FNCS;
   }

   public static enum Replace {
      Pickaxes,
      Item,
      Both;
   }
}
