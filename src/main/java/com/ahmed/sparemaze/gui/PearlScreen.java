package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.PearlConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class PearlScreen extends BaseSettingsScreen {
    public PearlScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.pearl"), parent);
    }

    @Override
    protected void buildEntries() {
        PearlConfig cfg = ConfigManager.getConfig().pearl;

        addBooleanToggle("Pearl Catching", () -> cfg.enabled, val -> cfg.enabled = val);
        addBooleanToggle("Actionbar Status Messages", () -> cfg.statusMessages, val -> cfg.statusMessages = val);
        addBooleanToggle("Automatic Pearl Detection", () -> cfg.autoPearlDetection, val -> cfg.autoPearlDetection = val);
        addBooleanToggle("Wind Charge Detection", () -> cfg.windChargeDetection, val -> cfg.windChargeDetection = val);
        addBooleanToggle("Aim Assistance", () -> cfg.aimAssistance, val -> cfg.aimAssistance = val);
        addIntSlider("Catch Timing Sensitivity", 1, 10, () -> cfg.catchTimingSensitivity, val -> cfg.catchTimingSensitivity = val);
        addIntSlider("Prediction Quality (Steps)", 10, 60, () -> cfg.predictionQuality, val -> cfg.predictionQuality = val);
        addIntSlider("Maximum Tracking Range", 16, 96, () -> (int) cfg.maxTrackingRange, val -> cfg.maxTrackingRange = val);
        addIntSlider("Timeout (Ticks)", 20, 120, () -> cfg.timeoutTicks, val -> cfg.timeoutTicks = val);
        addBooleanToggle("Restore Previous Slot", () -> cfg.restorePreviousSlot, val -> cfg.restorePreviousSlot = val);
        addFullWidthButton(Text.literal("Configure Keybinds..."), btn -> this.client.setScreen(new ControlsScreen(this)));
    }
}
