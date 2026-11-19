package com.ahmed.sparemaze.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtils {
    private RotationUtils() {}

    public static float[] calculateRotations(Vec3d eyePos, Vec3d targetPos) {
        double diffX = targetPos.x - eyePos.x;
        double diffY = targetPos.y - eyePos.y;
        double diffZ = targetPos.z - eyePos.z;
        double horizontalDist = Math.hypot(diffX, diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, horizontalDist));

        return new float[] {
            MathHelper.wrapDegrees(yaw),
            MathHelper.clamp(pitch, -90.0f, 90.0f)
        };
    }
}
