package dev.violet.gui;

import dev.violet.SoberClient;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class BrandWindow {
   private static final int[] SIZES = new int[]{16, 32, 48, 64, 128, 256};

   private BrandWindow() {
   }

   public static String title(String vanilla) {
      return vanilla == null ? "Copium Client" : vanilla.replaceFirst("^Minecraft\\*?", "Copium Client");
   }

   public static void apply(class_310 client) {
      int platform = GLFW.glfwGetPlatform();
      if (platform != 393217 && platform != 393220) {
         client.method_24288();
      } else {
         List<ByteBuffer> buffers = new ArrayList<>();

         try {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               Buffer images = GLFWImage.malloc(SIZES.length, stack);
               int count = 0;

               for (int size : SIZES) {
                  try (InputStream stream = client.method_1478().open(class_2960.method_60655("violet", "textures/brand/icon_" + size + ".png"))) {
                     class_1011 image = class_1011.method_4309(stream);

                     try {
                        ByteBuffer pixels = MemoryUtil.memAlloc(image.method_4307() * image.method_4323() * 4);
                        buffers.add(pixels);
                        pixels.asIntBuffer().put(image.method_48463());
                        images.position(count);
                        images.width(image.method_4307());
                        images.height(image.method_4323());
                        images.pixels(pixels);
                        count++;
                     } catch (Throwable var26) {
                        if (image != null) {
                           try {
                              image.close();
                           } catch (Throwable var25) {
                              var26.addSuppressed(var25);
                           }
                        }

                        throw var26;
                     }

                     if (image != null) {
                        image.close();
                     }
                  }
               }

               if (count > 0) {
                  images.position(0);
                  images.limit(count);
                  GLFW.glfwSetWindowIcon(client.method_22683().method_4490(), images);
               }
            } catch (Throwable var28) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var23) {
                     var28.addSuppressed(var23);
                  }
               }

               throw var28;
            }

            if (stack != null) {
               stack.close();
            }
         } catch (Exception var29) {
            SoberClient.LOG.warn("Could not set the Copium Client window icon", var29);
         } finally {
            buffers.forEach(MemoryUtil::memFree);
         }

         client.method_24288();
      }
   }
}
