package dev.violet.module;

public enum Category {
   VISUALS("Visuals"),
   CLIENT("Client"),
   CONFIG("Config");

   public final String title;

   private Category(String title) {
      this.title = title;
   }
}
