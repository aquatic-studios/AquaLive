package com.aquaticstudios.aqualive.platforms.bungeecord.command;

import com.aquaticstudios.aqualive.platforms.bungeecord.BungeePlayer;
import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;

public final class BungeeLiveCommand extends Command implements TabExecutor {
    private final AquaLive core;
    private final BungeeAudiences audiences;

    public BungeeLiveCommand(final String alias, final AquaLive core, final BungeeAudiences audiences) {
        super(alias);
        this.core = core;
        this.audiences = audiences;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            audiences.sender(sender).sendMessage(
                    Component.text("Only players can announce a stream.", NamedTextColor.RED));
            return;
        }
        core.live().execute(new BungeePlayer(player, audiences), getName(), args);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        return Collections.emptyList();
    }
}
