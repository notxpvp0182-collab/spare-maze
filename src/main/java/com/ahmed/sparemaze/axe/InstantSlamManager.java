package com.ahmed.sparemaze.axe;

import com.ahmed.sparemaze.config.AxeConfig;
import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.mace.MaceManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import com.ahmed.sparemaze.util.HotbarUtils;
import com.ahmed.sparemaze.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class InstantSlamManager {
    private final SlotManager slotManager;
    private final TargetManager targetManager;
    private final ShieldBreakHandler breakHandler = new ShieldBreakHandler();

    private InstantSlamState state = InstantSlamState.IDLE;
    private int stageTicks = 0;
    private int cachedMaceSlot = -1;

    public InstantSlamManager(SlotManager slotManager, TargetManager targetManager) {
        this.slotManager = slotManager;
        this.targetManager = targetManager;
    }

    public void triggerOrFallbackAxeMace(MinecraftClient client, MaceManager maceManager) {
        AxeConfig cfg = ConfigManager.getConfig().axe;
        if (!cfg.instantSlamEnabled || client.player == null || client.world == null) {
            maceManager.triggerAxeToMace(client);
            return;
        }

        if (state != InstantSlamState.IDLE) {
            return;
        }

        double range = ConfigManager.getConfig().mace.maxTargetRange;
        LivingEntity target = targetManager.acquireTarget(client, range);

        int axeSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isAxe);
        this.cachedMaceSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isMace);

        if (cachedMaceSlot == -1) {
            return;
        }

        boolean blocking = target != null && ShieldDetector.isActivelyBlockingWithShield(target);

        if (!blocking || axeSlot == -1) {
            maceManager.triggerAxeToMace(client);
            return;
        }

        int currentSlot = HotbarUtils.getSelectedSlot(client);
        int returnSlot = (axeSlot != -1) ? axeSlot : currentSlot;
        slotManager.captureSpecificSlot(returnSlot);

        state = InstantSlamState.BREAKING_SHIELD;
        breakHandler.performAxeShieldStrike(client, slotManager, axeSlot, target);
        this.stageTicks = 0;
        state = InstantSlamState.WAITING_SHIELD_BREAK;
    }

    public void tick(MinecraftClient client) {
        if (state == InstantSlamState.IDLE) {
            return;
        }

        AxeConfig cfg = ConfigManager.getConfig().axe;
        double range = ConfigManager.getConfig().mace.maxTargetRange;
        stageTicks++;

        if (state == InstantSlamState.WAITING_SHIELD_BREAK) {
            if (!targetManager.validateLockedTarget(client, range)) {
                cancel(client);
                return;
            }
            LivingEntity target = targetManager.getLockedTarget();
            if (stageTicks >= 1 && (ShieldDetector.hasShieldBrokenOrDropped(target) || stageTicks >= 2)) {
                state = InstantSlamState.MACE_HIT;
                breakHandler.performMaceFollowup(client, slotManager, cachedMaceSlot, target);
                state = InstantSlamState.RESTORING;
                stageTicks = 0;
            }
        } else if (state == InstantSlamState.RESTORING) {
            if (stageTicks >= 2) {
                slotManager.restoreOriginalSlot(client, cfg.restorePreviousSlot);
                targetManager.clearTarget();
                state = InstantSlamState.IDLE;
                stageTicks = 0;
                cachedMaceSlot = -1;
            }
        }
    }

    public void cancel(MinecraftClient client) {
        if (state != InstantSlamState.IDLE) {
            slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().axe.restorePreviousSlot);
            targetManager.clearTarget();
            state = InstantSlamState.IDLE;
            stageTicks = 0;
            cachedMaceSlot = -1;
        }
    }

    public InstantSlamState getState() {
        return state;
    }
}
