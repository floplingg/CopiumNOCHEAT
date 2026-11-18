package dev.violet.render;

import dev.violet.modules.SkeletonESP;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_591;

public class XrayFeature extends class_3887<class_10055, class_591> {
   public XrayFeature(class_3883<class_10055, class_591> context) {
      super(context);
   }

   public void render(class_4587 matrices, class_11659 queue, int light, class_10055 state, float limbAngle, float limbDistance) {
      SkeletonESP.capture((class_591)this.method_17165(), state, matrices);
   }
}
