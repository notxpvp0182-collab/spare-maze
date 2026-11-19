package com.ahmed.sparemaze.mace;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.MaceConfig;
import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import com.ahmed.sparemaze.util.DebugLogger;
import com.ahmed.sparemaze.util.HotbarUtils;
import com.ahmed.sparemaze.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class AxeMaceHandler {
    private final SlotManager slotManager;
    private final TargetManager targetManager;

    private MaceState state = MaceState.IDLE;

    public AxeMaceHandler(SlotManager slotManager, TargetManager targetManager) {
        this.slotManager = slotManager;
        this.targetManager = targetManager;
    }

    public void execute(MinecraftClient client) {
        MaceConfig cfg = ConfigManager.getConfig().mace;
        if (!cfg.axeToMaceEnabled || client.player == null || client.world == null) {
            return;
        }

        state = MaceState.SEARCHING_WEAPON;
        int currentSlot = client.player.getInventory().selectedSlot;
        int axeSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isAxe);
        int maceSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isMace);

        if (maceSlot == -1) {
            state = MaceState.FAILED;
            DebugLogger.notifyPlayer(client, "Axe -> Mace cancelled: No Mace in hotbar.");
            state = MaceState.IDLE;
            return;
        }

        LivingEntity target = targetManager.acquireTarget(client, cfg.maxTargetRange);

        int baseSlot = (axeSlot != -1) ? axeSlot : currentSlot;
        slotManager.captureSpecificSlot(baseSlot);

        state = MaceState.SWITCHING;
        if (axeSlot != -1 && currentSlot != axeSlot) {
            slotManager.switchToSlot(client, axeSlot);
        }

        slotManager.switchToSlot(client, maceSlot);
        state = MaceState.READY;

        state = MaceState.ATTACKING;
        InputManager.performLeftClickAttack(client, target);

        state = MaceState.RESTORING;
        slotManager.restoreOriginalSlot(client, cfg.restorePreviousSlot);
        targetManager.clearTarget();

        state = MaceState.COMPLETED;
        state = MaceState.IDLE;
    }

    public void cancel(MinecraftClient client) {
        if (state != MaceState.IDLE) {
            slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().mace.restorePreviousSlot);
            targetManager.clearTarget();
            state = MaceState.IDLE;
        }
    }

    public MaceState getState() {
        return state;
    }
}
