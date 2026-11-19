package com.ahmed.sparemaze.spear;

import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.SpearConfig;
import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.util.DebugLogger;
import com.ahmed.sparemaze.util.HotbarUtils;
import com.ahmed.sparemaze.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class SpearLungeManager {
    private final SlotManager slotManager;
    private LungeState state = LungeState.IDLE;

    public SpearLungeManager(SlotManager slotManager) {
        this.slotManager = slotManager;
    }

    public void trigger(MinecraftClient client) {
        SpearConfig cfg = ConfigManager.getConfig().spear;
        if (!cfg.spearLungeEnabled || client.player == null || client.world == null) {
            return;
        }

        if (state != LungeState.IDLE) {
            cancel(client);
        }

        if (!validateVanillaLungeConditions(client.player)) {
            fail(client, "Player state ineligible for Spear Lunge (water/elytra/hunger)");
            return;
        }

        state = LungeState.CAPTURE_SLOT;
        slotManager.captureOriginalSlot(client);

        state = LungeState.SEARCHING_SPEAR;
        int spearSlot = findUsableSpearSlot(client, cfg.requireLungeEnchantment);
        if (spearSlot == -1) {
            fail(client, "No valid Lunge Spear found in hotbar");
            return;
        }

        ItemStack spearStack = client.player.getInventory().getStack(spearSlot);
        if (client.player.getItemCooldownManager().isCoolingDown(spearStack)) {
            fail(client, "Spear is currently on cooldown");
            return;
        }

        state = LungeState.SWITCHING;
        slotManager.switchToSlot(client, spearSlot);

        state = LungeState.JAB_INPUT;
        InputManager.performJabInput(client);

        state = LungeState.LUNGE_ACTIVE;
        state = LungeState.RESTORING;
        slotManager.restoreOriginalSlot(client, cfg.restorePreviousSlot);

        state = LungeState.COMPLETED;
        state = LungeState.IDLE;
    }

    public void tick(MinecraftClient client) {
        if (state != LungeState.IDLE && state != LungeState.COMPLETED) {
            slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().spear.restorePreviousSlot);
            state = LungeState.IDLE;
        }
    }

    private int findUsableSpearSlot(MinecraftClient client, boolean requireLunge) {
        if (requireLunge) {
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
        }
    }

    private void fail(MinecraftClient client, String reason) {
        state = LungeState.FAILED;
        DebugLogger.notifyPlayer(client, "Spear Lunge: " + reason);
        slotManager.restoreOriginalSlot(client, ConfigManager.getConfig().spear.restorePreviousSlot);
        state = LungeState.IDLE;
    }

    public LungeState getState() {
        return state;
    }
}
