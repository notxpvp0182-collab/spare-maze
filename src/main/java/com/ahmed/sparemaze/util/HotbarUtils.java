package com.ahmed.sparemaze.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.function.Predicate;

public final class HotbarUtils {
    public static final int HOTBAR_SIZE = 9;

    private HotbarUtils() {}

    public static boolean isValidHotbarSlot(int slot) {
        return slot >= 0 && slot < HOTBAR_SIZE;
    }

    public static int findItemInHotbar(MinecraftClient client, Predicate<ItemStack> predicate) {
        if (client.player == null) {
            return -1;
        }
        PlayerInventory inventory = client.player.getInventory();
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public static void selectHotbarSlot(MinecraftClient client, int slot) {
        if (client.player == null || !isValidHotbarSlot(slot)) {
            return;
        }
        PlayerInventory inventory = client.player.getInventory();
        if (inventory.selectedSlot != slot) {
            inventory.selectedSlot = slot;
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }
        }
    }
}
