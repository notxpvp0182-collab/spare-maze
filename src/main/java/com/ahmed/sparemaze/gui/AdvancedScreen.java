package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.SpareMazeClient;
import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.SpareMazeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AdvancedScreen extends BaseSettingsScreen {
    public AdvancedScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.advanced"), parent);
    }

    @Override
    protected void buildEntries() {
        SpareMazeConfig cfg = ConfigManager.getConfig();

        addBooleanToggle("Debug Mode", () -> cfg.debugMode, val -> cfg.debugMode = val);
        addBooleanToggle("Prediction Diagnostics", () -> cfg.predictionDiagnostics, val -> cfg.predictionDiagnostics = val);
        addBooleanToggle("State Information", () -> cfg.stateInformation, val -> cfg.stateInformation = val);
        addBooleanToggle("Safe Mode", () -> cfg.safeMode, val -> cfg.safeMode = val);

        addFullWidthButton(Text.literal("Emergency Reset All System States"), btn -> {
            if (SpareMazeClient.getInstance() != null) {
                SpareMazeClient.getInstance().resetAllSystems(this.client);
            }
        });
    }
}
