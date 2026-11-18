package dev.violet.mixin.feature;

import dev.violet.event.impl.PacketReceiveEvent;
import dev.violet.event.impl.PacketReceiveNettyEvent;
import dev.violet.event.impl.PacketSendEvent;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_2598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2535.class})
public abstract class PacketEventsMixin {
   @Unique
   private final BooleanSupplier violet$connectionActive = this::method_10758;

   @Shadow
   public abstract boolean method_10758();

   @Shadow
   public abstract class_2598 method_36121();

   @Inject(
      method = {"method_10770(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/class_2596;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$onPacketReceive(ChannelHandlerContext ctx, class_2596<?> packet, CallbackInfo ci) {
      if (this.method_36121() == class_2598.field_11942) {
         if (PacketReceiveNettyEvent.dispatchSync(packet)) {
            ci.cancel();
         } else {
            PacketReceiveEvent.dispatchAsync(packet, this.violet$connectionActive);
         }
      }
   }

   @Inject(
      method = {"method_52906(Lnet/minecraft/class_2596;Lio/netty/channel/ChannelFutureListener;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void violet$onPacketSend(class_2596<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
      if (this.method_36121() == class_2598.field_11942 && PacketSendEvent.dispatchSync(packet)) {
         ci.cancel();
      }
   }
}
