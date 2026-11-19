package com.ahmed.sparemaze.axe;

import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.mace.MaceManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import net.minecraft.client.MinecraftClient;

public class AxeManager {
    private final InstantSlamManager instantSlamManager;

    public AxeManager(SlotManager slotManager, TargetManager targetManager) {
        this.instantSlamManager = new InstantSlamManager(slotManager, targetManager);
    }

    public void triggerInstantSlamOrAxeMace(MinecraftClient client, MaceManager maceManager) {
        instantSlamManager.triggerOrFallbackAxeMace(client, maceManager);
    }

    public void onHoldKey(MinecraftClient client, MaceManager maceManager) {
        if (instantSlamManager.getState() == InstantSlamState.IDLE) {
            instantSlamManager.triggerOrFallbackAxeMace(client, maceManager);
        }
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
