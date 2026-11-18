package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.Freecam;
import dev.violet.modules.Freelook;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4184.class})
public abstract class CameraMixin {
   @Shadow
   private float field_18722;
   @Shadow
   private float field_18721;

   @Shadow
   protected abstract void method_19325(float var1, float var2);

   @Shadow
   protected abstract void method_19322(class_243 var1);

   @Shadow
   protected abstract void method_19324(float var1, float var2, float var3);

   @Shadow
   private float method_19318(float distance) {
      throw new AssertionError();
   }

   @Inject(
      method = {"method_19321"},
      at = {@At("TAIL")}
   )
   private void violet$moveCamera(class_1937 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
      if (focusedEntity == class_310.method_1551().field_1724) {
         Freecam freecam = SoberClient.MODULES.get(Freecam.class);
         if (freecam.isActive()) {
            this.method_19325(freecam.yaw(), freecam.pitch());
            this.method_19322(freecam.update());
         } else {
            Freelook freelook = SoberClient.MODULES.get(Freelook.class);
            if (freelook.isActive() || freelook.exiting()) {
               this.method_19325(freelook.yaw(), freelook.pitch());
               this.method_19322(
                  new class_243(
                     class_3532.method_16436((double)tickProgress, focusedEntity.field_6014, focusedEntity.method_23317()),
                     class_3532.method_16436((double)tickProgress, focusedEntity.field_6036, focusedEntity.method_23318())
                        + (double)class_3532.method_16439(tickProgress, this.field_18722, this.field_18721),
                     class_3532.method_16436((double)tickProgress, focusedEntity.field_5969, focusedEntity.method_23321())
                  )
               );
               float distance = (float)freelook.distance();
               this.method_19324(-(freelook.throughWalls() ? distance : this.method_19318(distance)), 0.0F, 0.0F);
            }
         }
      }
   }
}
