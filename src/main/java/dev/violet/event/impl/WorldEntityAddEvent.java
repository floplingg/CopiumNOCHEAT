package dev.violet.event.impl;

import dev.violet.event.Event;
import dev.violet.event.EventType;
import net.minecraft.class_1297;

public final class WorldEntityAddEvent extends Event {
   public static final EventType<WorldEntityAddEvent> TYPE = new EventType<>("WorldEntityAddEvent");
   public class_1297 entity;
   private static final WorldEntityAddEvent INSTANCE = new WorldEntityAddEvent();

   private WorldEntityAddEvent() {
   }

   public static void dispatch(class_1297 entity) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.entity = entity;
         TYPE.dispatch(INSTANCE);
         INSTANCE.entity = null;
      }
   }
}
