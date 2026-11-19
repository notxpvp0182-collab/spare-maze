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
    private int verificationTicks = 0;
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
            cancel(client, "Manual toggle off");
            return;
        }

        this.sequenceTicks = 0;
        this.verificationTicks = 0;
        slotManager.captureOriginalSlot(client);
        tracker.snapshotExistingPearls(client, cfg.maxTrackingRange);

        if (tracker.locateOwnActivePearl(client, cfg.maxTrackingRange)) {
            transitionTo(PearlCatchState.PEARL_FOUND);
            return;
        }

        transitionTo(PearlCatchState.SEARCHING_PEARL);
        int pearlSlot = HotbarUtils.findItemInHotbar(client, stack -> stack.isOf(Items.ENDER_PEARL));
        if (pearlSlot == -1) {
            fail(client, "No Ender Pearl in hotbar");
            return;
        }

        if (client.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack())) {
            fail(client, "Ender Pearl is on cooldown");
            return;
        }

        slotManager.switchToSlot(client, pearlSlot);
        InputManager.performUseItem(client);
    }

    public void tick(MinecraftClient client) {
        if (state == PearlCatchState.IDLE) {
            return;
        }

        PearlConfig cfg = ConfigManager.getConfig().pearl;
        if (!cfg.enabled || client.player == null || client.world == null || !client.player.isAlive()) {
            cancel(client, "Invalid player or world state");
            return;
        }

        sequenceTicks++;
        if (sequenceTicks > cfg.timeoutTicks) {
            fail(client, "Pearl Catch sequence timed out");
            return;
        }

        switch (state) {
            case SEARCHING_PEARL -> handleSearchingPearl(client, cfg);
            case PEARL_FOUND -> handlePearlFound(client, cfg);
            case TRACKING_PEARL, SEARCHING_WIND_CHARGE -> handleSearchingWindCharge(client, cfg);
            case PREDICTING, AIMING, READY_TO_FIRE -> handlePredictionAndAim(client, cfg);
            case FIRING -> handleFiring(client, cfg);
            case VERIFYING -> handleVerifying(client, cfg);
            case COMPLETED, CANCELLED, FAILED -> resetToIdle(client, cfg.restorePreviousSlot);
            default -> resetToIdle(client, cfg.restorePreviousSlot);
        }
    }

    private void handleSearchingPearl(MinecraftClient client, PearlConfig cfg) {
        if (tracker.locateOwnActivePearl(client, cfg.maxTrackingRange)) {
            transitionTo(PearlCatchState.PEARL_FOUND);
        } else if (sequenceTicks > 12) {
            fail(client, "Own thrown Ender Pearl did not spawn");
        }
    }

    private void handlePearlFound(MinecraftClient client, PearlConfig cfg) {
        if (!tracker.updateTelemetry(client)) {
            fail(client, "Tracked pearl lost immediately");
            return;
        }
        transitionTo(PearlCatchState.SEARCHING_WIND_CHARGE);
        handleSearchingWindCharge(client, cfg);
    }

    private void handleSearchingWindCharge(MinecraftClient client, PearlConfig cfg) {
        if (!tracker.updateTelemetry(client)) {
            fail(client, "Tracked pearl disappeared");
            return;
        }
        if (!cfg.windChargeDetection || !windChargeController.findAndPrepareWindCharge(client)) {
            fail(client, "No Wind Charge available in hotbar");
            return;
        }
        if (!windChargeController.selectWindChargeSlot(client, slotManager)) {
            return;
        }
        transitionTo(PearlCatchState.PREDICTING);
        handlePredictionAndAim(client, cfg);
    }

    private void handlePredictionAndAim(MinecraftClient client, PearlConfig cfg) {
        if (!tracker.updateTelemetry(client)) {
            fail(client, "Tracked pearl became invalid");
            return;
        }
        if (tracker.getDistanceFromPlayer() > cfg.maxTrackingRange) {
            fail(client, "Tracked pearl exceeded max tracking range");
            return;
        }

        PearlTrajectoryPredictor.InterceptSolution solution = predictor.computeIntercept(
            client,
            tracker,
            cfg.predictionQuality,
            cfg.catchTimingSensitivity
        );

        if (!solution.valid()) {
            fail(client, "Unable to compute intercept trajectory");
            return;
        }

        this.latestPredictedPos = solution.interceptPoint();

        if (cfg.aimAssistance) {
            transitionTo(PearlCatchState.AIMING);
            if (!rotationController.isLocked()) {
                rotationController.lockToTarget(client, latestPredictedPos, cfg.timeoutTicks);
            } else {
                rotationController.updateTargetRotation(client, latestPredictedPos);
            }
        }

        if (solution.fireNow()) {
            transitionTo(PearlCatchState.READY_TO_FIRE);
            handleFiring(client, cfg);
        }
    }

    private void handleFiring(MinecraftClient client, PearlConfig cfg) {
        transitionTo(PearlCatchState.FIRING);
        if (!windChargeController.selectWindChargeSlot(client, slotManager)) {
            fail(client, "Wind Charge unavailable on firing tick");
            return;
        }
        if (cfg.aimAssistance) {
            rotationController.updateTargetRotation(client, latestPredictedPos);
        }
        windChargeController.fireWindCharge(client);
        this.verificationTicks = 0;
        transitionTo(PearlCatchState.VERIFYING);
    }

    private void handleVerifying(MinecraftClient client, PearlConfig cfg) {
        verificationTicks++;
        rotationController.releaseLock();

        boolean alive = tracker.updateTelemetry(client);
        if (!alive || tracker.hasDetectedDeflectionOrCatch() || verificationTicks >= 3) {
            transitionTo(PearlCatchState.COMPLETED);
            DebugLogger.notifyPlayer(client, "Pearl Catch completed.");
            resetToIdle(client, cfg.restorePreviousSlot);
        }
    }

    public void cancel(MinecraftClient client, String reason) {
        if (state != PearlCatchState.IDLE) {
            transitionTo(PearlCatchState.CANCELLED);
            DebugLogger.log("Pearl Catch cancelled: " + reason);
            resetToIdle(client, ConfigManager.getConfig().pearl.restorePreviousSlot);
        }
    }

    private void fail(MinecraftClient client, String reason) {
        transitionTo(PearlCatchState.FAILED);
        DebugLogger.notifyPlayer(client, "Pearl Catch failed: " + reason);
        resetToIdle(client, ConfigManager.getConfig().pearl.restorePreviousSlot);
    }

    private void resetToIdle(MinecraftClient client, boolean restoreSlot) {
        rotationController.releaseLock();
        slotManager.restoreOriginalSlot(client, restoreSlot);
        tracker.clear();
        windChargeController.reset();
        latestPredictedPos = Vec3d.ZERO;
        sequenceTicks = 0;
        verificationTicks = 0;
        state = PearlCatchState.IDLE;
    }

    private void transitionTo(PearlCatchState nextState) {
        this.state = nextState;
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
