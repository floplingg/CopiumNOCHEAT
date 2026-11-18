package dev.violet.render;

public record Color(int r, int g, int b, int a) {
   public static final Color WHITE = new Color(255, 255, 255, 255);

   public Color withAlpha(int alpha) {
      return new Color(this.r, this.g, this.b, Math.max(0, Math.min(255, alpha)));
   }

   public Color scaleAlpha(double factor) {
      return this.withAlpha((int)((double)this.a * Math.max(0.0, Math.min(1.0, factor))));
   }

   public Color lerp(Color to, double t) {
      return new Color(
         (int)((double)this.r + (double)(to.r - this.r) * t),
         (int)((double)this.g + (double)(to.g - this.g) * t),
         (int)((double)this.b + (double)(to.b - this.b) * t),
         (int)((double)this.a + (double)(to.a - this.a) * t)
      );
   }

   public int packed() {
      return (this.a & 0xFF) << 24 | (this.r & 0xFF) << 16 | (this.g & 0xFF) << 8 | this.b & 0xFF;
   }

   public static Color fromPacked(int argb) {
      return new Color(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24);
   }

   public Color scaleRgb(double factor) {
      return new Color(clamp((double)this.r * factor), clamp((double)this.g * factor), clamp((double)this.b * factor), this.a);
   }

   public float[] toHsv() {
      float rf = (float)this.r / 255.0F;
      float gf = (float)this.g / 255.0F;
      float bf = (float)this.b / 255.0F;
      float max = Math.max(rf, Math.max(gf, bf));
      float min = Math.min(rf, Math.min(gf, bf));
      float delta = max - min;
      float hue;
      if (delta == 0.0F) {
         hue = 0.0F;
      } else if (max == rf) {
         hue = (gf - bf) / delta % 6.0F;
      } else if (max == gf) {
         hue = (bf - rf) / delta + 2.0F;
      } else {
         hue = (rf - gf) / delta + 4.0F;
      }

      hue /= 6.0F;
      if (hue < 0.0F) {
         hue++;
      }

      float saturation = max == 0.0F ? 0.0F : delta / max;
      return new float[]{hue, saturation, max};
   }

   public static Color fromHsv(float hue, float saturation, float value, int alpha) {
      float h = (hue - (float)Math.floor((double)hue)) * 6.0F;
      float s = Math.max(0.0F, Math.min(1.0F, saturation));
      float v = Math.max(0.0F, Math.min(1.0F, value));
      int sector = (int)Math.floor((double)h);
      float f = h - (float)sector;
      float p = v * (1.0F - s);
      float q = v * (1.0F - s * f);
      float t = v * (1.0F - s * (1.0F - f));
      float rf;
      float gf;
      float bf;
      switch (sector % 6) {
         case 0:
            rf = v;
            gf = t;
            bf = p;
            break;
         case 1:
            rf = q;
            gf = v;
            bf = p;
            break;
         case 2:
            rf = p;
            gf = v;
            bf = t;
            break;
         case 3:
            rf = p;
            gf = q;
            bf = v;
            break;
         case 4:
            rf = t;
            gf = p;
            bf = v;
            break;
         default:
            rf = v;
            gf = p;
            bf = q;
      }

      return new Color(Math.round(rf * 255.0F), Math.round(gf * 255.0F), Math.round(bf * 255.0F), alpha);
   }

   public static Color rainbow(double offset, double speed, float saturation, float value, int alpha) {
      double t = (double)System.currentTimeMillis() / 1000.0 * speed + offset;
      return fromHsv((float)(t - Math.floor(t)), saturation, value, alpha);
   }

   private static int clamp(double v) {
      return (int)Math.max(0.0, Math.min(255.0, v));
   }
}
