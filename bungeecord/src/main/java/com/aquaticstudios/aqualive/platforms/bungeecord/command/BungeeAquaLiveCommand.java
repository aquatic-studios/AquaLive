package com.aquaticstudios.aqualive.platforms.bungeecord.command;

import com.aquaticstudios.aqualive.platforms.bungeecord.BungeeSender;
import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class BungeeAquaLiveCommand extends Command implements TabExecutor {
    private final AquaLive core;
    private final BungeeAudiences audiences;

    public BungeeAquaLiveCommand(final AquaLive core, final BungeeAudiences audiences) {
        super("aqualive");
        this.core = core;
        this.audiences = audiences;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        core.admin().execute(new BungeeSender(sender, audiences), args);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (args.length > 1) return Collections.emptyList();

        final String typed = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        final List<String> out = new ArrayList<>();
        for (final String sub : core.admin().subCommands()) {
            if (sub.startsWith(typed)) out.add(sub);
        }
        return out;
    }
}
