package com.ahmed.sparemaze.targeting;

import com.ahmed.sparemaze.util.EntityUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TargetManager {
    private LivingEntity lockedTarget = null;

    public LivingEntity acquireTarget(MinecraftClient client, double maxRange) {
        if (client.player == null || client.world == null) {
            return null;
        }

        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (TargetValidator.isValidCombatTarget(client, hitEntity, maxRange)) {
                lockedTarget = (LivingEntity) hitEntity;
                return lockedTarget;
            }
        }

        LivingEntity aimedEntity = EntityUtils.findCrosshairLivingTarget(client, maxRange);
        if (aimedEntity != null && TargetValidator.isValidCombatTarget(client, aimedEntity, maxRange)) {
            lockedTarget = aimedEntity;
            return lockedTarget;
        }

        return null;
    }

    public boolean validateLockedTarget(MinecraftClient client, double maxRange) {
        if (lockedTarget == null) {
            return false;
        }
        if (!TargetValidator.isValidCombatTarget(client, lockedTarget, maxRange)) {
            lockedTarget = null;
            return false;
        }
        return true;
    }

    public LivingEntity getLockedTarget() {
        return lockedTarget;
    }

    public void clearTarget() {
        this.lockedTarget = null;
    }
}
