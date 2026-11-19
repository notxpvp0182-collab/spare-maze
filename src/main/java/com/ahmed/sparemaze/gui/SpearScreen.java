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
        addIntSlider("Lunge With Slot (0=Current, 1-9=Slot)", 0, 9, () -> cfg.preferredBaseSlot, val -> cfg.preferredBaseSlot = val);
        addIntSlider("Spear Hotbar Slot (0=Auto, 1-9=Slot)", 0, 9, () -> cfg.preferredSpearSlot, val -> cfg.preferredSpearSlot = val);
        addBooleanToggle("Require Lunge Enchantment", () -> cfg.requireLungeEnchantment, val -> cfg.requireLungeEnchantment = val);
        addBooleanToggle("Restore Selected Slot", () -> cfg.restorePreviousSlot, val -> cfg.restorePreviousSlot = val);
        addFullWidthButton(Text.literal("Configure Keybinds..."), btn -> this.client.setScreen(new ControlsScreen(this)));
    }
}
