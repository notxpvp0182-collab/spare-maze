package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.SpareMazeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SpareMazeScreen extends BaseSettingsScreen {
    public SpareMazeScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.title"), parent);
    }

    @Override
    protected void buildEntries() {
        SpareMazeConfig cfg = ConfigManager.getConfig();

        addBooleanToggle("Master Enable", () -> cfg.masterEnable, val -> cfg.masterEnable = val);
        addBooleanToggle("Notifications", () -> cfg.notifications, val -> cfg.notifications = val);

        addPairButtons(
            Text.literal("Pearl Catching..."),
            btn -> this.client.setScreen(new PearlScreen(this)),
            Text.literal("Mace Systems..."),
            btn -> this.client.setScreen(new MaceScreen(this))
        );

        addPairButtons(
            Text.literal("Axe Systems..."),
            btn -> this.client.setScreen(new AxeScreen(this)),
            Text.literal("Spear Systems..."),
            btn -> this.client.setScreen(new SpearScreen(this))
        );

        addPairButtons(
            Text.literal("Controls..."),
            btn -> this.client.setScreen(new ControlsScreen(this)),
            Text.literal("Advanced..."),
            btn -> this.client.setScreen(new AdvancedScreen(this))
        );

        addFullWidthButton(Text.literal("Restore Default Configuration"), btn -> {
            ConfigManager.restoreDefaults();
            this.clearAndInit();
        });
    }
}
