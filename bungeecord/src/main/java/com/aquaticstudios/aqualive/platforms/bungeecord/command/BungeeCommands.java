package com.aquaticstudios.aqualive.platforms.bungeecord.command;

import com.aquaticstudios.aqualive.shared.AquaLive;
import com.aquaticstudios.aqualive.shared.platform.CommandSync;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public final class BungeeCommands implements CommandSync {
    private final Plugin plugin;
    private final AquaLive core;
    private final BungeeAudiences audiences;

    private final List<Command> live = new ArrayList<>();

    private BungeeCommands(final Plugin plugin, final AquaLive core, final BungeeAudiences audiences) {
        this.plugin = plugin;
        this.core = core;
        this.audiences = audiences;
    }

    public static void register(final Plugin plugin, final AquaLive core, final BungeeAudiences audiences) {
        final BungeeCommands commands = new BungeeCommands(plugin, core, audiences);

        ProxyServer.getInstance().getPluginManager()
                .registerCommand(plugin, new BungeeAquaLiveCommand(core, audiences));

        commands.sync(core.settings().aliases());
        core.commandSync(commands);
    }

    @Override
    public void sync(final List<String> aliases) {
        final PluginManager manager = ProxyServer.getInstance().getPluginManager();

        for (final Command command : live) {
            manager.unregisterCommand(command);
        }
        live.clear();

        for (final String alias : aliases) {
            final BungeeLiveCommand command = new BungeeLiveCommand(alias, core, audiences);
            manager.registerCommand(plugin, command);
            live.add(command);
        }
    }
}
