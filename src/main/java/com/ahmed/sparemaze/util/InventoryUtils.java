package com.ahmed.sparemaze.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public final class InventoryUtils {
    private InventoryUtils() {}

    public static boolean hasInHotbar(MinecraftClient client, Predicate<ItemStack> predicate) {
        return HotbarUtils.findItemInHotbar(client, predicate) != -1;
    }

    public static ItemStack getCurrentlyHeldStack(MinecraftClient client) {
        if (client.player == null) {
            return ItemStack.EMPTY;
        }
        return client.player.getMainHandStack();
    }
}
