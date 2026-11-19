package com.ahmed.sparemaze.config;

public class SpearConfig {
    public boolean spearLungeEnabled = true;
    public int preferredBaseSlot = 1; // 0 = Current Slot, 1-9 = নির্দিষ্ট হটবার স্লট (Slot 1-9)
    public int preferredSpearSlot = 0; // 0 = Auto Detect Spear, 1-9 = নির্দিষ্ট স্পেয়ার স্লট
    public boolean automaticSpearDetection = true;
    public boolean requireLungeEnchantment = false;
    public boolean restorePreviousSlot = true;
}
