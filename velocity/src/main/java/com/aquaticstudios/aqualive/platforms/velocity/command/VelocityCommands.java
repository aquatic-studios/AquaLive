package com.aquaticstudios.aqualive.platforms.velocity.command;

import com.aquaticstudios.aqualive.shared.AquaLive;
import com.aquaticstudios.aqualive.shared.platform.CommandSync;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.List;

public final class VelocityCommands implements CommandSync {
    private final Object plugin;
    private final ProxyServer server;
    private final AquaLive core;

    private final List<String> live = new ArrayList<>();

    private VelocityCommands(final Object plugin, final ProxyServer server, final AquaLive core) {
        this.plugin = plugin;
        this.server = server;
        this.core = core;
    }

    public static void register(final Object plugin, final ProxyServer server, final AquaLive core) {
        final VelocityCommands commands = new VelocityCommands(plugin, server, core);
        final CommandManager manager = server.getCommandManager();

        manager.register(
                manager.metaBuilder("aqualive").plugin(plugin).build(),
                new VelocityAquaLiveCommand(core));

        commands.sync(core.settings().aliases());
        core.commandSync(commands);
    }

    @Override
    public void sync(final List<String> aliases) {
        final CommandManager manager = server.getCommandManager();

        for (final String alias : live) {
            manager.unregister(alias);
        }
        live.clear();

        for (final String alias : aliases) {
            manager.register(
                    manager.metaBuilder(alias).plugin(plugin).build(),
                    new VelocityLiveCommand(core));
            live.add(alias);
        }
    }
}
