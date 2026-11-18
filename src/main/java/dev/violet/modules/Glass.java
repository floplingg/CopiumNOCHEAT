package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.Color;
import dev.violet.render.GlassGear;
import dev.violet.render.GlassLayers;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.ColorSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_10055;
import net.minecraft.class_1921;
import net.minecraft.class_4722;
import net.minecraft.class_7920;
import net.minecraft.class_811;

public class Glass extends Module {
   public final Setting<Boolean> self = this.add(new BoolSetting("Self", "Turns you into glass.", true));
   public final Setting<Boolean> others = this.add(new BoolSetting("Other Players", "Turns other players into glass.", true));
   public final Setting<Boolean> gear = this.add(new BoolSetting("Armor & Elytra", "Armor, elytras and capes of glass players are glass too.", true));
   public final Setting<Boolean> items = this.add(
      new BoolSetting("Items", "Items in the world are glass too: held items, dropped items and item frames.", true)
   );
   public final Setting<Boolean> inventory = this.add(new BoolSetting("Inventory", "Items in your inventory, hotbar and menus are glass too.", true));
   public final Setting<Color> tint = this.add(new ColorSetting("Tint", "Color of the glass.", new Color(226, 238, 255, 255)));

   public Glass() {
      super("Glass", "Players, their armor and all items made of clear glass with glowing edges. Only you can see it.", Category.VISUALS);
   }

   private static Glass active() {
      Glass module = SoberClient.MODULES.get(Glass.class);
      return module != null && module.isActive() ? module : null;
   }

   public static boolean applies(Object state) {
      if (state instanceof class_10055 player) {
         Glass module = active();
         if (module == null) {
            return false;
         } else {
            boolean own = mc.field_1724 != null && player.field_53528 == mc.field_1724.method_5628();
            return own ? module.self.get() : module.others.get();
         }
      } else {
         return false;
      }
   }

   public static class_1921 layer(class_10055 state) {
      return GlassLayers.player(state.field_53520.comp_1629() == class_7920.field_41122);
   }

   public static int tint() {
      Glass module = active();
      return 0xFF000000 | (module == null ? 14872319 : module.tint.get().packed() & 16777215);
   }

   public static boolean glassGear(Object state) {
      Glass module = active();
      return module != null && module.gear.get() && applies(state);
   }

   public static void gear(boolean on) {
      GlassGear.set(on);
   }

   public static class_1921 item(class_1921 layer, class_811 context) {
      Glass module = active();
      if (module == null) {
         return layer;
      } else {
         boolean gui = context == class_811.field_4317;
         if (gui ? module.inventory.get() : module.items.get()) {
            if (layer == class_4722.method_29382()) {
               return GlassLayers.item(false, gui);
            } else {
               return layer != class_4722.method_76545() && layer != class_4722.method_24074() && layer != class_4722.method_24073()
                  ? layer
                  : GlassLayers.item(true, gui);
            }
         } else {
            return layer;
         }
      }
   }
}
