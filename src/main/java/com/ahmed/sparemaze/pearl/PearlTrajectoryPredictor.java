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
        float yawRad = client.player.getYaw() * 0.017453292F;
        float pitchRad = client.player.getPitch() * 0.017453292F;
        double cosPitch = MathHelper.cos(pitchRad);

        double vx = -MathHelper.sin(yawRad) * cosPitch;
        double vy = -MathHelper.sin(pitchRad);
        double vz = MathHelper.cos(yawRad) * cosPitch;

        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (len > 0.0) {
            vx = (vx / len) * 1.5;
            vy = (vy / len) * 1.5;
            vz = (vz / len) * 1.5;
        }

        Vec3d pVel = client.player.getVelocity();
        vx += pVel.x;
        vy += pVel.y;
        vz += pVel.z;

        double sx = client.player.getX();
        double sy = client.player.getEyeY() - 0.1;
        double sz = client.player.getZ();

        for (int i = 0; i < 4; i++) {
            sx += vx;
            sy += vy;
            sz += vz;
            vx *= PEARL_DRAG;
            vy = vy * PEARL_DRAG - PEARL_GRAVITY;
            vz *= PEARL_DRAG;
        }
        return new Vec3d(sx, sy, sz);
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

        double eyeX = client.player.getX();
        double eyeY = client.player.getEyeY();
        double eyeZ = client.player.getZ();

        Vec3d pearlPos = tracker.getCurrentPosition();
        Vec3d pearlVel = tracker.getCurrentVelocity();
        int pearlAge = tracker.getTrackedPearl().age;

        double simX = pearlPos.x;
        double simY = pearlPos.y;
        double simZ = pearlPos.z;

        double velX = pearlVel.x;
        double velY = pearlVel.y;
        double velZ = pearlVel.z;

        double bestX = simX + velX;
        double bestY = simY + velY;
        double bestZ = simZ + velZ;
        int bestStep = 1;
        double minError = Double.MAX_VALUE;

        int steps = Math.max(6, Math.min(25, maxSteps));
        for (int t = 1; t <= steps; t++) {
            simX += velX;
            simY += velY;
            simZ += velZ;

            velX *= PEARL_DRAG;
            velY = velY * PEARL_DRAG - PEARL_GRAVITY;
            velZ *= PEARL_DRAG;

            double dx = simX - eyeX;
            double dy = simY - eyeY;
            double dz = simZ - eyeZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double wcTicks = dist / WIND_CHARGE_SPEED;
            double diff = Math.abs(t - wcTicks);

            if (diff < minError) {
                minError = diff;
                bestX = simX;
                bestY = simY;
                bestZ = simZ;
                bestStep = t;
            }
        }

        double curDx = pearlPos.x - eyeX;
        double curDy = pearlPos.y - eyeY;
        double curDz = pearlPos.z - eyeZ;
        double currentDistSq = curDx * curDx + curDy * curDy + curDz * curDz;

        boolean optimalCatchWindow = (pearlAge >= 1 && currentDistSq >= 1.44 && currentDistSq <= 196.0)
            || (minError <= (0.65 + timingSensitivity * 0.15) && bestStep <= 8);

        return new InterceptSolution(true, new Vec3d(bestX, bestY, bestZ), bestStep, optimalCatchWindow);
    }
}
