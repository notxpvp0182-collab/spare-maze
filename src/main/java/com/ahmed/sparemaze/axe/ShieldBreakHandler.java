package com.ahmed.sparemaze.axe;

import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.SlotManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class ShieldBreakHandler {
    public boolean performAxeShieldStrike(MinecraftClient client, SlotManager slotManager, int axeSlot, LivingEntity target) {
        if (!slotManager.switchToSlot(client, axeSlot)) {
            return false;
        }
        InputManager.performLeftClickAttack(client, target);
        return true;
    }

    public boolean performMaceFollowup(MinecraftClient client, SlotManager slotManager, int maceSlot, LivingEntity target) {
        if (!slotManager.switchToSlot(client, maceSlot)) {
            return false;
        }
        InputManager.performLeftClickAttack(client, target);
        return true;
    }
}
