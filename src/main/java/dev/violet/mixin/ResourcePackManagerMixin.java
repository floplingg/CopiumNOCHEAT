package dev.violet.mixin;

import dev.violet.gui.SystemFontPack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.class_3262;
import net.minecraft.class_3283;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_3283.class})
public abstract class ResourcePackManagerMixin {
   @Inject(
      method = {"method_29211"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void violet$systemFonts(CallbackInfoReturnable<List<class_3262>> cir) {
      try {
         SystemFontPack pack = new SystemFontPack();
         if (pack.isEmpty()) {
            return;
         }

         List<class_3262> packs = new ArrayList<>(((List)cir.getReturnValue()).size() + 1);
         packs.add(pack);
         packs.addAll((Collection<? extends class_3262>)cir.getReturnValue());
         cir.setReturnValue(packs);
      } catch (RuntimeException var4) {
      }
   }
}
