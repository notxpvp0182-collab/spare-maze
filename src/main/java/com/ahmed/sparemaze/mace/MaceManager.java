package com.ahmed.sparemaze.mace;

import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import net.minecraft.client.MinecraftClient;

public class MaceManager {
    private final SwordMaceHandler swordMaceHandler;
    private final AxeMaceHandler axeMaceHandler;
    private final SpearMaceHandler spearMaceHandler;

    public MaceManager(SlotManager slotManager, TargetManager targetManager) {
        this.swordMaceHandler = new SwordMaceHandler(slotManager, targetManager);
        this.axeMaceHandler = new AxeMaceHandler(slotManager, targetManager);
        this.spearMaceHandler = new SpearMaceHandler(slotManager, targetManager);
    }

    public void triggerSwordToMace(MinecraftClient client) {
        swordMaceHandler.execute(client);
    }

    public void triggerAxeToMace(MinecraftClient client) {
        axeMaceHandler.execute(client);
    }

    public void triggerSpearToMace(MinecraftClient client) {
        spearMaceHandler.execute(client);
    }

    public void tick(MinecraftClient client) {
        // Synchronous single-tick execution with state safety
    }

    public void cancelAll(MinecraftClient client) {
        swordMaceHandler.cancel(client);
        axeMaceHandler.cancel(client);
        spearMaceHandler.cancel(client);
    }

    public MaceState getActiveState() {
        if (swordMaceHandler.getState() != MaceState.IDLE) return swordMaceHandler.getState();
        if (axeMaceHandler.getState() != MaceState.IDLE) return axeMaceHandler.getState();
        return spearMaceHandler.getState();
    }
}
