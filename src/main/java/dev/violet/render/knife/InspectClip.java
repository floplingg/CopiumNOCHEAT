package dev.violet.render.knife;

import net.minecraft.class_3532;

public final class InspectClip {
   public static final int X = 0;
   public static final int Y = 1;
   public static final int Z = 2;
   public static final int YAW = 3;
   public static final int PITCH = 4;
   public static final int ROLL = 5;
   public static final int FLIP = 6;
   public static final int SPIN = 7;
   public static final int PART_A = 8;
   public static final int PART_B = 9;
   public static final int SHINE = 10;
   public static final int SLIDE = 11;
   public static final int DRAW = 12;
   public static final int SHEATH = 13;
   public static final int OFF_HAND = 14;
   public static final int OFF_AT = 15;
   public static final int CHANNELS = 16;
   final float[][] keys;
   public final float seconds;
   public final float loopEnter;
   public final float loopExit;
   private final float[][] loop;

   InspectClip(float[][] keys, float seconds) {
      this(keys, seconds, 0.0F, 0.0F);
   }

   InspectClip(float[][] keys, float seconds, float loopEnter, float loopExit) {
      this.keys = keys;
      this.seconds = seconds;
      this.loopEnter = loopEnter;
      this.loopExit = loopExit;
      this.loop = loopExit > loopEnter ? cut(keys, loopEnter, loopExit) : null;
   }

   public boolean loops() {
      return this.loop != null;
   }

   public float[] sample(float t, float[] out) {
      return this.loop != null && t > this.loopEnter && t < this.loopExit
         ? sampleLoop(this.loop, (t - this.loopEnter) / (this.loopExit - this.loopEnter), out)
         : sample(this.keys, t, out);
   }

   private static float[][] cut(float[][] keys, float enter, float exit) {
      int from = -1;
      int to = -1;

      for (int i = 0; i < keys.length; i++) {
         if (Math.abs(keys[i][0] - enter) < 1.0E-4F) {
            from = i;
         }

         if (Math.abs(keys[i][0] - exit) < 1.0E-4F) {
            to = i;
         }
      }

      if (from >= 0 && to > from) {
         float[][] loop = new float[to - from + 1][];

         for (int i = from; i <= to; i++) {
            loop[i - from] = (float[])keys[i].clone();
            loop[i - from][0] = (keys[i][0] - enter) / (exit - enter);
         }

         return loop;
      } else {
         throw new IllegalArgumentException("a loop starts and ends on keys");
      }
   }

   static float[] sample(float[][] keys, float t, float[] out) {
      t = class_3532.method_15363(t, 0.0F, 1.0F);
      int i = 0;

      while (i < keys.length - 2 && t > keys[i + 1][0]) {
         i++;
      }

      float[] from = keys[i];
      float[] to = keys[i + 1];
      float span = to[0] - from[0];
      float u = span <= 0.0F ? 1.0F : (t - from[0]) / span;

      for (int c = 0; c < 16; c++) {
         out[c] = hermite(value(from, c), value(to, c), slope(keys, i, c) * span, slope(keys, i + 1, c) * span, u);
      }

      return out;
   }

   static float[] sampleLoop(float[][] keys, float t, float[] out) {
      t -= (float)Math.floor((double)t);
      int n = keys.length;
      int i = 0;

      while (i < n - 2 && t > keys[i + 1][0]) {
         i++;
      }

      float[] from = keys[i];
      float[] to = keys[i + 1];
      float span = to[0] - from[0];
      float u = span <= 0.0F ? 1.0F : (t - from[0]) / span;

      for (int c = 0; c < 16; c++) {
         out[c] = hermite(value(from, c), value(to, c), loopSlope(keys, i, c) * span, loopSlope(keys, i + 1, c) * span, u);
      }

      return out;
   }

   private static float loopSlope(float[][] keys, int i, int c) {
      int n = keys.length;
      float drift = value(keys[n - 1], c) - value(keys[0], c);
      float prevT;
      float prevV;
      if (i == 0) {
         prevT = keys[n - 2][0] - 1.0F;
         prevV = value(keys[n - 2], c) - drift;
      } else {
         prevT = keys[i - 1][0];
         prevV = value(keys[i - 1], c);
      }

      float nextT;
      float nextV;
      if (i == n - 1) {
         nextT = keys[1][0] + 1.0F;
         nextV = value(keys[1], c) + drift;
      } else {
         nextT = keys[i + 1][0];
         nextV = value(keys[i + 1], c);
      }

      return (nextV - prevV) / (nextT - prevT);
   }

   private static float value(float[] key, int channel) {
      return channel + 1 < key.length ? key[channel + 1] : 0.0F;
   }

   private static float slope(float[][] keys, int i, int c) {
      if (i != 0 && i != keys.length - 1) {
         float[] prev = keys[i - 1];
         float[] next = keys[i + 1];
         return (value(next, c) - value(prev, c)) / (next[0] - prev[0]);
      } else {
         return 0.0F;
      }
   }

   private static float hermite(float p0, float p1, float m0, float m1, float u) {
      float u2 = u * u;
      float u3 = u2 * u;
      return (2.0F * u3 - 3.0F * u2 + 1.0F) * p0 + (u3 - 2.0F * u2 + u) * m0 + (-2.0F * u3 + 3.0F * u2) * p1 + (u3 - u2) * m1;
   }
}
