package com.ahmed.sparemaze.input;

import com.ahmed.sparemaze.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

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

    public static void performSpearLungeJab(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        // যদি মাটির দিকে (BLOCK) এম থাকে এবং আকাশে না থাকে, তখন ব্লক মাইনিং না করেও যাতে আকাশে তাকানোর মতোই ফ্রিলি লাঞ্জ হয়
        HitResult originalHit = client.crosshairTarget;
        if (originalHit != null && originalHit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) originalHit;
            client.crosshairTarget = BlockHitResult.createMissed(bhr.getPos(), bhr.getSide(), bhr.getBlockPos());
        }

        boolean attacked = ((MinecraftClientAccessor) client).invokeDoAttack();

        if (!attacked && client.player != null) {
            client.player.swingHand(Hand.MAIN_HAND);
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }

        client.crosshairTarget = originalHit;
    }

    public static void performUseItem(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        ((MinecraftClientAccessor) client).invokeDoItemUse();
    }
}
