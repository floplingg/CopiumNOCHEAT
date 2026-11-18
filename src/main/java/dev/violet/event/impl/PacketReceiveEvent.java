package dev.violet.event.impl;

import dev.violet.event.Event;
import dev.violet.event.EventBus;
import dev.violet.event.PooledEventType;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2596;

public final class PacketReceiveEvent extends Event implements Runnable {
   public static final PooledEventType<PacketReceiveEvent> TYPE = new PooledEventType<>("PacketReceiveEvent", PacketReceiveEvent::new, 1024);
   public class_2596<?> packet;
   private BooleanSupplier connectionActive;

   private PacketReceiveEvent() {
   }

   public static void dispatchAsync(class_2596<?> packet) {
      dispatchAsync(packet, null);
   }

   public static void dispatchAsync(class_2596<?> packet, BooleanSupplier connectionActive) {
      if (TYPE.hasListeners()) {
         PacketReceiveEvent event = TYPE.pool.acquire();
         event.packet = packet;
         event.connectionActive = connectionActive;
         if (!EventBus.INSTANCE.postAsync(event)) {
            event.release();
         }
      }
   }

   @Override
   public void run() {
      try {
         if (this.connectionActive == null || this.connectionActive.getAsBoolean()) {
            TYPE.dispatch(this);
            TYPE.dispatchPost(this);
            return;
         }
      } finally {
         this.release();
      }
   }

   private void release() {
      this.packet = null;
      this.connectionActive = null;
      TYPE.pool.release(this);
   }
}
