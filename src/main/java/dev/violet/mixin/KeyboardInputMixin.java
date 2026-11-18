package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.modules.Boatfly;
import dev.violet.modules.Freecam;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_743.class})
public abstract class KeyboardInputMixin extends class_744 {
   @Inject(
      method = {"method_3129"},
      at = {@At("TAIL")}
   )
   private void violet$boatControls(CallbackInfo ci) {
      if (Boatfly.controlling()) {
         class_10185 input = this.field_54155;
         this.field_54155 = new class_10185(input.comp_3159(), input.comp_3160(), input.comp_3161(), input.comp_3162(), false, Boatfly.exitPressed(), false);
      }
   }

   @Inject(
      method = {"method_3129"},
      at = {@At("TAIL")}
   )
   private void violet$freezePlayer(CallbackInfo ci) {
      Freecam freecam = SoberClient.MODULES.get(Freecam.class);
      if (freecam.isActive()) {
         this.field_54155 = freecam.frozenInput();
         this.field_55868 = class_241.field_1340;
      }
   }
}
