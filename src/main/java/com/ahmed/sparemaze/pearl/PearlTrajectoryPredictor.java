package com.ahmed.sparemaze.pearl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PearlTrajectoryPredictor {
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    private static final double WIND_CHARGE_SPEED = 1.5;
    private static final double WIND_CHARGE_ACCEL_DRAG = 0.99;

    public record InterceptSolution(
        boolean valid,
        Vec3d aimPoint,
        int ticksUntilHit,
        boolean fireNow
    ) {}

    public Vec3d preCalculateThrowIntercept(MinecraftClient client) {
        if (client.player == null) {
            return Vec3d.ZERO;
        }
        float yawRad = client.player.getYaw() * 0.017453292F;
        float pitchRad = client.player.getPitch() * 0.017453292F;
        double cosPitch = MathHelper.cos(pitchRad);

        double dirX = -MathHelper.sin(yawRad) * cosPitch;
        double dirY = -MathHelper.sin(pitchRad);
        double dirZ = MathHelper.cos(yawRad) * cosPitch;

        double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        double vx = (len > 0.0) ? (dirX / len) * 1.5 : 0.0;
        double vy = (len > 0.0) ? (dirY / len) * 1.5 : 0.0;
        double vz = (len > 0.0) ? (dirZ / len) * 1.5 : 0.0;

        Vec3d pVel = client.player.getVelocity();
        vx += pVel.x;
        if (!client.player.isOnGround()) {
            vy += pVel.y;
        }
        vz += pVel.z;

        double sx = client.player.getX();
        double sy = client.player.getEyeY() - 0.1;
        double sz = client.player.getZ();

        // ৩ টিক এগিয়ে প্রি-এইম পজিশন বের করা যাতে পার্ল স্পন হওয়ার আগেই ক্যামেরা পারফেক্ট জায়গায় থাকে
        for (int i = 0; i < 3; i++) {
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

        // উইন্ড চার্জের আসল স্পন পজিশন (প্লেয়ারের চোখ থেকে ০.১ ব্লক নিচে)
        double originX = client.player.getX();
        double originY = client.player.getEyeY() - 0.1;
        double originZ = client.player.getZ();

        Vec3d pVel = client.player.getVelocity();
        double pVx = pVel.x;
        double pVy = client.player.isOnGround() ? 0.0 : pVel.y;
        double pVz = pVel.z;

        Vec3d pearlPos = tracker.getCurrentPosition();
        Vec3d pearlVel = tracker.getCurrentVelocity();

        double simX = pearlPos.x;
        double simY = pearlPos.y;
        double simZ = pearlPos.z;

        double velX = pearlVel.x;
        double velY = pearlVel.y;
        double velZ = pearlVel.z;

        double bestAimX = simX + velX;
        double bestAimY = simY + velY;
        double bestAimZ = simZ + velZ;
        int bestStep = 1;
        double smallestGap = Double.MAX_VALUE;

        int steps = Math.max(8, Math.min(35, maxSteps));

        for (int t = 1; t <= steps; t++) {
            simX += velX;
            simY += velY;
            simZ += velZ;

            velX *= PEARL_DRAG;
            velY = velY * PEARL_DRAG - PEARL_GRAVITY;
            velZ *= PEARL_DRAG;

            // যদি আমরা এখনই উইন্ড চার্জ ফায়ার করি, t টিক পরে উইন্ড চার্জ কত দূরত্ব অতিক্রম করবে?
            // প্লেয়ারের মুভমেন্ট মোমেন্টাম বাদ দিয়ে টার্গেট ভেক্টর বের করা
            double targetDx = (simX - originX) - (pVx * t);
            double targetDy = (simY - originY) - (pVy * t);
            double targetDz = (simZ - originZ) - (pVz * t);

            double requiredDist = Math.sqrt(targetDx * targetDx + targetDy * targetDy + targetDz * targetDz);
            double wcReachDist = WIND_CHARGE_SPEED * t;

            double distanceGap = Math.abs(requiredDist - wcReachDist);

            if (distanceGap < smallestGap) {
                smallestGap = distanceGap;
                bestAimX = originX + targetDx;
                bestAimY = originY + targetDy;
                bestAimZ = originZ + targetDz;
                bestStep = t;
            }
        }

        // উইন্ড চার্জের হিটবক্স ও ব্লাস্ট রেডিয়াসের ভেতরে (১.৩৫ ব্লক) আসলেই সাথে সাথে ফায়ার করবে
        double triggerRadius = 0.85 + (timingSensitivity * 0.08);
        boolean shouldFireNow = (smallestGap <= triggerRadius && bestStep >= 1 && bestStep <= 14);

        return new InterceptSolution(true, new Vec3d(bestAimX, bestAimY, bestAimZ), bestStep, shouldFireNow);
    }
}
