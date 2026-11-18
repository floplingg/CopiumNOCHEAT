package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import net.minecraft.class_10799;
import net.minecraft.class_1306;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1731;
import net.minecraft.class_1735;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.minecraft.class_8566;

public class MediaHider extends Module {
   private static volatile MediaHider instance;
   private final Setting<Boolean> autoFit = this.add(
      new BoolSetting(
         "Auto Fit",
         "Fits the pictures automatically and exactly over the item slots and the hotbar, also over the offhand slot when you hold something there. Turn off to set size and position by hand.",
         true
      )
   );
   private final Setting<Boolean> inventory;
   private final Setting<MediaHider.Area> inventoryArea;
   private final Setting<MediaHider.Picture> inventoryPicture;
   private final Setting<MediaHider.Fit> inventoryFit;
   private final Setting<Integer> inventorySize;
   private final Setting<Integer> inventoryX;
   private final Setting<Integer> inventoryY;
   private final Setting<Boolean> tooltips;
   private final Setting<Boolean> hotbar;
   private final Setting<MediaHider.Picture> hotbarPicture;
   private final Setting<MediaHider.Fit> hotbarFit;
   private final Setting<Integer> hotbarWidth;
   private final Setting<Integer> hotbarHeight;
   private final Setting<Integer> hotbarY;
   private final Setting<Boolean> heldName;
   private final Setting<Integer> opacity;

   public MediaHider() {
      super("Media Hider", "Covers your inventory and hotbar with the Copium Client banner, for videos and streams.", Category.CLIENT);
      this.group("Inventory");
      this.inventory = this.add(
         new BoolSetting(
            "Hide Inventory",
            "Covers your own inventory with the picture while you have it open with E. Chests, shulkers and other containers stay visible.",
            true
         )
      );
      this.inventoryArea = this.add(
            new EnumSetting<>(
               "Inventory Area",
               "Items covers exactly the item slots of your inventory and hotbar row. Window covers the whole inventory window.",
               MediaHider.Area.Items
            )
         )
         .visible(() -> this.inventory.get() && !this.autoFit.get());
      this.inventoryPicture = this.add(new EnumSetting<>("Inventory Picture", "Picture that covers the inventory.", MediaHider.Picture.Banner))
         .visible(this.inventory::get);
      this.inventoryFit = this.add(
            new EnumSetting<>(
               "Inventory Fit",
               "Cover fills the whole area, Contain shows the whole picture on a dark background, Stretch squeezes it to fit.",
               MediaHider.Fit.Cover
            )
         )
         .visible(this.inventory::get);
      this.inventorySize = this.add(new IntSetting("Inventory Size", "Size of the cover compared to the covered area, in percent.", 104, 50, 250))
         .visible(() -> this.inventory.get() && !this.autoFit.get());
      this.inventoryX = this.add(new IntSetting("Inventory Offset X", "Moves the inventory cover left or right.", 0, -300, 300))
         .visible(() -> this.inventory.get() && !this.autoFit.get());
      this.inventoryY = this.add(new IntSetting("Inventory Offset Y", "Moves the inventory cover up or down.", 0, -300, 300))
         .visible(() -> this.inventory.get() && !this.autoFit.get());
      this.tooltips = this.add(new BoolSetting("Hide Tooltips", "Hides item names and tooltips while the inventory is covered.", true))
         .visible(this.inventory::get);
      this.group("Hotbar");
      this.hotbar = this.add(new BoolSetting("Hide Hotbar", "Covers the hotbar at the bottom of the screen.", true));
      this.hotbarPicture = this.add(new EnumSetting<>("Hotbar Picture", "Picture that covers the hotbar.", MediaHider.Picture.Strip)).visible(this.hotbar::get);
      this.hotbarFit = this.add(
            new EnumSetting<>(
               "Hotbar Fit",
               "Cover fills the whole area, Contain shows the whole picture on a dark background, Stretch squeezes it to fit.",
               MediaHider.Fit.Cover
            )
         )
         .visible(this.hotbar::get);
      this.hotbarWidth = this.add(new IntSetting("Hotbar Width", "Width of the cover compared to the hotbar, in percent.", 110, 50, 400))
         .visible(() -> this.hotbar.get() && !this.autoFit.get());
      this.hotbarHeight = this.add(new IntSetting("Hotbar Height", "Height of the cover compared to the hotbar, in percent.", 110, 50, 600))
         .visible(() -> this.hotbar.get() && !this.autoFit.get());
      this.hotbarY = this.add(new IntSetting("Hotbar Offset Y", "Moves the hotbar cover up or down.", 0, -200, 200))
         .visible(() -> this.hotbar.get() && !this.autoFit.get());
      this.heldName = this.add(new BoolSetting("Hide Item Name", "Hides the name of the held item that pops up above the hotbar.", true))
         .visible(this.hotbar::get);
      this.group("Look");
      this.opacity = this.add(new IntSetting("Opacity", "How solid the pictures are in percent.", 100, 10, 100));
      instance = this;
   }

   private static MediaHider active() {
      MediaHider module = instance;
      return module != null && module.isActive() ? module : null;
   }

   private boolean covers(class_437 screen) {
      return this.inventory.get() && (screen instanceof class_490 || screen instanceof class_481);
   }

   public static boolean hidesTooltips(class_437 screen) {
      MediaHider module = active();
      return module != null && module.tooltips.get() && module.covers(screen);
   }

   public static boolean hidesHeldName() {
      MediaHider module = active();
      return module != null && module.hotbar.get() && module.heldName.get();
   }

   public static void coverScreen(class_332 context, class_437 screen, int x, int y, int width, int height, class_1703 handler) {
      MediaHider module = active();
      if (module != null && module.covers(screen)) {
         boolean auto = module.autoFit.get();
         if ((auto || module.inventoryArea.get() == MediaHider.Area.Items) && handler != null) {
            int minX = 2147483647;
            int minY = 2147483647;
            int maxX = -2147483648;
            int maxY = -2147483648;

            for (class_1735 slot : handler.field_7761) {
               if (itemSlot(slot)) {
                  minX = Math.min(minX, slot.field_7873);
                  minY = Math.min(minY, slot.field_7872);
                  maxX = Math.max(maxX, slot.field_7873 + 16);
                  maxY = Math.max(maxY, slot.field_7872 + 16);
               }
            }

            if (minX <= maxX) {
               x += minX - 1;
               y += minY - 1;
               width = maxX - minX + 2;
               height = maxY - minY + 2;
            }
         }

         float scale = auto ? 1.0F : (float)module.inventorySize.get().intValue() / 100.0F;
         int w = Math.round((float)width * scale);
         int h = Math.round((float)height * scale);
         int cx = x + width / 2 + (auto ? 0 : module.inventoryX.get());
         int cy = y + height / 2 + (auto ? 0 : module.inventoryY.get());
         context.method_71048();
         module.picture(context, module.inventoryPicture.get(), module.inventoryFit.get(), cx - w / 2, cy - h / 2, w, h);
      }
   }

   private static boolean itemSlot(class_1735 slot) {
      return slot.field_7871 instanceof class_1661
         ? slot.method_34266() < 36
         : !(slot.field_7871 instanceof class_8566) && !(slot.field_7871 instanceof class_1731);
   }

   public static void coverHotbar(class_332 context) {
      MediaHider module = active();
      class_310 mc = class_310.method_1551();
      if (module != null && module.hotbar.get() && !mc.field_1690.field_1842 && mc.field_1724 != null && !mc.field_1724.method_7325()) {
         int sw = context.method_51421();
         int sh = context.method_51443();
         if (module.autoFit.get()) {
            int left = sw / 2 - 92;
            int right = sw / 2 + 92;
            if (!mc.field_1724.method_6079().method_7960()) {
               if (mc.field_1724.method_6068() == class_1306.field_6183) {
                  left -= 29;
               } else {
                  right += 29;
               }
            }

            module.picture(context, module.hotbarPicture.get(), module.hotbarFit.get(), left, sh - 24, right - left, 24);
         } else {
            int w = Math.round((float)(182 * module.hotbarWidth.get()) / 100.0F);
            int h = Math.round((float)(22 * module.hotbarHeight.get()) / 100.0F);
            int x = sw / 2 - w / 2;
            int y = sh - h - module.hotbarY.get();
            module.picture(context, module.hotbarPicture.get(), module.hotbarFit.get(), x, y, w, h);
         }
      }
   }

   private void picture(class_332 context, MediaHider.Picture picture, MediaHider.Fit fit, int x, int y, int w, int h) {
      if (w > 0 && h > 0) {
         int alpha = Math.round((float)(255 * this.opacity.get()) / 100.0F);
         int color = alpha << 24 | 16777215;
         int tw = picture.width;
         int th = picture.height;
         switch (fit) {
            case Cover: {
               float scale = Math.max((float)w / (float)tw, (float)h / (float)th);
               int rw = Math.max(1, Math.round((float)w / scale));
               int rh = Math.max(1, Math.round((float)h / scale));
               float u = (float)(tw - rw) / 2.0F;
               float v = (float)(th - rh) / 2.0F;
               context.method_25293(class_10799.field_56883, picture.texture, x, y, u, v, w, h, rw, rh, tw, th, color);
               break;
            }
            case Contain: {
               context.method_25294(x, y, x + w, y + h, alpha << 24 | 921104);
               float scale = Math.min((float)w / (float)tw, (float)h / (float)th);
               int dw = Math.max(1, Math.round((float)tw * scale));
               int dh = Math.max(1, Math.round((float)th * scale));
               context.method_25293(class_10799.field_56883, picture.texture, x + (w - dw) / 2, y + (h - dh) / 2, 0.0F, 0.0F, dw, dh, tw, th, tw, th, color);
               break;
            }
            case Stretch:
               context.method_25293(class_10799.field_56883, picture.texture, x, y, 0.0F, 0.0F, w, h, tw, th, tw, th, color);
         }
      }
   }

   @Override
   public boolean showInHud() {
      return false;
   }

   public static enum Area {
      Items,
      Window;
   }

   public static enum Fit {
      Cover,
      Contain,
      Stretch;
   }

   public static enum Picture {
      Banner("Copium Banner", "sober_banner", 1774, 887),
      Discord("Discord Banner", "sober_discord", 1774, 887),
      Strip("Copium Strip", "sober_strip", 1774, 95),
      Bottle("Copium Bottle", "sober_bottle", 512, 512);

      private final String title;
      private final class_2960 texture;
      private final int width;
      private final int height;

      private Picture(String title, String file, int width, int height) {
         this.title = title;
         this.texture = class_2960.method_60655("violet", "textures/mediahider/" + file + ".png");
         this.width = width;
         this.height = height;
      }

      @Override
      public String toString() {
         return this.title;
      }
   }
}
