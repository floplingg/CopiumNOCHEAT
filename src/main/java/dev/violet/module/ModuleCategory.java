package dev.violet.module;

public final class ModuleCategory {
   public static final ModuleCategory MISC = new ModuleCategory(Category.VISUALS);
   public static final ModuleCategory CLIENT = new ModuleCategory(Category.CLIENT);
   public static final ModuleCategory VISUALS = new ModuleCategory(Category.VISUALS);
   private final Category category;

   private ModuleCategory(Category category) {
      this.category = category;
   }

   public Category category() {
      return this.category;
   }
}
