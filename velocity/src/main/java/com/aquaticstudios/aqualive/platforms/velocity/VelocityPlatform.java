package com.aquaticstudios.aqualive.platforms.velocity;

import com.aquaticstudios.aqualive.shared.platform.AquaPlayer;
import com.aquaticstudios.aqualive.shared.platform.Platform;
import com.aquaticstudios.aqualive.shared.platform.PlatformType;
import com.aquaticstudios.aqualive.shared.platform.PluginLogger;
import com.aquaticstudios.aqualive.shared.platform.Scheduler;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public final class VelocityPlatform implements Platform {
    private final ProxyServer server;
    private final PluginLogger logger;
    private final Path dataFolder;

    public VelocityPlatform(final ProxyServer server, final Logger logger, final Path dataFolder) {
        this.server = server;
        this.logger = new VelocityLogger(logger);
        this.dataFolder = dataFolder;
    }

    @Override
    public PlatformType type() {
        return PlatformType.VELOCITY;
    }

    @Override
    public Collection<? extends AquaPlayer> onlinePlayers() {
        final Collection<AquaPlayer> users = new ArrayList<>();
        for (final Player player : server.getAllPlayers()) {
            users.add(new VelocityPlayer(player));
        }
        return users;
    }

    @Override
    public int maxPlayers() {
        return server.getConfiguration().getShowMaxPlayers();
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
        return dataFolder;
    }
}
