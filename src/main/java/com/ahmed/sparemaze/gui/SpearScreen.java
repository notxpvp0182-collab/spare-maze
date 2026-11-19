package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.SpearConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SpearScreen extends BaseSettingsScreen {
    public SpearScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.spear"), parent);
    }

    @Override
    protected void buildEntries() {
        SpearConfig cfg = ConfigManager.getConfig().spear;

        addBooleanToggle("Spear Lunge", () -> cfg.spearLungeEnabled, val -> cfg.spearLungeEnabled = val);
        addBooleanToggle("Automatic Spear Detection", () -> cfg.automaticSpearDetection, val -> cfg.automaticSpearDetection = val);
        addBooleanToggle("Require Lunge Enchantment", () -> cfg.requireLungeEnchantment, val -> cfg.requireLungeEnchantment = val);
        addBooleanToggle("Restore Previous Slot", () -> cfg.restorePreviousSlot, val -> cfg.restorePreviousSlot = val);
        addFullWidthButton(Text.literal("Configure Keybinds..."), btn -> this.client.setScreen(new ControlsScreen(this)));
    }
}
