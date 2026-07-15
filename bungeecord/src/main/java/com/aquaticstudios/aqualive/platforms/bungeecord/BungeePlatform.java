package com.aquaticstudios.aqualive.platforms.bungeecord;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.JavaLogger;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.aquaticstudios.aqualive.shared.platform.PlatformType;
import com.aquaticstudios.aqualive.shared.platform.PluginLogger;
import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public final class BungeePlatform implements Platform {
    private final Plugin plugin;
    private final BungeeAudiences audiences;
    private final PluginLogger logger;

    public BungeePlatform(final Plugin plugin, final BungeeAudiences audiences) {
        this.plugin = plugin;
        this.audiences = audiences;
        this.logger = new JavaLogger(plugin.getLogger());
    }

    @Override
    public PlatformType type() {
        return PlatformType.BUNGEE;
    }

    @Override
    public Collection<? extends AquaPlayer> onlinePlayers() {
        final Collection<AquaPlayer> users = new ArrayList<>();
        for (final ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            users.add(new BungeePlayer(player, audiences));
        }
        return users;
    }

    @Override
    public int maxPlayers() {
        final int limit = ProxyServer.getInstance().getConfig().getPlayerLimit();
        return limit > 0 ? limit : ProxyServer.getInstance().getPlayers().size();
    }

    @Override
    public Scheduler scheduler() {
        return Runnable::run;
    }

    @Override
    public PluginLogger logger() {
        return logger;
    }

    @Override
    public Path dataFolder() {
        return plugin.getDataFolder().toPath();
    }
}
