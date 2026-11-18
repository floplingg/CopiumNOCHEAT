package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.KeySetting;
import dev.violet.setting.Setting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_639;
import net.minecraft.class_642;

public class RelogMethod extends Module {
   private final Setting<RelogMethod.Mode> mode = this.add(
      new EnumSetting<>("Mode", "Height relogs when you pass a Y level. Keybind relogs when you press your relog key.", RelogMethod.Mode.Height)
   );
   private final KeySetting relogKey = (KeySetting)this.add(
         new KeySetting("Relog Key", "Relogs when you press it. Click and press a key. Right click removes it.", () -> null)
      )
      .visible(() -> this.mode.get() == RelogMethod.Mode.Keybind);
   private final Setting<Integer> height = this.add(new IntSetting("Height", "Y level that triggers the relog.", 0, -64, 320))
      .visible(() -> this.mode.get() == RelogMethod.Mode.Height);
   private final Setting<RelogMethod.Trigger> trigger = this.add(
         new EnumSetting<>("Trigger", "Relog when you go below or above the height.", RelogMethod.Trigger.Below)
      )
      .visible(() -> this.mode.get() == RelogMethod.Mode.Height);
   private final Setting<Boolean> rejoin = this.add(new BoolSetting("Rejoin", "Joins the server again right after the logout.", true));
   private final Setting<Integer> delay = this.add(new IntSetting("Rejoin Delay", "Milliseconds to wait before joining again.", 1000, 0, 10000))
      .visible(this.rejoin::get);
   private final Setting<Boolean> autoOff = this.add(
      new BoolSetting("Auto Off", "Turns Relog Method off after one relog. Off keeps it running for the next relog.", false)
   );
   private boolean armed;
   private boolean joinChecked;
   private boolean keyDown;
   private class_642 server;
   private long rejoinAt = -1L;
   private int triggeredY;
   private boolean byKey;

   public RelogMethod() {
      super("Relog Method", "Logs out when you reach a set height or press your relog key, and joins again right away.", Category.DONUT);
      ClientTickEvents.END_CLIENT_TICK.register(this::tickAlways);
   }

   @Override
   public void onEnable() {
      this.armed = mc.field_1724 == null || !this.past(mc.field_1724.method_23318());
      this.keyDown = this.relogKey.isPressed();
      this.rejoinAt = -1L;
   }

   @Override
   public void onDisable() {
      this.rejoinAt = -1L;
   }

   @Override
   public void onGameLeft() {
      this.joinChecked = false;
   }

   private boolean past(double y) {
      return this.trigger.get() == RelogMethod.Trigger.Below ? y <= (double)this.height.get().intValue() : y >= (double)this.height.get().intValue();
   }

   @Override
   public void onTick() {
      if (this.mode.get() != RelogMethod.Mode.Keybind) {
         if (mc.field_1724 != null && !mc.method_1542() && mc.field_1724.field_6012 >= 40) {
            double y = mc.field_1724.method_23318();
            if (!this.joinChecked) {
               this.joinChecked = true;
               if (this.past(y)) {
                  this.armed = false;
               }
            }

            if (!this.armed) {
               if (!this.past(y)) {
                  this.armed = true;
               }
            } else if (this.past(y)) {
               this.relog(y, false);
            }
         }
      } else {
         boolean down = this.relogKey.isPressed();
         boolean pressed = down && !this.keyDown;
         this.keyDown = down;
         if (pressed && mc.field_1724 != null && !mc.method_1542() && mc.field_1724.field_6012 >= 40) {
            this.relog(mc.field_1724.method_23318(), true);
         }
      }
   }

   private void relog(double y, boolean key) {
      class_642 info = mc.method_1558();
      if (info != null) {
         this.armed = false;
         this.server = info;
         this.byKey = key;
         this.triggeredY = (int)Math.floor(y);
         this.rejoinAt = this.rejoin.get() ? System.currentTimeMillis() + (long)this.delay.get().intValue() : -1L;
         if (this.autoOff.get()) {
            this.setEnabledSilently(false);
            SoberClient.CONFIG.save();
         }

         mc.execute(() -> mc.method_73360(class_2561.method_43470("SoberClient Relog Method")));
      }
   }

   private void tickAlways(class_310 client) {
      if (this.rejoinAt >= 0L && this.server != null && System.currentTimeMillis() >= this.rejoinAt) {
         if (client.field_1687 == null && client.method_1562() == null) {
            this.rejoinAt = -1L;
            class_642 info = this.server;
            class_412.method_36877(new class_500(new class_442()), client, class_639.method_2950(info.field_3761), info, false, null);
            if (this.autoOff.get()) {
               this.info("Relogged at Y %d. Relog Method is now off.", new Object[]{this.triggeredY});
            } else if (this.byKey) {
               this.info("Relogged at Y %d. Press %s to relog again.", new Object[]{this.triggeredY, this.relogKey.display()});
            } else {
               this.info(
                  "Relogged at Y %d. Relog Method stays on and triggers again after you went back past Y %d.", new Object[]{this.triggeredY, this.height.get()}
               );
            }
         }
      }
   }

   @Override
   public String getInfoString() {
      return this.mode.get() == RelogMethod.Mode.Keybind
         ? this.relogKey.display()
         : (this.trigger.get() == RelogMethod.Trigger.Below ? "< " : "> ") + this.height.get();
   }

   public static enum Mode {
      Height,
      Keybind;
   }

   public static enum Trigger {
      Below,
      Above;
   }
}
