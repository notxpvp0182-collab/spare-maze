package com.ahmed.sparemaze.pearl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;

public final class PearlTargetValidator {
    private PearlTargetValidator() {}

    public static boolean isLocalPlayerPearl(MinecraftClient client, EnderPearlEntity pearl) {
        if (client.player == null || pearl == null || pearl.isRemoved() || !pearl.isAlive()) {
            return false;
        }
        Entity owner = pearl.getOwner();
        if (owner != null) {
            return owner.getUuid().equals(client.player.getUuid());
        }
        return false;
    }
}
