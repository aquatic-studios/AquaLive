package com.aquaticstudios.aqualive.shared.platform;

import com.aquaticstudios.aqualive.shared.hook.PlaceholderHook;

import java.nio.file.Path;
import java.util.Collection;

public interface Platform {
    PlatformType type();

    Collection<? extends AquaPlayer> onlinePlayers();

    int maxPlayers();

    Scheduler scheduler();

    PluginLogger logger();

    Path dataFolder();

    default PlaceholderHook placeholders() {
        return PlaceholderHook.NONE;
    }
}
