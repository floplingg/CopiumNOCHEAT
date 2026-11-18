package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;

public class SwingAnimations extends Module {
   public final Setting<SwingAnimations.Style> style = this.add(new EnumSetting<>("Style", "How your item swings.", SwingAnimations.Style.Smooth));
   public final Setting<Integer> speed = this.add(new IntSetting("Swing Speed", "Swing speed in percent. Lower is slower and smoother.", 70, 25, 200));
   public final Setting<Integer> strength = this.add(new IntSetting("Strength", "How big the swing is, in percent.", 100, 20, 200));
   public final Setting<Boolean> noEquip = this.add(new BoolSetting("No Equip Dip", "Items don't dip down when you switch or hit.", true));
   public final Setting<Integer> scale;
   public final Setting<Integer> offsetX;
   public final Setting<Integer> offsetY;
   public final Setting<Integer> offsetZ;

   public SwingAnimations() {
      super("Swing Animations", "Custom swing animations, swing speed and item position.", Category.VISUALS);
      this.group("Item");
      this.scale = this.add(new IntSetting("Scale", "Size of the held item in percent.", 100, 40, 160));
      this.offsetX = this.add(new IntSetting("Offset X", "Moves the item left or right.", 0, -60, 60));
      this.offsetY = this.add(new IntSetting("Offset Y", "Moves the item up or down.", 0, -60, 60));
      this.offsetZ = this.add(new IntSetting("Offset Z", "Moves the item closer or further away.", 0, -60, 60));
   }

   public int duration(int vanilla) {
      return Math.max(1, Math.round((float)vanilla * 100.0F / (float)this.speed.get().intValue()));
   }

   public void offset(class_4587 matrices) {
      matrices.method_46416(
         (float)this.offsetX.get().intValue() * 0.01F, (float)this.offsetY.get().intValue() * 0.01F, (float)this.offsetZ.get().intValue() * 0.01F
      );
      float s = (float)this.scale.get().intValue() / 100.0F;
      if (s != 1.0F) {
         matrices.method_22905(s, s, s);
      }
   }

   public boolean swing(class_4587 matrices, float progress, int side) {
      SwingAnimations.Style current = this.style.get();
      if (current == SwingAnimations.Style.Vanilla) {
         return false;
      } else {
         float k = (float)this.strength.get().intValue() / 100.0F;
         float root = class_3532.method_15374((double)(class_3532.method_15355(progress) * 3.1415927F));
         float sine = class_3532.method_15374((double)(progress * 3.1415927F));
         float eased = class_3532.method_15374((double)(progress * progress * 3.1415927F));
         switch (current) {
            case Smooth:
               matrices.method_46416(
                  (float)side * -0.25F * root * k,
                  0.12F * class_3532.method_15374((double)(class_3532.method_15355(progress) * 6.2831855F)) * k,
                  -0.12F * sine * k
               );
               matrices.method_22907(class_7833.field_40716.rotationDegrees((float)side * (45.0F - 14.0F * eased * k)));
               matrices.method_22907(class_7833.field_40718.rotationDegrees((float)side * root * -14.0F * k));
               matrices.method_22907(class_7833.field_40714.rotationDegrees(root * -58.0F * k));
               matrices.method_22907(class_7833.field_40716.rotationDegrees((float)side * -45.0F));
               break;
            case Slide:
               matrices.method_46416((float)side * 0.05F, 0.08F * k, -0.04F);
               matrices.method_22907(class_7833.field_40716.rotationDegrees((float)side * 45.0F));
               matrices.method_22907(class_7833.field_40718.rotationDegrees((float)side * root * -10.0F * k));
               matrices.method_22907(class_7833.field_40714.rotationDegrees(root * -35.0F * k));
               matrices.method_22907(class_7833.field_40716.rotationDegrees((float)side * -45.0F));
               matrices.method_46416(0.0F, -0.25F * root * k, 0.0F);
               break;
            case Stab:
               matrices.method_46416((float)side * -0.08F * root * k, 0.04F * root * k, -0.45F * root * k);
               matrices.method_22907(class_7833.field_40714.rotationDegrees(-18.0F * root * k));
               break;
            case Spin:
               matrices.method_46416((float)side * -0.1F * root * k, 0.05F * root * k, -0.1F * sine * k);
               matrices.method_22907(class_7833.field_40716.rotationDegrees((float)side * -360.0F * progress));
               break;
            case Swirl:
               matrices.method_46416((float)side * -0.15F * root * k, 0.08F * root * k, -0.1F * sine * k);
               matrices.method_22907(class_7833.field_40718.rotationDegrees((float)side * -360.0F * progress));
               break;
            case Chop:
               matrices.method_46416(0.0F, 0.18F * root * k, -0.08F * sine * k);
               matrices.method_22907(class_7833.field_40714.rotationDegrees(-95.0F * root * k));
               break;
            default:
               return false;
         }

         return true;
      }
   }

   @Override
   public String getInfoString() {
      return this.style.get().name();
   }

   public static enum Style {
      Vanilla,
      Smooth,
      Slide,
      Stab,
      Spin,
      Swirl,
      Chop;
   }
}
