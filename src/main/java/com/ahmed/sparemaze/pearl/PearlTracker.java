package com.ahmed.sparemaze.pearl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PearlTracker {
    private final Set<Integer> preExistingPearlIds = new HashSet<>();
    private EnderPearlEntity trackedPearl = null;
    private Vec3d currentPosition = Vec3d.ZERO;
    private Vec3d currentVelocity = Vec3d.ZERO;
    private Vec3d previousVelocity = Vec3d.ZERO;
    private double distanceFromPlayer = 0.0;

    public void snapshotExistingPearls(MinecraftClient client, double range) {
        preExistingPearlIds.clear();
        if (client.player == null || client.world == null) {
            return;
        }
        Box searchBox = client.player.getBoundingBox().expand(range);
        List<EnderPearlEntity> existing = client.world.getEntitiesByClass(
            EnderPearlEntity.class,
            searchBox,
            pearl -> true
        );
        for (EnderPearlEntity pearl : existing) {
            preExistingPearlIds.add(pearl.getId());
        }
    }

    public boolean locateOwnActivePearl(MinecraftClient client, double maxRange) {
        if (client.player == null || client.world == null) {
            return false;
        }
        Box searchBox = client.player.getBoundingBox().expand(maxRange);
        List<EnderPearlEntity> candidates = client.world.getEntitiesByClass(
            EnderPearlEntity.class,
            searchBox,
            pearl -> PearlTargetValidator.isLocalPlayerPearl(client, pearl)
        );

        EnderPearlEntity bestCandidate = null;
        int youngestAge = Integer.MAX_VALUE;

        for (EnderPearlEntity pearl : candidates) {
            if (!preExistingPearlIds.contains(pearl.getId()) && pearl.age < youngestAge) {
                bestCandidate = pearl;
                youngestAge = pearl.age;
            }
        }

        if (bestCandidate == null) {
            for (EnderPearlEntity pearl : candidates) {
                if (pearl.age < youngestAge) {
                    bestCandidate = pearl;
                    youngestAge = pearl.age;
                }
            }
        }

        if (bestCandidate != null) {
            this.trackedPearl = bestCandidate;
            updateTelemetry(client);
            return true;
        }
        return false;
    }

    public boolean updateTelemetry(MinecraftClient client) {
        if (client.player == null || trackedPearl == null || trackedPearl.isRemoved() || !trackedPearl.isAlive()) {
            return false;
        }
        if (!PearlTargetValidator.isLocalPlayerPearl(client, trackedPearl)) {
            return false;
        }
        this.previousVelocity = this.currentVelocity;
        this.currentPosition = new Vec3d(trackedPearl.getX(), trackedPearl.getY(), trackedPearl.getZ());
        this.currentVelocity = trackedPearl.getVelocity();
        this.distanceFromPlayer = client.player.getEyePos().distanceTo(currentPosition);
        return true;
    }

    public boolean hasDetectedDeflectionOrCatch() {
        if (trackedPearl == null || trackedPearl.isRemoved()) {
            return true;
        }
        double deltaVel = currentVelocity.subtract(previousVelocity).length();
        return deltaVel > 0.32;
    }

    public void clear() {
        preExistingPearlIds.clear();
        trackedPearl = null;
        currentPosition = Vec3d.ZERO;
        currentVelocity = Vec3d.ZERO;
        previousVelocity = Vec3d.ZERO;
        distanceFromPlayer = 0.0;
    }

    public EnderPearlEntity getTrackedPearl() {
        return trackedPearl;
    }

    public Vec3d getCurrentPosition() {
        return currentPosition;
    }

    public Vec3d getCurrentVelocity() {
        return currentVelocity;
    }

    public double getVerticalMovement() {
        return currentVelocity.y;
    }

    public double getHorizontalMovement() {
        return Math.hypot(currentVelocity.x, currentVelocity.z);
    }

    public double getDistanceFromPlayer() {
        return distanceFromPlayer;
    }
}
