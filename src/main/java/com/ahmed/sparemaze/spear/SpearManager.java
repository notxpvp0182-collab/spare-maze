package com.ahmed.sparemaze.spear;

import com.ahmed.sparemaze.input.SlotManager;
import net.minecraft.client.MinecraftClient;

public class SpearManager {
    private final SpearLungeManager spearLungeManager;

    public SpearManager(SlotManager slotManager) {
        this.spearLungeManager = new SpearLungeManager(slotManager);
    }

    public void triggerLunge(MinecraftClient client) {
        spearLungeManager.trigger(client);
    }

    public void tick(MinecraftClient client) {
        spearLungeManager.tick(client);
    }

    public void cancel(MinecraftClient client) {
        spearLungeManager.cancel(client);
    }

    public SpearLungeManager getSpearLungeManager() {
        return spearLungeManager;
    }
}
