package dev.violet.modules;

import dev.violet.SoberClient;
import dev.violet.module.Category;
import dev.violet.module.Module;
import dev.violet.render.knife.InspectClip;
import dev.violet.render.knife.KnifeMaterial;
import dev.violet.render.knife.KnifeSkin;
import dev.violet.render.knife.SlashTrail;
import dev.violet.setting.BoolSetting;
import dev.violet.setting.EnumSetting;
import dev.violet.setting.IntSetting;
import dev.violet.setting.KeySetting;
import dev.violet.setting.Setting;
import net.minecraft.class_11659;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CsPickaxe extends Module {
   private static final int IDLE = 0;
   private static final int INSPECT = 1;
   private static final int DEPLOY = 2;
   private static final float EASE_OUT_SECONDS = 0.15F;
   private final KeySetting inspectKey = (KeySetting)this.<Integer>add(
      new KeySetting("Inspect Key", "Plays the CS inspect while you hold a pickaxe. Hold it to keep the knife spinning. Right click removes it.", () -> null)
   );
   private final Setting<CsPickaxe.Knife> knife = this.add(
      new EnumSetting<>(
         "Knife Skin", "Butterfly flips its two handles, Karambit spins on its finger ring, Katana is drawn from its scabbard.", CsPickaxe.Knife.Butterfly
      )
   );
   private final Setting<CsPickaxe.Material> material = this.add(
      new EnumSetting<>(
         "Material", "Colours of the knife. Auto follows the pickaxe: diamond, netherite, iron, gold, copper, stone or wood.", CsPickaxe.Material.Auto
      )
   );
   private final Setting<Integer> size = this.add(new IntSetting("Knife Size", "Size of the knife in percent.", 85, 50, 200));
   private final Setting<Integer> speed = this.add(new IntSetting("Speed", "Speed of the inspect and deploy in percent.", 100, 50, 200));
   private final Setting<Boolean> deploy = this.add(new BoolSetting("Deploy Animation", "The knife spins into your hand when you switch to a pickaxe.", true));
   private final Setting<Boolean> slashTrail = this.add(new BoolSetting("Slash Trail", "A glowing trail behind the blade when you hit or swing fast.", true));
   private final Setting<Integer> trailLength = this.add(new IntSetting("Trail Length", "How long the trail stays, in hundredths of a second.", 14, 5, 35))
      .visible(this.slashTrail::get);
   private final Setting<Boolean> sparkles = this.add(new BoolSetting("Blade Sparkle", "A small star flashes at the tip at the end of the inspect.", true));
   private final Setting<Boolean> sneakSkips = this.add(new BoolSetting("Sneak Skips Inspect", "No inspect while you sneak.", true));
   private final Setting<Boolean> thirdPerson = this.add(new BoolSetting("Third Person", "Shows the knife in third person too.", true));
   private final Setting<Integer> offsetX = this.add(
      new IntSetting("Offset X", "Moves the knife left or right in first person. 0 is where Anubis puts it.", 0, -60, 60)
   );
   private final Setting<Integer> offsetY = this.add(
      new IntSetting("Offset Y", "Moves the knife up or down in first person. 0 is where Anubis puts it.", 0, -60, 60)
   );
   private final Setting<Integer> offsetZ = this.add(new IntSetting("Offset Z", "Moves the knife closer or further away in first person.", 0, -60, 60));
   private final Setting<Integer> sizeVersion = this.add(new IntSetting("Size Version", "", 0, 0, 100)).visible(() -> false);
   private int mode = 0;
   private InspectClip clip;
   private KnifeSkin clipKnife;
   private long startedAt;
   private float seconds;
   private boolean easingOut;
   private long easeStartedAt;
   private float easeFrom;
   private class_1792 item;
   private int heldSlot = -1;
   private class_1792 heldItem;
   private boolean keyDown;
   private final float[] state = new float[16];
   private final float[] trailSample = new float[12];
   private final SlashTrail[] trails = new SlashTrail[]{new SlashTrail(), new SlashTrail()};
   private final Matrix4f base = new Matrix4f();
   private final Matrix4f baseInverse = new Matrix4f();
   private final Matrix3f baseNormal = new Matrix3f();
   private boolean baseKnown;

   public CsPickaxe() {
      super(
         "CsPickaxe",
         "Your pickaxes become a CS knife with the CS inspect, deploy and hit animations. Press the inspect key (F) while holding a pickaxe.",
         Category.VISUALS
      );
      this.inspectKey.bind(70);
   }

   public boolean migrate() {
      if (this.sizeVersion.get() >= 1) {
         return false;
      } else {
         if (this.size.get() == 100) {
            this.size.set(85);
         }

         this.sizeVersion.set(1);
         return true;
      }
   }

   private static CsPickaxe active() {
      CsPickaxe module = SoberClient.MODULES.get(CsPickaxe.class);
      return module != null && module.isActive() ? module : null;
   }

   private KnifeSkin skin() {
      return KnifeSkin.of(this.knife.get().name());
   }

   @Override
   public void onEnable() {
      this.mode = 0;
      this.heldItem = null;
      this.clearTrails();
   }

   @Override
   public void onDisable() {
      this.mode = 0;
      this.clearTrails();
      this.baseKnown = false;
   }

   private void clearTrails() {
      for (SlashTrail trail : this.trails) {
         trail.clear();
      }
   }

   private static boolean matches(class_1799 stack) {
      return !stack.method_7960() && stack.method_31573(class_3489.field_42614);
   }

   private KnifeMaterial knifeMaterial(class_1799 stack) {
      return this.material.get() == CsPickaxe.Material.Auto ? KnifeMaterial.of(stack) : KnifeMaterial.named(this.material.get().name());
   }

   public static boolean instantSwap(class_1799 held) {
      CsPickaxe module = active();
      if (module != null && module.deploy.get() && matches(held)) {
         class_746 player = class_310.method_1551().field_1724;
         if (player == null) {
            return false;
         } else {
            boolean switched = player.method_31548().method_67532() != module.heldSlot || !held.method_31574(module.heldItem);
            return switched || module.mode == 2 && !module.easingOut;
         }
      } else {
         return false;
      }
   }

   public static boolean blocks(int key) {
      CsPickaxe module = active();
      if (module != null && key >= 0 && module.inspectKey.get() == key) {
         class_310 client = class_310.method_1551();
         class_746 player = client.field_1724;
         return player != null && client.field_1755 == null && matches(player.method_6047()) && (!module.sneakSkips.get() || !player.method_5715());
      } else {
         return false;
      }
   }

   @Override
   public void onTick() {
      class_746 player = mc.field_1724;
      if (player == null) {
         this.mode = 0;
         this.heldItem = null;
      } else {
         boolean down = this.inspectKey.isPressed();
         if (down && !this.keyDown) {
            this.pressed(player);
         }

         this.keyDown = down;
         class_1799 held = player.method_6047();
         int selected = player.method_31548().method_67532();
         if (selected != this.heldSlot || !held.method_31574(this.heldItem)) {
            this.heldSlot = selected;
            this.heldItem = held.method_7909();
            if (this.deploy.get() && matches(held)) {
               this.start(2, held);
            } else if (this.mode != 0 && !this.easingOut) {
               this.easeOut();
            }
         } else if (this.mode != 0 && !this.easingOut) {
            if (player.field_6252) {
               this.mode = 0;
            } else if (player.method_6115()) {
               this.easeOut();
            } else if (this.loopedProgress(System.nanoTime()) >= 1.0F) {
               this.mode = 0;
            }
         }
      }
   }

   public void inspectNow() {
      if (mc.field_1724 != null) {
         this.pressed(mc.field_1724);
      }
   }

   private void pressed(class_746 player) {
      if (mc.field_1755 == null && !player.method_7325()) {
         if (!this.sneakSkips.get() || !player.method_5715()) {
            class_1799 held = player.method_6047();
            if (matches(held) && !player.method_6115()) {
               this.start(1, held);
            }
         }
      }
   }

   private void start(int next, class_1799 held) {
      KnifeSkin skin = this.skin();
      this.clip = next == 2 ? skin.deploy : skin.inspect;
      this.clipKnife = skin;
      this.mode = next;
      this.seconds = this.clip.seconds;
      this.easingOut = false;
      this.startedAt = System.nanoTime();
      this.item = held.method_7909();
   }

   private void easeOut() {
      long now = System.nanoTime();
      this.easeFrom = Math.min(this.progress(now), 1.0F);
      this.easeStartedAt = now;
      this.easingOut = true;
   }

   private float progress(long now) {
      return (float)(now - this.startedAt) / 1.0E9F / (this.seconds / ((float)this.speed.get().intValue() / 100.0F));
   }

   private float loopedProgress(long now) {
      float progress = this.progress(now);
      if (this.mode == 1 && this.clip.loops() && !(progress < this.clip.loopExit) && this.inspectKey.isPressed()) {
         float duration = this.seconds / ((float)this.speed.get().intValue() / 100.0F);
         long cycle = (long)((this.clip.loopExit - this.clip.loopEnter) * duration * 1.0E9F);
         if (cycle <= 0L) {
            return progress;
         } else {
            long over = (long)((progress - this.clip.loopExit) * duration * 1.0E9F);
            this.startedAt += (over / cycle + 1L) * cycle;
            return this.progress(now);
         }
      } else {
         return progress;
      }
   }

   private float animate(class_1799 stack, KnifeSkin skin, long now) {
      if (this.mode == 0 || !stack.method_31574(this.item)) {
         return 0.0F;
      } else if (skin != this.clipKnife) {
         this.mode = 0;
         return 0.0F;
      } else {
         float weight = 1.0F;
         float progress;
         if (this.easingOut) {
            float t = (float)(now - this.easeStartedAt) / 1.0E9F / 0.15F;
            if (t >= 1.0F) {
               this.mode = 0;
               return 0.0F;
            }

            progress = this.easeFrom;
            weight = 1.0F - t * t * (3.0F - 2.0F * t);
         } else {
            progress = this.loopedProgress(now);
            if (progress >= 1.0F) {
               this.mode = 0;
               return 0.0F;
            }
         }

         this.clip.sample(progress, this.state);
         return weight;
      }
   }

   public static void handsBegin(class_4587 pose) {
      CsPickaxe module = active();
      if (module != null) {
         module.base.set(pose.method_23760().method_23761());
         module.base.invert(module.baseInverse);
         module.baseNormal.set(pose.method_23760().method_23762());
         module.baseKnown = true;
      }
   }

   public static boolean renderFirstPerson(class_1309 holder, class_1799 stack, class_811 context, class_4587 pose, class_11659 queue, int light) {
      CsPickaxe module = active();
      if (module == null || !matches(stack)) {
         return false;
      } else {
         return context != class_811.field_4322 && context != class_811.field_4321 ? false : module.drawFirstPerson(holder, stack, context, pose, queue, light);
      }
   }

   private boolean drawFirstPerson(class_1309 holder, class_1799 stack, class_811 context, class_4587 pose, class_11659 queue, int light) {
      int side = context == class_811.field_4321 ? -1 : 1;
      class_1306 mainArm = holder instanceof class_1657 player ? player.method_6068() : class_1306.field_6183;
      boolean mainHand = mainArm == class_1306.field_6183 == side > 0;
      KnifeSkin skin = this.skin();
      long now = System.nanoTime();
      float weight = mainHand ? this.animate(stack, skin, now) : 0.0F;
      float[] at = weight > 0.0F ? this.state : KnifeSkin.REST;
      boolean trailing = mainHand && this.slashTrail.get() && this.baseKnown;
      if (mainHand && !trailing) {
         this.clearTrails();
      }

      KnifeMaterial knifeMaterial = this.knifeMaterial(stack);
      pose.method_22903();
      pose.method_46416(
         (float)this.offsetX.get().intValue() / 100.0F * (float)side,
         (float)this.offsetY.get().intValue() / 100.0F,
         (float)this.offsetZ.get().intValue() / 100.0F
      );
      if (side < 0) {
         pose.method_22905(-1.0F, 1.0F, 1.0F);
      }

      skin.draw(
         pose,
         at,
         weight,
         (float)this.size.get().intValue() / 100.0F,
         KnifeSkin.sink(queue, light, knifeMaterial, stack.method_7958()),
         trailing ? this.trailSample : null
      );
      pose.method_22909();
      if (trailing) {
         int streaks = skin.streakCount();
         Vector3f p = new Vector3f();

         for (int s = 0; s < this.trails.length; s++) {
            if (s >= streaks) {
               this.trails[s].clear();
            } else {
               for (int i = s * 6; i < s * 6 + 6; i += 3) {
                  this.baseInverse.transformPosition(this.trailSample[i], this.trailSample[i + 1], this.trailSample[i + 2], p);
                  this.trailSample[i] = p.x;
                  this.trailSample[i + 1] = p.y;
                  this.trailSample[i + 2] = p.z;
               }

               this.trails[s].add(now, this.trailSample, s * 6);
            }
         }

         float flash = this.sparkles.get() && weight > 0.0F ? skin.sparkle(at, weight) : 0.0F;
         long lifetime = (long)this.trailLength.get().intValue() * 10000000L;
         boolean showing = flash > 0.0F;

         for (int sx = 0; sx < streaks && !showing; sx++) {
            showing = !this.trails[sx].isEmpty(now, lifetime);
         }

         if (showing) {
            skin.glow(queue, this.base, this.baseNormal, this.trails, now, lifetime, knifeMaterial.glow(), flash, this.trailSample);
         }
      }

      return true;
   }

   public static boolean renderThirdPerson(class_1799 stack, boolean left, class_4587 pose, class_11659 queue, int light) {
      CsPickaxe module = active();
      if (module != null && module.thirdPerson.get() && matches(stack)) {
         float side = left ? -1.0F : 1.0F;
         pose.method_22903();
         pose.method_46416(0.0F, 0.25F, 0.03125F);
         pose.method_22907(new Quaternionf().rotationXYZ(0.0F, (float)Math.toRadians((double)(-90.0F * side)), (float)Math.toRadians((double)(55.0F * side))));
         pose.method_22905(0.85F, 0.85F, 0.85F);
         pose.method_46416(-0.5F, -0.5F, -0.5F);
         pose.method_46416(0.36F, 0.36F, 0.5F);
         pose.method_22907(class_7833.field_40718.rotationDegrees(45.0F));
         module.skin()
            .drawHeld(pose, (float)module.size.get().intValue() / 100.0F, KnifeSkin.sink(queue, light, module.knifeMaterial(stack), stack.method_7958()));
         pose.method_22909();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public String getInfoString() {
      return this.knife.get().name();
   }

   public static enum Knife {
      Butterfly,
      Karambit,
      Katana;
   }

   public static enum Material {
      Auto,
      Diamond,
      Netherite,
      Iron,
      Golden,
      Copper,
      Stone,
      Wooden;
   }
}
