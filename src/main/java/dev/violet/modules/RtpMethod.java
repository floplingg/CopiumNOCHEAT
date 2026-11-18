package dev.violet.modules;

import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.setting.StringSetting;
import dev.violet.util.DonutUtils;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Game;
import net.minecraft.class_1109;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3417;
import net.minecraft.class_3486;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_6908;

public class RtpMethod extends Module {
   private static final Pattern TIME = Pattern.compile("(\\d+)\\s*(s|sec|secs|second|seconds|sekunde|sekunden|m|min|mins|minute|minutes|minuten)?\\b");
   private final Setting<String> command;
   private final Setting<Integer> delay;
   private final Setting<Integer> timeout;
   private final Setting<Integer> maxTries;
   private final Setting<RtpMethod.Mode> mode;
   private final Setting<Integer> testTries;
   private final Setting<Integer> maxDistance;
   private final Setting<Boolean> square;
   private final Setting<Boolean> noWater;
   private final Setting<Boolean> notifyTries;
   private final Setting<Boolean> sound;
   private final Setting<Boolean> copy;
   private RtpMethod.Stage stage;
   private int tries;
   private long nextAt;
   private long deadline;
   private long sentAt;
   private long landedAt;
   private class_243 startPos;
   private class_638 startWorld;
   private class_243 landPos;
   private double best;

   public RtpMethod() {
      super("RTP Method", "Runs a random teleport command again and again until you land as close to 0 0 as it gets.", Category.DONUT);
      this.group("Command");
      this.command = this.add(
         new StringSetting("Command", "Command that gets sent, without the slash. For example rtp east, rtp west or rtp nether.", "rtp east")
      );
      this.delay = this.add(
         new IntSetting(
            "Delay", "Seconds between two tries. Set it to the rtp cooldown of the server, a cooldown message in chat is detected automatically.", 12, 3, 120
         )
      );
      this.timeout = this.add(new IntSetting("Teleport Timeout", "Seconds to wait for the teleport before trying again.", 20, 5, 60));
      this.maxTries = this.add(new IntSetting("Max Tries", "Stops after this many tries. 0 means no limit.", 50, 0, 500));
      this.group("Good Coordinates");
      this.mode = this.add(
         new EnumSetting<>(
            "Mode",
            "Smart looks at a few test spots first and then stops at the first spot that is closer to 0 0 than all spots before. It does not have to be perfect, just as close as it gets. Max Distance only stops inside the max distance.",
            RtpMethod.Mode.Smart
         )
      );
      this.testTries = this.add(new IntSetting("Test Tries", "Spots Smart only looks at to learn how close the rtp usually gets.", 5, 1, 30))
         .visible(() -> this.mode.get() == RtpMethod.Mode.Smart);
      this.maxDistance = this.add(
         new IntSetting(
            "Max Distance",
            "Spots within this distance from 0 0 are always good and stop right away. Hold Ctrl and scroll for steps of 100 blocks.",
            50,
            1,
            1000
         ) {
            @Override
            public String display() {
               return this.value * 100 + " blocks";
            }
         }
      );
      this.square = this.add(
         new BoolSetting("Square Distance", "Measures the distance like a square (the bigger one of X and Z) instead of a circle around 0 0.", false)
      );
      this.noWater = this.add(new BoolSetting("No Ocean", "Skips spots in oceans, rivers or water.", false));
      this.group("Messages");
      this.notifyTries = this.add(new BoolSetting("Notify Tries", "Shows every spot that gets skipped, its distance to 0 0 and the closest spot so far.", true));
      this.sound = this.add(new BoolSetting("Sound", "Plays a sound when good coordinates are found.", true));
      this.copy = this.add(new BoolSetting("Copy Coords", "Copies the good coordinates to your clipboard.", false));
      this.stage = RtpMethod.Stage.SEND;
      this.best = 1.7976931348623157E308;
      ClientReceiveMessageEvents.GAME.register((Game)(message, overlay) -> {
         if (!overlay) {
            this.onMessage(message.getString());
         }
      });
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.command.get().isBlank()) {
         this.tries = 0;
         this.best = 1.7976931348623157E308;
         this.stage = RtpMethod.Stage.SEND;
         this.nextAt = 0L;
         double here = this.distance(mc.field_1724.method_73189());
         if (here <= this.limit()) {
            this.info("You are already %d blocks from 0 0, within the max distance of %d. Starting anyway.", new Object[]{(int)here, (int)this.limit()});
         }
      } else {
         if (this.command.get().isBlank()) {
            this.warning("Set a command first, for example rtp east.", new Object[0]);
         }

         this.setEnabled(false);
      }
   }

   @Override
   public void onGameLeft() {
      if (this.stage != RtpMethod.Stage.TELEPORT) {
         this.stage = RtpMethod.Stage.WAIT;
         this.nextAt = System.currentTimeMillis() + (long)this.delay.get().intValue() * 1000L;
      }
   }

   private double limit() {
      return (double)this.maxDistance.get().intValue() * 100.0;
   }

   private double distance(class_243 pos) {
      return this.square.get()
         ? Math.max(Math.abs(pos.field_1352), Math.abs(pos.field_1350))
         : Math.sqrt(pos.field_1352 * pos.field_1352 + pos.field_1350 * pos.field_1350);
   }

   @Override
   public void onTick() {
      long now = System.currentTimeMillis();
      switch (this.stage) {
         case SEND:
            this.send(now);
            break;
         case TELEPORT:
            this.waitTeleport(now);
            break;
         case CHECK:
            this.check(now);
            break;
         case WAIT:
            if (now >= this.nextAt) {
               this.stage = RtpMethod.Stage.SEND;
            }
      }
   }

   private void send(long now) {
      if (now >= this.nextAt) {
         if (this.maxTries.get() > 0 && this.tries >= this.maxTries.get()) {
            if (this.best < 1.7976931348623157E308) {
               this.warning(
                  "No spot within %d blocks after %d tries. The closest was %d blocks from 0 0.", new Object[]{(int)this.limit(), this.tries, (int)this.best}
               );
            } else {
               this.warning("No spot found after %d tries.", new Object[]{this.tries});
            }

            this.setEnabled(false);
         } else {
            this.tries++;
            this.startPos = mc.field_1724.method_73189();
            this.startWorld = mc.field_1687;
            this.sentAt = now;
            this.deadline = now + (long)this.timeout.get().intValue() * 1000L;
            this.stage = RtpMethod.Stage.TELEPORT;
            String text = this.command.get().trim();
            DonutUtils.command(text.startsWith("/") ? text.substring(1) : text);
         }
      }
   }

   private void waitTeleport(long now) {
      boolean moved = mc.field_1687 != this.startWorld || mc.field_1724.method_73189().method_1025(this.startPos) > 4096.0;
      if (moved) {
         this.stage = RtpMethod.Stage.CHECK;
         this.landedAt = now;
         this.landPos = mc.field_1724.method_73189();
      } else if (now >= this.deadline) {
         if (this.notifyTries.get()) {
            this.info("Try %d: no teleport happened, trying again.", new Object[]{this.tries});
         }

         this.retry(now);
      }
   }

   private void check(long now) {
      class_243 pos = mc.field_1724.method_73189();
      if (pos.method_1025(this.landPos) > 4096.0) {
         this.landPos = pos;
         this.landedAt = now;
      }

      double distance = this.distance(pos);
      boolean within = distance <= this.limit();
      boolean smart = this.mode.get() == RtpMethod.Mode.Smart;
      boolean testing = smart && this.tries <= this.testTries.get();
      boolean closer = smart && !testing && distance < this.best;
      if (!within && !closer) {
         String reason = testing ? "test spot " + this.tries + "/" + this.testTries.get() : (smart ? "not closer than " + (int)this.best : "too far");
         this.best = Math.min(this.best, distance);
         this.reject(now, distance, reason);
      } else {
         if (this.noWater.get()) {
            if (now - this.landedAt < 1500L) {
               return;
            }

            if (this.wet()) {
               this.reject(now, distance, "water or ocean");
               return;
            }
         }

         this.best = Math.min(this.best, distance);
         this.found(distance);
      }
   }

   private boolean wet() {
      class_2338 feet = mc.field_1724.method_24515();
      if (mc.field_1724.method_5799()) {
         return true;
      } else if (mc.field_1687.method_8316(feet.method_10074()).method_15767(class_3486.field_15517)) {
         return true;
      } else {
         class_6880<class_1959> biome = mc.field_1687.method_23753(feet);
         return biome.method_40220(class_6908.field_36509) || biome.method_40220(class_6908.field_36508) || biome.method_40220(class_6908.field_36511);
      }
   }

   private void reject(long now, double distance, String reason) {
      if (this.notifyTries.get()) {
         class_2338 pos = mc.field_1724.method_24515();
         this.info(
            "Try %d: X %d Z %d is %d blocks from 0 0, %s. Closest so far: %d.",
            new Object[]{this.tries, pos.method_10263(), pos.method_10260(), (int)distance, reason, (int)this.best}
         );
      }

      this.retry(now);
   }

   private void retry(long now) {
      this.stage = RtpMethod.Stage.WAIT;
      this.nextAt = Math.max(now + 500L, this.sentAt + (long)this.delay.get().intValue() * 1000L);
   }

   private void found(double distance) {
      class_2338 pos = mc.field_1724.method_24515();
      this.info(
         "Good spot after %d tries: X %d Y %d Z %d, only %d blocks from 0 0.",
         new Object[]{this.tries, pos.method_10263(), pos.method_10264(), pos.method_10260(), (int)distance}
      );
      if (this.sound.get() && mc.method_1483() != null) {
         mc.method_1483().method_4873(class_1109.method_4757(class_3417.field_15195, 1.2F, 0.7F));
      }

      if (this.copy.get()) {
         mc.field_1774.method_1455(pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260());
      }

      this.setEnabled(false);
   }

   private void onMessage(String text) {
      if (this.isActive() && this.stage == RtpMethod.Stage.TELEPORT) {
         String lower = text.toLowerCase(Locale.ROOT);
         long now = System.currentTimeMillis();
         if (!lower.contains("cancel") && !lower.contains("abgebrochen")) {
            boolean cooldown = lower.contains("cooldown")
               || lower.contains("wait")
               || lower.contains("warte")
               || lower.contains("again in")
               || lower.contains("erneut");
            if (cooldown) {
               long wait = (long)this.delay.get().intValue() * 1000L;
               Matcher matcher = TIME.matcher(lower);
               if (matcher.find()) {
                  long amount = Long.parseLong(matcher.group(1));
                  String unit = matcher.group(2);
                  boolean minutes = unit != null && unit.startsWith("m");
                  wait = Math.min(600000L, amount * (minutes ? 60000L : 1000L));
               }

               if (this.notifyTries.get()) {
                  this.info("Cooldown, next try in %d seconds.", new Object[]{(wait + 1000L) / 1000L});
               }

               this.tries = Math.max(0, this.tries - 1);
               this.stage = RtpMethod.Stage.WAIT;
               this.nextAt = now + wait + 1000L;
            }
         } else {
            if (this.notifyTries.get()) {
               this.info("Try %d: teleport was cancelled, trying again.", new Object[]{this.tries});
            }

            this.stage = RtpMethod.Stage.WAIT;
            this.nextAt = now + 2000L;
         }
      }
   }

   @Override
   public String getInfoString() {
      if (this.tries == 0) {
         return null;
      } else {
         return this.best < 1.7976931348623157E308 ? "#" + this.tries + " " + (int)this.best : "#" + this.tries;
      }
   }

   public static enum Mode {
      Smart,
      MaxDistance;
   }

   private static enum Stage {
      SEND,
      TELEPORT,
      CHECK,
      WAIT;
   }
}
