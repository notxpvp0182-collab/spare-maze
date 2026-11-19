package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.MaceConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MaceScreen extends BaseSettingsScreen {
    public MaceScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.mace"), parent);
    }

    @Override
    protected void buildEntries() {
        MaceConfig cfg = ConfigManager.getConfig().mace;

        addBooleanToggle("Sword -> Mace", () -> cfg.swordToMaceEnabled, val -> cfg.swordToMaceEnabled = val);
        addBooleanToggle("Axe -> Mace", () -> cfg.axeToMaceEnabled, val -> cfg.axeToMaceEnabled = val);
        addBooleanToggle("Spear -> Mace", () -> cfg.spearToMaceEnabled, val -> cfg.spearToMaceEnabled = val);
        addBooleanToggle("Restore Previous Slot", () -> cfg.restorePreviousSlot, val -> cfg.restorePreviousSlot = val);
        addFullWidthButton(Text.literal("Configure Keybinds..."), btn -> this.client.setScreen(new ControlsScreen(this)));
    }
}
