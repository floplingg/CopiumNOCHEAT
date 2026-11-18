package dev.violet.mixin;

import dev.violet.util.ChunkThreadGuard;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2818;
import net.minecraft.class_2818.class_2819;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_2818.class})
public abstract class WorldChunkThreadMixin {
   @Inject(
      method = {"method_12201(Lnet/minecraft/class_2338;Lnet/minecraft/class_2818$class_2819;)Lnet/minecraft/class_2586;"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$readOffThread(class_2338 pos, class_2819 type, CallbackInfoReturnable<class_2586> cir) {
      class_2818 self = (class_2818)this;
      if (ChunkThreadGuard.offThread(self.method_12200())) {
         class_2586 blockEntity = (class_2586)self.method_12214().get(pos);
         cir.setReturnValue(blockEntity != null && !blockEntity.method_11015() ? blockEntity : null);
      }
   }

   @Inject(
      method = {"method_12007(Lnet/minecraft/class_2586;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$setOffThread(class_2586 blockEntity, CallbackInfo ci) {
      class_2818 self = (class_2818)this;
      if (ChunkThreadGuard.offThread(self.method_12200())) {
         ChunkThreadGuard.defer("setBlockEntity", () -> self.method_12007(blockEntity));
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_12216(Lnet/minecraft/class_2586;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$addOffThread(class_2586 blockEntity, CallbackInfo ci) {
      class_2818 self = (class_2818)this;
      if (ChunkThreadGuard.offThread(self.method_12200())) {
         ChunkThreadGuard.defer("addBlockEntity", () -> self.method_12216(blockEntity));
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_12041(Lnet/minecraft/class_2338;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void violet$removeOffThread(class_2338 pos, CallbackInfo ci) {
      class_2818 self = (class_2818)this;
      if (ChunkThreadGuard.offThread(self.method_12200())) {
         class_2338 copy = pos.method_10062();
         ChunkThreadGuard.defer("removeBlockEntity", () -> self.method_12041(copy));
         ci.cancel();
      }
   }
}
