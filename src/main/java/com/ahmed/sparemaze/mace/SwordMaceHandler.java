package com.ahmed.sparemaze.mace;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.MaceConfig;
import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import com.ahmed.sparemaze.util.HotbarUtils;
import com.ahmed.sparemaze.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class SwordMaceHandler {
    private final SlotManager slotManager;
    private final TargetManager targetManager;

    private MaceState state = MaceState.IDLE;
    private int maceSlot = -1;
    private int stageTicks = 0;

    public SwordMaceHandler(SlotManager slotManager, TargetManager targetManager) {
        this.slotManager = slotManager;
        this.targetManager = targetManager;
    }

    public void execute(MinecraftClient client) {
        MaceConfig cfg = ConfigManager.getConfig().mace;
        if (!cfg.swordToMaceEnabled || client.player == null || client.world == null || state != MaceState.IDLE) {
            return;
        }

        int currentSlot = HotbarUtils.getSelectedSlot(client);
        int swordSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isSword);
        this.maceSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isMace);

        if (this.maceSlot == -1) {
            return;
        }

        targetManager.acquireTarget(client, cfg.maxTargetRange);
        int returnSlot = (swordSlot != -1) ? swordSlot : currentSlot;
        slotManager.captureSpecificSlot(returnSlot);

        slotManager.switchToSlot(client, this.maceSlot);
        LivingEntity target = targetManager.getLockedTarget();
        InputManager.performLeftClickAttack(client, target);

        this.state = MaceState.RESTORING;
        this.stageTicks = 0;
    }

    public void tick(MinecraftClient client) {
        if (state == MaceState.RESTORING) {
            stageTicks++;
            if (stageTicks >= 2) {
                slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().mace.restorePreviousSlot);
                targetManager.clearTarget();
                state = MaceState.IDLE;
                stageTicks = 0;
            }
        }
    }

    public void cancel(MinecraftClient client) {
        if (state != MaceState.IDLE) {
            slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().mace.restorePreviousSlot);
            targetManager.clearTarget();
            state = MaceState.IDLE;
            stageTicks = 0;
        }
    }

    public MaceState getState() {
        return state;
    }
}
