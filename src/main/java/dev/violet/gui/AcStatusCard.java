package dev.violet.gui;

import java.util.List;
import net.minecraft.class_332;

public final class AcStatusCard {
   private static final String BOLD = "montserrat-bold.ttf";
   private static final float PAD = 6.0F;
   private static final float STATUS_EM = 8.4F;
   private static final float ROW_EM = 6.4F;

   private AcStatusCard() {
   }

   static int width(AcStatusCard.Model model) {
      float headerW = HudRenderer.headerWidth("ANTICHEAT");
      float statusW = 12.0F + CrispText.width(model.status(), "montserrat-bold.ttf", 8.4F, 0.1F);
      float rowsW = 0.0F;

      for (AcStatusCard.Row row : model.rows()) {
         rowsW = Math.max(
            rowsW, CrispText.width(row.place(), "montserrat-bold.ttf", 6.4F, 0.1F) + 12.0F + CrispText.width(row.distance(), "montserrat-bold.ttf", 6.4F, 0.1F)
         );
      }

      return Math.round(Math.max(Math.max(headerW, statusW), rowsW) + 12.0F);
   }

   static int height(AcStatusCard.Model model) {
      return Math.round(38.0F + (float)model.rows().size() * 10.0F);
   }

   static void draw(class_332 context, AcStatusCard.Model model) {
      float time = (float)(System.nanoTime() % 3600000000000L) / 1.0E9F;
      int w = width(model);
      int h = height(model);
      HudRenderer.panel(context, 0, 0, w, h, model.state().color);
      HudRenderer.header(context, "ANTICHEAT", 6.0F, 6.0F);
      float statusY = 27.0F;
      float pulse = model.state() == AcStatusCard.State.OFF ? 0.55F + 0.45F * (float)Math.sin((double)time * 6.0) : 1.0F;
      Smooth.circle(context, Math.round(9.0F), Math.round(statusY - 3.0F), 2, MenuKit.alpha(model.state().color, pulse));
      CrispText.draw(context, model.status(), "montserrat-bold.ttf", 16.0F, statusY, 8.4F, 0.1F, model.state().color);
      float rowY = statusY + 11.0F;

      for (AcStatusCard.Row row : model.rows()) {
         CrispText.draw(context, row.place(), "montserrat-bold.ttf", 6.0F, rowY, 6.4F, 0.1F, -723723);
         float dw = CrispText.width(row.distance(), "montserrat-bold.ttf", 6.4F, 0.1F);
         CrispText.draw(context, row.distance(), "montserrat-bold.ttf", (float)w - 6.0F - dw, rowY, 6.4F, 0.1F, -5329226);
         rowY += 10.0F;
      }
   }

   public static record Model(AcStatusCard.State state, String status, List<AcStatusCard.Row> rows) {
   }

   public static record Row(String place, String distance) {
   }

   public static enum State {
      ON(-11870592),
      OFF(-41846),
      UNKNOWN(-4669752);

      final int color;

      private State(int color) {
         this.color = color;
      }
   }
}
