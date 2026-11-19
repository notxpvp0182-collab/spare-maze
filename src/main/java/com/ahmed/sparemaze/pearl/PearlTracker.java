package com.ahmed.sparemaze.pearl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PearlTracker {
    private int ignoredPearlId = -1;
    private EnderPearlEntity trackedPearl = null;
    private Vec3d currentPosition = Vec3d.ZERO;
    private Vec3d currentVelocity = Vec3d.ZERO;
    private double distanceFromPlayer = 0.0;

    public void snapshotExistingPearls(MinecraftClient client, double range) {
        this.ignoredPearlId = -1;
        if (client.player == null || client.world == null) {
            return;
        }
        Box smallBox = client.player.getBoundingBox().expand(6.0);
        List<EnderPearlEntity> existing = client.world.getEntitiesByClass(
            EnderPearlEntity.class,
            smallBox,
            pearl -> PearlTargetValidator.isLocalPlayerPearl(client, pearl)
        );
        if (!existing.isEmpty()) {
            this.ignoredPearlId = existing.get(0).getId();
        }
    }

    public boolean locateOwnActivePearl(MinecraftClient client, double maxRange) {
        if (client.player == null || client.world == null) {
            return false;
        }
        Box fastSearchBox = client.player.getBoundingBox().expand(10.0);
        List<EnderPearlEntity> candidates = client.world.getEntitiesByClass(
            EnderPearlEntity.class,
            fastSearchBox,
            pearl -> PearlTargetValidator.isLocalPlayerPearl(client, pearl)
        );

        EnderPearlEntity bestCandidate = null;
        int youngestAge = Integer.MAX_VALUE;

        for (int i = 0, size = candidates.size(); i < size; i++) {
            EnderPearlEntity pearl = candidates.get(i);
            if (pearl.getId() != ignoredPearlId && pearl.age < youngestAge) {
                bestCandidate = pearl;
                youngestAge = pearl.age;
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
        double px = trackedPearl.getX();
        double py = trackedPearl.getY();
        double pz = trackedPearl.getZ();
        this.currentPosition = new Vec3d(px, py, pz);
        this.currentVelocity = trackedPearl.getVelocity();

        double dx = client.player.getX() - px;
        double dy = client.player.getEyeY() - py;
        double dz = client.player.getZ() - pz;
        this.distanceFromPlayer = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return true;
    }

    public void clear() {
        ignoredPearlId = -1;
        trackedPearl = null;
        currentPosition = Vec3d.ZERO;
        currentVelocity = Vec3d.ZERO;
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

    public double getDistanceFromPlayer() {
        return distanceFromPlayer;
    }
}
