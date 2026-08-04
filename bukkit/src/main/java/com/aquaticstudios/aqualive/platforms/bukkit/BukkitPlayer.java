package com.aquaticstudios.aqualive.platforms.bukkit;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
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
    public void sendMessage(final Component message) {
        if (BukkitDisplay.sendMessage(player, message)) return;
        audience().sendMessage(message);
    }

    @Override
    public void showTitle(final Title title) {
        if (BukkitDisplay.showTitle(player, title)) return;
        audience().showTitle(title);
    }

    @Override
    public void sendActionBar(final Component message) {
        if (BukkitDisplay.sendActionBar(player, message)) return;
        audience().sendActionBar(message);
    }

    @Override
    public void showBossBar(final BossBar bar) {
        if (BukkitDisplay.showBossBar(player, bar)) return;
        audience().showBossBar(bar);
    }

    @Override
    public void hideBossBar(final BossBar bar) {
        if (BukkitDisplay.hideBossBar(player, bar)) return;
        audience().hideBossBar(bar);
    }

    @Override
    public String serverName() {
        return null;
    }

    public Player bukkit() {
        return player;
    }
}
