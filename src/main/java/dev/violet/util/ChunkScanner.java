package dev.violet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import net.minecraft.class_1923;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_310;

public class ChunkScanner {
   private final String name;
   private final Consumer<class_2818> task;
   private ExecutorService executor;

   public ChunkScanner(String name, Consumer<class_2818> task) {
      this.name = name;
      this.task = task;
   }

   public void start() {
      this.stop();
      this.executor = Executors.newSingleThreadExecutor(r -> {
         Thread thread = new Thread(r, "SoberClient-" + this.name);
         thread.setDaemon(true);
         return thread;
      });
   }

   public void stop() {
      if (this.executor != null) {
         this.executor.shutdownNow();
      }

      this.executor = null;
   }

   public void submit(class_2818 chunk) {
      ExecutorService ex = this.executor;
      if (ex != null && !ex.isShutdown() && chunk != null) {
         ex.submit(() -> {
            try {
               this.task.accept(chunk);
            } catch (Throwable var3) {
            }
         });
      }
   }

   public void submitLoaded() {
      for (class_2818 chunk : loadedChunks()) {
         this.submit(chunk);
      }
   }

   public static List<class_2818> loadedChunks() {
      class_310 mc = class_310.method_1551();
      List<class_2818> chunks = new ArrayList<>();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_1923 center = mc.field_1724.method_31476();
         int radius = mc.field_1690.method_38521() + 2;

         for (int x = center.field_9181 - radius; x <= center.field_9181 + radius; x++) {
            for (int z = center.field_9180 - radius; z <= center.field_9180 + radius; z++) {
               class_2818 var7 = mc.field_1687.method_2935().method_2857(x, z, class_2806.field_12803, false);
               if (var7 instanceof class_2818) {
                  chunks.add(var7);
               }
            }
         }

         return chunks;
      } else {
         return chunks;
      }
   }
}
