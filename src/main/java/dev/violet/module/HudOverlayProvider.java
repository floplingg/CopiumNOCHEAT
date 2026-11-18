package dev.violet.module;

import dev.violet.gui.OverlayCanvas;

public interface HudOverlayProvider {
   boolean isHudOverlayEnabled();

   void renderHudOverlay(OverlayCanvas var1, float var2, float var3);
}
