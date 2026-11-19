package com.ahmed.sparemaze;

import com.ahmed.sparemaze.gui.SpareMazeScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class SpareMazeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SpareMazeScreen::new;
    }
}
