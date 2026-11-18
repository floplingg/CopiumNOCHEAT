package dev.violet.gui;

import net.minecraft.class_2960;
import net.minecraft.class_11719.class_11721;

public final class Fonts {
   public static final class_11721 VANILLA = new class_11721(class_2960.method_60655("violet", "vanilla"));
   public static final class_11721 UNIFONT = new class_11721(class_2960.method_60656("uniform"));

   private Fonts() {
   }

   public static enum Family {
      Geist("Geist", 0.98F, "geist", "medium", "medium", "semibold", "semibold"),
      Ubuntu("Ubuntu", 1.0F, "ubuntu", "regular", "medium", "bold", "bold"),
      Xuong("Xuong", 1.0F, "xuong", "regular", "regular", "regular", "regular"),
      Inter("Inter", 0.96F, "inter", "regular", "regular", "bold", "bold"),
      Roboto("Roboto", 0.985F, "roboto", "regular", "medium", "bold", "black"),
      Montserrat("Montserrat", 0.98F, "montserrat", "regular", "medium", "bold", "black"),
      SourceSans("Source Sans", 1.06F, "sourcesans", "regular", "semibold", "bold", "bold"),
      Nunito("Nunito", 1.015F, "nunito", "regular", "regular", "regular", "regular"),
      Isometra("Isometra", 0.87F, "isometra", "regular", "regular", "regular", "regular"),
      RobotoMono("Roboto Mono", 0.985F, "robotomono", "regular", "medium", "bold", "bold"),
      Consolas("Consolas", 1.075F, "consolas", "regular", "regular", "bold", "bold"),
      EduQLDHand("Edu QLD Hand", 1.02F, "eduqldhand", "regular", "regular", "bold", "bold"),
      BlackOpsOne("Black Ops One", 1.03F, "blackopsone", "regular", "regular", "regular", "regular"),
      Caveat("Caveat", 1.25F, "caveat", "regular", "regular", "bold", "bold"),
      Minecraft("Minecraft", 1.0F, null),
      Unifont("Unifont", 1.0F, null);

      private final String title;
      private final float scale;
      private final String key;
      private final String[] files;
      private final class_11721[] weights = new class_11721[Fonts.Weight.values().length];
      private final class_11721[] large = new class_11721[Fonts.Weight.values().length];

      private Family(String title, float scale, String key, String... files) {
         this.title = title;
         this.scale = scale;
         this.key = key;
         this.files = files;
         if (key != null) {
            for (int i = 0; i < this.weights.length; i++) {
               this.weights[i] = new class_11721(class_2960.method_60655("violet", key + "_" + files[i]));
               this.large[i] = new class_11721(class_2960.method_60655("violet", key + "_" + files[i] + "_hd"));
            }
         }
      }

      public float scale() {
         return this.scale;
      }

      public class_11721 large(Fonts.Weight weight) {
         return this.large[weight.ordinal()];
      }

      public boolean custom() {
         return this.weights[0] != null;
      }

      public boolean hasWeights() {
         return this.custom() && !this.weights[0].equals(this.weights[this.weights.length - 1]);
      }

      public class_11721 font(Fonts.Weight weight) {
         return this.weights[weight.ordinal()];
      }

      public String file(Fonts.Weight weight) {
         return this.key == null ? null : this.key + "-" + this.files[weight.ordinal()] + ".ttf";
      }

      @Override
      public String toString() {
         return this.title;
      }
   }

   public static enum Weight {
      Regular,
      Medium,
      Bold,
      Black;

      public Fonts.Weight heavier() {
         return switch (this) {
            case Regular, Medium -> Bold;
            case Bold, Black -> Black;
         };
      }
   }
}
