package dev.violet.event.impl;

import dev.violet.event.CancellableEvent;
import dev.violet.event.CancellableEventType;
import net.minecraft.class_1297;

public final class PlayerAttackEntityEvent extends CancellableEvent {
   public static final CancellableEventType<PlayerAttackEntityEvent> TYPE = new CancellableEventType<>("PlayerAttackEntityEvent");
   public class_1297 target;
   private static final PlayerAttackEntityEvent INSTANCE = new PlayerAttackEntityEvent();

   private PlayerAttackEntityEvent() {
   }

   public static boolean dispatch(class_1297 target) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.target = target;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         boolean cancelled = INSTANCE.isCancelled();
         INSTANCE.target = null;
         return cancelled;
      }
   }
}
