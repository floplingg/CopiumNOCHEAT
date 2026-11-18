package dev.violet.event.impl;

import dev.violet.event.CancellableEvent;
import dev.violet.event.CancellableEventType;

public final class MouseInputEvent extends CancellableEvent {
   public static final CancellableEventType<MouseInputEvent> TYPE = new CancellableEventType<>("MouseInputEvent");
   public long window;
   public int button;
   public int action;
   public int mods;
   private static final MouseInputEvent INSTANCE = new MouseInputEvent();

   private MouseInputEvent() {
   }

   public static boolean dispatch(long window, int button, int action, int mods) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.window = window;
         INSTANCE.button = button;
         INSTANCE.action = action;
         INSTANCE.mods = mods;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
