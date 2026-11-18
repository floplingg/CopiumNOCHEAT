package dev.violet.event.impl;

import dev.violet.event.CancellableEvent;
import dev.violet.event.CancellableEventType;

public final class ChatSendEvent extends CancellableEvent {
   public static final CancellableEventType<ChatSendEvent> TYPE = new CancellableEventType<>("ChatSendEvent");
   public String message;
   private static final ChatSendEvent INSTANCE = new ChatSendEvent();

   private ChatSendEvent() {
   }

   public static boolean dispatch(String message) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.message = message;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         boolean cancelled = INSTANCE.isCancelled();
         INSTANCE.message = null;
         return cancelled;
      }
   }
}
