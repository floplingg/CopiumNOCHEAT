package dev.violet.sky;

import net.minecraft.class_1058;
import net.minecraft.class_4588;
import net.minecraft.class_777;
import net.minecraft.class_4587.class_4665;

public final class GlintFit implements class_4588 {
   private final class_4588 glint;
   private final class_4588 base;
   private float minU;
   private float minV;
   private float spanU = 1.0F;
   private float spanV = 1.0F;

   public GlintFit(class_4588 glint, class_4588 base) {
      this.glint = glint;
      this.base = base;
   }

   public void method_22920(
      class_4665 matrixEntry, class_777 quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay
   ) {
      class_1058 sprite = quad.comp_3724();
      if (sprite != null && sprite.method_4577() > sprite.method_4594() && sprite.method_4575() > sprite.method_4593()) {
         this.minU = sprite.method_4594();
         this.minV = sprite.method_4593();
         this.spanU = sprite.method_4577() - this.minU;
         this.spanV = sprite.method_4575() - this.minV;
      } else {
         this.minU = 0.0F;
         this.minV = 0.0F;
         this.spanU = 1.0F;
         this.spanV = 1.0F;
      }

      super.method_22920(matrixEntry, quad, brightnesses, red, green, blue, alpha, lights, overlay);
   }

   public void method_23919(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.glint.method_23919(x, y, z, color, (u - this.minU) / this.spanU, (v - this.minV) / this.spanV, overlay, light, normalX, normalY, normalZ);
      this.base.method_23919(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
   }

   public class_4588 method_22912(float x, float y, float z) {
      this.glint.method_22912(x, y, z);
      this.base.method_22912(x, y, z);
      return this;
   }

   public class_4588 method_1336(int red, int green, int blue, int alpha) {
      this.glint.method_1336(red, green, blue, alpha);
      this.base.method_1336(red, green, blue, alpha);
      return this;
   }

   public class_4588 method_39415(int argb) {
      this.glint.method_39415(argb);
      this.base.method_39415(argb);
      return this;
   }

   public class_4588 method_22913(float u, float v) {
      this.glint.method_22913((u - this.minU) / this.spanU, (v - this.minV) / this.spanV);
      this.base.method_22913(u, v);
      return this;
   }

   public class_4588 method_60796(int u, int v) {
      this.glint.method_60796(u, v);
      this.base.method_60796(u, v);
      return this;
   }

   public class_4588 method_22921(int u, int v) {
      this.glint.method_22921(u, v);
      this.base.method_22921(u, v);
      return this;
   }

   public class_4588 method_22914(float x, float y, float z) {
      this.glint.method_22914(x, y, z);
      this.base.method_22914(x, y, z);
      return this;
   }

   public class_4588 method_75298(float width) {
      this.glint.method_75298(width);
      this.base.method_75298(width);
      return this;
   }
}
