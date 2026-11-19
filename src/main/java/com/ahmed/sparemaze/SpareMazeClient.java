package com.ahmed.sparemaze;

import com.ahmed.sparemaze.axe.AxeManager;
import com.ahmed.sparemaze.axe.InstantSlamState;
import com.ahmed.sparemaze.config.ConfigManager;
import com.ahmed.sparemaze.config.SpareMazeConfig;
import com.ahmed.sparemaze.controls.KeybindManager;
import com.ahmed.sparemaze.input.RotationController;
import com.ahmed.sparemaze.input.SlotManager;
import com.ahmed.sparemaze.mace.MaceManager;
import com.ahmed.sparemaze.mace.MaceState;
import com.ahmed.sparemaze.pearl.PearlCatchState;
import com.ahmed.sparemaze.pearl.PearlCatchingManager;
import com.ahmed.sparemaze.spear.LungeState;
import com.ahmed.sparemaze.spear.SpearManager;
import com.ahmed.sparemaze.targeting.TargetManager;
import com.ahmed.sparemaze.util.DebugLogger;
import com.ahmed.sparemaze.util.TickUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public class SpareMazeClient implements ClientModInitializer {
    public static final String MOD_ID = "sparemaze";
    private static SpareMazeClient instance;

    private final SlotManager slotManager = new SlotManager();
    private final RotationController rotationController = new RotationController();
    private final TargetManager targetManager = new TargetManager();
    private final PearlCatchingManager pearlCatchingManager = new PearlCatchingManager(slotManager, rotationController);
    private final MaceManager maceManager = new MaceManager(slotManager, targetManager);
    private final AxeManager axeManager = new AxeManager(slotManager, targetManager);
    private final SpearManager spearManager = new SpearManager(slotManager);

    private boolean wasActiveLastTick = false;

    @Override
    public void onInitializeClient() {
        instance = this;
        ConfigManager.load();
        KeybindManager.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            resetAllSystems(client);
        });
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null || !client.player.isAlive()) {
            if (wasActiveLastTick) {
                resetAllSystems(client);
                wasActiveLastTick = false;
            }
            return;
        }

        SpareMazeConfig cfg = ConfigManager.getConfig();
        if (!cfg.masterEnable) {
            if (wasActiveLastTick) {
                resetAllSystems(client);
                wasActiveLastTick = false;
            }
            return;
        }

        KeybindManager.handleInput(client, pearlCatchingManager, maceManager, axeManager, spearManager);

        boolean rotActive = RotationController.isCameraLocked;
        boolean pearlActive = pearlCatchingManager.getState() != PearlCatchState.IDLE;
        boolean maceActive = maceManager.getActiveState() != MaceState.IDLE;
        boolean axeActive = axeManager.getInstantSlamManager().getState() != InstantSlamState.IDLE;
        boolean spearActive = spearManager.getSpearLungeManager().getState() != LungeState.IDLE;

        if (!rotActive && !pearlActive && !maceActive && !axeActive && !spearActive) {
            wasActiveLastTick = false;
            return;
        }

        wasActiveLastTick = true;
        TickUtils.onTick();

        if (rotActive) rotationController.tick(client);
        if (pearlActive) pearlCatchingManager.tick(client);
        if (maceActive) maceManager.tick(client);
        if (axeActive) axeManager.tick(client);
        if (spearActive) spearManager.tick(client);

        if (cfg.debugMode) {
            DebugLogger.tickDiagnostics(
                client,
                pearlCatchingManager,
                maceManager,
                axeManager,
                spearManager,
                slotManager,
                targetManager
            );
        }
    }

    public void resetAllSystems(MinecraftClient client) {
        pearlCatchingManager.cancel(client, "Reset");
        maceManager.cancelAll(client);
        axeManager.cancel(client);
        spearManager.cancel(client);
        rotationController.releaseLock();
        slotManager.forceRelease(client);
        targetManager.clearTarget();
    }

    public static SpareMazeClient getInstance() {
        return instance;
    }

    public SlotManager getSlotManager() {
        return slotManager;
    }

    public RotationController getRotationController() {
        return rotationController;
    }

    public TargetManager getTargetManager() {
        return targetManager;
    }

    public PearlCatchingManager getPearlCatchingManager() {
        return pearlCatchingManager;
    }

    public MaceManager getMaceManager() {
        return maceManager;
    }

    public AxeManager getAxeManager() {
        return axeManager;
    }

    public SpearManager getSpearManager() {
        return spearManager;
    }
}
