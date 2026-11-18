package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.DonutUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;

public class FakeStats extends Module {
   private static volatile FakeStats instance;
   private static final String SMALL = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
   private static final int LINES = 15;
   private final Setting<String> money;
   private final Setting<String> shards;
   private final Setting<String> kills;
   private final Setting<String> deaths;
   private final Setting<String> playtime;
   private final Setting<Boolean> lineNumbers;
   private final Setting<Integer> moneyLine;
   private final Setting<Integer> shardsLine;
   private final Setting<Integer> killsLine;
   private final Setting<Integer> deathsLine;
   private final Setting<Integer> playtimeLine;
   private final List<FakeStats.Stat> stats;
   private final String[] scoreValues;

   public FakeStats() {
      super("Fake Stats", "Changes your money, shards, kills, deaths and playtime on the scoreboard. Only you see it.", Category.DONUT);
      this.group("Values");
      this.money = this.add(new StringSetting("Money", "Money shown on the scoreboard, for example 25.4B. Empty keeps your real money.", ""))
         .onChanged(this::changed);
      this.shards = this.add(new StringSetting("Shards", "Shards shown on the scoreboard. Empty keeps your real shards.", "")).onChanged(this::changed);
      this.kills = this.add(new StringSetting("Kills", "Kills shown on the scoreboard. Empty keeps your real kills.", "")).onChanged(this::changed);
      this.deaths = this.add(new StringSetting("Deaths", "Deaths shown on the scoreboard. Empty keeps your real deaths.", "")).onChanged(this::changed);
      this.playtime = this.add(new StringSetting("Playtime", "Playtime shown on the scoreboard, for example 30d 4h. Empty keeps your real playtime.", ""))
         .onChanged(this::changed);
      this.group("Lines");
      this.lineNumbers = this.add(
         new BoolSetting(
            "Show Line Numbers",
            "Writes the line number in front of every scoreboard line, so you know which number to pick below. Turn it off again afterwards.",
            false
         )
      );
      this.moneyLine = this.add(line("Money Line", "money"));
      this.shardsLine = this.add(line("Shards Line", "shards"));
      this.killsLine = this.add(line("Kills Line", "kills"));
      this.deathsLine = this.add(line("Deaths Line", "deaths"));
      this.playtimeLine = this.add(line("Playtime Line", "playtime"));
      this.stats = List.of(
         new FakeStats.Stat(this.money, this.moneyLine, new String[]{"money", "balance", "bal", "coins"}, 1),
         new FakeStats.Stat(this.shards, this.shardsLine, new String[]{"shards", "shard"}, 2),
         new FakeStats.Stat(this.kills, this.killsLine, new String[]{"kills", "kill"}, 3),
         new FakeStats.Stat(this.deaths, this.deathsLine, new String[]{"deaths", "death"}, 4),
         new FakeStats.Stat(this.playtime, this.playtimeLine, new String[]{"playtime", "play time", "time played"}, 5)
      );
      this.scoreValues = new String[17];
      instance = this;
   }

   private static IntSetting line(String name, String what) {
      return new IntSetting(
         name,
         "Scoreboard line (counted from the top) that shows your "
            + what
            + ". Auto finds it by its name, and on DonutSMP it uses the usual line. Use Show Line Numbers to see the numbers.",
         0,
         0,
         15
      ) {
         @Override
         public String display() {
            return this.value == 0 ? "Auto" : String.valueOf(this.value);
         }
      };
   }

   private void changed(String value) {
      if (value != null && !value.isBlank() && !this.isActive()) {
         this.setEnabled(true);
      }
   }

   public static void beginSidebar() {
      FakeStats module = instance;
      if (module != null) {
         Arrays.fill(module.scoreValues, null);
      }
   }

   public static class_2561 applyLine(int line, class_2561 text) {
      FakeStats module = instance;
      if (module != null && text != null && module.isActive() && line >= 1 && line <= 15) {
         class_2561 result = text;
         String plain = text.getString();
         String normal = normalize(plain);
         boolean donut = DonutUtils.onDonut();

         for (FakeStats.Stat stat : module.stats) {
            String value = stat.value().get().trim();
            if (!value.isEmpty()) {
               int fixed = stat.line().get();
               int labelEnd = -1;
               if (fixed == 0) {
                  for (String label : stat.labels()) {
                     int at = indexOfWord(normal, label);
                     if (at >= 0) {
                        labelEnd = at + label.length();
                        break;
                     }
                  }

                  if (labelEnd < 0) {
                     if (!donut || stat.donutLine() != line) {
                        continue;
                     }

                     labelEnd = 0;
                  }
               } else {
                  if (fixed != line) {
                     continue;
                  }

                  labelEnd = labelEnd(normal, stat.labels());
               }

               int start = valueStart(plain, Math.max(0, labelEnd));
               if (start < 0) {
                  module.scoreValues[line] = value;
               } else {
                  result = replace(text, start, value);
               }
               break;
            }
         }

         if (module.lineNumbers.get()) {
            result = class_2561.method_43473().method_10852(class_2561.method_43470(line + " ").method_27692(class_124.field_1080)).method_10852(result);
         }

         return result;
      } else {
         return text;
      }
   }

   public static class_2561 applyScore(int line, class_2561 score) {
      FakeStats module = instance;
      if (module != null && score != null && module.isActive() && line >= 1 && line <= 15) {
         String value = module.scoreValues[line];
         if (value != null && !score.getString().isBlank()) {
            class_2583 style = firstStyle(score);
            return class_2561.method_43470(value).method_10862(style);
         } else {
            return score;
         }
      } else {
         return score;
      }
   }

   private static int labelEnd(String normal, String[] labels) {
      for (String label : labels) {
         int at = indexOfWord(normal, label);
         if (at >= 0) {
            return at + label.length();
         }
      }

      return 0;
   }

   private static int indexOfWord(String text, String word) {
      int from = 0;

      while (true) {
         int at = text.indexOf(word, from);
         if (at < 0) {
            return -1;
         }

         boolean before = at == 0 || !Character.isLetter(text.charAt(at - 1));
         boolean after = at + word.length() >= text.length() || !Character.isLetter(text.charAt(at + word.length()));
         if (before && after) {
            return at;
         }

         from = at + 1;
      }
   }

   private static String normalize(String text) {
      StringBuilder out = new StringBuilder(text.length());

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         int small = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ".indexOf(c);
         out.append(small >= 0 ? (char)(97 + small) : Character.toLowerCase(c));
      }

      return out.toString();
   }

   private static int valueStart(String plain, int from) {
      for (int i = from; i < plain.length(); i++) {
         if (Character.isDigit(plain.charAt(i))) {
            return i;
         }
      }

      int colon = plain.indexOf(58, from);
      if (colon >= 0) {
         for (int ix = colon + 1; ix < plain.length(); ix++) {
            if (!Character.isWhitespace(plain.charAt(ix))) {
               return ix;
            }
         }
      }

      return -1;
   }

   private static class_2583 firstStyle(class_2561 text) {
      class_2583[] found = new class_2583[]{null};
      text.method_27658((style, string) -> {
         if (!string.isEmpty() && found[0] == null) {
            found[0] = style;
         }

         return Optional.empty();
      }, class_2583.field_24360);
      return found[0] == null ? class_2583.field_24360 : found[0];
   }

   private static class_2561 replace(class_2561 line, int start, String value) {
      List<class_2583> styles = new ArrayList<>();
      List<String> parts = new ArrayList<>();
      line.method_27658((stylex, string) -> {
         styles.add(stylex);
         parts.add(string);
         return Optional.empty();
      }, class_2583.field_24360);
      class_5250 out = class_2561.method_43473();
      int position = 0;

      for (int i = 0; i < parts.size(); i++) {
         String part = parts.get(i);
         class_2583 style = styles.get(i);
         int end = position + part.length();
         if (end > start) {
            int cut = Math.max(0, start - position);
            if (cut > 0) {
               out.method_10852(class_2561.method_43470(part.substring(0, cut)).method_10862(style));
            }

            out.method_10852(class_2561.method_43470(value).method_10862(style));
            return out;
         }

         out.method_10852(class_2561.method_43470(part).method_10862(style));
         position = end;
      }

      return line;
   }

   @Override
   public String getInfoString() {
      int count = 0;

      for (FakeStats.Stat stat : this.stats) {
         if (!stat.value().get().isBlank()) {
            count++;
         }
      }

      return count == 0 ? null : String.valueOf(count);
   }

   private static record Stat(Setting<String> value, Setting<Integer> line, String[] labels, int donutLine) {
   }
}
