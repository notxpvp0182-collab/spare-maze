package com.ahmed.sparemaze.axe;

import com.ahmed.sparemaze.config.AxeConfig;
import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import com.ahmed.sparemaze.util.DebugLogger;
import com.ahmed.sparemaze.util.HotbarUtils;
import com.ahmed.sparemaze.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class InstantSlamManager {
    private final SlotManager slotManager;
    private final TargetManager targetManager;
    private final ShieldBreakHandler breakHandler = new ShieldBreakHandler();

    private InstantSlamState state = InstantSlamState.IDLE;
    private int waitTicks = 0;
    private int cachedMaceSlot = -1;

    public InstantSlamManager(SlotManager slotManager, TargetManager targetManager) {
        this.slotManager = slotManager;
        this.targetManager = targetManager;
    }

    public void trigger(MinecraftClient client) {
        AxeConfig cfg = ConfigManager.getConfig().axe;
        if (!cfg.instantSlamEnabled || client.player == null || client.world == null) {
            return;
        }

        if (state != InstantSlamState.IDLE) {
            cancel(client);
            return;
        }

        state = InstantSlamState.VALIDATING_TARGET;
        double range = ConfigManager.getConfig().mace.maxTargetRange;
        LivingEntity target = targetManager.acquireTarget(client, range);

        if (cfg.targetValidation && target == null) {
            fail(client, "No valid target in range");
            return;
        }

        int axeSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isAxe);
        this.cachedMaceSlot = HotbarUtils.findItemInHotbar(client, ItemUtils::isMace);

        if (cachedMaceSlot == -1) {
            fail(client, "No Mace in hotbar");
            return;
        }

        slotManager.captureOriginalSlot(client);

        boolean blocking = target != null && ShieldDetector.isActivelyBlockingWithShield(target);
        if (cfg.shieldDetection && !blocking) {
            if (cfg.fallbackAttackIfNoShield) {
                executeMaceHitAndRestore(client, cfg, target);
            } else {
                fail(client, "Target is not blocking with a shield");
            }
            return;
        }

        if (axeSlot == -1) {
            fail(client, "No Axe in hotbar to break shield");
            return;
        }

        state = InstantSlamState.SWITCHING_TO_AXE;
        state = InstantSlamState.BREAKING_SHIELD;
        if (!breakHandler.performAxeShieldStrike(client, slotManager, axeSlot, target)) {
            fail(client, "Could not switch to Axe");
            return;
        }

        this.waitTicks = 0;
        if (ShieldDetector.hasShieldBrokenOrDropped(target)) {
            executeMaceHitAndRestore(client, cfg, target);
        } else {
            state = InstantSlamState.WAITING_SHIELD_BREAK;
        }
    }

    public void tick(MinecraftClient client) {
        if (state == InstantSlamState.IDLE) {
            return;
        }

        AxeConfig cfg = ConfigManager.getConfig().axe;
        double range = ConfigManager.getConfig().mace.maxTargetRange;

        if (!targetManager.validateLockedTarget(client, range)) {
            fail(client, "Locked target became invalid");
            return;
        }

        LivingEntity target = targetManager.getLockedTarget();

        if (state == InstantSlamState.WAITING_SHIELD_BREAK) {
            waitTicks++;
            if (ShieldDetector.hasShieldBrokenOrDropped(target)) {
                executeMaceHitAndRestore(client, cfg, target);
            } else if (waitTicks > cfg.maxWaitShieldBreakTicks) {
                fail(client, "Shield break state not confirmed within window");
            }
        }
    }

    private void executeMaceHitAndRestore(MinecraftClient client, AxeConfig cfg, LivingEntity target) {
        state = InstantSlamState.SWITCHING_TO_MACE;
        state = InstantSlamState.MACE_HIT;
        breakHandler.performMaceFollowup(client, slotManager, cachedMaceSlot, target);

        state = InstantSlamState.RESTORING;
        slotManager.restoreOriginalSlot(client, cfg.restorePreviousSlot);
        targetManager.clearTarget();

        state = InstantSlamState.COMPLETED;
        state = InstantSlamState.IDLE;
        waitTicks = 0;
        cachedMaceSlot = -1;
    }

    public void cancel(MinecraftClient client) {
        if (state != InstantSlamState.IDLE) {
            slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().axe.restorePreviousSlot);
            targetManager.clearTarget();
            state = InstantSlamState.IDLE;
            waitTicks = 0;
            cachedMaceSlot = -1;
        }
    }

    private void fail(MinecraftClient client, String reason) {
        state = InstantSlamState.FAILED;
        DebugLogger.notifyPlayer(client, "Instant Slam: " + reason);
        slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().axe.restorePreviousSlot);
        targetManager.clearTarget();
        state = InstantSlamState.IDLE;
        waitTicks = 0;
        cachedMaceSlot = -1;
    }

    public InstantSlamState getState() {
        return state;
    }
}
