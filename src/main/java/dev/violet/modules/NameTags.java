package dev.violet.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import net.minecraft.class_8824;

public class NameTags extends Module {
   private static volatile NameTags instance;
   private static volatile String cachedFrom = "";
   private static volatile String cachedTo = "";
   private static volatile Pattern cachedPattern;
   private static final String DEV_REFERENCE = "ArchivePedro";
   private final Setting<Boolean> showOwn = this.add(new BoolSetting("Show Own", "Shows your own name tag above your head in third person (F5).", true));
   private final Setting<String> customName = this.add(
         new StringSetting(
            "Custom Name",
            "Replaces your real name everywhere on your screen: name tag, chat, tab list and scoreboard. Only you see it. Turns the module on automatically.",
            ""
         )
      )
      .onChanged(this::nameChanged);
   private final Setting<NameTags.FakeRole> fakeRole;
   private final Setting<String> copyFrom;
   private final Setting<String> devIcon;
   private final Setting<String> staffIcon;
   private final Setting<String> copyIcon;
   private String decodedFrom;
   private class_2561 decoded;
   private int ticks;
   private boolean warned;

   public NameTags() {
      super("Name Tags", "Shows your own name tag and lets you change the name you see for yourself.", Category.VISUALS);
      this.group("Fake Role");
      this.fakeRole = this.add(
            new EnumSetting<>(
               "Fake Role",
               "Puts a DonutSMP role icon in front of your own name tag: Developer is the purple icon, Staff the green one. Copy takes the icon of any player in the tab list. Only you see it.",
               NameTags.FakeRole.None
            )
         )
         .onChanged(v -> {
            if (v != NameTags.FakeRole.None && !this.isActive()) {
               this.setEnabled(true);
            }

            this.warned = false;
         });
      this.copyFrom = this.add(new StringSetting("Copy From", "Player in the tab list whose role icon gets copied.", ""))
         .visible(() -> this.fakeRole.get() == NameTags.FakeRole.Copy);
      this.devIcon = this.add(new StringSetting("Dev Icon", "", "", true)).visible(() -> false);
      this.staffIcon = this.add(new StringSetting("Staff Icon", "", "", true)).visible(() -> false);
      this.copyIcon = this.add(new StringSetting("Copy Icon", "", "", true)).visible(() -> false);
      instance = this;
   }

   public static String customName() {
      return instance == null ? "" : instance.customName.get();
   }

   public static void setCustomName(String name) {
      if (instance != null) {
         instance.customName.set(name == null ? "" : name);
      }
   }

   private void nameChanged(String value) {
      if (value != null && !value.isBlank() && !this.isActive()) {
         this.setEnabled(true);
      }

      refreshChat();
   }

   @Override
   public void onEnable() {
      refreshChat();
   }

   @Override
   public void onDisable() {
      refreshChat();
   }

   private static void refreshChat() {
      class_310 mc = class_310.method_1551();
      if (mc != null) {
         mc.execute(() -> {
            if (mc.field_1705 != null) {
               mc.field_1705.method_1743().method_1817();
            }
         });
      }
   }

   @Override
   public void onTick() {
      if (++this.ticks % 40 == 0 && mc.method_1562() != null) {
         this.learn();
         if (this.fakeRole.get() != NameTags.FakeRole.None && this.icon() == null && !this.warned && this.ticks > 200) {
            this.warned = true;

            String who = switch ((NameTags.FakeRole)this.fakeRole.get()) {
               case Developer -> "ArchivePedro";
               case Copy -> this.copyFrom.get().isBlank() ? "the player in Copy From" : this.copyFrom.get().trim();
               default -> "a staff member";
            };
            this.warning("No role icon saved yet. It gets copied from the tab list as soon as %s is online once.", new Object[]{who});
         }
      }
   }

   private void learn() {
      Set<String> staff = new HashSet<>();

      String copyName = this.copyFrom.get().trim();
      Map<String, Integer> counts = new HashMap<>();
      Map<String, class_2561> prefixes = new HashMap<>();
      String devKey = null;

      for (class_640 entry : mc.method_1562().method_2880()) {
         class_2561 display = entry.method_2971();
         if (display != null) {
            String name = entry.method_2966().name();
            class_2561[] parts = split(display, name);
            if (parts != null) {
               class_2561 prefix = parts[0];
               String key = prefix.getString();
               if (!key.isBlank()) {
                  if (name.equalsIgnoreCase("ArchivePedro")) {
                     devKey = key;
                     store(this.devIcon, prefix);
                  } else if (staff.contains(name.toLowerCase(Locale.ROOT))) {
                     counts.merge(key, 1, Integer::sum);
                     prefixes.putIfAbsent(key, prefix);
                  }

                  if (this.fakeRole.get() == NameTags.FakeRole.Copy && !copyName.isEmpty() && name.equalsIgnoreCase(copyName)) {
                     store(this.copyIcon, prefix);
                  }
               }
            }
         }
      }

      String best = null;
      int bestCount = 0;

      for (Entry<String, Integer> count : counts.entrySet()) {
         if (!count.getKey().equals(devKey) && count.getValue() > bestCount) {
            best = count.getKey();
            bestCount = count.getValue();
         }
      }

      if (best != null) {
         store(this.staffIcon, prefixes.get(best));
      }
   }

   private static void store(Setting<String> setting, class_2561 prefix) {
      String json = encode(prefix);
      if (!json.isEmpty() && !json.equals(setting.get())) {
         setting.set(json);
      }
   }

   private static DynamicOps<JsonElement> ops() {
      class_310 mc = class_310.method_1551();
      return (DynamicOps<JsonElement>)(mc.field_1687 != null ? mc.field_1687.method_30349().method_57093(JsonOps.INSTANCE) : JsonOps.INSTANCE);
   }

   private static String encode(class_2561 text) {
      try {
         return class_8824.field_46597.encodeStart(ops(), text).result().<String>map(JsonElement::toString).orElse("");
      } catch (RuntimeException var2) {
         return "";
      }
   }

   private class_2561 icon() {
      String json = switch ((NameTags.FakeRole)this.fakeRole.get()) {
         case Developer -> (String)this.devIcon.get();
         case Staff -> (String)this.staffIcon.get();
         case Copy -> (String)this.copyIcon.get();
         default -> "";
      };
      if (json.isEmpty()) {
         return null;
      } else if (json.equals(this.decodedFrom)) {
         return this.decoded;
      } else {
         this.decodedFrom = json;

         try {
            this.decoded = (class_2561)class_8824.field_46597.parse(ops(), JsonParser.parseString(json)).result().orElse(null);
         } catch (RuntimeException var3) {
            this.decoded = null;
         }

         return this.decoded;
      }
   }

   public static class_2561 decorate(class_1297 entity, class_2561 displayName) {
      class_310 mc = class_310.method_1551();
      if (displayName != null && entity == mc.field_1724) {
         NameTags module = instance;
         if (module != null && module.isActive() && module.fakeRole.get() != NameTags.FakeRole.None) {
            class_2561 icon = module.icon();
            if (icon == null) {
               return displayName;
            } else {
               class_2561[] parts = split(displayName, mc.field_1724.method_7334().name());
               return (class_2561)(parts == null ? class_2561.method_43473().method_10852(icon.method_27661()).method_10852(displayName) : join(parts, icon));
            }
         } else {
            return displayName;
         }
      } else {
         return displayName;
      }
   }

   public static boolean isOwn(class_640 entry) {
      class_310 mc = class_310.method_1551();
      return entry != null && mc.field_1724 != null && entry.method_2966().id().equals(mc.field_1724.method_5667());
   }

   public static class_2561 decorateText(class_2561 text) {
      NameTags module = instance;
      if (module != null && text != null && module.isActive() && module.fakeRole.get() != NameTags.FakeRole.None) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 == null) {
            return text;
         } else {
            String name = mc.field_1724.method_7334().name();
            if (!containsName(text.getString(), name)) {
               return text;
            } else {
               class_2561 icon = module.icon();
               if (icon == null) {
                  return text;
               } else {
                  class_2561[] parts = split(text, name);
                  return parts == null ? text : join(parts, icon);
               }
            }
         }
      } else {
         return text;
      }
   }

   private static class_2561 join(class_2561[] parts, class_2561 icon) {
      class_5250 out = class_2561.method_43473();
      if (!iconOnly(parts[0].getString())) {
         out.method_10852(parts[0]);
      }

      return out.method_10852(icon.method_27661()).method_10852(parts[1]);
   }

   private static boolean iconOnly(String text) {
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         boolean icon = Character.isWhitespace(cp) || cp >= 57344 && cp <= 63743 || cp >= 983040;
         if (!icon) {
            return false;
         }

         i += Character.charCount(cp);
      }

      return true;
   }

   private static boolean containsName(String text, String name) {
      if (name != null && !name.isEmpty()) {
         int length = name.length();

         for (int i = 0; i + length <= text.length(); i++) {
            if (text.regionMatches(true, i, name, 0, length)) {
               boolean before = i == 0 || !isNameChar(text.charAt(i - 1));
               boolean after = i + length == text.length() || !isNameChar(text.charAt(i + length));
               if (before && after) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isNameChar(char c) {
      return c == '_' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
   }

   static class_2561[] split(class_2561 text, String name) {
      if (name != null && !name.isEmpty()) {
         String lower = name.toLowerCase(Locale.ROOT);
         if (!text.getString().toLowerCase(Locale.ROOT).contains(lower)) {
            return null;
         } else {
            List<class_2561> siblings = text.method_10855();
            String own = text.method_27662().getString();
            if (!siblings.isEmpty() && !own.toLowerCase(Locale.ROOT).contains(lower)) {
               int index = -1;

               for (int i = 0; i < siblings.size(); i++) {
                  if (siblings.get(i).getString().toLowerCase(Locale.ROOT).contains(lower)) {
                     index = i;
                     break;
                  }
               }

               if (index >= 0) {
                  class_5250 prefix = text.method_27662().method_10862(text.method_10866());

                  for (int ix = 0; ix < index; ix++) {
                     prefix.method_10852(siblings.get(ix).method_27661());
                  }

                  class_5250 rest = class_2561.method_43473().method_10862(text.method_10866());
                  class_2561[] inner = split(siblings.get(index), name);
                  if (inner != null && !inner[0].getString().isEmpty()) {
                     prefix.method_10852(inner[0]);
                     rest.method_10852(inner[1]);
                  } else {
                     rest.method_10852(siblings.get(index).method_27661());
                  }

                  for (int ix = index + 1; ix < siblings.size(); ix++) {
                     rest.method_10852(siblings.get(ix).method_27661());
                  }

                  return new class_2561[]{prefix, rest};
               }
            }

            List<class_2583> styles = new ArrayList<>();
            List<String> strings = new ArrayList<>();
            text.method_27658((stylex, string) -> {
               styles.add(stylex);
               strings.add(string);
               return Optional.empty();
            }, class_2583.field_24360);
            class_5250 prefix = class_2561.method_43473();
            class_5250 rest = class_2561.method_43473();
            boolean found = false;

            for (int ix = 0; ix < strings.size(); ix++) {
               String s = strings.get(ix);
               class_2583 style = styles.get(ix);
               if (found) {
                  rest.method_10852(class_2561.method_43470(s).method_10862(style));
               } else {
                  int at = s.toLowerCase(Locale.ROOT).indexOf(lower);
                  if (at < 0) {
                     prefix.method_10852(class_2561.method_43470(s).method_10862(style));
                  } else {
                     if (at > 0) {
                        prefix.method_10852(class_2561.method_43470(s.substring(0, at)).method_10862(style));
                     }

                     rest.method_10852(class_2561.method_43470(s.substring(at)).method_10862(style));
                     found = true;
                  }
               }
            }

            return found ? new class_2561[]{prefix, rest} : null;
         }
      } else {
         return null;
      }
   }

   public static boolean showOwnTag(class_1297 entity) {
      NameTags module = instance;
      class_310 mc = class_310.method_1551();
      if (module == null || !module.isActive() || !module.showOwn.get() || entity != mc.field_1724) {
         return false;
      } else if (!class_310.method_1498() || mc.field_1690.method_31044().method_31034()) {
         return false;
      } else {
         return !(mc.field_1755 instanceof class_490) && !(mc.field_1755 instanceof class_481) ? !entity.method_5756(mc.field_1724) : false;
      }
   }

   private static boolean containsIgnoreCase(String text, String part) {
      int length = part.length();

      for (int i = 0; i + length <= text.length(); i++) {
         if (text.regionMatches(true, i, part, 0, length)) {
            return true;
         }
      }

      return false;
   }

   public static String replace(String text) {
      NameTags module = instance;
      if (module != null && text != null && !text.isEmpty() && module.isActive()) {
         String to = module.customName.get().trim();
         if (to.isEmpty()) {
            return text;
         } else {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1548() != null) {
               String from = mc.method_1548().method_1676();
               if (from != null && !from.isEmpty() && !from.equals(to) && containsIgnoreCase(text, from)) {
                  Pattern pattern = cachedPattern;
                  if (pattern == null || !from.equals(cachedFrom) || !to.equals(cachedTo)) {
                     pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(from) + "(?![A-Za-z0-9_])", 2);
                     cachedFrom = from;
                     cachedTo = to;
                     cachedPattern = pattern;
                  }

                  try {
                     return pattern.matcher(text).replaceAll(Matcher.quoteReplacement(to));
                  } catch (RuntimeException var7) {
                     SoberClient.LOG.debug("Name replace failed", var7);
                     return text;
                  }
               } else {
                  return text;
               }
            } else {
               return text;
            }
         }
      } else {
         return text;
      }
   }

   public static enum FakeRole {
      None,
      Developer,
      Staff,
      Copy;
   }
}
