package com.ahmed.sparemaze.input;

import com.ahmed.sparemaze.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

public final class InputManager {
    private InputManager() {}

    public static void performLeftClickAttack(MinecraftClient client, Entity explicitTarget) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        if (explicitTarget != null && explicitTarget.isAlive()) {
            client.interactionManager.attackEntity(client.player, explicitTarget);
            client.player.swingHand(Hand.MAIN_HAND);
        } else {
            ((MinecraftClientAccessor) client).invokeDoAttack();
        }
    }

    public static void performJabInput(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        ((MinecraftClientAccessor) client).invokeDoAttack();
    }

    public static void performUseItem(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        ((MinecraftClientAccessor) client).invokeDoItemUse();
    }
}
