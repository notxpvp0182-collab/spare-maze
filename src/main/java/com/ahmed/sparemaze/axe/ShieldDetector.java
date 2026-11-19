package com.ahmed.sparemaze.axe;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

public final class ShieldDetector {
    private ShieldDetector() {}

    public static boolean isActivelyBlockingWithShield(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target.isBlocking()) {
            return true;
        }
        return target.isUsingItem() && target.getActiveItem().isOf(Items.SHIELD);
    }

    public static boolean hasShieldBrokenOrDropped(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof PlayerEntity player) {
            if (player.getItemCooldownManager().isCoolingDown(Items.SHIELD.getDefaultStack())) {
                return true;
            }
        }
        return !isActivelyBlockingWithShield(target);
    }
}
