package com.ahmed.sparemaze.util;

import com.ahmed.sparemaze.axe.AxeManager;
import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.mace.MaceManager;
import com.ahmed.sparemaze.pearl.PearlCatchState;
import com.ahmed.sparemaze.pearl.PearlCatchingManager;
import com.ahmed.sparemaze.pearl.PearlTracker;
import com.ahmed.sparemaze.spear.SpearManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class DebugLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpareMaze");

    private DebugLogger() {}

    public static void log(String message) {
        if (ConfigManager.getConfig().debugMode) {
            LOGGER.info("[SpareMaze-Debug] {}", message);
        }
    }

    public static void notifyPlayer(MinecraftClient client, String message) {
        if (client.player != null && ConfigManager.getConfig().notifications) {
            client.player.sendMessage(Text.literal("[Spare Maze] " + message), true);
        }
    }

    public static void tickDiagnostics(
        MinecraftClient client,
        PearlCatchingManager pearlManager,
        MaceManager maceManager,
        AxeManager axeManager,
        SpearManager spearManager,
        SlotManager slotManager,
        TargetManager targetManager
    ) {
        if (client.player == null || !ConfigManager.getConfig().debugMode) {
            return;
        }

        boolean anyActive = pearlManager.getState() != PearlCatchState.IDLE
            || ConfigManager.getConfig().stateInformation;

        if (!anyActive || TickUtils.getClientTickCount() % 5 != 0) {
            return;
        }

        PearlTracker tracker = pearlManager.getTracker();
        EnderPearlEntity pearl = tracker.getTrackedPearl();
        int pearlId = (pearl != null) ? pearl.getId() : -1;
        Vec3d pos = tracker.getCurrentPosition();
        Vec3d vel = tracker.getCurrentVelocity();
        Vec3d pred = pearlManager.getLatestPredictedPos();
        int wcSlot = pearlManager.getWindChargeController().getWindChargeSlot();
        int selSlot = client.player.getInventory().selectedSlot;
        int origSlot = slotManager.getOriginalSelectedSlot();
        LivingEntity target = targetManager.getLockedTarget();
        String targetName = (target != null) ? target.getName().getString() : "None";

        String diag = String.format(
            Locale.ROOT,
            "Tick:%d | Pearl[ID:%d Pos:(%.1f,%.1f,%.1f) Vel:(%.2f,%.2f,%.2f) Pred:(%.1f,%.1f,%.1f) WC:%d State:%s] | Slot[Cur:%d Orig:%d] | Target:%s | Slam:%s | Lunge:%s",
            TickUtils.getClientTickCount(),
            pearlId,
            pos.x, pos.y, pos.z,
            vel.x, vel.y, vel.z,
            pred.x, pred.y, pred.z,
            wcSlot,
            pearlManager.getState().name(),
            selSlot,
            origSlot,
            targetName,
            axeManager.getInstantSlamManager().getState().name(),
            spearManager.getSpearLungeManager().getState().name()
        );

        client.player.sendMessage(Text.literal(diag), true);
    }
}
