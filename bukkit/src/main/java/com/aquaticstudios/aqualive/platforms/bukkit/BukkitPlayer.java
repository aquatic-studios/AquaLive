package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class BukkitPlayer implements AquaPlayer {
    private final Player player;
    private final BukkitAudiences audiences;

    public BukkitPlayer(final Player player, final BukkitAudiences audiences) {
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
        return null;
    }

    public Player bukkit() {
        return player;
    }
}
