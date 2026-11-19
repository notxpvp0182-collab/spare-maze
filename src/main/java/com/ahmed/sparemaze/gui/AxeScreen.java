package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.AxeConfig;
import com.ahmed.sparemaze.config.ConfigManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AxeScreen extends BaseSettingsScreen {
    public AxeScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.axe"), parent);
    }

    @Override
    protected void buildEntries() {
        AxeConfig cfg = ConfigManager.getConfig().axe;

        addBooleanToggle("Instant Slam", () -> cfg.instantSlamEnabled, val -> cfg.instantSlamEnabled = val);
        addBooleanToggle("Shield Detection", () -> cfg.shieldDetection, val -> cfg.shieldDetection = val);
        addBooleanToggle("Target Validation", () -> cfg.targetValidation, val -> cfg.targetValidation = val);
        addBooleanToggle("Fallback Mace Hit if No Shield", () -> cfg.fallbackAttackIfNoShield, val -> cfg.fallbackAttackIfNoShield = val);
        addBooleanToggle("Restore Previous Slot", () -> cfg.restorePreviousSlot, val -> cfg.restorePreviousSlot = val);
        addIntSlider("Max Shield-Break Verification Ticks", 2, 20, () -> cfg.maxWaitShieldBreakTicks, val -> cfg.maxWaitShieldBreakTicks = val);
        addFullWidthButton(Text.literal("Configure Keybinds..."), btn -> this.client.setScreen(new ControlsScreen(this)));
    }
}
