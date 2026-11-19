package com.ahmed.sparemaze.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public final class ItemUtils {
    private ItemUtils() {}

    public static boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.isIn(ItemTags.SWORDS);
    }

    public static boolean isAxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.isIn(ItemTags.AXES);
    }

    public static boolean isMace(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.isOf(Items.MACE);
    }

    public static boolean isSpear(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id.getPath().endsWith("spear");
    }

    public static boolean hasLungeEnchantment(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ItemEnchantmentsComponent enchantments = stack.getOrDefault(
            DataComponentTypes.ENCHANTMENTS,
            ItemEnchantmentsComponent.DEFAULT
        );
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            if (entry.getKey().isPresent()) {
                Identifier enchId = entry.getKey().get().getValue();
                if ("lunge".equals(enchId.getPath()) && enchantments.getLevel(entry) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
