package dev.violet.util;

import com.mojang.authlib.GameProfile;
import dev.violet.gui.Notifications;
import dev.violet.modules.Hud;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1934;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;

public final class StaffList {
   private static final Pattern STAFF_TAG = Pattern.compile(
      "(?i)(?<![a-z])(?:owner|admin(?:istrator)?|sr[ ._-]?mod(?:erator)?|mod(?:erator)?|helper|staff|support|developer|dev|operator)(?![a-z])"
   );
   private static final Pattern ACCOUNT_NAME = Pattern.compile("[.*]?[A-Za-z0-9_]{1,16}");
   private static final int SCAN_INTERVAL_TICKS = 10;
   private static final int SETTLE_TICKS = 60;
   private static final int GROUP_THRESHOLD = 3;
   private static List<StaffList.Member> cache = List.of();
   private static Object cacheHandler;
   private static String cacheConfig = "";
   private static long scanned;
   private static StaffMatcher ranks;
   private static String ranksConfig;
   private static Object alertHandler;
   private static Map<UUID, StaffList.Member> tracked = new LinkedHashMap<>();
   private static int settleTicks;
   private static int ticksToScan;
   private static boolean primed;

   private StaffList() {
   }

   public static List<StaffList.Member> online(class_310 mc, Hud hud) {
      if (mc.method_1562() != null && hud != null) {
         long now = System.currentTimeMillis();
         String config = hud.staffNames.get();
         if (cacheHandler == mc.method_1562() && cacheConfig.equals(config) && now - scanned < 1000L) {
            return cache;
         } else {
            StaffMatcher matcher = matcher(config);
            Set<String> known = new HashSet<>();

            for (String part : config.split(",")) {
               String name = part.trim().toLowerCase(Locale.ROOT);
               if (!name.isEmpty()) {
                  known.add(name);
               }
            }

            List<class_640> tabOrder = new ArrayList<>(mc.method_1562().method_45732());
            tabOrder.sort(
               Comparator.<class_640>comparingInt(e -> e.method_2958() == class_1934.field_9219 ? 1 : 0)
                  .thenComparingInt(e -> -e.method_62154())
                  .thenComparing(e -> e.method_2955() == null ? "" : e.method_2955().method_1197())
                  .thenComparing(e -> e.method_2966().name(), String.CASE_INSENSITIVE_ORDER)
            );
            List<StaffList.Member> result = new ArrayList<>();

            for (class_640 entry : tabOrder) {
               GameProfile profile = entry.method_2966();
               UUID id = profile.id();
               String name = profile.name();
               if (id != null && isAccount(id, name)) {
                  String labels = labels(mc, entry);
                  if (known.contains(name.toLowerCase(Locale.ROOT)) || STAFF_TAG.matcher(labels).find()) {
                     class_268 team = entry.method_2955();
                     StaffMatcher.Match match = matcher.match(
                        name,
                        team == null ? null : team.method_1197(),
                        plain(entry.method_2971()),
                        team == null ? null : plain(team.method_1144()),
                        team == null ? null : plain(team.method_1136())
                     );
                     if (match != null) {
                        result.add(new StaffList.Member(entry, match.rank(), match.seniority()));
                     } else {
                        result.add(new StaffList.Member(entry, legacyRole(labels), 2147483647));
                     }
                  }
               }
            }

            result.sort(Comparator.comparingInt(StaffList.Member::seniority));
            cacheHandler = mc.method_1562();
            cacheConfig = config;
            scanned = now;
            cache = List.copyOf(result);
            return cache;
         }
      } else {
         return List.of();
      }
   }

   public static void tick(class_310 mc, Hud hud) {
      class_634 connection = mc.method_1562();
      if (connection != null && mc.field_1724 != null && hud != null && hud.staffList.get() && hud.staffAlerts.get()) {
         if (connection != alertHandler) {
            alertHandler = connection;
            tracked.clear();
            settleTicks = 60;
            primed = false;
         }

         if (settleTicks > 0) {
            settleTicks--;
         }

         if (--ticksToScan <= 0) {
            ticksToScan = 10;
            Map<UUID, StaffList.Member> current = new LinkedHashMap<>();

            for (StaffList.Member member : online(mc, hud)) {
               current.put(member.entry().method_2966().id(), member);
            }

            List<StaffList.Member> joined = new ArrayList<>();
            List<StaffList.Member> left = new ArrayList<>();

            for (Entry<UUID, StaffList.Member> entry : current.entrySet()) {
               if (!tracked.containsKey(entry.getKey())) {
                  joined.add(entry.getValue());
               }
            }

            for (Entry<UUID, StaffList.Member> entryx : tracked.entrySet()) {
               if (!current.containsKey(entryx.getKey())) {
                  left.add(entryx.getValue());
               }
            }

            tracked = current;
            if (!primed) {
               if (settleTicks == 0) {
                  primed = true;
               }
            } else {
               announce(joined, "joined", Notifications.Type.WARNING);
               announce(left, "left", Notifications.Type.SUCCESS);
            }
         }
      } else {
         tracked.clear();
         alertHandler = null;
         primed = false;
      }
   }

   private static void announce(List<StaffList.Member> group, String verb, Notifications.Type type) {
      if (!group.isEmpty()) {
         if (group.size() > 3) {
            List<String> labels = new ArrayList<>(group.size());

            for (StaffList.Member member : group) {
               labels.add(label(member));
            }

            Notifications.push("Staff", group.size() + " staff " + verb + ": " + String.join(", ", labels), type);
         } else {
            for (StaffList.Member member : group) {
               Notifications.push("Staff", label(member) + " " + verb, type);
            }
         }
      }
   }

   private static String label(StaffList.Member member) {
      return member.name() + " (" + member.role() + ")";
   }

   private static StaffMatcher matcher(String config) {
      if (ranks == null || !config.equals(ranksConfig)) {
         ranks = new StaffMatcher(
            "Ranks + Markers + Team",
            "owner, co-owner, manager, admin, administrator, developer, dev, sr-admin, sr-mod, senior-mod, moderator, mod, trial-mod, sr-helper, senior-helper, helper, trial-helper, support, staff",
            "U+2605 U+2606 U+2726 U+2727 U+272A U+272F U+2730 U+2B50",
            false,
            5,
            config,
            ""
         );
         ranksConfig = config;
      }

      return ranks;
   }

   private static boolean isAccount(UUID id, String name) {
      return name != null && id.version() != 2 && ACCOUNT_NAME.matcher(name).matches();
   }

   private static String plain(class_2561 text) {
      return text == null ? null : text.getString();
   }

   private static String labels(class_310 mc, class_640 entry) {
      StringBuilder labels = new StringBuilder(mc.field_1705.method_1750().method_1918(entry).getString());
      if (entry.method_2971() != null) {
         labels.append(' ').append(entry.method_2971().getString());
      }

      class_268 team = entry.method_2955();
      if (team != null) {
         labels.append(' ').append(team.method_1144().getString()).append(' ').append(team.method_1136().getString()).append(' ').append(team.method_1197());
      }

      return labels.toString();
   }

   private static String legacyRole(String labels) {
      Matcher match = STAFF_TAG.matcher(labels);
      if (!match.find()) {
         return "Staff";
      } else {
         String role = match.group().toLowerCase(Locale.ROOT);
         if (role.contains("owner")) {
            return "Owner";
         } else if (role.contains("admin")) {
            return "Admin";
         } else if (role.contains("mod")) {
            return "Mod";
         } else if (role.contains("helper") || role.contains("support")) {
            return "Helper";
         } else {
            return role.contains("dev") ? "Dev" : "Staff";
         }
      }
   }

   public static record Member(class_640 entry, String role, int seniority) {
      public String name() {
         return this.entry.method_2966().name();
      }
   }
}
