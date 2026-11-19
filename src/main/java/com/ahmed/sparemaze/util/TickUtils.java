package com.ahmed.sparemaze.util;

public final class TickUtils {
    private static long clientTickCount = 0L;

    private TickUtils() {}

    public static void onTick() {
        clientTickCount++;
    }

    public static long getClientTickCount() {
        return clientTickCount;
    }
}
