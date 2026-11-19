package com.ahmed.sparemaze.pearl;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.PearlConfig;
import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.RotationController;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.util.DebugLogger;
import com.ahmed.sparemaze.util.HotbarUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class PearlCatchingManager {
    private final SlotManager slotManager;
    private final RotationController rotationController;
    private final PearlTracker tracker = new PearlTracker();
    private final PearlTrajectoryPredictor predictor = new PearlTrajectoryPredictor();
    private final WindChargeController windChargeController = new WindChargeController();

    private PearlCatchState state = PearlCatchState.IDLE;
    private int sequenceTicks = 0;
    private int stageTicks = 0;
    private Vec3d latestPredictedPos = Vec3d.ZERO;

    public PearlCatchingManager(SlotManager slotManager, RotationController rotationController) {
        this.slotManager = slotManager;
        this.rotationController = rotationController;
    }

    public void trigger(MinecraftClient client) {
        PearlConfig cfg = ConfigManager.getConfig().pearl;
        if (!cfg.enabled || client.player == null || client.world == null) {
            return;
        }

        if (state != PearlCatchState.IDLE) {
            cancel(client, "Cancelled");
            return;
        }

        if (!windChargeController.findAndPrepareWindCharge(client)) {
            sendPearlStatus(client, "No Wind Charge in hotbar");
            return;
        }

        this.sequenceTicks = 0;
        this.stageTicks = 0;
        slotManager.captureOriginalSlot(client);
        tracker.snapshotExistingPearls(client, cfg.maxTrackingRange);

        if (tracker.locateOwnActivePearl(client, cfg.maxTrackingRange)) {
            transitionTo(PearlCatchState.PEARL_FOUND);
            return;
        }

        int pearlSlot = HotbarUtils.findItemInHotbar(client, stack -> stack.isOf(Items.ENDER_PEARL));
        if (pearlSlot == -1) {
            slotManager.restoreOriginalSlot(client, false);
            sendPearlStatus(client, "No Ender Pearl in hotbar");
            return;
        }

        if (client.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack())) {
            slotManager.restoreOriginalSlot(client, false);
            return;
        }

        this.latestPredictedPos = predictor.preCalculateThrowIntercept(client);
        slotManager.switchToSlot(client, pearlSlot);
        InputManager.performUseItem(client);
        transitionTo(PearlCatchState.SEARCHING_PEARL);
    }

    public void tick(MinecraftClient client) {
        if (state == PearlCatchState.IDLE) {
            return;
        }

        PearlConfig cfg = ConfigManager.getConfig().pearl;
        if (!cfg.enabled || client.player == null || client.world == null || !client.player.isAlive()) {
            cancel(client, "Invalid state");
            return;
        }

        sequenceTicks++;
        stageTicks++;

        if (sequenceTicks > cfg.timeoutTicks) {
            fail(client, "Timed out");
            return;
        }

        switch (state) {
            case SEARCHING_PEARL -> {
                windChargeController.selectWindChargeSlot(client, slotManager);
                if (cfg.aimAssistance && latestPredictedPos != Vec3d.ZERO) {
                    if (!rotationController.isLocked()) {
                        rotationController.lockToTarget(client, latestPredictedPos, cfg.timeoutTicks);
                    } else {
                        rotationController.updateTargetRotation(client, latestPredictedPos);
                    }
                }
                if (tracker.locateOwnActivePearl(client, cfg.maxTrackingRange)) {
                    transitionTo(PearlCatchState.PREDICTING);
                    handlePredictionAndFire(client, cfg);
                } else if (stageTicks > 10) {
                    fail(client, "Pearl did not spawn");
                }
            }
            case PEARL_FOUND, TRACKING_PEARL, SEARCHING_WIND_CHARGE, PREDICTING, AIMING, READY_TO_FIRE -> {
                handlePredictionAndFire(client, cfg);
            }
            case FIRING -> {
                windChargeController.fireWindCharge(client);
                transitionTo(PearlCatchState.VERIFYING);
            }
            case VERIFYING -> {
                rotationController.releaseLock();
                if (stageTicks >= 2) {
                    transitionTo(PearlCatchState.COMPLETED);
                    sendPearlStatus(client, "Pearl Caught!");
                    resetToIdle(client, cfg.restorePreviousSlot);
                }
            }
            default -> resetToIdle(client, cfg.restorePreviousSlot);
        }
    }

    private void handlePredictionAndFire(MinecraftClient client, PearlConfig cfg) {
        if (!tracker.updateTelemetry(client)) {
            fail(client, "Pearl lost");
            return;
        }

        windChargeController.selectWindChargeSlot(client, slotManager);

        PearlTrajectoryPredictor.InterceptSolution sol = predictor.computeIntercept(
            client,
            tracker,
            cfg.predictionQuality,
            cfg.catchTimingSensitivity
        );

        if (!sol.valid()) {
            fail(client, "No trajectory");
            return;
        }

        this.latestPredictedPos = sol.interceptPoint();

        if (cfg.aimAssistance) {
            if (!rotationController.isLocked()) {
                rotationController.lockToTarget(client, latestPredictedPos, cfg.timeoutTicks);
            } else {
                rotationController.updateTargetRotation(client, latestPredictedPos);
            }
        }

        if (sol.fireNow()) {
            if (cfg.aimAssistance) {
                rotationController.updateTargetRotation(client, latestPredictedPos);
            }
            windChargeController.fireWindCharge(client);
            transitionTo(PearlCatchState.VERIFYING);
        }
    }

    private void sendPearlStatus(MinecraftClient client, String msg) {
        if (ConfigManager.getConfig().pearl.statusMessages && ConfigManager.getConfig().notifications) {
            DebugLogger.notifyPlayer(client, msg);
        }
    }

    public void cancel(MinecraftClient client, String reason) {
        if (state != PearlCatchState.IDLE) {
            resetToIdle(client, ConfigManager.getConfig().pearl.restorePreviousSlot);
        }
    }

    private void fail(MinecraftClient client, String reason) {
        sendPearlStatus(client, reason);
        resetToIdle(client, ConfigManager.getConfig().pearl.restorePreviousSlot);
    }

    private void resetToIdle(MinecraftClient client, boolean restoreSlot) {
        rotationController.releaseLock();
        slotManager.restoreOriginalSlot(client, restoreSlot);
        tracker.clear();
        windChargeController.reset();
        latestPredictedPos = Vec3d.ZERO;
        sequenceTicks = 0;
        stageTicks = 0;
        state = PearlCatchState.IDLE;
    }

    private void transitionTo(PearlCatchState nextState) {
        this.state = nextState;
        this.stageTicks = 0;
    }

    public PearlCatchState getState() {
        return state;
    }

    public PearlTracker getTracker() {
        return tracker;
    }

    public Vec3d getLatestPredictedPos() {
        return latestPredictedPos;
    }

    public WindChargeController getWindChargeController() {
        return windChargeController;
    }
}
