package dev.violet.gui;

public final class HudPalette {
   public static final int CARD = -233301989;
   public static final int BORDER = -13750733;
   public static final int INNER = 351597813;
   public static final int INNER_BORDER = 653587701;
   public static final int PRESSED = -218827531;
   public static final int TEXT = -723723;
   public static final int MUTED = -5329226;
   public static final int HEADER = -2565922;
   public static final int GLASS_VEIL = -2146430958;

   private HudPalette() {
   }

   public static int card() {
      return Theme.opacity(-233301989);
   }
}
