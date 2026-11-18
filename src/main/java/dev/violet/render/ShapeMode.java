package dev.violet.render;

public enum ShapeMode {
   Lines,
   Sides,
   Both;

   public boolean lines() {
      return this != Sides;
   }

   public boolean sides() {
      return this != Lines;
   }
}
