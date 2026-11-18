package dev.violet.gui;

import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_11719.class_11721;

public record CardFont(class_2583 style, float ascent, float capTop) {
   public static final CardFont TEXT = new CardFont(
      class_2583.field_24360.method_27704(new class_11721(class_2960.method_60655("violet", "geist_text"))), 0.8F, 0.2F
   );
   public static final CardFont LABEL = new CardFont(
      class_2583.field_24360.method_27704(new class_11721(class_2960.method_60655("violet", "geist_label"))), 0.8F, 0.2F
   );
}
