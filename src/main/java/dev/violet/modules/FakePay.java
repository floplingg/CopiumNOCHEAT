package dev.violet.modules;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_5251;

public class FakePay extends Module {
   private final Setting<String> icon = this.add(
      new StringSetting("Icon", "Text put in front of the player name where %icon% is used, for example a rank.", "")
   );
   private final Setting<String> chatPaid;
   private final Setting<String> actionbarPaid;
   private final Setting<String> chatReceived;
   private final Setting<String> actionbarReceived;
   private final Setting<String> amounts;

   public FakePay() {
      super(
         "Fake Pay",
         "While it is on, /pay <player> <amount> is not sent to the server and only shows the pay message in your own chat and action bar. /fakepay send and /fakepay receive show a message without paying. Nothing is sent and nobody else sees it.",
         Category.DONUT
      );
      this.group("Messages");
      this.chatPaid = this.add(
         new StringSetting(
            "Chat Paid",
            "Chat line after /pay. Use %player%, %amount%, %amount_grouped% and %icon%, & color codes and &#RRGGBB. Empty shows nothing.",
            "&fYou paid %icon%%player% &a$ %amount%"
         )
      );
      this.actionbarPaid = this.add(
         new StringSetting("Action Bar Paid", "Action bar text after /pay. Empty shows nothing.", "&fYou paid %player% &a$ %amount%")
      );
      this.chatReceived = this.add(
         new StringSetting("Chat Received", "Chat line for /fakepay receive. Empty shows nothing.", "&f%icon%%player% paid you &a$ %amount%")
      );
      this.actionbarReceived = this.add(
         new StringSetting("Action Bar Received", "Action bar text for /fakepay receive. Empty shows nothing.", "&f%player% paid you &a$ %amount%")
      );
      this.amounts = this.add(
         new StringSetting("Amounts", "Amounts suggested when you press Tab in /fakepay, separated by commas.", "100, 1000, 10000, 100000, 1000000")
      );
      ClientSendMessageEvents.ALLOW_COMMAND.register(this::allowCommand);
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> this.register(dispatcher));
   }

   private boolean allowCommand(String command) {
      if (!this.isActive()) {
         return true;
      } else {
         String[] parts = command.trim().split("\\s+", 3);
         if (parts.length >= 3 && parts[0].equalsIgnoreCase("pay")) {
            mc.execute(() -> this.showPaid(parts[1], parts[2].trim()));
            return false;
         } else {
            return true;
         }
      }
   }

   private void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
      SuggestionProvider<FabricClientCommandSource> players = (context, builder) -> class_2172.method_9265(
            ((FabricClientCommandSource)context.getSource()).method_9262(), builder
         );
      SuggestionProvider<FabricClientCommandSource> money = (context, builder) -> class_2172.method_9265(this.amountList(), builder);
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal(
                              "fakepay"
                           )
                           .executes(context -> this.status((FabricClientCommandSource)context.getSource())))
                        .then(ClientCommandManager.literal("on").executes(context -> this.intercept((FabricClientCommandSource)context.getSource(), true))))
                     .then(ClientCommandManager.literal("off").executes(context -> this.intercept((FabricClientCommandSource)context.getSource(), false))))
                  .then(
                     ClientCommandManager.literal("toggle")
                        .executes(context -> this.intercept((FabricClientCommandSource)context.getSource(), !this.isActive()))
                  ))
               .then(
                  ClientCommandManager.literal("send")
                     .then(
                        ClientCommandManager.argument("player", StringArgumentType.word())
                           .suggests(players)
                           .then(
                              ClientCommandManager.argument("amount", StringArgumentType.greedyString())
                                 .suggests(money)
                                 .executes(context -> this.run(context, true))
                           )
                     )
               ))
            .then(
               ClientCommandManager.literal("receive")
                  .then(
                     ClientCommandManager.argument("player", StringArgumentType.word())
                        .suggests(players)
                        .then(
                           ClientCommandManager.argument("amount", StringArgumentType.greedyString())
                              .suggests(money)
                              .executes(context -> this.run(context, false))
                        )
                  )
            )
      );
   }

   private int run(CommandContext<FabricClientCommandSource> context, boolean paid) {
      String player = StringArgumentType.getString(context, "player");
      String amount = StringArgumentType.getString(context, "amount").trim();
      if (paid) {
         this.showPaid(player, amount);
      } else {
         this.showReceived(player, amount);
      }

      return 1;
   }

   private int intercept(FabricClientCommandSource source, boolean on) {
      this.setEnabled(on);
      SoberClient.CONFIG.save();
      return this.status(source);
   }

   private int status(FabricClientCommandSource source) {
      source.sendFeedback(
         parse(this.isActive() ? "&7Fake Pay: /pay is &ablocked&7 and only shows the message." : "&7Fake Pay: /pay is &csent to the server&7 as normal.")
      );
      return 1;
   }

   private List<String> amountList() {
      List<String> list = new ArrayList<>();

      for (String part : this.amounts.get().split(",")) {
         String value = part.trim();
         if (!value.isEmpty()) {
            list.add(value);
         }
      }

      return list;
   }

   private void showPaid(String player, String amount) {
      this.show(this.chatPaid.get(), this.actionbarPaid.get(), player, amount);
   }

   private void showReceived(String player, String amount) {
      this.show(this.chatReceived.get(), this.actionbarReceived.get(), player, amount);
   }

   private void show(String chat, String actionbar, String player, String amount) {
      if (mc.field_1705 != null) {
         String chatLine = this.fill(chat, player, amount);
         String barLine = this.fill(actionbar, player, amount);
         if (!chatLine.isBlank()) {
            mc.field_1705.method_1743().method_1812(parse(chatLine));
         }

         if (!barLine.isBlank()) {
            mc.field_1705.method_1758(parse(barLine), false);
         }
      }
   }

   private String fill(String template, String player, String amount) {
      return template == null
         ? ""
         : template.replace("%icon%", this.icon.get()).replace("%player%", player).replace("%amount_grouped%", grouped(amount)).replace("%amount%", amount);
   }

   static String grouped(String amount) {
      try {
         String plain = amount.replace(",", "").replace("_", "").trim();
         if (plain.contains(".")) {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            format.setMaximumFractionDigits(2);
            return format.format(Double.parseDouble(plain));
         } else {
            return NumberFormat.getIntegerInstance(Locale.US).format(Long.parseLong(plain));
         }
      } catch (NumberFormatException var3) {
         return amount;
      }
   }

   static class_5250 parse(String input) {
      class_5250 out = class_2561.method_43473();
      if (input != null && !input.isEmpty()) {
         class_2583 style = class_2583.field_24360;
         StringBuilder pending = new StringBuilder();

         for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == 167) && i + 1 < input.length()) {
               char code = Character.toLowerCase(input.charAt(i + 1));
               if (code == '#' && i + 7 < input.length()) {
                  try {
                     int rgb = Integer.parseInt(input.substring(i + 2, i + 8), 16);
                     flush(out, pending, style);
                     style = class_2583.field_24360.method_27703(class_5251.method_27717(rgb));
                     i += 7;
                     continue;
                  } catch (NumberFormatException var8) {
                  }
               }

               class_124 formatting = class_124.method_544(code);
               if (formatting != null) {
                  flush(out, pending, style);
                  if (formatting == class_124.field_1070) {
                     style = class_2583.field_24360;
                  } else if (formatting.method_543()) {
                     style = class_2583.field_24360.method_27706(formatting);
                  } else {
                     style = style.method_27706(formatting);
                  }

                  i++;
                  continue;
               }
            }

            pending.append(c);
         }

         flush(out, pending, style);
         return out;
      } else {
         return out;
      }
   }

   private static void flush(class_5250 out, StringBuilder pending, class_2583 style) {
      if (!pending.isEmpty()) {
         out.method_10852(class_2561.method_43470(pending.toString()).method_10862(style));
         pending.setLength(0);
      }
   }

   @Override
   public boolean showInHud() {
      return false;
   }
}
