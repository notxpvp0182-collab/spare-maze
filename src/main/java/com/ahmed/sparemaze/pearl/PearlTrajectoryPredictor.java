package com.ahmed.sparemaze.pearl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class PearlTrajectoryPredictor {
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    private static final double WIND_CHARGE_SPEED = 1.5;

    public record InterceptSolution(
        boolean valid,
        Vec3d interceptPoint,
        int ticksUntilIntercept,
        boolean fireNow
    ) {}

    public InterceptSolution computeIntercept(
        MinecraftClient client,
        PearlTracker tracker,
        int maxSteps,
        int timingSensitivity
    ) {
        if (client.player == null || tracker.getTrackedPearl() == null) {
            return new InterceptSolution(false, Vec3d.ZERO, -1, false);
        }

        Vec3d playerEye = client.player.getEyePos();
        Vec3d pearlPos = tracker.getCurrentPosition();
        Vec3d pearlVel = tracker.getCurrentVelocity();

        double bestError = Double.MAX_VALUE;
        Vec3d bestPoint = pearlPos;
        int bestStep = 1;

        Vec3d simPos = pearlPos;
        Vec3d simVel = pearlVel;

        int steps = Math.max(10, Math.min(60, maxSteps));
        for (int t = 1; t <= steps; t++) {
            simPos = simPos.add(simVel);
            simVel = simVel.multiply(PEARL_DRAG).subtract(0.0, PEARL_GRAVITY, 0.0);

            double distFromEye = playerEye.distanceTo(simPos);
            double windChargeTravelTicks = distFromEye / WIND_CHARGE_SPEED;
            double timeDiff = Math.abs(t - windChargeTravelTicks);

            if (timeDiff < bestError) {
                bestError = timeDiff;
                bestPoint = simPos;
                bestStep = t;
            }
        }

        double sensitivityThreshold = 0.8 + (timingSensitivity * 0.18);
        boolean withinWindow = bestError <= sensitivityThreshold && bestStep <= 15;

        return new InterceptSolution(true, bestPoint, bestStep, withinWindow);
    }
}
