package com.ahmed.sparemaze.targeting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class TargetValidator {
    private TargetValidator() {}

    public static boolean isValidCombatTarget(MinecraftClient client, Entity entity, double maxRange) {
        if (client.player == null || client.world == null || entity == null) {
            return false;
        }
        if (entity == client.player || !entity.isAlive() || entity.isRemoved()) {
            return false;
        }
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        if (living.isDead() || entity.isSpectator()) {
            return false;
        }
        double distSq = client.player.squaredDistanceTo(entity);
        return distSq <= (maxRange * maxRange);
    }
}
