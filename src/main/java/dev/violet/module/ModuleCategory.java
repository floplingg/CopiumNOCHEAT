package dev.violet.module;

public final class ModuleCategory {
   public static final ModuleCategory COMBAT = new ModuleCategory(Category.COMBAT);
   public static final ModuleCategory MISC = new ModuleCategory(Category.VISUALS);
   public static final ModuleCategory DONUT = new ModuleCategory(Category.DONUT);
   public static final ModuleCategory DONUT_MACROS = new ModuleCategory(Category.DONUT);
   public static final ModuleCategory RENDER = new ModuleCategory(Category.BASE_FINDING);
   public static final ModuleCategory CLIENT = new ModuleCategory(Category.CLIENT);
   public static final ModuleCategory MOVEMENT = new ModuleCategory(Category.MOVEMENT);
   public static final ModuleCategory BASE_RAID = new ModuleCategory(Category.BASE_FINDING);
   public static final ModuleCategory VISUALS = new ModuleCategory(Category.VISUALS);
   private final Category category;

   private ModuleCategory(Category category) {
      this.category = category;
   }

   public Category category() {
      return this.category;
   }
}
