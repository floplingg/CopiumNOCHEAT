package dev.violet.event.impl;

import dev.violet.event.Event;
import dev.violet.event.EventType;
import dev.violet.module.BusModule;

public final class ModuleToggleEvent extends Event {
   public static final EventType<ModuleToggleEvent> TYPE = new EventType<>("ModuleToggleEvent");
   private static int silentDepth;
   public final BusModule module;
   public final boolean enabled;

   private ModuleToggleEvent(BusModule module, boolean enabled) {
      this.module = module;
      this.enabled = enabled;
   }

   public static void dispatch(BusModule module) {
      if (silentDepth <= 0 && TYPE.hasPreListeners()) {
         TYPE.dispatch(new ModuleToggleEvent(module, module.isEnabled()));
      }
   }

   public static void silently(Runnable changes) {
      silentDepth++;

      try {
         changes.run();
      } finally {
         silentDepth--;
      }
   }
}
