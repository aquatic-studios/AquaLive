package com.aquaticstudios.aqualive.platforms.bungeecord;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public final class BungeePlayer implements AquaPlayer {
    private final ProxiedPlayer player;
    private final BungeeAudiences audiences;

    public BungeePlayer(final ProxiedPlayer player, final BungeeAudiences audiences) {
        this.player = player;
        this.audiences = audiences;
    }

    @Override
    public UUID uuid() {
        return player.getUniqueId();
    }

    @Override
    public String name() {
        return player.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    @Override
    public Audience audience() {
        return audiences.player(player);
    }

    @Override
    public String serverName() {
        return player.getServer() == null ? null : player.getServer().getInfo().getName();
    }
}
