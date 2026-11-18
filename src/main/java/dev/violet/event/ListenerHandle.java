package dev.violet.event;

import java.util.function.Consumer;

public record ListenerHandle<E extends Event>(EventType<E> type, Stage stage, EventPriority priority, Object owner, Consumer<E> callback) {
   public void unregister() {
      this.type.unregister(this);
   }
}
