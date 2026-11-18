package dev.violet.render;

public final class ChunkHighlight {
   private ChunkHighlight() {
   }

   public static void draw(Render3D renderer, int chunkX, int chunkZ, double y, Color color, boolean beam) {
      double x0 = (double)chunkX * 16.0;
      double z0 = (double)chunkZ * 16.0;
      double x1 = x0 + 16.0;
      double z1 = z0 + 16.0;
      if (renderer.inView(x0, y - 0.5, z0, x1, y + 0.5, z1)) {
         renderer.quad(x0, y, z0, x1, y, z0, x1, y, z1, x0, y, z1, color.scaleAlpha(0.2));
         Color edge = color.withAlpha(Math.min(255, color.a() + 40));
         renderer.setLineWidth(2.5F);
         renderer.line(x0, y, z0, x1, y, z0, edge);
         renderer.line(x1, y, z0, x1, y, z1, edge);
         renderer.line(x1, y, z1, x0, y, z1, edge);
         renderer.line(x0, y, z1, x0, y, z0, edge);
         renderer.setLineWidth(2.0F);
      }

      if (beam) {
         renderer.beam(x0 + 8.0, z0 + 8.0, y, y + 96.0, 0.45, color.scaleAlpha(0.55), 1.0);
      }
   }

   public static void tracer(Render3D renderer, int chunkX, int chunkZ, double y, Color color) {
      renderer.tracer((double)chunkX * 16.0 + 8.0, y, (double)chunkZ * 16.0 + 8.0, color);
   }
}
