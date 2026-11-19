package com.ahmed.sparemaze.controls;

import com.ahmed.sparemaze.axe.AxeManager;
import com.ahmed.sparemaze.mace.MaceManager;
import com.ahmed.sparemaze.pearl.PearlCatchingManager;
import com.ahmed.sparemaze.spear.SpearManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeybindManager {
    public static KeyBinding pearlCatchingKey;
    public static KeyBinding swordToMaceKey;
    public static KeyBinding axeToMaceKey;
    public static KeyBinding spearToMaceKey;
    public static KeyBinding instantSlamKey;
    public static KeyBinding spearLungeKey;

    private KeybindManager() {}

    public static void register() {
        pearlCatchingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sparemaze.pearl_catching",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KeybindCategory.MAIN_CATEGORY
        ));

        swordToMaceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sparemaze.sword_mace",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KeybindCategory.MAIN_CATEGORY
        ));

        axeToMaceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sparemaze.axe_mace",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KeybindCategory.MAIN_CATEGORY
        ));

        spearToMaceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sparemaze.spear_mace",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KeybindCategory.MAIN_CATEGORY
        ));

        instantSlamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sparemaze.instant_slam",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KeybindCategory.MAIN_CATEGORY
        ));

        spearLungeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sparemaze.spear_lunge",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KeybindCategory.MAIN_CATEGORY
        ));
    }

    public static void handleInput(
        MinecraftClient client,
        PearlCatchingManager pearlManager,
        MaceManager maceManager,
        AxeManager axeManager,
        SpearManager spearManager
    ) {
        if (client.currentScreen != null) {
            return;
        }

        while (pearlCatchingKey.wasPressed()) {
            pearlManager.trigger(client);
        }

        while (swordToMaceKey.wasPressed()) {
            maceManager.triggerSwordToMace(client);
        }

        while (axeToMaceKey.wasPressed()) {
            maceManager.triggerAxeToMace(client);
        }

        while (spearToMaceKey.wasPressed()) {
            maceManager.triggerSpearToMace(client);
        }

        while (instantSlamKey.wasPressed()) {
            axeManager.triggerInstantSlam(client);
        }

        while (spearLungeKey.wasPressed()) {
            spearManager.triggerLunge(client);
        }
    }
}
