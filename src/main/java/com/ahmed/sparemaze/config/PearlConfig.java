package com.ahmed.sparemaze.config;

public class PearlConfig {
    public boolean enabled = true;
    public boolean autoPearlDetection = true;
    public boolean windChargeDetection = true;
    public boolean aimAssistance = true;
    public int catchTimingSensitivity = 5; // 1 to 10 scale
    public int predictionQuality = 30;     // simulation steps (10 to 60)
    public double maxTrackingRange = 48.0; // blocks
    public int timeoutTicks = 80;          // 4 seconds max sequence time
    public boolean restorePreviousSlot = true;
    public boolean cancelOnInvalidState = true;
}
