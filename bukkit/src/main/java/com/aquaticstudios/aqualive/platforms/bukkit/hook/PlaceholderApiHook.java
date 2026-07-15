package com.aquaticstudios.aqualive.platforms.bukkit.hook;

import com.aquaticstudios.aqualive.platforms.bukkit.BukkitPlayer;
import com.aquaticstudios.aqualive.shared.hook.PlaceholderHook;
import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import me.clip.placeholderapi.PlaceholderAPI;

public final class PlaceholderApiHook implements PlaceholderHook {
    @Override
    public String resolve(final AquaPlayer user, final String text) {
        if (!(user instanceof BukkitPlayer)) return text;
        return PlaceholderAPI.setPlaceholders(((BukkitPlayer) user).bukkit(), text);
    }
}
