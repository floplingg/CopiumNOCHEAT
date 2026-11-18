package dev.violet.modules;

import dev.violet.gui.Notifications;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.setting.BlockListSetting;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.Setting;
import dev.violet.util.ChunkScanner;
import dev.violet.util.DonutUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;

public class BlockNotifier extends Module {
   private final BlockListSetting blocks = (BlockListSetting)this.add(
         new BlockListSetting(
            "Blocks",
            "Click to pick the blocks that trigger a notification.",
            class_2246.field_10260,
            class_2246.field_22109,
            class_2246.field_10442,
            class_2246.field_29029
         )
      )
      .onChanged(v -> this.targets = v);
   private final Setting<Integer> minCount = this.add(new IntSetting("Min Count", "Blocks of one type needed in a chunk before you get notified.", 1, 1, 64));
   private final Setting<Boolean> placed = this.add(new BoolSetting("Placed Blocks", "Also notify when a picked block gets placed near you.", true));
   private final Setting<Boolean> chat = this.add(new BoolSetting("Chat Message", "Also writes the find into the chat.", false));
   private final Set<class_2338> known = ConcurrentHashMap.newKeySet();
   private final Map<class_2248, Long> lastNotify = new ConcurrentHashMap<>();
   private final ChunkScanner scanner = new ChunkScanner("BlockNotifier", this::scan);
   private volatile Set<class_2248> targets = Set.of();
   private volatile int regionRow = -1;
   private volatile int regionCol = -1;
   private int rescanIn = -1;

   public BlockNotifier() {
      super("Block Notifier", "Notifies you when blocks you picked show up.", Category.BASE_FINDING);
   }

   @Override
   public void onEnable() {
      this.targets = this.blocks.get();
      this.known.clear();
      this.updateRegion();
      this.rescanIn = -1;
      this.scanner.start();
   }

   @Override
   public void onTick() {
      int row = this.regionRow;
      int col = this.regionCol;
      this.updateRegion();
      if (row >= 0 && (row != this.regionRow || col != this.regionCol)) {
         this.rescanIn = 60;
      }

      if (this.rescanIn > 0 && --this.rescanIn == 0) {
         this.rescanIn = -1;
         this.scanner.submitLoaded();
      }
   }

   private void updateRegion() {
      dev.violet.gui.RegionMap.Region region = DonutUtils.onDonut() ? dev.violet.gui.RegionMap.current(mc) : null;
      this.regionRow = region == null ? -1 : region.row();
      this.regionCol = region == null ? -1 : region.col();
   }

   private boolean otherRegion(class_2338 pos) {
      int row = this.regionRow;
      int col = this.regionCol;
      if (row < 0) {
         return false;
      } else {
         dev.violet.gui.RegionMap.Region region = dev.violet.gui.RegionMap.at((double)pos.method_10263() + 0.5, (double)pos.method_10260() + 0.5);
         return region.row() != row || region.col() != col;
      }
   }

   @Override
   public void onDisable() {
      this.scanner.stop();
      this.known.clear();
   }

   @Override
   public void onGameLeft() {
      this.known.clear();
   }

   @Override
   public void onChunkLoad(class_2818 chunk) {
      this.scanner.submit(chunk);
   }

   private void scan(class_2818 chunk) {
      Set<class_2248> wanted = this.targets;
      if (!wanted.isEmpty()) {
         if (this.known.size() > 100000) {
            this.known.clear();
         }

         Map<class_2248, Integer> counts = new HashMap<>();
         Map<class_2248, class_2338> first = new HashMap<>();
         class_2826[] sections = chunk.method_12006();
         int bottom = chunk.method_31607();

         for (int i = 0; i < sections.length; i++) {
            class_2826 section = sections[i];
            if (section != null && !section.method_38292() && section.method_19523(state -> wanted.contains(state.method_26204()))) {
               int baseY = bottom + i * 16;

               for (int x = 0; x < 16; x++) {
                  for (int y = 0; y < 16; y++) {
                     for (int z = 0; z < 16; z++) {
                        class_2248 block = section.method_12254(x, y, z).method_26204();
                        if (wanted.contains(block)) {
                           class_2338 pos = new class_2338(chunk.method_12004().method_8326() + x, baseY + y, chunk.method_12004().method_8328() + z);
                           if (!this.otherRegion(pos) && this.known.add(pos)) {
                              counts.merge(block, 1, Integer::sum);
                              first.putIfAbsent(block, pos);
                           }
                        }
                     }
                  }
               }
            }
         }

         for (Entry<class_2248, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= this.minCount.get()) {
               this.notifyFound(entry.getKey(), entry.getValue(), first.get(entry.getKey()));
            }
         }
      }
   }

   private void notifyFound(class_2248 block, int count, class_2338 pos) {
      long now = System.currentTimeMillis();
      Long last = this.lastNotify.get(block);
      if (last == null || now - last >= 1500L) {
         this.lastNotify.put(block, now);
         mc.execute(
            () -> {
               if (this.isActive() && mc.field_1724 != null && mc.field_1687 != null) {
                  if (!this.otherRegion(pos) && mc.field_1687.method_8320(pos).method_27852(block)) {
                     int distance = (int)Math.round(Math.sqrt(mc.field_1724.method_24515().method_10262(pos)));
                     String name = block.method_9518().getString();
                     String message = (count > 1 ? count + "x " : "")
                        + name
                        + " at "
                        + pos.method_10263()
                        + " "
                        + pos.method_10264()
                        + " "
                        + pos.method_10260()
                        + " ("
                        + distance
                        + "m)";
                     Notifications.push("Block Found", message, Notifications.Type.SUCCESS);
                     if (this.chat.get()) {
                        this.info(message, new Object[0]);
                     }
                  } else {
                     this.known.remove(pos);
                  }
               }
            }
         );
      }
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 oldState, class_2680 newState) {
      if (this.placed.get() && this.targets.contains(newState.method_26204()) && oldState.method_26204() != newState.method_26204()) {
         class_2338 immutable = pos.method_10062();
         if (!this.otherRegion(immutable) && this.known.add(immutable)) {
            this.notifyFound(newState.method_26204(), 1, immutable);
         }
      }
   }

   @Override
   public String getInfoString() {
      return String.valueOf(this.blocks.get().size());
   }
}
