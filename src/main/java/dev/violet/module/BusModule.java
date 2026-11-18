package dev.violet.module;

import dev.violet.event.Event;
import dev.violet.event.EventBus;
import dev.violet.event.EventPriority;
import dev.violet.event.EventType;
import dev.violet.event.ListenerHandle;
import dev.violet.event.Stage;
import dev.violet.event.impl.ModuleToggleEvent;
import dev.violet.setting.Setting;
import dev.violet.setting.lite.BooleanSetting;
import dev.violet.setting.lite.LiteSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_3675;

public abstract class BusModule extends Module {
   private final List<ListenerHandle<?>> listeners = new ArrayList<>();
   private final List<LiteSetting> liteSettings = new ArrayList<>();
   private BooleanSetting notifications;
   private boolean registered;
   private boolean holdMode;
   private final String id;

   protected BusModule(String name, String description, ModuleCategory category) {
      this(name.toLowerCase(Locale.ROOT).replace(' ', '-'), name, description, category);
   }

   protected BusModule(String id, String name, String description, ModuleCategory category) {
      super(name, description, category.category());
      this.id = id;
   }

   public final String id() {
      return this.id;
   }

   protected final <S extends LiteSetting> S addSetting(String id, S setting) {
      this.register(setting.build());
      this.liteSettings.add(setting);
      return setting;
   }

   public final List<LiteSetting> settings() {
      return this.liteSettings;
   }

   protected final BooleanSetting usesNotifications() {
      if (this.notifications == null) {
         this.notifications = this.addSetting("notifications", new BooleanSetting("Notifications", true));
      }

      return this.notifications;
   }

   public boolean persistEnabled() {
      return true;
   }

   public boolean enabledByDefault() {
      return false;
   }

   public boolean inConfigs() {
      return true;
   }

   public int settingsVersion() {
      return 0;
   }

   public void migrateSettings(int savedVersion) {
   }

   public boolean isAction() {
      return false;
   }

   public void onClick() {
   }

   protected final <S extends LiteSetting> S addSetting(S setting) {
      return this.addSetting(setting.name(), setting);
   }

   private <T> void register(Setting<T> setting) {
      this.add(setting);
   }

   public final boolean isEnabled() {
      return this.isActive();
   }

   public final String name() {
      return this.name;
   }

   public boolean notificationsOn() {
      return this.notifications == null || this.notifications.get();
   }

   @Override
   public void setEnabled(boolean enabled) {
      if (this.isActive() != enabled) {
         if (enabled) {
            this.registered = EventBus.INSTANCE.register(this) > 0;
            super.setEnabled(true);
         } else {
            super.setEnabled(false);
            this.listeners.forEach(ListenerHandle::unregister);
            this.listeners.clear();
            if (this.registered) {
               this.registered = false;
               EventBus.INSTANCE.unregister(this);
            }
         }

         ModuleToggleEvent.dispatch(this);
      }
   }

   protected final void requestDisable() {
      this.setEnabled(false);
   }

   protected final void setKeybind(int key) {
      this.setKey(key);
   }

   protected final void setHoldMode(boolean holdMode) {
      this.holdMode = holdMode;
   }

   @Override
   public void onTick() {
      if (this.holdMode && this.getKey() >= 0 && mc.method_22683() != null && !class_3675.method_15987(mc.method_22683(), this.getKey())) {
         this.setEnabled(false);
      }
   }

   protected <E extends Event> void on(EventType<E> type, Consumer<E> callback) {
      this.on(type, Stage.PRE, EventPriority.NORMAL, callback);
   }

   protected <E extends Event> void on(EventType<E> type, Stage stage, Consumer<E> callback) {
      this.on(type, stage, EventPriority.NORMAL, callback);
   }

   protected <E extends Event> void on(EventType<E> type, Stage stage, EventPriority priority, Consumer<E> callback) {
      this.listeners.add(type.listen(this, stage, priority, callback));
   }
}
