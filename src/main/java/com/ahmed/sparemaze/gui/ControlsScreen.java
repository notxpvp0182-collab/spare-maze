package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.controls.KeybindManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.text.Text;

public class ControlsScreen extends BaseSettingsScreen {
    public ControlsScreen(Screen parent) {
        super(Text.translatable("screen.sparemaze.controls"), parent);
    }

    @Override
    protected void buildEntries() {
        addFullWidthButton(
            Text.literal("Open Native Minecraft Keybinds Screen..."),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );

        addFullWidthButton(
            Text.literal("Pearl Catching: " + KeybindManager.pearlCatchingKey.getBoundKeyLocalizedText().getString()),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );
        addFullWidthButton(
            Text.literal("Sword -> Mace: " + KeybindManager.swordToMaceKey.getBoundKeyLocalizedText().getString()),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );
        addFullWidthButton(
            Text.literal("Axe -> Mace: " + KeybindManager.axeToMaceKey.getBoundKeyLocalizedText().getString()),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );
        addFullWidthButton(
            Text.literal("Spear -> Mace: " + KeybindManager.spearToMaceKey.getBoundKeyLocalizedText().getString()),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );
        addFullWidthButton(
            Text.literal("Axe Instant Slam: " + KeybindManager.instantSlamKey.getBoundKeyLocalizedText().getString()),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );
        addFullWidthButton(
            Text.literal("Spear Lunge: " + KeybindManager.spearLungeKey.getBoundKeyLocalizedText().getString()),
            btn -> this.client.setScreen(new KeybindsScreen(this, this.client.options))
        );
    }
}
