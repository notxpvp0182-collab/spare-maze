package com.ahmed.sparemaze.pearl;

import com.ahmed.sparemaze.input.InputManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.util.HotbarUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class WindChargeController {
    private int windChargeSlot = -1;

    public boolean findAndPrepareWindCharge(MinecraftClient client) {
        this.windChargeSlot = HotbarUtils.findItemInHotbar(client, stack -> stack.isOf(Items.WIND_CHARGE));
        return this.windChargeSlot != -1;
    }

    public boolean selectWindChargeSlot(MinecraftClient client, SlotManager slotManager) {
        if (windChargeSlot == -1) {
            return false;
        }
        if (client.player != null && client.player.getItemCooldownManager().isCoolingDown(Items.WIND_CHARGE.getDefaultStack())) {
            return false;
        }
        return slotManager.switchToSlot(client, windChargeSlot);
    }

    public void fireWindCharge(MinecraftClient client) {
        InputManager.performUseItem(client);
    }

    public void reset() {
        this.windChargeSlot = -1;
    }

    public int getWindChargeSlot() {
        return windChargeSlot;
    }
}
