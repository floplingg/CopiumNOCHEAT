package dev.violet.util;

import dev.violet.mixin.PlayerListHudAccessor;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5251;
import net.minecraft.class_642;
import net.minecraft.class_1792.class_9635;

public final class DonutUtils {
   private static final class_310 mc = class_310.method_1551();
   private static final Pattern PRICE_LINE = Pattern.compile("(?i).*price\\s*:.*");
   private static final Pattern PRICE_NUMBER = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([KMBT])?", 2);
   private static final class_5251 WHITE = class_5251.method_27718(class_124.field_1068);
   private static long donutCheckedAt;
   private static boolean donut;

   private DonutUtils() {
   }

   public static double parsePrice(String text) {
      if (text == null) {
         return -1.0;
      } else {
         String s = text.trim().replace(",", "").replace("$", "").toUpperCase(Locale.ROOT);
         if (s.isEmpty()) {
            return -1.0;
         } else {
            double multiplier = 1.0;
            char last = s.charAt(s.length() - 1);
            switch (last) {
               case 'B':
                  multiplier = 1.0E9;
                  break;
               case 'K':
                  multiplier = 1000.0;
                  break;
               case 'M':
                  multiplier = 1000000.0;
                  break;
               case 'T':
                  multiplier = 1.0E12;
            }

            if (multiplier != 1.0) {
               s = s.substring(0, s.length() - 1).trim();
            }

            try {
               return Double.parseDouble(s) * multiplier;
            } catch (NumberFormatException var6) {
               return -1.0;
            }
         }
      }
   }

   public static String formatPrice(double price) {
      if (price >= 1.0E9) {
         return String.format(Locale.ROOT, "%.2fB", price / 1.0E9);
      } else if (price >= 1000000.0) {
         return String.format(Locale.ROOT, "%.2fM", price / 1000000.0);
      } else {
         return price >= 1000.0 ? String.format(Locale.ROOT, "%.2fK", price / 1000.0) : String.format(Locale.ROOT, "%.2f", price);
      }
   }

   public static List<class_2561> tooltip(class_1799 stack) {
      return stack.method_7950(class_9635.method_59528(mc.field_1687), mc.field_1724, class_1836.field_41070);
   }

   public static double extractPrice(class_1799 stack) {
      for (class_2561 line : tooltip(stack)) {
         String text = line.getString();
         if (PRICE_LINE.matcher(text).matches()) {
            String afterColon = text.substring(text.indexOf(58) + 1).replace(",", "").replace("$", "");
            Matcher matcher = PRICE_NUMBER.matcher(afterColon);
            if (matcher.find()) {
               String unit = matcher.group(2) != null ? matcher.group(2) : "";
               return parsePrice(matcher.group(1) + unit);
            }
         }
      }

      return -1.0;
   }

   public static boolean isSortActive(class_1799 sortButton, String option) {
      for (class_2561 line : tooltip(sortButton)) {
         if (line.getString().contains(option)) {
            AtomicBoolean white = new AtomicBoolean(false);
            line.method_27658((style, part) -> {
               if (part.contains(option) && WHITE.equals(style.method_10973())) {
                  white.set(true);
               }

               return Optional.empty();
            }, class_2583.field_24360);
            if (white.get()) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isShulkerBox(class_1799 stack) {
      return !stack.method_7960() && stack.method_7909().toString().contains("shulker_box");
   }

   public static void click(int syncId, int slot, int button, class_1713 action) {
      mc.field_1761.method_2906(syncId, slot, button, action, mc.field_1724);
   }

   public static void command(String command) {
      if (mc.method_1562() != null) {
         mc.method_1562().method_45730(command);
      }
   }

   public static String brand() {
      if (mc.method_1562() == null) {
         return "";
      } else {
         String brand = mc.method_1562().method_52790();
         return brand == null ? "" : brand;
      }
   }

   public static boolean onDonut() {
      long now = System.currentTimeMillis();
      if (now - donutCheckedAt < 1000L) {
         return donut;
      } else {
         donutCheckedAt = now;
         donut = false;
         if (mc.method_1562() == null) {
            return false;
         } else {
            class_642 server = mc.method_1558();
            if (server != null && server.field_3761 != null && server.field_3761.toLowerCase(Locale.ROOT).contains("donut")) {
               donut = true;
               return true;
            } else if (brand().toLowerCase(Locale.ROOT).contains("donut")) {
               donut = true;
               return true;
            } else {
               PlayerListHudAccessor list = (PlayerListHudAccessor)mc.field_1705.method_1750();
               class_2561 header = list.violet$getHeader();
               class_2561 footer = list.violet$getFooter();
               if (header != null && header.getString().toLowerCase(Locale.ROOT).contains("donut")) {
                  donut = true;
                  return true;
               } else {
                  return donut = footer != null && footer.getString().toLowerCase(Locale.ROOT).contains("donut");
               }
            }
         }
      }
   }
}
