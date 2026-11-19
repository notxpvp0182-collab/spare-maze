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
        int baseSlot = (cfg.preferredBaseSlot >= 1 && cfg.preferredBaseSlot <= 9)
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

        slotManager.captureSpecificSlot(baseSlot);

        if (currentSlot != baseSlot) {
            slotManager.switchToSlot(client, baseSlot);
        }

        InputManager.performJabInput(client);
        slotManager.switchToSlot(client, this.spearSlot);
        InputManager.performJabInput(client);

        this.state = LungeState.RESTORING;
        this.stageTicks = 0;
    }

    public void tick(MinecraftClient client) {
        if (state == LungeState.RESTORING) {
            stageTicks++;
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
