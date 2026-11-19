package com.ahmed.sparemaze.axe;

import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import net.minecraft.client.MinecraftClient;

public class AxeManager {
    private final InstantSlamManager instantSlamManager;

    public AxeManager(SlotManager slotManager, TargetManager targetManager) {
        this.instantSlamManager = new InstantSlamManager(slotManager, targetManager);
    }

    public void triggerInstantSlam(MinecraftClient client) {
        instantSlamManager.trigger(client);
    }

    public void tick(MinecraftClient client) {
        instantSlamManager.tick(client);
    }

    public void cancel(MinecraftClient client) {
        instantSlamManager.cancel(client);
    }

    public InstantSlamManager getInstantSlamManager() {
        return instantSlamManager;
    }
}
