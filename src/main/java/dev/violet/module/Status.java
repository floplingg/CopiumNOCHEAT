package dev.violet.module;

import java.util.List;

public interface Status {
   String statusTitle();

   List<Status.Row> statusRows();

   String statusDescription();

   public static record Row(String title, String detail, String badge, int badgeColor, String link) {
   }
}
