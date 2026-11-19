package com.ahmed.sparemaze.input;

import com.ahmed.sparemaze.util.RotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationController {
    public static boolean isCameraLocked = false;

    private float targetYaw = 0.0f;
    private float targetPitch = 0.0f;
    private int lockTicksRemaining = 0;

    public void lockToTarget(MinecraftClient client, Vec3d targetPos, int maxDurationTicks) {
        if (client.player == null) {
            return;
        }
        float[] rotations = RotationUtils.calculateRotations(client.player.getEyePos(), targetPos);
        this.targetYaw = rotations[0];
        this.targetPitch = MathHelper.clamp(rotations[1], -90.0f, 90.0f);
        isCameraLocked = true;
        this.lockTicksRemaining = maxDurationTicks;
        applyRotation(client);
    }

    public void updateTargetRotation(MinecraftClient client, Vec3d targetPos) {
        if (!isCameraLocked || client.player == null) {
            return;
        }
        float[] rotations = RotationUtils.calculateRotations(client.player.getEyePos(), targetPos);
        this.targetYaw = rotations[0];
        this.targetPitch = MathHelper.clamp(rotations[1], -90.0f, 90.0f);
        applyRotation(client);
    }

    public void tick(MinecraftClient client) {
        if (!isCameraLocked) {
            return;
        }
        if (client.player == null || !client.player.isAlive() || --lockTicksRemaining <= 0) {
            releaseLock();
            return;
        }
        applyRotation(client);
    }

    private void applyRotation(MinecraftClient client) {
        if (client.player != null) {
            client.player.setYaw(targetYaw);
            client.player.setPitch(targetPitch);
            client.player.headYaw = targetYaw;
        }
    }

    public void releaseLock() {
        isCameraLocked = false;
        this.lockTicksRemaining = 0;
    }

    public boolean isLocked() {
        return isCameraLocked;
    }
}
