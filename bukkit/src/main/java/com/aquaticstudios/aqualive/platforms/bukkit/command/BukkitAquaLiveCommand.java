package com.aquaticstudios.aqualive.platforms.bukkit.command;

import com.aquaticstudios.aqualive.platforms.bukkit.BukkitSender;
import com.aquaticstudios.aqualive.shared.AquaLive;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class BukkitAquaLiveCommand implements CommandExecutor, TabCompleter {
    private final AquaLive core;
    private final BukkitAudiences audiences;

    public BukkitAquaLiveCommand(final AquaLive core, final BukkitAudiences audiences) {
        this.core = core;
        this.audiences = audiences;
    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String label,
                             final String[] args) {
        core.admin().execute(new BukkitSender(sender, audiences), args);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender,
                                      final Command command,
                                      final String label,
                                      final String[] args) {
        if (args.length != 1) return Collections.emptyList();

        final String typed = args[0].toLowerCase(Locale.ROOT);
        final List<String> out = new ArrayList<>();
        for (final String sub : core.admin().subCommands()) {
            if (sub.startsWith(typed)) out.add(sub);
        }
        return out;
    }
}
