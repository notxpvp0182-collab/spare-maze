package com.ahmed.sparemaze.controls;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

public final class KeybindCategory {
    public static final KeyBinding.Category MAIN_CATEGORY = KeyBinding.Category.create(Identifier.of("sparemaze", "main"));

    private KeybindCategory() {}
}
