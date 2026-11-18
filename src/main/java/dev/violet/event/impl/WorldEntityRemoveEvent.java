package dev.violet.event.impl;

import dev.violet.event.Event;
import dev.violet.event.EventType;
import net.minecraft.class_1297;

public final class WorldEntityRemoveEvent extends Event {
   public static final EventType<WorldEntityRemoveEvent> TYPE = new EventType<>("WorldEntityRemoveEvent");
   public class_1297 entity;
   private static final WorldEntityRemoveEvent INSTANCE = new WorldEntityRemoveEvent();

   private WorldEntityRemoveEvent() {
   }

   public static void dispatch(class_1297 entity) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.entity = entity;
         TYPE.dispatch(INSTANCE);
         INSTANCE.entity = null;
      }
   }
}
