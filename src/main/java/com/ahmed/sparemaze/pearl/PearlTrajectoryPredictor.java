package com.ahmed.sparemaze.pearl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PearlTrajectoryPredictor {
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    private static final double WIND_CHARGE_SPEED = 1.55;

    public record InterceptSolution(
        boolean valid,
        Vec3d interceptPoint,
        int ticksUntilIntercept,
        boolean fireNow
    ) {}

    public Vec3d preCalculateThrowIntercept(MinecraftClient client) {
        if (client.player == null) {
            return Vec3d.ZERO;
        }
        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();
        float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float g = -MathHelper.sin(pitch * 0.017453292F);
        float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);

        Vec3d throwVel = new Vec3d(f, g, h).normalize().multiply(1.5).add(client.player.getVelocity());
        Vec3d startPos = client.player.getEyePos().subtract(0, 0.1, 0);

        Vec3d simPos = startPos;
        Vec3d simVel = throwVel;
        for (int i = 0; i < 4; i++) {
            simPos = simPos.add(simVel);
            simVel = simVel.multiply(PEARL_DRAG).subtract(0.0, PEARL_GRAVITY, 0.0);
        }
        return simPos;
    }

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
        int pearlAge = tracker.getTrackedPearl().age;

        Vec3d simPos = pearlPos;
        Vec3d simVel = pearlVel;
        Vec3d bestPoint = pearlPos.add(pearlVel);
        int bestStep = 1;
        double minError = Double.MAX_VALUE;

        int steps = Math.max(8, Math.min(45, maxSteps));
        for (int t = 1; t <= steps; t++) {
            simPos = simPos.add(simVel);
            simVel = simVel.multiply(PEARL_DRAG).subtract(0.0, PEARL_GRAVITY, 0.0);

            double dist = playerEye.distanceTo(simPos);
            double wcTicks = dist / WIND_CHARGE_SPEED;
            double diff = Math.abs(t - wcTicks);

            if (diff < minError) {
                minError = diff;
                bestPoint = simPos;
                bestStep = t;
            }
        }

        double currentDist = playerEye.distanceTo(pearlPos);
        boolean optimalCatchWindow = (pearlAge >= 1 && currentDist >= 1.2 && currentDist <= 14.0)
            || (minError <= (0.65 + timingSensitivity * 0.15) && bestStep <= 8);

        return new InterceptSolution(true, bestPoint, bestStep, optimalCatchWindow);
    }
}
