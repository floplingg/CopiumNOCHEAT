package dev.violet.mixin;

import dev.violet.SoberClient;
import dev.violet.gui.InventoryStyle;
import dev.violet.modules.MediaHider;
import net.minecraft.class_1703;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_465.class})
public abstract class HandledScreenMixin<T extends class_1703> extends class_437 {
   @Shadow
   protected int field_2776;
   @Shadow
   protected int field_2800;
   @Shadow
   protected int field_2792;
   @Shadow
   protected int field_2779;
   @Shadow
   @Final
   protected T field_2797;

   protected HandledScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(
      method = {"method_25420"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_465;method_2389(Lnet/minecraft/class_332;FII)V"
      )},
      require = 0
   )
   private void violet$behind(class_332 context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
      try {
         InventoryStyle.behind(
            context, this.field_22789, this.field_22790, this.field_2776, this.field_2800, this.field_2792, this.field_2779, (Object)this instanceof class_481
         );
      } catch (RuntimeException var7) {
         SoberClient.LOG.error("Inventory style failed", var7);
      }
   }

   @Inject(
      method = {"method_25420"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_465;method_2389(Lnet/minecraft/class_332;FII)V",
         shift = Shift.AFTER
      )},
      require = 0
   )
   private void violet$over(class_332 context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
      try {
         InventoryStyle.over(context, this.field_2776, this.field_2800, this.field_2792, this.field_2779, this.field_2797, (Object)this instanceof class_481);
      } catch (RuntimeException var7) {
         SoberClient.LOG.error("Inventory style failed", var7);
      }
   }

   @Inject(
      method = {"method_71085"},
      at = {@At("TAIL")}
   )
   private void violet$mediaHider(class_332 context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
      try {
         MediaHider.coverScreen(context, this, this.field_2776, this.field_2800, this.field_2792, this.field_2779, this.field_2797);
      } catch (RuntimeException var7) {
         SoberClient.LOG.error("Media hider failed", var7);
      }
   }

   @Inject(
      method = {"method_2380"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$hideTooltip(class_332 context, int mouseX, int mouseY, CallbackInfo ci) {
      if (MediaHider.hidesTooltips(this)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_71083"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$hideCursorStack(class_332 context, int mouseX, int mouseY, CallbackInfo ci) {
      if (MediaHider.hidesTooltips(this)) {
         ci.cancel();
      }
   }

   @ModifyArg(
      method = {"method_2388"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V"
      ),
      index = 4,
      require = 0
   )
   private int violet$titleColor(int color) {
      return InventoryStyle.titleColor(color);
   }
}
