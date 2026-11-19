package com.ahmed.sparemaze.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class EntityUtils {
    private EntityUtils() {}

    public static LivingEntity findCrosshairLivingTarget(MinecraftClient client, double maxRange) {
        if (client.player == null || client.world == null) {
            return null;
        }
        Vec3d eyePos = client.player.getEyePos();
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        Vec3d traceEnd = eyePos.add(lookVec.multiply(maxRange));
        Box searchBox = client.player.getBoundingBox().stretch(lookVec.multiply(maxRange)).expand(1.0);

        EntityHitResult hitResult = ProjectileUtil.raycast(
            client.player,
            eyePos,
            traceEnd,
            searchBox,
            entity -> !entity.isSpectator() && entity.canHit() && entity instanceof LivingEntity,
            maxRange * maxRange
        );

        if (hitResult != null) {
            Entity hit = hitResult.getEntity();
            if (hit instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}
