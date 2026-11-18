package dev.violet.event.impl;

import dev.violet.event.CancellableEvent;
import dev.violet.event.CancellableEventType;

public final class KeyEvent extends CancellableEvent {
   public static final CancellableEventType<KeyEvent> TYPE = new CancellableEventType<>("KeyEvent");
   public int key;
   public int scancode;
   public int action;
   public int mods;
   private static final KeyEvent INSTANCE = new KeyEvent();

   private KeyEvent() {
   }

   public static boolean dispatch(int key, int scancode, int action, int mods) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.key = key;
         INSTANCE.scancode = scancode;
         INSTANCE.action = action;
         INSTANCE.mods = mods;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
