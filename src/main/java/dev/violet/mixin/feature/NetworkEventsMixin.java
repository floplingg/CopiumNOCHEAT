package dev.violet.mixin.feature;

import dev.violet.event.impl.ChatSendEvent;
import dev.violet.event.impl.PacketAppliedEvent;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2678;
import net.minecraft.class_2743;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public abstract class NetworkEventsMixin {
   @Inject(
      method = {"method_45729"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$onSendChat(String message, CallbackInfo ci) {
      if (ChatSendEvent.dispatch(message)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_11120"},
      at = {@At("TAIL")}
   )
   private void violet$afterLogin(class_2678 packet, CallbackInfo ci) {
      PacketAppliedEvent.dispatch(packet);
   }

   @Inject(
      method = {"method_11128"},
      at = {@At("TAIL")}
   )
   private void violet$afterChunkData(class_2672 packet, CallbackInfo ci) {
      PacketAppliedEvent.dispatch(packet);
   }

   @Inject(
      method = {"method_11136"},
      at = {@At("TAIL")}
   )
   private void violet$afterBlockUpdate(class_2626 packet, CallbackInfo ci) {
      PacketAppliedEvent.dispatch(packet);
   }

   @Inject(
      method = {"method_11100"},
      at = {@At("TAIL")}
   )
   private void violet$afterSectionUpdate(class_2637 packet, CallbackInfo ci) {
      PacketAppliedEvent.dispatch(packet);
   }

   @Inject(
      method = {"method_11143"},
      at = {@At("TAIL")}
   )
   private void violet$afterLightUpdate(class_2676 packet, CallbackInfo ci) {
      PacketAppliedEvent.dispatch(packet);
   }

   @Inject(
      method = {"method_11132"},
      at = {@At("TAIL")}
   )
   private void violet$afterEntityMotion(class_2743 packet, CallbackInfo ci) {
      PacketAppliedEvent.dispatch(packet);
   }
}
