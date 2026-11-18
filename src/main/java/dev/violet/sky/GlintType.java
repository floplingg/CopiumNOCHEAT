package dev.violet.sky;

import org.jspecify.annotations.Nullable;

public enum GlintType {
   VANILLA(null, 1.0F, null),
   PALM("palm", 6.0F, null),
   GALAXY("galaxy", 3.0F, null),
   RAINBOW("rainbow", 2.5F, null),
   ICE("ice", 3.0F, null),
   SUMMER("summer", 3.0F, null),
   LAGOON("lagoon", 3.0F, null),
   INFERNO("inferno", 3.0F, null),
   VELVET("velvet", 3.0F, null),
   AURUM("aurum", 3.0F, null),
   FIRE(null, 1.0F, "fire"),
   LIGHTNING(null, 1.0F, "lightning"),
   SAKURA(null, 1.0F, "sakura"),
   GOLD(null, 1.0F, "gold"),
   VOID(null, 1.0F, "void"),
   MATRIX(null, 1.0F, "matrix"),
   SYNTHWAVE(null, 1.0F, "synthwave"),
   CHROME(null, 1.0F, "chrome"),
   TOXIC(null, 1.0F, "toxic"),
   HEARTS(null, 1.0F, "hearts");

   @Nullable
   private final String texture;
   private final float scale;
   @Nullable
   private final String shader;

   private GlintType(@Nullable String texture, float scale, @Nullable String shader) {
      this.texture = texture;
      this.scale = scale;
      this.shader = shader;
   }

   @Nullable
   public String texture() {
      return this.texture;
   }

   public float scale() {
      return this.scale;
   }

   @Nullable
   public String shader() {
      return this.shader;
   }
}
