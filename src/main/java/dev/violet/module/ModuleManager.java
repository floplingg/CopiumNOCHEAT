package dev.violet.module;

import dev.violet.SoberClient;
import dev.violet.gui.UiSounds;
import dev.violet.modules.ClickGui;
import dev.violet.render.Render3D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2338;
import net.minecraft.class_2604;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_3675;

public final class ModuleManager {
   private final List<Module> modules = new ArrayList<>();
   private final Set<Integer> heldKeys = new HashSet<>();

   public void add(Module module) {
      this.modules.add(module);
   }

   public List<Module> all() {
      return Collections.unmodifiableList(this.modules);
   }

   public List<Module> inCategory(Category category) {
      List<Module> list = new ArrayList<>();

      for (Module module : this.modules) {
         if (module.category == category) {
            list.add(module);
         }
      }

      return list;
   }

   public <T extends Module> T get(Class<T> type) {
      for (Module module : this.modules) {
         if (module.getClass() == type) {
            return (T)module;
         }
      }

      return null;
   }

   public Module byName(String name) {
      for (Module module : this.modules) {
         if (module.name.equals(name)) {
            return module;
         }
      }

      return null;
   }

   public boolean isBound(int key) {
      if (key < 0) {
         return false;
      } else {
         for (Module module : this.modules) {
            if (module.getKey() == key) {
               return true;
            }
         }

         return false;
      }
   }

   public void pollKeybinds(class_310 mc) {
      if (mc.method_22683() != null) {
         Set<Integer> checked = new HashSet<>();
         Set<Integer> fresh = new HashSet<>();

         for (Module module : this.modules) {
            int key = module.getKey();
            if (key >= 0 && checked.add(key)) {
               boolean pressed = class_3675.method_15987(mc.method_22683(), key);
               if (pressed && this.heldKeys.add(key)) {
                  fresh.add(key);
               } else if (!pressed) {
                  this.heldKeys.remove(key);
               }
            }
         }

         if (!fresh.isEmpty() && mc.field_1755 == null) {
            boolean changed = false;

            for (Module modulex : this.modules) {
               if (fresh.contains(modulex.getKey())) {
                  modulex.toggle();
                  if (!(modulex instanceof ClickGui)) {
                     UiSounds.moduleToggle(modulex.isActive());
                  }

                  changed = true;
               }
            }

            if (changed) {
               SoberClient.CONFIG.save();
            }
         }
      }
   }

   public void onTick() {
      for (Module module : this.modules) {
         if (module.isActive()) {
            safely(module, module::onTick);
         }
      }
   }

   public void onRender(Render3D renderer) {
      for (Module module : this.modules) {
         if (module.isActive()) {
            renderer.setAlpha(module.fadeIn());
            safely(module, () -> module.onRender(renderer));
         }
      }

      renderer.setAlpha(1.0F);
   }

   public void onChunkLoad(class_2818 chunk) {
      for (Module module : this.modules) {
         if (module.isActive()) {
            safely(module, () -> module.onChunkLoad(chunk));
         }
      }
   }

   public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
      for (Module module : this.modules) {
         if (module.isActive()) {
            safely(module, () -> module.onBlockUpdate(pos, oldState, newState));
         }
      }
   }

   public void onGameLeft() {
      for (Module module : this.modules) {
         if (module.isActive()) {
            safely(module, module::onGameLeft);
         }
      }
   }

   public boolean onEntitySpawn(class_2604 packet) {
      for (Module module : this.modules) {
         if (module.isActive()) {
            try {
               if (module.onEntitySpawn(packet)) {
                  return true;
               }
            } catch (Exception var5) {
               SoberClient.LOG.error("Module {} threw an exception", module.name, var5);
            }
         }
      }

      return false;
   }

   private static void safely(Module module, Runnable action) {
      try {
         action.run();
      } catch (Exception var3) {
         SoberClient.LOG.error("Module {} threw an exception", module.name, var3);
      }
   }
}
