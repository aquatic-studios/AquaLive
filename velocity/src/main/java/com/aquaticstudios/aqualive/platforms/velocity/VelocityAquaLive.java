package com.aquaticstudios.aqualive.platforms.velocity;

import com.aquaticstudios.aqualive.platforms.velocity.command.VelocityAquaLiveCommand;
import com.aquaticstudios.aqualive.platforms.velocity.command.VelocityLiveCommand;
import com.aquaticstudios.aqualive.shared.AquaLive;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public final class VelocityAquaLive {
    private static final int BSTATS_ID = 32630;

    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final Path dataFolder;

    private AquaLive core;

    @Inject
    public VelocityAquaLive(final ProxyServer server,
                            final Logger logger,
                            final Metrics.Factory metricsFactory,
                            @DataDirectory final Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        try {
            core = AquaLive.start(new VelocityPlatform(server, logger, dataFolder, this));
        } catch (IOException ex) {
            logger.error("Could not read the configuration; AquaLive will stay idle", ex);
            return;
        }

        registerCommands();
        metricsFactory.make(this, BSTATS_ID);
    }

    @Subscribe
    public void onPostLogin(final PostLoginEvent event) {
        if (core != null) core.warmSkin(new VelocityPlayer(event.getPlayer()));
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        if (core != null) core.stop();
    }

    private void registerCommands() {
        final CommandManager commands = server.getCommandManager();

        commands.register(
                commands.metaBuilder("aqualive").plugin(this).build(),
                new VelocityAquaLiveCommand(core));

        for (final String alias : core.settings().aliases()) {
            commands.register(
                    commands.metaBuilder(alias).plugin(this).build(),
                    new VelocityLiveCommand(core));
        }
    }
}
