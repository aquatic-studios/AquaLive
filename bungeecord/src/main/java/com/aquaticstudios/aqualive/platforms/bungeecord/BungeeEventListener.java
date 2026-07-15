package com.aquaticstudios.aqualive.platforms.bungeecord;

import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class BungeeEventListener implements Listener {
    private final AquaLive core;
    private final BungeeAudiences audiences;

    public BungeeEventListener(final AquaLive core, final BungeeAudiences audiences) {
        this.core = core;
        this.audiences = audiences;
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent event) {
        core.warmSkin(new BungeePlayer(event.getPlayer(), audiences));
    }
}
