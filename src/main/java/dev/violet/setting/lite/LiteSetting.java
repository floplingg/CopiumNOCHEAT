package dev.violet.setting.lite;

import dev.violet.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class LiteSetting {
   private final String name;
   private final List<Runnable> listeners = new ArrayList<>();
   private BooleanSupplier visible = () -> true;
   private Setting<?> built;
   private long revision;
   private boolean sessionOnly;

   protected LiteSetting(String name) {
      this.name = name;
   }

   public final String name() {
      return this.name;
   }

   public LiteSetting onChange(Runnable listener) {
      this.listeners.add(listener);
      return this;
   }

   public LiteSetting visibleWhen(BooleanSupplier condition) {
      this.visible = condition;
      if (this.built != null) {
         this.built.visible(condition);
      }

      return this;
   }

   public final boolean isVisible() {
      return this.visible.getAsBoolean();
   }

   public final String id() {
      return this.name;
   }

   public final long revision() {
      return this.revision;
   }

   public final LiteSetting privateValue() {
      return this;
   }

   public final LiteSetting sessionOnly() {
      this.sessionOnly = true;
      return this;
   }

   public final boolean isSessionOnly() {
      return this.sessionOnly;
   }

   public void resetToDefault() {
      if (this.built != null) {
         this.built.reset();
      }
   }

   protected final void notifyChanged() {
      this.revision++;

      for (Runnable listener : this.listeners) {
         listener.run();
      }
   }

   protected abstract Setting<?> create();

   public final Setting<?> build() {
      if (this.built == null) {
         this.built = this.create();
         this.built.visible(this.visible);
      }

      return this.built;
   }
}
