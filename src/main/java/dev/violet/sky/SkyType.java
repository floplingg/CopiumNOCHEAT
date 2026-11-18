package dev.violet.sky;

import net.minecraft.class_2960;
import org.jspecify.annotations.Nullable;

public enum SkyType {
   VANILLA("vanilla", null),
   AURORA("aurora", new float[]{0.03F, 0.085F, 0.14F}),
   GALAXY("galaxy", new float[]{0.03F, 0.02F, 0.07F}),
   ANIME("anime", new float[]{0.72F, 0.89F, 1.0F}),
   STORM("storm", new float[]{0.09F, 0.16F, 0.26F}),
   BLOODMOON("bloodmoon", new float[]{0.2F, 0.03F, 0.025F}),
   PETALDUSK("petaldusk", new float[]{0.95F, 0.7F, 0.62F}),
   EVENTHORIZON("eventhorizon", new float[]{0.02F, 0.012F, 0.04F}),
   FROSTHALO("frosthalo", new float[]{0.78F, 0.87F, 0.97F}),
   FIREFLYNIGHT("fireflynight", new float[]{0.045F, 0.11F, 0.14F}),
   CANDYCLOUDS("candyclouds", new float[]{1.0F, 0.86F, 0.9F}),
   NEBULABLOOM("nebulabloom", new float[]{0.12F, 0.05F, 0.2F}, class_2960.method_60655("violet", "textures/sky/nebula_bloom.png")),
   ASHENSQUALL("ashensquall", new float[]{0.27F, 0.29F, 0.32F}, class_2960.method_60655("violet", "textures/sky/ashen_squall.png")),
   NIGHTSHADE("nightshade", new float[]{0.06F, 0.025F, 0.11F}),
   PHANTOMMIST("phantommist", new float[]{0.15F, 0.16F, 0.18F}),
   SYNTHWAVE("synthwave", new float[]{0.2F, 0.05F, 0.24F}),
   BLACKHOLE("blackhole", new float[]{0.02F, 0.015F, 0.045F}),
   SAKURA("sakura", new float[]{0.86F, 0.62F, 0.66F}),
   UNDERWATER("underwater", new float[]{0.035F, 0.22F, 0.32F}),
   CYBERPUNK("cyberpunk", new float[]{0.13F, 0.05F, 0.17F}),
   SOBER("sober", new float[]{0.02F, 0.02F, 0.024F}, class_2960.method_60655("violet", "textures/sky/sober_discord.png"));

   private final String id;
   @Nullable
   private final float[] fogColor;
   @Nullable
   private final class_2960 texture;

   private SkyType(String id, @Nullable float[] fogColor) {
      this(id, fogColor, null);
   }

   private SkyType(String id, @Nullable float[] fogColor, @Nullable class_2960 texture) {
      this.id = id;
      this.fogColor = fogColor;
      this.texture = texture;
   }

   public String id() {
      return this.id;
   }

   public boolean isCustom() {
      return this != VANILLA;
   }

   @Nullable
   public float[] fogColor() {
      return this.fogColor;
   }

   @Nullable
   public class_2960 texture() {
      return this.texture;
   }
}
