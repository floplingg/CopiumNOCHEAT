package dev.violet.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VectorIcon {
   private static final Pattern TOKEN = Pattern.compile("[MmLlHhVvCcSsZz]|-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?");
   private static final int CURVE_STEPS = 24;

   private VectorIcon() {
   }

   static List<float[]> parse(String path) {
      List<String> tokens = new ArrayList<>();
      Matcher matcher = TOKEN.matcher(path);

      while (matcher.find()) {
         tokens.add(matcher.group());
      }

      List<float[]> shapes = new ArrayList<>();
      VectorIcon.Points points = null;
      float x = 0.0F;
      float y = 0.0F;
      float startX = 0.0F;
      float startY = 0.0F;
      float lastCx = 0.0F;
      float lastCy = 0.0F;
      char command = 'M';
      boolean curve = false;
      int i = 0;

      while (i < tokens.size()) {
         String token = tokens.get(i);
         if (Character.isLetter(token.charAt(0))) {
            command = token.charAt(0);
            i++;
            if (command == 'Z' || command == 'z') {
               if (points != null) {
                  shapes.add(points.done());
               }

               points = null;
               x = startX;
               y = startY;
               curve = false;
               continue;
            }
         }

         boolean relative = Character.isLowerCase(command);
         float ox = relative ? x : 0.0F;
         float oy = relative ? y : 0.0F;
         switch (Character.toUpperCase(command)) {
            case 'C':
            case 'S':
               float c1x;
               float c1y;
               if (Character.toUpperCase(command) == 'C') {
                  c1x = ox + number(tokens, i++);
                  c1y = oy + number(tokens, i++);
               } else {
                  c1x = curve ? 2.0F * x - lastCx : x;
                  c1y = curve ? 2.0F * y - lastCy : y;
               }

               float c2x = ox + number(tokens, i++);
               float c2y = oy + number(tokens, i++);
               float ex = ox + number(tokens, i++);
               float ey = oy + number(tokens, i++);
               points = ensure(points, startX, startY);

               for (int s = 1; s <= 24; s++) {
                  float t = (float)s / 24.0F;
                  float u = 1.0F - t;
                  float px = u * u * u * x + 3.0F * u * u * t * c1x + 3.0F * u * t * t * c2x + t * t * t * ex;
                  float py = u * u * u * y + 3.0F * u * u * t * c1y + 3.0F * u * t * t * c2y + t * t * t * ey;
                  points.add(px, py);
               }

               lastCx = c2x;
               lastCy = c2y;
               x = ex;
               y = ey;
               curve = true;
               break;
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'I':
            case 'J':
            case 'K':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            default:
               i++;
               break;
            case 'H':
               x = ox + number(tokens, i++);
               points = ensure(points, startX, startY);
               points.add(x, y);
               curve = false;
               break;
            case 'L':
               x = ox + number(tokens, i++);
               y = oy + number(tokens, i++);
               points = ensure(points, startX, startY);
               points.add(x, y);
               curve = false;
               break;
            case 'M':
               if (points != null) {
                  shapes.add(points.done());
               }

               x = ox + number(tokens, i++);
               y = oy + number(tokens, i++);
               startX = x;
               startY = y;
               points = new VectorIcon.Points();
               points.add(x, y);
               command = (char)(relative ? 108 : 76);
               curve = false;
               break;
            case 'V':
               y = oy + number(tokens, i++);
               points = ensure(points, startX, startY);
               points.add(x, y);
               curve = false;
         }
      }

      if (points != null) {
         shapes.add(points.done());
      }

      return shapes;
   }

   private static VectorIcon.Points ensure(VectorIcon.Points points, float x, float y) {
      if (points != null) {
         return points;
      } else {
         VectorIcon.Points created = new VectorIcon.Points();
         created.add(x, y);
         return created;
      }
   }

   private static float number(List<String> tokens, int index) {
      return index < tokens.size() ? Float.parseFloat(tokens.get(index)) : 0.0F;
   }

   static float[] coverage(List<float[]> shapes, int from, int to, float viewBox, int size) {
      float[] result = new float[size * size];
      int samples = 8;
      float scale = (float)size / viewBox;
      float[] crossings = new float[64];

      for (int row = 0; row < size; row++) {
         for (int s = 0; s < samples; s++) {
            float sy = ((float)row + ((float)s + 0.5F) / (float)samples) / scale;
            int count = 0;

            for (int shape = from; shape < to; shape++) {
               float[] p = shapes.get(shape);
               int n = p.length / 2;

               for (int k = 0; k < n; k++) {
                  float ax = p[k * 2];
                  float ay = p[k * 2 + 1];
                  float bx = p[(k + 1) % n * 2];
                  float by = p[(k + 1) % n * 2 + 1];
                  if (ay <= sy != by <= sy) {
                     if (count >= crossings.length) {
                        crossings = Arrays.copyOf(crossings, crossings.length * 2);
                     }

                     crossings[count++] = (ax + (sy - ay) / (by - ay) * (bx - ax)) * scale;
                  }
               }
            }

            Arrays.sort(crossings, 0, count);

            for (int c = 0; c + 1 < count; c += 2) {
               float left = Math.max(0.0F, crossings[c]);
               float right = Math.min((float)size, crossings[c + 1]);
               if (!(right <= left)) {
                  int first = (int)Math.floor((double)left);
                  int last = Math.min(size - 1, (int)Math.ceil((double)right) - 1);

                  for (int px = first; px <= last; px++) {
                     float overlap = Math.min(right, (float)(px + 1)) - Math.max(left, (float)px);
                     if (overlap > 0.0F) {
                        result[row * size + px] = result[row * size + px] + overlap / (float)samples;
                     }
                  }
               }
            }
         }
      }

      return result;
   }

   private static final class Points {
      private float[] values = new float[64];
      private int count;

      void add(float x, float y) {
         if (this.count + 2 > this.values.length) {
            this.values = Arrays.copyOf(this.values, this.values.length * 2);
         }

         this.values[this.count++] = x;
         this.values[this.count++] = y;
      }

      float[] done() {
         return Arrays.copyOf(this.values, this.count);
      }
   }
}
