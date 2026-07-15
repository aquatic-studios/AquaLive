package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class BukkitEventListener implements Listener {
    private final AquaLive core;
    private final BukkitAudiences audiences;

    public BukkitEventListener(final AquaLive core, final BukkitAudiences audiences) {
        this.core = core;
        this.audiences = audiences;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        core.warmSkin(new BukkitPlayer(event.getPlayer(), audiences));
    }
}
