package dev.violet.module;

public interface Presets {
   int presetCount();

   String presetName(int var1);

   int presetColor(int var1);

   boolean presetActive(int var1);

   void selectPreset(int var1);

   String presetDescription();
}
