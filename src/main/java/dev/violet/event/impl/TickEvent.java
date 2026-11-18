package dev.violet.event.impl;

import dev.violet.event.Event;
import dev.violet.event.EventType;

public final class TickEvent extends Event {
   public static final EventType<TickEvent> TYPE = new EventType<>("TickEvent");
   private static final TickEvent INSTANCE = new TickEvent();

   private TickEvent() {
   }

   public static void dispatch() {
      TYPE.dispatch(INSTANCE);
   }

   public static void dispatchPost() {
      TYPE.dispatchPost(INSTANCE);
   }
}
