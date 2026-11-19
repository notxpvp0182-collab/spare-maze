package com.ahmed.sparemaze.config;

public class PearlConfig {
    public boolean enabled = true;
    public boolean statusMessages = false; // ডিফল্টে অফ থাকবে
    public boolean autoPearlDetection = true;
    public boolean windChargeDetection = true;
    public boolean aimAssistance = true;
    public int catchTimingSensitivity = 7;
    public int predictionQuality = 40;
    public double maxTrackingRange = 64.0;
    public int timeoutTicks = 60;
    public boolean restorePreviousSlot = true;
    public boolean cancelOnInvalidState = true;
}
