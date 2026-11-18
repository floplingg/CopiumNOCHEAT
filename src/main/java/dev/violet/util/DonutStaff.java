package dev.violet.util;

import dev.violet.chat.ModuleChat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1934;

public final class DonutStaff {
   public static final List<String> DONUT_STAFF = List.of(
      "FluffyMaster07",
      "archivePedro",
      "Munkerlich",
      "Frenk_Btw",
      "Napooo_",
      "auzzitech",
      "CryptoDaveYT",
      "W1zox_",
      "ZEEF69",
      "showered",
      "CaptainMoose35",
      "BobIsFound",
      "NoahvdAa",
      "0GSummer",
      "LzouzMP5",
      "Pastagamer08",
      "u_vv",
      "Owen1212055",
      "Splaterd",
      "Fallerfly",
      "Bloodspulse",
      "_chaon",
      "Dough4",
      "evify"
   );
   private static final Set<String> STAFF_KEYS = staffKeys();

   private DonutStaff() {
   }

   public static boolean isStaffName(String name) {
      return name != null && STAFF_KEYS.contains(name.toLowerCase(Locale.ROOT));
   }

   public static boolean isInYourRegion(UUID id) {
      return false;
   }

   public static boolean alarmedRecently(UUID id) {
      return false;
   }

   public static ModuleChat.Body switchedTo(ModuleChat.Body body, String mode) {
      if (mode == null) {
         return body.text("switched game mode");
      } else {
         body.text("switched to ");
         if (class_1934.field_9219.method_8381().equals(mode)) {
            return body.danger(mode);
         } else {
            return class_1934.field_9220.method_8381().equals(mode) ? body.warn(mode) : body.name(mode);
         }
      }
   }

   private static Set<String> staffKeys() {
      Set<String> keys = new HashSet<>();

      for (String name : DONUT_STAFF) {
         keys.add(name.toLowerCase(Locale.ROOT));
      }

      return Set.copyOf(keys);
   }
}
