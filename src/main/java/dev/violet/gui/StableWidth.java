package dev.violet.gui;

final class StableWidth {
   private float current = -1.0F;
   private float narrowerFor;

   float update(float target, float dt) {
      if (!(this.current < 0.0F) && !(target >= this.current)) {
         this.narrowerFor += dt;
         if (this.narrowerFor < 1.5F) {
            return this.current;
         } else {
            this.current = this.current + (target - this.current) * (1.0F - (float)Math.exp((double)(-12.0F * dt)));
            if (Math.abs(this.current - target) < 0.25F) {
               this.current = target;
            }

            return this.current;
         }
      } else {
         this.current = target;
         this.narrowerFor = 0.0F;
         return this.current;
      }
   }
}
