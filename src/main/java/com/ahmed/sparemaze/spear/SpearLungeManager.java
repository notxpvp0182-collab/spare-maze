package com.ahmed.sparemaze.spear;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.SpearConfig;
import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.util.HotbarUtils;
import com.ahmed.sparemaze.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class SpearLungeManager {
    private final SlotManager slotManager;
    private LungeState state = LungeState.IDLE;
    private int spearSlot = -1;
    private int baseSlot = -1;
    private int stageTicks = 0;

    public SpearLungeManager(SlotManager slotManager) {
        this.slotManager = slotManager;
    }

    public void trigger(MinecraftClient client) {
        SpearConfig cfg = ConfigManager.getConfig().spear;
        if (!cfg.spearLungeEnabled || client.player == null || client.world == null || state != LungeState.IDLE) {
            return;
        }

        if (!validateVanillaLungeConditions(client.player)) {
            return;
        }

        int currentSlot = HotbarUtils.getSelectedSlot(client);
        this.baseSlot = (cfg.preferredBaseSlot >= 1 && cfg.preferredBaseSlot <= 9)
            ? (cfg.preferredBaseSlot - 1)
            : currentSlot;

        this.spearSlot = resolveSpearSlot(client, cfg);
        if (this.spearSlot == -1) {
            return;
        }

        ItemStack spearStack = client.player.getInventory().getStack(this.spearSlot);
        if (client.player.getItemCooldownManager().isCoolingDown(spearStack)) {
            return;
        }

        // ক্যাপচার করে রাখা যাতে কাজ শেষে এই নির্দিষ্ট স্লটেই ফিরে আসে
        slotManager.captureSpecificSlot(this.baseSlot);

        // সরাসরি স্পেয়ার স্লটে সুইচ করা (আগে অন্য স্লটে অ্যাটাক করে কোল্ডডাউন নষ্ট করা যাবে না!)
        slotManager.switchToSlot(client, this.spearSlot);

        // স্পেয়ার হাতে থাকা অবস্থায় Jab / Lunge ট্রিগার করা
        InputManager.performSpearLungeJab(client);

        this.state = LungeState.RESTORING;
        this.stageTicks = 0;
    }

    public void tick(MinecraftClient client) {
        if (state == LungeState.RESTORING) {
            stageTicks++;
            if (stageTicks == 1 && client.player != null && HotbarUtils.getSelectedSlot(client) == this.spearSlot) {
                // ১ টিক পর নিশ্চিত করা যাতে সার্ভার ও ক্লায়েন্টে লাঞ্জ ভেলোসিটি সম্পূর্ণ রেজিস্টার হয়
            }
            if (stageTicks >= 2) {
                slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().spear.restorePreviousSlot);
                state = LungeState.IDLE;
                stageTicks = 0;
            }
        }
    }

    private int resolveSpearSlot(MinecraftClient client, SpearConfig cfg) {
        if (cfg.preferredSpearSlot >= 1 && cfg.preferredSpearSlot <= 9) {
            int configuredIndex = cfg.preferredSpearSlot - 1;
            if (client.player != null) {
                ItemStack stack = client.player.getInventory().getStack(configuredIndex);
                if (!stack.isEmpty() && ItemUtils.isSpear(stack)) {
                    return configuredIndex;
                }
            }
        }
        if (cfg.requireLungeEnchantment) {
            int enchantedSlot = HotbarUtils.findItemInHotbar(
                client,
                stack -> ItemUtils.isSpear(stack) && ItemUtils.hasLungeEnchantment(stack)
            );
            if (enchantedSlot != -1) {
                return enchantedSlot;
            }
        }
        return HotbarUtils.findItemInHotbar(client, ItemUtils::isSpear);
    }

    private boolean validateVanillaLungeConditions(ClientPlayerEntity player) {
        if (!player.isAlive() || player.isSpectator()) {
            return false;
        }
        if (player.isSubmergedInWater() || player.isTouchingWater()) {
            return false;
        }
        if (player.isGliding()) {
            return false;
        }
        if (!player.getAbilities().creativeMode && player.getHungerManager().getFoodLevel() <= 6) {
            return false;
        }
        return true;
    }

    public void cancel(MinecraftClient client) {
        if (state != LungeState.IDLE) {
            slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().spear.restorePreviousSlot);
            state = LungeState.IDLE;
            stageTicks = 0;
        }
    }

    public LungeState getState() {
        return state;
    }
}
