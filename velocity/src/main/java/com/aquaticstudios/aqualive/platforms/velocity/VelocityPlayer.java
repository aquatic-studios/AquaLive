package com.aquaticstudios.aqualive.platforms.velocity;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;

import java.util.UUID;

public final class VelocityPlayer implements AquaPlayer {
    private final Player player;
    private final Audience audience;

    public VelocityPlayer(final Player player) {
        this.player = player;
        this.audience = new VelocityAudience(player);
    }

    @Override
    public UUID uuid() {
        return player.getUniqueId();
    }

    @Override
    public String name() {
        return player.getUsername();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    @Override
    public Audience audience() {
        return audience;
    }

    @Override
    public String serverName() {
        return player.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse(null);
    }
}
