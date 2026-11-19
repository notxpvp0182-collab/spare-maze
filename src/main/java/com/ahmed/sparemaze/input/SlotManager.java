package com.ahmed.sparemaze.input;

import com.ahmed.sparemaze.util.HotbarUtils;
import net.minecraft.client.MinecraftClient;

public class SlotManager {
    private int originalSelectedSlot = -1;
    private boolean holdingOverride = false;

    public void captureOriginalSlot(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        if (!holdingOverride) {
            this.originalSelectedSlot = HotbarUtils.getSelectedSlot(client);
            this.holdingOverride = true;
        }
    }

    public void captureSpecificSlot(int slot) {
        if (HotbarUtils.isValidHotbarSlot(slot) && !holdingOverride) {
            this.originalSelectedSlot = slot;
            this.holdingOverride = true;
        }
    }

    public boolean switchToSlot(MinecraftClient client, int slot) {
        if (client.player == null || !HotbarUtils.isValidHotbarSlot(slot)) {
            return false;
        }
        HotbarUtils.selectHotbarSlot(client, slot);
        return true;
    }

    public void restoreOriginalSlot(MinecraftClient client, boolean shouldRestore) {
        if (client.player != null && holdingOverride && shouldRestore && HotbarUtils.isValidHotbarSlot(originalSelectedSlot)) {
            HotbarUtils.selectHotbarSlot(client, originalSelectedSlot);
        }
        this.holdingOverride = false;
        this.originalSelectedSlot = -1;
    }

    public void forceRelease(MinecraftClient client) {
        if (client.player != null && holdingOverride && HotbarUtils.isValidHotbarSlot(originalSelectedSlot)) {
            HotbarUtils.selectHotbarSlot(client, originalSelectedSlot);
        }
        this.holdingOverride = false;
        this.originalSelectedSlot = -1;
    }

    public int getOriginalSelectedSlot() {
        return originalSelectedSlot;
    }

    public boolean isHoldingOverride() {
        return holdingOverride;
    }
}
