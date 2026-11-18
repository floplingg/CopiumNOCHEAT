package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.gui.Theme;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.AccessoryMesh;
import dev.violet.render.Color;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_591;
import net.minecraft.class_9334;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class CustomAccessories extends Module {
   private static final class_1921 LAYER = class_12249.method_75994(class_2960.method_60655("violet", "textures/models/white.png"));
   private static final int FULL_BRIGHT = 15728880;
   private final Setting<Boolean> wings = this.add(
      new BoolSetting("Violet Wings", "Black wings with violet joints and glowing feather tips on your back.", true)
   );
   private final Setting<Boolean> crown = this.add(new BoolSetting("Crown", "A violet crown with glowing gems on your head.", true));
   private final Setting<Color> crownColor = this.add(
         new ColorSetting("Crown Color", "Color of the crown. Pick any color on the wheel.", new Color(196, 181, 253, 255))
      )
      .visible(this.crown::get);
   private final Setting<Integer> wingSize = this.add(new IntSetting("Wing Size", "Size of the wings in percent.", 100, 60, 140));
   private final Setting<Boolean> animate = this.add(new BoolSetting("Wing Animation", "The wings breathe slowly up and down.", true));
   private final Setting<CustomAccessories.GemColor> gemMode = this.add(
      new EnumSetting<>(
         "Gem Color", "Theme uses your accent color for the gems and feather tips. Custom uses your own color.", CustomAccessories.GemColor.Theme
      )
   );
   private final Setting<Color> gemColor = this.add(new ColorSetting("Custom Gem Color", "Color of the gems and feather tips.", new Color(167, 139, 250, 255)))
      .visible(() -> this.gemMode.get() == CustomAccessories.GemColor.Custom);
   private final AccessoryMesh mesh = new AccessoryMesh();
   private final Map<Integer, CustomAccessories.Baked[]> wingCache = new HashMap<>();
   private CustomAccessories.Baked crownMesh;
   private float bakedSize = -1.0F;
   private int bakedAccent;
   private int crownAccent;
   private int crownMetal;

   public CustomAccessories() {
      super(
         "Custom Accessories",
         "The Violet Wings and a crown on your player. While you wear an elytra the normal elytra shows instead of the wings. Only you can see them.",
         Category.VISUALS
      );
   }

   private static CustomAccessories instance() {
      return SoberClient.MODULES.get(CustomAccessories.class);
   }

   private static boolean self(class_10017 state) {
      if (state instanceof class_10055 player
         && mc.field_1724 != null
         && player.field_53528 == mc.field_1724.method_5628()
         && !player.field_53333
         && !player.field_53542) {
         return true;
      }

      return false;
   }

   private static boolean wearsElytra(class_10055 state) {
      return state.field_53418 != null && state.field_53418.method_57826(class_9334.field_54197);
   }

   public static void render(class_591 model, class_10055 state, class_4587 matrices, class_11659 queue, int light, float limbAngle, float limbDistance) {
      CustomAccessories module = instance();
      if (module != null && module.isActive() && !state.field_53333 && !state.field_53542) {
         boolean me = self(state);
         if (me) {
            boolean showWings = module.wings.get() && !wearsElytra(state);
            int accent = module.gemMode.get() == CustomAccessories.GemColor.Custom ? 0xFF000000 | module.gemColor.get().packed() : 0xFF000000 | Theme.accent();
            if (showWings) {
               float raw = state.field_53411 ? Math.max(0.0F, Math.min(1.0F, (-state.field_53417 - 0.2618F) / 1.309F)) : 0.0F;
               int step = Math.round(raw * 40.0F);
               float spread = (float)step / 40.0F;
               float size = (float)module.wingSize.get().intValue() / 100.0F;
               if (size != module.bakedSize || accent != module.bakedAccent) {
                  module.wingCache.clear();
                  module.bakedSize = size;
                  module.bakedAccent = accent;
               }

               CustomAccessories.Baked[] pair = module.wingCache
                  .computeIfAbsent(
                     step,
                     key -> new CustomAccessories.Baked[]{
                           CustomAccessories.Baked.of(module.mesh.reset(), mesh -> mesh.wing(new Matrix4f(), 1, spread, 0.0F, size, accent)),
                           CustomAccessories.Baked.of(module.mesh.reset(), mesh -> mesh.wing(new Matrix4f(), -1, spread, 0.0F, size, accent))
                        }
                  );
               float flap = module.animate.get() ? (float)Math.sin((double)(state.field_53328 * 0.09F)) * 0.05F * (1.0F - spread) : 0.0F;
               matrices.method_22903();
               model.field_3391.method_22703(matrices);
               matrices.method_22905(0.0625F, 0.0625F, 0.0625F);

               for (int side = 1; side >= -1; side -= 2) {
                  matrices.method_22903();
                  matrices.method_46416((float)side * 1.6F, 2.4F, 2.3F);
                  matrices.method_22907(new Quaternionf().rotationY(-flap * (float)side));
                  matrices.method_46416((float)(-side) * 1.6F, -2.4F, -2.3F);
                  pair[side == 1 ? 0 : 1].submit(matrices, queue, light);
                  matrices.method_22909();
               }

               matrices.method_22909();
            }

            if (me && module.crown.get()) {
               int metal = 0xFF000000 | module.crownColor.get().packed() & 16777215;
               if (module.crownMesh == null || module.crownAccent != accent || module.crownMetal != metal) {
                  module.crownMesh = CustomAccessories.Baked.of(new AccessoryMesh(), mesh -> mesh.crown(new Matrix4f(), accent, metal));
                  module.crownAccent = accent;
                  module.crownMetal = metal;
               }

               matrices.method_22903();
               model.field_3398.method_22703(matrices);
               matrices.method_22905(0.0625F, 0.0625F, 0.0625F);
               module.crownMesh.submit(matrices, queue, light);
               matrices.method_22909();
            }
         }
      }
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   private static record Baked(float[] positions, float[] normals, int[] colors, boolean[] glow, int quads) {
      static CustomAccessories.Baked of(AccessoryMesh mesh, Consumer<AccessoryMesh> build) {
         build.accept(mesh);
         int quads = mesh.quads;
         return new CustomAccessories.Baked(
            Arrays.copyOf(mesh.positions, quads * 12),
            Arrays.copyOf(mesh.normals, quads * 3),
            Arrays.copyOf(mesh.colors, quads),
            Arrays.copyOf(mesh.glow, quads),
            quads
         );
      }

      void submit(class_4587 matrices, class_11659 queue, int light) {
         queue.method_73483(
            matrices,
            CustomAccessories.LAYER,
            (entry, consumer) -> {
               for (int q = 0; q < this.quads; q++) {
                  int packed = this.glow[q] ? 15728880 : light;
                  float nx = this.normals[q * 3];
                  float ny = this.normals[q * 3 + 1];
                  float nz = this.normals[q * 3 + 2];

                  for (int k = 0; k < 4; k++) {
                     int o = q * 12 + k * 3;
                     consumer.method_56824(entry, this.positions[o], this.positions[o + 1], this.positions[o + 2])
                        .method_39415(this.colors[q])
                        .method_22913(0.5F, 0.5F)
                        .method_22922(class_4608.field_21444)
                        .method_60803(packed)
                        .method_60831(entry, nx, ny, nz);
                  }
               }
            }
         );
      }
   }

   public static enum GemColor {
      Theme,
      Custom;
   }
}
